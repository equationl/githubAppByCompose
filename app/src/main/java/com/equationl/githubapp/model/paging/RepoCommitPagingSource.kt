package com.equationl.githubapp.model.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.equationl.githubapp.common.database.CacheDB
import com.equationl.githubapp.common.database.DBRepositoryCommits
import com.equationl.githubapp.common.net.PageInfo
import com.equationl.githubapp.model.conversion.EventConversion
import com.equationl.githubapp.model.ui.CommitUIModel
import com.equationl.githubapp.service.CommitService
import com.equationl.githubapp.util.fromJson
import com.equationl.githubapp.util.toJson
import retrofit2.HttpException

class RepoCommitPagingSource(
    private val commitService: CommitService,
    private val userName: String,
    private val repoName: String,
    private val branch: String?,
    private val dataBase: CacheDB,
    private val onLoadFirstPageSuccess: () -> Unit
): PagingSource<Int, CommitUIModel>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CommitUIModel> {
        try {
            val nextPageNumber = params.key ?: 1  // 从第 1 页开始加载
            val response = commitService.getRepoCommits(
                forceNetWork = true,
                owner = userName,
                repo = repoName,
                page = nextPageNumber,
                branch = if (branch.isNullOrEmpty()) "master" else branch
            )
            if (!response.isSuccessful) {
                return LoadResult.Error(HttpException(response))
            }
            val totalPage = response.headers()["page_info"]?.fromJson<PageInfo>()?.last ?: -1

            Log.i("el", "load: 总页数 = $totalPage")

            val commitUiModel = response.body()?.map { EventConversion.commitToCommitUIModel(it) }

            if (nextPageNumber == 1) { // 缓存第一页数据
                dataBase.cacheDB().insertRepositoryCommits(
                    DBRepositoryCommits(
                        "$userName/$repoName/$branch",
                        "$userName/$repoName",
                        branch,
                        response.body()?.toJson()
                    )
                )

                if (!commitUiModel.isNullOrEmpty()) {
                    onLoadFirstPageSuccess()
                }
            }

            return LoadResult.Page(
                data = commitUiModel ?: listOf(),
                prevKey = null, // 设置为 null 表示只加载下一页
                nextKey = if (nextPageNumber >= totalPage || totalPage == -1) null else nextPageNumber + 1
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, CommitUIModel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}