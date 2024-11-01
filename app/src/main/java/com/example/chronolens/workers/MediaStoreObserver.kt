package com.example.chronolens.workers

import android.content.Context
import android.database.ContentObserver

class MediaStoreObserver(
    private val context: Context,
    private val onPhotoDetected: () -> Unit
) : ContentObserver(null) {

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        // Trigger the checksum job when a new photo is detected
        onPhotoDetected()
    }
}