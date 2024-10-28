package com.example.chronolens

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()
        enableEdgeToEdge()
        setContent {
            ChronoLens()
        }
    }

    private val readExternal = READ_EXTERNAL_STORAGE
    private val readVideo = READ_MEDIA_VIDEO
    private val readImages = READ_MEDIA_IMAGES
    private val permissions = arrayOf(
        readVideo, readImages
    )

    //register a permissions activity launcher for multiple permissions
    private val videoImagesPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionMap ->
            if (permissionMap.all { it.value }) {
                Toast.makeText(this, "Media permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Media permissions not granted!", Toast.LENGTH_SHORT).show()
            }
        }

    //register a permissions activity launcher for a single permission
    private val readExternalPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(this, "Read external storage permission granted", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(this, "Read external storage permission denied!", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    private fun requestPermissions() {
        //check the API level
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            //filter permissions array in order to get permissions that have not been granted
            val notGrantedPermissions = permissions.filterNot { permission ->
                ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            }
            if (notGrantedPermissions.isNotEmpty()) {
                //check if permission was previously denied and return a boolean value
                val showRationale = notGrantedPermissions.any { permission ->
                    shouldShowRequestPermissionRationale(permission)
                }
                //if true, explain to user why granting this permission is important
                if (showRationale) {
                    AlertDialog.Builder(this)
                        .setTitle("Storage Permission")
                        .setMessage("Storage permission is needed in order to show images and videos")
                        .setNegativeButton("Cancel") { dialog, _ ->
                            Toast.makeText(
                                this,
                                "Read media storage permission denied!",
                                Toast.LENGTH_SHORT
                            ).show()
                            dialog.dismiss()
                        }
                        .setPositiveButton("OK") { _, _ ->
                            videoImagesPermission.launch(notGrantedPermissions.toTypedArray())
                        }
                        .show()
                } else {
                    //launch the videoPermission ActivityResultContract
                    videoImagesPermission.launch(notGrantedPermissions.toTypedArray())
                }
            } else {
                Toast.makeText(this, "Read media storage permission granted", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            //check if permission is granted
            if (ContextCompat.checkSelfPermission(
                    this,
                    readExternal
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(this, "Read external storage permission granted", Toast.LENGTH_SHORT)
                    .show()
            } else {
                if (shouldShowRequestPermissionRationale(readExternal)) {
                    AlertDialog.Builder(this)
                        .setTitle("Storage Permission")
                        .setMessage("Storage permission is needed in order to show images and video")
                        .setNegativeButton("Cancel") { dialog, _ ->
                            Toast.makeText(
                                this,
                                "Read external storage permission denied!",
                                Toast.LENGTH_SHORT
                            ).show()
                            dialog.dismiss()
                        }
                        .setPositiveButton("OK") { _, _ ->
                            readExternalPermission.launch(readExternal)
                        }
                        .show()
                } else {
                    readExternalPermission.launch(readExternal)
                }
            }
        }
    }
}
