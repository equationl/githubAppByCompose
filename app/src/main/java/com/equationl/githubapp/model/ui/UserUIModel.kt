package com.equationl.githubapp.model.ui

import java.util.Date
import java.util.UUID

class UserUIModel {
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

    // 仅用于 lazyColumn key
    val lazyColumnKey: String = UUID.randomUUID().toString()
}