package com.example.chronolens.utils

enum class ChronolensNav {
    Login,
    MediaGrid,
    FullScreenMedia,
    Albums,
    Search,
    Error, // TODO: remove?
    Settings,

    // TODO: make this a navGraph?
    //Settings stuff
    BackgroundUpload,
    ActivityHistory,
    MachineLearning


}

val noBottomBar:List<ChronolensNav> = listOf(ChronolensNav.FullScreenMedia,ChronolensNav.Login)
val noTopBar:List<ChronolensNav> = listOf(ChronolensNav.FullScreenMedia,ChronolensNav.Login)
