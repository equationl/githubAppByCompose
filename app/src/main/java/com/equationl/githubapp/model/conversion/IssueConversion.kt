package com.equationl.githubapp.model.conversion

import com.equationl.githubapp.common.utlis.CommonUtils
import com.equationl.githubapp.model.bean.Issue
import com.equationl.githubapp.model.bean.IssueEvent
import com.equationl.githubapp.model.ui.IssueUIModel


/**
 * Issue相关实体转换
 * Created by guoshuyu
 * Date: 2018-10-29
 */
object IssueConversion {

    fun issueToIssueUIModel(issue: Issue): IssueUIModel {
        val issueUIModel = IssueUIModel()
        issueUIModel.username = issue.user?.login ?: ""
        issueUIModel.image = issue.user?.avatarUrl ?: ""
        issueUIModel.action = issue.title ?: ""
        issueUIModel.time = CommonUtils.getDateStr(issue.createdAt)
        issueUIModel.comment = issue.commentNum.toString()
        issueUIModel.issueNum = issue.number
        issueUIModel.status = issue.state ?: ""
        issueUIModel.content = issue.body ?: ""
        issueUIModel.locked = issue.locked
        return issueUIModel
    }


    fun issueEventToIssueUIModel(issue: IssueEvent): IssueUIModel {
        val issueUIModel = IssueUIModel()
        issueUIModel.username = issue.user?.login ?: ""
        issueUIModel.image = issue.user?.avatarUrl ?: ""
        issueUIModel.action = issue.body ?: ""
        issueUIModel.time = CommonUtils.getDateStr(issue.createdAt)
        issueUIModel.status = issue.id ?: ""
        issueUIModel.isComment = true
        return issueUIModel
    }

}