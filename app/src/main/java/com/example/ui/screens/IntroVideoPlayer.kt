package com.example.ui.screens

import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun IntroVideoPlayer(
    onVideoFinished: () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    // States
    var animationProgress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animationProgress,
        animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
        label = "animProgress"
    )

    // Cinematic Timer to guarantee smooth 2.2 second transition
    LaunchedEffect(Unit) {
        animationProgress = 1.0f
        delay(2200)
        onVideoFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MahoorDarkBg) // Elegant royal navy background matching logo
            .testTag("intro_video_container"),
        contentAlignment = Alignment.Center
    ) {
        
        // High-Fidelity Compose Cinema Backdrop
        // This covers the background with luxury blueprint graphics and dynamic lighting
        Box(modifier = Modifier.fillMaxSize()) {
            
            // Background Animation (Blueprint Panning and Real-time listings radar)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                
                // Draw luxury building blueprint outlines on background with smooth camera movement
                val offsetPan = (animatedProgress * 160f)
                
                for (i in -2..5) {
                    val lineX = i * (w * 0.25f) - (offsetPan * 0.5f)
                    drawLine(
                        color = MahoorPrimary.copy(alpha = 0.09f),
                        start = Offset(lineX, 0f),
                        end = Offset(lineX + (w * 0.1f), h),
                        strokeWidth = 2f
                    )
                }

                // Dynamic sonar sweep radar highlighting listing coordinates synced in real-time
                val sweepY = (animatedProgress * h * 1.5f) % h
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MahoorPrimary.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    ),
                    topLeft = Offset(0f, sweepY - (h * 0.1f)),
                    size = Size(w, h * 0.2f)
                )

                // Highlighting target dots representing newly compiled real estate listings
                val targetCoordinates = listOf(
                    Offset(w * 0.3f, h * 0.25f),
                    Offset(w * 0.7f, h * 0.4f),
                    Offset(w * 0.4f, h * 0.55f),
                    Offset(w * 0.6f, h * 0.7f)
                )

                targetCoordinates.forEachIndexed { index, coord ->
                    val showIndexProgress = (animatedProgress * 4.0f)
                    if (showIndexProgress > index) {
                        // Pulse circle
                        drawCircle(
                            color = MahoorPrimary.copy(alpha = 0.3f),
                            radius = (40f * (1f - (showIndexProgress - index) % 1.0f)),
                            center = coord,
                            style = Stroke(width = 3f)
                        )
                        // Solid inner
                        drawCircle(
                            color = MahoorPrimary,
                            radius = 8f,
                            center = coord
                        )
                    }
                }
            }


            // Cinematic text & numbers layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.4f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header: High-Tech Status indicators
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Active monitor signal
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Sensors,
                            contentDescription = null,
                            tint = MahoorPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "تیزر معرفی زنده".toFarsiDigits(),
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Skip Button
                    Button(
                        onClick = onVideoFinished,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MahoorPrimary.copy(alpha = 0.5f)),
                        modifier = Modifier.testTag("skip_intro_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "رد کردن ویدیو".toFarsiDigits(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = "Skip",
                                tint = MahoorPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Center Cinematic Logo and Title
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val scaleFactor = 1.0f + (0.15f * animatedProgress)
                    
                    Box(
                        modifier = Modifier.size(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Drawing building lines and crescent moon in high visual weight
                        MahoorBrandLogo(
                            scale = scaleFactor,
                            showText = false,
                            animate = true,
                            backgroundColor = MahoorDarkBg,
                            drawColor = MahoorPrimary,
                            accentColor = MahoorPrimary,
                            textColor = MahoorPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "MAHOOR REAL ESTATE",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = MahoorPrimary,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // Bottom Cinematic Subtitles syncing
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val subtitleText = when {
                        animatedProgress < 0.33f -> "نسل متمایز و پیشرفته معاملات املاک..."
                        animatedProgress < 0.66f -> "همگام‌سازی مستقیم با هوش مصنوعی ماهور..."
                        else -> "خوش آمدید به پورتال هوشمند املاک ماهور"
                    }

                    AnimatedContent(
                        targetState = subtitleText,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
                        },
                        label = "subtitle"
                    ) { text ->
                        Text(
                            text = text.toFarsiDigits(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "در حال پردازش فریم‌های معرفی... ${(animatedProgress * 100).toInt()}%".toFarsiDigits(),
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}


// Extension to get Gold color quickly
private fun Color.Companion.C5A059(): Color = Color(0xFFC5A059)

// Convert standard numbers to beautiful Persian layout digits
private fun String.toFarsiDigits(): String {
    var result = this
    val farsi = arrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    for (i in 0..9) {
        result = result.replace(i.toString()[0], farsi[i])
    }
    return result
}
