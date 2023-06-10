package com.equationl.githubapp.model.conversion

import com.equationl.githubapp.common.utlis.CommonUtils
import com.equationl.githubapp.model.bean.Release
import com.equationl.githubapp.model.ui.ReleaseAssertUIModel
import com.equationl.githubapp.model.ui.ReleaseUIModel

object ReleaseConversion {
    fun releaseToReleaseUiModel(release: Release): ReleaseUIModel {
        val releaseUIModel = ReleaseUIModel()
        if (release.publishedAt != null) {
            releaseUIModel.time = CommonUtils.getNewsTimeStr(release.publishedAt)
        }

        releaseUIModel.title = release.name ?: release.tagName
        releaseUIModel.body = release.body ?: ""

        releaseUIModel.tarDownload = release.tarballUrl
        releaseUIModel.zipDownload = release.zipballUrl

        if (!release.assets.isNullOrEmpty()) {
            releaseUIModel.assert = release.assets.map {
                ReleaseAssertUIModel(name = it.name, downloadLink = it.browserDownloadUrl)
            }
        }

        return releaseUIModel
    }
}