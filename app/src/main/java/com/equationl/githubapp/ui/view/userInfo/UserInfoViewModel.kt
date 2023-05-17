package com.equationl.githubapp.ui.view.userInfo

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewModelScope
import com.equationl.githubapp.common.utlis.CommonUtils
import com.equationl.githubapp.model.bean.User
import com.equationl.githubapp.model.bean.UserInfoRequestModel
import com.equationl.githubapp.service.RepoService
import com.equationl.githubapp.service.UserService
import com.equationl.githubapp.ui.common.BaseAction
import com.equationl.githubapp.ui.common.BaseEvent
import com.equationl.githubapp.ui.common.BaseViewModel
import com.equationl.githubapp.util.datastore.DataKey
import com.equationl.githubapp.util.datastore.DataStoreUtils
import com.equationl.githubapp.util.toJson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserInfoViewModel @Inject constructor(
    private val userService: UserService,
    private val repoService: RepoService
): BaseViewModel() {
    var viewStates by mutableStateOf(UserInfoState())
        private set

    fun dispatch(action: UserInfoAction) {
        when (action) {
            is UserInfoAction.UpdateUserInfo -> updateUserInfo()
            is UserInfoAction.EditUserInfo -> editUserInfo(action.type, action.content)
        }
    }

    private fun updateUserInfo() {
        viewModelScope.launch(exception) {
            val response = userService.getPersonInfo(true)
            if (response.isSuccessful) {
                val user = response.body()
                if (user == null) {
                    throw Exception("获取用户信息失败：response is null")
                }
                else {
                    viewStates = viewStates.copy(userInfo = user)
                }
            }
            else {
                throw Exception("获取用户信息失败：${response.errorBody()?.string()}")
            }
        }
    }

    private fun editUserInfo(type: UserInfoItemType, content: String) {
        val userInfoRequestModel = UserInfoRequestModel()
        when (type) {
            UserInfoItemType.Name -> userInfoRequestModel.name = content
            UserInfoItemType.Email -> userInfoRequestModel.email = content
            UserInfoItemType.Link -> userInfoRequestModel.blog = content
            UserInfoItemType.Company -> userInfoRequestModel.company = content
            UserInfoItemType.Location -> userInfoRequestModel.location = content
            UserInfoItemType.Bio -> userInfoRequestModel.bio = content
        }

        viewModelScope.launch(exception) {
            val response = userService.saveUserInfo(userInfoRequestModel)
            if (response.isSuccessful) {
                val user = response.body()
                if (user == null) {
                    _viewEvents.trySend(BaseEvent.ShowMsg("更新失败： 返回为空"))
                }
                else {
                    CommonUtils.updateStar(user, repoService) // 更新start数量
                    DataStoreUtils.saveStringData(DataKey.UserInfo, user.toJson())

                    viewStates = viewStates.copy(userInfo = user)

                    _viewEvents.trySend(BaseEvent.ShowMsg("已更新"))

                }
            }
            else {
                _viewEvents.trySend(BaseEvent.ShowMsg("更新失败：${response.errorBody()?.string()}"))
            }
        }
    }
}

data class UserInfoState(
    val userInfo: User = User()
)

sealed class UserInfoAction: BaseAction() {
    object UpdateUserInfo: UserInfoAction()
    data class EditUserInfo(val type: UserInfoItemType, val content: String): UserInfoAction()
}

enum class UserInfoItemType(val title: String, val Icon: ImageVector) {
    Name("姓名", Icons.Filled.Person),
    Email("邮箱", Icons.Filled.Email),
    Link("链接", Icons.Filled.Link),
    Company("公司", Icons.Filled.Business),
    Location("位置", Icons.Outlined.Place),
    Bio("简介", Icons.Filled.Description)
}

fun UserInfoItemType.getItemValue(user: User): String? {
    return when (this) {
        UserInfoItemType.Name -> user.name
        UserInfoItemType.Email -> user.email
        UserInfoItemType.Link -> user.blog
        UserInfoItemType.Company -> user.company
        UserInfoItemType.Location -> user.location
        UserInfoItemType.Bio -> user.bio
    }
}