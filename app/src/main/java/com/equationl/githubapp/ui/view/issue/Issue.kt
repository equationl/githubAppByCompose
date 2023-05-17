package com.equationl.githubapp.ui.view.issue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import com.equationl.githubapp.common.route.Route
import com.equationl.githubapp.common.utlis.copy
import com.equationl.githubapp.model.ui.IssueUIModel
import com.equationl.githubapp.ui.common.AvatarContent
import com.equationl.githubapp.ui.common.MoreMenu
import com.equationl.githubapp.ui.common.TopBar
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material.MaterialRichText
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.rememberMaterialDialogState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueDetailScreen(
    userName: String,
    repoName: String,
    issueNumber: Int,
    navController: NavHostController,
    viewModel: IssueViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val viewState = viewModel.viewStates
    val scaffoldState = rememberBottomSheetScaffoldState()
    val commentList = viewState.issueCommentFlow?.collectAsLazyPagingItems()
    val commentMenuDialogState: MaterialDialogState = rememberMaterialDialogState(false)
    val editDialogState: MaterialDialogState = rememberMaterialDialogState(false)

    var editIssue: IssueUIModel? by remember { mutableStateOf(null) }

    LaunchedEffect(commentList) {
        viewModel.viewEvents.collect {
            when (it) {
                is IssueEvent.ShowMsg -> {
                    scaffoldState.snackbarHostState.showSnackbar(message = it.msg)
                }

                is IssueEvent.Refresh -> {
                    commentList?.refresh()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.dispatch(IssueAction.LoadData(userName, repoName, issueNumber))
    }

    Scaffold(
        topBar = {
            var isShowDropMenu by remember { mutableStateOf(false) }

            TopBar(
                title = viewState.issueInfo.action,
                actions = {
                    IconButton(onClick = {
                        //navController.navigate("${Route.REPO_DETAIL}/$repoName/$userName")
                        navController.popBackStack("${Route.REPO_DETAIL}/$repoName/$userName", false)
                    }) {
                        Icon(Icons.Outlined.Home, "Home")
                    }

                    MoreMenu(
                        isShow = isShowDropMenu,
                        onDismissRequest = { isShowDropMenu = false },
                        onClick = {
                            viewModel.dispatch(IssueAction.ClickMoreMenu(context, it, userName, repoName, issueNumber))
                        }
                    )

                    IconButton(onClick = { isShowDropMenu = !isShowDropMenu}) {
                        Icon(Icons.Outlined.MoreHoriz, "More")
                    }
                }
            ) {
                navController.popBackStack()
            }
        },
        bottomBar = {
            BottomBar(
                issueUIModel = viewState.issueInfo,
                onChangeIssueStatus = { viewModel.dispatch(IssueAction.OnChangeIssueStatus(it)) },
                onChangeIssueLockStatus = { viewModel.dispatch(IssueAction.OnChangeIssueLockStatus(it)) },
                onClickAddComment = {
                    editIssue = null
                    editDialogState.show()
                },
                onClickEditIssue = {
                    editIssue = it
                    editDialogState.show()
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = scaffoldState.snackbarHostState) { snackBarData ->
                Snackbar(snackbarData = snackBarData)
            }
        }
    ) {
        var clickCommentItem: IssueUIModel? by remember { mutableStateOf(null) }

        CommentEditMenuDialog(
            commentItem = clickCommentItem,
            viewModel = viewModel,
            dialogState = commentMenuDialogState
        ) { commentItem ->
            editIssue = commentItem
            editDialogState.show()
        }

        EditDialog(
            issueUIModel = editIssue,
            editDialogState
        ) { tittle: String, content: String, operate: EditIssueDialogOperate ->
            when (operate) {
                EditIssueDialogOperate.AddComment -> { viewModel.dispatch(IssueAction.AddComment(content)) }
                EditIssueDialogOperate.EditComment -> { viewModel.dispatch(IssueAction.EditComment(content, clickCommentItem?.status ?: "")) }
                EditIssueDialogOperate.EditIssue -> { viewModel.dispatch(IssueAction.EditIssue(tittle, content)) }
            }
        }

        Column(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .padding(it)
        ) {
            IssueContent(commentList, viewState.issueInfo, navController, onError = {msg ->
                viewModel.dispatch(IssueAction.ShowMag(msg))
            }, onClickComment = { issueUuiModel ->
                clickCommentItem = issueUuiModel
                commentMenuDialogState.show()
            })
        }
    }

}

@Composable
private fun EditDialog(
    issueUIModel: IssueUIModel? = null,
    dialogState: MaterialDialogState,
    onPostDate: (tittle: String, content: String, from: EditIssueDialogOperate) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    if (issueUIModel != null) {
        if (issueUIModel.isComment) {
            title = ""
            content = issueUIModel.action
        }
        else {
            title = issueUIModel.action
            content = issueUIModel.content
        }
    }
    else {
        title = ""
        content = ""
    }

    val titleText: String
    val operate: EditIssueDialogOperate

    if (title.isEmpty()) {
        if (issueUIModel?.isComment == true) {
            titleText = "编辑回复"
            operate = EditIssueDialogOperate.EditComment
        }
        else {
            titleText = "回复"
            operate = EditIssueDialogOperate.AddComment
        }
    }
    else {
        titleText = "编辑"
        operate = EditIssueDialogOperate.EditIssue
    }

    MaterialDialog(
        dialogState = dialogState,
        autoDismiss = true,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {

                Text(text = titleText)
            }

            if (title.isNotEmpty()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it},
                    label = {
                        Text(text = "标题")
                    },
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = {
                    Text(text = "内容（支持 markdown）")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                TextButton(onClick = { dialogState.hide() }) {
                    Text(text = "取消")
                }

                Divider(modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp))

                TextButton(onClick = {
                    dialogState.hide()

                    onPostDate(title, content, operate)
                }) {
                    Text(text = "确定")
                }
            }
        }
    }
}

@Composable
private fun CommentEditMenuDialog(
    commentItem: IssueUIModel?,
    viewModel: IssueViewModel,
    dialogState: MaterialDialogState,
    onEditComment: (commentItem: IssueUIModel) -> Unit
) {
    val context = LocalContext.current

    MaterialDialog(
        dialogState = dialogState,
        autoDismiss = true
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = {
                dialogState.hide()
                if (commentItem != null) {
                    context.copy(commentItem.action)
                    viewModel.dispatch(IssueAction.ShowMag("已复制"))
                }
            }) {
                Text(text = "复制")
            }
            TextButton(onClick = {
                dialogState.hide()
                if (commentItem != null) {
                    onEditComment(commentItem)
                }
            }) {
                Text(text = "编辑")
            }
            TextButton(onClick = {
                dialogState.hide()
                if (commentItem != null) {
                    viewModel.dispatch(IssueAction.DelComment(commentItem))
                }
            }) {
                Text(text = "删除")
            }
        }
    }
}

@Composable
private fun IssueContent(
    commentList: LazyPagingItems<IssueUIModel>?,
    issueInfo: IssueUIModel,
    navController: NavHostController,
    onError: (msg: String) -> Unit,
    onClickComment: (issueUIModel: IssueUIModel) -> Unit
) {
    if (commentList == null) {
        Text(text = "need init...")
    }
    else {
        CommentRefreshContent(
            commentPagingItems = commentList,
            navController = navController,
            onLoadError = {
                onError(it)
            },
            onClickComment = onClickComment,
            headerItem = {
                item(key = "Header") {
                    Header(issueUIModel = issueInfo, navController = navController)
                }
            }
        )
    }
}

@Composable
private fun CommentRefreshContent(
    commentPagingItems: LazyPagingItems<IssueUIModel>,
    navController: NavHostController,
    onLoadError: (msg: String) -> Unit,
    onClickComment: (issueUIModel: IssueUIModel) -> Unit,
    headerItem: (LazyListScope.() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null
) {
    val rememberSwipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)

    if (commentPagingItems.loadState.refresh is LoadState.Error) {
        onLoadError("加载失败！")
    }

    if (commentPagingItems.itemCount < 1 && commentPagingItems.loadState.refresh == LoadState.Loading) {
        Text(text = "正在加载中…")
    }
    else {
        rememberSwipeRefreshState.isRefreshing = (commentPagingItems.loadState.refresh is LoadState.Loading)

        SwipeRefresh(
            state = rememberSwipeRefreshState,
            onRefresh = {
                commentPagingItems.refresh()
                onRefresh?.invoke()
            },
            modifier = Modifier.fillMaxSize()
        ) {
            IssueLazyColumn(
                commentPagingItems,
                navController,
                onClickComment,
                headerItem = headerItem
            )
        }
    }
}

@Composable
private fun IssueLazyColumn(
    commentPagingItems: LazyPagingItems<IssueUIModel>,
    navController: NavHostController,
    onClickComment: (eventUiModel: IssueUIModel) -> Unit,
    headerItem: (LazyListScope.() -> Unit)? = null,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 2.dp)
    ) {
        headerItem?.let { it() }

        itemsIndexed(commentPagingItems, key = { _, item -> item.lazyColumnKey}) { _, item ->
            if (item != null) {
                Column(modifier = Modifier.padding(8.dp)) {
                    CommentItem(
                        item,
                        navController
                    ) {
                        onClickComment(item)
                    }
                }
            }
        }

        if (commentPagingItems.itemCount < 1) {
            item {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "没有回复哦")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommentItem(
    issueUIModel: IssueUIModel,
    navController: NavHostController,
    onClickComment: (issueUIModel: IssueUIModel) -> Unit
) {
    Card(
        onClick = {
            onClickComment(issueUIModel)
        }
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarContent(
                        data = issueUIModel.image,
                        navHostController = navController,
                        userName = issueUIModel.username,
                    )

                    Text(text = issueUIModel.username, modifier = Modifier.padding(start = 4.dp))
                }

                Text(text = issueUIModel.time)
            }

            MaterialRichText(modifier = Modifier.padding(start = 30.dp)) {
                Markdown(content = issueUIModel.action)
            }
        }
    }

}

@Composable
private fun Header(
    issueUIModel: IssueUIModel,
    navController: NavHostController
) {
    Card {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarContent(
                    data = issueUIModel.image,
                    size = DpSize(90.dp, 90.dp),
                    navHostController = navController,
                    userName = issueUIModel.username
                )

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = issueUIModel.username,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Row {
                                Text(text = issueUIModel.status)
                                Text(text = "#${issueUIModel.issueNum}", modifier = Modifier.padding(start = 4.dp))
                                Icon(imageVector = Icons.Filled.Comment, contentDescription = "comment", modifier = Modifier.padding(start = 4.dp))
                                Text(text = issueUIModel.comment)
                            }
                        }

                        Text(text = issueUIModel.time)
                    }

                    Text(text = issueUIModel.action)
                }
            }

            MaterialRichText {
                Markdown(content = issueUIModel.content)
            }
        }
    }
}

@Composable
private fun BottomBar(
    issueUIModel: IssueUIModel,
    onChangeIssueStatus: (status: String) -> Unit,
    onChangeIssueLockStatus: (isLocked: Boolean) -> Unit,
    onClickEditIssue: (issueUiModel: IssueUIModel) -> Unit,
    onClickAddComment: () -> Unit
) {
    Divider(modifier = Modifier.fillMaxWidth())

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(onClick = {
            onChangeIssueLockStatus(!issueUIModel.locked)
        }) {
            if (issueUIModel.locked) {
                Text(text = "解锁")
            }
            else {
                Text(text = "锁定")
            }
        }

        TextButton(onClick = {
            onChangeIssueStatus(if (issueUIModel.status == "open") "closed" else "open")
        }) {
            if (issueUIModel.status == "open") {
                Text(text = "关闭")
            }
            else {
                Text(text = "打开")
            }
        }

        TextButton(onClick = { onClickEditIssue(issueUIModel) }) {
            Text(text = "编辑")
        }

        TextButton(onClick = onClickAddComment) {
            Text(text = "回复")
        }
    }
}