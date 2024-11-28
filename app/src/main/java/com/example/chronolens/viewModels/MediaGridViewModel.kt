package com.example.chronolens.viewModels

import android.content.Context
import android.util.Log
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.BitmapImage
import com.example.chronolens.models.FullMedia
import com.example.chronolens.models.LocalMedia
import com.example.chronolens.models.MediaAsset
import com.example.chronolens.models.Person
import com.example.chronolens.models.RemoteMedia
import com.example.chronolens.models.UnknownPerson
import com.example.chronolens.repositories.MediaGridRepository
import com.example.chronolens.utils.APIUtils
import com.example.chronolens.utils.Prefs
import com.example.chronolens.utils.SyncManager
import com.example.chronolens.utils.shareImages
import com.example.chronolens.utils.loadExifData
import kotlinx.coroutines.Dispatchers
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

enum class DownloadingState {
    Downloading,
    Downloaded
}

data class MediaGridState(
    val media: List<MediaAsset> = listOf(),
    val isLoading: Boolean = true,
    val selected: Map<String, MediaAsset> = mapOf(),
    val isSelecting: Boolean = false,
    val selectingType: SelectingType = SelectingType.None,
    val syncState: SyncState = SyncState.Synced,
    val syncProgress: Pair<Int, Int> = Pair(0, 0),
    val isUploading: Boolean = false,
    val isSelectingPerson: Boolean = false,
    val people: List<Person> = listOf(),
    val selectedPeople: Map<Int, Person> = mapOf(),

    val uploadProgress: Pair<Int, Int> = Pair(0, 0),
    val isDownloading: Boolean = false,
    val downloadProgress: Pair<Int, Int> = Pair(0, 0)
)

data class FullscreenImageState(
    val image: BitmapImage? = null,
    val uploading: Boolean = false,
    val downloadState: DownloadingState? = null,
    val currentMediaAsset: MediaAsset? = null,
    val currentFullMedia: FullMedia? = null
)

// We have to null all these values after the user leaves the screen, there's a weird bug
// that randomly displays one of the images from the previous screen
data class PersonPhotoGridState(
    val person: Person? = null,
    val photos: List<Pair<String, String>> = listOf(),
    var currentPage: Int = 1,
    var isLoading: Boolean = false,
    var hasMore: Boolean = true
)

data class ClipSearchState(
    val currentSearch: String = "",
    val photos: List<Pair<String, String>> = listOf(),
    var currentPage: Int = 1,
    var isLoading: Boolean = false,
    var hasMore: Boolean = true
)


class MediaGridViewModel(private val mediaGridRepository: MediaGridRepository) : ViewModel() {

    private val _mediaGridState = MutableStateFlow(MediaGridState())
    val mediaGridState: StateFlow<MediaGridState> = _mediaGridState.asStateFlow()

    private val _fullscreenImageState = MutableStateFlow(FullscreenImageState())
    val fullscreenImageState: StateFlow<FullscreenImageState> = _fullscreenImageState.asStateFlow()

    private val _personPhotoGridState = MutableStateFlow(PersonPhotoGridState())
    val personPhotoGridState: StateFlow<PersonPhotoGridState> = _personPhotoGridState.asStateFlow()

    private val _clipSearchState = MutableStateFlow(ClipSearchState())
    val clipSearchState: StateFlow<ClipSearchState> = _clipSearchState.asStateFlow()

    private val _showNameDialog = MutableStateFlow(false)
    val showNameDialog: StateFlow<Boolean> = _showNameDialog.asStateFlow()

    private val syncManager = SyncManager(mediaGridRepository)
    private var remoteAssets: List<RemoteMedia> = mutableListOf()
    private var localAssets: List<LocalMedia> = mutableListOf()
    val lazyGridState = LazyGridState()


    fun init(context: Context) {
        viewModelScope.launch {
            loadMediaGrid(context)
            setIsLoading(false)
            loadPeople()

        }
    }

    fun getAvailableAlbums(context: Context): List<String> {
        return syncManager.getAvailableAlbums(context)
    }

    fun getUserAlbums(): List<String>? {
        return mediaGridRepository.getUserAlbums()

    }

    fun setAlbums(albums: List<String>) {
        viewModelScope.launch {
            mediaGridRepository.sharedPreferences.edit()
                .putStringSet(Prefs.ALBUMS, albums.toSet())
                .apply()
        }
    }

    private suspend fun loadMediaGrid(context: Context) {
        setSyncState(SyncState.FetchingRemote)
        remoteAssets = syncManager.getRemoteAssets()
        setSyncState(SyncState.Merging)
        mergeMediaAssets()
        setSyncState(SyncState.FetchingLocal)
        loadLocalAssets(context)
    }

    fun refreshMediaGrid(context: Context) {
        viewModelScope.launch {
            setIsLoading(true)
            loadMediaGrid(context)
            loadPeople()
            setIsLoading(false)
        }
    }

    private fun loadLocalAssets(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val albums = getUserAlbums() ?: listOf()
            val localMedia = syncManager.getLocalAssets(albums, context)
            val localMediaIds = localMedia.map { it.id }
            val checksums = mediaGridRepository.dbGetChecksumsFromList(localMediaIds)
            Log.i("LOG", "Already calculated checksums length: ${checksums.size}")
            val checkSumsMap = checksums.associate { it.localId to it.checksum }

            val localMediaNotCalculated: MutableList<LocalMedia> = mutableListOf()
            val localMediaCalculated: MutableList<LocalMedia> = mutableListOf()

            var i = 0
            setSyncProgress(i, localMedia.size)
            for (media in localMedia) {
                val checksum = checkSumsMap[media.id]
                if (checksum != null) {
                    media.checksum = checksum
                    localMediaCalculated += media
                    setSyncProgress(++i, localMedia.size)
                } else {
                    localMediaNotCalculated += media
                }
            }

            localAssets = localMediaCalculated.toList()

            mergeMediaAssets()
            setSyncState(SyncState.FetchingLocal)
            localMediaCalculated.clear()

            for (media in localMediaNotCalculated) {
                val checksum = mediaGridRepository.getOrComputeChecksum(media.id, media.path)
                media.checksum = checksum
                localMediaCalculated += media
                setSyncProgress(++i, localMedia.size)
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

    fun updateCurrentAssetHelper(preview: Pair<String, String>) {
        viewModelScope.launch {
            val remoteId = preview.first
            val checksum = ""
            val timestamp = 0.toLong()
            val media = RemoteMedia(remoteId, checksum, timestamp)
            updateCurrentAsset(media)
        }
    }

    fun updateCurrentAsset(mediaAsset: MediaAsset) {
        viewModelScope.launch {
            if (mediaAsset is RemoteMedia) {
                val fullMedia = mediaGridRepository.apiGetFullImage(mediaAsset.id)
                _fullscreenImageState.update { currState ->
                    currState.copy(
                        currentMediaAsset = mediaAsset,
                        currentFullMedia = fullMedia
                    )
                }
            } else if (mediaAsset is LocalMedia) {
                val fullMedia = loadExifData(mediaAsset.path, mediaAsset.timestamp)
                _fullscreenImageState.update { currState ->
                    currState.copy(
                        currentMediaAsset = mediaAsset,
                        currentFullMedia = fullMedia
                    )
                }
            }
        }
    }

    fun clipSearchNextPage(searchInput: String) {
        viewModelScope.launch {
            val state = _clipSearchState.value

            if (state.isLoading || !state.hasMore) return@launch

            _clipSearchState.update { it.copy(isLoading = true, currentSearch = searchInput) }

            try {
                val nextPage = state.currentPage
                val pageSize = 20
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

    fun groupClusters(clusterIds: List<Int>, personName: String) {
        viewModelScope.launch {
            val success = mediaGridRepository.apiCreateFace(clusterIds, personName)
            if (success) {
                loadPeople()
            }
        }
    }

    fun deselectPeople() {
        viewModelScope.launch {
            _mediaGridState.update {
                it.copy(
                    selectedPeople = emptyMap(),
                    isSelectingPerson = false
                )
            }
        }
    }

    fun togglePersonSelection(person: Person) {
        viewModelScope.launch {
            _mediaGridState.update { currentState ->
                val updatedSelectedPeople = currentState.selectedPeople.toMutableMap()
                var isSelectingPerson = currentState.isSelectingPerson

                if (updatedSelectedPeople.containsKey(person.personId)) {

                    updatedSelectedPeople.remove(person.personId)
                    if (updatedSelectedPeople.isEmpty()) {

                        isSelectingPerson = false
                    }
                } else {

                    updatedSelectedPeople[person.personId] = person
                    isSelectingPerson = true
                }

                currentState.copy(
                    selectedPeople = updatedSelectedPeople,
                    isSelectingPerson = isSelectingPerson
                )
            }
        }
    }

    fun confirmPersonClustering() {
        _showNameDialog.value = true
    }

    fun onNameConfirmed(personName: String) {
        viewModelScope.launch {
            val selectedPeople = mediaGridState.value.selectedPeople
            if (selectedPeople.isNotEmpty()) {
                val clusterIds = selectedPeople
                    .values
                    .filterIsInstance<UnknownPerson>()
                    .map { it.personId }

                if (clusterIds.isNotEmpty()) {
                    groupClusters(clusterIds, personName)

                    _mediaGridState.update {
                        it.copy(
                            selectedPeople = emptyMap(),
                            isSelectingPerson = false
                        )
                    }
                }
            }
            _showNameDialog.value = false

        }
    }

    fun dismissNameDialog() {
        _showNameDialog.value = false
    }

    fun clearSearchResults() {
        viewModelScope.launch {
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


    fun uploadSingle(localMedia: LocalMedia) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.i("UPLOAD", "uploading ${localMedia.path}")
            _fullscreenImageState.update { it.copy(uploading = false) }
            if (localMedia.checksum == null) {
                val checksum =
                    mediaGridRepository.getOrComputeChecksum(localMedia.id, localMedia.path)
                localMedia.checksum = checksum
            }

            val result = mediaGridRepository.uploadMedia(listOf(localMedia)).firstOrNull()
            _fullscreenImageState.update { currState ->
                currState.copy(
                    uploading = false,
                    currentMediaAsset = localMedia.copy(remoteId = result?.first)
                )
            }
            if (result?.first != null) {
                updateMediaUploads(listOf(Pair(result.first, localMedia.checksum!!)))
            }
        }
    }


    suspend fun getRemoteAssetPreviewUrl(id: String): String {
        return mediaGridRepository.apiGetPreview(id)
    }



    private fun updateMediaUploads(updates: List<Pair<String?, String>>) {
        viewModelScope.launch {

            val mediaList = _mediaGridState.value.media.toMutableList()
            for ((remoteId, checksum) in updates) {
                if (remoteId == null) {
                    continue
                }
                val index = mediaList.indexOfFirst { it.checksum == checksum && it is LocalMedia }

                if (index != -1) {
                    val media = mediaList[index] as LocalMedia
                    mediaList[index] = media.copy(remoteId = remoteId)
                }
            }

            _mediaGridState.update { currState ->
                currState.copy(media = mediaList)
            }
        }
    }


    private fun setIsLoading(isLoading: Boolean) {
        viewModelScope.launch {
            _mediaGridState.update { currState ->
                currState.copy(isLoading = isLoading)
            }
        }
    }


    private fun loadPeople() {
        viewModelScope.launch {
            val peopleList = mediaGridRepository.apiGetPeople()
            _mediaGridState.update { currState ->
                currState.copy(people = peopleList)
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

    fun deselectAll() {
        viewModelScope.launch {
            _mediaGridState.update { currState ->
                currState.copy(
                    isSelecting = false,
                    selected = mapOf(),
                    selectingType = SelectingType.None
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


    private fun setSyncProgress(progress: Int, max: Int) {
        viewModelScope.launch {
            _mediaGridState.update { currState ->
                currState.copy(syncProgress = Pair(progress, max))
            }
        }
    }

    fun shareLocalImages(context: Context, media: List<MediaAsset>) {
        if (_mediaGridState.value.selectingType == SelectingType.Local) {
            shareImages(context, media.map { it as LocalMedia })
            deselectAll()
        }
    }

    private fun setUploadProgress(progress: Int, max: Int) {
        viewModelScope.launch {
            _mediaGridState.update { currState ->
                currState.copy(uploadProgress = Pair(progress, max))
            }
        }
    }

    private fun setIsUploading(isUploading: Boolean) {
        _mediaGridState.update {
            it.copy(isUploading = isUploading)
        }
    }


    fun uploadMultipleMedia(mediaList: List<MediaAsset>) {
        viewModelScope.launch {
            setIsUploading(true)
            val remoteIds = mediaGridRepository.uploadMedia(
                localMedia = mediaList.map { it as LocalMedia }.filter { it.remoteId == null },
                setProgress = { setUploadProgress(it, mediaList.size) }
            )
            updateMediaUploads(remoteIds)
            setIsUploading(false)
        }
        deselectAll()
    }

    private fun setDownloadProgress(progress: Int, max: Int) {
        viewModelScope.launch {
            _mediaGridState.update { currState ->
                currState.copy(downloadProgress = Pair(progress, max))
            }
        }
    }

    private fun setIsDownloading(isDownloading: Boolean) {
        _mediaGridState.update {
            it.copy(isDownloading = isDownloading)
        }
    }


    fun downloadSingle(media: RemoteMedia, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _fullscreenImageState.update {
                it.copy(downloadState = DownloadingState.Downloading)
            }
            val result = APIUtils.downloadMedia(
                context = context,
                mediaList = listOf(media),
                sharedPreferences = mediaGridRepository.sharedPreferences
            )
            val success = if (result.isEmpty()) {
                false
            } else {
                result.first()
            }
            _fullscreenImageState.update {
                it.copy(downloadState = if (success) DownloadingState.Downloaded else null)
            }
            if (success) {
                refreshMediaGrid(context)
            }
        }
    }

    fun downloadMultipleMedia(mediaList: List<MediaAsset>, context: Context) {
        viewModelScope.launch {
            setIsDownloading(true)
            APIUtils.downloadMedia(
                context = context,
                mediaList = mediaList.map { it as RemoteMedia },
                sharedPreferences = mediaGridRepository.sharedPreferences,
                setProgress = { setDownloadProgress(it, mediaList.size) }
            )
            setIsDownloading(false)
            refreshMediaGrid(context)
        }
        deselectAll()
    }

    fun resetDownloadState() {
        viewModelScope.launch {
            _fullscreenImageState.update { it.copy(downloadState = null) }
        }
    }

}
