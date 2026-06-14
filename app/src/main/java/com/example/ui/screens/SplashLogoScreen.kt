package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashLogoScreen(
    onTimeout: () -> Unit
) {
    var loadingProgress by remember { mutableStateOf(0f) }
    var statusText by remember { mutableStateOf("در حال بارگذاری داده‌های پایه ماهور...") }

    LaunchedEffect(Unit) {
        // Smoothly animate the loader for 3 seconds
        val duration = 3000
        val steps = 30
        for (i in 1..steps) {
            delay((duration / steps).toLong())
            loadingProgress = i.toFloat() / steps.toFloat()
            if (i == 10) {
                statusText = "برقراری کانال رمزگذاری شده با دیوار و شیپور..."
            } else if (i == 20) {
                statusText = "بارگذاری نهایی رابط کاربری پیشرفته ملکی..."
            }
        }
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MahoorDarkBg) // Majestic corporate dark navy background
            .testTag("splash_logo_screen_root"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Premium Corporate Logo adapted for navy background
            MahoorBrandLogo(
                scale = 1.3f,
                showText = true,
                animate = true,
                backgroundColor = MahoorDarkBg,
                textColor = Color.White,
                drawColor = MahoorPrimary,
                accentColor = MahoorPrimary
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = statusText,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("splash_status_text")
                )

                Spacer(modifier = Modifier.height(16.dp))

                LinearProgressIndicator(
                    progress = { loadingProgress },
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .testTag("splash_progress_bar"),
                    color = MahoorPrimary, // Gold loading accent
                    trackColor = MahoorPrimary.copy(alpha = 0.15f)
                )
            }
        }
    }
}
