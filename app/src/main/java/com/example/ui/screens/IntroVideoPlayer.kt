package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun IntroVideoPlayer(
    onVideoFinished: () -> Unit
) {
    // Media Playback States
    var currentScene by remember { mutableStateOf(1) } // Scenes 1 to 5
    var playbackProgress by remember { mutableStateOf(0f) }
    var isMuted by remember { mutableStateOf(false) }

    // Control video timing precisely (Total dur: 6500ms)
    LaunchedEffect(Unit) {
        val sceneDuration = 1300L
        for (scene in 1..5) {
            currentScene = scene
            val steps = 13
            for (step in 1..steps) {
                playbackProgress = ((scene - 1) * steps + step).toFloat() / (5 * steps).toFloat()
                delay(sceneDuration / steps)
            }
        }
        onVideoFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MahoorDarkBg)
            .testTag("intro_video_container"),
        contentAlignment = Alignment.Center
    ) {
        // 1. Cinematic Background glow / blueprint lines
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(MahoorPrimary.copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(w / 2f, h / 2f),
                    radius = h * 0.5f
                ),
                radius = h * 0.5f,
                center = Offset(w / 2f, h / 2f)
            )
        }

        // 2. Active Scene Display
        AnimatedContent(
            targetState = currentScene,
            transitionSpec = {
                fadeIn(animationSpec = tween(600)) togetherWith fadeOut(animationSpec = tween(600))
            },
            label = "VideoSceneAnimation"
        ) { scene ->
            when (scene) {
                1 -> SceneLogoIntro()
                2 -> SceneAppPhoneShow()
                3 -> SceneRadarMapScan()
                4 -> SceneBusinessWomanCTA()
                5 -> SceneLogoOutro()
            }
        }

        // 3. Cinematic Frame Overlay (Vignette & Sound bar)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Subtitles
                val subtitlesText = when (currentScene) {
                    1 -> "برنامه املاک ماهور: خانه رویایی خود را بیابید"
                    2 -> "بررسی هوشمند، مدرن و آسان ملک با موبایل"
                    3 -> "نقشه پیشرفته و همگام‌سازی ملکی در سراسر کشور"
                    4 -> "تیم پشتیبانی هوشمند ماهور مقتدر در کنار شماست"
                    else -> "ماهور، نسل متمایز و پیشرفته معاملات املاک"
                }

                Text(
                    text = subtitlesText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )

                // Player Controls Overlay
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute / sound playing animation waves
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.4f))
                            .clickable { isMuted = !isMuted }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                            contentDescription = "Mute",
                            tint = MahoorPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        if (!isMuted) {
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                                repeat(3) { index ->
                                    val infiniteTransition = rememberInfiniteTransition(label = "wave")
                                    val scaleY by infiniteTransition.animateFloat(
                                        initialValue = 0.3f,
                                        targetValue = 1f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(400 + index * 150, easing = LinearEasing),
                                            repeatMode = RepeatMode.Reverse
                                        ),
                                        label = "soundAnimation"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height((12 * scaleY).dp)
                                            .background(MahoorPrimary)
                                    )
                                }
                            }
                        }
                    }

                    // Seek Progress indicator
                    LinearProgressIndicator(
                        progress = { playbackProgress },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = MahoorPrimary,
                        trackColor = Color.White.copy(alpha = 0.15f)
                    )

                    // Skip button
                    Button(
                        onClick = onVideoFinished,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, MahoorPrimary.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("skip_intro_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("رد کردن", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Icon(imageVector = Icons.Filled.SkipNext, contentDescription = null, tint = MahoorPrimary, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
        }
    }
}

// Exit Cinematic Video Player played upon exit
@Composable
fun ExitVideoPlayer(
    onFinished: () -> Unit
) {
    var closingProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        val exitDuration = 2500L
        val steps = 25
        for (step in 1..steps) {
            closingProgress = step.toFloat() / steps.toFloat()
            delay(exitDuration / steps)
        }
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MahoorDarkBg)
            .testTag("exit_video_container"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Pulsing Mahoor Logo
            MahoorBrandLogo(
                scale = 1.3f,
                animate = true,
                backgroundColor = MahoorDarkBg
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Exit Message
            Text(
                text = "با تشکر از انتخاب و اعتماد شما",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "مشاورین املاک ماهور • همراه امین شما",
                fontSize = 11.sp,
                color = MahoorPrimary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Cinematic Exit loader sweep
            LinearProgressIndicator(
                progress = { closingProgress },
                modifier = Modifier
                    .width(160.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp)),
                color = MahoorPrimary,
                trackColor = Color.White.copy(alpha = 0.1f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "در حال خروج از برنامه...",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.4f)
            )
        }
    }
}

// === SCENE COMPOSABLES REPLICATING THE VIDEO ATTACHED ===

@Composable
fun SceneLogoIntro() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MahoorLogoIvory), // Ivory background matching logo
        contentAlignment = Alignment.Center
    ) {
        MahoorBrandLogo(
            scale = 1.4f,
            animate = false,
            backgroundColor = MahoorLogoIvory
        )
    }
}

@Composable
fun SceneAppPhoneShow() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Elegant mobile layout drawing
            Card(
                modifier = Modifier
                    .width(200.dp)
                    .height(380.dp)
                    .border(3.dp, Color.LightGray, RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MahoorSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    // Mobile notch
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(60.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                            .background(Color.Black)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Search bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MahoorDarkBg.copy(alpha = 0.4f))
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.Search, contentDescription = null, tint = MahoorPrimary, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("جستجو در املاک ماهور...", fontSize = 8.sp, color = Color.White.copy(alpha = 0.5f))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("برنامه هوشمند املاک ماهور", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MahoorPrimary)
                    Text("خانه رویایی خود را پیدا کنید", fontSize = 7.sp, color = MahoorOnBackground.copy(alpha = 0.6f))
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    // Mock Property Items
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MahoorDarkBg.copy(alpha = 0.8f))
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .background(Color.DarkGray)
                            ) {
                                // House placeholder
                                Image(
                                    painter = painterResource(id = R.drawable.img_mahoor_logo_1781448528082),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MahoorPrimary)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("ویژه", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }
                            PaddingValues(8.dp).let {
                                Column(modifier = Modifier.padding(it)) {
                                    Text("برج باغ رویال الهیه", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("سرمایه گذاری مطمئن و آسان", fontSize = 7.sp, color = Color.LightGray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SceneRadarMapScan() {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "بررسی و مکان‌یابی ماهواره‌ای املاک",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Radar Circle
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(MahoorDarkBg)
                    .border(1.5.dp, MahoorPrimary.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Spinning sweep
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = size.width / 2

                    // radar grids
                    drawCircle(color = MahoorPrimary.copy(alpha = 0.15f), radius = radius * 0.7f, style = Stroke(width = 1f))
                    drawCircle(color = MahoorPrimary.copy(alpha = 0.15f), radius = radius * 0.4f, style = Stroke(width = 1f))
                    drawCircle(color = MahoorPrimary.copy(alpha = 0.15f), radius = radius * 0.1f, style = Stroke(width = 1f))

                    // Radar sweep line
                    val angleRad = Math.toRadians(sweepAngle.toDouble())
                    val lineEnd = Offset(
                        (center.x + radius * Math.cos(angleRad)).toFloat(),
                        (center.y + radius * Math.sin(angleRad)).toFloat()
                    )
                    drawLine(
                        color = MahoorPrimary,
                        start = center,
                        end = lineEnd,
                        strokeWidth = 3f
                    )
                }

                // Radar Location Pins glowing
                repeat(4) { i ->
                    val offsetMultiplierX = if (i % 2 == 0) 1 else -1
                    val offsetMultiplierY = if (i < 2) 1 else -1
                    val infiniteProp = rememberInfiniteTransition(label = "pulse_pin")
                    val pulseRadius by infiniteProp.animateFloat(
                        initialValue = 4f,
                        targetValue = 16f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000 + i * 200, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulser"
                    )

                    Box(
                        modifier = Modifier
                            .offset(x = (offsetMultiplierX * (35 + i * 15)).dp, y = (offsetMultiplierY * (25 + i * i * 10)).dp)
                    ) {
                        Canvas(modifier = Modifier.size(24.dp)) {
                            drawCircle(color = MahoorPrimary.copy(alpha = 0.35f), radius = pulseRadius)
                            drawCircle(color = MahoorPrimary, radius = 5f)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SceneBusinessWomanCTA() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Elegant digital support agent avatar card representing the businesswoman
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .border(2.dp, MahoorPrimary, CircleShape)
                    .background(MahoorLogoNavy),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.SupportAgent,
                    contentDescription = null,
                    tint = MahoorPrimary,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "همین حالا با ماهور تماس بگیرید",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = "مشاورین دلسوز و حرفه‌ای شما در معاملات ملکی",
                fontSize = 11.sp,
                color = Color.LightGray,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Call icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MahoorPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Filled.Call, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                }
                // Chat icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2ECC71)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Filled.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun SceneLogoOutro() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MahoorDarkBg),
        contentAlignment = Alignment.Center
    ) {
        MahoorBrandLogo(
            scale = 1.4f,
            animate = true,
            backgroundColor = MahoorDarkBg
        )
    }
}

