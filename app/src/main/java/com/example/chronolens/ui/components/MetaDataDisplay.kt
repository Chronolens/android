package com.example.chronolens.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.chronolens.R
import com.example.chronolens.models.FullMedia
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MetadataDisplay(fullMedia: FullMedia) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        fullMedia.createdAt?.let { timestamp ->
            item {
                val formattedDate = SimpleDateFormat("dd MMMM yyyy - HH:mm", Locale.getDefault())
                    .format(Date(timestamp))

                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }
        }

        if (fullMedia.make == null &&
            fullMedia.model == null &&
            fullMedia.exposureTime == null &&
            fullMedia.fNumber == null &&
            fullMedia.photographicSensitivity == null &&
            fullMedia.imageWidth == null &&
            fullMedia.imageLength == null &&
            fullMedia.latitude == null &&
            fullMedia.longitude == null
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No details available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiary
                )
            }
        }

        if (fullMedia.make != null ||
            fullMedia.model != null ||
            fullMedia.exposureTime != null ||
            fullMedia.fNumber != null ||
            fullMedia.photographicSensitivity != null
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                CameraDetails(
                    phoneMake = fullMedia.make,
                    phoneModel = fullMedia.model,
                    exposureTime = fullMedia.exposureTime,
                    fNumber = fullMedia.fNumber,
                    iso = fullMedia.photographicSensitivity
                )
            }
        }

        if (fullMedia.imageWidth != null ||
            fullMedia.imageLength != null ||
            fullMedia.fileName != null ||
            fullMedia.fileSize != null
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                PhotoDetails(
                    photoWidthValue = fullMedia.imageWidth?.toString(),
                    photoHeightValue = fullMedia.imageLength?.toString(),
                    photoNameValue = fullMedia.fileName,
                    photoSizeValue = fullMedia.fileSize?.toString()
                )
            }
        }

        if (fullMedia.latitude != null || fullMedia.longitude != null) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                PhotoGPSInfo(
                    latitude = fullMedia.latitude?.toString(),
                    longitude = fullMedia.longitude?.toString()
                )
            }
        }
    }
}


@Composable
fun PhotoGPSInfo(latitude: String?, longitude: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.mappin),
            contentDescription = "Map",
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Location",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White
            )

            val locationText = "${latitude ?: "N/A"} • ${longitude ?: "N/A"}"

            Text(
                text = locationText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onTertiary
            )
        }
    }
}


@Composable
fun CameraDetails(
    phoneMake: String?,
    phoneModel: String?,
    exposureTime: String?,
    fNumber: String?,
    iso: String?
) {
    val exposureTimeFraction = exposureTime?.toFloatOrNull()?.let {
        if (it > 0) "1/${(1 / it).toInt()}" else "N/A"
    } ?: "N/A"

    val fStop = fNumber?.toFloatOrNull()?.let { "f/${"%.1f".format(it)}" } ?: "N/A"
    val isoValue = iso ?: "N/A"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.devicemobilecamera),
            contentDescription = "Phone",
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = phoneMake ?: "N/A",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White
            )
            Text(
                text = "${phoneModel ?: "N/A"} • $fStop • $exposureTimeFraction • ISO $isoValue",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onTertiary
            )
        }
    }
}


@Composable
fun PhotoDetails(
    photoWidthValue: String?,
    photoHeightValue: String?,
    photoNameValue: String?,
    photoSizeValue: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.imagesquare),
            contentDescription = "Photo",
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = photoNameValue ?: "N/A",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White
            )
            val resolutionText = "${photoWidthValue ?: "N/A"} x ${photoHeightValue ?: "N/A"}"

            val fileSizeText = photoSizeValue?.let {
                val fileSize = it.toLong()
                val fileSizeKB = fileSize / 1024
                val fileSizeMB = fileSizeKB / 1024
                if (fileSizeMB > 0) {
                    "${fileSizeMB} MB"
                } else {
                    "${fileSizeKB} KB"
                }
            } ?: "${photoSizeValue ?: "N/A"} bytes"

            Text(
                text = "$resolutionText • $fileSizeText",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onTertiary
            )
        }
    }
}


@Composable
fun MetadataItem(key: String, value: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = key,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value ?: "N/A",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
    }
}