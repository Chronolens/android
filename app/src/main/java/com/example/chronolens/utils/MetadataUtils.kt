package com.example.chronolens.utils

import android.media.ExifInterface
import java.io.File

fun loadExifData(path: String): Map<String, String?> {
    val exif = ExifInterface(path)
    val latLong = FloatArray(2)

    val hasGps = exif.getLatLong(latLong)
    val latitude = if (hasGps) latLong[0] else null
    val longitude = if (hasGps) latLong[1] else null

    val file = File(path)
    val fileName = file.name
    val fileSize = file.length().toString()


    return mapOf(
        "fileName" to fileName,
        "fileSize" to fileSize,
        "filePath" to path,

        "imageWidth" to exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH),
        "imageHeight" to exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH),
        "orientation" to exif.getAttribute(ExifInterface.TAG_ORIENTATION),
        "dateTime" to exif.getAttribute(ExifInterface.TAG_DATETIME),
        "software" to exif.getAttribute(ExifInterface.TAG_SOFTWARE),

        // Camera Information
        "make" to exif.getAttribute(ExifInterface.TAG_MAKE),
        "model" to exif.getAttribute(ExifInterface.TAG_MODEL),

        // Exposure Information
        "exposureTime" to exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME),
        "fNumber" to exif.getAttribute(ExifInterface.TAG_F_NUMBER),
        "iso" to exif.getAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS),
        "focalLength" to exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH),
        "focalLength35mm" to exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM),
        "shutterSpeed" to exif.getAttribute(ExifInterface.TAG_SHUTTER_SPEED_VALUE),
        "aperture" to exif.getAttribute(ExifInterface.TAG_APERTURE_VALUE),
        "brightness" to exif.getAttribute(ExifInterface.TAG_BRIGHTNESS_VALUE),
        "flash" to exif.getAttribute(ExifInterface.TAG_FLASH),

        // GPS Information
        "latitude" to latitude.toString(),
        "longitude" to longitude.toString(),
        "altitude" to exif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE),
        "latitudeRef" to exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF),
        "longitudeRef" to exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF),
        "altitudeRef" to exif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF),
        "gpsTimestamp" to exif.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP),
        "gpsDatestamp" to exif.getAttribute(ExifInterface.TAG_GPS_DATESTAMP),
        "gpsProcessingMethod" to exif.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD),

        // Date and Time
        "dateTimeOriginal" to exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL),
        "dateTimeDigitized" to exif.getAttribute(ExifInterface.TAG_DATETIME_DIGITIZED),
        "subsecTime" to exif.getAttribute(ExifInterface.TAG_SUBSEC_TIME),
        "subsecTimeOriginal" to exif.getAttribute(ExifInterface.TAG_SUBSEC_TIME_ORIGINAL),
        "subsecTimeDigitized" to exif.getAttribute(ExifInterface.TAG_SUBSEC_TIME_DIGITIZED)
    )
}
