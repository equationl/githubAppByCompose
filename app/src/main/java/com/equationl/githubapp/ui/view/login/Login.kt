package com.equationl.githubapp.ui.view.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.equationl.githubapp.common.route.Route

@Composable
fun LoginScreen(navHostController: NavHostController) {
    LoginContent(navHostController)
}

@Composable
fun LoginContent(navHostController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedButton(onClick = {
            navHostController.navigate(Route.OAuthLogin)
        }) {
            Text(text = "OAuth 授权登录")
        }
    }
}