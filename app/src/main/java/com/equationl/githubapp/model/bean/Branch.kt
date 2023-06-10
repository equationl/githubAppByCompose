package com.equationl.githubapp.model.bean

data class Branch(
    val name: String? = null,
    val zipballUrl: String? = null,
    val tarballUrl: String? = null,
    val isBranch: Boolean = true,


    val isClickAble: Boolean = true
)
