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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MediaGridState(
    val media: List<MediaAsset> = listOf(),
    val isLoading: Boolean = true
)

data class FullscreenImageState(
    val image: BitmapImage? = null,
    val currentMedia: MediaAsset? = null
)


class MediaGridScreenViewModel(private val mediaGridRepository: MediaGridRepository) : ViewModel() {
    // List to hold the final merged images
    private val _mediaGridState = MutableStateFlow(MediaGridState())
    val mediaGridState: StateFlow<MediaGridState> = _mediaGridState.asStateFlow()

    private val _fullscreenImageState = MutableStateFlow(FullscreenImageState())
    val fullscreenImageState: StateFlow<FullscreenImageState> = _fullscreenImageState.asStateFlow()

    private val syncManager = SyncManager(mediaGridRepository)
    private var remoteAssets: List<RemoteMedia> = mutableListOf()
    private var localAssets: List<LocalMedia> = mutableListOf()

    // Initialize sync manager and fetch assets
    fun init() {
        loadMediaGrid()
    }

    fun loadMediaGrid() {
        viewModelScope.launch {
            setIsLoading()
            remoteAssets = syncManager.getRemoteAssets()
            mergeMediaAssets()
            loadLocalAssets()
            setIsLoaded()
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

            // Update the state with assets that have checksums
            localAssets = localMediaCalculated.toList()
            mergeMediaAssets()
            localMediaCalculated.clear()

            for (media in localMediaNotCalculated) {
                val checksum = mediaGridRepository.getOrComputeChecksum(media.id, media.path)
                media.checksum = checksum
                // Update state in the main thread
                localMediaCalculated += media
            }
            localAssets += localMediaCalculated
            // Finally, merge local and remote assets
            mergeMediaAssets()
        }
    }


    // Merge local and remote assets
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
            if (remoteId != null) {
                updateMediaList(remoteId, localMedia.checksum!!)
            }
        }
    }

    suspend fun getRemoteAssetPreviewUrl(id: String): String {
        return mediaGridRepository.apiGetPreview(id)
    }

    suspend fun getRemoteAssetFullImageUrl(id: String): String {
        return mediaGridRepository.apiGetFullImage(id)
    }

    // TODO: is it worth to "mergeMediaAssets()" or to do this??
    private fun updateMediaList(remoteId: String, checksum: String) {
        viewModelScope.launch {

            val mediaList = _mediaGridState.value.media.toMutableList()
            val index = mediaList.indexOfFirst { it.checksum == checksum && it is LocalMedia }

            if (index != -1) {
                val media = mediaList[index] as LocalMedia
                mediaList[index] = media.copy(remoteId = remoteId)
            }

            _mediaGridState.update { currState ->
                currState.copy(media = mediaList)
            }
        }
    }

    private fun setIsLoading() {
        viewModelScope.launch {
            _mediaGridState.update { currState ->
                currState.copy(isLoading = true)
            }
        }
    }

    private fun setIsLoaded() {
        viewModelScope.launch {
            _mediaGridState.update { currState ->
                currState.copy(isLoading = false)
            }
        }
    }

}
