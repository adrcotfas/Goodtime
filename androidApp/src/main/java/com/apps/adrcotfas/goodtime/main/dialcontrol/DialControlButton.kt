/**
 *     Goodtime Productivity
 *     Copyright (C) 2025 Adrian Cotfas
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.apps.adrcotfas.goodtime.main.dialcontrol

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max

@Composable
fun DialControlButton(disabled: Boolean, selected: Boolean, region: DialRegion) {
    region.icon?.let {
        val animatedSize = animateFloatAsState(
            targetValue = if (selected) 48f else 8f,
            label = "size",
            animationSpec = spring(),
        )

        Box(
            modifier = Modifier.wrapContentSize(),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (disabled) {
                            Color.Transparent
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        },
                    )
                    .size(animatedSize.value.dp),

            ) {
                Icon(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(max(animatedSize.value.dp - 24.dp, 0.dp)),
                    imageVector = region.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            region.label?.let {
                Box(
                    Modifier
                        .wrapContentSize()
                        .graphicsLayer {
                            alpha = if (selected) 1f else 0f
                            translationY =
                                (42 * if (region == DialRegion.TOP) 1 else -1).dp.toPx()
                        }
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(MaterialTheme.colorScheme.onBackground)
                        .padding(4.dp),
                ) {
                    Text(
                        text = region.label,
                        modifier = Modifier,
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.background),
                    )
                }
            }
        }
    }
}
