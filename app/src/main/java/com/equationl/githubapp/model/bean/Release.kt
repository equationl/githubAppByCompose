package com.equationl.githubapp.model.bean


import com.google.gson.annotations.SerializedName

import java.util.Date

data class Release(
    @SerializedName("assets")
    val assets: List<Asset>?,
    @SerializedName("assets_url")
    val assetsUrl: String,
    @SerializedName("author")
    val author: User,
    @SerializedName("body")
    val body: String?,
    @SerializedName("body_html")
    val bodyHtml: String?,
    @SerializedName("created_at")
    val createdAt: Date,
    @SerializedName("draft")
    val draft: Boolean,
    @SerializedName("html_url")
    val htmlUrl: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String?,
    @SerializedName("node_id")
    val nodeId: String,
    @SerializedName("prerelease")
    val prerelease: Boolean,
    @SerializedName("published_at")
    val publishedAt: Date?,
    @SerializedName("tag_name")
    val tagName: String,
    @SerializedName("tarball_url")
    val tarballUrl: String?,
    @SerializedName("target_commitish")
    val targetCommitish: String,
    @SerializedName("upload_url")
    val uploadUrl: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("zipball_url")
    val zipballUrl: String?
)

data class Asset(
    @SerializedName("browser_download_url")
    val browserDownloadUrl: String,
    @SerializedName("content_type")
    val contentType: String,
    @SerializedName("created_at")
    val createdAt: Date,
    @SerializedName("download_count")
    val downloadCount: Int,
    @SerializedName("id")
    val id: Int,
    @SerializedName("label")
    val label: Any,
    @SerializedName("name")
    val name: String,
    @SerializedName("node_id")
    val nodeId: String,
    @SerializedName("size")
    val size: Int,
    @SerializedName("state")
    val state: String,
    @SerializedName("updated_at")
    val updatedAt: Date,
    @SerializedName("uploader")
    val uploader: User,
    @SerializedName("url")
    val url: String
)
