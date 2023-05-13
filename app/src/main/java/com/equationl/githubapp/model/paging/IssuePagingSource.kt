package com.equationl.githubapp.model.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.equationl.githubapp.common.net.PageInfo
import com.equationl.githubapp.model.conversion.IssueConversion
import com.equationl.githubapp.model.ui.IssueUIModel
import com.equationl.githubapp.service.IssueService
import com.equationl.githubapp.util.fromJson
import retrofit2.HttpException

class IssueCommentsPagingSource(
    private val userName: String,
    private val repoName: String,
    private val issueNumber: Int,
    private val issueService: IssueService
): PagingSource<Int, IssueUIModel>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, IssueUIModel> {
        try {
            val nextPageNumber = params.key ?: 1
            val response = issueService.getIssueComments(true, userName, repoName, issueNumber, nextPageNumber)

            if (!response.isSuccessful) {
                return LoadResult.Error(HttpException(response))
            }
            val totalPage = response.headers()["page_info"]?.fromJson<PageInfo>()?.last ?: -1

            Log.i("el", "load: 总页数 = $totalPage")

            val issueUiModel = response.body()?.map { IssueConversion.issueEventToIssueUIModel(it) }


            return LoadResult.Page(
                data = issueUiModel ?: listOf(),
                prevKey = null, // 设置为 null 表示只加载下一页
                nextKey = if (nextPageNumber >= totalPage || totalPage == -1) null else nextPageNumber + 1
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, IssueUIModel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}