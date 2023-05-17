package com.equationl.githubapp.ui.view.list

enum class GeneralListEnum {
    /**用户粉丝*/
    UserFollower,
    /**用户关注*/
    UserFollowed,
    /**用户仓库*/
    UserRepository,
    /**用户star*/
    UserStar,
    /**仓库star用户*/
    RepositoryStarUser,
    /**仓库被fork列表*/
    RepositoryForkUser,
    /**仓库订阅用户*/
    RepositoryWatchUser,
}

enum class GeneralRepoListSort(val showName: String, val requestValue: String) {
    // for 个人仓库列表
    Push("提交", "pushed"),
    Create("创建", "created"),
    Name("名称", "full_name"),

    // for star 仓库列表
    Stars("最多Star", "stars"),
    RecentlyStar("最近Star", "created"),
    Update("最近更新", "updated")
}