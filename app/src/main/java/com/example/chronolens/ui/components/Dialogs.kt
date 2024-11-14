package com.example.chronolens.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.chronolens.R

@Composable
fun AlertConfirmDialog(
    title: String,
    text: String,
    icon: ImageVector = Icons.Filled.Warning,
    confirmOption: () -> Unit,
    visible: MutableState<Boolean>
) {

    Dialog(onDismissRequest = { visible.value = false }) {
        Card {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(15.dp)
            ) {
                Spacer(modifier = Modifier.height(5.dp))
                Icon(imageVector = icon, contentDescription = null)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.h5,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = text, style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(20.dp))
                Row {
                    Spacer(modifier = Modifier.weight(0.5f))
                    TextButton(onClick = { visible.value = false }) {
                        Text(text = stringResource(R.string.cancel), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = {
                            visible.value = false
                            confirmOption()
                        }
                    ) {
                        Text(text = stringResource(R.string.confirm), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.weight(0.5f))
                }
            }

        }
    }

}

//@Composable
//@Preview(showSystemUi = true)
//fun AlertDialogPrev() {
//    Box(modifier = Modifier.fillMaxSize()) {
//        AlertConfirmDialog(
//            title = "Upload All Media Now",
//            text = "Are you sure bro?",
//            confirmOption = {},
//            visible = mutableStateOf(true)
//        )
//    }
//}
