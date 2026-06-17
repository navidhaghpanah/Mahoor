package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R

// Color Palette from the Logo Image
val MahoorLogoNavy = Color(0xFF0C2C54)
val MahoorLogoGold = Color(0xFFC5A059)
val MahoorLogoIvory = Color(0xFFF9F7F2)

@Composable
fun MahoorBrandLogo(
    modifier: Modifier = Modifier,
    scale: Float = 1.0f,
    showText: Boolean = true, // Ignored because the uploaded image already includes the logo symbol & texts together!
    animate: Boolean = false,
    backgroundColor: Color = MahoorLogoIvory,
    textColor: Color = MahoorLogoNavy,
    accentColor: Color = MahoorLogoGold,
    drawColor: Color = MahoorLogoNavy
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scaleAnim by if (animate) {
        infiniteTransition.animateFloat(
            initialValue = 0.97f,
            targetValue = 1.03f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )
    } else {
        remember { mutableStateOf(1.0f) }
    }

    Box(
        modifier = modifier
            .testTag("mahoor_brand_logo_col")
            .scale(scale * scaleAnim),
        contentAlignment = Alignment.Center
    ) {
        // Display the actual high-quality, authentic logo image uploaded by the user!
        Image(
            painter = painterResource(id = R.drawable.img_mahoor_logo_1781448528082),
            contentDescription = "Mahoor Real Estate Logo",
            modifier = Modifier
                .size((140 * scale).dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Fit
        )
    }
}

