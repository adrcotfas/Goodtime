package com.apps.adrcotfas.goodtime.settings.notification_sounds

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apps.adrcotfas.goodtime.bl.SoundPlayer
import com.apps.adrcotfas.goodtime.common.findActivity
import com.apps.adrcotfas.goodtime.common.getFileName
import com.apps.adrcotfas.goodtime.data.settings.SoundData
import com.apps.adrcotfas.goodtime.settings.SoundsViewModel
import com.apps.adrcotfas.goodtime.ui.common.PreferenceGroupTitle
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun NotificationSoundPickerDialog(
    viewModel: SoundsViewModel = koinViewModel(),
    title: String,
    selectedItem: SoundData,
    onSelected: (SoundData) -> Unit,
    onSave: (SoundData) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val activity = context.findActivity()
    val soundPlayer = koinInject<SoundPlayer>()

    val pickSoundLauncher = rememberLauncherForActivityResult(
        contract = object : ActivityResultContracts.OpenDocument() {
            override fun createIntent(context: Context, input: Array<String>): Intent {
                return super.createIntent(context, input).apply {
                    addFlags(FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "audio/*"
                }
            }
        }
    ) { uri ->
        if (uri != null) {
            context.getFileName(uri)?.let {
                val soundData = SoundData(name = it, uriString = uri.toString())
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                viewModel.saveUserSound(soundData)
                onSelected(soundData)
            }
        }
    }

    activity?.let {
        LaunchedEffect(Unit) {
            viewModel.fetchNotificationSounds(activity)
        }
    }

    val items by viewModel.notificationSoundData.collectAsStateWithLifecycle()
    val userItems by viewModel.userSoundData.collectAsStateWithLifecycle()

    NotificationSoundPickerDialogContent(
        title = title,
        selectedItem = selectedItem,
        items = items,
        userItems = userItems,
        onAddSoundClick = {
            coroutineScope.launch {
                soundPlayer.stop()
            }
            pickSoundLauncher.launch(arrayOf("audio/*"))
        },
        onRemoveUserSound = {
            viewModel.removeUserSound(it)
        },
        onSelected = {
            onSelected(it)
            coroutineScope.launch {
                soundPlayer.play(it)
            }
        },
        onSave = onSave,
        onDismiss = {
            coroutineScope.launch {
                soundPlayer.stop()
            }
            onDismiss()
        }
    )
}

@Composable
private fun NotificationSoundPickerDialogContent(
    title: String,
    selectedItem: SoundData,
    items: Set<SoundData>,
    userItems: Set<SoundData>,
    onSelected: (SoundData) -> Unit,
    onSave: (SoundData) -> Unit,
    onDismiss: () -> Unit,
    onAddSoundClick: () -> Unit,
    onRemoveUserSound: (SoundData) -> Unit
) {

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier
                .background(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface
                ),
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .fillMaxHeight(0.75f),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    modifier = Modifier
                        .padding(start = 24.dp)
                        .fillMaxWidth(),
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )

                LazyColumn(modifier = Modifier.weight(1f)) {
                    item(key = "user sounds") {
                        PreferenceGroupTitle(modifier = Modifier.animateItem(),  text = "Your sounds")
                    }
                    items(userItems.toList(), key = { "user" + it.uriString }) { item ->
                        val isSelected = selectedItem == item
                        NotificationSoundItem(
                            modifier = Modifier.animateItem(),
                            name = item.name,
                            isSelected = isSelected,
                            isCustomSound = true,
                            onRemove = { onRemoveUserSound(item) }
                        ) {
                            onSelected(item)
                        }
                    }
                    item(key = "add custom sound") {
                        AddCustomSoundButton(modifier = Modifier.animateItem(), onAddUserSound = onAddSoundClick)
                    }
                    item(key = "system sounds") {
                        PreferenceGroupTitle(modifier = Modifier.animateItem(), text = "System sounds")
                    }
                    item(key = "silent") {
                        NotificationSoundItem(
                            modifier = Modifier.animateItem(),
                            name = "Silent",
                            isSilent = true,
                            isSelected = selectedItem.uriString == Uri.EMPTY.toString()
                        ) {
                            onSelected(SoundData(isSilent = true))
                        }
                    }
                    items(items.toList(), key = { it.uriString }) { item ->
                        val isSelected = selectedItem == item
                        NotificationSoundItem(
                            modifier = Modifier.animateItem(),
                            name = item.name,
                            isSelected = isSelected
                        ) {
                            onSelected(item)
                        }
                    }
                }
                ButtonsRow(onSave, onDismiss, selectedItem)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotificationSoundItem(
    modifier: Modifier = Modifier,
    name: String,
    isSilent: Boolean = false,
    isCustomSound: Boolean = false,
    isSelected: Boolean,
    onRemove: (() -> Unit)? = null,
    onSelected: () -> Unit,
) {
    var dropDownMenuExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .let {
                if (isSelected) it.background(
                    MaterialTheme.colorScheme.inverseSurface.copy(
                        alpha = 0.1f
                    )
                ) else it
            }
            .combinedClickable(onClick = {
                onSelected()
            }, onLongClick = {
                if (onRemove != null) {
                    dropDownMenuExpanded = true
                }
            })
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onRemove != null) {
            DropdownMenu(
                expanded = dropDownMenuExpanded,
                onDismissRequest = { dropDownMenuExpanded = false }) {
                DropdownMenuItem(
                    text = { Text("Remove") },
                    onClick = {
                        onRemove()
                        dropDownMenuExpanded = false
                    })
            }
        }

        Icon(
            imageVector =
            if (isCustomSound) Icons.Filled.MusicNote
            else if (isSilent) Icons.Filled.NotificationsOff
            else Icons.Filled.NotificationsActive,
            contentDescription = null,
            modifier = Modifier.padding(end = 16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        if (isSelected) {
            Icon(
                imageVector =
                Icons.Filled.CheckCircle,
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = null
            )
        }
    }
}

@Composable
fun AddCustomSoundButton(modifier: Modifier = Modifier, onAddUserSound: () -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onAddUserSound() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            modifier = Modifier.padding(end = 16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Add custom sound",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}


@Composable
private fun ButtonsRow(
    onSave: (SoundData) -> Unit,
    onDismiss: () -> Unit,
    selectedItem: SoundData
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            modifier = Modifier
                .padding(end = 8.dp, bottom = 4.dp),
            onClick = onDismiss
        ) { Text(stringResource(id = android.R.string.cancel)) }

        TextButton(
            modifier = Modifier
                .padding(end = 8.dp, bottom = 4.dp),
            onClick = {
                onSave(selectedItem)
                onDismiss()
            }
        ) { Text(stringResource(id = android.R.string.ok)) }
    }
}

@Preview
@Composable
fun NotificationSoundPickerDialogPreview() {
    NotificationSoundPickerDialogContent(
        title = "Work finished notification sound",
        selectedItem = SoundData("Mallet", "Mallet"),
        onSelected = {},
        onSave = {},
        onDismiss = {},
        onAddSoundClick = { },
        userItems = setOf(
            SoundData("Custom 1", "Custom 1"),
            SoundData("Custom 2", "Custom 2"),
            SoundData("Custom 3", "Custom 3"),
        ),
        items = setOf(
            SoundData("Coconuts", "Coconuts"),
            SoundData("Mallet", "Mallet"),
            SoundData("Music Box", "Music Box"),
        ),
        onRemoveUserSound = {}
    )
}