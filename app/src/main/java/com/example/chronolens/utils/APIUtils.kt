package com.example.chronolens.utils

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.provider.MediaStore
import android.util.Log
import com.example.chronolens.R
import com.example.chronolens.models.FullMedia
import com.example.chronolens.models.KnownPerson
import com.example.chronolens.models.LocalMedia
import com.example.chronolens.models.Person
import com.example.chronolens.models.RemoteMedia
import com.example.chronolens.models.UnknownPerson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.io.path.Path


class APIUtils {
    companion object {

        suspend fun checkLogin(sharedPreferences: SharedPreferences): Boolean {
            return refreshToken(sharedPreferences)
        }

        private suspend fun refreshToken(sharedPreferences: SharedPreferences): Boolean =
            withContext(Dispatchers.IO) {

                val oldAccessToken = sharedPreferences.getString(Prefs.ACCESS_TOKEN, null)
                val oldRefreshToken = sharedPreferences.getString(Prefs.REFRESH_TOKEN, null)
                val server = sharedPreferences.getString(Prefs.SERVER, null)

                // Login for the first time
                if (server == null || oldAccessToken == null || oldRefreshToken == null) {
                    return@withContext false
                }

                var connection: HttpURLConnection? = null
                try {
                    val url = URL("$server/refresh")
                    val payload = JSONObject().apply {
                        put(Json.ACCESS_TOKEN, oldAccessToken)
                        put(Json.REFRESH_TOKEN, oldRefreshToken)
                    }
                    val body = payload.toString()
                    connection = (url.openConnection() as HttpURLConnection).apply {
                        setRequestProperty("Content-Type", "application/json")
                        setRequestProperty("Accept", "application/json")
                        requestMethod = "POST"
                        doOutput = true
                        outputStream.write(body.toByteArray())
                    }

                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val response = connection.inputStream.bufferedReader().readText()
                        val jsonResponse = JSONObject(response)
                        val token = jsonResponse.getString(Json.ACCESS_TOKEN)
                        val expiresAt = jsonResponse.getLong(Json.EXPIRES_AT)
                        val refreshToken = jsonResponse.getString(Json.REFRESH_TOKEN)
                        sharedPreferences.edit().apply {
                            putString(Prefs.ACCESS_TOKEN, token)
                            putLong(Prefs.EXPIRES_AT, expiresAt)
                            putString(Prefs.REFRESH_TOKEN, refreshToken)
                            apply()
                        }
                        return@withContext true
                    } else {
                        return@withContext false
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    return@withContext false
                } finally {
                    connection?.disconnect()
                }
            }

        suspend fun login(
            sharedPreferences: SharedPreferences,
            server: String,
            username: String,
            password: String
        ): Int? = withContext(Dispatchers.IO) {
            val payload = JSONObject().apply {
                put(Json.USERNAME, username)
                put(Json.PASSWORD, password)
            }
            val body = payload.toString()
            var connection: HttpURLConnection? = null

            try {
                val url = URL("$server/login")
                connection = (url.openConnection() as HttpURLConnection).apply {
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Accept", "application/json")
                    requestMethod = "POST"
                    doOutput = true
                    outputStream.write(body.toByteArray())
                }
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val jsonResponse = JSONObject(response)
                    val token = jsonResponse.getString(Json.ACCESS_TOKEN)
                    val expiresAt = jsonResponse.getLong(Json.EXPIRES_AT)
                    val refreshToken = jsonResponse.getString(Json.REFRESH_TOKEN)

                    sharedPreferences.edit().apply {
                        putString(Prefs.SERVER, server)
                        putString(Prefs.USERNAME, username)
                        putString(Prefs.ACCESS_TOKEN, token)
                        putLong(Prefs.EXPIRES_AT, expiresAt)
                        putString(Prefs.REFRESH_TOKEN, refreshToken)
                        apply()
                    }
                }
                responseCode
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } finally {
                connection?.disconnect()
            }
        }


        suspend fun uploadMedia(
            sharedPreferences: SharedPreferences,
            assets: List<LocalMedia>,
            setProgress: (Int) -> Unit = {}
        ): List<Pair<String?, String>> = withContext(Dispatchers.IO) {
            val server = sharedPreferences.getString(Prefs.SERVER, null)
            val jwtToken = sharedPreferences.getString(Prefs.ACCESS_TOKEN, null)

            if (server == null || jwtToken == null) {
                EventBus.logoutEvent.emit(Unit)
                return@withContext emptyList()
            }

            val client = OkHttpClient()
            val results = mutableListOf<Pair<String?, String>>()
            var i = 0
            setProgress(i)
            for (asset in assets) {
                val file = try {
                    File(Path(asset.path).toUri())
                } catch (_: Exception) {
                    continue
                }
                val mimeType = asset.mimeType
                val checksum = asset.checksum!!

                try {
                    val requestBody = MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart(
                            checksum,
                            file.name,
                            file.asRequestBody(mimeType.toMediaTypeOrNull())
                        )
                        .build()

                    val url = "$server/image/upload"
                    val request = Request.Builder()
                        .url(url)
                        .header("Authorization", "Bearer $jwtToken")
                        .header("Timestamp", asset.timestamp.toString())
                        .post(requestBody)
                        .build()

                    client.newCall(request).execute().use { response ->
                        Log.i("UPLOAD", response.code.toString())

                        when (response.code) {
                            HttpURLConnection.HTTP_OK -> {
                                results.add(Pair(response.body?.string(), checksum))
                            }

                            HttpURLConnection.HTTP_UNAUTHORIZED -> {
                                val authed = refreshToken(sharedPreferences)
                                if (authed) {
                                    val retryResult =
                                        uploadMedia(sharedPreferences, listOf(asset)).firstOrNull()
                                    if (retryResult != null) {
                                        results.add(retryResult)
                                    } else {

                                    }
                                } else {
                                    EventBus.logoutEvent.emit(Unit)
                                    results.add(Pair(null, checksum))
                                }
                            }

                            HttpURLConnection.HTTP_PRECON_FAILED -> {
                                results.add(Pair(null, checksum))
                            }

                            else -> {
                                results.add(Pair(null, checksum))
                            }
                        }

                    }
                } catch (e: Exception) {
                    results.add(Pair(null, checksum))
                }
                setProgress(++i)
            }
            return@withContext results
        }


        suspend fun syncFullRemote(
            sharedPreferences: SharedPreferences
        ): List<RemoteMedia> = withContext(Dispatchers.IO) {
            val server = sharedPreferences.getString(Prefs.SERVER, null)
            val accessToken = sharedPreferences.getString(Prefs.ACCESS_TOKEN, null)

            if (server == null || accessToken == null) {
                EventBus.logoutEvent.emit(Unit)
            }
            var connection: HttpURLConnection? = null
            try {
                val url = URL("$server/sync/full")
                connection = (url.openConnection() as HttpURLConnection).apply {
                    setRequestProperty("Authorization", "Bearer $accessToken")
                    setRequestProperty("Accept", "application/json")
                    requestMethod = "GET"
                }

                val responseCode = connection.responseCode
                when (responseCode) {
                    HttpURLConnection.HTTP_OK -> {
                        connection.inputStream.use { inputStream ->
                            val response = inputStream.bufferedReader().readText()
                            val syncArray = JSONArray(response)

                            val mediaList = mutableListOf<RemoteMedia>()
                            for (i in 0 until syncArray.length()) {
                                val mediaJson = syncArray.getJSONObject(i)
                                mediaList.add(RemoteMedia.fromJson(mediaJson))
                            }

                            val since = connection.getHeaderField("since")?.toLong() ?: 0
                            sharedPreferences.edit().putLong(Prefs.LAST_SYNC, since).apply()
                            mediaList
                        }
                    }

                    HttpURLConnection.HTTP_UNAUTHORIZED -> {
                        val authed = refreshToken(sharedPreferences)
                        if (authed) {
                            return@withContext syncFullRemote(sharedPreferences)
                        } else {
                            EventBus.logoutEvent.emit(Unit)
                            emptyList()
                        }
                    }

                    else -> {
                        emptyList()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            } finally {
                connection?.disconnect()
            }
        }

        suspend fun syncPartialRemote(
            sharedPreferences: SharedPreferences,
            lastSync: Long
        ): Pair<List<RemoteMedia>, List<String>> = withContext(Dispatchers.IO) {
            val server = sharedPreferences.getString(Prefs.SERVER, null)
            val jwtToken = sharedPreferences.getString(Prefs.ACCESS_TOKEN, null)

            if (server == null || jwtToken == null) {
                EventBus.logoutEvent.emit(Unit)
            }

            var connection: HttpURLConnection? = null

            try {
                val url = URL("$server/sync/partial")
                connection = (url.openConnection() as HttpURLConnection).apply {
                    setRequestProperty("Authorization", "Bearer $jwtToken")
                    setRequestProperty("Since", lastSync.toString())
                    requestMethod = "GET"
                }
                val responseCode = connection.responseCode
                when (responseCode) {
                    HttpURLConnection.HTTP_OK -> {
                        val response = connection.inputStream.bufferedReader().readText()
                        val syncResponse = JSONObject(response)

                        val uploadedList = syncResponse.getJSONArray(Json.UPLOADED)
                        val deletedList = syncResponse.getJSONArray(Json.DELETED)

                        val uploaded =
                            (0 until uploadedList.length()).map { index ->
                                RemoteMedia.fromJson(
                                    uploadedList.getJSONObject(
                                        index
                                    )
                                )
                            }
                        val deleted =
                            (0 until deletedList.length()).map { deletedList.getString(it) }

                        val since = connection.getHeaderField("since")?.toLong() ?: lastSync
                        sharedPreferences.edit().putLong(Prefs.LAST_SYNC, since).apply()

                        Pair(uploaded, deleted)
                    }

                    HttpURLConnection.HTTP_UNAUTHORIZED -> {
                        val authed = refreshToken(sharedPreferences)
                        if (authed) {
                            return@withContext syncPartialRemote(sharedPreferences, lastSync)
                        } else {
                            EventBus.logoutEvent.emit(Unit)
                            Pair(emptyList(), emptyList())
                        }
                    }

                    else -> {
                        Pair(emptyList(), emptyList())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Pair(emptyList(), emptyList())
            } finally {
                connection?.disconnect()
            }
        }

        suspend fun getPreview(
            sharedPreferences: SharedPreferences,
            uuid: String
        ): String = withContext(Dispatchers.IO) {
            val server = sharedPreferences.getString(Prefs.SERVER, null)
            val jwtToken = sharedPreferences.getString(Prefs.ACCESS_TOKEN, null)

            if (server == null || jwtToken == null) {
                EventBus.logoutEvent.emit(Unit)
            }

            var connection: HttpURLConnection? = null
            try {
                val url = URL("$server/preview/$uuid")
                connection = (url.openConnection() as HttpURLConnection).apply {
                    setRequestProperty("Authorization", "Bearer $jwtToken")
                    requestMethod = "GET"
                }
                val responseCode = connection.responseCode
                when (responseCode) {
                    HttpURLConnection.HTTP_OK -> {
                        connection.inputStream.bufferedReader().readText()
                    }

                    HttpURLConnection.HTTP_UNAUTHORIZED -> {
                        val authed = refreshToken(sharedPreferences)
                        if (authed) {
                            return@withContext getPreview(sharedPreferences, uuid)
                        } else {
                            EventBus.logoutEvent.emit(Unit)
                            ""
                        }
                    }

                    else -> {
                        ""
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            } finally {
                connection?.disconnect()
            }
        }

        suspend fun getFullImage(
            sharedPreferences: SharedPreferences,
            uuid: String
        ): FullMedia? = withContext(Dispatchers.IO) {

            val jwtToken = sharedPreferences.getString(Prefs.ACCESS_TOKEN, null)
            val server = sharedPreferences.getString(Prefs.SERVER, null)

            if (server == null || jwtToken == null) {
                EventBus.logoutEvent.emit(Unit)
            }

            var connection: HttpURLConnection? = null
            try {
                val url = URL("$server/media/$uuid")
                connection = (url.openConnection() as HttpURLConnection).apply {
                    setRequestProperty("Authorization", "Bearer $jwtToken")
                    requestMethod = "GET"
                }
                val responseCode = connection.responseCode
                when (responseCode) {
                    HttpURLConnection.HTTP_OK -> {
                        val responseText = connection.inputStream.bufferedReader().readText()
                        val jsonResponse = JSONObject(responseText)
                        FullMedia.fromJson(jsonResponse)
                    }

                    HttpURLConnection.HTTP_UNAUTHORIZED -> {
                        val authed = refreshToken(sharedPreferences)
                        if (authed) {
                            return@withContext getFullImage(sharedPreferences, uuid)
                        } else {
                            EventBus.logoutEvent.emit(Unit)
                            null
                        }
                    }

                    else -> {
                        null
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                null
            } finally {
                connection?.disconnect()
            }
        }


        suspend fun getPeople(sharedPreferences: SharedPreferences): List<Person> =
            withContext(Dispatchers.IO) {

                // FIXME
                Log.i("APIUtils", "getPeople")
                sharedPreferences.getString(Prefs.SERVER, "")?.let { Log.i("APIUtils", it) }
                sharedPreferences.getString(Prefs.ACCESS_TOKEN, "")?.let { Log.i("APIUtils", it) }

                val server = sharedPreferences.getString(Prefs.SERVER, null)
                val accessToken = sharedPreferences.getString(Prefs.ACCESS_TOKEN, null)

                if (server == null || accessToken == null) {
                    EventBus.logoutEvent.emit(Unit)
                }

                var connection: HttpURLConnection? = null

                try {
                    val url = URL("$server/faces")
                    connection = (url.openConnection() as HttpURLConnection).apply {
                        setRequestProperty("Authorization", "Bearer $accessToken")
                        setRequestProperty("Accept", "application/json")
                        requestMethod = "GET"
                    }
                    val responseCode = connection.responseCode
                    when (responseCode) {
                        HttpURLConnection.HTTP_OK -> {
                            connection.inputStream.use { inputStream ->
                                val responseJson =
                                    JSONObject(inputStream.bufferedReader().readText())
                                val knownPeople = responseJson.getJSONArray(Json.KNOWN_PEOPLE)
                                val unknownPeople = responseJson.getJSONArray(Json.UNKNOWN_PEOPLE)

                                Log.i("APIUtils", responseJson.toString())
                                val peopleList = mutableListOf<Person>()

                                for (i in 0 until knownPeople.length()) {
                                    val personJson = knownPeople.getJSONObject(i)
                                    val person = KnownPerson.fromJson(personJson)
                                    peopleList.add(person)
                                }

                                for (i in 0 until unknownPeople.length()) {
                                    val personJson = unknownPeople.getJSONObject(i)
                                    val person = UnknownPerson.fromJson(personJson)
                                    peopleList.add(person)
                                }

                                println(peopleList)
                                return@withContext peopleList
                            }
                        }

                        HttpURLConnection.HTTP_UNAUTHORIZED -> {
                            val authed = refreshToken(sharedPreferences)
                            if (authed) {
                                return@withContext getPeople(sharedPreferences)
                            } else {
                                EventBus.logoutEvent.emit(Unit)
                                emptyList()
                            }
                        }

                        else -> {
                            emptyList()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    emptyList()
                } finally {
                    connection?.disconnect()
                }
            }


        // TODO: Add the type of request face or cluster to this function as input and the only thing we need to change is the URL
        suspend fun getClusterPreviewsPage(
            sharedPreferences: SharedPreferences,
            clusterId: Int,
            page: Int = 1,
            pageSize: Int = 10,
            requestType: String
        ): List<Pair<String, String>>? = withContext(Dispatchers.IO) {
            val server = sharedPreferences.getString(Prefs.SERVER, null)
            val accessToken = sharedPreferences.getString(Prefs.ACCESS_TOKEN, null)

            if (server == null || accessToken == null) {
                EventBus.logoutEvent.emit(Unit)
            }

            var connection: HttpURLConnection? = null

            try {
                val url = URL("$server/$requestType/$clusterId?page=$page&page_size=$pageSize")
                connection = (url.openConnection() as HttpURLConnection).apply {
                    setRequestProperty("Authorization", "Bearer $accessToken")
                    setRequestProperty("Accept", "application/json")
                    requestMethod = "GET"
                }
                val responseCode = connection.responseCode
                when (responseCode) {
                    HttpURLConnection.HTTP_OK -> {
                        connection.inputStream.use { inputStream ->
                            val responseJson = JSONArray(inputStream.bufferedReader().readText())
                            val previews = mutableListOf<Pair<String, String>>()

                            for (i in 0 until responseJson.length()) {
                                val item = responseJson.getJSONObject(i)
                                val preview =
                                    Pair(item.getString("id"), item.getString("preview_url"))
                                previews.add(preview)
                            }
                            return@withContext previews
                        }
                    }

                    HttpURLConnection.HTTP_FORBIDDEN -> {
                        val authed = refreshToken(sharedPreferences)
                        if (authed) {
                            return@withContext getClusterPreviewsPage(
                                sharedPreferences = sharedPreferences,
                                clusterId = clusterId,
                                page = page,
                                pageSize = pageSize,
                                requestType = requestType,
                            )
                        } else {
                            EventBus.logoutEvent.emit(Unit)
                            emptyList()
                        }
                    }

                    else -> {
                        return@withContext null
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                null
            } finally {
                connection?.disconnect()
            }
        }


        suspend fun loadNextClipSearchPage(
            sharedPreferences: SharedPreferences,
            query: String,
            page: Int = 1,
            pageSize: Int = 20
        ): List<Pair<String, String>>? = withContext(Dispatchers.IO) {
            val server = sharedPreferences.getString(Prefs.SERVER, null)
            val accessToken = sharedPreferences.getString(Prefs.ACCESS_TOKEN, null)

            if (server == null || accessToken == null) {
                EventBus.logoutEvent.emit(Unit)
            }

            var connection: HttpURLConnection? = null

            try {
                val url = URL("$server/search?query=$query&page=$page&page_size=$pageSize")
                connection = (url.openConnection() as HttpURLConnection).apply {
                    setRequestProperty("Authorization", "Bearer $accessToken")
                    setRequestProperty("Accept", "application/json")
                    requestMethod = "GET"
                }
                val responseCode = connection.responseCode
                when (responseCode) {
                    HttpURLConnection.HTTP_OK -> {
                        connection.inputStream.use { inputStream ->
                            val responseJson = JSONArray(inputStream.bufferedReader().readText())
                            val previews = mutableListOf<Pair<String, String>>()

                            for (i in 0 until responseJson.length()) {
                                val item = responseJson.getJSONObject(i)
                                val preview =
                                    Pair(item.getString("id"), item.getString("preview_url"))
                                previews.add(preview)
                            }

                            return@withContext previews
                        }
                    }

                    HttpURLConnection.HTTP_UNAUTHORIZED -> {
                        val authed = refreshToken(sharedPreferences)
                        if (authed) {
                            return@withContext loadNextClipSearchPage(
                                sharedPreferences = sharedPreferences,
                                query = query,
                                page = page,
                                pageSize = pageSize
                            )
                        } else {
                            EventBus.logoutEvent.emit(Unit)
                            emptyList()
                        }
                    }

                    else -> {
                        return@withContext null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext null
            } finally {
                connection?.disconnect()
            }
        }

        suspend fun downloadMedia(
            context: Context,
            mediaList: List<RemoteMedia>,
            sharedPreferences: SharedPreferences,
            setProgress: (Int) -> Unit = {}
        ): List<Boolean> {
            return withContext(Dispatchers.IO) {
                val albumName = context.resources.getString(R.string.app_name)
                val client = OkHttpClient()
                val results = mutableListOf<Boolean>()
                var i = 0
                setProgress(i)
                for (media in mediaList) {

                    val fullMedia = getFullImage(sharedPreferences, media.id)
                    if (fullMedia == null) {
                        results.add(false)
                        continue
                    }
                    try {
                        val request = Request.Builder().url(fullMedia.mediaUrl!!).build()
                        val response = client.newCall(request).execute()
                        if (!response.isSuccessful) {
                            results.add(false)
                            continue
                        }

                        val mimeType = response.headers["Content-Type"]
                        val inputStream = response.body?.byteStream()
                        if (inputStream == null) {
                            results.add(false)
                            response.close()
                            continue
                        }

                        val contentValues = ContentValues().apply {
                            put(MediaStore.Images.Media.DISPLAY_NAME, fullMedia.fileName)
                            put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$albumName")
                            put(MediaStore.Images.Media.DATE_TAKEN, media.timestamp)
                        }

                        val uri = context.contentResolver.insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            contentValues
                        )
                        if (uri == null) {
                            inputStream.close()
                            response.close()
                            results.add(false)
                            continue
                        }

                        val outputStream: OutputStream? =
                            context.contentResolver.openOutputStream(uri)
                        outputStream?.use { out ->
                            inputStream.copyTo(out)
                        }
                        inputStream.close()
                        response.close()

                        results.add(true)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        results.add(false)
                    }
                    setProgress(++i)
                }
                results
            }
        }

    }
}