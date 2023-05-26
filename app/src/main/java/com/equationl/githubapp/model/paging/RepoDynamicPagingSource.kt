package com.equationl.githubapp.model.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.equationl.githubapp.common.database.CacheDB
import com.equationl.githubapp.common.database.DBRepositoryEvent
import com.equationl.githubapp.common.net.PageInfo
import com.equationl.githubapp.model.conversion.EventConversion
import com.equationl.githubapp.model.ui.EventUIModel
import com.equationl.githubapp.service.RepoService
import com.equationl.githubapp.util.fromJson
import com.equationl.githubapp.util.toJson
import retrofit2.HttpException

class RepoDynamicPagingSource(
    private val repoService: RepoService,
    private val userName: String,
    private val repoName: String,
    private val dataBase: CacheDB,
    private val onLoadFirstPagSuccess: () -> Unit
): PagingSource<Int, EventUIModel>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, EventUIModel> {
        try {
            val nextPageNumber = params.key ?: 1  // 从第 1 页开始加载
            val response = repoService.getRepoEvent(
                forceNetWork = true,
                owner = userName,
                repo = repoName,
                page = nextPageNumber
            )
            if (!response.isSuccessful) {
                return LoadResult.Error(HttpException(response))
            }
            val totalPage = response.headers()["page_info"]?.fromJson<PageInfo>()?.last ?: -1

            Log.i("el", "load: 总页数 = $totalPage")

            val uiEventModel = response.body()?.map { EventConversion.eventToEventUIModel(it) }

            if (nextPageNumber == 1) {
                dataBase.cacheDB().insertRepositoryEvent(
                    DBRepositoryEvent(
                        "$userName/$repoName",
                        "$userName/$repoName",
                        response.body()?.toJson()
                    )
                )
                if (!uiEventModel.isNullOrEmpty()) {
                    onLoadFirstPagSuccess()
                }
            }

            return LoadResult.Page(
                data = uiEventModel ?: listOf(),
                prevKey = null, // 设置为 null 表示只加载下一页
                nextKey = if (nextPageNumber >= totalPage || totalPage == -1) null else nextPageNumber + 1
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, EventUIModel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}