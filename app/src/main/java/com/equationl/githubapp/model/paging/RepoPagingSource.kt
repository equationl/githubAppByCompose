package com.equationl.githubapp.model.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.equationl.githubapp.common.constant.LocalCache
import com.equationl.githubapp.common.net.PageInfo
import com.equationl.githubapp.model.conversion.ReposConversion
import com.equationl.githubapp.model.ui.ReposUIModel
import com.equationl.githubapp.service.RepoService
import com.equationl.githubapp.ui.view.list.GeneralListEnum
import com.equationl.githubapp.ui.view.list.GeneralRepoListSort
import com.equationl.githubapp.util.fromJson
import retrofit2.HttpException

class RepoPagingSource(
    private val repoServer: RepoService,
    private val userName: String,
    private val repoName: String,
    private val sort: GeneralRepoListSort?,
    private val requestType: GeneralListEnum
): PagingSource<Int, ReposUIModel>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ReposUIModel> {
        try {
            if (requestType == GeneralListEnum.UserHonor) { // 如果是荣誉列表则优先使用本地缓存
                Log.i("el", "load: 检查缓存：${LocalCache.UserHonorCacheList}")
                if (!LocalCache.UserHonorCacheList.isNullOrEmpty() && LocalCache.UserHonorCacheList?.get(0)?.owner?.login == userName) {
                    Log.i("el", "load: 缓存有效，优先使用缓存")

                    val uiEventModel = LocalCache.UserHonorCacheList?.map { ReposConversion.reposToReposUIModel(it) }

                    val resultList =  uiEventModel?.sortedByDescending {
                        it.repositoryStar.toIntOrNull() ?: 0
                    } ?: listOf()

                    return LoadResult.Page(
                        data = resultList,
                        prevKey = null,
                        nextKey = null
                    )
                }
            }

            val nextPageNumber = params.key ?: 1  // 从第 1 页开始加载
            val response =
                when (requestType) {
                    GeneralListEnum.UserRepository -> {
                        repoServer.getUserPublicRepos(true, userName, nextPageNumber, sort?.requestValue ?: GeneralRepoListSort.Push.requestValue)
                    }
                    GeneralListEnum.UserStar -> {
                        repoServer.getStarredRepos(true, userName, nextPageNumber, sort?.requestValue ?: GeneralRepoListSort.RecentlyStar.requestValue)
                    }
                    GeneralListEnum.RepositoryForkUser -> {
                        repoServer.getForks(true, userName, repoName, nextPageNumber)
                    }
                    GeneralListEnum.UserHonor -> {
                        repoServer.getUserRepository100StatusDao(true, userName, 1)
                    }
                    else -> {
                        repoServer.getUserPublicRepos(true, userName, nextPageNumber, sort?.requestValue ?: GeneralRepoListSort.Push.requestValue)
                    }
                }
            if (!response.isSuccessful) {
                return LoadResult.Error(HttpException(response))
            }
            val totalPage: Int
            if (requestType == GeneralListEnum.UserHonor) {
                totalPage = -1
                LocalCache.UserHonorCacheList = response.body() // 缓存结果
            }
            else {
                totalPage = response.headers()["page_info"]?.fromJson<PageInfo>()?.last ?: -1
                Log.i("el", "load: 总页数 = $totalPage")
            }

            val uiEventModel = response.body()?.map { ReposConversion.reposToReposUIModel(it) }

            val resultList = if (requestType == GeneralListEnum.UserHonor) {
                uiEventModel?.sortedByDescending {
                    it.repositoryStar.toIntOrNull() ?: 0
                } ?: listOf()
            } else {
                uiEventModel ?: listOf()
            }

            return LoadResult.Page(
                data = resultList,
                prevKey = null, // 设置为 null 表示只加载下一页
                nextKey = if (nextPageNumber >= totalPage || totalPage == -1) null else nextPageNumber + 1
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ReposUIModel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}