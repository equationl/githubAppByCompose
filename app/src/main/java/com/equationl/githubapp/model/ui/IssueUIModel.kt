package com.equationl.githubapp.model.ui

import com.equationl.githubapp.model.BaseUIModel

/**
 * Issue相关UI类型
 */
data class IssueUIModel(
    var username: String = "---",

    var image: String = "",

    var action: String = "---",

    var time: String = "---",

    var comment: String = "---",

    var content: String = "---",

    var issueNum: Int = 0,

    var status: String = "---",

    var locked: Boolean = false,

    var isComment: Boolean = false
): BaseUIModel()


