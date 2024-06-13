package com.apps.adrcotfas.goodtime.labels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apps.adrcotfas.goodtime.data.model.Label
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivedLabelsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ArchivedLabelsViewModel = koinViewModel()
) {
    val labels by viewModel.archivedLabels.collectAsStateWithLifecycle(emptyList())
    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Archived Labels") },
                scrollBehavior = topAppBarScrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                }
            )
        },
        content = {
            LazyColumn(Modifier.padding(it).fillMaxSize()) {
                items(labels, key = { label -> label.name }) { label ->
                    ArchivedLabelListItem(
                        Modifier.animateItem(),
                        label = label,
                        onUnarchive = { viewModel.unarchiveLabel(label.name) },
                        onDelete = { viewModel.deleteLabel(label.name) })
                }
            }
        }
    )
}

@Composable
fun ArchivedLabelListItem(
    modifier: Modifier,
    label: Label,
    onUnarchive: () -> Unit,
    onDelete: () -> Unit
) {
    val labelName = label.name

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(4.dp)
    ) {
        Icon(
            modifier = Modifier
                .padding(8.dp),
            imageVector = Icons.AutoMirrored.Outlined.Label,
            contentDescription = null,
            //TODO: take color from label.colorId
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label.name,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.weight(1f))

        var dropDownMenuExpanded by remember { mutableStateOf(false) }
        Box {
            IconButton(onClick = { dropDownMenuExpanded = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "More about $labelName")
            }

            DropdownMenu(
                expanded = dropDownMenuExpanded,
                onDismissRequest = { dropDownMenuExpanded = false }) {
                DropdownMenuItem(
                    text = { Text("Unarchive") },
                    onClick = {
                        onUnarchive()
                        dropDownMenuExpanded = false
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Archive,
                            contentDescription = "Unarchive $labelName"
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
                            Icons.Filled.DeleteOutline,
                            contentDescription = "Delete $labelName"
                        )
                    })
            }
        }
    }
}