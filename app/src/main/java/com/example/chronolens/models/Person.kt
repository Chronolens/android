package com.example.chronolens.models

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

abstract class Person(
    @Transient open var photoLink: String,
    @Transient open var bounding_box: List<Int>,
    @Transient open var photoBitmap: Bitmap? = null
) {
    @Composable
    abstract fun BuildCard()
}

data class KnownPerson(
    var personId: Int,
    var name: String,
    override var photoLink: String,
    override var bounding_box: List<Int>,
    override var photoBitmap: Bitmap? = null
) : Person(photoLink, bounding_box, photoBitmap) {

    @Composable
    override fun BuildCard() {
        Box {
            Text(text = name)
        }
    }

    // TODO: loading from the json in sequential order is not ideal
    companion object {
        suspend fun fromJson(personJson: JSONObject): KnownPerson {
            val personId = personJson.optInt("face_id")
            val name = personJson.optString("name", "")
            val photoLink = personJson.optString("photo_link", "")
            val boundingBoxArray = personJson.optJSONArray("bbox")

            val boundingBox = mutableListOf<Int>()
            if (boundingBoxArray != null) {
                for (i in 0 until boundingBoxArray.length()) {
                    boundingBox.add(boundingBoxArray.getDouble(i).toInt())
                }
            }

            val photoBitmap = withContext(Dispatchers.IO) { fetchImage(photoLink) }

            var bboxWidth = boundingBox[2] - boundingBox[0]
            var bboxHeight = boundingBox[3] - boundingBox[1]

            if (bboxWidth > bboxHeight) {
                val diff = bboxWidth - bboxHeight
                boundingBox[1] -= diff / 2
//                boundingBox[3] += diff / 2
                bboxHeight = bboxWidth
            } else {
                val diff = bboxHeight - bboxWidth
                boundingBox[0] -= diff / 2
//                boundingBox[2] += diff / 2
                bboxWidth = bboxHeight
            }

            val croppedBitmap = photoBitmap?.let { Bitmap.createBitmap(it, boundingBox[0], boundingBox[1], bboxWidth, bboxHeight) }
            photoBitmap?.recycle()

            val resizedBitmap = Bitmap.createScaledBitmap(croppedBitmap!!, 256, 256, false)
            croppedBitmap.recycle()

            return KnownPerson(personId, name, photoLink, boundingBox, resizedBitmap)
        }
    }
}

data class UnknownPerson(
    var clusterId: Int,
    override var photoLink: String,
    override var bounding_box: List<Int>,
    override var photoBitmap: Bitmap? = null
) : Person(photoLink, bounding_box, photoBitmap) {

    @Composable
    override fun BuildCard() {
        Box {
            Text(text = "")
        }
    }

    companion object {
        suspend fun fromJson(personJson: JSONObject): UnknownPerson {
            val clusterId = personJson.optInt("cluster_id")
            val photoLink = personJson.optString("photo_link", "")
            val boundingBoxArray = personJson.optJSONArray("bbox")

            val boundingBox = mutableListOf<Int>()
            if (boundingBoxArray != null) {
                for (i in 0 until boundingBoxArray.length()) {
                    boundingBox.add(boundingBoxArray.getDouble(i).toInt())
                }
            }

            val photoBitmap = withContext(Dispatchers.IO) { fetchImage(photoLink) }

            var cropWidth = boundingBox[2] - boundingBox[0]
            var cropHeight = boundingBox[3] - boundingBox[1]

            if (cropWidth > cropHeight) {
                val diff = cropWidth - cropHeight
                boundingBox[1] -= diff / 2
//                boundingBox[3] += diff / 2
                cropHeight = cropWidth
            } else {
                val diff = cropHeight - cropWidth
                boundingBox[0] -= diff / 2
//                boundingBox[2] += diff / 2
                cropWidth = cropHeight
            }

            val croppedBitmap = photoBitmap?.let { Bitmap.createBitmap(it, boundingBox[0], boundingBox[1], cropWidth, cropHeight) }
            photoBitmap?.recycle()

            val resizedBitmap = Bitmap.createScaledBitmap(croppedBitmap!!, 256, 256, false)
            croppedBitmap.recycle()

            return UnknownPerson(clusterId, photoLink, boundingBox, resizedBitmap)
        }
    }
}

// Function to fetch an image from a URL and retrieve Bitmap
private fun fetchImage(url: String): Bitmap? {
    return try {
        val connection = URL(url).openStream()
        BitmapFactory.decodeStream(connection)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
