package com.example.chronolens.utils

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.chronolens.models.LocalMedia

fun shareImages(context: Context, mediaList: List<LocalMedia>) {
    try {
        val uris = ArrayList<Uri>()
        for (media in mediaList) {
            val contentUri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                media.id
            )
            uris.add(contentUri)
        }

        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "image/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share Images"))
    } catch (e: SecurityException) {
        Log.e("ShareError", "SecurityException: ${e.message}")
    } catch (e: Exception) {
        Log.e("ShareError", "Error sharing images: ${e.message}")
    }
}
