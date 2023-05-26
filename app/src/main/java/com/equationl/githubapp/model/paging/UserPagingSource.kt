package com.equationl.githubapp.model.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.equationl.githubapp.common.database.CacheDB
import com.equationl.githubapp.common.database.DBOrgMember
import com.equationl.githubapp.common.database.DBRepositoryStar
import com.equationl.githubapp.common.database.DBRepositoryWatcher
import com.equationl.githubapp.common.database.DBUserFollowed
import com.equationl.githubapp.common.database.DBUserFollower
import com.equationl.githubapp.common.net.PageInfo
import com.equationl.githubapp.model.conversion.UserConversion
import com.equationl.githubapp.model.ui.UserUIModel
import com.equationl.githubapp.service.RepoService
import com.equationl.githubapp.service.UserService
import com.equationl.githubapp.ui.view.list.GeneralListEnum
import com.equationl.githubapp.util.fromJson
import com.equationl.githubapp.util.toJson
import retrofit2.HttpException

class UserPagingSource(
    private val userService: UserService,
    private val repoServer: RepoService,
    private val userName: String,
    private val repoName: String,
    private val requestType: GeneralListEnum,
    private val dataBase: CacheDB,
    private val onLoadFirstPagSuccess: () -> Unit
): PagingSource<Int, UserUIModel>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, UserUIModel> {
        try {
            val nextPageNumber = params.key ?: 1  // 从第 1 页开始加载
            val response =
                when (requestType) {
                    GeneralListEnum.UserFollower -> {
                        userService.getFollowers(true, userName, nextPageNumber)
                    }
                    GeneralListEnum.UserFollowed -> {
                        userService.getFollowing(true, userName, nextPageNumber)
                    }
                    GeneralListEnum.RepositoryStarUser -> {
                        repoServer.getStargazers(true, userName, repoName, nextPageNumber)
                    }
                    GeneralListEnum.RepositoryWatchUser -> {
                        repoServer.getWatchers(true, userName, repoName, nextPageNumber)
                    }
                    GeneralListEnum.OrgMembers -> {
                        userService.getOrgMembers(true, userName, nextPageNumber)
                    }
                    else -> {
                        userService.getFollowers(true, userName, nextPageNumber)
                    }
                }
            if (!response.isSuccessful) {
                return LoadResult.Error(HttpException(response))
            }
            val totalPage = response.headers()["page_info"]?.fromJson<PageInfo>()?.last ?: -1

            Log.i("el", "load: 总页数 = $totalPage")

            val userUIModels = response.body()?.map { UserConversion.userToUserUIModel(it) }

            if (nextPageNumber == 1) { // 缓存第一页数据
                when (requestType) {
                    GeneralListEnum.UserFollower -> {
                        dataBase.cacheDB().insertUserFollower(
                            DBUserFollower(
                                userName,
                                userName,
                                response.body()?.toJson()
                            )
                        )

                    }
                    GeneralListEnum.UserFollowed -> {
                        dataBase.cacheDB().insertUserFollowed(
                            DBUserFollowed(
                                userName,
                                userName,
                                response.body()?.toJson()
                            )
                        )

                    }
                    GeneralListEnum.RepositoryStarUser -> {
                        dataBase.cacheDB().insertRepositoryStar(
                            DBRepositoryStar(
                                "$userName/$repoName",
                                "$userName/$repoName",
                                response.body()?.toJson()
                            )
                        )

                    }
                    GeneralListEnum.RepositoryWatchUser -> {
                        dataBase.cacheDB().insertRepositoryWatcher(
                            DBRepositoryWatcher(
                                "$userName/$repoName",
                                "$userName/$repoName",
                                response.body()?.toJson()
                            )
                        )

                    }
                    GeneralListEnum.OrgMembers -> {
                        dataBase.cacheDB().insertOrgMember(
                            DBOrgMember(
                                userName,
                                userName,
                                response.body()?.toJson()
                            )
                        )

                    }
                    else -> { }
                }

                if (!userUIModels.isNullOrEmpty()) {
                    onLoadFirstPagSuccess()
                }
            }

            return LoadResult.Page(
                data = userUIModels ?: listOf(),
                prevKey = null, // 设置为 null 表示只加载下一页
                nextKey = if (nextPageNumber >= totalPage || totalPage == -1) null else nextPageNumber + 1
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, UserUIModel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}