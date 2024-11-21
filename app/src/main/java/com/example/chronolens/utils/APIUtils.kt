package com.example.chronolens.utils

import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import com.example.chronolens.models.KnownPerson
import com.example.chronolens.models.LocalMedia
import com.example.chronolens.models.Person
import com.example.chronolens.models.RemoteMedia
import com.example.chronolens.models.UnknownPerson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlin.io.path.Path

// TODO: error checking ALL OVER THIS CLASS
class APIUtils {
    companion object {

        suspend fun checkLogin(sharedPreferences: SharedPreferences): Boolean {
            return checkValidToken(sharedPreferences)
        }

        private suspend fun checkValidToken(sharedPreferences: SharedPreferences): Boolean =
            withContext(Dispatchers.IO) {

                val oldAccessToken = sharedPreferences.getString(Prefs.ACCESS_TOKEN, null)
                val oldRefreshToken = sharedPreferences.getString(Prefs.REFRESH_TOKEN, null)
                val server = sharedPreferences.getString(Prefs.SERVER, null)

                // Login for the first time
                if (server == null || oldAccessToken == null || oldRefreshToken == null) {
                    return@withContext false
                }

                // Assume token is always expired
                val url = URL("$server/refresh")
                val payload = JSONObject().apply {
                    put(Json.ACCESS_TOKEN, oldAccessToken)
                    put(Json.REFRESH_TOKEN, oldRefreshToken)
                }
                val body = payload.toString()
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Accept", "application/json")
                    requestMethod = "POST"
                    doOutput = true
                    outputStream.write(body.toByteArray())
                }

                try {
                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val response = connection.inputStream.bufferedReader().readText()
                        val jsonResponse = JSONObject(response)
                        val token = jsonResponse.getString(Json.ACCESS_TOKEN)
                        val expiresAt = jsonResponse.getLong(Json.EXPIRES_AT)
                        val refreshToken = jsonResponse.getString(Json.REFRESH_TOKEN)

                        sharedPreferences.edit().putString(Prefs.ACCESS_TOKEN, token).apply()
                        sharedPreferences.edit().putLong(Prefs.EXPIRES_AT, expiresAt).apply()
                        sharedPreferences.edit().putString(Prefs.REFRESH_TOKEN, refreshToken)
                            .apply()

                        return@withContext true
                    } else {
                        return@withContext false
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    return@withContext false

                } finally {
                    connection.disconnect()
                }
            }

        // Login function
        suspend fun login(
            sharedPreferences: SharedPreferences,
            server: String,
            username: String,
            password: String
        ): Int? = withContext(Dispatchers.IO) {
            val payload = JSONObject().apply {
                put("username", username)
                put("password", password)
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

                    sharedPreferences.edit().putString(Prefs.SERVER, server).apply()
                    sharedPreferences.edit().putString(Prefs.USERNAME, username).apply()
                    sharedPreferences.edit().putString(Prefs.ACCESS_TOKEN, token).apply()
                    sharedPreferences.edit().putLong(Prefs.EXPIRES_AT, expiresAt).apply()
                    sharedPreferences.edit().putString(Prefs.REFRESH_TOKEN, refreshToken).apply()
                }
                responseCode
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } finally {
                connection?.disconnect()
            }
        }

        // TODO: handle other error codes
        suspend fun uploadMedia(
            sharedPreferences: SharedPreferences,
            asset: LocalMedia
        ): String? = withContext(Dispatchers.IO) {
            val server = sharedPreferences.getString(Prefs.SERVER, "") ?: return@withContext null
            val jwtToken =
                sharedPreferences.getString(Prefs.ACCESS_TOKEN, "") ?: return@withContext null
            val url = "$server/image/upload"

            val file = File(Path(asset.path).toUri())
            val mimeType = asset.mimeType
            val checksum = asset.checksum!!

            val client = OkHttpClient()
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    checksum,
                    file.name,
                    file.asRequestBody(mimeType.toMediaTypeOrNull())
                )
                .build()

            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $jwtToken")
                .header("Timestamp", asset.timestamp.toString())
                .post(requestBody)
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    Log.i("UPLOAD", response.code.toString())
                    if (response.code == HttpURLConnection.HTTP_OK) {
                        return@withContext response.body?.string()
                    } else {
                        return@withContext null
                    }
                }
            } catch (e: Exception) {
                return@withContext null
            }
        }

        suspend fun syncFullRemote(
            sharedPreferences: SharedPreferences
        ): List<RemoteMedia> = withContext(Dispatchers.IO) {
            val server = sharedPreferences.getString(Prefs.SERVER, "")
            val accessToken =
                sharedPreferences.getString(Prefs.ACCESS_TOKEN, "")
                    ?: return@withContext emptyList()
            val url = URL("$server/sync/full")

            val connection = (url.openConnection() as HttpURLConnection).apply {
                setRequestProperty("Authorization", "Bearer $accessToken")
                setRequestProperty("Accept", "application/json")
                requestMethod = "GET"
            }

            try {
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
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
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            } finally {
                connection.disconnect()
            }
        }

        suspend fun syncPartialRemote(
            sharedPreferences: SharedPreferences,
            lastSync: Long
        ): Pair<List<RemoteMedia>, List<String>> = withContext(Dispatchers.IO) {
            val server = sharedPreferences.getString(Prefs.SERVER, "")
            val jwtToken =
                sharedPreferences.getString(Prefs.ACCESS_TOKEN, "") ?: return@withContext Pair(
                    emptyList(),
                    emptyList()
                )
            val url = URL("$server/sync/partial")

            val connection = (url.openConnection() as HttpURLConnection).apply {
                setRequestProperty("Authorization", "Bearer $jwtToken")
                setRequestProperty("Since", lastSync.toString())
                requestMethod = "GET"
            }

            try {
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
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
                    val deleted = (0 until deletedList.length()).map { deletedList.getString(it) }

                    val since = connection.getHeaderField("since")?.toLong() ?: lastSync
                    sharedPreferences.edit().putLong(Prefs.LAST_SYNC, since).apply()

                    Pair(uploaded, deleted)
                } else {
                    Pair(emptyList(), emptyList())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Pair(emptyList(), emptyList())
            } finally {
                connection.disconnect()
            }
        }

        suspend fun getPreview(
            sharedPreferences: SharedPreferences,
            uuid: String
        ): String = withContext(Dispatchers.IO) {
            val server = sharedPreferences.getString(Prefs.SERVER, "")
            val jwtToken =
                sharedPreferences.getString(Prefs.ACCESS_TOKEN, "") ?: return@withContext ""
            val url = URL("$server/preview/$uuid")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                setRequestProperty("Authorization", "Bearer $jwtToken")
                requestMethod = "GET"
            }

            try {
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    connection.inputStream.bufferedReader().readText()
                } else {
                    ""
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            } finally {
                connection.disconnect()
            }
        }

        suspend fun getFullImage(
            sharedPreferences: SharedPreferences,
            uuid: String
        ): String = withContext(Dispatchers.IO) {
            val jwtToken =
                sharedPreferences.getString(Prefs.ACCESS_TOKEN, "") ?: return@withContext ""
            val server = sharedPreferences.getString(Prefs.SERVER, "")
            val url = URL("$server/media/$uuid")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                setRequestProperty("Authorization", "Bearer $jwtToken")
                requestMethod = "GET"
            }

            try {
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseText = connection.inputStream.bufferedReader().readText()
                    val jsonResponse = JSONObject(responseText)
                    jsonResponse.getString("media_url")
                } else {
                    ""
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            } finally {
                connection.disconnect()
            }
        }

        suspend fun getPeople(sharedPreferences: SharedPreferences): List<Person> =
            withContext(Dispatchers.IO) {

                Log.i("APIUtils", "getPeople")
                sharedPreferences.getString(Prefs.SERVER, "")?.let { Log.i("APIUtils", it) }
                sharedPreferences.getString(Prefs.ACCESS_TOKEN, "")?.let { Log.i("APIUtils", it) }

                val server = sharedPreferences.getString(Prefs.SERVER, "")
                val accessToken = sharedPreferences.getString(Prefs.ACCESS_TOKEN, "")
                    ?: return@withContext emptyList()

                val url = "$server/faces".toHttpUrlOrNull()!!.newBuilder()
                    .build().toUrl()

                val connection = (url.openConnection() as HttpURLConnection).apply {
                    setRequestProperty("Authorization", "Bearer $accessToken")
                    setRequestProperty("Accept", "application/json")
                    requestMethod = "GET"
                }

                try {
                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        connection.inputStream.use { inputStream ->
                            val responseJson = JSONObject(inputStream.bufferedReader().readText())
                            val knownPeople = responseJson.getJSONArray("faces")
                            val unknownPeople = responseJson.getJSONArray("clusters")

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
                            return@use peopleList
                        }
                    } else {
                        emptyList()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    emptyList()
                } finally {
                    connection.disconnect()
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
            val server = sharedPreferences.getString(Prefs.SERVER, "") ?: return@withContext null
            val accessToken =
                sharedPreferences.getString(Prefs.ACCESS_TOKEN, "") ?: return@withContext null

            val url = URL("$server/$requestType/$clusterId?page=$page&page_size=$pageSize")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                setRequestProperty("Authorization", "Bearer $accessToken")
                setRequestProperty("Accept", "application/json")
                requestMethod = "GET"
            }

            return@withContext try {
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    connection.inputStream.use { inputStream ->
                        val responseJson = JSONArray(inputStream.bufferedReader().readText())
                        val previews = mutableListOf<Pair<String, String>>()

                        for (i in 0 until responseJson.length()) {
                            val item = responseJson.getJSONObject(i)
                            val preview = Pair(item.getString("id"), item.getString("preview_url"))
                            previews.add(preview)
                        }

                        previews
                    }
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } finally {
                connection.disconnect()
            }
        }


        suspend fun loadNextClipSearchPage(
            sharedPreferences: SharedPreferences,
            search: String,
            page: Int = 1,
            pageSize: Int = 20
        ): List<Pair<String, String>>? {
            val server = sharedPreferences.getString(Prefs.SERVER, "") ?: return null
            val accessToken = sharedPreferences.getString(Prefs.ACCESS_TOKEN, "") ?: return null

            // Log the API call
            Log.i("APIUtils", "loadNextClipSearchPage")
            val url = "$server/search?query=$search&page=$page&page_size=$pageSize".toHttpUrlOrNull()!!.newBuilder()
                .build().toUrl()

            Log.i("APIUtils", url.toString())

            return try {
                // Run the network operation on the background thread using withContext
                withContext(Dispatchers.IO) {
                    val connection = (url.openConnection() as HttpURLConnection).apply {
                        setRequestProperty("Authorization", "Bearer $accessToken")
                        setRequestProperty("Accept", "application/json")
                        requestMethod = "GET"
                    }

                    val responseCode = connection.responseCode
                    Log.i("APIUtils", "Response Code: $responseCode")

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        connection.inputStream.use { inputStream ->
                            val responseText = inputStream.bufferedReader().readText()
                            Log.i("APIUtils", "Response: $responseText")
                            try {
                                val responseJson = JSONArray(responseText)
                                val previews = mutableListOf<Pair<String, String>>()

                                for (i in 0 until responseJson.length()) {
                                    val item = responseJson.getJSONObject(i)
                                    val preview = Pair(item.getString("id"), item.getString("preview_url"))
                                    previews.add(preview)
                                }
                                Log.i("APIUtils", previews.toString())
                                return@withContext previews
                            } catch (e: JSONException) {
                                Log.e("APIUtils", "Error parsing JSON response", e)
                                return@withContext null
                            }
                        }
                    } else {
                        Log.e("APIUtils", "HTTP Error: $responseCode")
                        return@withContext null
                    }
                }
            } catch (e: Exception) {
                Log.e("APIUtils", "Exception during API call", e)
                return null
            }
        }
    }
}