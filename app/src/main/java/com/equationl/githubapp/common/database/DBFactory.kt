package com.equationl.githubapp.common.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        DBTrendRepository::class,
        DBReceivedEvent::class,
        DBUserEvent::class,
        DBIssueDetail::class,
        DBIssueComment::class,
        DBUserRepos::class,
        DBUserStared::class,
        DBRepositoryFork::class,
        DBUserFollowed::class,
        DBUserFollower::class,
        DBRepositoryStar::class,
        DBRepositoryWatcher::class,
        DBRepositoryCommits::class,
        DBOrgMember::class,
        DBRepositoryIssue::class,
        DBRepositoryEvent::class,
        DBRepositoryDetail::class,
        DBRepositoryDetailReadme::class,
        DBUserInfo::class,
        DBRepositoryRelease::class
    ],
    version = 7,
    exportSchema = false
)
//@TypeConverters(DBConverters::class)
abstract class CacheDB : RoomDatabase() {
    companion object {
        fun create(context: Context, useInMemory: Boolean = false): CacheDB {
            val databaseBuilder = if (useInMemory) {
                Room.inMemoryDatabaseBuilder(context, CacheDB::class.java)
            } else {
                Room.databaseBuilder(context, CacheDB::class.java, "cache_data.db")
            }
            return databaseBuilder
                .fallbackToDestructiveMigration()
                .build()
        }
    }

    abstract fun cacheDB(): CacheDao
}