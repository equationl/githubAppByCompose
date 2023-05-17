package com.equationl.githubapp.ui.view.person

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.equationl.githubapp.common.utlis.CommonUtils
import com.equationl.githubapp.common.utlis.browse
import com.equationl.githubapp.common.utlis.copy
import com.equationl.githubapp.common.utlis.share
import com.equationl.githubapp.model.bean.User
import com.equationl.githubapp.service.RepoService
import com.equationl.githubapp.service.UserService
import com.equationl.githubapp.ui.view.dynamic.DynamicViewEvent
import com.equationl.githubapp.ui.view.dynamic.DynamicViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonViewModel @Inject constructor(
    private val repoService: RepoService,
    private val userService: UserService,
): DynamicViewModel(userService) {
    override val isGetUserEvent = true

    var personViewState by mutableStateOf(PersonViewState(userInfo ?: User()))
        private set

    fun dispatch(action: PersonAction) {
        when (action) {
            is PersonAction.GetUser -> getUser(action.user)
            is PersonAction.ClickMoreMenu -> clickMoreMenu(action.context, action.pos, action.user)
        }
    }

    private fun clickMoreMenu(context: Context, pos: Int, user: String) {
        val realUrl = CommonUtils.getUserHtmlUrl(user)
        when (pos) {
            0 -> { // 在浏览器中打开
                context.browse(realUrl)
            }
            1 -> { // 复制链接
                context.copy(realUrl)
                _viewEvents.trySend(DynamicViewEvent.ShowMsg("已复制"))
            }
            2 -> { // 分享
                context.share(realUrl)
            }
        }
    }

    private fun getUser(user: String) {
        // TODO 没有判断打开的是组织还是用户
        // TODO 没有检查是否关注 checkFocus
        // TODO 没有根据用户名判断是否是当前登录用户
        viewModelScope.launch {
            personViewState = personViewState.copy(user = User())
            val response = userService.getUser(true, user)
            if (response.isSuccessful) {
                response.body()?.let {
                    CommonUtils.updateStar(it, repoService)
                    personViewState = personViewState.copy(user = it)
                }
            }
            else {
                val errorText = response.errorBody()?.string()
                _viewEvents.trySend(DynamicViewEvent.ShowMsg(errorText ?: "获取用户信息失败"))
            }
        }
    }
}

data class PersonViewState(
    val user: User
)

sealed class PersonAction {
    data class GetUser(val user: String) : PersonAction()
    data class ClickMoreMenu(val context: Context, val pos: Int, val user: String): PersonAction()
}