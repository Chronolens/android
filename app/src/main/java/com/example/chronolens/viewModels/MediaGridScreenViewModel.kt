package com.example.chronolens.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.BitmapImage
import com.example.chronolens.utils.SyncManager
import com.example.chronolens.models.LocalMedia
import com.example.chronolens.models.MediaAsset
import com.example.chronolens.models.RemoteMedia
import com.example.chronolens.repositories.MediaGridRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.graphics.Bitmap
import android.media.ExifInterface
import java.io.File
import java.io.IOException

data class MediaGridState(
    val media: List<MediaAsset> = listOf(),
)

data class FullscreenImageState(
    val image: BitmapImage? = null,
    val currentMedia: MediaAsset? = null
)


class MediaGridScreenViewModel(private val mediaGridRepository: MediaGridRepository) : ViewModel() {

    private val _mediaGridState = MutableStateFlow(MediaGridState())
    val mediaGridState: StateFlow<MediaGridState> = _mediaGridState.asStateFlow()

    private val _fullscreenImageState = MutableStateFlow(FullscreenImageState())
    val fullscreenImageState: StateFlow<FullscreenImageState> = _fullscreenImageState.asStateFlow()

    private val syncManager = SyncManager(mediaGridRepository)
    private var remoteAssets: List<RemoteMedia> = mutableListOf()
    private var localAssets: List<LocalMedia> = mutableListOf()

    fun init() {
        viewModelScope.launch {
            remoteAssets = syncManager.getRemoteAssets()
            mergeMediaAssets()
            loadLocalAssets()
        }
    }

    private fun loadLocalAssets() {
        viewModelScope.launch(Dispatchers.IO) {
            val localMedia = syncManager.getLocalAssets()
            val localMediaIds = localMedia.map { it.id }
            val checksums = mediaGridRepository.dbGetChecksumsFromList(localMediaIds)
            Log.i("LOG", "Already calculated checksums length: ${checksums.size}")
            val checkSumsMap = checksums.associate { it.localId to it.checksum }

            val localMediaNotCalculated: MutableList<LocalMedia> = mutableListOf()
            val localMediaCalculated: MutableList<LocalMedia> = mutableListOf()

            for (media in localMedia) {
                val checksum = checkSumsMap[media.id]
                if (checksum != null) {
                    media.checksum = checksum
                    localMediaCalculated += media
                } else {
                    localMediaNotCalculated += media
                }
            }

            localAssets = localMediaCalculated.toList()
            mergeMediaAssets()
            localMediaCalculated.clear()

            for (media in localMediaNotCalculated) {
                val checksum = mediaGridRepository.getOrComputeChecksum(media.id, media.path)
                media.checksum = checksum
                media.thumbnail = loadExifThumbnail(media)
                localMediaCalculated += media
            }
            localAssets += localMediaCalculated

            mergeMediaAssets()
        }
    }


    private fun mergeMediaAssets() {
        _mediaGridState.update { currState ->
            currState.copy(
                media = syncManager.mergeAssets(localAssets, remoteAssets)
            )
        }
    }

    fun updateCurrentAsset(mediaAsset: MediaAsset) {
        _fullscreenImageState.update { currState ->
            currState.copy(
                currentMedia = mediaAsset
            )
        }
    }

    fun uploadMedia(localMedia: LocalMedia) {
        viewModelScope.launch(Dispatchers.IO) {
            val remoteId: String? = mediaGridRepository.apiUploadFileStream(localMedia)
            _fullscreenImageState.update { currState ->
                currState.copy(
                    currentMedia = localMedia.copy(remoteId = remoteId)
                )
            }
        }
    }

    suspend fun getRemoteAssetPreviewUrl(id: String): String {
        return mediaGridRepository.apiGetPreview(id)
    }

    suspend fun getRemoteAssetFullImageUrl(id: String): String {
        return mediaGridRepository.apiGetFullImage(id)
    }


    private fun loadExifThumbnail(localMedia: LocalMedia): Bitmap? {
        return try {
            val file = File(localMedia.path)
            val exifInterface = ExifInterface(file)
            exifInterface.thumbnailBitmap
        } catch (e: IOException) {
            Log.e("MediaGridScreenViewModel", "Failed to load EXIF thumbnail: ${e.message}")
            null
        }
    }

}
