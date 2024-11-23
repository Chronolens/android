package com.example.chronolens.utils

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.chronolens.models.LocalMedia


fun shareImage(context: Context, media: LocalMedia) {
    try {

        // Get the content URI using the MediaStore ID
        val contentUri = ContentUris.withAppendedId(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            media.id
        )

        // Create the share intent
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Start the chooser
        context.startActivity(Intent.createChooser(shareIntent, "Share Image"))
    } catch (e: SecurityException) {
        Log.e("ShareError", "SecurityException: ${e.message}")
    } catch (e: Exception) {
        Log.e("ShareError", "Error sharing image: ${e.message}")
    }
}

