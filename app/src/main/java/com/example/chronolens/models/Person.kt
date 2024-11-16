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
    @Transient open var personId: Int,
    @Transient open var photoLink: String,
    @Transient open var bounding_box: List<Int>,
    @Transient open var photoBitmap: Bitmap? = null
) {
    @Composable
    abstract fun BuildCard()

    companion object {
        fun adjustBoundingBox(boundingBox: MutableList<Int>) {
            val bboxWidth = boundingBox[2] - boundingBox[0]
            val bboxHeight = boundingBox[3] - boundingBox[1]

            if (bboxWidth > bboxHeight) {
                val diff = bboxWidth - bboxHeight
                boundingBox[1] -= diff / 2
            } else {
                val diff = bboxHeight - bboxWidth
                boundingBox[0] -= diff / 2
            }
        }

        fun resizeAndCropBitmap(photoBitmap: Bitmap?, boundingBox: List<Int>): Bitmap? {
            val bboxWidth = boundingBox[2] - boundingBox[0]
            val bboxHeight = boundingBox[3] - boundingBox[1]

            val croppedBitmap = photoBitmap?.let {
                Bitmap.createBitmap(it, boundingBox[0], boundingBox[1], bboxWidth, bboxHeight)
            }
            photoBitmap?.recycle()

            return croppedBitmap?.let {
                Bitmap.createScaledBitmap(it, 256, 256, false).also { croppedBitmap.recycle() }
            }
        }

        suspend fun fetchImage(url: String): Bitmap? {
            return withContext(Dispatchers.IO) {
                try {
                    val connection = URL(url).openStream()
                    BitmapFactory.decodeStream(connection)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        }
    }
}

data class KnownPerson(
    override var personId: Int,
    var name: String,
    override var photoLink: String,
    override var bounding_box: List<Int>,
    override var photoBitmap: Bitmap? = null
) : Person(personId, photoLink, bounding_box, photoBitmap) {

    @Composable
    override fun BuildCard() {
        Box {
            Text(text = name)
        }
    }

    companion object {
        suspend fun fromJson(personJson: JSONObject): KnownPerson {
            val personId = personJson.optInt("face_id")
            val name = personJson.optString("name", "")
            val photoLink = personJson.optString("photo_link", "")
            val boundingBoxArray = personJson.optJSONArray("bbox")

            val boundingBox = mutableListOf<Int>().apply {
                boundingBoxArray?.let {
                    for (i in 0 until it.length()) add(it.getDouble(i).toInt())
                }
            }

            Person.adjustBoundingBox(boundingBox)

            val photoBitmap = Person.fetchImage(photoLink)
            val resizedBitmap = Person.resizeAndCropBitmap(photoBitmap, boundingBox)

            return KnownPerson(personId, name, photoLink, boundingBox, resizedBitmap)
        }
    }
}

data class UnknownPerson(
    override var personId: Int,
    override var photoLink: String,
    override var bounding_box: List<Int>,
    override var photoBitmap: Bitmap? = null
) : Person(personId, photoLink, bounding_box, photoBitmap) {

    @Composable
    override fun BuildCard() {
        Box {
            Text(text = "Unknown Person")
        }
    }

    companion object {
        suspend fun fromJson(personJson: JSONObject): UnknownPerson {
            val personId = personJson.optInt("cluster_id")
            val photoLink = personJson.optString("photo_link", "")
            val boundingBoxArray = personJson.optJSONArray("bbox")

            val boundingBox = mutableListOf<Int>().apply {
                boundingBoxArray?.let {
                    for (i in 0 until it.length()) add(it.getDouble(i).toInt())
                }
            }

            Person.adjustBoundingBox(boundingBox)

            val photoBitmap = Person.fetchImage(photoLink)
            val resizedBitmap = Person.resizeAndCropBitmap(photoBitmap, boundingBox)

            return UnknownPerson(personId, photoLink, boundingBox, resizedBitmap)
        }
    }
}
