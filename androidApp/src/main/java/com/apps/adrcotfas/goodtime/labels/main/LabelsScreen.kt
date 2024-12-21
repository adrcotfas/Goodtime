package com.apps.adrcotfas.goodtime.labels.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.apps.adrcotfas.goodtime.R
import com.apps.adrcotfas.goodtime.data.model.isDefault
import com.apps.adrcotfas.goodtime.labels.DeleteConfirmationDialog
import com.apps.adrcotfas.goodtime.labels.add_edit.AddEditLabelScreen
import com.apps.adrcotfas.goodtime.main.Destination
import com.apps.adrcotfas.goodtime.ui.DraggableItem
import com.apps.adrcotfas.goodtime.ui.common.TopBar
import com.apps.adrcotfas.goodtime.ui.dragContainer
import com.apps.adrcotfas.goodtime.ui.rememberDragDropState
import compose.icons.EvaIcons
import compose.icons.evaicons.Outline
import compose.icons.evaicons.outline.Archive
import compose.icons.evaicons.outline.Plus
import org.koin.androidx.compose.koinViewModel

//TODO(fix bug): renaming a label should not change the active label
//TODO: what happens when switching to a different timerType label? Should we warn the user to stop the active timer first?
//TODO: consider sub-labels?
// not here but it can be part of the stats screen; the only precondition can be the name of the labels,
// for example group together according to a prefix, e.g. "Work/Label1", "Work/Label2", "Work/Label3" etc.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelsScreen(
    navController: NavController,
    viewModel: LabelsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val labels = uiState.unarchivedLabels
    val activeLabelName = uiState.activeLabelName
    val defaultLabelName = stringResource(id = R.string.label_default)

    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var labelToDelete by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    val dragDropState =
        rememberDragDropState(listState) { fromIndex, toIndex ->
            viewModel.rearrangeLabel(fromIndex, toIndex)
        }

    val showAddEditDialog = uiState.showAddEditDialog

    val activeLabelIndex = labels.indexOfFirst { it.name == activeLabelName }
    if (labels.isNotEmpty()) {
        LaunchedEffect(Unit) {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index?.let {
                if (activeLabelIndex > it) {
                    listState.scrollToItem(activeLabelIndex)
                }
            }
        }
    }

    val showFab = listState.isScrollingUp()

    Scaffold(
        modifier = Modifier
            .windowInsetsPadding(
                WindowInsets.statusBars
            ),
        topBar = {
            TopBar(title = "Labels", onNavigateBack = {
                navController.navigate(Destination.Settings.route)
            })
            CenterAlignedTopAppBar(
                title = { Text("Labels") },
                actions = {
                    ArchivedLabelsButton(uiState.archivedLabelCount) {
                        navController.navigate(
                            Destination.ArchivedLabels.route
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                enter = slideInVertically(initialOffsetY = { it * 2 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it * 2 }) + fadeOut(),
                visible = showFab
            ) {
                LargeFloatingActionButton(
                    shape = CircleShape,
                    onClick = {
                        viewModel.setShowAddEditDialog(true)
                    }
                ) {
                    Icon(EvaIcons.Outline.Plus, "Localized description")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = paddingValues
        ) {
            itemsIndexed(labels, key = { _, item -> item.name }) { index, label ->
                DraggableItem(dragDropState, index) { isDragging ->
                    LabelListItem(
                        label = label,
                        isActive = label.name == activeLabelName,
                        isDragging = isDragging,
                        dragModifier = Modifier.dragContainer(
                            dragDropState = dragDropState,
                            key = label.name,
                            onDragFinished = { viewModel.rearrangeLabelsToDisk() }
                        ),
                        onActivate = { viewModel.setActiveLabel(label.name) },
                        onEdit = {
                            viewModel.setShowAddEditDialog(true, label)
                        },
                        onDuplicate = {
                            viewModel.duplicateLabel(
                                if (label.isDefault()) defaultLabelName else label.name,
                                label.isDefault()
                            )
                        },
                        onArchive = { viewModel.setArchived(label.name, true) },
                        onDelete = {
                            labelToDelete = label.name
                            showDeleteConfirmationDialog = true
                        }
                    )
                }
            }
        }
        if (showDeleteConfirmationDialog) {
            DeleteConfirmationDialog(
                labelToDeleteName = labelToDelete,
                onConfirm = {
                    viewModel.deleteLabel(labelToDelete)
                    showDeleteConfirmationDialog = false
                },
                onDismiss = { showDeleteConfirmationDialog = false })
        }
        if (showAddEditDialog) {
            ModalBottomSheet(
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                onDismissRequest = {
                    viewModel.setShowAddEditDialog(false)
                    viewModel.resetLabelToEdit()
                }) {
                AddEditLabelScreen(
                    isEditMode = uiState.labelToEditInitialName.isNotEmpty(),
                    labelToEditInitialName = uiState.labelToEditInitialName,
                    labelToEdit = uiState.labelToEdit,
                    labelNames = uiState.labelNames,
                    onEditLabelToEdit = { label ->
                        viewModel.updateLabelToEdit(label)
                    },
                    onSave = { label ->
                        viewModel.addLabel(label)
                        viewModel.resetLabelToEdit()
                    },
                    onUpdate = { name, label ->
                        viewModel.updateLabel(name, label)
                        viewModel.resetLabelToEdit()
                    },
                    onNavigateBack = {
                        viewModel.setShowAddEditDialog(false)
                        viewModel.resetLabelToEdit()
                    }
                )
            }
        }
    }
}

@Composable
fun ArchivedLabelsButton(count: Int, onClick: () -> Unit) {
    AnimatedVisibility(visible = count > 0, enter = fadeIn(), exit = fadeOut()) {
        BadgedBox(
            modifier = Modifier.padding(end = 8.dp),
            badge = {
                Badge(
                    modifier = Modifier.padding(end = 8.dp),
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ) {
                    Text(count.let {
                        if (it > 9) "9+"
                        else it.toString()
                    })
                }
            }
        ) {
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = EvaIcons.Outline.Archive,
                    contentDescription = "Navigate to archived labels",
                )
            }
        }
    }
}

@Composable
private fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}