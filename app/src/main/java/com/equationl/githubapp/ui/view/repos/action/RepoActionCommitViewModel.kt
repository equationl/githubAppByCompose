package com.equationl.githubapp.ui.view.repos.action

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.equationl.githubapp.common.config.AppConfig
import com.equationl.githubapp.common.database.CacheDB
import com.equationl.githubapp.model.bean.RepoCommit
import com.equationl.githubapp.model.conversion.EventConversion
import com.equationl.githubapp.model.paging.RepoCommitPagingSource
import com.equationl.githubapp.model.ui.CommitUIModel
import com.equationl.githubapp.service.CommitService
import com.equationl.githubapp.ui.common.BaseAction
import com.equationl.githubapp.ui.common.BaseEvent
import com.equationl.githubapp.ui.common.BaseViewModel
import com.equationl.githubapp.util.fromJson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RepoActionCommitViewModel @Inject constructor(
    private val commitService: CommitService,
    private val dataBase: CacheDB
): BaseViewModel() {

    private var commitFlow: Flow<PagingData<CommitUIModel>>? = null

    var viewStates by mutableStateOf(RepoActionCommitState(commitFlow = commitFlow))
        private set

    fun dispatch(action: RepoActionCommitAction) {
        when (action) {
            is RepoActionCommitAction.ShowMsg -> { _viewEvents.trySend(BaseEvent.ShowMsg(action.msg)) }
            is RepoActionCommitAction.SetData -> setData(action.owner, action.repo)
        }
    }

    private fun setData(owner: String, repo: String) {
        if (isInit) return

        viewModelScope.launch(exception) {
            val cacheData = dataBase.cacheDB().queryRepositoryCommits("$owner/$repo")
            if (!cacheData.isNullOrEmpty()) {
                val body = cacheData[0].data?.fromJson<List<RepoCommit>>()
                if (body != null) {
                    Log.i("el", "refreshData: 使用缓存数据")
                    viewStates = viewStates.copy(cacheCommitList = body.map { EventConversion.commitToCommitUIModel(it) })
                }
            }

            commitFlow = Pager(
                PagingConfig(pageSize = AppConfig.PAGE_SIZE, initialLoadSize = AppConfig.PAGE_SIZE)
            ) {
                RepoCommitPagingSource(commitService, owner, repo, dataBase) {
                    viewStates = viewStates.copy(cacheCommitList = null)
                    isInit = true
                }
            }.flow.cachedIn(viewModelScope)

            viewStates = viewStates.copy(commitFlow = commitFlow)
        }
    }
}

data class RepoActionCommitState(
    val commitFlow: Flow<PagingData<CommitUIModel>>? = null,
    val cacheCommitList: List<CommitUIModel>? = null
)

sealed class RepoActionCommitAction: BaseAction() {
    data class ShowMsg(val msg: String): RepoActionCommitAction()
    data class SetData(val owner: String, val repo: String): RepoActionCommitAction()
}