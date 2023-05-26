package com.equationl.githubapp.model.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.equationl.githubapp.common.database.CacheDB
import com.equationl.githubapp.common.database.DBReceivedEvent
import com.equationl.githubapp.common.database.DBUserEvent
import com.equationl.githubapp.common.net.PageInfo
import com.equationl.githubapp.model.conversion.EventConversion
import com.equationl.githubapp.model.ui.EventUIModel
import com.equationl.githubapp.service.UserService
import com.equationl.githubapp.util.fromJson
import com.equationl.githubapp.util.toJson
import retrofit2.HttpException

class DynamicPagingSource(
    private val userService: UserService,
    private val user: String,
    private val getUserEvent: Boolean = false,
    private val dataBase: CacheDB,
    private val onLoadFirstPageSuccess: () -> Unit
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

            if (nextPageNumber == 1) { // 缓存第一页
                if (getUserEvent) {
                    val data = DBUserEvent(key = user, userName = user).apply { data = response.body()?.toJson() }
                    dataBase.cacheDB().insertUserEvent(data)
                }
                else {
                    val data = DBReceivedEvent(key = "receive_cache").apply { data = response.body()?.toJson() }
                    dataBase.cacheDB().insertReceiveEvent(data)
                }
                if (!uiEventModel.isNullOrEmpty()) {
                    onLoadFirstPageSuccess()
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