package com.equationl.githubapp.model.ui

import com.equationl.githubapp.model.BaseUIModel
import java.util.Date

class UserUIModel: BaseUIModel() {
    var login: String? = null


    var id: String? = null


    var name: String? = null


    var avatarUrl: String? = null


    var htmlUrl: String? = null


    var type: String? = null


    var company: String? = null


    var blog: String? = null


    var location: String? = null


    var email: String? = null


    var bio: String? = null

    var bioDes: String? = null


    var starRepos: String = ""

    var honorRepos: String = ""

    var publicRepos: String = ""

    var publicGists: Int = 0

    var followers: String = ""

    var following: String = ""

    var createdAt: Date? = null

    var updatedAt: Date? = null

    var actionUrl: String = ""
}