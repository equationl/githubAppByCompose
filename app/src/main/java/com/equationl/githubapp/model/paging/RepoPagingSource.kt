package com.equationl.githubapp.model.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
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
                    else -> {
                        repoServer.getUserPublicRepos(true, userName, nextPageNumber, sort?.requestValue ?: GeneralRepoListSort.Push.requestValue)
                    }
                }
            if (!response.isSuccessful) {
                return LoadResult.Error(HttpException(response))
            }
            val totalPage = response.headers()["page_info"]?.fromJson<PageInfo>()?.last ?: -1

            Log.i("el", "load: 总页数 = $totalPage")

            val uiEventModel = response.body()?.map { ReposConversion.reposToReposUIModel(it) }

            return LoadResult.Page(
                data = uiEventModel ?: listOf(),
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