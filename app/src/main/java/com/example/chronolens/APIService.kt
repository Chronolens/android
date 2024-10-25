package com.example.chronolens

import android.content.SharedPreferences
import com.example.chronolens.models.LocalMedia
import com.example.chronolens.models.RemoteMedia
import com.example.chronolens.utils.ChecksumUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlin.io.path.Path

class APIService {
    // Login function
    fun login(
        sharedPreferences: SharedPreferences,
        username: String,
        password: String,
        baseUrl: String
    ): Int? {
        val url = URL("$baseUrl/login")
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

        return try {
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().readText()
                val jsonResponse = JSONObject(response)
                val token = jsonResponse.getString("access_token")

                sharedPreferences.edit().putString("JWT_TOKEN", token).apply()
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
        asset: LocalMedia,
        baseUrl: String
    ): Int = withContext(Dispatchers.IO){
        val jwtToken = sharedPreferences.getString("JWT_TOKEN", "") ?: return@withContext 0
        val url = URL("$baseUrl/image/upload")

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

fun syncFullRemote(
    sharedPreferences: SharedPreferences,
    baseUrl: String
): List<RemoteMedia> {
    val jwtToken =
        sharedPreferences.getString("JWT_TOKEN", "") ?: return emptyList()
    val url = URL("$baseUrl/sync/full")

    val connection = (url.openConnection() as HttpURLConnection).apply {
        setRequestProperty("Authorization", "Bearer $jwtToken")
        requestMethod = "GET"
    }

    return try {
        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val response = connection.inputStream.bufferedReader().readText()
            val syncArray = JSONArray(response)
            val mediaList = mutableListOf<RemoteMedia>()
            for (i in 0 until syncArray.length()) {
                val mediaJson = syncArray.getJSONObject(i)
                mediaList.add(RemoteMedia.fromJson(mediaJson))
            }

            val since = connection.getHeaderField("since")?.toLong() ?: 0
            sharedPreferences.edit().putLong("last_sync", since).apply()
            mediaList
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

fun syncPartialRemote(
    sharedPreferences: SharedPreferences,
    lastSync: Long,
    baseUrl: String
): Pair<List<RemoteMedia>, List<String>> {
    val jwtToken = sharedPreferences.getString("JWT_TOKEN", "") ?: return Pair(
        emptyList(),
        emptyList()
    )
    val url = URL("$baseUrl/sync/partial")

    val connection = (url.openConnection() as HttpURLConnection).apply {
        setRequestProperty("Authorization", "Bearer $jwtToken")
        setRequestProperty("Since", lastSync.toString())
        requestMethod = "GET"
    }

    return try {
        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val response = connection.inputStream.bufferedReader().readText()
            val syncResponse = JSONObject(response)

            val uploadedList = syncResponse.getJSONArray("uploaded")
            val deletedList = syncResponse.getJSONArray("deleted")

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
            sharedPreferences.edit().putLong("last_sync", since).apply()

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

fun getPreview(
    sharedPreferences: SharedPreferences,
    uuid: String,
    baseUrl: String
): String {
    val jwtToken = sharedPreferences.getString("JWT_TOKEN", "") ?: return ""

    val url = URL("$baseUrl/preview/$uuid")
    val connection = (url.openConnection() as HttpURLConnection).apply {
        setRequestProperty("Authorization", "Bearer $jwtToken")
        requestMethod = "GET"
    }

    return try {
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

fun getFullImage(
    sharedPreferences: SharedPreferences,
    uuid: String,
    baseUrl: String
): String {
    val jwtToken = sharedPreferences.getString("JWT_TOKEN", "") ?: return ""

    val url = URL("$baseUrl/media/$uuid")
    val connection = (url.openConnection() as HttpURLConnection).apply {
        setRequestProperty("Authorization", "Bearer $jwtToken")
        requestMethod = "GET"
    }

    return try {
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
