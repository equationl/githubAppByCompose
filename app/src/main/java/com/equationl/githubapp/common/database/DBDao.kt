package com.equationl.githubapp.common.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CacheDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrend(data: DBTrendRepository)

    @Query("SELECT * FROM trend_repository WHERE languageType = :languageType AND since = :since")
    suspend fun queryTrend(languageType: String, since: String): List<DBTrendRepository>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceiveEvent(data: DBReceivedEvent)

    @Query("SELECT * FROM received_event WHERE `key` = :key")
    suspend fun queryReceiveEvent(key: String = "receive_cache"): List<DBReceivedEvent>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserEvent(data: DBUserEvent)

    @Query("SELECT * FROM user_event WHERE userName = :userName")
    suspend fun queryUserEvent(userName: String): List<DBUserEvent>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIssueDetail(data: DBIssueDetail)

    @Query("SELECT * FROM issue_detail WHERE fullName = :fullName AND number = :number")
    suspend fun queryIssueDetail(fullName: String, number: String): List<DBIssueDetail>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIssueComment(data: DBIssueComment)

    @Query("SELECT * FROM issue_comment WHERE fullName = :fullName AND number = :number")
    suspend fun queryIssueComment(fullName: String, number: String): List<DBIssueComment>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserRepos(data: DBUserRepos)

    @Query("SELECT * FROM user_repos WHERE userName = :userName AND sort = :sort")
    suspend fun queryUserRepos(userName: String, sort: String): List<DBUserRepos>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStared(data: DBUserStared)

    @Query("SELECT * FROM user_stared WHERE userName = :userName AND sort = :sort")
    suspend fun queryUserStared(userName: String, sort: String): List<DBUserStared>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepositoryFork(data: DBRepositoryFork)

    @Query("SELECT * FROM repository_fork WHERE fullName = :fullName")
    suspend fun queryRepositoryFork(fullName: String): List<DBRepositoryFork>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserFollower(data: DBUserFollower)

    @Query("SELECT * FROM user_follower WHERE userName = :userName")
    suspend fun queryUserFollower(userName: String): List<DBUserFollower>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserFollowed(data: DBUserFollowed)

    @Query("SELECT * FROM user_followed WHERE userName = :userName")
    suspend fun queryUserFollowed(userName: String): List<DBUserFollowed>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepositoryStar(data: DBRepositoryStar)

    @Query("SELECT * FROM repository_star WHERE fullName = :fullName")
    suspend fun queryRepositoryStar(fullName: String): List<DBRepositoryStar>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepositoryWatcher(data: DBRepositoryWatcher)

    @Query("SELECT * FROM repository_watcher WHERE fullName = :fullName")
    suspend fun queryRepositoryWatcher(fullName: String): List<DBRepositoryWatcher>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrgMember(data: DBOrgMember)

    @Query("SELECT * FROM org_member WHERE org = :org")
    suspend fun queryOrgMember(org: String): List<DBOrgMember>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepositoryCommits(data: DBRepositoryCommits)

    @Query("SELECT * FROM repository_commits WHERE fullName = :fullName")
    suspend fun queryRepositoryCommits(fullName: String): List<DBRepositoryCommits>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepositoryIssue(data: DBRepositoryIssue)

    @Query("SELECT * FROM repository_issue WHERE fullName = :fullName AND state = :state")
    suspend fun queryRepositoryIssue(fullName: String, state: String): List<DBRepositoryIssue>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepositoryEvent(data: DBRepositoryEvent)

    @Query("SELECT * FROM repository_event WHERE fullName = :fullName")
    suspend fun queryRepositoryEvent(fullName: String): List<DBRepositoryEvent>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepositoryDetail(data: DBRepositoryDetail)

    @Query("SELECT * FROM repository_detail WHERE fullName = :fullName")
    suspend fun queryRepositoryDetail(fullName: String): List<DBRepositoryDetail>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepositoryDetailReadme(data: DBRepositoryDetailReadme)

    @Query("SELECT * FROM repository_readme WHERE fullName = :fullName")
    suspend fun queryRepositoryDetailReadme(fullName: String): List<DBRepositoryDetailReadme>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserInfo(data: DBUserInfo)

    @Query("SELECT * FROM user_info WHERE userName = :userName")
    suspend fun queryUserInfo(userName: String): List<DBUserInfo>?
}