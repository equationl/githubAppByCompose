package com.equationl.githubapp.model.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.equationl.githubapp.common.net.PageInfo
import com.equationl.githubapp.model.conversion.EventConversion
import com.equationl.githubapp.model.ui.EventUIModel
import com.equationl.githubapp.service.UserService
import com.equationl.githubapp.util.fromJson
import retrofit2.HttpException

class DynamicPagingSource(
    private val userService: UserService,
    private val user: String,
    private val getUserEvent: Boolean = false
): PagingSource<Int, EventUIModel>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, EventUIModel> {
        try {
            val nextPageNumber = params.key ?: 1  // 从第 1 页开始加载
            val response =
                if (getUserEvent)
                    userService.getUserEvents(forceNetWork = true, user = user, page = nextPageNumber, per_page = params.loadSize)
                else userService.getNewsEvent(forceNetWork = true, user = user, page = nextPageNumber, per_page = params.loadSize)
            if (!response.isSuccessful) {
                return LoadResult.Error(HttpException(response))
            }
            val totalPage = response.headers()["page_info"]?.fromJson<PageInfo>()?.last ?: -1

            Log.i("el", "load: 总页数 = $totalPage")

            val uiEventModel = response.body()?.map { EventConversion.eventToEventUIModel(it) }

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