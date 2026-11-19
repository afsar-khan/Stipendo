package com.stipendo.app.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Layout pieces for the Search screen. They reuse neuSurface() from Neumorphic.kt,
 * so light and dark mode work automatically through LocalNeu.
 */

/** Equal-width option button. Selected = pressed in, accent text and a small glowing dot. */
@Composable
fun NeuCell(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = LocalNeu.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val elevation by animateDpAsState(if (pressed) 2.dp else 5.dp, tween(120), label = "cellElevation")
    Row(
        modifier = modifier
            .heightIn(min = 52.dp)
            .neuSurface(cornerRadius = 16.dp, elevation = elevation, inset = selected || pressed)
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (selected) {
            Box(Modifier.size(7.dp).background(colors.accent, CircleShape))
            Spacer(Modifier.width(7.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) colors.accent else colors.textPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

/**
 * Options laid out on a strict 3-column grid. Every column has the same width on every row.
 * An unfinished last row stretches its last button across the free columns.
 * A null option means "All".
 */
@Composable
fun <T> NeuOptionGrid(
    options: List<T?>,
    selected: T?,
    label: (T?) -> String,
    onSelect: (T?) -> Unit,
    columns: Int = 3,
) {
    val gap = 14.dp
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val cell = (maxWidth - gap * (columns - 1)) / columns
        Column(verticalArrangement = Arrangement.spacedBy(gap)) {
            options.chunked(columns).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                    row.forEachIndexed { i, option ->
                        val span = if (i == row.lastIndex) columns - row.size + 1 else 1
                        NeuCell(
                            label = label(option),
                            selected = option == selected,
                            modifier = Modifier.width(cell * span + gap * (span - 1)),
                        ) { onSelect(if (option == selected) null else option) }
                    }
                }
            }
        }
    }
}

/** Round raised icon button (46 dp) with an optional count badge. */
@Composable
fun NeuRoundButton(
    text: String,
    description: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    badge: Int = 0,
    onClick: () -> Unit,
) {
    val colors = LocalNeu.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val elevation by animateDpAsState(if (pressed) 2.dp else 5.dp, tween(120), label = "roundElevation")
    Box(
        modifier = modifier
            .size(46.dp)
            .semantics { contentDescription = description }
            .neuSurface(cornerRadius = 23.dp, elevation = elevation, inset = selected || pressed)
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium, color = if (selected) colors.accent else colors.textPrimary)
        if (badge > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-8).dp)
                    .size(24.dp)
                    .neuSurface(cornerRadius = 12.dp, elevation = 2.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(badge.toString(), style = MaterialTheme.typography.labelLarge, color = colors.accent)
            }
        }
    }
}

/** Pressed-in circle showing the number of days left. Amber when the deadline is close. */
@Composable
fun NeuGauge(value: String, urgent: Boolean, description: String) {
    val colors = LocalNeu.current
    val amber = if (colors.background == DarkNeu.background) Color(0xFFF2B36B) else Color(0xFFA5510A)
    Box(
        modifier = Modifier
            .size(64.dp)
            .semantics { contentDescription = description }
            .neuSurface(cornerRadius = 32.dp, elevation = 4.dp, inset = true),
        contentAlignment = Alignment.Center,
    ) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = if (urgent) amber else colors.accent)
    }
}

/** A thin groove used as a divider inside a card. */
@Composable
fun NeuGroove() {
    Box(Modifier.fillMaxWidth().height(3.dp).neuSurface(cornerRadius = 2.dp, elevation = 1.dp, inset = true))
}

/** Small pressed-in label, for example "Full". */
@Composable
fun NeuTag(text: String, highlight: Boolean) {
    val colors = LocalNeu.current
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = if (highlight) colors.accent else colors.textSecondary,
        modifier = Modifier
            .neuSurface(cornerRadius = 14.dp, elevation = 2.dp, inset = true)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

/** Light / dark choice. SYSTEM follows the phone. Saved in preferences by MainActivity. */
enum class ThemeMode {
    SYSTEM, LIGHT, DARK;

    fun next(): ThemeMode = entries[(ordinal + 1) % entries.size]

    val icon: String
        get() = when (this) {
            SYSTEM -> "\u25D0"
            LIGHT -> "\u2600\uFE0E"
            DARK -> "\u263E"
        }

    val label: String
        get() = when (this) {
            SYSTEM -> "System"
            LIGHT -> "Light"
            DARK -> "Dark"
        }

    companion object {
        fun fromName(name: String?): ThemeMode = entries.firstOrNull { it.name == name } ?: SYSTEM
    }
}
