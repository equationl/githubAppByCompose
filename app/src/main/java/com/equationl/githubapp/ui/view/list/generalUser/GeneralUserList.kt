package com.equationl.githubapp.ui.view.list.generalUser

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.equationl.githubapp.common.route.Route
import com.equationl.githubapp.model.ui.UserUIModel
import com.equationl.githubapp.ui.common.AvatarContent
import com.equationl.githubapp.ui.common.BaseAction
import com.equationl.githubapp.ui.common.BaseEvent
import com.equationl.githubapp.ui.common.BaseRefreshPaging
import com.equationl.githubapp.ui.common.TopBar
import com.equationl.githubapp.ui.common.comPlaceholder
import com.equationl.githubapp.ui.view.list.GeneralListEnum
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralUserListScreen(
    repoName: String,
    userName: String,
    requestType: GeneralListEnum,
    navHostController: NavHostController,
    viewModel: GeneralUserListViewModel = hiltViewModel()
) {
    val viewState = viewModel.viewStates
    val scaffoldState = rememberBottomSheetScaffoldState()

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            when (it) {
                is BaseEvent.ShowMsg -> {
                    launch {
                        scaffoldState.snackbarHostState.showSnackbar(message = it.msg)
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.dispatch(GeneralUserListAction.SetData(userName, repoName, requestType))
    }

    Scaffold(
        topBar = {
            val title = remember {
                when (requestType) {
                    GeneralListEnum.UserFollower -> "粉丝"
                    GeneralListEnum.UserFollowed -> "关注"
                    GeneralListEnum.RepositoryStarUser -> "Star"
                    GeneralListEnum.RepositoryWatchUser -> "Watch"
                    else -> "Null"
                }
            }
            TopBar(
                title = title
            ) {
                navHostController.popBackStack()
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = scaffoldState.snackbarHostState) { snackBarData ->
                Snackbar(snackbarData = snackBarData)
            }
        }
    ) {
        val userList = viewState.userListFlow?.collectAsLazyPagingItems()

        Column(modifier = Modifier.padding(it)) {
            UserListContent(
                userPagingItems = userList,
                viewState.cacheUserList,
                isInit = viewModel.isInit,
                onLoadError = { msg ->
                    viewModel.dispatch(BaseAction.ShowMag(msg))
                },
                onClickItem = { userUiModel ->
                    navHostController.navigate("${Route.PERSON_DETAIL}/${userUiModel.login}")
                }
            )
        }
    }
}

@Composable
fun UserListContent(
    userPagingItems: LazyPagingItems<UserUIModel>?,
    cacheUserList: List<UserUIModel>?,
    isInit: Boolean,
    onLoadError: (msg: String) -> Unit,
    onClickItem: (userUiModel: UserUIModel) -> Unit,
    headerItem: (LazyListScope.() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null
) {
    if (userPagingItems?.itemCount == 0 && isInit && cacheUserList.isNullOrEmpty()) {
        return
    }

    BaseRefreshPaging(
        pagingItems = userPagingItems,
        cacheItems = cacheUserList,
        itemUi = { userUIModel, isRefresh ->
            UserItem(userUiModel = userUIModel, isRefresh = isRefresh) {
                onClickItem(it)
            }
        },
        onLoadError = onLoadError,
        onClickItem = {},
        onRefresh = onRefresh,
        headerItem = headerItem
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserItem(
    userUiModel: UserUIModel,
    isRefresh: Boolean,
    onClickItem: (user: UserUIModel) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp),
        onClick = {
            onClickItem(userUiModel)
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
            AvatarContent(
                data = userUiModel.avatarUrl ?: "",
                size = DpSize(35.dp, 35.dp),
                onClick = { onClickItem(userUiModel) },
                isRefresh = isRefresh
            )
            Column {
                Text(
                    text = userUiModel.login ?: "",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.comPlaceholder(isRefresh)
                )
                Row {
                    Icon(imageVector = Icons.Filled.Person, contentDescription = "person", modifier = Modifier.comPlaceholder(isRefresh))
                    Text(text = userUiModel.name ?: "", color = MaterialTheme.colorScheme.secondary, modifier = Modifier.comPlaceholder(isRefresh))
                }
            }
        }
    }
}