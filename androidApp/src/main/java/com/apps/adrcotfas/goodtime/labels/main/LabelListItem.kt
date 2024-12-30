package com.apps.adrcotfas.goodtime.labels.main

import android.annotation.SuppressLint
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.apps.adrcotfas.goodtime.R
import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.data.model.TimerProfile
import com.apps.adrcotfas.goodtime.data.model.isDefault
import com.apps.adrcotfas.goodtime.ui.localColorsPalette
import compose.icons.EvaIcons
import compose.icons.evaicons.Outline
import compose.icons.evaicons.outline.Archive
import compose.icons.evaicons.outline.Copy
import compose.icons.evaicons.outline.Edit
import compose.icons.evaicons.outline.MoreVertical
import compose.icons.evaicons.outline.Trash


@Composable
fun LabelListItem(
    label: Label,
    isActive: Boolean,
    isDragging: Boolean,
    @SuppressLint("ModifierParameter")
    dragModifier: Modifier,
    onActivate: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val labelName =
        if (label.isDefault()) stringResource(id = R.string.label_default) else label.name

    Crossfade(targetState = isActive, label = "Active label crossfade") { active ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .clickable { onActivate() }
                .let {
                    if (active) it.background(
                        MaterialTheme.colorScheme.secondaryContainer.copy(
                            alpha = 0.1f
                        )
                    ) else it
                }
                .let {
                    if (isDragging) it.background(
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.05f)
                    ) else it
                }
                .padding(4.dp)
        ) {
            Icon(
                modifier = dragModifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {}),
                imageVector = Icons.Filled.DragIndicator,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                contentDescription = "Drag indicator for $labelName",
            )
            Icon(
                modifier = Modifier
                    .padding(8.dp),
                imageVector = if (active) {
                    Icons.AutoMirrored.Filled.Label
                } else Icons.AutoMirrored.Outlined.Label,
                contentDescription = (if (active) {
                    "Active label"
                } else "Inactive label") + ": $labelName",
                tint = MaterialTheme.localColorsPalette.colors[label.colorIndex.toInt()]
            )
            Text(
                modifier = Modifier.weight(1f),
                text = labelName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge
            )

            if (label.isDefault()) {
                IconButton(onClick = {
                    onEdit()
                }) {
                    Icon(EvaIcons.Outline.Edit, contentDescription = "Edit $labelName")
                }
            } else {
                var dropDownMenuExpanded by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { dropDownMenuExpanded = true }) {
                        Icon(
                            EvaIcons.Outline.MoreVertical,
                            contentDescription = "More about $labelName"
                        )
                    }
                    DropdownMenu(
                        expanded = dropDownMenuExpanded,
                        onDismissRequest = { dropDownMenuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                onEdit()
                                dropDownMenuExpanded = false
                            },
                            leadingIcon = {
                                Icon(EvaIcons.Outline.Edit, contentDescription = "Edit $labelName")
                            })
                        DropdownMenuItem(
                            text = { Text("Duplicate") },
                            onClick = {
                                onDuplicate()
                                dropDownMenuExpanded = false
                            },
                            leadingIcon = {
                                Icon(
                                    EvaIcons.Outline.Copy,
                                    contentDescription = "Duplicate $labelName"
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Archive") },
                            onClick = {
                                onArchive()
                                dropDownMenuExpanded = false
                            },
                            leadingIcon = {
                                Icon(
                                    EvaIcons.Outline.Archive,
                                    contentDescription = "Archive $labelName"
                                )
                            })
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                onDelete()
                                dropDownMenuExpanded = false
                            },
                            leadingIcon = {
                                Icon(
                                    EvaIcons.Outline.Trash,
                                    contentDescription = "Delete $labelName"
                                )
                            })
                    }
                }
            }
        }

    }
}

@Preview
@Composable
fun LabelCardPreview() {
    LabelListItem(
        label = Label(
            name = "Default",
            useDefaultTimeProfile = false,
            timerProfile = TimerProfile(sessionsBeforeLongBreak = 4)
        ),
        isActive = true,
        isDragging = false,
        dragModifier = Modifier,
        onActivate = {},
        onEdit = {},
        onDuplicate = {},
        onArchive = {},
        onDelete = {}
    )
}
