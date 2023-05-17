package com.equationl.githubapp.model.conversion

import com.equationl.githubapp.model.bean.User
import com.equationl.githubapp.model.ui.UserUIModel

/**
 * 用户相关实体转换
 * Created by guoshuyu
 * Date: 2018-10-23
 */
object UserConversion {

    fun userToUserUIModel(user: User): UserUIModel {
        val userUIModel = UserUIModel()
        userUIModel.login = user.login
        userUIModel.name = if (user.type == "User") {
            "personal"
        } else {
            "organization"
        }
        userUIModel.avatarUrl = user.avatarUrl
        return userUIModel

    }
}