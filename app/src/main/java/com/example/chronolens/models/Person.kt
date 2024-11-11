package com.example.chronolens.models

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.json.JSONObject


abstract class Person(
    @Transient open var photoLink: String,
    @Transient open var bounding_box: List<Float>,
) {
    @Composable
    abstract fun BuildCard()
}

data class KnownPerson(
    var personId: Int,
    var name: String,
    override var photoLink: String,
    override var bounding_box: List<Float>
) : Person(photoLink, bounding_box) {

    @Composable
    override fun BuildCard() {
        Box() {
            Text(text = name)
        }
    }

    companion object {
        fun fromJson(personJson: JSONObject): KnownPerson {
            val personId = personJson.optInt("person_id")
            val name = personJson.optString("name", "")
            val photoLink = personJson.optString("photo_link", "")
            val boundingBoxArray = personJson.optJSONArray("bounding_box")


            val boundingBox = mutableListOf<Float>()
            if (boundingBoxArray != null) {
                for (i in 0 until boundingBoxArray.length()) {
                    boundingBox.add(boundingBoxArray.getDouble(i).toFloat())
                }
            }


            return KnownPerson(personId, name, photoLink, boundingBox)
        }
    }
}

data class UnknownPerson(
    var clusterId: Int,
    override var photoLink: String,
    override var bounding_box: List<Float>
) : Person(photoLink, bounding_box) {

    @Composable
    override fun BuildCard() {
        Box() {
            Text(text = "")
        }
    }

    companion object {
        fun fromJson(personJson: JSONObject): UnknownPerson {
            val clusterId = personJson.optInt("cluster_id")
            val photoLink = personJson.optString("photo_link", "")
            val boundingBoxArray = personJson.optJSONArray("bounding_box")


            val boundingBox = mutableListOf<Float>()
            if (boundingBoxArray != null) {
                for (i in 0 until boundingBoxArray.length()) {
                    boundingBox.add(boundingBoxArray.getDouble(i).toFloat())
                }
            }


            return UnknownPerson(clusterId, photoLink, boundingBox)
        }
    }
}
