package com.equationl.githubapp.model.ui

import com.equationl.githubapp.model.BaseUIModel


/**
 * 仓库相关UI类型
 */
class ReposUIModel: BaseUIModel() {

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
}