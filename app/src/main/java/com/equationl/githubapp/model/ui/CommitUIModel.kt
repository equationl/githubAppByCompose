package com.equationl.githubapp.model.ui

import com.equationl.githubapp.model.BaseUIModel


data class CommitUIModel(
    var userName: String = "",
    var des: String = "",
    var sha: String = "",
    var time: String = ""
): BaseUIModel()