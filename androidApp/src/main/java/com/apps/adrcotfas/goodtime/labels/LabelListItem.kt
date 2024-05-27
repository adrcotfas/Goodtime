package com.apps.adrcotfas.goodtime.labels

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.apps.adrcotfas.goodtime.R
import com.apps.adrcotfas.goodtime.data.model.Label
import com.apps.adrcotfas.goodtime.data.model.TimerProfile

@Composable
fun LabelListItem(
    label: Label,
    isActive: Boolean,
    onClick: (String) -> Unit,
    onLongClick: (String) -> Unit, //TODO: replace this with a handle for rearranging labels
) {
    //TODO: integrate label info in row
    Crossfade(targetState = isActive, label = "") { active ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable {
                    onClick(label.name)
                }
                .let { if (active) it.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) else it }
                .padding(4.dp)
        ) {

            Icon(
                modifier = Modifier.padding(8.dp),
                imageVector = if (active) {
                    Icons.AutoMirrored.Filled.Label
                } else Icons.AutoMirrored.Outlined.Label, contentDescription = null,
                //TODO: take color from label.colorId
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                label.name.let { it.ifEmpty { stringResource(id = R.string.label_default) } },
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Filled.Edit, contentDescription = null)
            }
            IconButton(onClick = { /*TODO*/ onLongClick(label.name) }) {
                Icon(Icons.Filled.MoreVert, contentDescription = null)
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
        onClick = {},
        onLongClick = {},
    )
}
