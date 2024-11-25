package com.example.chronolens.models

import org.json.JSONObject


data class FullMedia(
    val id: String,
    val mediaUrl: String?,
    val createdAt: Long?,
    val fileSize: Long?,
    val fileName: String?,
    val longitude: Double?,
    val latitude: Double?,
    val imageWidth: Int?,
    val imageLength: Int?,
    val make: String?,
    val model: String?,
    val fNumber: String?,
    val exposureTime: String?,
    val photographicSensitivity: String?,
    val orientation: Int?
) {
    companion object {
        fun fromJson(json: JSONObject): FullMedia {
            return FullMedia(
                id = json.getString("id"),
                mediaUrl = getStringOrNull(json, "media_url"),
                createdAt = json.optLong("created_at"),
                fileSize = json.optLong("file_size"),
                fileName = getStringOrNull(json, "file_name"),
                longitude = json.optDouble("longitude").takeIf { !it.isNaN() },
                latitude = json.optDouble("latitude").takeIf { !it.isNaN() },
                imageWidth = json.optInt("image_width").takeIf { it != 0 },
                imageLength = json.optInt("image_length").takeIf { it != 0 },
                make = getStringOrNull(json, "make"),
                model = getStringOrNull(json, "model"),
                fNumber = getStringOrNull(json, "fnumber"),
                exposureTime = getStringOrNull(json, "exposure_time"),
                photographicSensitivity = getStringOrNull(json, "photographic_sensitivity"),
                orientation = json.optInt("orientation").takeIf { it != 0 }
            )
        }

        private fun getStringOrNull(json: JSONObject, key: String): String? {
            val value = json.optString(key, "")
            return if (value == "null") null else value
        }
    }
}