package com.example.chronolens.utils

import android.media.ExifInterface
import com.example.chronolens.models.FullMedia
import java.io.File

fun loadExifData(path: String, id: String, timestamp: Long): FullMedia {
    val exif = ExifInterface(path)
    val latLong = FloatArray(2)

    val hasGps = exif.getLatLong(latLong)
    val latitude = if (hasGps) latLong[0].toDouble() else null
    val longitude = if (hasGps) latLong[1].toDouble() else null

    val file = File(path)
    val fileName = file.name
    val fileSize = file.length()

    return FullMedia(
        id = id,
        mediaUrl = null,
        createdAt = timestamp,
        fileSize = fileSize,
        fileName = fileName,
        longitude = longitude,
        latitude = latitude,
        imageWidth = exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH)?.toIntOrNull(),
        imageLength = exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH)?.toIntOrNull(),
        make = exif.getAttribute(ExifInterface.TAG_MAKE),
        model = exif.getAttribute(ExifInterface.TAG_MODEL),
        fNumber = exif.getAttribute(ExifInterface.TAG_F_NUMBER),
        exposureTime = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME),
        photographicSensitivity = exif.getAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS),
        orientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION)?.toIntOrNull()
    )
}
