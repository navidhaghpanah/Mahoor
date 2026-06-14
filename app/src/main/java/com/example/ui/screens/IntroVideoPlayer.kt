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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun IntroVideoPlayer(
    onVideoFinished: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // States
    var isBuffering by remember { mutableStateOf(true) }
    var videoErrorOccurred by remember { mutableStateOf(false) }
    var isVideoPlaying by remember { mutableStateOf(false) }
    
    // Animation Progress Timeline from 0.0f to 1.0f
    var animationProgress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animationProgress,
        animationSpec = tween(durationMillis = 10000, easing = LinearEasing),
        label = "animProgress"
    )

    // Fallback Timer to guarantee transition even if video hangs or network stops
    LaunchedEffect(Unit) {
        animationProgress = 1.0f
        // 10-second safe guard boundary
        delay(10200)
        onVideoFinished()
    }

    // Try to load a real vertical sample clip
    val videoUri = remember {
        val resId = context.resources.getIdentifier("mahoor_intro", "raw", context.packageName)
        if (resId != 0) {
            Uri.parse("android.resource://${context.packageName}/$resId")
        } else {
            Uri.parse("https://assets.mixkit.co/videos/preview/mixkit-modern-apartment-interior-living-room-vertical-shot-41710-large.mp4")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C2C54)) // Majestic navy background for cinematic intro
            .testTag("intro_video_container"),
        contentAlignment = Alignment.Center
    ) {
        
        // 1. Hardware VideoView attempt
        if (!videoErrorOccurred) {
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        // Crucial for native layout in Jetpack Compose to avoid 0x0 size
                        layoutParams = android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setVideoURI(videoUri)
                        setOnPreparedListener { mediaPlayer ->
                            mediaPlayer.isLooping = false
                            isBuffering = false
                            isVideoPlaying = true
                            mediaPlayer.setVolume(1.0f, 1.0f)
                            start()
                        }
                        setOnCompletionListener {
                            onVideoFinished()
                        }
                        setOnErrorListener { _, _, _ ->
                            videoErrorOccurred = true
                            isBuffering = false
                            isVideoPlaying = false
                            true
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2. High-Fidelity Compose Cinema Overlay of Mahoor Real Estate
        // This only covers the screen with raw graphics when video fails or is buffering
        Box(modifier = Modifier.fillMaxSize()) {
            
            if (videoErrorOccurred) {
                // Background Animation (Blueprint Panning and Real-time listings radar) shown ONLY on error fallback
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    
                    // Draw luxury building blueprint outlines on background with smooth camera movement based on progress
                    val offsetPan = (animatedProgress * 100f) % 360f
                    
                    for (i in -2..5) {
                        val lineX = i * (w * 0.25f) - (offsetPan * 0.5f)
                        drawLine(
                            color = Color(0xFFC5A059).copy(alpha = 0.08f),
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
                                Color(0xFFC5A059).copy(alpha = 0.2f),
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
                                color = Color(0xFFC5A059).copy(alpha = 0.4f),
                                radius = (40f * (1f - (showIndexProgress - index) % 1.0f)),
                                center = coord,
                                style = Stroke(width = 3f)
                            )
                            // Solid inner
                            drawCircle(
                                color = Color(0xFFC5A059),
                                radius = 8f,
                                center = coord
                            )
                        }
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
                            tint = Color(0xFFC5A059),
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
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC5A059).copy(alpha = 0.5f)),
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
                                tint = Color.C5A059(),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Center Cinematic Logo or buffering indicator
                if (videoErrorOccurred) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val scaleFactor = 1.0f + (0.15f * animatedProgress)
                        
                        Box(
                            modifier = Modifier.size(140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Drawing building lines and crescent moon in high visual weight in real-time
                            MahoorBrandLogo(
                                scale = scaleFactor,
                                showText = false,
                                animate = true
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "MAHOOR REAL ESTATE",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFC5A059),
                            letterSpacing = 2.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else if (isBuffering) {
                    CircularProgressIndicator(
                        color = Color(0xFFC5A059),
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(48.dp)
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
                        animatedProgress < 0.25f -> "نسل متمایز و پیشرفته معاملات املاک..."
                        animatedProgress < 0.50f -> "همگام‌سازی مستقیم با بزرگترین درگاه‌های دیوار و شیپور..."
                        animatedProgress < 0.75f -> "سیستم مکانیزه مانیتورینگ تغییرات قیمت منطقه..."
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
