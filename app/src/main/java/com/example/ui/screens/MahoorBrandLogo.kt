package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Color Palette from the Logo Image
val MahoorLogoNavy = Color(0xFF0C2C54)
val MahoorLogoGold = Color(0xFFC5A059)
val MahoorLogoIvory = Color(0xFFF9F7F2)

@Composable
fun MahoorBrandLogo(
    modifier: Modifier = Modifier,
    scale: Float = 1.0f,
    showText: Boolean = true,
    animate: Boolean = false,
    backgroundColor: Color = MahoorLogoIvory,
    textColor: Color = MahoorLogoNavy,
    accentColor: Color = MahoorLogoGold,
    drawColor: Color = MahoorLogoNavy
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by if (animate) {
        infiniteTransition.animateFloat(
            initialValue = 0.7f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha"
        )
    } else {
        remember { mutableStateOf(1.0f) }
    }

    val rotationAnim by if (animate) {
        infiniteTransition.animateFloat(
            initialValue = -3f,
            targetValue = 3f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "rotation"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    Column(
        modifier = modifier
            .testTag("mahoor_brand_logo_col"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 1. Draw the vector emblem of Mahoor Real Estate
        Box(
            modifier = Modifier
                .size((140 * scale).dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Drawing Background subtle Buildings in Navy Outline
                // Leftmost lower building
                drawRect(
                    color = drawColor,
                    topLeft = Offset(w * 0.22f, h * 0.45f),
                    size = Size(w * 0.12f, h * 0.45f),
                    style = Stroke(width = w * 0.03f)
                )

                // Left medium building
                drawRect(
                    color = drawColor,
                    topLeft = Offset(w * 0.32f, h * 0.30f),
                    size = Size(w * 0.12f, h * 0.60f),
                    style = Stroke(width = w * 0.03f)
                )

                // Center main tall building
                drawRect(
                    color = drawColor,
                    topLeft = Offset(w * 0.46f, h * 0.18f),
                    size = Size(w * 0.14f, h * 0.72f),
                    style = Stroke(width = w * 0.03f)
                )
                // Windows in the central building
                drawRect(color = accentColor, topLeft = Offset(w * 0.51f, h * 0.30f), size = Size(w * 0.04f, h * 0.08f))
                drawRect(color = accentColor, topLeft = Offset(w * 0.51f, h * 0.45f), size = Size(w * 0.04f, h * 0.08f))
                drawRect(color = accentColor, topLeft = Offset(w * 0.51f, h * 0.60f), size = Size(w * 0.04f, h * 0.08f))

                // Right medium building
                drawRect(
                    color = drawColor,
                    topLeft = Offset(w * 0.58f, h * 0.35f),
                    size = Size(w * 0.12f, h * 0.55f),
                    style = Stroke(width = w * 0.03f)
                )

                // Rightmost low building in gold accent
                drawRect(
                    color = accentColor,
                    topLeft = Offset(w * 0.68f, h * 0.48f),
                    size = Size(w * 0.10f, h * 0.42f),
                    style = Stroke(width = w * 0.03f)
                )

                // Base navy platform bar
                drawLine(
                    color = drawColor,
                    start = Offset(w * 0.15f, h * 0.90f),
                    end = Offset(w * 0.85f, h * 0.90f),
                    strokeWidth = w * 0.04f
                )

                // 2. Overlap the majestic shining gold Crescent Moon in the foreground (Mahr symbol)
                // Left overlapping moon
                val moonPath = Path().apply {
                    val cx = w * 0.41f
                    val cy = h * 0.46f
                    val r = h * 0.22f
                    // Outer arc
                    addOval(androidx.compose.ui.geometry.Rect(cx - r, cy - r, cx + r, cy + r))
                }
                
                val innerSubtractPath = Path().apply {
                    val cx = w * 0.47f
                    val cy = h * 0.41f
                    val r = h * 0.21f
                    // Inner subtracting oval
                    addOval(androidx.compose.ui.geometry.Rect(cx - r, cy - r, cx + r, cy + r))
                }

                // Draw Golden Moon
                drawPath(
                    path = moonPath,
                    color = accentColor
                )
                // Subtract the inner side to create the elegant crescent look
                drawPath(
                    path = innerSubtractPath,
                    color = backgroundColor.copy(alpha = alphaAnim)
                )

                // Little golden crescent moon hanging at the peak center
                val tinyMoonPath = Path().apply {
                    val cx = w * 0.53f
                    val cy = h * 0.18f
                    val r = h * 0.07f
                    addOval(androidx.compose.ui.geometry.Rect(cx - r, cy - r, cx + r, cy + r))
                }
                val tinyMoonSubtract = Path().apply {
                    val cx = w * 0.55f
                    val cy = h * 0.16f
                    val r = h * 0.07f
                    addOval(androidx.compose.ui.geometry.Rect(cx - r, cy - r, cx + r, cy + r))
                }
                drawPath(path = tinyMoonPath, color = accentColor)
                drawPath(path = tinyMoonSubtract, color = backgroundColor.copy(alpha = alphaAnim))
            }
        }

        if (showText) {
            Spacer(modifier = Modifier.height((8 * scale).dp))
            
            // "املاک ماهور" Kalligraph
            Text(
                text = "املاک ماهور",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.ExtraBold,
                fontSize = (28 * scale).sp,
                color = textColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height((2 * scale).dp))

            // Subtitle English: "MAHOOR REAL ESTATE"
            Text(
                text = "MAHOOR REAL ESTATE",
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Black,
                fontSize = (13 * scale).sp,
                color = textColor,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height((4 * scale).dp))

            // Footer Subtext
            Text(
                text = "مشاورین املاک و سرمایه گذاری",
                fontSize = (11 * scale).sp,
                fontWeight = FontWeight.Medium,
                color = accentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}
