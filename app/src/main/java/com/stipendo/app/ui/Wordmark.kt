package com.stipendo.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * The app title. The final "o" is a jade disc with a hole in the middle, a nod to the
 * round jade disc (bi) and to a coin, which is what a stipend is. It scales with the text.
 */
@Composable
fun StipendoWordmark(modifier: Modifier = Modifier, fontSize: TextUnit = 44.sp) {
    val colors = LocalNeu.current
    val text = buildAnnotatedString {
        append("Stipend")
        appendInlineContent("disc", "o")
    }
    val inline = mapOf(
        "disc" to InlineTextContent(
            Placeholder(
                width = 0.56.em,
                height = 0.56.em,
                placeholderVerticalAlign = PlaceholderVerticalAlign.AboveBaseline,
            ),
        ) { JadeDisc() },
    )
    Text(
        text = text,
        inlineContent = inline,
        modifier = modifier,
        style = TextStyle(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.ExtraBold,
            fontSize = fontSize,
            letterSpacing = (-1).sp,
            color = colors.textPrimary,
            shadow = Shadow(
                color = colors.dark.copy(alpha = 0.6f),
                offset = Offset(3f, 4f),
                blurRadius = 6f,
            ),
        ),
    )
}

@Composable
private fun JadeDisc() {
    val colors = LocalNeu.current
    Box(Modifier.fillMaxSize()) {
        // Raised jade disc
        Box(Modifier.fillMaxSize().neuSurface(cornerRadius = 50.dp, elevation = 2.5.dp, base = colors.accent))
        // Pressed-in hole through the middle
        Box(
            Modifier
                .align(Alignment.Center)
                .fillMaxSize(0.36f)
                .neuSurface(cornerRadius = 50.dp, elevation = 1.5.dp, inset = true),
        )
    }
}
