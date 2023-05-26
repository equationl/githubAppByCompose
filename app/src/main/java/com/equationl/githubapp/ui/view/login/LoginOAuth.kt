package com.equationl.githubapp.ui.view.login

import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.equationl.githubapp.BuildConfig
import com.equationl.githubapp.common.config.AppConfig
import com.equationl.githubapp.ui.common.CustomWebView
import com.equationl.githubapp.ui.common.LoadItem
import com.equationl.githubapp.ui.common.TopBar
import kotlinx.coroutines.launch

private const val TAG = "el, OAuthLogin"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OAuthLoginScreen(
    navHostController: NavHostController,
    viewModel: LoginOauthViewModel = hiltViewModel()
) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    val coroutineState = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            if (it is LoginOauthViewEvent.ShowMessage) {
                coroutineState.launch {
                    scaffoldState.snackbarHostState.showSnackbar(message = it.message)
                }
            }
            else if (it is LoginOauthViewEvent.Goto) {
                navHostController.navigate(it.route)
            }
        }
    }

    Scaffold(
        topBar = {
            TopBar("授权登录") {
                navHostController.popBackStack()
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = scaffoldState.snackbarHostState) { snackBarData ->
                Snackbar(snackbarData = snackBarData)
            }
        }
    ) {
        Column(modifier = Modifier.padding(it)) {
            if (viewModel.viewStates.isRequestToken) {
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LoadItem("授权成功，正在登录")
                }
            }
            else {
                OAuthWebView(viewModel, navHostController)
            }
        }
    }
}

@Composable
fun OAuthWebView(
    viewModel: LoginOauthViewModel,
    navHostController: NavHostController,
) {
    val url = "https://github.com/login/oauth/authorize?client_id=${BuildConfig.CLIENT_ID}&state=app&redirect_uri=${AppConfig.AuthUri}"

    var rememberWebProgress: Int by remember { mutableStateOf(-1)}

    Box(Modifier.fillMaxSize()) {
        CustomWebView(
            url = url,
            istoInterceptBackKey = true,
            onBack = {
                if (it?.canGoBack() == true) {
                    it.goBack()
                }
                else {
                    navHostController.popBackStack()
                }
            },
            onShouldOverrideUrlLoading = { _: WebView?, request: WebResourceRequest? ->
                if (request != null && request.url != null &&
                    request.url.toString().startsWith(AppConfig.AuthUri)) {
                    val code = request.url.getQueryParameter("code")
                    if (code != null) {
                        Log.i(TAG, "OAuthLoginScreen: url=${request.url}")
                        viewModel.dispatch(LoginOauthViewAction.RequestToken(code))
                        true
                    }
                    else {
                        Log.i(TAG, "OAuthWebView: code 为空！")
                        viewModel.dispatch(LoginOauthViewAction.WebViewLoadError("参数读取错误！"))
                        false
                    }
                }
                else {
                    Log.i(TAG, "OAuthWebView: 地址不符合")
                    false
                }
            },
            onProgressChange = {progress ->
                rememberWebProgress = progress
            },
            onReceivedError = {
                Log.e(TAG, "OAuthWebView: 加载失败：code=${it?.errorCode}, des=${it?.description}")
                viewModel.dispatch(LoginOauthViewAction.WebViewLoadError(it?.description.toString()))
            },
            modifier = Modifier.fillMaxSize())

        LinearProgressIndicator(
            progress = rememberWebProgress * 1.0F / 100F,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .height(if (rememberWebProgress == 100) 0.dp else 5.dp))
    }
}