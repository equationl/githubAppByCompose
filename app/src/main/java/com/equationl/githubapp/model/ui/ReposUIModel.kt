package com.equationl.githubapp.model.ui


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


    fun cloneFrom(reposUIModel: ReposUIModel) {
        ownerName = reposUIModel.ownerName
        if (ownerPic != reposUIModel.ownerPic) {
            ownerPic = reposUIModel.ownerPic
        }
        repositoryName = reposUIModel.repositoryName
        repositoryStar = reposUIModel.repositoryStar
        repositoryFork = reposUIModel.repositoryFork
        repositoryWatch = reposUIModel.repositoryWatch
        hideWatchIcon = reposUIModel.hideWatchIcon
        repositoryType = reposUIModel.repositoryType
        repositoryDes = reposUIModel.repositoryDes
        repositorySize = reposUIModel.repositorySize
        repositoryLicense = reposUIModel.repositoryLicense
        repositoryAction = reposUIModel.repositoryAction
        repositoryIssue = reposUIModel.repositoryIssue
    }
}