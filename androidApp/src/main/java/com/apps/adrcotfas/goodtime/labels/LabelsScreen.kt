package com.apps.adrcotfas.goodtime.labels

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.pinnedScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.apps.adrcotfas.goodtime.R
import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.data.model.isDefault
import com.apps.adrcotfas.goodtime.ui.DraggableItem
import com.apps.adrcotfas.goodtime.ui.dragContainer
import com.apps.adrcotfas.goodtime.ui.rememberDragDropState
import org.koin.androidx.compose.koinViewModel

//TODO: consider sub-labels?
// not here but it can be part of the stats screen; the only precondition can be the name of the labels,
// for example group together according to a prefix, e.g. "Work/Label1", "Work/Label2", "Work/Label3" etc.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelsScreen(viewModel: LabelsViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val labels = uiState.unarchivedLabels
    val activeLabelName = uiState.activeLabelName
    val defaultLabelName = stringResource(id = R.string.label_default)

    val listState = rememberLazyListState()
    val dragDropState =
        rememberDragDropState(listState) { fromIndex, toIndex ->
            viewModel.rearrangeLabel(fromIndex, toIndex)
        }

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
    val topAppBarScrollBehavior = pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Labels") },
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                enter = slideInVertically(initialOffsetY = { it * 2 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it * 2 }) + fadeOut(),
                visible = showFab
            ) {
                val lastIndex =
                    labels.filter { !it.isDefault() && !it.name.contains(defaultLabelName) }
                        .maxOfOrNull { it.name.toInt() } ?: 0
                ExtendedFloatingActionButton(
                    onClick = {
                        //TODO: navigate to AddEditLabelScreen
                        viewModel.addLabel(Label(name = "${lastIndex + 1}"))
                    },
                    icon = { Icon(Icons.Filled.Add, "Localized description") },
                    text = { Text(text = "Create label") },
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 64.dp)
        ) {
            itemsIndexed(labels, key = { _, item -> item.name }) { index, item ->
                DraggableItem(dragDropState, index) { isDragging ->
                    //TODO: use isDragging to modify the UI of the dragged item
                    LabelListItem(
                        label = item,
                        isActive = item.name == activeLabelName,
                        dragModifier = Modifier.dragContainer(dragDropState, item.name),
                        onActivate = { viewModel.setActiveLabel(item.name) },
                        //TODO: navigate to AddEditLabelScreen
                        onEdit = {},
                        onDuplicate = {
                            viewModel.duplicateLabel(
                                if (item.isDefault()) defaultLabelName else item.name,
                                item.isDefault()
                            )
                        },
                        onArchive = { viewModel.setArchived(item.name, true) },
                        onDelete = { viewModel.deleteLabel(item.name) }
                    )
                }
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