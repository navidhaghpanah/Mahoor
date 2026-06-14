package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AgentProfile
import com.example.data.model.RealEstateAd
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ProfileAndSubscriptionTab(
    agentProfile: AgentProfile?,
    onUpdateProfile: (AgentProfile) -> Unit
) {
    if (agentProfile == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MahoorPrimary)
        }
        return
    }

    var isEditingProfile by remember { mutableStateOf(false) }
    var showUpgradeDialog by remember { mutableStateOf(false) }

    // Temporary inputs for profiles edit mode
    var tempFullName by remember(agentProfile) { mutableStateOf(agentProfile.fullName) }
    var tempAgencyName by remember(agentProfile) { mutableStateOf(agentProfile.agencyName) }
    var tempLicenseNumber by remember(agentProfile) { mutableStateOf(agentProfile.licenseNumber) }
    var tempPhoneNumber by remember(agentProfile) { mutableStateOf(agentProfile.phoneNumber) }
    var tempEmail by remember(agentProfile) { mutableStateOf(agentProfile.email) }
    var tempAgencyAddress by remember(agentProfile) { mutableStateOf(agentProfile.agencyAddress) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MahoorSurface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Circular Avatar
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MahoorPrimary.copy(alpha = 0.15f))
                        .border(1.5.dp, MahoorPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (agentProfile.fullName.isNotEmpty()) agentProfile.fullName.take(2) else "م",
                        color = MahoorPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = agentProfile.fullName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MahoorOnBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = agentProfile.agencyName,
                        fontSize = 12.sp,
                        color = MahoorOnBackground.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Card 1: Advisor Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MahoorSurface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "پروفایل کاری مشاور",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MahoorPrimary
                    )

                    if (!isEditingProfile) {
                        IconButton(
                            onClick = { isEditingProfile = true },
                            modifier = Modifier.testTag("edit_profile_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "ویرایش مشخصات",
                                tint = MahoorPrimary
                            )
                        }
                    }
                }

                HorizontalDivider(color = MahoorSurfaceVariant)

                if (isEditingProfile) {
                    // Editable fields
                    OutlinedTextField(
                        value = tempFullName,
                        onValueChange = { tempFullName = it },
                        label = { Text("نام و نام خانوادگی") },
                        modifier = Modifier.fillMaxWidth().testTag("profile_input_name"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MahoorPrimary,
                            unfocusedBorderColor = MahoorOnBackground.copy(alpha = 0.2f),
                            focusedLabelColor = MahoorPrimary
                        )
                    )

                    OutlinedTextField(
                        value = tempAgencyName,
                        onValueChange = { tempAgencyName = it },
                        label = { Text("نام آژانس املاک") },
                        modifier = Modifier.fillMaxWidth().testTag("profile_input_agencyName"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MahoorPrimary,
                            unfocusedBorderColor = MahoorOnBackground.copy(alpha = 0.2f),
                            focusedLabelColor = MahoorPrimary
                        )
                    )

                    OutlinedTextField(
                        value = tempLicenseNumber,
                        onValueChange = { tempLicenseNumber = it },
                        label = { Text("شماره پروانه کسب") },
                        modifier = Modifier.fillMaxWidth().testTag("profile_input_license"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MahoorPrimary,
                            unfocusedBorderColor = MahoorOnBackground.copy(alpha = 0.2f),
                            focusedLabelColor = MahoorPrimary
                        )
                    )

                    OutlinedTextField(
                        value = tempPhoneNumber,
                        onValueChange = { tempPhoneNumber = it },
                        label = { Text("شماره تلفن همراه") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth().testTag("profile_input_phone"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MahoorPrimary,
                            unfocusedBorderColor = MahoorOnBackground.copy(alpha = 0.2f),
                            focusedLabelColor = MahoorPrimary
                        )
                    )

                    OutlinedTextField(
                        value = tempEmail,
                        onValueChange = { tempEmail = it },
                        label = { Text("آدرس ایمیل") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth().testTag("profile_input_email"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MahoorPrimary,
                            unfocusedBorderColor = MahoorOnBackground.copy(alpha = 0.2f),
                            focusedLabelColor = MahoorPrimary
                        )
                    )

                    OutlinedTextField(
                        value = tempAgencyAddress,
                        onValueChange = { tempAgencyAddress = it },
                        label = { Text("آدرس دفتر آژانس") },
                        modifier = Modifier.fillMaxWidth().testTag("profile_input_address"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MahoorPrimary,
                            unfocusedBorderColor = MahoorOnBackground.copy(alpha = 0.2f),
                            focusedLabelColor = MahoorPrimary
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onUpdateProfile(
                                    agentProfile.copy(
                                        fullName = tempFullName,
                                        agencyName = tempAgencyName,
                                        licenseNumber = tempLicenseNumber,
                                        phoneNumber = tempPhoneNumber,
                                        email = tempEmail,
                                        agencyAddress = tempAgencyAddress
                                    )
                                )
                                isEditingProfile = false
                            },
                            modifier = Modifier.weight(1f).testTag("profile_save_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MahoorPrimary)
                        ) {
                            Text("ذخیره تغییرات", color = MahoorOnPrimary, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                // Cancel and revert
                                tempFullName = agentProfile.fullName
                                tempAgencyName = agentProfile.agencyName
                                tempLicenseNumber = agentProfile.licenseNumber
                                tempPhoneNumber = agentProfile.phoneNumber
                                tempEmail = agentProfile.email
                                tempAgencyAddress = agentProfile.agencyAddress
                                isEditingProfile = false
                            },
                            modifier = Modifier.weight(1f).testTag("profile_cancel_btn")
                        ) {
                            Text("انصراف", color = MahoorOnBackground)
                        }
                    }
                } else {
                    // Read-only info fields
                    InfoItemRow(icon = Icons.Filled.Person, label = "نام و نام خانوادگی", value = agentProfile.fullName)
                    InfoItemRow(icon = Icons.Filled.Business, label = "آژانس املاک", value = agentProfile.agencyName)
                    InfoItemRow(icon = Icons.Filled.Verified, label = "شماره پروانه کسب", value = agentProfile.licenseNumber)
                    InfoItemRow(icon = Icons.Filled.Phone, label = "تلفن همراه مشاور", value = agentProfile.phoneNumber)
                    InfoItemRow(icon = Icons.Filled.Email, label = "پست الکترونیکی", value = agentProfile.email)
                    InfoItemRow(icon = Icons.Filled.LocationOn, label = "موقعیت دفتر", value = agentProfile.agencyAddress)
                }
            }
        }

        // Card 2: Subscription & Channel Quotas
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MahoorSurface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "جزئیات اشتراک شماور و سهمیه ابزارها",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MahoorPrimary
                )

                HorizontalDivider(color = MahoorSurfaceVariant)

                // Current Plan Badge Area
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "طرح اشتراکی فعال:",
                            fontSize = 11.sp,
                            color = MahoorOnBackground.copy(alpha = 0.5f)
                        )
                        Text(
                            text = agentProfile.currentPlan,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = MahoorPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MahoorPrimary.copy(alpha = 0.15f))
                            .border(1.dp, MahoorPrimary, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "تا ${agentProfile.planExpiryDate}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MahoorPrimary
                        )
                    }
                }

                // Listing Limit quota
                val listingsProgress = if (agentProfile.totalAdsAllowed > 0) {
                    (agentProfile.totalAdsAllowed.toFloat() - agentProfile.adsLimitRemaining.toFloat()) / agentProfile.totalAdsAllowed.toFloat()
                } else 1f
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "سهمیه درج آگهی ملکی در پورتال", fontSize = 12.sp, color = MahoorOnBackground)
                        Text(
                            text = "${agentProfile.adsLimitRemaining} از ${agentProfile.totalAdsAllowed} باقی‌مانده",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MahoorPrimary
                        )
                    }
                    LinearProgressIndicator(
                        progress = { listingsProgress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                        color = MahoorPrimary,
                        trackColor = MahoorSurfaceVariant
                    )
                }

                // Direct web sync limit with external portals
                val syncProgress = if (agentProfile.totalDirectSyncLimit > 0) {
                    (agentProfile.totalDirectSyncLimit.toFloat() - agentProfile.directSyncLimitRemaining.toFloat()) / agentProfile.totalDirectSyncLimit.toFloat()
                } else 1f
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "سهمیه همگام‌سازی دیوار و شیپور", fontSize = 12.sp, color = MahoorOnBackground)
                        Text(
                            text = "${agentProfile.directSyncLimitRemaining} از ${agentProfile.totalDirectSyncLimit} باقی‌مانده",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MahoorPrimary
                        )
                    }
                    LinearProgressIndicator(
                        progress = { syncProgress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                        color = Color(0xFF2ECC71),
                        trackColor = MahoorSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Upgrade Button
                Button(
                    onClick = { showUpgradeDialog = true },
                    modifier = Modifier.fillMaxWidth().testTag("btn_open_upgrade"),
                    colors = ButtonDefaults.buttonColors(containerColor = MahoorPrimary)
                ) {
                    Icon(imageVector = Icons.Filled.WorkspacePremium, contentDescription = null, tint = MahoorOnPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("خرید، ارتقاء یا تمدید اشتراک", color = MahoorOnPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showUpgradeDialog) {
        UpgradeSubscriptionDialog(
            profile = agentProfile,
            onDismiss = { showUpgradeDialog = false },
            onSaveUpdatedLimit = { updatedProfile ->
                onUpdateProfile(updatedProfile)
                showUpgradeDialog = false
            }
        )
    }
}

@Composable
fun InfoItemRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MahoorPrimary.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, fontSize = 10.sp, color = MahoorOnBackground.copy(alpha = 0.5f))
            Text(text = value, fontSize = 13.sp, color = MahoorOnBackground, fontWeight = FontWeight.Medium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpgradeSubscriptionDialog(
    profile: AgentProfile,
    onDismiss: () -> Unit,
    onSaveUpdatedLimit: (AgentProfile) -> Unit
) {
    var selectPlanTier by remember { mutableStateOf(2) } // 0: Free, 1: Silver, 2: Gold, 3: Diamond
    var isAnnualCycle by remember { mutableStateOf(false) } // False = Monthly, True = Annual (25% off)

    // Transaction mock states
    var isProcessingPayment by remember { mutableStateOf(false) }
    var paymentProgress by remember { mutableStateOf(0f) }
    var paymentStatusStep by remember { mutableStateOf("") }
    var isPaymentSuccess by remember { mutableStateOf(false) }

    // Card Input fields for processing
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiryMonth by remember { mutableStateOf("") }
    var cardExpiryYear by remember { mutableStateOf("") }
    var cardCvv2 by remember { mutableStateOf("") }
    var userCaptcha by remember { mutableStateOf("") }
    var bankPinCode by remember { mutableStateOf("") }
    val correctCaptcha = remember { (1000..9999).random().toString() }

    val billingDiscount = if (isAnnualCycle) 0.75f else 1.0f

    val tierName = when (selectPlanTier) {
        0 -> "طرح برنزی (رایگان)"
        1 -> "طرح نقره‌ای (همگام‌ساز)"
        2 -> "طرح طلایی کارگزاری (VIP)"
        else -> "طرح الماس نامحدود"
    }

    val tierBasePrice = when (selectPlanTier) {
        0 -> 0L
        1 -> 450000L
        2 -> 1200000L
        else -> 2800000L
    }

    val calculatedPrice = (tierBasePrice.toDouble() * billingDiscount).toLong()

    val formattedCalculatedPrice = if (calculatedPrice == 0L) "رایگان" else {
        val formatter = java.text.NumberFormat.getInstance(java.util.Locale.US)
        "${formatter.format(calculatedPrice)} تومان"
    }

    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = { if (!isProcessingPayment) onDismiss() },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MahoorSurface,
            tonalElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!isProcessingPayment && !isPaymentSuccess) {
                    // Plan Picker Main Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ارتقاء تخصصی همگام‌ساز ماهور",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MahoorPrimary
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "بستن")
                        }
                    }

                    HorizontalDivider(color = MahoorSurfaceVariant)

                    // Billing Switch Segment: Monthly vs Annual (25% gold medal badge)
                    Text(
                        text = "دوره پرداخت هزینه‌ها:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MahoorOnBackground.copy(alpha = 0.7f)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MahoorSurfaceVariant)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!isAnnualCycle) MahoorPrimary else Color.Transparent)
                                .clickable { isAnnualCycle = false }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ماهانه عادي",
                                color = if (!isAnnualCycle) MahoorOnPrimary else MahoorOnBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isAnnualCycle) MahoorPrimary else Color.Transparent)
                                .clickable { isAnnualCycle = true }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "سالانه ویژه",
                                    color = if (isAnnualCycle) MahoorOnPrimary else MahoorOnBackground,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF2ECC71))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(text = "۲۵٪ تخفیف", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }

                    // Active plans tier boxes
                    Text(
                        text = "انتخاب بهترین طرح ابزار مشاوران:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MahoorOnBackground.copy(alpha = 0.7f)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        PlanSelectBox(
                            title = "طرح برنزی (پایه رایگان)",
                            features = "۵ ملک فعال • منقضی بدون دسترسی همگام‌ساز",
                            priceText = "رایگان",
                            isSelected = selectPlanTier == 0,
                            color = Color(0xFFA67C52),
                            onSelect = { selectPlanTier = 0 }
                        )

                        PlanSelectBox(
                            title = "طرح نقره‌ای همگام‌ساز",
                            features = "۲۰ ملک همزمان • ۳۰ تماس مستقیم درگاه‌ها",
                            priceText = if (isAnnualCycle) "۳۳۷,۵۰۰ ت/ماه" else "۴۵۰,۰۰۰ تومان",
                            isSelected = selectPlanTier == 1,
                            color = Color(0xFFBDC3C7),
                            onSelect = { selectPlanTier = 1 }
                        )

                        PlanSelectBox(
                            title = "طرح طلایی کارگزاری (VIP ویژه پیشنهاد)",
                            features = "۵۰ ملک فعال • ۱۰۰ همگام‌ساز مستقیم هوشمند",
                            priceText = if (isAnnualCycle) "۹۰۰,۰۰۰ ت/ماه" else "۱,۲۰۰,۰۰۰ تومان",
                            isSelected = selectPlanTier == 2,
                            color = MahoorPrimary,
                            onSelect = { selectPlanTier = 2 }
                        )

                        PlanSelectBox(
                            title = "طرح الماس بی‌مرز بنگاهی",
                            features = "درج نامحدود • همگام‌ساز تماماً نامحدود سهمیه",
                            priceText = if (isAnnualCycle) "۲,۱۰۰,۰۰۰ ت/ماه" else "۲,۸۰۰,۰۰۰ تومان",
                            isSelected = selectPlanTier == 3,
                            color = Color(0xFF2980B9),
                            onSelect = { selectPlanTier = 3 }
                        )
                    }

                    // Invoice Calculations Visualizer
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MahoorSurfaceVariant.copy(alpha = 0.5f))
                            .border(0.5.dp, MahoorPrimary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "طرح انتخابی:", fontSize = 12.sp, color = MahoorOnBackground.copy(alpha = 0.6f))
                                Text(text = tierName, fontSize = 13.sp, color = MahoorOnBackground, fontWeight = FontWeight.Bold)
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "بازه تمدید:", fontSize = 12.sp, color = MahoorOnBackground.copy(alpha = 0.6f))
                                Text(text = if (isAnnualCycle) "یک‌ساله (۱۲ ماهه)" else "یک‌ماهه", fontSize = 13.sp, color = MahoorOnBackground)
                            }

                            if (isAnnualCycle && calculatedPrice > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "تخفیف سالانه (۲۵٪):", fontSize = 12.sp, color = Color(0xFF2ECC71))
                                    Text(
                                        text = "- ${(tierBasePrice - calculatedPrice).toString().take(6)} تومان",
                                        fontSize = 13.sp,
                                        color = Color(0xFF2ECC71),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            HorizontalDivider(color = MahoorSurfaceVariant, thickness = 0.5.dp)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "مبلغ خالص فاکتور:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MahoorOnBackground)
                                Text(
                                    text = formattedCalculatedPrice,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MahoorPrimary
                                )
                            }
                        }
                    }

                    // Proceed action Button
                    Button(
                        onClick = {
                            if (selectPlanTier == 0) {
                                // Free plan simply updates immediately without payment screen
                                val newProfile = profile.copy(
                                    currentPlan = "برنزی (رایگان)",
                                    planExpiryDate = "۱۴۰۵/۰۷/۳۰",
                                    adsLimitRemaining = 5,
                                    totalAdsAllowed = 5,
                                    directSyncLimitRemaining = 0,
                                    totalDirectSyncLimit = 0
                                )
                                onSaveUpdatedLimit(newProfile)
                            } else {
                                isProcessingPayment = true
                                // Simulated payment steps
                                coroutineScope.launch {
                                    paymentStatusStep = "درحال آماده‌سازی پیش‌فاکتور ملکی..."
                                    paymentProgress = 0.1f
                                    delay(800)
                                    paymentStatusStep = "اتصال ایمن به سوئیچ یکپارچه به پرداخت شتاب..."
                                    paymentProgress = 0.3f
                                    delay(900)
                                    paymentStatusStep = "منتظر ورود اطلاعات کارت بانکی..."
                                    paymentProgress = 0.5f
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("upgrade_proceed_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MahoorPrimary)
                    ) {
                        Text(
                            text = if (selectPlanTier == 0) "ارتقاء رایگان سریع" else "ورود به درگاه امن پرداخت شتاب",
                            fontWeight = FontWeight.Bold,
                            color = MahoorOnPrimary
                        )
                    }
                } else if (isProcessingPayment && !isPaymentSuccess) {
                    // MOCK PAYMENT GATEWAY INTERFACE (Saman/Sadaad lookalike!)
                    Text(
                        text = "درگاه پرداخت الکترونیک شتاب (ماهور پی)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF2980B9),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "فاکتور خرید اشتراک برای: $tierName • مبلغ نهایی: $formattedCalculatedPrice",
                        fontSize = 11.sp,
                        color = MahoorOnBackground.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider(color = MahoorSurfaceVariant)

                    if (paymentProgress < 0.5f) {
                        // Connecting spinner
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { paymentProgress },
                                color = Color(0xFF2980B9),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = paymentStatusStep, fontSize = 13.sp, color = MahoorOnBackground)
                        }
                    } else {
                        // Form Input
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = cardNumber,
                                onValueChange = { if (it.length <= 16) cardNumber = it },
                                label = { Text("شماره کارت ۱۶ رقمی (بانک شتاب)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                suffix = { Icon(imageVector = Icons.Filled.CreditCard, contentDescription = null, tint = Color.Gray) },
                                modifier = Modifier.fillMaxWidth().testTag("card_input_number"),
                                singleLine = true
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = cardExpiryMonth,
                                    onValueChange = { if (it.length <= 2) cardExpiryMonth = it },
                                    label = { Text("ماه انقضاء (01-12)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f).testTag("card_input_month"),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = cardExpiryYear,
                                    onValueChange = { if (it.length <= 2) cardExpiryYear = it },
                                    label = { Text("سال انقضاء") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f).testTag("card_input_year"),
                                    singleLine = true
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = cardCvv2,
                                    onValueChange = { if (it.length <= 4) cardCvv2 = it },
                                    label = { Text("کد CVV2") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f).testTag("card_input_cvv2"),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = userCaptcha,
                                    onValueChange = { userCaptcha = it },
                                    label = { Text("کد امنیتی: $correctCaptcha") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1.2f).testTag("card_input_captcha"),
                                    singleLine = true
                                )
                            }

                            OutlinedTextField(
                                value = bankPinCode,
                                onValueChange = { bankPinCode = it },
                                label = { Text("رمز دوم پویا یا ثابت کارت") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().testTag("card_input_pin"),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Button(
                                onClick = {
                                    if (cardNumber.length < 16 || cardCvv2.isEmpty() || userCaptcha != correctCaptcha || bankPinCode.isEmpty()) {
                                        paymentStatusStep = "خطا! لطفاً اطلاعات کارت و کد امنیتی را صحیح وارد کنید."
                                    } else {
                                        // Process Mock Pay
                                        coroutineScope.launch {
                                            paymentProgress = 0.6f
                                            paymentStatusStep = "درحال بررسی و کسر وجه مبالغ فاکتور..."
                                            delay(1000)
                                            paymentProgress = 0.8f
                                            paymentStatusStep = "تاییدیه امن کد ارسالی بانک رفاه/صادرات..."
                                            delay(1000)
                                            paymentProgress = 1.0f
                                            isPaymentSuccess = true
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("card_submit_pay_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27AE60))
                            ) {
                                Text("تایید نهایی پرداخت شتاب", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            
                            if (paymentStatusStep.startsWith("خطا")) {
                                Text(
                                    text = paymentStatusStep,
                                    color = Color(0xFFE74C3C),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    isProcessingPayment = false
                                    paymentProgress = 0f
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("انصراف و بازگشت به سبد خرید", color = MahoorOnBackground)
                            }
                        }
                    }
                } else {
                    // SUCCESS ANIMATED CARD TRANSACTION SCREEN!
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "موفق",
                            tint = Color(0xFF2ECC71),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "تراکنش با موفقیت به پایان رسید!",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = MahoorOnBackground,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "طرح انتخابی شما ($tierName) فعال شد. فاکتور تاییدیه تمدید درگاه از طریق پیامک برای شما ارسال گردید.",
                            fontSize = 12.sp,
                            color = MahoorOnBackground.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Transaction Info
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MahoorSurfaceVariant)
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "کد رهگیری شتاب:", fontSize = 11.sp, color = MahoorOnBackground.copy(alpha = 0.6f))
                                    Text(text = "MHR-TX-9382183", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = MahoorOnBackground)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "مبلغ کسر شده:", fontSize = 11.sp, color = MahoorOnBackground.copy(alpha = 0.6f))
                                    Text(text = formattedCalculatedPrice, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MahoorPrimary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                // Compute dynamic new limits based on selected tier
                                val updatedProfile = when (selectPlanTier) {
                                    1 -> profile.copy(
                                        currentPlan = "اشتراک نقره‌ای همگام‌ساز",
                                        planExpiryDate = "۱۴۰۶/۰۸/۱۵",
                                        adsLimitRemaining = 20,
                                        totalAdsAllowed = 20,
                                        directSyncLimitRemaining = 30,
                                        totalDirectSyncLimit = 30
                                    )
                                    2 -> profile.copy(
                                        currentPlan = "اشتراک طلایی ۳ ستاره (VIP)",
                                        planExpiryDate = "۱۴۰۶/۰۷/۱۵",
                                        adsLimitRemaining = 45,
                                        totalAdsAllowed = 50,
                                        directSyncLimitRemaining = 98,
                                        totalDirectSyncLimit = 100
                                    )
                                    3 -> profile.copy(
                                        currentPlan = "اشتراک الماس ویژه بی‌مرز",
                                        planExpiryDate = "۱۴۰۷/۰۶/۳۰",
                                        adsLimitRemaining = 999,
                                        totalAdsAllowed = 999,
                                        directSyncLimitRemaining = 999,
                                        totalDirectSyncLimit = 999
                                    )
                                    else -> profile
                                }
                                onSaveUpdatedLimit(updatedProfile)
                            },
                            modifier = Modifier.fillMaxWidth().testTag("success_confirm_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MahoorPrimary)
                        ) {
                            Text("بازگشت به پنل مشاوران", fontWeight = FontWeight.Bold, color = MahoorOnPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlanSelectBox(
    title: String,
    features: String,
    priceText: String,
    isSelected: Boolean,
    color: Color,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) color.copy(alpha = 0.1f) else MahoorSurfaceVariant)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) color else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSelect() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MahoorOnBackground
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = features,
                    fontSize = 11.sp,
                    color = MahoorOnBackground.copy(alpha = 0.6f)
                )
            }

            Text(
                text = priceText,
                color = if (isSelected) color else MahoorOnBackground.copy(alpha = 0.7f),
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}


// -----------------------------------------------------------------
// EDIT REAL ESTATE AD DIALOG (FOR TAB 0 SEAMLESS MULTI-PORTAL REDIRECTS)
// -----------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAdDialog(
    ad: RealEstateAd,
    onDismiss: () -> Unit,
    onSave: (RealEstateAd) -> Unit
) {
    var title by remember { mutableStateOf(ad.title) }
    var description by remember { mutableStateOf(ad.description) }
    var priceStr by remember { mutableStateOf(ad.price.toString()) }
    var areaStr by remember { mutableStateOf(ad.areaSize.toString()) }
    var roomsStr by remember { mutableStateOf(ad.rooms.toString()) }
    var location by remember { mutableStateOf(ad.location) }
    var type by remember { mutableStateOf(ad.type) }

    var publishDivar by remember { mutableStateOf(ad.publishToDivar) }
    var publishSheypoor by remember { mutableStateOf(ad.publishToSheypoor) }
    var publishMahoor by remember { mutableStateOf(ad.publishToMahoor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MahoorSurface,
            tonalElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ویرایش آگهی همگام‌ساز",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MahoorPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "بستن")
                    }
                }

                HorizontalDivider(color = MahoorSurfaceVariant)

                // Dropdown or Row selector for property Type
                Text(
                    text = "نوع معامله و ملک:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MahoorPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val types = listOf("فروش مسکونی", "رهن و اجاره", "تجاری و اداری", "خرید/فروش زمین")
                    types.forEach { item ->
                        val selected = type == item
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) MahoorPrimary else MahoorSurfaceVariant)
                                .clickable { type = item }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = item,
                                color = if (selected) MahoorOnPrimary else MahoorOnBackground,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان آگهی") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_input_title"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MahoorPrimary,
                        unfocusedBorderColor = MahoorOnBackground.copy(alpha = 0.2f),
                        focusedLabelColor = MahoorPrimary
                    )
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("موقعیت ملک") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_input_location"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MahoorPrimary,
                        unfocusedBorderColor = MahoorOnBackground.copy(alpha = 0.2f),
                        focusedLabelColor = MahoorPrimary
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("قیمت کل (تومان)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.3f).testTag("edit_input_price"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MahoorPrimary,
                            unfocusedBorderColor = MahoorOnBackground.copy(alpha = 0.2f),
                            focusedLabelColor = MahoorPrimary
                        )
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = areaStr,
                        onValueChange = { areaStr = it },
                        label = { Text("متراژ (متر مربع)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("edit_input_area"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MahoorPrimary,
                            unfocusedBorderColor = MahoorOnBackground.copy(alpha = 0.2f),
                            focusedLabelColor = MahoorPrimary
                        )
                    )

                    OutlinedTextField(
                        value = roomsStr,
                        onValueChange = { roomsStr = it },
                        label = { Text("تعداد اتاق") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("edit_input_rooms"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MahoorPrimary,
                            unfocusedBorderColor = MahoorOnBackground.copy(alpha = 0.2f),
                            focusedLabelColor = MahoorPrimary
                        )
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("توضیحات تکمیلی") },
                    modifier = Modifier.fillMaxWidth().height(100.dp).testTag("edit_input_desc"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MahoorPrimary,
                        unfocusedBorderColor = MahoorOnBackground.copy(alpha = 0.2f),
                        focusedLabelColor = MahoorPrimary
                    )
                )

                // Channel selections
                Text(
                    text = "درگاه‌های فعال جهت همگام‌سازی:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MahoorPrimary
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { publishDivar = !publishDivar }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "بروزرسانی مستقیم در دیوار", fontSize = 12.sp, color = MahoorOnBackground)
                        Checkbox(
                            checked = publishDivar,
                            onCheckedChange = { publishDivar = it },
                            modifier = Modifier.testTag("edit_checkbox_divar")
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { publishSheypoor = !publishSheypoor }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "بروزرسانی مستقیم در شیپور", fontSize = 12.sp, color = MahoorOnBackground)
                        Checkbox(
                            checked = publishSheypoor,
                            onCheckedChange = { publishSheypoor = it },
                            modifier = Modifier.testTag("edit_checkbox_sheypoor")
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { publishMahoor = !publishMahoor }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "بروزرسانی در پورتال ماهور", fontSize = 12.sp, color = MahoorOnBackground)
                        Checkbox(
                            checked = publishMahoor,
                            onCheckedChange = { publishMahoor = it },
                            modifier = Modifier.testTag("edit_checkbox_mahoor")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val priceVal = priceStr.toLongOrNull() ?: ad.price
                            val areaVal = areaStr.toIntOrNull() ?: ad.areaSize
                            val roomsVal = roomsStr.toIntOrNull() ?: ad.rooms

                            onSave(
                                ad.copy(
                                    title = title,
                                    description = description,
                                    price = priceVal,
                                    areaSize = areaVal,
                                    rooms = roomsVal,
                                    location = location,
                                    type = type,
                                    publishToDivar = publishDivar,
                                    publishToSheypoor = publishSheypoor,
                                    publishToMahoor = publishMahoor
                                )
                            )
                        },
                        modifier = Modifier.weight(1f).testTag("edit_save_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MahoorPrimary)
                    ) {
                        Text("ذخیره تغییرات", color = MahoorOnPrimary, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).testTag("edit_cancel_btn")
                    ) {
                        Text("انصراف", color = MahoorOnBackground)
                    }
                }
            }
        }
    }
}
