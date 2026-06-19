package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

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

    Column(
        modifier = modifier
            .testTag("mahoor_brand_logo_col")
            .scale(scale * scaleAnim),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Display the actual high-quality, authentic logo emblem image
        Image(
            painter = painterResource(id = R.drawable.img_mahoor_emblem_1781807051115),
            contentDescription = "Mahoor Real Estate Emblem",
            modifier = Modifier
                .size((140 * scale).dp)
                .clip(RoundedCornerShape(70.dp)),
            contentScale = ContentScale.Crop
        )
        
        if (showText) {
            Spacer(modifier = Modifier.height((12 * scale).dp))
            
            Text(
                text = "املاک ماهور",
                fontSize = (26 * scale).sp,
                fontWeight = FontWeight.Black,
                color = textColor,
                modifier = Modifier.testTag("mahoor_brand_text_fa")
            )
            
            Spacer(modifier = Modifier.height((4 * scale).dp))
            
            Text(
                text = "MAHOOR REAL ESTATE",
                fontSize = (12 * scale).sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                letterSpacing = (2 * scale).sp,
                modifier = Modifier.testTag("mahoor_brand_text_en")
            )
            
            Spacer(modifier = Modifier.height((2 * scale).dp))
            
            Text(
                text = "مشاورین املاک و سرمایه گذاری",
                fontSize = (9 * scale).sp,
                fontWeight = FontWeight.Normal,
                color = textColor.copy(alpha = 0.6f),
                modifier = Modifier.testTag("mahoor_brand_text_slogan")
            )
        }
    }
}

