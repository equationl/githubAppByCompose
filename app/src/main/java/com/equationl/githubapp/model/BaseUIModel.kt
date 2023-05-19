package com.equationl.githubapp.model

import java.util.UUID

abstract class BaseUIModel {
    /**仅用于 LazyColumn 的key*/
    val lazyColumnKey: String = UUID.randomUUID().toString()
}