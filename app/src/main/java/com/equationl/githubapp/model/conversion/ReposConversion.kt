package com.equationl.githubapp.model.conversion

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.ui.graphics.Color
import com.equationl.githubapp.common.utlis.CommonUtils
import com.equationl.githubapp.common.utlis.HtmlUtils
import com.equationl.githubapp.model.bean.CommitFile
import com.equationl.githubapp.model.bean.FileModel
import com.equationl.githubapp.model.bean.RepoCommitExt
import com.equationl.githubapp.model.bean.Repository
import com.equationl.githubapp.model.bean.TrendingRepoModel
import com.equationl.githubapp.model.ui.FileUIModel
import com.equationl.githubapp.model.ui.PushUIModel
import com.equationl.githubapp.model.ui.ReposUIModel


/**
 * 仓库相关实体转换
 * Created by guoshuyu
 * Date: 2018-10-29
 */
object ReposConversion {

    fun trendToReposUIModel(trendModel: TrendingRepoModel): ReposUIModel {
        val reposUIModel = ReposUIModel()
        reposUIModel.hideWatchIcon = true
        reposUIModel.ownerName = trendModel.name
        reposUIModel.ownerPic = trendModel.contributors[0]
        reposUIModel.repositoryDes = trendModel.description
        reposUIModel.repositoryName = trendModel.reposName
        reposUIModel.repositoryFork = trendModel.forkCount
        reposUIModel.repositoryStar = trendModel.starCount
        reposUIModel.repositoryWatch = trendModel.meta
        reposUIModel.repositoryType = trendModel.language
        return reposUIModel
    }

    fun reposToReposUIModel(repository: Repository?): ReposUIModel {
        val reposUIModel = ReposUIModel()
        reposUIModel.hideWatchIcon = true
        reposUIModel.ownerName = repository?.owner?.login ?: ""
        reposUIModel.ownerPic = repository?.owner?.avatarUrl ?: ""
        reposUIModel.repositoryDes = repository?.description ?: ""
        reposUIModel.repositoryName = repository?.name ?: ""
        reposUIModel.repositoryFork = repository?.forksCount?.toString() ?: ""
        reposUIModel.repositoryStar = repository?.stargazersCount?.toString() ?: ""
        reposUIModel.repositoryWatch = repository?.subscribersCount?.toString() ?: ""
        reposUIModel.repositoryType = repository?.language ?: ""
        reposUIModel.repositorySize = (((repository?.size
                ?: 0) / 1024.0)).toString().substring(0, 3) + "M"
        reposUIModel.repositoryLicense = repository?.license?.name ?: ""


        val createStr = if (repository != null && repository.fork)
            "Forked from" + (repository.parent?.name ?: "")
        else
            "创建于" + CommonUtils.getDateStr(repository?.createdAt)

        reposUIModel.repositoryAction = createStr
        reposUIModel.repositoryIssue = repository?.openIssuesCount?.toString() ?: ""

        reposUIModel.repositoryTopics = repository?.topics ?: listOf()
        reposUIModel.repositoryLastUpdateTime = CommonUtils.getDateStr(repository?.pushedAt)
        reposUIModel.defaultBranch = repository?.defaultBranch
        return reposUIModel
    }

    fun fileListToFileUIList(list: ArrayList<FileModel>): List<FileUIModel> {
        val result = ArrayList<FileUIModel>()
        val dirs = ArrayList<FileUIModel>()
        val files = ArrayList<FileUIModel>()

        list.forEach {
            val fileUIModel = FileUIModel()
            fileUIModel.title = it.name ?: ""
            fileUIModel.type = it.type ?: ""
            if (it.type == "file") {
                fileUIModel.icon = Icons.Filled.FileOpen
                fileUIModel.next = ""
                files.add(fileUIModel)
            } else {
                fileUIModel.icon = Icons.Filled.FolderOpen
                fileUIModel.next = "{GSY-REPOS_ITEM_NEXT}"
                dirs.add(fileUIModel)
            }
        }
        result.addAll(dirs)
        result.addAll(files)
        return result
    }

    fun pushInfoToPushUIModel(commit: RepoCommitExt): PushUIModel {
        val pushUIModel = PushUIModel()
        var name = "---"
        var pic = "---"
        if (commit.committer != null) {
            name = commit.committer?.login ?: ""
        } else if (commit.commit != null && commit.commit?.author != null) {
            name = commit.commit?.author?.name ?: ""
        }
        if (commit.committer != null && commit.committer?.avatarUrl != null) {
            pic = commit.committer?.avatarUrl ?: ""
        }
        pushUIModel.pushUserName = name
        pushUIModel.pushImage = pic
        pushUIModel.pushDes = "Push at " + commit.commit?.message
        pushUIModel.pushTime = CommonUtils.getNewsTimeStr(commit.commit?.committer?.date)
        pushUIModel.pushEditCount = commit.files?.size?.toString() ?: ""
        pushUIModel.pushAddCount = commit.stats?.additions?.toString() ?: ""
        pushUIModel.pushReduceCount = commit.stats?.deletions?.toString() ?: ""
        return pushUIModel
    }

    fun repoCommitToFileUIModel(commit: CommitFile, backGroundColor: Color, primaryColor: Color): FileUIModel {
        val fileUIModel = FileUIModel()
        val filename = commit.fileName ?: ""
        val nameSplit = filename.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        fileUIModel.title = nameSplit[nameSplit.size - 1]
        fileUIModel.dir = filename
        fileUIModel.icon = Icons.Filled.FileOpen
        fileUIModel.sha = commit.sha ?: ""

        val html = HtmlUtils.generateCode2HTml(HtmlUtils.parseDiffSource(commit.patch ?: "", false), backGroundColor, primaryColor)
        fileUIModel.patch = html
        return fileUIModel
    }
}