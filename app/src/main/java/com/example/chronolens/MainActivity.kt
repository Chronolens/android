package com.example.chronolens

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.chronolens.utils.createNotificationChannels

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestRequiredPermissions()
        createNotificationChannels(this)
        enableEdgeToEdge()
        setContent {
            ChronoLens()
        }
    }

    // Permissions to request based on API level
    private val permissions: List<String>
        get() {
            val basePermissions = mutableListOf(
                Manifest.permission.ACCESS_MEDIA_LOCATION
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                basePermissions.addAll(
                    listOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                )
            } else {
                basePermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

            return basePermissions
        }

    // Permission request launcher
    private val requestMultiplePermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.forEach { (permission, isGranted) ->
                if (isGranted) {
                    Log.i("PERMISSIONS", "$permission granted")
                } else {
                    Log.e("PERMISSIONS", "$permission denied")
                }
            }
            handlePermissionResults(permissions)
        }

    // Request all required permissions
    private fun requestRequiredPermissions() {
        val notGrantedPermissions = permissions.filterNot { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }

        if (notGrantedPermissions.isNotEmpty()) {
            val showRationale = notGrantedPermissions.any { permission ->
                shouldShowRequestPermissionRationale(permission)
            }

            if (showRationale) {
                showPermissionRationaleDialog(notGrantedPermissions)
            } else {
                requestMultiplePermissionsLauncher.launch(notGrantedPermissions.toTypedArray())
            }
        } else {
            Toast.makeText(this, "All required permissions granted", Toast.LENGTH_SHORT).show()
        }
    }

    // Handle permission results
    private fun handlePermissionResults(permissions: Map<String, Boolean>) {
        val deniedPermissions = permissions.filterValues { !it }
        if (deniedPermissions.isNotEmpty()) {
            Toast.makeText(
                this,
                "Some permissions were denied: ${deniedPermissions.keys.joinToString()}",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
        }
    }

    // Show rationale dialog
    private fun showPermissionRationaleDialog(permissions: List<String>) {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("This app requires permissions to function correctly. Please grant them.")
            .setPositiveButton("OK") { _, _ ->
                requestMultiplePermissionsLauncher.launch(permissions.toTypedArray())
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                Toast.makeText(
                    this,
                    "Permissions were not granted. Some features may not work.",
                    Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
            }
            .show()
    }

}
