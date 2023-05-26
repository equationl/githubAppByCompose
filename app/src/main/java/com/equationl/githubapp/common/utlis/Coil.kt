package com.equationl.githubapp.common.utlis

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache

fun Context.getImageLoader(): ImageLoader {
    return ImageLoader.Builder(this)
        .respectCacheHeaders(false) // 禁用网络的缓存政策，不然不会拿本地缓存
        .memoryCache {
            MemoryCache.Builder(this)
                .maxSizePercent(0.25)
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(imgCachePath(this))
                .maxSizeBytes(100 * 1024 * 1024)
                .build()
        }
        .build()
}