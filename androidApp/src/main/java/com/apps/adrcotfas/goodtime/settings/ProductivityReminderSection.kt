package com.apps.adrcotfas.goodtime.settings

import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.apps.adrcotfas.goodtime.ui.ApplicationTheme
import com.apps.adrcotfas.goodtime.utils.secondsOfDayToTimerFormat
import kotlinx.datetime.DayOfWeek
import java.time.format.TextStyle

@Composable
fun ProductivityReminderSection(
    firstDayOfWeek: DayOfWeek,
    selectedDays: Set<DayOfWeek>,
    reminderSecondOfDay: Int,
    onSelectDay: (DayOfWeek) -> Unit,
    onReminderTimeClick: () -> Unit
) {
    val context = LocalContext.current
    val locale = context.resources.configuration.locales[0]

    val iconButtonColors = IconButtonDefaults.filledIconButtonColors()
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = "Days of the week",
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val daysInOrder = rotateList(DayOfWeek.entries.toList(), firstDayOfWeek)
            for (day in daysInOrder) {
                FilledIconButton(
                    colors =
                    if (selectedDays.contains(day)) iconButtonColors.copy(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                    else iconButtonColors.copy(
                        containerColor = iconButtonColors.disabledContainerColor,
                        contentColor = iconButtonColors.disabledContentColor
                    ),
                    onClick = { onSelectDay(day) }) {
                    Text(
                        text = day.getDisplayName(TextStyle.SHORT, locale),
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        val timeModifier =
            if (selectedDays.isEmpty()) Modifier else Modifier
                .clickable {
                    onReminderTimeClick()
                }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = timeModifier
                .padding(16.dp)
        ) {
            val textColor =
                if (selectedDays.isEmpty()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                else MaterialTheme.colorScheme.onSurface
            Text(
                modifier = Modifier.weight(1f),
                text = "Reminder time",
                style = MaterialTheme.typography.bodyLarge,
                color = textColor
            )
            Text(
                text = secondsOfDayToTimerFormat(
                    reminderSecondOfDay,
                    DateFormat.is24HourFormat(context)
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = textColor
            )
        }
    }
}

private fun rotateList(list: List<DayOfWeek>, firstDayOfWeek: DayOfWeek): List<DayOfWeek> {
    val index = list.indexOf(firstDayOfWeek)
    return list.subList(index, list.size) + list.subList(0, index)
}

@Preview
@Composable
fun ProductivityReminderSectionPreview() {
    ApplicationTheme {
        ProductivityReminderSection(
            firstDayOfWeek = DayOfWeek.MONDAY,
            selectedDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            reminderSecondOfDay = 10 * 60 * 60,
            onSelectDay = {},
            onReminderTimeClick = {}
        )
    }
}