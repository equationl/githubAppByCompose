package com.equationl.githubapp.model.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.equationl.githubapp.common.database.CacheDB
import com.equationl.githubapp.common.database.DBRepositoryRelease
import com.equationl.githubapp.common.net.PageInfo
import com.equationl.githubapp.model.conversion.ReleaseConversion
import com.equationl.githubapp.model.ui.ReleaseUIModel
import com.equationl.githubapp.service.RepoService
import com.equationl.githubapp.util.fromJson
import com.equationl.githubapp.util.toJson
import retrofit2.HttpException

class ReleasePagingSource(
    private val repoService: RepoService,
    private val userName: String,
    private val repoName: String,
    private val isRelease: Boolean,
    private val dataBase: CacheDB,
    private val onLoadFirstPagSuccess: () -> Unit
): PagingSource<Int, ReleaseUIModel>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ReleaseUIModel> {
        try {
            val nextPageNumber = params.key ?: 1  // 从第 1 页开始加载
            val response = if (isRelease) {
                repoService.getReleases(true, userName, repoName, nextPageNumber)
            }
            else {
                repoService.getTags(userName, repoName, nextPageNumber)
            }

            if (!response.isSuccessful) {
                return LoadResult.Error(HttpException(response))
            }
            val totalPage = response.headers()["page_info"]?.fromJson<PageInfo>()?.last ?: -1

            Log.i("el", "load: 总页数 = $totalPage")

            val releaseUiModel = response.body()?.map { ReleaseConversion.releaseToReleaseUiModel(it) }

            if (nextPageNumber == 1) {
                dataBase.cacheDB().insertRepositoryRelease(
                    DBRepositoryRelease(
                        "$userName/$repoName/$isRelease",
                        "$userName/$repoName",
                        response.body()?.toJson(),
                        isRelease,)
                )
                if (!releaseUiModel.isNullOrEmpty()) {
                    onLoadFirstPagSuccess()
                }
            }

            return LoadResult.Page(
                data = releaseUiModel ?: listOf(),
                prevKey = null, // 设置为 null 表示只加载下一页
                nextKey = if (nextPageNumber >= totalPage || totalPage == -1) null else nextPageNumber + 1
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ReleaseUIModel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}