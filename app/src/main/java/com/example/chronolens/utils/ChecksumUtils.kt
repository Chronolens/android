package com.example.chronolens.utils

import android.util.Base64
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

class ChecksumUtils {
    fun computeChecksum(path: String): String {
        val file = File(path)
        val inputStream = FileInputStream(file)
        val digest = MessageDigest.getInstance("SHA-1")
        val buffer = ByteArray(8192)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            digest.update(buffer, 0, bytesRead)
        }
        val hashBytes = digest.digest()
        inputStream.close()
        return Base64.encodeToString(hashBytes, Base64.NO_WRAP)
    }
}