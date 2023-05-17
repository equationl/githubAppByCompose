package com.equationl.githubapp.model.ui

import java.util.UUID


/**
 * 仓库相关UI类型
 */
class ReposUIModel {

    var ownerName: String = "--"

    var ownerPic: String = ""

    var repositoryName: String = "---"

    var repositoryStar: String = "---"

    var repositoryFork: String = "---"

    var repositoryWatch: String = "---"

    var hideWatchIcon: Boolean = true

    var repositoryType: String = "---"

    var repositoryDes: String = "--"

    var repositorySize: String = "--"

    var repositoryLicense: String = "--"

    var repositoryAction: String = "--"

    var repositoryIssue: String = "--"

    /**仅用于 LazyColumn 的key*/
    var lazyColumnKey: String = UUID.randomUUID().toString()
}