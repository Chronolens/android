package com.example.chronolens

import android.content.SharedPreferences
import android.util.Log
import com.example.chronolens.models.LocalMedia
import com.example.chronolens.models.RemoteMedia
import com.example.chronolens.utils.ChecksumUtils
import com.example.chronolens.utils.Json
import com.example.chronolens.utils.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlin.io.path.Path

// TODO: error checking ALL OVER THIS CLASS
class APIService {

    fun checkLogin(sharedPreferences: SharedPreferences) {
        checkValidToken(sharedPreferences)

    }

    private fun checkValidToken(sharedPreferences: SharedPreferences) {
        val oldAccessToken = sharedPreferences.getString(Prefs.ACCESS_TOKEN, "")
        val oldExpiresAt = sharedPreferences.getLong(Prefs.EXPIRES_AT, -1L)
        val oldRefreshToken = sharedPreferences.getString(Prefs.REFRESH_TOKEN, "")
        val oldServer = sharedPreferences.getString(Prefs.SERVER, "")

        // Expired token
        if (System.currentTimeMillis() >= oldExpiresAt) {
            val url = URL("$oldServer/refresh")
            val payload = JSONObject().apply {
                put("access_token", oldAccessToken)
                put("refresh_token", oldRefreshToken)
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
                    sharedPreferences.edit().putString(Prefs.REFRESH_TOKEN, refreshToken).apply()
                }

            } catch (e: Exception) {
                e.printStackTrace()

            } finally {
                connection.disconnect()
            }

        } else {


        }


    }

    // Login function
    suspend fun login(
        sharedPreferences: SharedPreferences,
        server: String,
        username: String,
        password: String
    ): Int? = withContext(Dispatchers.IO) {
        val url = URL("$server/login")
        val payload = JSONObject().apply {
            put("username", username)
            put("password", password)
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

                sharedPreferences.edit().putString(Prefs.SERVER, server).apply()
                sharedPreferences.edit().putString(Prefs.ACCESS_TOKEN, token).apply()
                sharedPreferences.edit().putLong(Prefs.EXPIRES_AT, expiresAt).apply()
                sharedPreferences.edit().putString(Prefs.REFRESH_TOKEN, refreshToken).apply()
            }
            responseCode
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            connection.disconnect()
        }
    }

    // Upload file using stream
    suspend fun uploadFileStream(
        sharedPreferences: SharedPreferences,
        asset: LocalMedia
    ): Int = withContext(Dispatchers.IO) {
        val server = sharedPreferences.getString(Prefs.SERVER, "")
        val jwtToken = sharedPreferences.getString(Prefs.ACCESS_TOKEN, "")
        val url = URL("$server/image/upload")

        val mimeType = asset.mimeType
        val path = Path(asset.path)
        val file = File(path.toUri())
        val checksum = ChecksumUtils().computeChecksum(asset.path)

        val connection = url.openConnection() as HttpURLConnection
        connection.doOutput = true

        connection.apply {
            setRequestProperty("Authorization", "Bearer $jwtToken")
            setRequestProperty("Content-Type", mimeType)
            setRequestProperty("Content-Digest", "sha-1=:$checksum:")
            setRequestProperty("Timestamp", asset.timestamp.toString())
            requestMethod = "POST"
            doOutput = true
        }

        connection.connect()
        try {
            // Stream file data to connection
            file.inputStream().use { input ->
                BufferedOutputStream(connection.outputStream).use { output ->
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                    output.flush() // Make sure all data is sent out
                }
            }
            return@withContext connection.responseCode
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection.disconnect()
        }
        0
    }

    suspend fun syncFullRemote(
        sharedPreferences: SharedPreferences
    ): List<RemoteMedia> = withContext(Dispatchers.IO) {
        val server = sharedPreferences.getString(Prefs.SERVER, "")
        Log.i("SERVER!!", server ?: "NULL")
        val accessToken =
            sharedPreferences.getString(Prefs.ACCESS_TOKEN, "") ?: return@withContext emptyList()
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
        val jwtToken = sharedPreferences.getString(Prefs.ACCESS_TOKEN, "") ?: return@withContext ""
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
        val jwtToken = sharedPreferences.getString(Prefs.ACCESS_TOKEN, "") ?: return@withContext ""
        val server = sharedPreferences.getString(Prefs.SERVER, "")
        val url = URL("$server/media/$uuid")
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

}
