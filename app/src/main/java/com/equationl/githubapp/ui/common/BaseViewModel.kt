package com.equationl.githubapp.ui.common

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

open class BaseViewModel : ViewModel() {
    open var isInit = false

    protected val _viewEvents = Channel<BaseEvent>(Channel.BUFFERED)
    val viewEvents = _viewEvents.receiveAsFlow()

    protected open val exception = CoroutineExceptionHandler { _, throwable ->
        viewModelScope.launch {
            Log.e("RVM", "Request Error: ", throwable)
            _viewEvents.send(BaseEvent.ShowMsg("错误："+throwable.message))
        }
    }

    open fun dispatch(action: BaseAction) {
        when (action) {
            is BaseAction.ShowMag -> {
                _viewEvents.trySend(BaseEvent.ShowMsg(action.msg))
            }
        }
    }

}

open class BaseAction {
    data class ShowMag(val msg: String): BaseAction()
}

open class BaseEvent {
    data class ShowMsg(val msg: String): BaseEvent()
}