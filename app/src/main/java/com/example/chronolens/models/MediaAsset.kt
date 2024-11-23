package com.example.chronolens.models

import android.graphics.Bitmap
import com.example.chronolens.utils.Json
import org.json.JSONObject


abstract class MediaAsset(
    @Transient open var checksum: String?,
    @Transient open var timestamp: Long
) {
    abstract fun eq(other: MediaAsset): Boolean
}        

data class LocalMedia(
    var remoteId: String?,
    var path: String,
    var id: Long,
    var mimeType: String,
    override var checksum: String?,
    override var timestamp: Long,
    @Transient var thumbnail: Bitmap? = null
) : MediaAsset(checksum, timestamp) {

    override fun eq(other: MediaAsset): Boolean {
        return other is LocalMedia && other.remoteId == remoteId
    }

}

data class RemoteMedia(
    var id: String,
    override var checksum: String?,
    override var timestamp: Long,
    @Transient var thumbnail: Bitmap? = null
) : MediaAsset(checksum, timestamp) {

    override fun eq(other: MediaAsset): Boolean {
        return (other is RemoteMedia) && (other.id == id)
    }

    companion object {
        fun fromJson(obj: JSONObject): RemoteMedia {
            val id = obj.getString(Json.ID)
            val checksum = obj.getString(Json.HASH)
            val timestamp = obj.getLong(Json.CREATED_AT)

            return RemoteMedia(id, checksum, timestamp);
        }
    }

}

