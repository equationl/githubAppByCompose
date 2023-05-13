package com.equationl.githubapp.model.ui

import java.util.UUID

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

    // 仅用于 LazyColumn 的 Key
    var lazyColumnKey: String = UUID.randomUUID().toString(),

    var isComment: Boolean = false
)


