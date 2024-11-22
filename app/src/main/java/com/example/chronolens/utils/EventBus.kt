package com.example.chronolens.utils

import kotlinx.coroutines.flow.MutableSharedFlow

object EventBus {
    val logoutEvent = MutableSharedFlow<Unit>()
}

