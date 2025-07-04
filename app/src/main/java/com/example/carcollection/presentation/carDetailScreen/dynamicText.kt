package com.example.carcollection.presentation.carDetailScreen

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlin.math.pow

// Extensión para androidx.compose.ui.graphics.Color
fun Color.isDark(): Boolean {
    // Convierte el color a valores RGB lineales
    fun channel(c: Float): Float =
        if (c <= 0.03928f) c / 12.92f else ((c + 0.055f) / 1.055f).pow(2.4f)

    val r = channel(red)
    val g = channel(green)
    val b = channel(blue)

    // Calcula luminancia relativa
    val luminance = 0.2126f * r + 0.7152f * g + 0.0722f * b

    return luminance < 0.5f
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    maxFontSize: TextUnit = 24.sp,
    minFontSize: TextUnit = 8.sp,
    // Hacemos color opcional, si no se pasa, calculamos automáticamente para buen contraste
    backgroundColor: Color? = null,
    fontWeight: FontWeight,
    textAlign: TextAlign
) {
    var fontSize by remember { mutableStateOf(maxFontSize) }
    var readyToDraw by remember { mutableStateOf(false) }

    val textColor = remember(backgroundColor) {
        backgroundColor?.let {
            if (it.isDark()) Color.White else Color.Black
        } ?: Color.White // default white si no hay backgroundColor
    }

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val maxHeightPx = constraints.maxHeight.toFloat()
        val maxWidthPx = constraints.maxWidth.toFloat()

        Text(
            text = text.uppercase(),
            color = textColor,
            fontWeight = fontWeight,
            textAlign = textAlign,
            fontSize = fontSize,
            maxLines = Int.MAX_VALUE,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
            onTextLayout = { textLayoutResult: TextLayoutResult ->
                if (!readyToDraw) {
                    val textHeight = textLayoutResult.size.height.toFloat()
                    val textWidth = textLayoutResult.size.width.toFloat()
                    if ((textHeight > maxHeightPx || textWidth > maxWidthPx) && fontSize > minFontSize) {
                        fontSize *= 0.9f
                    } else {
                        readyToDraw = true
                    }
                }
            }
        )
    }
}
