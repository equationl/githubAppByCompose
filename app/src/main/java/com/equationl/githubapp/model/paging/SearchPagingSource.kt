package com.equationl.githubapp.model.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.equationl.githubapp.common.net.PageInfo
import com.equationl.githubapp.model.bean.Repository
import com.equationl.githubapp.model.bean.User
import com.equationl.githubapp.model.conversion.ReposConversion
import com.equationl.githubapp.model.conversion.UserConversion
import com.equationl.githubapp.service.SearchService
import com.equationl.githubapp.ui.view.search.SearchTab
import com.equationl.githubapp.util.fromJson
import retrofit2.HttpException

class SearchPagingSource(
    private val searchService: SearchService,
    private val query: String,
    private val order: String,
    private val sort: String,
    private val language: String,
    private val searchTab: SearchTab
): PagingSource<Int, Any>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Any> {
        try {

            if (query.isBlank()) {
                Log.w("el", "load: 没有搜索关键字")
                return LoadResult.Page(
                    data = listOf(),
                    prevKey = null, // 设置为 null 表示只加载下一页
                    nextKey = null
                )
            }

            var queryString = query
            if (searchTab == SearchTab.Repo) {
                if (language.isNotBlank()) {
                    queryString = "$queryString+language:$language"
                }
            }

            val nextPageNumber = params.key ?: 1  // 从第 1 页开始加载
            val response =
                when (searchTab) {
                    SearchTab.Repo -> searchService.searchRepos(queryString, sort, order, nextPageNumber)
                    SearchTab.User -> searchService.searchUsers(queryString, sort, order, nextPageNumber)
                }
            if (!response.isSuccessful) {
                return LoadResult.Error(HttpException(response))
            }
            val totalPage = response.headers()["page_info"]?.fromJson<PageInfo>()?.last ?: -1

            val searchResult = response.body() ?: return LoadResult.Error(HttpException(response))
            val resultUiModel = when (searchTab) {
                SearchTab.Repo -> {
                    searchResult.items?.map { ReposConversion.reposToReposUIModel(it as Repository) }
                }
                SearchTab.User -> {
                    searchResult.items?.map { UserConversion.userToUserUIModel(it as User) }
                }
            }

            Log.i("el", "load: 总页数 = $totalPage")

            return LoadResult.Page(
                data = resultUiModel ?: listOf(),
                prevKey = null, // 设置为 null 表示只加载下一页
                nextKey = if (nextPageNumber >= totalPage || totalPage == -1) null else nextPageNumber + 1
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Any>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}