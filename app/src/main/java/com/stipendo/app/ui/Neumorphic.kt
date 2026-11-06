package com.stipendo.app.ui

import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Core neumorphic modifier.
 *  - raised: soft highlight top-left + soft shadow bottom-right (the element "pops out")
 *  - inset:  the same two shadows drawn inside (the element is "pressed in")
 *
 * Uses Paint.setShadowLayer, which renders reliably on Android 9 (API 28) and above.
 * Leave a little padding around these elements so the shadows are not clipped.
 */
@Composable
fun Modifier.neuSurface(
    cornerRadius: Dp = 20.dp,
    elevation: Dp = 8.dp,
    inset: Boolean = false,
    base: Color? = null,
): Modifier {
    val colors = LocalNeu.current
    val fill = base ?: colors.background
    return this.drawBehind {
        drawNeu(colors, fill, cornerRadius.toPx(), elevation.toPx(), inset)
    }
}

private fun DrawScope.drawNeu(c: NeuColors, fill: Color, radius: Float, elev: Float, inset: Boolean) {
    val w = size.width
    val h = size.height
    val blur = (elev * 1.6f).coerceAtLeast(1f)
    drawIntoCanvas { canvas ->
        val native = canvas.nativeCanvas
        val rect = RectF(0f, 0f, w, h)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = fill.toArgb() }

        // Below Android 9 the soft shadows may not render, so draw a thin outline instead.
        if (android.os.Build.VERSION.SDK_INT < 28) {
            native.drawRoundRect(rect, radius, radius, paint)
            val sw = (elev * 0.35f).coerceAtLeast(1f)
            val outline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = sw
                color = c.dark.copy(alpha = if (inset) 0.9f else 0.55f).toArgb()
            }
            native.drawRoundRect(RectF(sw / 2, sw / 2, w - sw / 2, h - sw / 2), radius, radius, outline)
            return@drawIntoCanvas
        }

        if (!inset) {
            paint.setShadowLayer(blur, -elev, -elev, c.light.toArgb())
            native.drawRoundRect(rect, radius, radius, paint)
            paint.setShadowLayer(blur, elev, elev, c.dark.toArgb())
            native.drawRoundRect(rect, radius, radius, paint)
        } else {
            val clip = Path().apply { addRoundRect(rect, radius, radius, Path.Direction.CW) }
            native.save()
            native.clipPath(clip)
            native.drawRoundRect(rect, radius, radius, paint)

            // A big frame with a rounded hole: only its shadow falls inside the hole.
            val frame = Path().apply {
                fillType = Path.FillType.EVEN_ODD
                addRect(-w, -h, w * 2, h * 2, Path.Direction.CW)
                addRoundRect(rect, radius, radius, Path.Direction.CW)
            }
            paint.setShadowLayer(blur, elev, elev, c.dark.toArgb())
            native.drawPath(frame, paint)
            paint.setShadowLayer(blur, -elev, -elev, c.light.toArgb())
            native.drawPath(frame, paint)
            native.restore()
        }
    }
}

/** A raised card. Pass onClick to make it tappable; it presses in while held. */
@Composable
fun NeuCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val elevation by animateDpAsState(
        targetValue = if (pressed && onClick != null) 3.dp else 8.dp,
        animationSpec = tween(120),
        label = "cardElevation",
    )
    val base = modifier
        .fillMaxWidth()
        .neuSurface(cornerRadius = 24.dp, elevation = elevation, inset = pressed && onClick != null)
    val tappable = if (onClick != null) {
        base.clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
    } else base
    Column(modifier = tappable.padding(20.dp), content = content)
}

/** Filter chip. Selected = pressed in, with the accent colour on the text. */
@Composable
fun NeuChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalNeu.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val elevation by animateDpAsState(
        targetValue = if (pressed) 2.dp else 5.dp,
        animationSpec = tween(120),
        label = "chipElevation",
    )
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = if (selected) colors.accent else colors.textSecondary,
        modifier = Modifier
            .neuSurface(cornerRadius = 16.dp, elevation = elevation, inset = selected || pressed)
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 11.dp),
    )
}

/** Main action button. */
@Composable
fun NeuButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
    enabled: Boolean = true,
) {
    val colors = LocalNeu.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val elevation by animateDpAsState(
        targetValue = if (pressed && enabled) 3.dp else 8.dp,
        animationSpec = tween(120),
        label = "buttonElevation",
    )
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = when {
            !enabled -> colors.textSecondary
            emphasized -> colors.accent
            else -> colors.textPrimary
        },
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .neuSurface(cornerRadius = 20.dp, elevation = elevation, inset = pressed || !enabled)
            .clickable(interactionSource = interaction, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 12.dp),
    )
}
