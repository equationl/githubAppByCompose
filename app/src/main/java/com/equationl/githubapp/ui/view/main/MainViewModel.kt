package com.equationl.githubapp.ui.view.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.equationl.githubapp.ui.common.BaseAction
import com.equationl.githubapp.ui.common.BaseEvent
import com.equationl.githubapp.ui.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    // private val dataBase: IssueDb
) : BaseViewModel() {

    var viewStates by mutableStateOf(MainViewState())
        private set

    fun dispatch(action: MainViewAction) {
        when (action) {
            is MainViewAction.ScrollTo -> scrollTo(action.pager)
            is MainViewAction.ChangeGesturesEnabled -> changeGesturesEnabled(action.enable)
        }
    }

    private fun changeGesturesEnabled(enable: Boolean) {
        viewStates = viewStates.copy(gesturesEnabled = enable)
    }

    private fun scrollTo(pager: MainPager) {
        viewStates = viewStates.copy(
            currentPage = pager,
            title = "GithubApp",
        )
    }
}

data class MainViewState(
    val title: String = "GithubApp",
    val currentPage: MainPager = MainPager.HOME_DYNAMIC,
    val gesturesEnabled: Boolean = true
)

sealed class MainViewEvent: BaseEvent() {
    data class Goto(val route: String): MainViewEvent()
}

sealed class MainViewAction: BaseAction() {
    data class ScrollTo(val pager: MainPager): MainViewAction()
    data class ChangeGesturesEnabled(val enable: Boolean): MainViewAction()
}

enum class MainPager {
    HOME_DYNAMIC,
    HOME_RECOMMEND,
    HOME_MY
}