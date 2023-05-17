package com.equationl.githubapp.model.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.equationl.githubapp.common.net.PageInfo
import com.equationl.githubapp.model.bean.Issue
import com.equationl.githubapp.model.bean.SearchResult
import com.equationl.githubapp.model.conversion.IssueConversion
import com.equationl.githubapp.model.ui.IssueUIModel
import com.equationl.githubapp.service.IssueService
import com.equationl.githubapp.service.SearchService
import com.equationl.githubapp.ui.view.repos.issue.IssueState
import com.equationl.githubapp.ui.view.repos.issue.QueryParameter
import com.equationl.githubapp.util.fromJson
import retrofit2.HttpException

class RepoIssuePagingSource(
    private val issueService: IssueService,
    private val searchService: SearchService,
    private val queryParameter: QueryParameter,
): PagingSource<Int, IssueUIModel>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, IssueUIModel> {
        try {
            val nextPageNumber = params.key ?: 1  // 从第 1 页开始加载
            val response = if (queryParameter.queryString.isBlank()) {
                issueService.getRepoIssues(true, queryParameter.userName, queryParameter.repoName, nextPageNumber, queryParameter.state.originalName)
            } else {
                val q = if (queryParameter.state == IssueState.All) {
                    "${queryParameter.queryString}+repo:${queryParameter.userName}/${queryParameter.repoName}"
                } else {
                    "${queryParameter.queryString}+repo:${queryParameter.userName}/${queryParameter.repoName}+state:${queryParameter.state.originalName}"
                }
                searchService.searchIssues(true, q, nextPageNumber)
            }

            if (!response.isSuccessful) {
                return LoadResult.Error(HttpException(response))
            }
            val totalPage = response.headers()["page_info"]?.fromJson<PageInfo>()?.last ?: -1

            Log.i("el", "load: 总页数 = $totalPage")

            @Suppress("UNCHECKED_CAST")
            val issueUiModel = if (queryParameter.queryString.isBlank()) {
                val body = (response.body() ?: arrayListOf<Issue>()) as ArrayList<Issue>
                body.map { IssueConversion.issueToIssueUIModel(it) }
            } else {
                val body = (response.body() ?: SearchResult<Issue>()) as SearchResult<Issue>
                body.items?.map { IssueConversion.issueToIssueUIModel(it) }
            }

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