package com.example.chronolens.models

import android.os.Parcelable
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
    var id: String,
    var mimeType: String,
    override var checksum: String?,
    override var timestamp: Long
) : MediaAsset(checksum, timestamp) {

    override fun eq(other: MediaAsset): Boolean {
        return other is LocalMedia && other.remoteId == remoteId
    }

}

data class RemoteMedia(
    var id: String,
    override var checksum: String?,
    override var timestamp: Long
) : MediaAsset(checksum, timestamp) {

    override fun eq(other: MediaAsset): Boolean {
        return (other is RemoteMedia) && (other.id == id)
    }

    companion object {
        fun fromJson(obj: JSONObject): RemoteMedia {
            val id = obj.getString("id")
            val checksum = obj.getString("hash")
            val timestamp = obj.getLong("created_at")

            return RemoteMedia(id, checksum, timestamp);
        }
    }

}

