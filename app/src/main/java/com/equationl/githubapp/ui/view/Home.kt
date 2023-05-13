package com.equationl.githubapp.ui.view

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.equationl.githubapp.common.route.Route
import com.equationl.githubapp.common.route.RouteParams
import com.equationl.githubapp.ui.view.code.CodeDetailScreen
import com.equationl.githubapp.ui.view.image.ImageScreen
import com.equationl.githubapp.ui.view.issue.IssueDetailScreen
import com.equationl.githubapp.ui.view.login.LoginScreen
import com.equationl.githubapp.ui.view.login.OAuthLoginScreen
import com.equationl.githubapp.ui.view.main.MainScreen
import com.equationl.githubapp.ui.view.person.PersonScreen
import com.equationl.githubapp.ui.view.push.PushDetailScreen
import com.equationl.githubapp.ui.view.repos.RepoDetailScreen
import com.equationl.githubapp.ui.view.search.SearchScreen
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeNavHost() {
    val navController = rememberAnimatedNavController()
    AnimatedNavHost(navController, Route.LOGIN) {

        // 登录
        composable(Route.LOGIN) {
            Column(Modifier.systemBarsPadding()) {
                LoginScreen(navHostController = navController)
            }
        }

        // OAuth 登录
        composable(Route.OAuthLogin) {
            Column(Modifier.systemBarsPadding()) {
                OAuthLoginScreen(navHostController = navController)
            }
        }

        composable(Route.MAIN) {
            Column(Modifier.systemBarsPadding()) {
                MainScreen(navController)
            }
        }

        composable(Route.SEARCH) {
            Column(Modifier.systemBarsPadding()) {
                SearchScreen()
            }
        }

        composable("${Route.REPO_DETAIL}/{${RouteParams.PAR_REPO_PATH}}/{${RouteParams.PAR_REPO_OWNER}}",
            arguments = listOf(
                navArgument(RouteParams.PAR_REPO_PATH) {
                    type = NavType.StringType
                    nullable = false
                },
                navArgument(RouteParams.PAR_REPO_OWNER) {
                    type = NavType.StringType
                    nullable = false
                }
            )) {
            val argument = requireNotNull(it.arguments)
            val repoPath = argument.getString(RouteParams.PAR_REPO_PATH)
            val repoOwner = argument.getString(RouteParams.PAR_REPO_OWNER)

            Column(Modifier.systemBarsPadding()) {
                RepoDetailScreen(navController, repoName = repoPath, repoOwner = repoOwner)
            }
        }

        composable("${Route.ISSUE_DETAIL}/{${RouteParams.PAR_REPO_PATH}}/{${RouteParams.PAR_REPO_OWNER}}/{${RouteParams.PAR_ISSUE_NUM}}",
            arguments = listOf(
                navArgument(RouteParams.PAR_REPO_PATH) {
                    type = NavType.StringType
                    nullable = false
                },
                navArgument(RouteParams.PAR_REPO_OWNER) {
                    type = NavType.StringType
                    nullable = false
                },
                navArgument(RouteParams.PAR_ISSUE_NUM) {
                    type = NavType.IntType
                    nullable = false
                }
            )) {
            val argument = requireNotNull(it.arguments)
            val repoPath = argument.getString(RouteParams.PAR_REPO_PATH)
            val repoOwner = argument.getString(RouteParams.PAR_REPO_OWNER)
            val issueNumber = argument.getInt(RouteParams.PAR_ISSUE_NUM)

            Column(Modifier.systemBarsPadding()) {
                IssueDetailScreen(
                    userName = repoOwner ?: "",
                    repoName = repoPath ?: "",
                    issueNumber = issueNumber,
                    navController = navController
                )
            }
        }

        composable("${Route.PUSH_DETAIL}/{${RouteParams.PAR_REPO_PATH}}/{${RouteParams.PAR_REPO_OWNER}}/{${RouteParams.PAR_PUSH_SHA}}",
            arguments = listOf(
                navArgument(RouteParams.PAR_REPO_PATH) {
                    type = NavType.StringType
                    nullable = false
                },
                navArgument(RouteParams.PAR_REPO_OWNER) {
                    type = NavType.StringType
                    nullable = false
                },
                navArgument(RouteParams.PAR_PUSH_SHA) {
                    type = NavType.StringType
                    nullable = false
                }
            )) {
            val argument = requireNotNull(it.arguments)
            val repoPath = argument.getString(RouteParams.PAR_REPO_PATH)
            val repoOwner = argument.getString(RouteParams.PAR_REPO_OWNER)
            val pushSha = argument.getString(RouteParams.PAR_PUSH_SHA)

            Column(Modifier.systemBarsPadding()) {
                PushDetailScreen(userName = repoOwner ?: "", repoName = repoPath ?: "", sha = pushSha ?: "", navController)
            }
        }

        composable("${Route.CODE_DETAIL}/{${RouteParams.PAR_REPO_PATH}}/{${RouteParams.PAR_REPO_OWNER}}/{${RouteParams.PAR_FILE_PATH}}/{${RouteParams.PAR_LOCAL_CODE}}/{${RouteParams.PAR_URL}}",
            arguments = listOf(
                navArgument(RouteParams.PAR_REPO_PATH) {
                    type = NavType.StringType
                    nullable = false
                },
                navArgument(RouteParams.PAR_REPO_OWNER) {
                    type = NavType.StringType
                    nullable = false
                },
                navArgument(RouteParams.PAR_FILE_PATH) {
                    type = NavType.StringType
                    nullable = false
                },
                navArgument(RouteParams.PAR_LOCAL_CODE) {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument(RouteParams.PAR_URL) {
                    type = NavType.StringType
                    nullable = true // TODO 这里的可空用错了
                },
            )) {
            val argument = requireNotNull(it.arguments)
            val repoPath = argument.getString(RouteParams.PAR_REPO_PATH)
            val repoOwner = argument.getString(RouteParams.PAR_REPO_OWNER)
            val filePath = argument.getString(RouteParams.PAR_FILE_PATH)
            val localCode = argument.getString(RouteParams.PAR_LOCAL_CODE)
            val url = argument.getString(RouteParams.PAR_URL)

            Column(Modifier.systemBarsPadding()) {
                CodeDetailScreen(
                    userName = repoOwner ?: "",
                    reposName = repoPath ?: "",
                    path = filePath ?: "",
                    localCode = localCode,
                    url = if (url == "null") "" else url,
                    navController = navController
                )
            }
        }

        composable("${Route.PERSON_DETAIL}/{${RouteParams.PAR_USER_NAME}}",
            arguments = listOf(
                navArgument(RouteParams.PAR_USER_NAME) {
                    type = NavType.StringType
                    nullable = false
                }
            )) {
            val argument = requireNotNull(it.arguments)
            val userName = argument.getString(RouteParams.PAR_USER_NAME)

            Column(Modifier.systemBarsPadding()) {
                PersonScreen(userName = userName ?: "", navController = navController)
            }
        }

        composable("${Route.IMAGE_PREVIEW}/{${RouteParams.PAR_IMAGE_URL}}",
            arguments = listOf(
                navArgument(RouteParams.PAR_IMAGE_URL) {
                    type = NavType.StringType
                    nullable = false
                }
            )) {
            val argument = requireNotNull(it.arguments)
            val imageUrl = argument.getString(RouteParams.PAR_IMAGE_URL)

            Column(Modifier.systemBarsPadding()) {
                ImageScreen(image = imageUrl ?: "", navController = navController)
            }
        }
    }
}