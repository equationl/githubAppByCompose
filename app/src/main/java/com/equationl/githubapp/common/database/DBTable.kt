package com.equationl.githubapp.common.database

import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * 仓库提交信息表
 */

@Entity(tableName = "repository_commits")
data class DBRepositoryCommits (
    @PrimaryKey
    var key: String,
    var fullName: String? = null,
    var data: String? = null
)

@Entity(tableName = "repository_watcher")
data class DBRepositoryWatcher(
    @PrimaryKey
    var key: String,
    var fullName: String,
    var data: String?,
)


@Entity(tableName = "repository_star")
data class DBRepositoryStar(
    @PrimaryKey
    var key: String,
    var fullName: String,
    var data: String?,
)


@Entity(tableName = "repository_fork")
data class DBRepositoryFork(
    @PrimaryKey
    var key: String,
    var fullName: String,
    var data: String?,
)


@Entity(tableName = "repository_detail")
data class DBRepositoryDetail(
    @PrimaryKey
    var key: String,
    var fullName: String?,
    var data: String?,
    var branch: String?,
)


@Entity(tableName = "repository_readme")
data class DBRepositoryDetailReadme(
    @PrimaryKey
    var key: String,
    var fullName: String?,
    var data: String?,
    var branch: String?,

)


@Entity(tableName = "repository_event")
data class DBRepositoryEvent(
    @PrimaryKey
    var key: String,
    var fullName: String?,
    var data: String?,
)


@Entity(tableName = "repository_issue")
data class DBRepositoryIssue(
    @PrimaryKey
    var key: String,
    var fullName: String?,
    var data: String?,
    var state: String?,
)


@Entity(tableName = "trend_repository")
data class DBTrendRepository(
    @PrimaryKey
    var key: String,
    var languageType: String?,
    var data: String?,
    var since: String?,
)


@Entity(tableName = "user_info")
data class DBUserInfo(
    @PrimaryKey
    var key: String,
    var userName: String?,
    var data: String?,
)


@Entity(tableName = "user_follower")
data class DBUserFollower(
    @PrimaryKey
    var key: String,
    var userName: String?,
    var data: String?,
)


@Entity(tableName = "user_followed")
data class DBUserFollowed(
    @PrimaryKey
    var key: String,
    var userName: String?,
    var data: String?,
)


@Entity(tableName = "org_member")
data class DBOrgMember(
    @PrimaryKey
    var key: String,
    var org: String?,
    var data: String?,
)


@Entity(tableName = "user_stared")
data class DBUserStared(
    @PrimaryKey
    var key: String,
    var userName: String?,
    var data: String?,
    var sort: String?,
)


@Entity(tableName = "user_repos")
data class DBUserRepos(
    @PrimaryKey
    var key: String,
    var userName: String?,
    var data: String?,
    var sort: String?,
)


@Entity(tableName = "received_event")
data class DBReceivedEvent(
    @PrimaryKey
    var key: String
): DBBaseTable()


@Entity(tableName = "user_event")
data class DBUserEvent(
    @PrimaryKey
    var key: String,
    var userName: String?
): DBBaseTable()


@Entity(tableName = "issue_detail")
data class DBIssueDetail(
    @PrimaryKey
    var key: String,
    var fullName: String?,
    var number: String?,
    var data: String?,
)


@Entity(tableName = "issue_comment")
data class DBIssueComment(
    @PrimaryKey
    var key: String,
    var fullName: String?,
    var number: String?,
    var commentId: String?,
    var data: String?,
)

open class DBBaseTable(
    var data: String? = null
)