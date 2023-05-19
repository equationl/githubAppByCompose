package com.equationl.githubapp.common.route

object Route {
    const val LOGIN = "login"
    const val REPO_LIST = "repoList"
    const val REPO_DETAIL = "repoDetail"
    const val PUSH_DETAIL = "pushDetail"
    const val OAuthLogin = "OAuthLogin"
    const val MAIN = "main"
    const val SEARCH = "search"
    const val ISSUE_DETAIL = "issueDetail"
    const val CODE_DETAIL = "codeDetail"
    const val USER_LIST = "userList"
    const val USER_INFO = "userInfo"
    const val PERSON_DETAIL = "personDetail"
    const val IMAGE_PREVIEW = "imagePreview"
    const val NOTIFY = "notify"
    const val WELCOME = "welcome"
}

object RouteParams {
    const val PAR_ISSUE_NUM = "issue_number"
    const val PAR_REPO_PATH = "repo_path"
    const val PAR_REPO_OWNER = "repo_owner"
    const val PAR_PUSH_SHA = "push_sha"
    const val PAR_LOCAL_CODE = "local_code"
    const val PAR_URL = "url"
    const val PAR_FILE_PATH = "file_path"
    const val PAR_USER_NAME = "user_name"
    const val PAR_IMAGE_URL = "image_url"
    const val PAR_REPO_REQUEST_TYPE = "repo_request_type"
}