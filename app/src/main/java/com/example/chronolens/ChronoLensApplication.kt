package com.example.chronolens

import android.app.Application

class ChronoLensApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = ChronoLensAppContainer(this)
    }
}