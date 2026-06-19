package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AgentProfile
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    currentProfile: AgentProfile?,
    onLoginSuccess: (AgentProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(1) } // 1: Phone, 2: OTP
    var countdown by remember { mutableStateOf(120) }
    var isSendingOtp by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Clean timer when in OTP step
    LaunchedEffect(step) {
        if (step == 2) {
            countdown = 120
            while (countdown > 0) {
                delay(1000)
                countdown--
            }
        }
    }


    fun toPersianDigits(text: String): String {
        return text.replace("0", "۰")
            .replace("1", "۱")
            .replace("2", "۲")
            .replace("3", "۳")
            .replace("4", "۴")
            .replace("5", "۵")
            .replace("6", "۶")
            .replace("7", "۷")
            .replace("8", "۸")
            .replace("9", "۹")
    }

    fun toEnglishDigits(text: String): String {
        return text.replace("۰", "0")
            .replace("۱", "1")
            .replace("۲", "2")
            .replace("۳", "3")
            .replace("۴", "4")
            .replace("۵", "5")
            .replace("۶", "6")
            .replace("۷", "7")
            .replace("۸", "8")
            .replace("۹", "9")
    }

    fun handleSendOtp() {
        val cleanPhone = toEnglishDigits(phoneNumber.trim())
        if (cleanPhone.length < 10) {
            errorMessage = "لطفاً شماره موبایل معتبر ۱۱ رقمی وارد کنید"
            return
        }
        errorMessage = null
        isSendingOtp = true
        scope.launch {
            delay(1200) // Simulated network delay
            isSendingOtp = false
            step = 2
        }
    }

    fun handleVerifyOtp() {
        if (otpCode.length < 4) {
            errorMessage = "کد تایید باید ۴ رقمی باشد"
            return
        }
        errorMessage = null

        val cleanPhone = toEnglishDigits(phoneNumber.trim())
        val profile = when {
            // Manager: Mohammad Mehdi Azad
            cleanPhone.endsWith("9113276647") || cleanPhone.contains("3276647") -> {
                AgentProfile(
                    id = 1,
                    fullName = "محمد مهدی آزاد (مدیر ارشد)",
                    agencyName = "مجموعه تخصصی املاک ماهور",
                    licenseNumber = "م-۱۵۹۸",
                    phoneNumber = toPersianDigits("09113276647"),
                    email = "info@mahoorrlste.ir",
                    agencyAddress = "محمودآباد، خیابان امام، بعد از نسیم ۶۹/۱ — روبروی پارکینگ قزوینی‌پور",
                    currentPlan = "سرویس ویژه پلاتینیوم (کنترل و ارزیابی کل)",
                    planExpiryDate = "۱۴۰۷/۱۲/۲۹",
                    adsLimitRemaining = 120,
                    totalAdsAllowed = 150,
                    directSyncLimitRemaining = 950,
                    totalDirectSyncLimit = 1000
                )
            }
            // Ms. Heidari
            cleanPhone.endsWith("120996426") || cleanPhone.contains("09120996426") || cleanPhone.contains("996426") -> {
                AgentProfile(
                    id = 1,
                    fullName = "خانم حیدری",
                    agencyName = "مجموعه تخصصی املاک ماهور",
                    licenseNumber = "م-۵۴۲۰",
                    phoneNumber = toPersianDigits("09120996426"),
                    email = "heidari@mahoorrlste.ir",
                    agencyAddress = "محمودآباد، خیابان امام، بعد از نسیم ۶۹/۱ — روبروی پارکینگ قزوینی‌پور",
                    currentPlan = "اشتراک نقره‌ای همگام‌ساز (مشاور)",
                    planExpiryDate = "۱۴۰۶/۱۲/۲۹",
                    adsLimitRemaining = 28,
                    totalAdsAllowed = 50,
                    directSyncLimitRemaining = 120,
                    totalDirectSyncLimit = 200
                )
            }
            // Mr. Rayee
            cleanPhone.endsWith("120997453") || cleanPhone.contains("09120997453") || cleanPhone.contains("997453") -> {
                AgentProfile(
                    id = 1,
                    fullName = "آقای راعی",
                    agencyName = "مجموعه تخصصی املاک ماهور",
                    licenseNumber = "م-۸۷۳۴",
                    phoneNumber = toPersianDigits("09120997453"),
                    email = "rayee@mahoorrlste.ir",
                    agencyAddress = "محمودآباد، خیابان امام، بعد از نسیم ۶۹/۱ — روبروی پارکینگ قزوینی‌پور",
                    currentPlan = "اشتراک طلایی همگام‌ساز (مشاور ارشد)",
                    planExpiryDate = "۱۴۰۶/۱۲/۲۹",
                    adsLimitRemaining = 35,
                    totalAdsAllowed = 50,
                    directSyncLimitRemaining = 345,
                    totalDirectSyncLimit = 500
                )
            }
            // Default Custom user representation
            else -> {
                AgentProfile(
                    id = 1,
                    fullName = "مشاور املاک ماهور",
                    agencyName = "مجموعه تخصصی املاک ماهور",
                    licenseNumber = "م-۹۹۸۸",
                    phoneNumber = toPersianDigits(cleanPhone),
                    email = "agent@mahoorrlste.ir",
                    agencyAddress = "محمودآباد، استان مازندران",
                    currentPlan = "اشتراک آزمایشی همگام‌سازی",
                    planExpiryDate = "۱۴۰۵/۰۹/۳۰",
                    adsLimitRemaining = 10,
                    totalAdsAllowed = 15,
                    directSyncLimitRemaining = 30,
                    totalDirectSyncLimit = 50
                )
            }
        }

        onLoginSuccess(profile)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF030D1E),
                        Color(0xFF0C2C54),
                        Color(0xFF030D1E)
                    )
                )
            )
            .testTag("login_screen_root"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .safeContentPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Corporate Logo branding at top
            MahoorBrandLogo(
                scale = 1.1f,
                showText = true,
                animate = false,
                backgroundColor = Color.Transparent,
                textColor = Color(0xFFF9F7F2),
                accentColor = Color(0xFFC5A059),
                drawColor = Color(0xFFC5A059)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Box Card container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1F3D).copy(alpha = 0.85f)),
                border = BorderStroke(1.dp, Color(0xFFC5A059).copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (step == 1) {
                        // Enter Phone number Step
                        Text(
                            text = "ورود به سیستم یکپارچه ماهور",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF9F7F2)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "برای دریافت فایل‌ها و ورود به پنل، با شماره همراه مشاور وارد شوید",
                            fontSize = 11.sp,
                            color = Color(0xFFF9F7F2).copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            placeholder = { Text("مثال: ۰۹۱۲۰۹۹۶۴۲۶", fontSize = 13.sp, color = Color.White.copy(alpha = 0.4f)) },
                            leadingIcon = { Icon(imageVector = Icons.Filled.PhoneAndroid, contentDescription = null, tint = Color(0xFFC5A059)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFC5A059),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedLabelColor = Color(0xFFC5A059),
                                cursorColor = Color(0xFFC5A059),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("phone_input_field")
                        )

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage ?: "",
                                fontSize = 11.sp,
                                color = Color(0xFFE74C3C),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { handleSendOtp() },
                            enabled = phoneNumber.isNotEmpty() && !isSendingOtp,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFC5A059),
                                contentColor = Color(0xFF0C2C54)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("send_otp_button")
                        ) {
                            if (isSendingOtp) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color(0xFF0C2C54),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(text = "ارسال کد یکبار مصرف (SMS)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }


                    } else {
                        // OTP Code Verification Step
                        Text(
                            text = "تایید کد پیامک شده",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF9F7F2)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "کد ۴ رقمی ارسال شده به شماره ${toPersianDigits(phoneNumber)} را وارد کنید",
                            fontSize = 11.sp,
                            color = Color(0xFFF9F7F2).copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { if (it.length <= 4) otpCode = it },
                            placeholder = { Text("کد مانند: ۱۲۳۴", fontSize = 13.sp, color = Color.White.copy(alpha = 0.4f)) },
                            leadingIcon = { Icon(imageVector = Icons.Filled.Lock, contentDescription = null, tint = Color(0xFFC5A059)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFC5A059),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedLabelColor = Color(0xFFC5A059),
                                cursorColor = Color(0xFFC5A059),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("otp_input_field")
                        )

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage ?: "",
                                fontSize = 11.sp,
                                color = Color(0xFFE74C3C),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Resend Countdown
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (countdown > 0) "ارسال مجدد کد تا: ${countdown / 60}:${String.format("%02d", countdown % 60)}".toPersianDigits() else "کدی دریافت نکردید؟",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                            if (countdown == 0) {
                                Text(
                                    text = "ارسال مجدد پیامک",
                                    fontSize = 10.sp,
                                    color = Color(0xFFC5A059),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable {
                                        countdown = 120
                                        otpCode = ""
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    step = 1
                                    errorMessage = null
                                    otpCode = ""
                                },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("بازگشت", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { handleVerifyOtp() },
                                enabled = otpCode.length >= 4,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFC5A059),
                                    contentColor = Color(0xFF0C2C54)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(2.5f)
                                    .height(48.dp)
                                    .testTag("verify_otp_button")
                            ) {
                                Text(text = "بررسی کد و ورود", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
