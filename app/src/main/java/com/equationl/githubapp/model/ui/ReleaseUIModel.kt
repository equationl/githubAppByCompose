package com.equationl.githubapp.model.ui

import com.equationl.githubapp.model.BaseUIModel

data class ReleaseUIModel(
    var time: String? = null,
    var title: String? = null,
    var body: String? = null,
    var tarDownload: String? = null,
    var zipDownload: String? = null,
    var assert: List<ReleaseAssertUIModel> = listOf()
): BaseUIModel()

data class ReleaseAssertUIModel (
    var name: String? = null,
    var downloadLink: String? = null
)