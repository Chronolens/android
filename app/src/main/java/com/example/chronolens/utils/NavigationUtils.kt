package com.example.chronolens.utils

enum class ChronolensNav {
    Login,
    MediaGrid,
    FullScreenMedia,
    Albums,
    Settings,
    Search,
    Error
}

val noBottomBar:List<ChronolensNav> = listOf(ChronolensNav.FullScreenMedia,ChronolensNav.Login)
val noTopBar:List<ChronolensNav> = listOf(ChronolensNav.FullScreenMedia,ChronolensNav.Login)
