package com.example.chronolens.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.BitmapImage
import com.example.chronolens.models.LocalMedia
import com.example.chronolens.models.MediaAsset
import com.example.chronolens.models.Person
import com.example.chronolens.models.RemoteMedia
import com.example.chronolens.repositories.MediaGridRepository
import com.example.chronolens.utils.SyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SelectingType {
    Remote,
    Local,
    None
}

enum class SyncState {
    Synced,
    FetchingRemote,
    FetchingLocal,
    Merging
}

data class MediaGridState(
    val media: List<MediaAsset> = listOf(),
    val isLoading: Boolean = true, // TODO: remove?
    val selected: Map<String, MediaAsset> = mapOf(),
    val isSelecting: Boolean = false,
    val selectingType: SelectingType = SelectingType.None,
    val people: List<Person> = listOf(),
    val syncState: SyncState = SyncState.Synced,
    val syncProgress: Pair<Int,Int>? = null
)

data class FullscreenImageState(
    val image: BitmapImage? = null,
    val currentMedia: MediaAsset? = null
)

// We have to null all these values after the user leaves the screen, there's a weird bug
// that randomly displays one of the images from the previous screen
data class PersonPhotoGridState(
    val person: Person? = null,
    val photos: List<Map<String, String>> = listOf(),
    val currentPage: Int = 1,
    val isLoading: Boolean = false,
    val hasMore: Boolean = true
)

data class ClipSearchState(
    val currentSearch: String = "",
    val photos: List<Map<String, String>> = listOf(),
    val currentPage: Int = 1,
    val isLoading: Boolean = false,
    val hasMore: Boolean = true
)


class MediaGridScreenViewModel(private val mediaGridRepository: MediaGridRepository) : ViewModel() {

    private val _mediaGridState = MutableStateFlow(MediaGridState())
    val mediaGridState: StateFlow<MediaGridState> = _mediaGridState.asStateFlow()

    private val _fullscreenImageState = MutableStateFlow(FullscreenImageState())
    val fullscreenImageState: StateFlow<FullscreenImageState> = _fullscreenImageState.asStateFlow()

    private val _personPhotoGridState = MutableStateFlow(PersonPhotoGridState())
    val personPhotoGridState: StateFlow<PersonPhotoGridState> = _personPhotoGridState.asStateFlow()

    private val _clipSearchState = MutableStateFlow(ClipSearchState())
    val clipSearchState: StateFlow<ClipSearchState> = _clipSearchState.asStateFlow()

    private val syncManager = SyncManager(mediaGridRepository)
    private var remoteAssets: List<RemoteMedia> = mutableListOf()
    private var localAssets: List<LocalMedia> = mutableListOf()

    fun init() {
        viewModelScope.launch {
            loadMediaGrid()
            setIsLoading(false)
            loadPeople()

        }
    }

    private suspend fun loadMediaGrid() {
        setSyncState(SyncState.FetchingRemote)
        delay(2000L)
        remoteAssets = syncManager.getRemoteAssets()
        setSyncState(SyncState.Merging)
        mergeMediaAssets()
        setSyncState(SyncState.FetchingLocal)
        loadLocalAssets()
    }

    fun refreshMediaGrid() {
        viewModelScope.launch {
            setIsLoading(true)
            loadMediaGrid()
            setIsLoading(false)
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
            setProgress(0,localMedia.size)
            var i = 0
            for (media in localMedia) {
                val checksum = checkSumsMap[media.id]
                if (checksum != null) {
                    media.checksum = checksum
                    localMediaCalculated += media
                    setProgress(++i,localMedia.size)
                } else {
                    localMediaNotCalculated += media
                }
                delay(500L)
            }

            localAssets = localMediaCalculated.toList()

            mergeMediaAssets()
            setSyncState(SyncState.FetchingLocal)
            localMediaCalculated.clear()

            for (media in localMediaNotCalculated) {
                val checksum = mediaGridRepository.getOrComputeChecksum(media.id, media.path)
                media.checksum = checksum
                localMediaCalculated += media
                setProgress(++i,localMedia.size)
                delay(500L)
            }
            localAssets += localMediaCalculated

            mergeMediaAssets()
            setSyncState(SyncState.Synced)
        }
    }

    // Merge local and remote assets
    private fun mergeMediaAssets() {
        setSyncState(SyncState.Merging)
        _mediaGridState.update { currState ->
            currState.copy(
                media = syncManager.mergeAssets(localAssets, remoteAssets)
            )
        }
    }


    fun updateCurrentPersonPhotoGrid(person: Person) {
        _personPhotoGridState.update {
            it.copy(
                person = person,
                photos = emptyList(),
                currentPage = 1,
                isLoading = false,
                hasMore = true
            )
        }
    }


    fun updateCurrentAssetHelper(preview: Map<String, String>) {
        val remoteId = preview["id"] ?: ""
        val checksum = preview["checksum"] ?: ""
        val timestamp = preview["timestamp"]?.toLong() ?: 0
        val media = RemoteMedia(remoteId, checksum, timestamp)
        updateCurrentAsset(media)
    }

    fun updateCurrentAsset(mediaAsset: MediaAsset) {
        _fullscreenImageState.update { currState ->
            currState.copy(
                currentMedia = mediaAsset
            )
        }
    }

    fun clipSearchNextPage(searchInput: String) {
        viewModelScope.launch {
            val state = _clipSearchState.value

            if (state.isLoading || !state.hasMore) return@launch

            _clipSearchState.update { it.copy(isLoading = true, currentSearch = searchInput) }

            try {
                val nextPage = state.currentPage
                val pageSize = 10
                val newPhotos =
                    mediaGridRepository.apiGetNextClipSearchPage(searchInput, nextPage, pageSize)

                _clipSearchState.update {
                    it.copy(
                        photos = it.photos + (newPhotos ?: emptyList()),
                        currentPage = it.currentPage + 1,
                        isLoading = false,
                        hasMore = !newPhotos.isNullOrEmpty()
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _clipSearchState.update { it.copy(isLoading = false) }
            }
        }
    }


    fun clearSearchResults() {
        _clipSearchState.update {
            it.copy(
                currentSearch = "",
                photos = emptyList(),
                currentPage = 1,
                isLoading = false,
                hasMore = true
            )
        }
    }

    fun loadClusterNextPage(clusterId: Int, requestType: String) {
        viewModelScope.launch {
            val state = _personPhotoGridState.value

            if (state.isLoading || !state.hasMore) return@launch

            _personPhotoGridState.update { it.copy(isLoading = true) }

            try {
                val nextPage = state.currentPage
                val pageSize = 10
                val newPhotos = mediaGridRepository.apiGetClusterPreviewsPage(
                    clusterId,
                    nextPage,
                    pageSize,
                    requestType
                )

                _personPhotoGridState.update {
                    it.copy(
                        photos = it.photos + (newPhotos ?: emptyList()),
                        currentPage = it.currentPage + 1,
                        isLoading = false,
                        hasMore = !newPhotos.isNullOrEmpty()
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _personPhotoGridState.update { it.copy(isLoading = false) }
            }
        }
    }


    fun uploadMedia(localMedia: LocalMedia) {
        viewModelScope.launch(Dispatchers.IO) {
            if (localMedia.checksum == null) {
                val checksum =
                    mediaGridRepository.getOrComputeChecksum(localMedia.id, localMedia.path)
                localMedia.checksum = checksum
            }

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

    private fun setIsLoading(isLoading:Boolean) {
        viewModelScope.launch {
            _mediaGridState.update { currState ->
                currState.copy(isLoading = isLoading)
            }
        }
    }

    private fun loadPeople() {
        viewModelScope.launch {
            val peopleThumbnails = mediaGridRepository.apiGetPeople()
            _mediaGridState.update { currState ->
                currState.copy(people = peopleThumbnails)
            }
        }
    }

    fun selectOrDeselect(id: String, media: MediaAsset) {
        viewModelScope.launch {
            _mediaGridState.update { currState ->
                val oldSelected = currState.selected.toMutableMap()
                var selectingType: SelectingType = currState.selectingType
                if (oldSelected.containsKey(id)) {
                    oldSelected.remove(id)
                    if (oldSelected.isEmpty()) {
                        selectingType = SelectingType.None
                    }
                } else {
                    if (media is LocalMedia && selectingType != SelectingType.Remote) {
                        oldSelected[id] = media
                        selectingType = SelectingType.Local
                    } else if (media is RemoteMedia && selectingType != SelectingType.Local) {
                        oldSelected[id] = media
                        selectingType = SelectingType.Remote
                    }
                }
                currState.copy(
                    selected = oldSelected,
                    isSelecting = oldSelected.isNotEmpty(),
                    selectingType = selectingType
                )
            }
        }
    }

    private fun setSyncState(syncState: SyncState) {
        viewModelScope.launch {
            _mediaGridState.update { currState ->
                currState.copy(syncState = syncState)
            }
        }
    }

    private fun setProgress(progress: Int,max:Int){
        viewModelScope.launch {
            _mediaGridState.update { currState ->
                currState.copy(syncProgress = Pair(progress,max))
            }
        }
    }

}
