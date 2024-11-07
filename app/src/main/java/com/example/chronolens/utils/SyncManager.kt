package com.example.chronolens.utils

import android.media.ExifInterface
import android.provider.MediaStore
import com.example.chronolens.models.LocalMedia
import com.example.chronolens.models.MediaAsset
import com.example.chronolens.models.RemoteMedia
import com.example.chronolens.repositories.MediaGridRepository

// TODO: we can get thumbnail bytes from exif or mediaStore directly
class SyncManager(
    val mediaGridRepository: MediaGridRepository
) {
    suspend fun getRemoteAssets(): List<RemoteMedia> {
        val lastSync = mediaGridRepository.sharedPreferences.getLong("last_sync", 0L)

        var remoteAssets: List<RemoteMedia> = if (lastSync == 0L) {
            // Full Sync
            val remote = mediaGridRepository.apiSyncFullRemote()
            mediaGridRepository.dbUpsertRemoteAssets(remote)
            remote
        } else {
            // Partial Sync
            val (uploaded, deleted) = mediaGridRepository.apiSyncPartialRemote(lastSync)
            mediaGridRepository.dbUpsertRemoteAssets(uploaded)
            mediaGridRepository.dbDeleteRemoteAssets(deleted)
            mediaGridRepository.dbGetRemoteAssets()
        }

        remoteAssets = remoteAssets.sortedByDescending { it.timestamp }
        return remoteAssets
    }

    fun getLocalAssets(): List<LocalMedia> {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,           // MediaStore ID
            MediaStore.Images.Media.DATA,          // File path
            MediaStore.Images.Media.DATE_TAKEN,    // Timestamp (date taken)
            MediaStore.Images.Media.DATE_MODIFIED,  // Last modified timestamp
            MediaStore.Images.Media.MIME_TYPE
        )
        val cursor = mediaGridRepository.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection, null, null, null
        )

        val localMediaInfo = mutableListOf<LocalMedia>()


        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val pathColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val dateTakenColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val dateModifiedColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
            val mimeTypeColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)

            while (cursor.moveToNext()) {
                val id = cursor.getString(idColumn)
                val path = cursor.getString(pathColumn)
                val dateTaken =
                    if (dateTakenColumn != -1) it.getLong(dateTakenColumn) else null // Get the DATE_TAKEN value, can be null
                val mimeType = cursor.getString(mimeTypeColumn)

                val finalTimestamp: Long = if (dateTaken == null || dateTaken == 0L) {
                    it.getLong(dateModifiedColumn) * 1000
                } else {
                    dateTaken
                }

                localMediaInfo.add(
                    LocalMedia(
                        remoteId = null,
                        id = id,
                        path = path,
                        mimeType = mimeType,
                        checksum = null,
                        timestamp = finalTimestamp
                    )
                )
            }
        }

        localMediaInfo.sortByDescending { it.timestamp }
        return localMediaInfo
    }

    fun mergeAssets(local: List<LocalMedia>, remote: List<RemoteMedia>): List<MediaAsset> {
        val mediaAssets = mutableListOf<MediaAsset>()
        val remoteMediaMap: Map<String, RemoteMedia> = remote.associateBy { it.checksum!! }

        val localAndRemoteHashes = HashSet<String>()

        local.forEach { localMedia ->
            val remoteMedia = remoteMediaMap[localMedia.checksum.toString()]
            if (remoteMedia != null) {
                mediaAssets.add(
                    LocalMedia(
                        remoteMedia.id,
                        localMedia.path,
                        localMedia.id,
                        localMedia.mimeType,
                        localMedia.checksum!!,
                        localMedia.timestamp
                    )
                )
                localAndRemoteHashes.add(localMedia.checksum!!)
            } else {
                mediaAssets.add(localMedia)
            }
        }

        remote.filterNot { it.checksum in localAndRemoteHashes }.forEach {
            mediaAssets.add(it)
        }

        mediaAssets.sortByDescending { it.timestamp }
        return mediaAssets
    }


    // TODO: calculate by photo or all at the same time?
    private fun loadExifData(path: String): Map<String, String?> {
        val exif = ExifInterface(path)
        val latLong = FloatArray(2)
        val latitude = if (exif.getLatLong(latLong)) latLong[0].toString() else null
        val longitude = if (exif.getLatLong(latLong)) latLong[1].toString() else null

        return mapOf(
            "imageWidth" to exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH),
            "imageHeight" to exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH),
            "orientation" to exif.getAttribute(ExifInterface.TAG_ORIENTATION),
            "dateTime" to exif.getAttribute(ExifInterface.TAG_DATETIME),
            "software" to exif.getAttribute(ExifInterface.TAG_SOFTWARE),

            // Camera Information
            "make" to exif.getAttribute(ExifInterface.TAG_MAKE),
            "model" to exif.getAttribute(ExifInterface.TAG_MODEL),
            "lensMake" to exif.getAttribute(ExifInterface.TAG_MAKE), // not sure
            "lensModel" to exif.getAttribute(ExifInterface.TAG_MODEL), // not sure
//            "lensSpecification" to exif.getAttribute(ExifInterface.TAG_LENS_SPECIFICATION),

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
            "latitude" to latitude,
            "longitude" to longitude,
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
            "subsecTimeDigitized" to exif.getAttribute(ExifInterface.TAG_SUBSEC_TIME_DIGITIZED),

            // Miscellaneous
            "userComment" to exif.getAttribute(ExifInterface.TAG_USER_COMMENT),
            "copyright" to exif.getAttribute(ExifInterface.TAG_COPYRIGHT),
            "imageDescription" to exif.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION),
            "artist" to exif.getAttribute(ExifInterface.TAG_ARTIST),
            "makerNote" to exif.getAttribute(ExifInterface.TAG_MAKER_NOTE)
        )
    }


}
