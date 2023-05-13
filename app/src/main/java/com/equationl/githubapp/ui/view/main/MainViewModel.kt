package com.equationl.githubapp.ui.view.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    // private val dataBase: IssueDb
) : ViewModel() {

    var viewStates by mutableStateOf(HomeViewState())
        private set

    private val _viewEvents = Channel<HomeViewEvent>(Channel.BUFFERED)
    val viewEvents = _viewEvents.receiveAsFlow()

    fun dispatch(action: HomeViewAction) {
        when (action) {
            is HomeViewAction.ScrollTo -> scrollTo(action.pager)
            is HomeViewAction.ChangeGesturesEnabled -> changeGesturesEnabled(action.enable)
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

data class HomeViewState(
    val title: String = "GithubApp",
    val currentPage: MainPager = MainPager.HOME_DYNAMIC,
    val gesturesEnabled: Boolean = true
)

sealed class HomeViewEvent {
    data class Goto(val route: String): HomeViewEvent()
    data class ShowMessage(val message: String) : HomeViewEvent()
}

sealed class HomeViewAction {
    data class ScrollTo(val pager: MainPager): HomeViewAction()
    data class ChangeGesturesEnabled(val enable: Boolean): HomeViewAction()
}

enum class MainPager {
    HOME_DYNAMIC,
    HOME_RECOMMEND,
    HOME_MY
}