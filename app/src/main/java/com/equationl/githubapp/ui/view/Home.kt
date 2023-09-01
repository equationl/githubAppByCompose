package com.equationl.githubapp.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.equationl.githubapp.common.route.Route
import com.equationl.githubapp.common.route.RouteParams
import com.equationl.githubapp.ui.view.code.CodeDetailScreen
import com.equationl.githubapp.ui.view.image.ImageScreen
import com.equationl.githubapp.ui.view.issue.IssueDetailScreen
import com.equationl.githubapp.ui.view.list.GeneralListEnum
import com.equationl.githubapp.ui.view.list.generalRepo.GeneralRepoListScreen
import com.equationl.githubapp.ui.view.list.generalUser.GeneralUserListScreen
import com.equationl.githubapp.ui.view.login.LoginScreen
import com.equationl.githubapp.ui.view.login.OAuthLoginScreen
import com.equationl.githubapp.ui.view.main.MainScreen
import com.equationl.githubapp.ui.view.notify.NotifyScreen
import com.equationl.githubapp.ui.view.person.PersonScreen
import com.equationl.githubapp.ui.view.push.PushDetailScreen
import com.equationl.githubapp.ui.view.release.ReleaseScreen
import com.equationl.githubapp.ui.view.repos.RepoDetailScreen
import com.equationl.githubapp.ui.view.search.SearchScreen
import com.equationl.githubapp.ui.view.userInfo.UserInfoScreen
import com.equationl.githubapp.ui.view.welcome.WelcomeScreen


@Composable
fun HomeNavHost(
    onFinish: () -> Unit
) {
    val navController = rememberNavController()
    NavHost(navController, Route.WELCOME) {

        // 欢迎页
        composable(Route.WELCOME) {
            Column(Modifier.systemBarsPadding()) {
                WelcomeScreen(navHostController = navController)
            }
        }

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

        // 主页
        composable(Route.MAIN) {
            Column(Modifier.systemBarsPadding()) {
                MainScreen(navController, onFinish)
            }
        }

        // 通知页
        composable(Route.NOTIFY) {
            Column(Modifier.systemBarsPadding()) {
                NotifyScreen(navHostController = navController)
            }
        }

        // 用户信息页
        composable(Route.USER_INFO) {
            Column(Modifier.systemBarsPadding()) {
                UserInfoScreen(navHostController = navController)
            }
        }

        // 搜索页
        composable("${Route.SEARCH}?${RouteParams.PAR_SEARCH_QUERY}={${RouteParams.PAR_SEARCH_QUERY}}",
            arguments = listOf(
                navArgument(RouteParams.PAR_SEARCH_QUERY) {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) {
            val query = it.arguments?.getString(RouteParams.PAR_SEARCH_QUERY)

            Column(Modifier.systemBarsPadding()) {
                SearchScreen(navHostController = navController, queryString = query)
            }
        }

        // 仓库详情页
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

        // ISSUE 详情页
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

        // 提交详情页
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

        // 代码预览页
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
                    nullable = true
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

        // 用户详情页
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

        // 图像预览页
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

        // 仓库列表页
        composable("${Route.REPO_LIST}/{${RouteParams.PAR_REPO_PATH}}/{${RouteParams.PAR_REPO_OWNER}}/{${RouteParams.PAR_REPO_REQUEST_TYPE}}",
            arguments = listOf(
                navArgument(RouteParams.PAR_REPO_PATH) {
                    type = NavType.StringType
                    nullable = false
                },
                navArgument(RouteParams.PAR_REPO_OWNER) {
                    type = NavType.StringType
                    nullable = false
                },
                navArgument(RouteParams.PAR_REPO_REQUEST_TYPE) {
                    type = NavType.StringType
                    nullable = false
                }
            )) {
            val argument = requireNotNull(it.arguments)
            val userName = argument.getString(RouteParams.PAR_REPO_OWNER)
            val repoName = argument.getString(RouteParams.PAR_REPO_PATH)
            val requestTypeString = argument.getString(RouteParams.PAR_REPO_REQUEST_TYPE)
            val requestType = GeneralListEnum.valueOf(requestTypeString ?: "")

            Column(Modifier.systemBarsPadding()) {
                GeneralRepoListScreen(
                    repoName ?: "",
                    userName ?: "",
                    requestType = requestType,
                    navController
                )
            }
        }

        // 用户列表页
        composable("${Route.USER_LIST}/{${RouteParams.PAR_REPO_PATH}}/{${RouteParams.PAR_REPO_OWNER}}/{${RouteParams.PAR_REPO_REQUEST_TYPE}}",
            arguments = listOf(
                navArgument(RouteParams.PAR_REPO_PATH) {
                    type = NavType.StringType
                    nullable = false
                },
                navArgument(RouteParams.PAR_REPO_OWNER) {
                    type = NavType.StringType
                    nullable = false
                },
                navArgument(RouteParams.PAR_REPO_REQUEST_TYPE) {
                    type = NavType.StringType
                    nullable = false
                }
            )) {
            val argument = requireNotNull(it.arguments)
            val userName = argument.getString(RouteParams.PAR_REPO_OWNER)
            val repoName = argument.getString(RouteParams.PAR_REPO_PATH)
            val requestTypeString = argument.getString(RouteParams.PAR_REPO_REQUEST_TYPE)
            val requestType = GeneralListEnum.valueOf(requestTypeString ?: "")

            Column(Modifier.systemBarsPadding()) {
                GeneralUserListScreen(
                    repoName ?: "",
                    userName ?: "",
                    requestType = requestType,
                    navController
                )
            }
        }

        // RELEASE 列表页
        composable("${Route.RELEASE_LIST}/{${RouteParams.PAR_REPO_OWNER}}/{${RouteParams.PAR_REPO_PATH}}",
            arguments = listOf(
                navArgument(RouteParams.PAR_REPO_OWNER) {
                    type = NavType.StringType
                    nullable = false
                },
                navArgument(RouteParams.PAR_REPO_PATH) {
                    type = NavType.StringType
                    nullable = false
                },
            )) {
            val argument = requireNotNull(it.arguments)
            val repoOwner = argument.getString(RouteParams.PAR_REPO_OWNER)
            val repoName = argument.getString(RouteParams.PAR_REPO_PATH)

            Column(Modifier.systemBarsPadding()) {
                ReleaseScreen(
                    repoName = repoName,
                    repoOwner = repoOwner,
                    navController = navController
                )
            }
        }
    }
}