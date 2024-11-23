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
                mediaUrl = json.getString("media_url"),
                createdAt = json.getLong("created_at"),
                fileSize = json.getLong("file_size"),
                fileName = json.getString("file_name"),
                longitude = json.optDouble("longitude").takeIf { !it.isNaN() },
                latitude = json.optDouble("latitude").takeIf { !it.isNaN() },
                imageWidth = json.optInt("image_width").takeIf { it != 0 },
                imageLength = json.optInt("image_length").takeIf { it != 0 },
                make = json.optString("make", null),
                model = json.optString("model", null),
                fNumber = json.optString("fnumber", null),
                exposureTime = json.optString("exposure_time", null),
                photographicSensitivity = json.optString("photographic_sensitivity", null),
                orientation = json.optInt("orientation").takeIf { it != 0 }
            )
        }
    }
}
