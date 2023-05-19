package com.equationl.githubapp.model.ui

import com.equationl.githubapp.model.BaseUIModel

/**
 * 事件相关UI实体
 */
data class EventUIModel(
    var id: String = "",
    var username: String = "",
    var image: String = "",
    var action: String = "",
    var des: String = "",
    var time: String = "---",
    var actionType: EventUIAction = EventUIAction.Person,
    var owner: String = "",
    var repositoryName: String = "",
    var IssueNum: Int = 0,
    var releaseUrl: String = "",
    var pushSha: ArrayList<String> = arrayListOf(),
    var pushShaDes: ArrayList<String> = arrayListOf(),
    var threadId: String = "",
): BaseUIModel()

/**
 * 事件相关UI类型
 */
enum class EventUIAction {
    Person,
    Repos,
    Push,
    Release,
    Issue
}

