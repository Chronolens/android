package com.example.chronolens.models

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable


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
    override fun BuildCard(){
        Box() {
            Text(text = name)
        }
    }
}

data class UnknownPerson(
    var clusterId: Int,
    override var photoLink: String,
    override var bounding_box: List<Float>
) : Person(photoLink, bounding_box) {

    @Composable
    override fun BuildCard(){
        Box() {
            Text(text = "")
        }
    }

}
