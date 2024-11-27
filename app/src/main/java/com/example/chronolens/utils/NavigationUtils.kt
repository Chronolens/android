package com.example.chronolens.utils

enum class ChronolensNav {
    Login,
    MediaGrid,
    FullScreenMedia,
    Albums,
    PersonPhotoGrid,
    Search,
    Settings,

    //Settings stuff
    BackgroundUpload,
    AlbumsPicker,
    ActivityHistory,
    MachineLearning


}

val noBottomBar:List<ChronolensNav> = listOf(ChronolensNav.FullScreenMedia,ChronolensNav.Login)
val noTopBar:List<ChronolensNav> = listOf(ChronolensNav.FullScreenMedia,ChronolensNav.Login)
