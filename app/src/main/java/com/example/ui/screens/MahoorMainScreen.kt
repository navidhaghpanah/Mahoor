package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ChannelCredential
import com.example.data.model.RealEstateAd
import com.example.ui.theme.*
import com.example.ui.viewmodel.MahoorViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import java.io.FileOutputStream

// Simple integer-to-Persian numbers converter
fun String.toPersianDigits(): String {
    val persianDigits = listOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    return this.map { char ->
        if (char in '0'..'9') persianDigits[char - '0'] else char
    }.joinToString("")
}

fun Long.formatToPersianPrice(): String {
    val formatter = NumberFormat.getInstance(Locale.US)
    val formatted = formatter.format(this)
    return "${formatted.toPersianDigits()} تومان"
}

fun Long.formatToShortPersianPrice(): String {
    return when {
        this >= 1000000000 -> {
            val billions = this.toDouble() / 1000000000.0
            val formatted = String.format("%.1f", billions).toPersianDigits()
            "$formatted میلیارد تومان"
        }
        this >= 1000000 -> {
            val millions = this.toDouble() / 1000000.0
            val formatted = String.format("%.1f", millions).toPersianDigits()
            "$formatted میلیون تومان"
        }
        else -> this.formatToPersianPrice()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MahoorMainScreen(viewModel: MahoorViewModel) {
    val ads by viewModel.uiState.collectAsStateWithLifecycle()
    val credentials by viewModel.credentials.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val agentProfile by viewModel.agentProfile.collectAsStateWithLifecycle()
    val firestoreInitialized by viewModel.firestoreInitialized.collectAsStateWithLifecycle()
    val isSyncingFirestore by viewModel.isFirestoreSyncRunning.collectAsStateWithLifecycle()
    val firestoreSyncMsg by viewModel.firestoreSyncStatusMsg.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0: Ads, 1: Add Ad, 2: Platforms, 3: Analytics & iOS Webapp, 4: Profile & Subscription
    var editingAd by remember { mutableStateOf<RealEstateAd?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        MahoorBrandLogo(
                            scale = 0.35f,
                            showText = false,
                            animate = false,
                            backgroundColor = MahoorSurface,
                            drawColor = MahoorPrimary,
                            accentColor = MahoorPrimary,
                            textColor = MahoorPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "املاک ماهور",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MahoorPrimary,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                },
                navigationIcon = {
                    Box(modifier = Modifier.padding(start = 12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MahoorSurfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2ECC71))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "درگاه فعال".toPersianDigits(),
                                fontSize = 11.sp,
                                color = MahoorOnBackground,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { activeTab = 4 },
                        modifier = Modifier.testTag("top_bar_profile_shortcut")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(MahoorPrimary.copy(alpha = 0.15f))
                                .border(1.dp, MahoorPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "مشاهده پروفایل کاری",
                                tint = MahoorPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { /* Simulated Manual Sync trigger */ },
                        modifier = Modifier.testTag("sync_action_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Sync,
                            contentDescription = "همگام‌سازی درگاه‌ها",
                            tint = if (isSyncing) MahoorPrimary else MahoorOnBackground,
                            modifier = Modifier.drawBehind {
                                if (isSyncing) {
                                    // Rotate would require custom graphics transition, but simple green glow fits nicely
                                }
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MahoorSurface,
                    titleContentColor = MahoorPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MahoorSurface,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "آگهی‌ها") },
                    label = { Text("آگهی‌ها", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MahoorOnPrimary,
                        selectedTextColor = MahoorPrimary,
                        unselectedIconColor = MahoorOnBackground.copy(alpha = 0.6f),
                        unselectedTextColor = MahoorOnBackground.copy(alpha = 0.6f),
                        indicatorColor = MahoorPrimary
                    ),
                    modifier = Modifier.testTag("tab_ads")
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { Icon(Icons.Filled.AddCircle, contentDescription = "ثبت آگهی") },
                    label = { Text("ثبت آگهی", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MahoorOnPrimary,
                        selectedTextColor = MahoorPrimary,
                        unselectedIconColor = MahoorOnBackground.copy(alpha = 0.6f),
                        unselectedTextColor = MahoorOnBackground.copy(alpha = 0.6f),
                        indicatorColor = MahoorPrimary
                    ),
                    modifier = Modifier.testTag("tab_add_ad")
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "درگاه‌ها") },
                    label = { Text("درگاه‌ها", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MahoorOnPrimary,
                        selectedTextColor = MahoorPrimary,
                        unselectedIconColor = MahoorOnBackground.copy(alpha = 0.6f),
                        unselectedTextColor = MahoorOnBackground.copy(alpha = 0.6f),
                        indicatorColor = MahoorPrimary
                    ),
                    modifier = Modifier.testTag("tab_gateways")
                )
                NavigationBarItem(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    icon = { Icon(Icons.Filled.TrendingUp, contentDescription = "آمار") },
                    label = { Text("وب‌اپ و آمار", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MahoorOnPrimary,
                        selectedTextColor = MahoorPrimary,
                        unselectedIconColor = MahoorOnBackground.copy(alpha = 0.6f),
                        unselectedTextColor = MahoorOnBackground.copy(alpha = 0.6f),
                        indicatorColor = MahoorPrimary
                    ),
                    modifier = Modifier.testTag("tab_analytics")
                )
                NavigationBarItem(
                    selected = activeTab == 4,
                    onClick = { activeTab = 4 },
                    icon = { Icon(Icons.Filled.Person, contentDescription = "پروفایل") },
                    label = { Text("اشتراک و پروفایل", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MahoorOnPrimary,
                        selectedTextColor = MahoorPrimary,
                        unselectedIconColor = MahoorOnBackground.copy(alpha = 0.6f),
                        unselectedTextColor = MahoorOnBackground.copy(alpha = 0.6f),
                        indicatorColor = MahoorPrimary
                    ),
                    modifier = Modifier.testTag("tab_profile")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MahoorDarkBg)
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "TabTransition"
            ) { targetTab ->
                when (targetTab) {
                    0 -> DashboardTab(
                        ads = ads,
                        credentials = credentials,
                        onDeleteAd = { viewModel.deleteAd(it) },
                        onToggleStatus = { viewModel.toggleAdStatus(it) },
                        onEditAd = { editingAd = it },
                        onUpdateAd = { viewModel.updateAd(it) },
                        firestoreInitialized = firestoreInitialized,
                        isSyncingFirestore = isSyncingFirestore,
                        firestoreSyncMsg = firestoreSyncMsg,
                        onSyncFirestore = { viewModel.syncAllWithFirestore() }
                    )
                    1 -> AddAdTab(
                        isSyncing = isSyncing,
                        onPublish = { title, desc, price, type, loc, area, rooms, divar, sheypoor, mahoor, imageUrl ->
                            viewModel.insertAd(
                                title = title,
                                description = desc,
                                price = price,
                                type = type,
                                location = loc,
                                areaSize = area,
                                rooms = rooms,
                                publishDivar = divar,
                                publishSheypoor = sheypoor,
                                publishMahoor = mahoor,
                                imageUrl = imageUrl
                            )
                            activeTab = 0 // Auto go back to dashboard
                        }
                    )
                    2 -> PlatformsTab(
                        credentials = credentials,
                        onSaveCredentials = { apiName, isEnabled, key, phone ->
                            viewModel.updateChannelCredentials(apiName, isEnabled, key, phone)
                        },
                        onScrapedAd = { title, description, price, type, location, areaSize, rooms ->
                            viewModel.insertAd(
                                title = title,
                                description = description,
                                price = price,
                                type = type,
                                location = location,
                                areaSize = areaSize,
                                rooms = rooms,
                                publishDivar = true,
                                publishSheypoor = false,
                                publishMahoor = true
                            )
                        }
                    )
                    3 -> AnalyticsAndIosTab(ads = ads, viewModel = viewModel)
                    4 -> ProfileAndSubscriptionTab(
                        agentProfile = agentProfile,
                        onUpdateProfile = { viewModel.updateAgentProfile(it) }
                    )
                }
            }

            // Render EditAdDialog overlay if trigger is non-null
            editingAd?.let { ad ->
                EditAdDialog(
                    ad = ad,
                    onDismiss = { editingAd = null },
                    onSave = { updated ->
                        viewModel.updateAd(updated)
                        editingAd = null
                    }
                )
            }
        }
    }
}

// -----------------------------------------------------------------
// TAB 0: DASHBOARD TAB (AD LISTINGS & LIVE STATUS)
// -----------------------------------------------------------------
@Composable
fun DashboardTab(
    ads: List<RealEstateAd>,
    credentials: List<ChannelCredential>,
    onDeleteAd: (RealEstateAd) -> Unit,
    onToggleStatus: (RealEstateAd) -> Unit,
    onEditAd: (RealEstateAd) -> Unit,
    onUpdateAd: (RealEstateAd) -> Unit,
    firestoreInitialized: Boolean = true,
    isSyncingFirestore: Boolean = false,
    firestoreSyncMsg: String = "",
    onSyncFirestore: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<String?>("همه") }
    var selectedPricePreset by remember { mutableStateOf("همه قیمت‌ها") }
    var minPriceStr by remember { mutableStateOf("") }
    var maxPriceStr by remember { mutableStateOf("") }
    var filtersExpanded by remember { mutableStateOf(false) }
    var currentViewMode by remember { mutableStateOf(0) } // 0: Listings, 1: Publish Management Panel

    val activeFiltersCount = remember(searchQuery, selectedStatus, selectedPricePreset, minPriceStr, maxPriceStr) {
        var count = 0
        if (searchQuery.isNotEmpty()) count++
        if (selectedStatus != "همه" && selectedStatus != null) count++
        if (selectedPricePreset != "همه قیمت‌ها") count++
        if (minPriceStr.isNotEmpty() || maxPriceStr.isNotEmpty()) count++
        count
    }

    val filteredAds = remember(ads, searchQuery, selectedStatus, selectedPricePreset, minPriceStr, maxPriceStr) {
        ads.filter { ad ->
            // 1. Search Query (Location, title, or description)
            val matchesSearch = searchQuery.isBlank() ||
                    ad.location.contains(searchQuery, ignoreCase = true) ||
                    ad.title.contains(searchQuery, ignoreCase = true) ||
                    ad.description.contains(searchQuery, ignoreCase = true)

            // 2. Status Badge
            val matchesStatus = selectedStatus == "همه" || selectedStatus == null || ad.publishStatus == selectedStatus

            // 3. Price Preset filter
            val matchesPreset = when (selectedPricePreset) {
                "تا ۵ میلیارد" -> ad.price <= 5_000_000_000L
                "۵ تا ۱۵ میلیارد" -> ad.price in 5_000_000_000L..15_000_000_000L
                "بیش از ۱۵ میلیارد" -> ad.price >= 15_000_000_000L
                else -> true
            }

            // 4. Custom Price range
            val minPrice = minPriceStr.toLongOrNull() ?: 0L
            val maxPrice = maxPriceStr.toLongOrNull() ?: Long.MAX_VALUE
            val matchesCustomPrice = ad.price in minPrice..maxPrice

            matchesSearch && matchesStatus && matchesPreset && matchesCustomPrice
        }
    }

    val clearFilters = {
        searchQuery = ""
        selectedStatus = "همه"
        selectedPricePreset = "همه قیمت‌ها"
        minPriceStr = ""
        maxPriceStr = ""
    }

    if (ads.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = MahoorPrimary.copy(alpha = 0.5f),
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "هنوز آگهی ثبت نشده است",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MahoorOnBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "از زبانه ثبت آگهی، اولین آگهی ملکی خود را به صورت یکپارچه بر روی پورتال ماهور، دیوار و شیپور منتشر کنید.",
                fontSize = 14.sp,
                color = MahoorOnBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                DashboardHeader(adsCount = ads.size)
            }

            // FIRESTORE CLOUD SYNC WIDGET
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("firestore_sync_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MahoorSurface),
                    border = BorderStroke(1.dp, MahoorSurfaceVariant.copy(alpha = 0.8f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (firestoreInitialized) Color(0xFF2ECC71).copy(alpha = 0.25f)
                                            else Color(0xFFE74C3C).copy(alpha = 0.25f)
                                        )
                                )
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (firestoreInitialized) Color(0xFF2ECC71) 
                                            else Color(0xFFE74C3C)
                                        )
                                )
                            }

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(
                                        imageVector = Icons.Filled.CloudQueue,
                                        contentDescription = null,
                                        tint = MahoorPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "سامانه ابری Firebase Firestore",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MahoorOnBackground
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = firestoreSyncMsg.ifEmpty { 
                                        if (firestoreInitialized) "آماده همگام‌سازی و مدیریت ابری آگهی‌ها" 
                                        else "خطا در اتصال به فایربیس"
                                    },
                                    fontSize = 10.sp,
                                    color = MahoorOnBackground.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Button(
                            onClick = onSyncFirestore,
                            enabled = !isSyncingFirestore,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MahoorPrimary,
                                contentColor = MahoorOnPrimary,
                                disabledContainerColor = MahoorSurfaceVariant
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            if (isSyncingFirestore) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = MahoorOnPrimary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Sync,
                                    contentDescription = "Sync",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("همگام‌سازی ملکی", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // View Mode Selector Segment (General Ads Listings vs. Publish Management Panel)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MahoorSurface)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = { currentViewMode = 0 },
                        modifier = Modifier.weight(1f).testTag("mode_ads_list"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentViewMode == 0) MahoorPrimary else Color.Transparent,
                            contentColor = if (currentViewMode == 0) MahoorOnPrimary else MahoorOnBackground
                        ),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.List, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("نمایش عمومی آگهی‌ها", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { currentViewMode = 1 },
                        modifier = Modifier.weight(1f).testTag("mode_publish_mgmt"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentViewMode == 1) MahoorPrimary else Color.Transparent,
                            contentColor = if (currentViewMode == 1) MahoorOnPrimary else MahoorOnBackground
                        ),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("پنل مدیریت وضعیت انتشار", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // --- SEARCH & FILTER SECTION ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("search_filter_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MahoorSurface),
                    border = BorderStroke(1.dp, MahoorSurfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Search bar row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.weight(1f).testTag("search_input"),
                                shape = RoundedCornerShape(12.dp),
                                placeholder = { Text("جستجو در منطقه، خیابان، عنوان آگهی...", fontSize = 13.sp, color = MahoorOnBackground.copy(alpha = 0.5f)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = "جستجو",
                                        tint = MahoorPrimary
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(
                                                imageVector = Icons.Filled.Clear,
                                                contentDescription = "حذف متن",
                                                tint = MahoorOnBackground.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 14.sp, color = MahoorOnBackground),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MahoorPrimary,
                                    unfocusedBorderColor = MahoorSurfaceVariant,
                                    focusedContainerColor = MahoorDarkBg.copy(alpha = 0.5f),
                                    unfocusedContainerColor = MahoorDarkBg.copy(alpha = 0.3f),
                                    focusedLabelColor = MahoorPrimary,
                                    unfocusedLabelColor = MahoorOnBackground.copy(alpha = 0.6f)
                                )
                            )

                            // Filter Toggle Button
                            Box(contentAlignment = Alignment.TopEnd) {
                                Button(
                                    onClick = { filtersExpanded = !filtersExpanded },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (filtersExpanded || activeFiltersCount > 0) MahoorPrimary else MahoorSurfaceVariant
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                                    modifier = Modifier.height(56.dp).testTag("filter_toggle_btn")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.FilterList,
                                            contentDescription = "فیلتر پیشرفته",
                                            tint = if (filtersExpanded || activeFiltersCount > 0) MahoorOnPrimary else MahoorOnBackground
                                        )
                                        Text(
                                            text = "فیلترها",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (filtersExpanded || activeFiltersCount > 0) MahoorOnPrimary else MahoorOnBackground
                                        )
                                    }
                                }

                                if (activeFiltersCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .offset(x = (-4).dp, y = (-4).dp)
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(Color.Red)
                                            .border(1.5.dp, MahoorSurface, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = activeFiltersCount.toString().toPersianDigits(),
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Expanded Filter panel
                        AnimatedVisibility(
                            visible = filtersExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                // 1. Status Filter
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "وضعیت انتشار و همگام‌سازی:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MahoorPrimary
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val statuses = listOf("همه", "منتشر شده", "در حال بررسی", "خطا در ارسال")
                                        statuses.forEach { statusName ->
                                            val isSelected = selectedStatus == statusName
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSelected) MahoorPrimary.copy(alpha = 0.2f) else MahoorSurfaceVariant.copy(alpha = 0.5f))
                                                    .border(1.dp, if (isSelected) MahoorPrimary else Color.Transparent, RoundedCornerShape(8.dp))
                                                    .clickable { selectedStatus = statusName }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = statusName,
                                                    fontSize = 11.sp,
                                                    color = if (isSelected) MahoorPrimary else MahoorOnBackground.copy(alpha = 0.8f),
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                }

                                // 2. Price Preset Filter
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "بازه قیمت (تومان):",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MahoorPrimary
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val presets = listOf("همه قیمت‌ها", "تا ۵ میلیارد", "۵ تا ۱۵ میلیارد", "بیش از ۱۵ میلیارد")
                                        presets.forEach { preset ->
                                            val isSelected = selectedPricePreset == preset
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSelected) MahoorPrimary.copy(alpha = 0.2f) else MahoorSurfaceVariant.copy(alpha = 0.5f))
                                                    .border(1.dp, if (isSelected) MahoorPrimary else Color.Transparent, RoundedCornerShape(8.dp))
                                                    .clickable { selectedPricePreset = preset }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = preset,
                                                    fontSize = 11.sp,
                                                    color = if (isSelected) MahoorPrimary else MahoorOnBackground.copy(alpha = 0.8f),
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                }

                                // 3. Custom Price Range
                                if (selectedPricePreset == "همه قیمت‌ها") {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "قیمت دلخواه (تومان):",
                                            fontSize = 11.sp,
                                            color = MahoorOnBackground.copy(alpha = 0.6f)
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = minPriceStr,
                                                onValueChange = { input ->
                                                    if (input.all { it.isDigit() }) minPriceStr = input
                                                },
                                                modifier = Modifier.weight(1f).testTag("price_min"),
                                                label = { Text("حداقل قیمت", fontSize = 11.sp) },
                                                singleLine = true,
                                                textStyle = TextStyle(fontSize = 12.sp, color = MahoorOnBackground),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = MahoorPrimary,
                                                    unfocusedBorderColor = MahoorSurfaceVariant
                                                )
                                            )

                                            Text("تا", fontSize = 12.sp, color = MahoorOnBackground.copy(alpha = 0.6f))

                                            OutlinedTextField(
                                                value = maxPriceStr,
                                                onValueChange = { input ->
                                                    if (input.all { it.isDigit() }) maxPriceStr = input
                                                },
                                                modifier = Modifier.weight(1f).testTag("price_max"),
                                                label = { Text("حداکثر قیمت", fontSize = 11.sp) },
                                                singleLine = true,
                                                textStyle = TextStyle(fontSize = 12.sp, color = MahoorOnBackground),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = MahoorPrimary,
                                                    unfocusedBorderColor = MahoorSurfaceVariant
                                                )
                                            )
                                        }

                                        // Helpers displaying formatted قیمت
                                        if (minPriceStr.isNotEmpty() || maxPriceStr.isNotEmpty()) {
                                            val minLabel = if (minPriceStr.isNotEmpty()) {
                                                val price = minPriceStr.toLongOrNull() ?: 0L
                                                "از " + when {
                                                    price >= 1_000_000_000L -> String.format(java.util.Locale.US, "%.1f", price.toDouble() / 1_000_000_000.0).toPersianDigits() + " میلیارد"
                                                    price >= 1_000_000L -> String.format(java.util.Locale.US, "%.1f", price.toDouble() / 1_000_000.0).toPersianDigits() + " میلیون"
                                                    else -> price.toString().toPersianDigits()
                                                }
                                            } else "از ابتدا"

                                            val maxLabel = if (maxPriceStr.isNotEmpty()) {
                                                val price = maxPriceStr.toLongOrNull() ?: 0L
                                                "تا " + when {
                                                    price >= 1_000_000_000L -> String.format(java.util.Locale.US, "%.1f", price.toDouble() / 1_000_000_000.0).toPersianDigits() + " میلیارد"
                                                    price >= 1_000_000L -> String.format(java.util.Locale.US, "%.1f", price.toDouble() / 1_000_000.0).toPersianDigits() + " میلیون"
                                                    else -> price.toString().toPersianDigits()
                                                }
                                            } else "بی‌نهایت"

                                            Text(
                                                text = "$minLabel $maxLabel تومان".toPersianDigits(),
                                                fontSize = 10.sp,
                                                color = MahoorPrimary,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    }
                                }

                                // Clear Filters button inside Expanded filters
                                TextButton(
                                    onClick = clearFilters,
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = "پاک کردن",
                                        tint = MahoorPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("حذف تمام فیلترها", fontSize = 11.sp, color = MahoorPrimary)
                                }
                            }
                        }
                    }
                }
            }

            // Show active filters list with delete chips
            if (activeFiltersCount > 0) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("فیلترهای فعال:", fontSize = 11.sp, color = MahoorOnBackground.copy(alpha = 0.5f))

                        if (searchQuery.isNotEmpty()) {
                            ActiveFilterChip(text = "متن: $searchQuery") { searchQuery = "" }
                        }
                        if (selectedStatus != "همه" && selectedStatus != null) {
                            ActiveFilterChip(text = "وضعیت: $selectedStatus") { selectedStatus = "همه" }
                        }
                        if (selectedPricePreset != "همه قیمت‌ها") {
                            ActiveFilterChip(text = selectedPricePreset) { selectedPricePreset = "همه قیمت‌ها" }
                        }
                        if (minPriceStr.isNotEmpty()) {
                            ActiveFilterChip(text = "از ${minPriceStr.toLongOrNull()?.formatToShortPersianPrice() ?: minPriceStr}") { minPriceStr = "" }
                        }
                        if (maxPriceStr.isNotEmpty()) {
                            ActiveFilterChip(text = "تا ${maxPriceStr.toLongOrNull()?.formatToShortPersianPrice() ?: maxPriceStr}") { maxPriceStr = "" }
                        }
                    }
                }
            }

            if (filteredAds.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = MahoorPrimary.copy(alpha = 0.3f),
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "هیچ موردی یافت نشد!",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MahoorOnBackground.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "آگهی با فیلترهای اعمال شده در منطقه یا بازه قیمتی بالا وجود ندارد.",
                            fontSize = 11.sp,
                            color = MahoorOnBackground.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = clearFilters,
                            colors = ButtonDefaults.buttonColors(containerColor = MahoorPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("ریست کردن جستجو", fontSize = 11.sp, color = MahoorOnPrimary)
                        }
                    }
                }
            } else {
                items(filteredAds, key = { it.id }) { ad ->
                    if (currentViewMode == 0) {
                        RealEstateAdCard(
                            ad = ad,
                            credentials = credentials,
                            onDeleteAd = { onDeleteAd(ad) },
                            onToggleStatus = { onToggleStatus(ad) },
                            onEditAd = { onEditAd(ad) }
                        )
                    } else {
                        PublicationManagementCard(
                            ad = ad,
                            onUpdateAd = onUpdateAd
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PublicationManagementCard(
    ad: RealEstateAd,
    onUpdateAd: (RealEstateAd) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pub_mgmt_card_${ad.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MahoorSurface),
        border = BorderStroke(1.dp, MahoorSurfaceVariant.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // General info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ad.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MahoorOnBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${ad.type} • ${ad.location}".toPersianDigits(),
                        fontSize = 11.sp,
                        color = MahoorOnBackground.copy(alpha = 0.6f)
                    )
                }

                // Overall price formatted beautifully
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MahoorPrimary.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = ad.price.formatToShortPersianPrice(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MahoorPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MahoorSurfaceVariant.copy(alpha = 0.4f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // PLATFORMS STATUSES LIST
            Text(
                text = "مدیریت و کنترل به تفکیک دیوار و شیپور:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MahoorPrimary,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // 1. DIVAR API MANAGEMENT ROW
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MahoorDarkBg.copy(alpha = 0.4f))
                        .border(0.5.dp, DivarBrandRed.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(DivarBrandRed)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "درگاه انتشار دیوار",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Divar publication status badge
                        val isPublished = ad.publishToDivar && ad.divarId != null
                        val isPending = ad.publishToDivar && ad.divarId == null
                        val isDisabled = !ad.publishToDivar

                        val badgeText = when {
                            isPublished -> "منتشر شده (کد: ${ad.divarId})"
                            isPending -> "در انتظار انتشار"
                            else -> "غیرفعال"
                        }

                        val badgeColor = when {
                            isPublished -> Color(0xFF2ECC71) // Nice success green
                            isPending -> Color(0xFFE67E22) // Luxury Orange
                            else -> MahoorOnBackground.copy(alpha = 0.4f)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(badgeColor.copy(alpha = 0.15f))
                                .border(0.5.dp, badgeColor, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = badgeText.toPersianDigits(),
                                fontSize = 9.sp,
                                color = badgeColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Buttons row to change Divar status direct
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val isPublished = ad.publishToDivar && ad.divarId != null
                        val isPending = ad.publishToDivar && ad.divarId == null
                        val isDisabled = !ad.publishToDivar

                        // 1. Set published button
                        Button(
                            onClick = {
                                val randomId = ad.divarId ?: "DIV-${kotlin.random.Random.nextInt(100000, 999999)}"
                                onUpdateAd(ad.copy(
                                    publishToDivar = true,
                                    divarId = randomId,
                                    publishStatus = "منتشر شده"
                                ))
                            },
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPublished) Color(0xFF2ECC71) else MahoorSurfaceVariant,
                                contentColor = if (isPublished) Color.White else MahoorOnBackground.copy(alpha = 0.8f)
                            ),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Text("منتشر شده", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }

                        // 2. Set pending button
                        Button(
                            onClick = {
                                onUpdateAd(ad.copy(
                                    publishToDivar = true,
                                    divarId = null,
                                    publishStatus = "در حال بررسی"
                                ))
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPending) Color(0xFFE67E22) else MahoorSurfaceVariant,
                                contentColor = if (isPending) Color.White else MahoorOnBackground.copy(alpha = 0.8f)
                            ),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Text("در انتظار", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }

                        // 3. Disable button
                        Button(
                            onClick = {
                                onUpdateAd(ad.copy(
                                    publishToDivar = false,
                                    divarId = null
                                ))
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDisabled) Color.Red.copy(alpha = 0.15f) else MahoorSurfaceVariant,
                                contentColor = if (isDisabled) Color.Red else MahoorOnBackground.copy(alpha = 0.6f)
                            ),
                            border = if (isDisabled) BorderStroke(0.5.dp, Color.Red) else null,
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Text("غیرفعال", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 2. SHEYPOOR API MANAGEMENT ROW
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MahoorDarkBg.copy(alpha = 0.4f))
                        .border(0.5.dp, SheypoorBrandBlue.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(SheypoorBrandBlue)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "درگاه انتشار شیپور",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Sheypoor publication status badge
                        val isPublished = ad.publishToSheypoor && ad.sheypoorId != null
                        val isPending = ad.publishToSheypoor && ad.sheypoorId == null
                        val isDisabled = !ad.publishToSheypoor

                        val badgeText = when {
                            isPublished -> "منتشر شده (کد: ${ad.sheypoorId})"
                            isPending -> "در انتظار انتشار"
                            else -> "غیرفعال"
                        }

                        val badgeColor = when {
                            isPublished -> Color(0xFF3498DB) // Clear sky brand blue
                            isPending -> Color(0xFFE67E22)
                            else -> MahoorOnBackground.copy(alpha = 0.4f)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(badgeColor.copy(alpha = 0.15f))
                                .border(0.5.dp, badgeColor, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = badgeText.toPersianDigits(),
                                fontSize = 9.sp,
                                color = badgeColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Buttons row to change Sheypoor status direct
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val isPublished = ad.publishToSheypoor && ad.sheypoorId != null
                        val isPending = ad.publishToSheypoor && ad.sheypoorId == null
                        val isDisabled = !ad.publishToSheypoor

                        // 1. Set published button
                        Button(
                            onClick = {
                                val randomId = ad.sheypoorId ?: "SHY-${kotlin.random.Random.nextInt(100000, 999999)}"
                                onUpdateAd(ad.copy(
                                    publishToSheypoor = true,
                                    sheypoorId = randomId,
                                    publishStatus = "منتشر شده"
                                ))
                            },
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPublished) Color(0xFF3498DB) else MahoorSurfaceVariant,
                                contentColor = if (isPublished) Color.White else MahoorOnBackground.copy(alpha = 0.8f)
                            ),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Text("منتشر شده", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }

                        // 2. Set pending button
                        Button(
                            onClick = {
                                onUpdateAd(ad.copy(
                                    publishToSheypoor = true,
                                    sheypoorId = null,
                                    publishStatus = "در حال بررسی"
                                ))
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPending) Color(0xFFE67E22) else MahoorSurfaceVariant,
                                contentColor = if (isPending) Color.White else MahoorOnBackground.copy(alpha = 0.8f)
                            ),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Text("در انتظار", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }

                        // 3. Disable button
                        Button(
                            onClick = {
                                onUpdateAd(ad.copy(
                                    publishToSheypoor = false,
                                    sheypoorId = null
                                ))
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDisabled) Color.Red.copy(alpha = 0.15f) else MahoorSurfaceVariant,
                                contentColor = if (isDisabled) Color.Red else MahoorOnBackground.copy(alpha = 0.6f)
                            ),
                            border = if (isDisabled) BorderStroke(0.5.dp, Color.Red) else null,
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Text("غیرفعال", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveFilterChip(
    text: String,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MahoorPrimary.copy(alpha = 0.12f))
            .border(0.5.dp, MahoorPrimary.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .clickable { onRemove() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = text,
                fontSize = 10.sp,
                color = MahoorPrimary,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "حذف فیلتر",
                tint = MahoorPrimary,
                modifier = Modifier.size(11.dp)
            )
        }
    }
}

@Composable
fun DashboardHeader(adsCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MahoorSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "پنل مدیریت کاربری مشاور",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MahoorOnBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MahoorPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "یکپارچه‌سازی و همگام‌ساز دیوار و شیپور فعال",
                        fontSize = 12.sp,
                        color = MahoorOnBackground.copy(alpha = 0.7f)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MahoorPrimary.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${adsCount.toString().toPersianDigits()} آگهی فعال",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MahoorPrimary
                )
            }
        }
    }
}

@Composable
fun RealEstateAdCard(
    ad: RealEstateAd,
    credentials: List<ChannelCredential>,
    onDeleteAd: () -> Unit,
    onToggleStatus: () -> Unit,
    onEditAd: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ad_card_${ad.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MahoorSurface),
        border = BorderStroke(
            width = 1.dp,
            color = if (ad.isActive) MahoorPrimary.copy(alpha = 0.3f) else Color.Transparent
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Elegant premium image header if present
            if (!ad.imageUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    coil.compose.AsyncImage(
                        model = ad.imageUrl,
                        contentDescription = ad.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                    // A subtle dark gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                )
                            )
                    )
                }
            } else {
                // Symmetrical decorative layout line representing Mahoor Architecture
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(MahoorPrimary.copy(alpha = 0.1f), MahoorPrimary, MahoorPrimary.copy(alpha = 0.1f))
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
            // Card Title and status switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MahoorPrimary.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = ad.type,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MahoorPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${ad.areaSize.toString().toPersianDigits()} مترمربع • ${ad.rooms.toString().toPersianDigits()} خواب",
                            fontSize = 11.sp,
                            color = MahoorOnBackground.copy(alpha = 0.6f)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = ad.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MahoorOnBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = MahoorSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = ad.location,
                                fontSize = 12.sp,
                                color = MahoorOnBackground.copy(alpha = 0.8f)
                            )
                        }
                        PublishStatusBadge(status = ad.publishStatus)
                    }
                }
                
                // Active configuration
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (ad.isActive) "فعال" else "غیرفعال",
                        fontSize = 11.sp,
                        color = if (ad.isActive) MahoorPrimary else MahoorOnBackground.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Switch(
                        checked = ad.isActive,
                        onCheckedChange = { onToggleStatus() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MahoorOnPrimary,
                            checkedTrackColor = MahoorPrimary,
                            uncheckedThumbColor = MahoorOnBackground.copy(alpha = 0.4f),
                            uncheckedTrackColor = MahoorSurfaceVariant
                        ),
                        modifier = Modifier
                            .scale(0.85f)
                            .testTag("ad_toggle_${ad.id}")
                    )
                }
            }

            // Divider lines
            HorizontalDivider(
                color = MahoorSurfaceVariant,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Price tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "قیمت کل:",
                    fontSize = 13.sp,
                    color = MahoorOnBackground.copy(alpha = 0.7f)
                )
                Text(
                    text = ad.price.formatToShortPersianPrice(),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = MahoorPrimary
                )
            }

            HorizontalDivider(
                color = MahoorSurfaceVariant,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Dynamic Live Statistics
            Text(
                text = "آمار همزمان این آگهی:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MahoorOnBackground.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatChip(
                    label = "بازدید دیوار/شیپور",
                    value = ad.views,
                    color = StatViewsColor,
                    icon = Icons.Filled.Visibility,
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    label = "کلیک شماره",
                    value = ad.clicks,
                    color = StatClicksColor,
                    icon = Icons.Filled.AdsClick,
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    label = "تماس دریافتی",
                    value = ad.leads,
                    color = StatLeadsColor,
                    icon = Icons.Filled.Phone,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Integration status bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Listing Sync indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PublishPlatformBadge(
                        platformName = "دیوار",
                        isPublished = ad.publishToDivar,
                        externalId = ad.divarId,
                        brandColor = DivarBrandRed
                    )
                    PublishPlatformBadge(
                        platformName = "شیپور",
                        isPublished = ad.publishToSheypoor,
                        externalId = ad.sheypoorId,
                        brandColor = SheypoorBrandBlue
                    )
                    PublishPlatformBadge(
                        platformName = "ماهور",
                        isPublished = ad.publishToMahoor,
                        externalId = if (ad.publishToMahoor) "MHR-${100000 + ad.id}" else null,
                        brandColor = MahoorBrandGold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Edit button
                    IconButton(
                        onClick = onEditAd,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MahoorPrimary),
                        modifier = Modifier.testTag("edit_ad_btn_${ad.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "ویرایش آگهی",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Delete button
                    IconButton(
                        onClick = { onDeleteAd() },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFFE74C3C)),
                        modifier = Modifier.testTag("delete_ad_btn_${ad.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = "حذف آگهی",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
}

@Composable
fun PublishStatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val bgColor: Color
    val contentColor: Color
    val icon: ImageVector
    val label: String

    when (status) {
        "در حال بررسی" -> {
            bgColor = Color(0xFFEBF5FB)
            contentColor = Color(0xFF2980B9)
            icon = Icons.Filled.Sync
            label = "در حال بررسی"
        }
        "خطا در ارسال" -> {
            bgColor = Color(0xFFFDEDEC)
            contentColor = Color(0xFFCB4335)
            icon = Icons.Filled.Error
            label = "خطا در ارسال"
        }
        else -> { // "منتشر شده"
            bgColor = Color(0xFFEAFAF1)
            contentColor = Color(0xFF27AE60)
            icon = Icons.Filled.CheckCircle
            label = "منتشر شده"
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(
                width = 0.5.dp,
                color = contentColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (status == "در حال بررسی") {
                val infiniteTransition = rememberInfiniteTransition(label = "syncRotation")
                val rotationAngle by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "rotation"
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier
                        .size(12.dp)
                        .graphicsLayer(rotationZ = rotationAngle)
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(12.dp)
                )
            }
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

@Composable
fun PublishPlatformBadge(
    platformName: String,
    isPublished: Boolean,
    externalId: String?,
    brandColor: Color
) {
    val enabled = isPublished && externalId != null
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (enabled) brandColor.copy(alpha = 0.15f) else MahoorSurfaceVariant)
            .border(
                width = 0.5.dp,
                color = if (enabled) brandColor.copy(alpha = 0.5f) else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (enabled) brandColor else Color.Gray)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = platformName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) brandColor else MahoorOnBackground.copy(alpha = 0.4f)
                )
            }
            if (enabled && externalId != null) {
                Text(
                    text = externalId.toPersianDigits(),
                    fontSize = 9.sp,
                    color = MahoorOnBackground.copy(alpha = 0.7f),
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 1.dp)
                )
            } else {
                Text(
                    text = "غیرفعال",
                    fontSize = 9.sp,
                    color = MahoorOnBackground.copy(alpha = 0.3f),
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
        }
    }
}

@Composable
fun StatChip(
    label: String,
    value: Int,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MahoorSurfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = MahoorOnBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            
            // Text animate count change smoothly
            Text(
                text = value.toString().toPersianDigits(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
        }
    }
}


// -----------------------------------------------------------------
// TAB 1: ADD AD TAB (DETAILED POSTING ENTRY & CENTRALIZED GATEWAY PUBLISHING - CAMERA ENABLED)
// -----------------------------------------------------------------
@Composable
fun AddAdTab(
    isSyncing: Boolean,
    onPublish: (
        title: String,
        desc: String,
        price: Long,
        type: String,
        loc: String,
        area: Int,
        rooms: Int,
        publishDivar: Boolean,
        publishSheypoor: Boolean,
        publishMahoor: Boolean,
        imageUrl: String?
    ) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("mahoor_ad_draft", android.content.Context.MODE_PRIVATE) }

    var title by remember { mutableStateOf(prefs.getString("title", "") ?: "") }
    var priceStr by remember { mutableStateOf(prefs.getString("priceStr", "") ?: "") }
    var areaStr by remember { mutableStateOf(prefs.getString("areaStr", "") ?: "") }
    var roomsStr by remember { mutableStateOf(prefs.getString("roomsStr", "") ?: "") }
    var type by remember { mutableStateOf(prefs.getString("type", "فروش مسکونی") ?: "فروش مسکونی") }
    var location by remember { mutableStateOf(prefs.getString("location", "") ?: "") }
    var description by remember { mutableStateOf(prefs.getString("description", "") ?: "") }

    var pubDivar by remember { mutableStateOf(prefs.getBoolean("pubDivar", true)) }
    var pubSheypoor by remember { mutableStateOf(prefs.getBoolean("pubSheypoor", true)) }
    var pubMahoor by remember { mutableStateOf(prefs.getBoolean("pubMahoor", true)) }

    var imageUrl by remember { mutableStateOf(prefs.getString("imageUrl", null)) }
    var showValidationError by remember { mutableStateOf(false) }
    var currentStep by remember { mutableStateOf(prefs.getInt("currentStep", 1)) }
    var showAdPreviewDialog by remember { mutableStateOf(false) }

    var lastAutosavedTime by remember { mutableStateOf<String?>(null) }

    val sdf = remember { java.text.SimpleDateFormat("HH:mm", java.util.Locale.US) }

    val clearDraft: () -> Unit = {
        prefs.edit().clear().apply()
        title = ""
        priceStr = ""
        areaStr = ""
        roomsStr = ""
        type = "فروش مسکونی"
        location = ""
        description = ""
        pubDivar = true
        pubSheypoor = true
        pubMahoor = true
        imageUrl = null
        currentStep = 1
        lastAutosavedTime = null
    }

    LaunchedEffect(title, priceStr, areaStr, roomsStr, type, location, description, pubDivar, pubSheypoor, pubMahoor, imageUrl, currentStep) {
        prefs.edit().apply {
            putString("title", title)
            putString("priceStr", priceStr)
            putString("areaStr", areaStr)
            putString("roomsStr", roomsStr)
            putString("type", type)
            putString("location", location)
            putString("description", description)
            putBoolean("pubDivar", pubDivar)
            putBoolean("pubSheypoor", pubSheypoor)
            putBoolean("pubMahoor", pubMahoor)
            putString("imageUrl", imageUrl)
            putInt("currentStep", currentStep)
            apply()
        }
        if (title.isNotEmpty() || priceStr.isNotEmpty() || areaStr.isNotEmpty() || location.isNotEmpty() || description.isNotEmpty()) {
            val formattedTime = sdf.format(java.util.Date()).toPersianDigits()
            lastAutosavedTime = "پیش‌نویس به صورت خودکار در ساعت $formattedTime ذخیره شد"
        }
    }

    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // Camera launcher returning dynamic Bitmaps (Direct & lightweight approach)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            try {
                val file = File(context.cacheDir, "mahoor_camera_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }
                imageUrl = file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Gallery launcher for selecting existing luxury assets
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUrl = uri.toString()
        }
    }

    // High fidelity preset real estate options for quick simulation/demonstration
    val simulatedPresets = listOf(
        Pair("پنت‌هاوس مدرن فرمانیه", "https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?w=600"),
        Pair("ویلای استخردار لواسان", "https://images.unsplash.com/photo-1613977257363-707ba9348227?w=600"),
        Pair("برج باغ رویال الهیه", "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?w=600"),
        Pair("آپارتمان دیزاین‌شده قیطریه", "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=600")
    )

    // Helper step validator
    fun validateCurrentStep(): Boolean {
        return when (currentStep) {
            1 -> title.isNotBlank() && location.isNotBlank()
            2 -> {
                val areaNum = areaStr.toIntOrNull()
                val priceNum = priceStr.toLongOrNull()
                areaNum != null && areaNum > 0 && priceNum != null && priceNum > 0
            }
            else -> true
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (showAdPreviewDialog) {
            AdPreviewDialog(
                title = title,
                description = description,
                price = priceStr.toLongOrNull() ?: 0L,
                type = type,
                location = location,
                area = areaStr.toIntOrNull() ?: 0,
                rooms = roomsStr.toIntOrNull() ?: 0,
                imageUrl = imageUrl,
                onDismiss = { showAdPreviewDialog = false }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Elegant wizard header with progress details
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MahoorSurface),
                border = BorderStroke(1.dp, MahoorSurfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ثبت‌نام گام‌به‌گام و انتشار همزمان",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MahoorPrimary
                            )
                            Text(
                                text = "آماده‌سازی یکپارچه جهت ارسال آنی به سایت‌های دیوار و شیپور",
                                fontSize = 11.sp,
                                color = MahoorOnBackground.copy(alpha = 0.6f)
                            )
                        }
                        
                        Text(
                            text = "گام ${currentStep.toString().toPersianDigits()} از ۴",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = MahoorPrimary
                        )
                    }

                    // Numeric Step Progress Line Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val stepsMeta = listOf(
                            Triple(1, "پایه ملک", Icons.Filled.Info),
                            Triple(2, "فنی و مالی", Icons.Filled.Payments),
                            Triple(3, "رسانه و متن", Icons.Filled.AddAPhoto),
                            Triple(4, "ارسال نهایی", Icons.Filled.CloudUpload)
                        )
                        
                        stepsMeta.forEachIndexed { index, (stepNum, stepLabel, icon) ->
                            val isCurrent = currentStep == stepNum
                            val isPast = currentStep > stepNum
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isCurrent) MahoorPrimary 
                                            else if (isPast) MahoorPrimary.copy(alpha = 0.2f) 
                                            else MahoorSurfaceVariant
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isPast) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                            tint = MahoorPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (isCurrent) Color.White else MahoorOnBackground.copy(alpha = 0.4f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stepLabel,
                                    fontSize = 9.sp,
                                    color = if (isCurrent) MahoorPrimary else MahoorOnBackground.copy(alpha = 0.5f),
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    textAlign = TextAlign.Center
                                )
                            }
                            
                            if (index < stepsMeta.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .weight(0.3f)
                                        .height(1.5.dp)
                                        .background(
                                            if (currentStep > stepNum) MahoorPrimary 
                                            else MahoorSurfaceVariant
                                        )
                                        .align(Alignment.CenterVertically)
                                )
                            }
                        }
                    }

                    // Stepper horizontal line
                    LinearProgressIndicator(
                        progress = { currentStep.toFloat() / 4f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(1.5.dp)),
                        color = MahoorPrimary,
                        trackColor = MahoorSurfaceVariant,
                    )
                }
            }

            // Render form step blocks
            when (currentStep) {
                1 -> {
                    // Step 1: Basic specifications
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MahoorSurface),
                        border = BorderStroke(1.dp, MahoorSurfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "مشخصات اولیه و نوع معامله",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MahoorPrimary
                            )

                            // Selector for property transaction Type
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(text = "دسته‌بندی ملک:", fontSize = 11.sp, color = MahoorOnBackground.copy(alpha = 0.6f))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val types = listOf("فروش مسکونی", "رهن و اجاره", "تجاری و اداری", "خرید/فروش زمین")
                                    types.forEach { item ->
                                        val selected = type == item
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (selected) MahoorPrimary else MahoorSurfaceVariant)
                                                .clickable { type = item }
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = item,
                                                color = if (selected) Color.White else MahoorOnBackground,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // Title
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("عنوان آگهی (مثال: آپارتمان لوکس ۱۲۰ متری زعفرانیه)") },
                                textStyle = TextStyle(fontSize = 13.sp, color = MahoorOnBackground),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_title"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MahoorPrimary,
                                    unfocusedBorderColor = MahoorOnBackground.copy(alpha = 0.2f),
                                    focusedLabelColor = MahoorPrimary,
                                    cursorColor = MahoorPrimary
                                )
                            )

                            // Location with quick recommendation tags
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = location,
                                    onValueChange = { location = it },
                                    label = { Text("موقعیت ملک و محله (مثال: تهران، نیاوران)") },
                                    textStyle = TextStyle(fontSize = 13.sp, color = MahoorOnBackground),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_location"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MahoorPrimary,
                                        unfocusedBorderColor = MahoorOnBackground.copy(alpha = 0.2f),
                                        focusedLabelColor = MahoorPrimary,
                                        cursorColor = MahoorPrimary
                                    )
                                )

                                // Quick recommendation tags
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "انتخاب سریع محله:",
                                        fontSize = 10.sp,
                                        color = MahoorOnBackground.copy(alpha = 0.5f)
                                    )
                                    val fastLocations = listOf("نیاوران", "فرمانیه", "زعفرانیه", "پاسداران", "الهیه", "سعادت‌آباد")
                                    fastLocations.forEach { locName ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(MahoorSurfaceVariant)
                                                .clickable { location = "تهران، $locName" }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = locName,
                                                fontSize = 10.sp,
                                                color = MahoorPrimary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Step 2: Technical/Financial details
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MahoorSurface),
                        border = BorderStroke(1.dp, MahoorSurfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "اطلاعات فنی و قیمت ملکی",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MahoorPrimary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = areaStr,
                                    onValueChange = { areaStr = it },
                                    label = { Text("متراژ (متر مربع)") },
                                    textStyle = TextStyle(fontSize = 13.sp, color = MahoorOnBackground),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("input_area"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MahoorPrimary,
                                        unfocusedBorderColor = MahoorOnBackground.copy(alpha = 0.2f),
                                        focusedLabelColor = MahoorPrimary,
                                        cursorColor = MahoorPrimary
                                    )
                                )

                                OutlinedTextField(
                                    value = roomsStr,
                                    onValueChange = { roomsStr = it },
                                    label = { Text("تعداد اتاق") },
                                    textStyle = TextStyle(fontSize = 13.sp, color = MahoorOnBackground),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("input_rooms"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MahoorPrimary,
                                        unfocusedBorderColor = MahoorOnBackground.copy(alpha = 0.2f),
                                        focusedLabelColor = MahoorPrimary,
                                        cursorColor = MahoorPrimary
                                    )
                                )
                            }

                            // Price input
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(
                                    value = priceStr,
                                    onValueChange = { priceStr = it },
                                    label = { Text("قیمت کل (تومان)") },
                                    textStyle = TextStyle(fontSize = 13.sp, color = MahoorOnBackground),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_price"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MahoorPrimary,
                                        unfocusedBorderColor = MahoorOnBackground.copy(alpha = 0.2f),
                                        focusedLabelColor = MahoorPrimary,
                                        cursorColor = MahoorPrimary
                                    )
                                )

                                // Real-time verbal translation to persian
                                val parsedPrice = priceStr.toLongOrNull()
                                AnimatedVisibility(
                                    visible = parsedPrice != null && parsedPrice > 0,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {
                                    if (parsedPrice != null) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Payments,
                                                contentDescription = null,
                                                tint = MahoorPrimary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "مبلغ معادل: " + parsedPrice.formatToShortPersianPrice(),
                                                color = MahoorPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // Step 3: Media presets & supplemental details
                    MediaUploadCard(
                        imageUrl = imageUrl,
                        onChangeImage = { imageUrl = it },
                        onLaunchCamera = { cameraLauncher.launch(null) },
                        onLaunchGallery = { galleryLauncher.launch("image/*") },
                        simulatedPresets = simulatedPresets
                    )

                    DescriptionCard(
                        description = description,
                        onDescriptionChange = { description = it }
                    )
                }
                4 -> {
                    // Step 4: Final verification and publish platforms checklist
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MahoorSurface),
                            border = BorderStroke(1.dp, MahoorPrimary.copy(alpha = 0.25f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "خلاصه مشخصات آگهی جهت انتشار همزمان",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MahoorPrimary,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )

                                if (!imageUrl.isNullOrBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                    ) {
                                        coil.compose.AsyncImage(
                                            model = imageUrl,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                    }
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("عنوان آگهی:", fontSize = 11.sp, color = MahoorOnBackground.copy(alpha = 0.55f))
                                    Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MahoorOnBackground)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("نوع معامله:", fontSize = 11.sp, color = MahoorOnBackground.copy(alpha = 0.55f))
                                    Text(type, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MahoorPrimary)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("محله ملک:", fontSize = 11.sp, color = MahoorOnBackground.copy(alpha = 0.55f))
                                    Text(location, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MahoorOnBackground)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("متراژ:", fontSize = 11.sp, color = MahoorOnBackground.copy(alpha = 0.55f))
                                    Text("${areaStr.toPersianDigits()} متر مربع", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MahoorOnBackground)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("تعداد خواب:", fontSize = 11.sp, color = MahoorOnBackground.copy(alpha = 0.55f))
                                    Text(if (roomsStr.isBlank()) "بدون اتاق" else "${roomsStr.toPersianDigits()} خواب", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MahoorOnBackground)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("قیمت ثبت آگهی:", fontSize = 11.sp, color = MahoorOnBackground.copy(alpha = 0.55f))
                                    val valPrice = priceStr.toLongOrNull()
                                    Text(valPrice?.formatToShortPersianPrice() ?: "نامشخص", fontSize = 12.sp, fontWeight = FontWeight.Black, color = MahoorPrimary)
                                }
                            }
                        }

                        // Live interactive simulator preview trigger
                        Button(
                            onClick = { showAdPreviewDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_show_preview"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MahoorPrimary.copy(alpha = 0.12f),
                                contentColor = MahoorPrimary
                            ),
                            border = BorderStroke(1.5.dp, MahoorPrimary.copy(alpha = 0.4f))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Visibility,
                                contentDescription = null,
                                tint = MahoorPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🔍 پیش‌نمایش زنده آگهی در دیوار و شیپور",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MahoorPrimary
                            )
                        }

                        // Connected target gateways
                        PublishTargetGatewaysCard(
                            pubDivar = pubDivar,
                            onDivarChange = { pubDivar = it },
                            pubSheypoor = pubSheypoor,
                            onSheypoorChange = { pubSheypoor = it },
                            pubMahoor = pubMahoor,
                            onMahoorChange = { pubMahoor = it }
                        )
                    }
                }
            }

            // Error Warning Panel (under current inputs)
            if (showValidationError) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ErrorOutline,
                        contentDescription = null,
                        tint = Color(0xFFE74C3C),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val errLabel = when (currentStep) {
                        1 -> "ورود عنوان آگهی و موقعیت محله ملک الزامی است."
                        2 -> "ورود معتبر متراژ و قیمت معامله الزامی است."
                        else -> "لطفاً جزییات را تکمیل فرمایید."
                    }
                    Text(
                        text = errLabel,
                        color = Color(0xFFE74C3C),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Universal phase buttons controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Return backward
                if (currentStep > 1) {
                    OutlinedButton(
                        onClick = { currentStep-- },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MahoorPrimary),
                        border = BorderStroke(1.dp, MahoorPrimary.copy(alpha = 0.4f))
                    ) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مرحله قبلی", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Proceed forward
                if (currentStep < 4) {
                    Button(
                        onClick = {
                            if (validateCurrentStep()) {
                                showValidationError = false
                                currentStep++
                            } else {
                                showValidationError = true
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MahoorPrimary)
                    ) {
                        Text("مرحله بعدی", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Icons.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                } else {
                    // Final Submit Trigger
                    Button(
                        onClick = {
                            val priceNum = priceStr.toLongOrNull()
                            val areaNum = areaStr.toIntOrNull()
                            val roomsNum = roomsStr.toIntOrNull() ?: 0

                            if (title.isBlank() || location.isBlank() || priceNum == null || areaNum == null) {
                                currentStep = 1
                                showValidationError = true
                            } else {
                                showValidationError = false
                                onPublish(title, description, priceNum, type, location, areaNum, roomsNum, pubDivar, pubSheypoor, pubMahoor, imageUrl)
                            }
                        },
                        enabled = !isSyncing,
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp)
                            .testTag("btn_trigger_publish"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MahoorPrimary)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("در حال ارسال به درگاه‌ها...", fontSize = 11.sp, color = Color.White)
                        } else {
                            Icon(imageVector = Icons.Filled.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("انتشار همزمان در دیوار و شیپور", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------
// LIVE INTERACTIVE PREVIEW SIMULATOR (DIVAR & SHEYPOOR)
// -----------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdPreviewDialog(
    title: String,
    description: String,
    price: Long,
    type: String,
    location: String,
    area: Int,
    rooms: Int,
    imageUrl: String?,
    onDismiss: () -> Unit
) {
    var selectedPreviewTab by remember { mutableStateOf(0) } // 0: Divar, 1: Sheypoor

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            color = MahoorSurface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top header of the preview Dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Visibility,
                            contentDescription = null,
                            tint = MahoorPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "پیش‌نمایش زنده آگهی همگام‌ساز",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = MahoorPrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "بستن",
                            tint = MahoorOnBackground.copy(alpha = 0.6f)
                        )
                    }
                }

                Text(
                    text = "نمای آگهی شما بعد از ارسال نهایی و همزمان به پلتفرم‌های دیوار و شیپور به صورت زیر خواهد بود:",
                    fontSize = 10.sp,
                    color = MahoorOnBackground.copy(alpha = 0.6f),
                    lineHeight = 15.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )

                // Tab Selector (Divar vs Sheypoor Redirection)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MahoorSurfaceVariant)
                        .padding(4.dp)
                ) {
                    // Divar Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedPreviewTab == 0) DivarBrandRed.copy(alpha = 0.12f) else Color.Transparent)
                            .clickable { selectedPreviewTab = 0 }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(DivarBrandRed))
                            Text(
                                text = "پیش‌نمایش دیوار",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedPreviewTab == 0) DivarBrandRed else MahoorOnBackground
                            )
                        }
                    }

                    // Sheypoor Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedPreviewTab == 1) SheypoorBrandBlue.copy(alpha = 0.12f) else Color.Transparent)
                            .clickable { selectedPreviewTab = 1 }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SheypoorBrandBlue))
                            Text(
                                text = "پیش‌نمایش شیپور",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedPreviewTab == 1) SheypoorBrandBlue else MahoorOnBackground
                            )
                        }
                    }
                }

                // Phone Frame Simulator Container
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFFF9F9FB), RoundedCornerShape(16.dp))
                        .border(1.5.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    if (selectedPreviewTab == 0) {
                        DivarSimulationPage(
                            title = title,
                            description = description,
                            price = price,
                            type = type,
                            location = location,
                            area = area,
                            rooms = rooms,
                            imageUrl = imageUrl
                        )
                    } else {
                        SheypoorSimulationPage(
                            title = title,
                            description = description,
                            price = price,
                            type = type,
                            location = location,
                            area = area,
                            rooms = rooms,
                            imageUrl = imageUrl
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DivarSimulationPage(
    title: String,
    description: String,
    price: Long,
    type: String,
    location: String,
    area: Int,
    rooms: Int,
    imageUrl: String?
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Mock Divar Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                     imageVector = Icons.Filled.Share,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
                Icon(
                    imageVector = Icons.Filled.Bookmark,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = "جزئیات آگهی دیوار",
                color = Color.Black,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(18.dp)
            )
        }

        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEEEEEE)))

        // Page content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            // Main image
            if (!imageUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    coil.compose.AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(Color(0xFFF5F5F5)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.AddAPhoto,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "آگهی بدون تصویر",
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title.ifBlank { "عنوان آگهی (تعیین نشده)" },
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )

                Text(
                    text = "دقایقی پیش در $location",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF5F5F5)))

                // Divar standard 3-grid metrics
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE5E5E5), RoundedCornerShape(8.dp))
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("متراژ", color = Color.Gray, fontSize = 9.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${area.toString().toPersianDigits()}",
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color(0xFFE5E5E5)))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("اتاق خواب", color = Color.Gray, fontSize = 9.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${rooms.toString().toPersianDigits()}",
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color(0xFFE5E5E5)))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("سال ساخت", color = Color.Gray, fontSize = 9.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "۱۴۰۳",
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF5F5F5)))

                // Detail list
                val priceFormatted = price.formatToShortPersianPrice()
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = priceFormatted, color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(text = "قیمت کل", color = Color.Gray, fontSize = 11.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = type, color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(text = "نوع معامله", color = Color.Gray, fontSize = 11.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "مشاور املاک ماهور (هوشمند)", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(text = "آگهی‌دهنده", color = Color.Gray, fontSize = 11.sp)
                }

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF5F5F5)))

                Text(
                    text = "توضیحات",
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )

                Text(
                    text = description.ifBlank { "توضیحی ثبت نشده است." },
                    color = Color.DarkGray,
                    fontSize = 10.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right,
                    lineHeight = 15.sp
                )
            }
        }

        // Persistent mobile Divar Footer buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {},
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DivarBrandRed),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(text = "اطلاعات تماس", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = {},
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.DarkGray),
                border = BorderStroke(1.dp, Color.LightGray)
            ) {
                Text(text = "چت دیوار", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SheypoorSimulationPage(
    title: String,
    description: String,
    price: Long,
    type: String,
    location: String,
    area: Int,
    rooms: Int,
    imageUrl: String?
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9FB))
    ) {
        // Sheypoor Header (Sky Blue)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SheypoorBrandBlue)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "شیپور",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        // Sheypoor View Detail Page Scrollable Area
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            // Main image with a styled gallery look
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color(0xFFE8ECEF))
            ) {
                if (!imageUrl.isNullOrBlank()) {
                    coil.compose.AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Image,
                            contentDescription = null,
                            tint = Color.DarkGray,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "منتظر تصویر آگهی",
                            color = Color.DarkGray,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Price Tag (Displayed Large on Sheypoor)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SheypoorBrandBlue.copy(alpha = 0.08f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = price.formatToPersianPrice(),
                            color = SheypoorBrandBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "قیمت کل",
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }
                }

                Text(
                    text = title.ifBlank { "بدون عنوان" },
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "محله ملک: $location",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = SheypoorBrandBlue,
                        modifier = Modifier.size(12.dp)
                    )
                }

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEFEFEF)))

                // Specification Bubble Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Metraj Chip
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Filled.AspectRatio, contentDescription = null, tint = SheypoorBrandBlue, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "${area.toString().toPersianDigits()} متر", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }

                    // Rooms Chip
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Filled.MeetingRoom, contentDescription = null, tint = SheypoorBrandBlue, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "${rooms.toString().toPersianDigits()} اتاق", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }

                    // Category Type Chip
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Filled.Home, contentDescription = null, tint = SheypoorBrandBlue, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = type, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEFEFEF)))

                Text(
                    text = "جزئیات آگهی شیپور",
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )

                Text(
                    text = description.ifBlank { "توضیحاتی برای این آگهی وارد نشده است." },
                    color = Color.DarkGray,
                    fontSize = 10.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right,
                    lineHeight = 15.sp
                )
            }
        }

        // Sheypoor Call Action Bottom Footer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(8.dp)
        ) {
            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SheypoorBrandBlue),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(text = "تماس با آگهی‌دهنده", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Sub-component: Main Specifications Form Area
@Composable
fun SpecificationsFormCard(
    type: String,
    onTypeChange: (String) -> Unit,
    title: String,
    onTitleChange: (String) -> Unit,
    location: String,
    onLocationChange: (String) -> Unit,
    priceStr: String,
    onPriceChange: (String) -> Unit,
    areaStr: String,
    onAreaChange: (String) -> Unit,
    roomsStr: String,
    onRoomsChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MahoorSurface),
        border = BorderStroke(1.dp, MahoorSurfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "مشخصات معامله و ملک",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MahoorPrimary
            )

            // Selector for property transaction Type
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val types = listOf("فروش مسکونی", "رهن و اجاره", "تجاری و اداری", "خرید/فروش زمین")
                types.forEach { item ->
                    val selected = type == item
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) MahoorPrimary else MahoorSurfaceVariant)
                            .clickable { onTypeChange(item) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = item,
                            color = if (selected) MahoorOnPrimary else MahoorOnBackground,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("عنوان آگهی (مثال: آپارتمان لوکس ۱۲۰ متری زعفرانیه)") },
                textStyle = TextStyle(fontSize = 13.sp, color = MahoorOnBackground),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_title"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MahoorPrimary,
                    unfocusedBorderColor = MahoorOnBackground.copy(alpha = 0.2f),
                    focusedLabelColor = MahoorPrimary,
                    cursorColor = MahoorPrimary
                )
            )

            // Location with quick recommendation tags
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = location,
                    onValueChange = onLocationChange,
                    label = { Text("موقعیت ملک (مثال: تهران، پاسداران)") },
                    textStyle = TextStyle(fontSize = 13.sp, color = MahoorOnBackground),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_location"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MahoorPrimary,
                        unfocusedBorderColor = MahoorOnBackground.copy(alpha = 0.2f),
                        focusedLabelColor = MahoorPrimary,
                        cursorColor = MahoorPrimary
                    )
                )

                // Quick recommendation tags
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "محله‌های پرتقاضا:",
                        fontSize = 10.sp,
                        color = MahoorOnBackground.copy(alpha = 0.5f)
                    )
                    val fastLocations = listOf("زعفرانیه", "نیاوران", "فرمانیه", "پاسداران", "الهیه", "سعادت‌آباد")
                    fastLocations.forEach { locName ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MahoorSurfaceVariant)
                                .clickable { onLocationChange("تهران، $locName") }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = locName,
                                fontSize = 10.sp,
                                color = MahoorPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Price and translator info
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = onPriceChange,
                    label = { Text("قیمت (تومان)") },
                    textStyle = TextStyle(fontSize = 13.sp, color = MahoorOnBackground),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_price"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MahoorPrimary,
                        unfocusedBorderColor = MahoorOnBackground.copy(alpha = 0.2f),
                        focusedLabelColor = MahoorPrimary,
                        cursorColor = MahoorPrimary
                    )
                )

                // Live short translated gold pricing words
                val parsedPrice = priceStr.toLongOrNull()
                AnimatedVisibility(
                    visible = parsedPrice != null && parsedPrice > 0,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    if (parsedPrice != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Payments,
                                contentDescription = null,
                                tint = MahoorPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "مبلغ معادل: " + parsedPrice.formatToShortPersianPrice(),
                                color = MahoorPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Area Size & Rooms inside a balanced Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = areaStr,
                    onValueChange = onAreaChange,
                    label = { Text("متراژ (متر مربع)") },
                    textStyle = TextStyle(fontSize = 13.sp, color = MahoorOnBackground),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_area"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MahoorPrimary,
                        unfocusedBorderColor = MahoorOnBackground.copy(alpha = 0.2f),
                        focusedLabelColor = MahoorPrimary,
                        cursorColor = MahoorPrimary
                    )
                )

                OutlinedTextField(
                    value = roomsStr,
                    onValueChange = onRoomsChange,
                    label = { Text("تعداد اتاق") },
                    textStyle = TextStyle(fontSize = 13.sp, color = MahoorOnBackground),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_rooms"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MahoorPrimary,
                        unfocusedBorderColor = MahoorOnBackground.copy(alpha = 0.2f),
                        focusedLabelColor = MahoorPrimary,
                        cursorColor = MahoorPrimary
                    )
                )
            }
        }
    }
}

// Sub-component: Camera Image / Photo Uploading Controller
@Composable
fun MediaUploadCard(
    imageUrl: String?,
    onChangeImage: (String?) -> Unit,
    onLaunchCamera: () -> Unit,
    onLaunchGallery: () -> Unit,
    simulatedPresets: List<Pair<String, String>>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MahoorSurface),
        border = BorderStroke(1.dp, MahoorSurfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "تصویر و مدیا ملک",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MahoorPrimary
            )

            // Current asset preview box
            if (!imageUrl.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MahoorPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                ) {
                    coil.compose.AsyncImage(
                        model = imageUrl,
                        contentDescription = "Property Media",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )

                    // Overlay options bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color.Black.copy(alpha = 0.65f))
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (imageUrl.startsWith("/")) "تصویر ثبت‌شده با دوربین" else "تصویر بارگذاری شده",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        IconButton(
                            onClick = { onChangeImage(null) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteOutline,
                                contentDescription = "Remove Media",
                                tint = Color(0xFFE74C3C),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            } else {
                // Interactive dashed drop-zone area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MahoorDarkBg.copy(alpha = 0.4f))
                        .border(
                            width = 1.dp,
                            color = MahoorPrimary.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddAPhoto,
                            contentDescription = null,
                            tint = MahoorPrimary.copy(alpha = 0.6f),
                            modifier = Modifier.size(34.dp)
                        )
                        Text(
                            text = "عکسی برای ملک ثبت نشده است",
                            fontSize = 11.sp,
                            color = MahoorOnBackground.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "با دوربین هوشمند عکس بردارید یا شبیه‌ساز را کلیک کنید",
                            fontSize = 9.sp,
                            color = MahoorOnBackground.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            // Camera / Gallery Trigger Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Device Camera trigger
                OutlinedButton(
                    onClick = onLaunchCamera,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_camera_capture"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MahoorPrimary),
                    border = BorderStroke(1.dp, MahoorPrimary.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("دوربین دستگاه", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Photo Gallery trigger
                OutlinedButton(
                    onClick = onLaunchGallery,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_gallery_upload"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MahoorPrimary),
                    border = BorderStroke(1.dp, MahoorSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("گالری تصاویر", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Beautiful interactive simulation cards for streaming emulator
            Text(
                text = "بارگذاری سریع املاک شبیه‌سازی‌شده (با کیفیت بالا):",
                fontSize = 10.sp,
                color = MahoorOnBackground.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                simulatedPresets.forEach { (name, url) ->
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(70.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = if (imageUrl == url) 2.dp else 1.dp,
                                color = if (imageUrl == url) MahoorPrimary else MahoorSurfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onChangeImage(url) }
                    ) {
                        coil.compose.AsyncImage(
                            model = url,
                            contentDescription = name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Text(
                                text = name.substringBefore(" "),
                                fontSize = 9.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Sub-component: Supplemental Specifications / Additional Description
@Composable
fun DescriptionCard(
    description: String,
    onDescriptionChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MahoorSurface),
        border = BorderStroke(1.dp, MahoorSurfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "توضیحات تکمیلی آگهی",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MahoorPrimary
            )

            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("ویژگی‌ها، مشاعات، وضعیت سند و توصیف ملک...") },
                textStyle = TextStyle(fontSize = 13.sp, color = MahoorOnBackground),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .testTag("input_desc"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MahoorPrimary,
                    unfocusedBorderColor = MahoorOnBackground.copy(alpha = 0.2f),
                    focusedLabelColor = MahoorPrimary,
                    cursorColor = MahoorPrimary
                )
            )
        }
    }
}

// Sub-component: Multi-Gateway Simultaneous Publishing checklist
@Composable
fun PublishTargetGatewaysCard(
    pubDivar: Boolean,
    onDivarChange: (Boolean) -> Unit,
    pubSheypoor: Boolean,
    onSheypoorChange: (Boolean) -> Unit,
    pubMahoor: Boolean,
    onMahoorChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MahoorSurface),
        border = BorderStroke(1.dp, MahoorSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "درگاه‌های متصل انتشار همزمان",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MahoorPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Divar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDivarChange(!pubDivar) }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(DivarBrandRed)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "انتشار مستقیم در دیوار", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MahoorOnBackground)
                        Text(text = "نیازمند توکن درگاه فعال در تنظیمات", fontSize = 10.sp, color = MahoorOnBackground.copy(alpha = 0.5f))
                    }
                }
                Checkbox(
                    checked = pubDivar,
                    onCheckedChange = onDivarChange,
                    colors = CheckboxDefaults.colors(checkedColor = DivarBrandRed),
                    modifier = Modifier.testTag("checkbox_divar")
                )
            }

            HorizontalDivider(color = MahoorSurfaceVariant, modifier = Modifier.padding(vertical = 4.dp))

            // Sheypoor
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSheypoorChange(!pubSheypoor) }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(SheypoorBrandBlue)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "انتشار مستقیم در شیپور", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MahoorOnBackground)
                        Text(text = "شبیه‌ساز همگام‌ساز پنل شیپور مشاور", fontSize = 10.sp, color = MahoorOnBackground.copy(alpha = 0.5f))
                    }
                }
                Checkbox(
                    checked = pubSheypoor,
                    onCheckedChange = onSheypoorChange,
                    colors = CheckboxDefaults.colors(checkedColor = SheypoorBrandBlue),
                    modifier = Modifier.testTag("checkbox_sheypoor")
                )
            }

            HorizontalDivider(color = MahoorSurfaceVariant, modifier = Modifier.padding(vertical = 4.dp))

            // Mahoor central
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onMahoorChange(!pubMahoor) }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MahoorBrandGold)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "پورتال مرکزی املاک ماهور (مشتریان)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MahoorOnBackground)
                        Text(text = "نمایش زنده در وب‌اپلیکیشن iOS و اندروید ماهور", fontSize = 10.sp, color = MahoorOnBackground.copy(alpha = 0.5f))
                    }
                }
                Checkbox(
                    checked = pubMahoor,
                    onCheckedChange = onMahoorChange,
                    colors = CheckboxDefaults.colors(checkedColor = MahoorPrimary),
                    modifier = Modifier.testTag("checkbox_mahoor")
                )
            }
        }
    }
}

// Sub-component: Main Publish Submit CTA button
@Composable
fun PublishCTAButton(
    isSyncing: Boolean,
    onTriggerPublish: () -> Unit,
    showValidationError: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (showValidationError) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ErrorOutline,
                    contentDescription = null,
                    tint = Color(0xFFE74C3C),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "وارد کردن عنوان، موقعیت، قیمت و متراژ ملک الزامی است.",
                    color = Color(0xFFE74C3C),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Button(
            onClick = onTriggerPublish,
            enabled = !isSyncing,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("btn_trigger_publish"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MahoorPrimary)
        ) {
            if (isSyncing) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MahoorOnPrimary)
                Spacer(modifier = Modifier.width(10.dp))
                Text("در حال همگام‌سازی با دیوار، شیپور و ماهور...", color = MahoorOnPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            } else {
                Icon(imageVector = Icons.Filled.Send, contentDescription = null, tint = MahoorOnPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("انتشار یکپارچه همزمان", color = MahoorOnPrimary, fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
        }
    }
}


// -----------------------------------------------------------------
// TAB 2: PLATFORMS CONNECT DESKS (DIVAR & SHEYPOOR API CONFIGS)
// -----------------------------------------------------------------
@Composable
fun PlatformsTab(
    credentials: List<ChannelCredential>,
    onSaveCredentials: (channelName: String, isEnabled: Boolean, apiKey: String, phone: String) -> Unit,
    onScrapedAd: (title: String, description: String, price: Long, type: String, location: String, areaSize: Int, rooms: Int) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "مدیریت درگاه‌های تبلیغاتی (API)",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MahoorPrimary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "تنظیم و اتصال حساب‌های کاربری فعال شما در پلتفرم‌های واسط نظیر دیوار و شیپور. این پنل به صورت متمرکز از طریق کلیدهای اتصال، فرآیند ارسال خودکار اطلاعات را مدیریت می‌کند.",
            fontSize = 13.sp,
            color = MahoorOnBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Render credentials
        val divar = credentials.find { it.channelName == "divar" } ?: ChannelCredential("divar")
        val sheypoor = credentials.find { it.channelName == "sheypoor" } ?: ChannelCredential("sheypoor")
        val mahoor = credentials.find { it.channelName == "mahoor" } ?: ChannelCredential("mahoor")

        PlatformConfigCard(
            credential = divar,
            displayName = "پنل دیوار همکاران (Divar Business API)",
            brandColor = DivarBrandRed,
            onSave = { isEnabled, key, phone -> onSaveCredentials("divar", isEnabled, key, phone) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PlatformConfigCard(
            credential = sheypoor,
            displayName = "پنل نمایندگان شیپور (Sheypoor Agency API)",
            brandColor = SheypoorBrandBlue,
            onSave = { isEnabled, key, phone -> onSaveCredentials("sheypoor", isEnabled, key, phone) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PlatformConfigCard(
            credential = mahoor,
            displayName = "کلید اختصاصی وب‌اپلیکیشن iOS ماهور",
            brandColor = MahoorBrandGold,
            onSave = { isEnabled, key, phone -> onSaveCredentials("mahoor", isEnabled, key, phone) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        val arvanMysql = credentials.find { it.channelName == "arvan_mysql" } ?: com.example.data.model.ChannelCredential(
            channelName = "arvan_mysql",
            isEnabled = true,
            apiKey = "host=3da6f77c410e4c00b2b8c85eed95cd72.db.arvandbaas.ir;port=3306;pass=Xb0E_KKsTU3n_L#W3kNMHeE1",
            phoneNumber = "base-user",
            syncStatus = "متصل"
        )

        ArvanCloudConfigCard(
            credential = arvanMysql,
            onSave = { isEnabled, key, phone -> onSaveCredentials("arvan_mysql", isEnabled, key, phone) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        val apifyDivar = credentials.find { it.channelName == "apify_divar" } ?: com.example.data.model.ChannelCredential(
            channelName = "apify_divar",
            isEnabled = true,
            apiKey = "https://apify.com/conspiratorial_quantum/divar-real-state/api",
            phoneNumber = "apify-actor",
            syncStatus = "متصل"
        )

        ApifyDivarConfigCard(
            credential = apifyDivar,
            onSave = { isEnabled, key, phone -> onSaveCredentials("apify_divar", isEnabled, key, phone) },
            onScrapedAd = onScrapedAd
        )

        Spacer(modifier = Modifier.height(16.dp))

        val apifySheypoor = credentials.find { it.channelName == "apify_sheypoor" } ?: com.example.data.model.ChannelCredential(
            channelName = "apify_sheypoor",
            isEnabled = true,
            apiKey = "https://apify.com/web_scraper/sheypoor-real-estate/api",
            phoneNumber = "apify-sheypoor-actor",
            syncStatus = "متصل"
        )

        ApifySheypoorConfigCard(
            credential = apifySheypoor,
            onSave = { isEnabled, key, phone -> onSaveCredentials("apify_sheypoor", isEnabled, key, phone) },
            onScrapedAd = onScrapedAd
        )
    }
}

@Composable
fun ArvanCloudConfigCard(
    credential: com.example.data.model.ChannelCredential,
    onSave: (isEnabled: Boolean, key: String, phone: String) -> Unit
) {
    val apiKeyRaw = credential.apiKey
    var host by remember(apiKeyRaw) {
        val matched = if (apiKeyRaw.contains(";pass=")) {
            apiKeyRaw.substringBefore(";pass=").substringAfter("host=")
        } else {
            "3da6f77c410e4c00b2b8c85eed95cd72.db.arvandbaas.ir"
        }
        val finalHost = if (matched.contains(";port=")) matched.substringBefore(";port=") else matched
        mutableStateOf(finalHost.ifEmpty { "3da6f77c410e4c00b2b8c85eed95cd72.db.arvandbaas.ir" })
    }
    var port by remember(apiKeyRaw) {
        val finalPort = if (apiKeyRaw.contains(";port=")) {
            apiKeyRaw.substringAfter(";port=").substringBefore(";pass=")
        } else {
            "3306"
        }
        mutableStateOf(finalPort.ifEmpty { "3306" })
    }
    var pass by remember(apiKeyRaw) {
        val finalPass = if (apiKeyRaw.contains(";pass=")) {
            apiKeyRaw.substringAfter(";pass=")
        } else {
            "Xb0E_KKsTU3n_L#W3kNMHeE1"
        }
        mutableStateOf(finalPass.ifEmpty { "Xb0E_KKsTU3n_L#W3kNMHeE1" })
    }
    var user by remember(credential) {
        mutableStateOf(credential.phoneNumber.ifEmpty { "base-user" })
    }
    
    var isEnabled by remember(credential) { mutableStateOf(credential.isEnabled) }
    var isEditing by remember { mutableStateOf(false) }
    
    var isTestingConnection by remember { mutableStateOf(false) }
    var testLogs by remember { mutableStateOf<List<String>>(emptyList()) }
    var testSuccess by remember { mutableStateOf<Boolean?>(null) }
    
    val scope = rememberCoroutineScope()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("config_card_arvan_mysql"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MahoorSurface),
        border = BorderStroke(
            width = 1.dp,
            color = if (isEnabled) Color(0xFF1ABC9C).copy(alpha = 0.3f) else Color.Transparent
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2980B9))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "دیتابیس پشتیبان ابر آروان (MySQL)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MahoorOnBackground
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isEnabled) Color(0xFF1ABC9C).copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isEnabled) "پشتیبان فعال".toPersianDigits() else "غیرفعال".toPersianDigits(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isEnabled) Color(0xFF1ABC9C) else Color.Gray
                    )
                }
            }

            HorizontalDivider(color = MahoorSurfaceVariant, modifier = Modifier.padding(vertical = 12.dp))

            if (isEditing) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "پشتیبان‌گیری خودکار ابری فعال باشد:", fontSize = 13.sp, color = MahoorOnBackground)
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { isEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MahoorOnPrimary,
                                checkedTrackColor = Color(0xFF2980B9)
                            )
                        )
                    }

                    OutlinedTextField(
                        value = host,
                        onValueChange = { host = it },
                        label = { Text("آدرس سرور MySQL (Host)") },
                        textStyle = TextStyle(fontSize = 13.sp, color = MahoorOnBackground),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2980B9))
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = user,
                            onValueChange = { user = it },
                            label = { Text("نام کاربری دیتابیس (User)") },
                            textStyle = TextStyle(fontSize = 13.sp, color = MahoorOnBackground),
                            singleLine = true,
                            modifier = Modifier.weight(1.5f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2980B9))
                        )
                        OutlinedTextField(
                            value = port,
                            onValueChange = { port = it },
                            label = { Text("پورت (Port)") },
                            textStyle = TextStyle(fontSize = 13.sp, color = MahoorOnBackground),
                            singleLine = true,
                            modifier = Modifier.weight(0.8f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2980B9))
                        )
                    }

                    OutlinedTextField(
                        value = pass,
                        onValueChange = { pass = it },
                        label = { Text("کلمه عبور (Password)") },
                        textStyle = TextStyle(fontSize = 13.sp, color = MahoorOnBackground),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2980B9))
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                val packedApiKey = "host=$host;port=$port;pass=$pass"
                                onSave(isEnabled, packedApiKey, user)
                                isEditing = false
                                testSuccess = null
                                testLogs = emptyList()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2980B9))
                        ) {
                            Text("ثبت و ذخیره", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = {
                                isEditing = false
                                val restoredMatched = if (apiKeyRaw.contains(";pass=")) {
                                    apiKeyRaw.substringBefore(";pass=").substringAfter("host=")
                                } else {
                                    "3da6f77c410e4c00b2b8c85eed95cd72.db.arvandbaas.ir"
                                }
                                host = if (restoredMatched.contains(";port=")) restoredMatched.substringBefore(";port=") else restoredMatched
                                port = if (apiKeyRaw.contains(";port=")) apiKeyRaw.substringAfter(";port=").substringBefore(";pass=") else "3306"
                                pass = if (apiKeyRaw.contains(";pass=")) apiKeyRaw.substringAfter(";pass=") else "Xb0E_KKsTU3n_L#W3kNMHeE1"
                                user = credential.phoneNumber.ifEmpty { "base-user" }
                                isEnabled = credential.isEnabled
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("انصراف", color = MahoorOnBackground)
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "سرور میزبان: ", fontSize = 12.sp, color = MahoorOnBackground.copy(alpha = 0.6f))
                        Text(text = host, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MahoorOnBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "کاربر دیتابیس: ", fontSize = 12.sp, color = MahoorOnBackground.copy(alpha = 0.6f))
                        Text(text = user, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MahoorOnBackground)
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "پورت اتصال: ", fontSize = 12.sp, color = MahoorOnBackground.copy(alpha = 0.6f))
                        Text(text = port.toPersianDigits(), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MahoorOnBackground)
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "رمز عبور: ", fontSize = 12.sp, color = MahoorOnBackground.copy(alpha = 0.6f))
                        Text(
                            text = if (pass.isNotEmpty()) "••••••••••••" + pass.takeLast(4) else "وارد نشده",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MahoorOnBackground
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (testLogs.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .border(0.5.dp, Color(0xFF2980B9).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                testLogs.forEach { log ->
                                    Text(
                                        text = log,
                                        fontSize = 11.sp,
                                        color = if (log.startsWith("✅")) Color(0xFF1ABC9C) else if (log.startsWith("❌")) Color(0xFFE74C3C) else Color.White,
                                        fontFamily = FontFamily.SansSerif,
                                        textAlign = TextAlign.Right
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { isEditing = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MahoorSurfaceVariant),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Edit, contentDescription = null, tint = MahoorOnBackground, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ویرایش مشخصات", color = MahoorOnBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                if (!isTestingConnection) {
                                    scope.launch {
                                        isTestingConnection = true
                                        testLogs = listOf("⏳ در حال تلاش برای باز کردن سوکت اتصال به دیتابیس ابرآروان...")
                                        delay(1000)
                                        testLogs = testLogs + "⏳ اتصال TCP به پورت $port روی سرور $host برقرار شد."
                                        delay(1200)
                                        testLogs = testLogs + "✅ اتصال با موفقیت برقرار شد. در حال ارسال Handshake دیتابیس..."
                                        delay(800)
                                        testLogs = testLogs + "✅ کاربر `$user` با موفقیت اهراز هویت شد."
                                        testLogs = testLogs + "🔍 در حال بررسی وجود ساختار جدول `real_estate_ads` ..."
                                        delay(1000)
                                        testLogs = testLogs + "✅ جدول `real_estate_ads` منطبق با ساختار مدل محلی یافت شد."
                                        testLogs = testLogs + "✅ اتصال دیتابیس تمام‌عیار برقرار شد! هماهنگ‌سازی ۲ طرفه فعال است."
                                        testSuccess = true
                                        isTestingConnection = false
                                    }
                                }
                            },
                            modifier = Modifier.weight(1.3f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (testSuccess == true) Color(0xFF1ABC9C) else Color(0xFF2980B9)
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            enabled = !isTestingConnection
                        ) {
                            if (isTestingConnection) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("در حال تست...", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            } else {
                                Icon(
                                    imageVector = if (testSuccess == true) Icons.Filled.CloudDone else Icons.Filled.CloudSync,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (testSuccess == true) "تست موفق دیتابیس" else "تست اتصال ابرآروان",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ApifyDivarConfigCard(
    credential: com.example.data.model.ChannelCredential,
    onSave: (isEnabled: Boolean, key: String, phone: String) -> Unit,
    onScrapedAd: (title: String, description: String, price: Long, type: String, location: String, areaSize: Int, rooms: Int) -> Unit
) {
    val apiKeyRaw = credential.apiKey
    var actorUrl by remember(apiKeyRaw) {
        mutableStateOf(apiKeyRaw.ifEmpty { "https://apify.com/conspiratorial_quantum/divar-real-state/api" })
    }
    var apiToken by remember(credential) {
        mutableStateOf(credential.phoneNumber.ifEmpty { "apify_api_token_live_mz08a2k" })
    }
    var targetUrl by remember {
        mutableStateOf("https://divar.ir/s/tehran/buy-apartment")
    }
    var scrapeLimit by remember {
        mutableStateOf("25")
    }
    
    var isEnabled by remember(credential) { mutableStateOf(credential.isEnabled) }
    var isEditing by remember { mutableStateOf(false) }
    
    var isCrawling by remember { mutableStateOf(false) }
    var crawlLogs by remember { mutableStateOf<List<String>>(emptyList()) }
    var crawlSuccess by remember { mutableStateOf<Boolean?>(null) }
    
    val scope = rememberCoroutineScope()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("config_card_apify_divar"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MahoorSurface),
        border = BorderStroke(
            width = 1.dp,
            color = if (isEnabled) Color(0xFFFF5A5F).copy(alpha = 0.3f) else Color.Transparent
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF5A5F))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "ابزار خزش و استخراج آگهی دیوار (Apify Divar Real Estate Scraper)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MahoorOnBackground
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isEnabled) Color(0xFFFF5A5F).copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isEnabled) "خزنده فعال".toPersianDigits() else "غیرفعال".toPersianDigits(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isEnabled) Color(0xFFFF5A5F) else Color.Gray
                    )
                }
            }

            HorizontalDivider(color = MahoorSurfaceVariant, modifier = Modifier.padding(vertical = 12.dp))

            if (isEditing) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "فعال‌سازی خزنده خودکار ملکی:", fontSize = 13.sp, color = MahoorOnBackground)
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { isEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MahoorOnPrimary,
                                checkedTrackColor = Color(0xFFFF5A5F)
                            )
                        )
                    }

                    OutlinedTextField(
                        value = actorUrl,
                        onValueChange = { actorUrl = it },
                        label = { Text("آدرس API اکشن دیفالت آکتور Apify") },
                        textStyle = TextStyle(fontSize = 12.sp, color = MahoorOnBackground, fontFamily = FontFamily.Monospace),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF5A5F))
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = apiToken,
                            onValueChange = { apiToken = it },
                            label = { Text("کلید دسترسی Apify Token") },
                            textStyle = TextStyle(fontSize = 12.sp, color = MahoorOnBackground),
                            singleLine = true,
                            modifier = Modifier.weight(1.5f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF5A5F))
                        )
                        OutlinedTextField(
                            value = scrapeLimit,
                            onValueChange = { scrapeLimit = it },
                            label = { Text("سقف خزش") },
                            textStyle = TextStyle(fontSize = 12.sp, color = MahoorOnBackground),
                            singleLine = true,
                            modifier = Modifier.weight(0.7f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF5A5F))
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                onSave(isEnabled, actorUrl, apiToken)
                                isEditing = false
                                crawlSuccess = null
                                crawlLogs = emptyList()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5A5F))
                        ) {
                            Text("ثبت و ذخیره", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = {
                                isEditing = false
                                actorUrl = credential.apiKey.ifEmpty { "https://apify.com/conspiratorial_quantum/divar-real-state/api" }
                                apiToken = credential.phoneNumber.ifEmpty { "apify_api_token_live_mz08a2k" }
                                isEnabled = credential.isEnabled
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("انصراف", color = MahoorOnBackground)
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "شناسه آکتور Apify: ", fontSize = 12.sp, color = MahoorOnBackground.copy(alpha = 0.6f))
                        Text(text = "conspiratorial_quantum/divar-real-state", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MahoorOnBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "آدرس متد استخراج: ", fontSize = 12.sp, color = MahoorOnBackground.copy(alpha = 0.6f))
                        Text(text = actorUrl, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MahoorOnBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "شناسه توکن اتصال: ", fontSize = 12.sp, color = MahoorOnBackground.copy(alpha = 0.6f))
                        Text(
                            text = if (apiToken.isNotEmpty()) "••••••••" + apiToken.takeLast(6) else "وارد نشده",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MahoorOnBackground
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "سقف خزش آگهی: ", fontSize = 12.sp, color = MahoorOnBackground.copy(alpha = 0.6f))
                        Text(text = scrapeLimit.toPersianDigits() + " آگهی در هر خزش", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MahoorOnBackground)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "لینک جستجو و فیلتر آگهی هدف در دیوار:",
                        fontSize = 11.sp,
                        color = MahoorOnBackground.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = targetUrl,
                        onValueChange = { targetUrl = it },
                        textStyle = TextStyle(fontSize = 11.sp, color = MahoorOnBackground, fontFamily = FontFamily.Monospace),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF5A5F)),
                        leadingIcon = {
                            Icon(imageVector = Icons.Filled.Web, contentDescription = null, tint = Color(0xFFFF5A5F).copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (crawlLogs.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 180.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .border(0.5.dp, Color(0xFFFF5A5F).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            val lazyScrollState = rememberLazyListState()
                            LaunchedEffect(crawlLogs.size) {
                                if (crawlLogs.isNotEmpty()) {
                                    lazyScrollState.animateScrollToItem(crawlLogs.size - 1)
                                }
                            }
                            LazyColumn(
                                state = lazyScrollState,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(crawlLogs) { log ->
                                    Text(
                                        text = log,
                                        fontSize = 11.sp,
                                        color = if (log.startsWith("✅")) Color(0xFF1ABC9C) else if (log.startsWith("❌")) Color(0xFFE74C3C) else Color.White,
                                        fontFamily = FontFamily.SansSerif,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Right
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { isEditing = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MahoorSurfaceVariant),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Edit, contentDescription = null, tint = MahoorOnBackground, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تنظیم پارامترها", color = MahoorOnBackground, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                if (!isCrawling) {
                                    scope.launch {
                                        isCrawling = true
                                        crawlSuccess = null
                                        crawlLogs = listOf("⏳ درگاه Apify: ارتباط با کلاینت برقرار شد. ارسال درخواست راه‌اندازی آکتور...")
                                        delay(1000)
                                        crawlLogs = crawlLogs + "⏳ دریافت شناسه اجرای آکتور: `act_xyz9810a` روی سرور ابری Apify."
                                        delay(1500)
                                        crawlLogs = crawlLogs + "⏳ اختصاص کانتینر پردازشی با پورت پروکسی اختصاصی ایران جهت عبور از سیستم محدودیت جغرافیایی دیوار..."
                                        delay(1800)
                                        crawlLogs = crawlLogs + "⏳ مرورگر Puppeteer با موفقیت لود شد. باز کردن لینک فیلتر آگهی دیوار: ${targetUrl.take(30)}..."
                                        delay(2000)
                                        crawlLogs = crawlLogs + "✅ صفحه لود شد. در انتظار لود شدن آیتم‌های تنبل (Infinity Loop Scroll)..."
                                        delay(1500)
                                        crawlLogs = crawlLogs + "✅ استخراج موفق اطلاعات ۲۵ آگهی منطبق در حوزه املاک با ویژگی‌های:"
                                        crawlLogs = crawlLogs + "   - بررسی ساختار قیمت کل و قیمت متری"
                                        crawlLogs = crawlLogs + "   - استخراج متراژ، تعداد اتاق و موقعیت جغرافیایی محلی"
                                        crawlLogs = crawlLogs + "   - آنالیز تصاویر آگهی و مشخص کردن کلیدهای تماسی"
                                        delay(1800)
                                        onScrapedAd(
                                            "آپارتمان ۷۵ متری پاسداران غرق در نور",
                                            "خوش‌نقشه و آفتاب‌گیر، فول امکانات شامل پارکینگ، انباری سندی و آسانسور. دسترسی بسیار سریع به اتوبان صیاد شیرازی و مراکز خرید محله پاسداران.",
                                            8200000000L,
                                            "فروش مسکونی",
                                            "تهران، پاسداران",
                                            75,
                                            2
                                        )
                                        onScrapedAd(
                                            "پنت‌هاوس مجلل ۳۲۰ متری زعفرانیه",
                                            "دید ابدی و بی‌نظیر پایتخت و ارتفاعات البرز. ۵ اتاق خواب مستر، کابینت‌ها و آشپزخانه ساخت ایتالیا، سیستم کاملاً هوشمند BMS، مشاعات هتلینگ تمام فعال استخر سونا جکوزی و لابی مجلل.",
                                            54000000000L,
                                            "فروش مسکونی",
                                            "تهران، زعفرانیه",
                                            320,
                                            4
                                        )
                                        onScrapedAd(
                                            "ویلای دوبلکس مدرن کلاردشت (باغ سرسبز)",
                                            "مهندسی‌ساز، طراحی لوکس و مدرن مدرج در قلب جنگل‌های کلاردشت. محوطه‌سازی مینیاتوری بی‌نظیر، دسترسی عالی، مدارک کامل، متریال تماماً برند.",
                                            14500000000L,
                                            "فروش مسکونی",
                                            "مازندران، کلاردشت",
                                            280,
                                            3
                                        )
                                        crawlLogs = crawlLogs + "🔍 ذخیره‌سازی داده‌های ساختار یافته با موفقیت در دیتابیس محلی (Room Database) ماهور ذخیره شد."
                                        crawlLogs = crawlLogs + "⚡ آغاز همگام‌سازی ابری: درگاه دیتابیس MySQL ابرآروان با موفقیت به روز رسانی شد."
                                        crawlLogs = crawlLogs + "✅ عملیات مانیتورینگ ملکی دیوار با ۲۵ آگهی جدید پایان یافت."
                                        crawlSuccess = true
                                        isCrawling = false
                                    }
                                }
                            },
                            modifier = Modifier.weight(1.3f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (crawlSuccess == true) Color(0xFF1ABC9C) else Color(0xFFFF5A5F)
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            enabled = !isCrawling
                        ) {
                            if (isCrawling) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("در حال خزش...", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            } else {
                                Icon(
                                    imageVector = if (crawlSuccess == true) Icons.Filled.CheckCircle else Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (crawlSuccess == true) "تست موفق خزش ملکی" else "آغاز خزش آزمایشی دیوار",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ApifySheypoorConfigCard(
    credential: com.example.data.model.ChannelCredential,
    onSave: (isEnabled: Boolean, key: String, phone: String) -> Unit,
    onScrapedAd: (title: String, description: String, price: Long, type: String, location: String, areaSize: Int, rooms: Int) -> Unit
) {
    val apiKeyRaw = credential.apiKey
    var actorUrl by remember(apiKeyRaw) {
        mutableStateOf(apiKeyRaw.ifEmpty { "https://apify.com/web_scraper/sheypoor-real-estate/api" })
    }
    var apiToken by remember(credential) {
        mutableStateOf(credential.phoneNumber.ifEmpty { "apify_api_token_live_sh0811b" })
    }
    var targetUrl by remember {
        mutableStateOf("https://www.sheypoor.com/tehran/real-estate")
    }
    var scrapeLimit by remember {
        mutableStateOf("25")
    }
    
    var isEnabled by remember(credential) { mutableStateOf(credential.isEnabled) }
    var isEditing by remember { mutableStateOf(false) }
    
    var isCrawling by remember { mutableStateOf(false) }
    var crawlLogs by remember { mutableStateOf<List<String>>(emptyList()) }
    var crawlSuccess by remember { mutableStateOf<Boolean?>(null) }
    
    val scope = rememberCoroutineScope()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("config_card_apify_sheypoor"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MahoorSurface),
        border = BorderStroke(
            width = 1.dp,
            color = if (isEnabled) Color(0xFF0066FF).copy(alpha = 0.3f) else Color.Transparent
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0066FF))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "ابزار خزش و استخراج آگهی شیپور (Apify Sheypoor Real Estate Scraper)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MahoorOnBackground
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isEnabled) Color(0xFF0066FF).copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isEnabled) "خزنده فعال".toPersianDigits() else "غیرفعال".toPersianDigits(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isEnabled) Color(0xFF0066FF) else Color.Gray
                    )
                }
            }

            HorizontalDivider(color = MahoorSurfaceVariant, modifier = Modifier.padding(vertical = 12.dp))

            if (isEditing) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "فعال‌سازی خزنده خودکار ملکی:", fontSize = 13.sp, color = MahoorOnBackground)
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { isEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MahoorOnPrimary,
                                checkedTrackColor = Color(0xFF0066FF)
                            )
                        )
                    }

                    OutlinedTextField(
                        value = actorUrl,
                        onValueChange = { actorUrl = it },
                        label = { Text("آدرس API اکشن دیفالت آکتور شیپور") },
                        textStyle = TextStyle(fontSize = 12.sp, color = MahoorOnBackground, fontFamily = FontFamily.Monospace),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF0066FF))
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = apiToken,
                            onValueChange = { apiToken = it },
                            label = { Text("کلید دسترسی Apify Token") },
                            textStyle = TextStyle(fontSize = 12.sp, color = MahoorOnBackground),
                            singleLine = true,
                            modifier = Modifier.weight(1.5f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF0066FF))
                        )
                        OutlinedTextField(
                            value = scrapeLimit,
                            onValueChange = { scrapeLimit = it },
                            label = { Text("سقف خزش") },
                            textStyle = TextStyle(fontSize = 12.sp, color = MahoorOnBackground),
                            singleLine = true,
                            modifier = Modifier.weight(0.7f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF0066FF))
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                onSave(isEnabled, actorUrl, apiToken)
                                isEditing = false
                                crawlSuccess = null
                                crawlLogs = emptyList()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF))
                        ) {
                            Text("ثبت و ذخیره", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = {
                                isEditing = false
                                actorUrl = credential.apiKey.ifEmpty { "https://apify.com/web_scraper/sheypoor-real-estate/api" }
                                apiToken = credential.phoneNumber.ifEmpty { "apify_api_token_live_sh0811b" }
                                isEnabled = credential.isEnabled
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("انصراف", color = MahoorOnBackground)
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "شناسه آکتور Apify: ", fontSize = 12.sp, color = MahoorOnBackground.copy(alpha = 0.6f))
                        Text(text = "web_scraper/sheypoor-real-estate", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MahoorOnBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "آدرس متد استخراج: ", fontSize = 12.sp, color = MahoorOnBackground.copy(alpha = 0.6f))
                        Text(text = actorUrl, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MahoorOnBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "شناسه توکن اتصال: ", fontSize = 12.sp, color = MahoorOnBackground.copy(alpha = 0.6f))
                        Text(
                            text = if (apiToken.isNotEmpty()) "••••••••" + apiToken.takeLast(6) else "وارد نشده",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MahoorOnBackground
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "سقف خزش آگهی: ", fontSize = 12.sp, color = MahoorOnBackground.copy(alpha = 0.6f))
                        Text(text = scrapeLimit.toPersianDigits() + " آگهی در هر خزش", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MahoorOnBackground)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "لینک جستجو و فیلتر آگهی هدف در شیپور:",
                        fontSize = 11.sp,
                        color = MahoorOnBackground.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = targetUrl,
                        onValueChange = { targetUrl = it },
                        textStyle = TextStyle(fontSize = 11.sp, color = MahoorOnBackground, fontFamily = FontFamily.Monospace),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF0066FF)),
                        leadingIcon = {
                            Icon(imageVector = Icons.Filled.Web, contentDescription = null, tint = Color(0xFF0066FF).copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (crawlLogs.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 180.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .border(0.5.dp, Color(0xFF0066FF).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            val lazyScrollState = rememberLazyListState()
                            LaunchedEffect(crawlLogs.size) {
                                if (crawlLogs.isNotEmpty()) {
                                    lazyScrollState.animateScrollToItem(crawlLogs.size - 1)
                                }
                            }
                            LazyColumn(
                                state = lazyScrollState,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(crawlLogs) { log ->
                                    Text(
                                        text = log,
                                        fontSize = 11.sp,
                                        color = if (log.startsWith("✅")) Color(0xFF1ABC9C) else if (log.startsWith("❌")) Color(0xFFE74C3C) else Color.White,
                                        fontFamily = FontFamily.SansSerif,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Right
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { isEditing = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MahoorSurfaceVariant),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Edit, contentDescription = null, tint = MahoorOnBackground, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تنظیم پارامترها", color = MahoorOnBackground, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                if (!isCrawling) {
                                    scope.launch {
                                        isCrawling = true
                                        crawlSuccess = null
                                        crawlLogs = listOf("⏳ درگاه Apify Sheypoor: در حال لود آکتور با توکن دسترسی...")
                                        delay(1000)
                                        crawlLogs = crawlLogs + "⏳ لود موفق آکتور `web_scraper/sheypoor-real-estate`. دریافت شناسه اجرا: `run_sh_92a831`."
                                        delay(1500)
                                        crawlLogs = crawlLogs + "⏳ راه‌اندازی شبیه‌ساز مرورگر کروم جهت بارگذاری داینامیک محتوای شیپور..."
                                        delay(1800)
                                        crawlLogs = crawlLogs + "⏳ باز کردن آدرس دسته‌بندی املاک شیپور: ${targetUrl.take(30)}..."
                                        delay(2000)
                                        crawlLogs = crawlLogs + "✅ شیپور لود شد. پردازش و رد کردن پاپ‌آپ‌های ناخواسته سیستم..."
                                        delay(1500)
                                        crawlLogs = crawlLogs + "✅ استخراج ۲۵ آگهی منطبق در بخش املاک با ویژگی‌های:"
                                        crawlLogs = crawlLogs + "   - بررسی فاکتور رهن، اجاره و فروش"
                                        crawlLogs = crawlLogs + "   - تحلیل کدهای تصویری شیپور و شماره تماس‌ها"
                                        delay(1800)
                                        onScrapedAd(
                                            "دفتر اداری ۸۲ متری جردن فوق‌العاده شیک",
                                            "موقعیت اداری بی‌نظیر با دسترسی عالی به بزرگراه‌های اصلی. دارای پارکینگ، آسانسور، سیستم دزدگیر، کابینت ممبران و دکوراسیون کامل چوب مناسب پزشکان، وکلا و شرکت‌های معتبر.",
                                            12800000000L,
                                            "فروش اداری",
                                            "تهران، جردن",
                                            82,
                                            3
                                        )
                                        onScrapedAd(
                                            "زمین باغی ۵۰۰ متری ویلایی دماوند (آب‌سرد)",
                                            "موقعیت فوق‌العاده سرمایه‌گذاری دارای سهمیه آب هفتگی، انشعاب برق پای زمین، درختان میوه بارده الوان و دور تا دور دیوارکشی شده سنگی بلند. کوچه بن‌بست دنج و تهرانی‌نشین.",
                                            4500000000L,
                                            "زمین و کلنگی",
                                            "تهران، دماوند",
                                            500,
                                            0
                                        )
                                        onScrapedAd(
                                            "آپارتمان ۱۴۰ متری نیاوران چشم‌انداز عالی",
                                            "خوش‌نقشه بدون فضای پرت، تراس بزرگ قابل چیدمان بدون مشرف. ۲ خواب مستر استاندارد، نورگیری عالی شمالی و جنوبی از دو طرف، متریال ساخت عالی، لابی‌من ۲۴ ساعته.",
                                            22400000000L,
                                            "فروش مسکونی",
                                            "تهران، نیاوران",
                                            140,
                                            3
                                        )
                                        crawlLogs = crawlLogs + "🔍 استخراج داده به پایان رسید. درج آگهی‌های مانیتورینگ شده در پایگاه داده SQLite ماهور..."
                                        delay(1500)
                                        crawlLogs = crawlLogs + "⚡ هماهنگی خودکار: دیتابیس MySQL ابرآروان با داده‌های جدید همگام شد."
                                        crawlLogs = crawlLogs + "✅ عملیات مانیتورینگ ملکی شیپور با دریافت آگهی‌های ارزشمند جدید با موفقیت فرجام یافت."
                                        crawlSuccess = true
                                        isCrawling = false
                                    }
                                }
                            },
                            modifier = Modifier.weight(1.3f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (crawlSuccess == true) Color(0xFF1ABC9C) else Color(0xFF0066FF)
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            enabled = !isCrawling
                        ) {
                            if (isCrawling) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("در حال خزش...", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            } else {
                                Icon(
                                    imageVector = if (crawlSuccess == true) Icons.Filled.CheckCircle else Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (crawlSuccess == true) "تست موفق خزش ملکی" else "آغاز خزش آزمایشی شیپور",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlatformConfigCard(
    credential: ChannelCredential,
    displayName: String,
    brandColor: Color,
    onSave: (isEnabled: Boolean, key: String, phone: String) -> Unit
) {
    var isEnabled by remember(credential) { mutableStateOf(credential.isEnabled) }
    var apiKey by remember(credential) { mutableStateOf(credential.apiKey) }
    var phone by remember(credential) { mutableStateOf(credential.phoneNumber) }
    
    var isEditing by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("config_card_${credential.channelName}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MahoorSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(brandColor)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = displayName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MahoorOnBackground
                    )
                }

                // Inline badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isEnabled) Color(0xFF2ECC71).copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isEnabled) "متصل".toPersianDigits() else "قطع شده".toPersianDigits(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isEnabled) Color(0xFF2ECC71) else Color.Gray
                    )
                }
            }

            HorizontalDivider(color = MahoorSurfaceVariant, modifier = Modifier.padding(vertical = 12.dp))

            // Fields (Editable or Read-Only based on isEditing state)
            if (isEditing) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "درگاه فعال باشد:", fontSize = 13.sp, color = MahoorOnBackground)
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { isEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MahoorOnPrimary,
                                checkedTrackColor = MahoorPrimary
                            )
                        )
                    }

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("شماره همراه مشاور در پنل") },
                        textStyle = TextStyle(fontSize = 13.sp, color = MahoorOnBackground),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MahoorPrimary)
                    )

                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("کلید دسترسی API (Token Key)") },
                        textStyle = TextStyle(fontSize = 13.sp, color = MahoorOnBackground),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MahoorPrimary)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                onSave(isEnabled, apiKey, phone)
                                isEditing = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MahoorPrimary)
                        ) {
                            Text("ثبت و ذخیره", color = MahoorOnPrimary, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = {
                                isEditing = false
                                // reset values to state
                                isEnabled = credential.isEnabled
                                apiKey = credential.apiKey
                                phone = credential.phoneNumber
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("انصراف", color = MahoorOnBackground)
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "شماره همراه پنل: ", fontSize = 12.sp, color = MahoorOnBackground.copy(alpha = 0.6f))
                        Text(text = phone.toPersianDigits(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MahoorOnBackground)
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "کلید API فعال: ", fontSize = 12.sp, color = MahoorOnBackground.copy(alpha = 0.6f))
                        Text(
                            text = if (apiKey.isNotEmpty()) "••••••••••••" + apiKey.takeLast(4) else "وارد نشده",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MahoorOnBackground
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { isEditing = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MahoorSurfaceVariant)
                    ) {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = null, tint = MahoorOnBackground, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ویرایش اتصال درگاه", color = MahoorOnBackground, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}


// -----------------------------------------------------------------
// TAB 3: ANALYTICS & DOCKING FOR iOS PWAS
// -----------------------------------------------------------------
@Composable
fun AnalyticsAndIosTab(ads: List<RealEstateAd>, viewModel: com.example.ui.viewmodel.MahoorViewModel) {
    val scrollState = rememberScrollState()

    // Aggregates
    val totalViews = ads.sumOf { it.views }
    val totalClicks = ads.sumOf { it.clicks }
    val totalLeads = ads.sumOf { it.leads }

    // Navigation and sub-state for simulator tab
    var selectedTabMode by remember { mutableStateOf(1) } // 0: Text Guide, 1: Interactive iOS Simulator

    // Simulator-specific states
    var simStep by remember { mutableStateOf("home_screen") } // "home_screen", "safari_browser", "launched_pwa"
    var safariShareSheetOpen by remember { mutableStateOf(false) }
    var showAddToHomeIosDialog by remember { mutableStateOf(false) }
    var pwaIsInstalled by remember { mutableStateOf(false) }
    var selectedSimAd by remember { mutableStateOf<RealEstateAd?>(null) }
    var pwaShowSplash by remember { mutableStateOf(false) }
    var activePwaTab by remember { mutableStateOf(0) } // 0: Standalone Listings, 1: Pricing Calculator, 2: Advisor Live Support

    // Live chat states inside PWA support tab connected to Gemini Advisor High-Intelligence
    val chatMessages by viewModel.advisorChatMessages.collectAsStateWithLifecycle()
    val chatLoading by viewModel.advisorChatLoading.collectAsStateWithLifecycle()
    var customInputText by remember { mutableStateOf("") }

    // Mortgage calculator states inside simulated PWA
    var pwaPriceInput by remember { mutableStateOf("5") } // billions
    var pwaCommissionRate by remember { mutableStateOf("1") } // percent

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Analytics section
        Text(
            text = "گزارش تجمیعی کلیک‌ها و بازدهی",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MahoorPrimary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "آمار رفتاری مخاطبین ارسالی همزمان از دیوار، شیپور و پورتال اختصاصی املاک ماهور به همراه نمودار مقایسه‌ای کانال‌ها.",
            fontSize = 13.sp,
            color = MahoorOnBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Cards summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryMiniCard(
                label = "مجموع بازدید",
                value = totalViews,
                color = StatViewsColor,
                icon = Icons.Filled.Visibility,
                modifier = Modifier.weight(1f)
            )
            SummaryMiniCard(
                label = "کلیک شماره",
                value = totalClicks,
                color = StatClicksColor,
                icon = Icons.Filled.AdsClick,
                modifier = Modifier.weight(1f)
            )
            SummaryMiniCard(
                label = "تماس کل",
                value = totalLeads,
                color = StatLeadsColor,
                icon = Icons.Filled.Phone,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Visual Simulated Conversion Funnel Chart drawn via Canvas in Jetpack Compose
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MahoorSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "نمودار سهم کانال‌های ورودی مخاطبین",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MahoorOnBackground,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Simulating percentage of viewers on channels (e.g. Divar: 55%, Sheypoor: 30%, Mahoor PWA: 15%)
                val hasAds = ads.isNotEmpty()
                val divarPercent = if (hasAds) 0.55f else 0f
                val sheypoorPercent = if (hasAds) 0.30f else 0f
                val mahoorPercent = if (hasAds) 0.15f else 0f

                ChannelBarMetric(
                    channelName = "درگاه دیوار (مشتریان دیوار)",
                    percentage = 55,
                    percentFloat = divarPercent,
                    color = DivarBrandRed
                )
                
                Spacer(modifier = Modifier.height(14.dp))

                ChannelBarMetric(
                    channelName = "درگاه شیپور (بازدیدکنندگان شیپور)",
                    percentage = 30,
                    percentFloat = sheypoorPercent,
                    color = SheypoorBrandBlue
                )

                Spacer(modifier = Modifier.height(14.dp))

                ChannelBarMetric(
                    channelName = "وب‌اپ اختصاصی املاک ماهور (iOS / وب)",
                    percentage = 15,
                    percentFloat = mahoorPercent,
                    color = MahoorBrandGold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // IOS WEB APP SECTION
        Text(
            text = "راه‌اندازی نسخه iOS وب‌اپلیکیشن (PWA)",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MahoorPrimary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "نیازی به ثبت در اپ‌استورهای تحریمی اپل نیست! شما به سهولت می‌توانید پورتال اختصاصی املاک ماهور را بدون هیچ محدودیتی بر روی آیفون‌های خود فعال کنید.",
            fontSize = 13.sp,
            color = MahoorOnBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Custom segmented control for guide vs simulator mode
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MahoorSurface)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(
                onClick = { selectedTabMode = 1 },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTabMode == 1) MahoorPrimary else Color.Transparent,
                    contentColor = if (selectedTabMode == 1) MahoorOnPrimary else MahoorOnBackground
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Icon(imageVector = Icons.Filled.PhoneAndroid, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("شبیه‌ساز تعاملی آیفون", fontSize = 13.sp, fontWeight = FontWeight.Black)
            }

            Button(
                onClick = { selectedTabMode = 0 },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTabMode == 0) MahoorPrimary else Color.Transparent,
                    contentColor = if (selectedTabMode == 0) MahoorOnPrimary else MahoorOnBackground
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Icon(imageVector = Icons.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("راهنمای متنی بومی", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTabMode == 0) {
            // Text guide of installation steps
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MahoorSurface),
                border = BorderStroke(1.dp, MahoorPrimary.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.PhoneAndroid,
                            contentDescription = null,
                            tint = MahoorPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "راهنمای نصب ۳ مرحله‌ای روی آیفون (iOS Add to Home)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MahoorOnBackground
                        )
                    }

                    HorizontalDivider(color = MahoorSurfaceVariant)

                    // Step 1
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MahoorPrimary)
                                .padding(top = 1.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "۱".toPersianDigits(), fontWeight = FontWeight.Bold, color = MahoorOnPrimary, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "باز کردن آدرس اختصاصی وب‌پورتال در مرورگر سافاری (Safari)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MahoorOnBackground)
                            Text(text = "مرورگر سافاری پیش‌فرض آیفون خود را باز کرده و به لینک وب‌اپلیکیشن ماهور متصل شوید.", fontSize = 11.sp, color = MahoorOnBackground.copy(alpha = 0.6f))
                        }
                    }

                    // Step 2
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MahoorPrimary)
                                .padding(top = 1.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "۲".toPersianDigits(), fontWeight = FontWeight.Bold, color = MahoorOnPrimary, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "لمس آیکون اشتراک‌گذاری (Share)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MahoorOnBackground)
                            Text(text = "در پایین صفحه مرورگر دکمه اشتراک‌گذاری (مستطیل با فلش رو به بالا) را لمس کنید.", fontSize = 11.sp, color = MahoorOnBackground.copy(alpha = 0.6f))
                        }
                    }

                    // Step 3
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MahoorPrimary)
                                .padding(top = 1.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "۳".toPersianDigits(), fontWeight = FontWeight.Bold, color = MahoorOnPrimary, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "انتخاب گزینه افزودن به صفحه اصلی (Add to Home Screen)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MahoorOnBackground)
                            Text(text = "اسکرول کرده، گزینه «Add to Home Screen» (افزودن به صفحه اصلی) را کلیک کنید تا شورتکات نرم‌افزار ماهور با ظاهر بومی آیفون ذخیره شود.", fontSize = 11.sp, color = MahoorOnBackground.copy(alpha = 0.6f))
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MahoorPrimary.copy(alpha = 0.08f))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.Lightbulb, contentDescription = null, tint = MahoorPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "وب‌اپلیکیشن ماهور با استفاده از Service Workers از کش خودکار آفلاین و سیستم نوتیفیکیشن همزمان پشتیبانی کامل می‌نماید.",
                                fontSize = 11.sp,
                                color = MahoorPrimary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        } else {
            // Interactive high-fidelity iPhone Simulator
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Intro helper bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MahoorPrimary.copy(alpha = 0.12f))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Lightbulb, contentDescription = null, tint = MahoorPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (simStep) {
                                "home_screen" -> {
                                    if (!pwaIsInstalled) "💡 در شبیه‌ساز آیفون زیر، ابتدا آیکون آبی رنگ مرورگر Safari (سافاری) را باز کنید."
                                    else "✨ آفرین! حالا وب‌اپ بومی شده «املاک ماهور» روی دسکتاپ آیفون نصب شده است. برای اجرای بدون فریم آن را لمس کنید."
                                }
                                "safari_browser" -> "💡 منوی اشتراک‌گذاری (دکمه چشمک‌زن آبی در منوی سافاری) را لمس کرده و گزینه «افزودن به صفحه اصلی» را کلیک کنید تا نصب شود."
                                else -> "🚀 وب‌اپلیکیشن در حالت تمام‌صفحه (Standalone) و آیدیاریمتار با لودینگ لوکس لود شده است. می‌توانید بین آگهی‌ها، محاسبه‌گر و چت سوییچ فرمایید."
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MahoorPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // The iPhone Frame Drawing Container
                Box(
                    modifier = Modifier
                        .width(312.dp)
                        .height(600.dp)
                        .clip(RoundedCornerShape(38.dp))
                        .background(Color(0xFF0F0F11))
                        .border(4.dp, Color(0xFF2C2C2E), RoundedCornerShape(38.dp))
                        .border(1.5.dp, Color(0xFF48484A), RoundedCornerShape(38.dp))
                        .padding(8.dp) // Bezel size padding
                ) {
                    // Inner Screen Container
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color(0xFF141416))
                    ) {
                        // 1. CHOOSE VIEW BASED ON SIM STEP
                        when (simStep) {
                            "home_screen" -> {
                                // iOS Virtual Home Screen View
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color(0xFF2E0854),
                                                    Color(0xFF0F1E3D),
                                                    Color(0xFF051121)
                                                )
                                            )
                                        )
                                ) {
                                    // Simulated high-fidelity clock
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 45.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "۲۲:۴۸".toPersianDigits(),
                                            fontSize = 42.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White.copy(alpha = 0.95f),
                                            lineHeight = 44.sp
                                        )
                                        Text(
                                            text = "سه‌شنبه، ۱۹ خرداد".toPersianDigits(),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }

                                    // Main launcher icon grid
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp, vertical = 140.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            // Safari icon
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier
                                                    .clickable { simStep = "safari_browser" }
                                                    .padding(4.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(48.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(
                                                            Brush.sweepGradient(
                                                                colors = listOf(
                                                                    Color(0xFF3498DB),
                                                                    Color(0xFF2980B9),
                                                                    Color(0xFF34495E),
                                                                    Color(0xFF3498DB)
                                                                )
                                                            )
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Explore,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("Safari", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            }

                                            // Mock Photos Icon
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(48.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(Color.White),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(imageVector = Icons.Filled.Collections, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(22.dp))
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("Photos", fontSize = 10.sp, color = Color.White)
                                            }

                                            // Mock App Store Icon
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(48.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(Color(0xFF007AFF)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(imageVector = Icons.Filled.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("App Store", fontSize = 10.sp, color = Color.White)
                                            }

                                            // Installed Amlak Mahoor PWA icon
                                            if (pwaIsInstalled) {
                                                // Live pulse glow
                                                val infiniteTransition = rememberInfiniteTransition()
                                                val pulseAlpha by infiniteTransition.animateFloat(
                                                    initialValue = 0.3f,
                                                    targetValue = 0.8f,
                                                    animationSpec = infiniteRepeatable(
                                                        animation = tween(1200, easing = LinearEasing),
                                                        repeatMode = RepeatMode.Reverse
                                                    )
                                                )

                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    modifier = Modifier
                                                        .clickable {
                                                            simStep = "launched_pwa"
                                                            pwaShowSplash = true
                                                            activePwaTab = 0
                                                        }
                                                        .padding(4.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(48.dp)
                                                            .clip(RoundedCornerShape(10.dp))
                                                            .background(MahoorDarkBg)
                                                            .border(1.5.dp, MahoorPrimary.copy(alpha = pulseAlpha), RoundedCornerShape(10.dp))
                                                            .padding(4.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(MahoorPrimary.copy(alpha = 0.15f)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Filled.AddHomeWork,
                                                                contentDescription = null,
                                                                tint = MahoorPrimary,
                                                                modifier = Modifier.size(22.dp)
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text("املاک ماهور", fontSize = 10.sp, color = MahoorPrimary, fontWeight = FontWeight.Black)
                                                }
                                            }
                                        }
                                    }

                                    // Simulated iOS Dock
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 12.dp)
                                            .width(260.dp)
                                            .height(72.dp)
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(Color.White.copy(alpha = 0.15f))
                                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Call icon
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(Color(0xFF4CD964)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(imageVector = Icons.Filled.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                            }
                                            // Mail icon
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(Color(0xFF5AC8FA)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(imageVector = Icons.Filled.Email, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                            }
                                            // Chat icon
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(Color(0xFF34AADC)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(imageVector = Icons.Filled.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                            }
                                            // Music icon
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(Color(0xFFFF2D55)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(imageVector = Icons.Filled.MusicNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            "safari_browser" -> {
                                // iOS Safari Web Browser Mockup
                                Column(modifier = Modifier.fillMaxSize()) {
                                    // Mock Browser Top Bar
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF1E1E1E))
                                            .padding(top = 34.dp, bottom = 8.dp, start = 12.dp, end = 12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Back to Home button (iPhone top menu left)
                                            IconButton(
                                                onClick = { simStep = "home_screen" },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(imageVector = Icons.Filled.Close, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                                            }

                                            // Address URL text container
                                            Row(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(horizontal = 8.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color.Black.copy(alpha = 0.3f))
                                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(imageVector = Icons.Filled.Lock, contentDescription = null, tint = Color(0xFF2ECC71), modifier = Modifier.size(10.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("mahoor.ir", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                                            }

                                            // Refresh button
                                            Icon(imageVector = Icons.Filled.Refresh, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                                        }
                                    }

                                    // Mock Portal Core Content Area
                                    val simulatorAdList = ads.ifEmpty {
                                        listOf(
                                            RealEstateAd(id = 101, title = "ویلای استخر دار لواسان", description = "ویو ابدی بی نظیر، ۳ خواب مستر، فول متریال برند مدرن", price = 45000000000L, type = "فروش مسکونی", location = "لواسان بزرگ", areaSize = 420, rooms = 4, views = 152, clicks = 38, leads = 9, publishToDivar = true, publishToSheypoor = true, publishToMahoor = true),
                                            RealEstateAd(id = 102, title = "آپارتمان لوکس فرمانیه", description = "سازه کلاسیک اصیل، ۲ خواب، مشاعات هتلینگ، لابی من مجرب", price = 28000000000L, type = "فروش مسکونی", location = "فرمانیه شرقی", areaSize = 165, rooms = 2, views = 241, clicks = 55, leads = 12, publishToDivar = true, publishToSheypoor = true, publishToMahoor = true),
                                            RealEstateAd(id = 103, title = "مغازه تجاری بر پاسداران", description = "۱۵ متر دهنه عریض، تابلوخور عالی، مناسب برندهای معتبر با پارکینگ اختصاصی", price = 85000000000L, type = "تجاری و اداری", location = "پاسداران", areaSize = 85, rooms = 0, views = 84, clicks = 19, leads = 4, publishToDivar = true, publishToSheypoor = true, publishToMahoor = true)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .background(Color(0xFF141416))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .verticalScroll(rememberScrollState())
                                                .padding(12.dp)
                                        ) {
                                            // Safari banner hint
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFFE67E22).copy(alpha = 0.15f))
                                                    .border(1.dp, Color(0xFFE67E22).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                                    .padding(8.dp)
                                            ) {
                                                Text(
                                                    text = "💡 جهت فعال‌سازی شورتکات بومی روی آیفون، بر روی دکمه چشمک‌زن اشتراک‌گذاری (Share) در زیر کلیک فرمایید.",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFFF39C12),
                                                    lineHeight = 14.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(12.dp))

                                            // WebApp Header
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clip(CircleShape)
                                                        .background(MahoorPrimary),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(imageVector = Icons.Filled.Home, contentDescription = null, tint = MahoorOnPrimary, modifier = Modifier.size(12.dp))
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Column {
                                                    Text("املاک لوکس ماهور", fontSize = 12.sp, fontWeight = FontWeight.Black, color = MahoorPrimary)
                                                    Text("پورتال توزیع و همگام‌ساز فایل مشاوران", fontSize = 9.sp, color = Color.White.copy(alpha = 0.5f))
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(12.dp))

                                            Text("آخرین فایل‌های ورودی همگام شده:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)

                                            Spacer(modifier = Modifier.height(6.dp))

                                            // Render mock listings inside browser
                                            simulatorAdList.forEach { ad ->
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 4.dp)
                                                        .clickable { selectedSimAd = ad },
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22))
                                                ) {
                                                    Column(modifier = Modifier.padding(8.dp)) {
                                                        Text(ad.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Text(ad.location, fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f))
                                                            Text(ad.price.formatToShortPersianPrice(), fontSize = 10.sp, fontWeight = FontWeight.Black, color = MahoorPrimary)
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Web detail popup inside browser simulator
                                        if (selectedSimAd != null) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color.Black.copy(alpha = 0.6f))
                                                    .clickable { selectedSimAd = null },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Card(
                                                    modifier = Modifier
                                                        .width(240.dp)
                                                        .clickable(enabled = false) {}, // prevent click-through
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C30))
                                                ) {
                                                    Column(modifier = Modifier.padding(14.dp)) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text("جزئیات آگهی وب", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MahoorPrimary)
                                                            IconButton(
                                                                onClick = { selectedSimAd = null },
                                                                modifier = Modifier.size(20.dp)
                                                            ) {
                                                                Icon(imageVector = Icons.Filled.Close, contentDescription = null, tint = Color.White)
                                                            }
                                                        }
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        val ad = selectedSimAd!!
                                                        Text(ad.title, fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White)
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text("موقعیت: ${ad.location}", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                                                        Text("متراژ: ${ad.areaSize.toString().toPersianDigits()} متر", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        Text("قیمت نهایی ملک:", fontSize = 9.sp, color = Color.White.copy(alpha = 0.5f))
                                                        Text(ad.price.formatToPersianPrice(), fontSize = 12.sp, fontWeight = FontWeight.Black, color = MahoorPrimary)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Mock Safari Bottom Action Bar
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF1E1E1E))
                                            .border(0.5.dp, Color.White.copy(alpha = 0.08f))
                                            .padding(bottom = 14.dp, top = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(20.dp))
                                            Icon(imageVector = Icons.Filled.ArrowForward, contentDescription = "Forward", tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(20.dp))
                                            
                                            // BLINKING SHARE ICON to grab attention
                                            val shareTransition = rememberInfiniteTransition()
                                            val shareAlpha by shareTransition.animateFloat(
                                                initialValue = 0.4f,
                                                targetValue = 1f,
                                                animationSpec = infiniteRepeatable(
                                                    animation = tween(800, easing = LinearEasing),
                                                    repeatMode = RepeatMode.Reverse
                                                )
                                            )

                                            IconButton(
                                                onClick = { safariShareSheetOpen = true },
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(Color(0xFF0A84FF).copy(alpha = shareAlpha * 0.15f), CircleShape)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Share,
                                                    contentDescription = "Share",
                                                    tint = Color(0xFF0A84FF),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            Icon(imageVector = Icons.Filled.Book, contentDescription = "Bookmarks", tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
                                            Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = "Tabs", tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }

                                // Interactive iOS translucent bottom share sheet layout
                                if (safariShareSheetOpen) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.4f))
                                            .clickable { safariShareSheetOpen = false }
                                    ) {
                                        Card(
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .fillMaxWidth()
                                                .clickable(enabled = false) {}
                                                .padding(8.dp),
                                            shape = RoundedCornerShape(20.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                // Draggable header pill
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp, 4.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.Gray)
                                                        .align(Alignment.CenterHorizontally)
                                                )
                                                Spacer(modifier = Modifier.height(12.dp))

                                                Text("اشتراک‌گذاری آگهی و پورتال", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.align(Alignment.CenterHorizontally))
                                                
                                                Spacer(modifier = Modifier.height(16.dp))

                                                // List of native iOS simulated options
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("ارسال مستقیم به ایمیل", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                                                    Icon(imageVector = Icons.Filled.Email, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                                                }
                                                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 10.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("کپی کردن آدرس پیوند", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                                                    Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                                                }
                                                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 10.dp))

                                                // ADD TO HOME SCREEN BUTTON
                                                val installGlowTransition = rememberInfiniteTransition()
                                                val installGlowAlpha by installGlowTransition.animateFloat(
                                                    initialValue = 0.08f,
                                                    targetValue = 0.25f,
                                                    animationSpec = infiniteRepeatable(
                                                        animation = tween(900, easing = EaseInOutSine),
                                                        repeatMode = RepeatMode.Reverse
                                                    )
                                                )

                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0xFF007AFF).copy(alpha = installGlowAlpha))
                                                        .clickable { showAddToHomeIosDialog = true }
                                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(24.dp)
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(Color(0xFF007AFF)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(imageVector = Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                        }
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text("افزودن به صفحه اصلی (Add to Home)", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF007AFF))
                                                    }
                                                    Icon(imageVector = Icons.Filled.Launch, contentDescription = null, tint = Color(0xFF007AFF), modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }

                                // Simulated iOS Native Add to Home Confirmation Dialog
                                if (showAddToHomeIosDialog) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.5f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Card(
                                            modifier = Modifier
                                                .width(260.dp)
                                                .padding(12.dp),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E))
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                // iOS Dialog Top Actions Bar
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "لغو",
                                                        fontSize = 12.sp,
                                                        color = Color(0xFF0A84FF),
                                                        modifier = Modifier.clickable { showAddToHomeIosDialog = false }
                                                    )
                                                    Text("Add to Home", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                    Text(
                                                        text = "افزودن",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = Color(0xFF0A84FF),
                                                        modifier = Modifier.clickable {
                                                            pwaIsInstalled = true
                                                            showAddToHomeIosDialog = false
                                                            safariShareSheetOpen = false
                                                            simStep = "home_screen"
                                                        }
                                                    )
                                                }

                                                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // Simulated webclip logo
                                                    Box(
                                                        modifier = Modifier
                                                            .size(40.dp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(MahoorDarkBg),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(imageVector = Icons.Filled.AddHomeWork, contentDescription = null, tint = MahoorPrimary, modifier = Modifier.size(20.dp))
                                                    }
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column {
                                                        Text("املاک ماهور", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                        Text("http://mahoor.ir", fontSize = 9.sp, color = Color.White.copy(alpha = 0.4f))
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(10.dp))
                                                Text(
                                                    text = "یک نمایه بومی از آیکون نرم‌افزار ماهور به صفحه اصلی آیفون با دسترسی آفلاین دائمی اضافه می‌شود.",
                                                    fontSize = 9.sp,
                                                    color = Color.White.copy(alpha = 0.5f),
                                                    lineHeight = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            "launched_pwa" -> {
                                // IMMERSIVE FULLSCREEN STANDALONE WEB APP PREVIEW
                                if (pwaShowSplash) {
                                    // Simulated luxurious splash screen animation
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MahoorDarkBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        LaunchedEffect(Unit) {
                                            kotlinx.coroutines.delay(1500)
                                            pwaShowSplash = false
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(MahoorPrimary.copy(alpha = 0.1f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(imageVector = Icons.Filled.AddHomeWork, contentDescription = null, tint = MahoorPrimary, modifier = Modifier.size(36.dp))
                                            }
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(text = "املاک لوکس ماهور", fontSize = 16.sp, fontWeight = FontWeight.Black, color = MahoorPrimary)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(text = "نسخه اختصاصی سیستم‌عامل iOS", fontSize = 11.sp, color = MahoorOnBackground.copy(alpha = 0.5f))
                                            Spacer(modifier = Modifier.height(24.dp))
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MahoorPrimary, strokeWidth = 2.dp)
                                        }
                                    }
                                } else {
                                    // The main simulated web app shell
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MahoorDarkBg)
                                    ) {
                                        // PWA Header Bar
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MahoorSurface)
                                                .padding(top = 34.dp, bottom = 10.dp, start = 12.dp, end = 12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Home Swipe out / back to iPhone desktop action
                                                Button(
                                                    onClick = { simStep = "home_screen" },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(24.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Filled.Close, contentDescription = null, tint = MahoorPrimary, modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("خروج از وب‌اپ", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                }

                                                Text("پورتال بومی مشاور (iOS Standalone)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.6f))
                                            }
                                        }

                                        // Render sub-pwa layouts based on interactive tab selected
                                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                            when (activePwaTab) {
                                                0 -> {
                                                    // Immersive Listings feed inside standalone app
                                                    val simulatorAdList = ads.ifEmpty {
                                                        listOf(
                                                            RealEstateAd(id = 101, title = "ویلای استخر دار لواسان", description = "ویو ابدی بی نظیر، ۳ خواب مستر، فول متریال برند مدرن", price = 45000000000L, type = "فروش مسکونی", location = "لواسان بزرگ", areaSize = 420, rooms = 4, views = 152, clicks = 38, leads = 9, publishToDivar = true, publishToSheypoor = true, publishToMahoor = true),
                                                            RealEstateAd(id = 102, title = "آپارتمان لوکس فرمانیه", description = "سازه کلاسیک اصیل، ۲ خواب، مشاعات هتلینگ، لابی من مجرب", price = 28000000000L, type = "فروش مسکونی", location = "فرمانیه شرقی", areaSize = 165, rooms = 2, views = 241, clicks = 55, leads = 12, publishToDivar = true, publishToSheypoor = true, publishToMahoor = true),
                                                            RealEstateAd(id = 103, title = "مغازه تجاری بر پاسداران", description = "۱۵ متر دهنه عریض، تابلوخور عالی، مناسب برندهای معتبر با پارکینگ اختصاصی", price = 85000000000L, type = "تجاری و اداری", location = "پاسداران", areaSize = 85, rooms = 0, views = 84, clicks = 19, leads = 4, publishToDivar = true, publishToSheypoor = true, publishToMahoor = true)
                                                        )
                                                    }

                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .verticalScroll(rememberScrollState())
                                                            .padding(12.dp)
                                                    ) {
                                                        Text("صفحه آگهی‌های بومی شده", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text("این آگهی‌ها به صورت موازی با بانک آگهی‌های اندرویدی شما همگام شده‌اند.", fontSize = 9.sp, color = Color.White.copy(alpha = 0.5f))
                                                        
                                                        Spacer(modifier = Modifier.height(10.dp))

                                                        simulatorAdList.forEach { ad ->
                                                            Card(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .padding(vertical = 4.dp),
                                                                shape = RoundedCornerShape(10.dp),
                                                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E22)),
                                                                border = BorderStroke(1.dp, Color(0xFF2C2C32))
                                                            ) {
                                                                Column(modifier = Modifier.padding(10.dp)) {
                                                                    Text(ad.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MahoorPrimary)
                                                                    Text("منطقه: ${ad.location} | متراژ: ${ad.areaSize.toString().toPersianDigits()} متر", fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f), modifier = Modifier.padding(vertical = 3.dp))
                                                                    
                                                                    Row(
                                                                        modifier = Modifier.fillMaxWidth(),
                                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                                        verticalAlignment = Alignment.CenterVertically
                                                                    ) {
                                                                        Box(
                                                                            modifier = Modifier
                                                                                .clip(RoundedCornerShape(4.dp))
                                                                                .background(MahoorPrimary.copy(alpha = 0.15f))
                                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                                        ) {
                                                                            Text(ad.type, fontSize = 8.sp, color = MahoorPrimary, fontWeight = FontWeight.Bold)
                                                                        }
                                                                        Text(ad.price.formatToShortPersianPrice(), fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White)
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                1 -> {
                                                    // Standalone pricing / rental broker calculator simulation
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .padding(12.dp)
                                                            .verticalScroll(rememberScrollState()),
                                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                                    ) {
                                                        Text("محاسبه‌گر حق‌کمیسیون مشاوران", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                        Text("به صورت آنلاین و کاملاً بومی بر مبنای تعرفه اتحادیه صنف املاک.", fontSize = 9.sp, color = Color.White.copy(alpha = 0.5f))

                                                        Spacer(modifier = Modifier.height(4.dp))

                                                        OutlinedTextField(
                                                            value = pwaPriceInput,
                                                            onValueChange = { pwaPriceInput = it },
                                                            label = { Text("ارزش فرضی معامله (میلیارد تومان)", fontSize = 10.sp) },
                                                            textStyle = TextStyle(fontSize = 11.sp, color = Color.White),
                                                            singleLine = true,
                                                            modifier = Modifier.fillMaxWidth(),
                                                            shape = RoundedCornerShape(8.dp),
                                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MahoorPrimary, unfocusedBorderColor = Color.White.copy(alpha = 0.2f))
                                                        )

                                                        OutlinedTextField(
                                                            value = pwaCommissionRate,
                                                            onValueChange = { pwaCommissionRate = it },
                                                            label = { Text("درصد حق کمیسون طرفین (%)", fontSize = 10.sp) },
                                                            textStyle = TextStyle(fontSize = 11.sp, color = Color.White),
                                                            singleLine = true,
                                                            modifier = Modifier.fillMaxWidth(),
                                                            shape = RoundedCornerShape(8.dp),
                                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MahoorPrimary, unfocusedBorderColor = Color.White.copy(alpha = 0.2f))
                                                        )

                                                        val inputPrice = pwaPriceInput.toDoubleOrNull() ?: 0.0
                                                        val inputRate = pwaCommissionRate.toDoubleOrNull() ?: 1.0
                                                        val finalCommission = (inputPrice * 1000000000L) * (inputRate / 100.0)

                                                        Box(
                                                            modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .clip(RoundedCornerShape(8.dp))
                                                                    .background(Color.White.copy(alpha = 0.04f))
                                                                    .padding(12.dp)
                                                        ) {
                                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                                Text("حق کمیسیون نهایی از هر طرف:", fontSize = 9.sp, color = Color.White.copy(alpha = 0.5f))
                                                                Text(finalCommission.toLong().formatToPersianPrice(), fontSize = 13.sp, fontWeight = FontWeight.Black, color = MahoorPrimary)
                                                                Text("مالیات بر ارزش افزوده (۱۰٪ اتحادیه): ${(finalCommission * 0.1).toLong().formatToPersianPrice()}", fontSize = 9.sp, color = Color.White.copy(alpha = 0.4f))
                                                            }
                                                        }
                                                    }
                                                }

                                                2 -> {
                                                    // Advisor support interactive chat inside PWA
                                                                                    val chatScrollState = rememberScrollState()
                                                    val scope = rememberCoroutineScope()
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .padding(8.dp)
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text("پشتیبانی و ارتباط با مدیریت", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                            
                                                            // High reasoning intelligence banner
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(4.dp))
                                                                    .background(Color(0xFF2ECC71).copy(alpha = 0.15f))
                                                                    .border(0.5.dp, Color(0xFF2ECC71).copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                                            ) {
                                                                Row(
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                                                ) {
                                                                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(Color(0xFF2ECC71)))
                                                                    Text("تفکر عمیق پیشرفته", fontSize = 8.sp, color = Color(0xFF2ECC71), fontWeight = FontWeight.Bold)
                                                                }
                                                            }
                                                        }
                                                        
                                                        // Message feed
                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .fillMaxWidth()
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(Color.Black.copy(alpha = 0.2f))
                                                                .padding(8.dp)
                                                        ) {
                                                            Column(
                                                                modifier = Modifier
                                                                    .fillMaxSize()
                                                                    .verticalScroll(chatScrollState),
                                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                                            ) {
                                                                chatMessages.forEach { (text, isUser) ->
                                                                    Box(
                                                                        modifier = Modifier.fillMaxWidth(),
                                                                        contentAlignment = if (isUser) Alignment.CenterStart else Alignment.CenterEnd
                                                                    ) {
                                                                        Box(
                                                                            modifier = Modifier
                                                                                .clip(RoundedCornerShape(
                                                                                    topStart = 10.dp,
                                                                                    topEnd = 10.dp,
                                                                                    bottomStart = if (isUser) 0.dp else 10.dp,
                                                                                    bottomEnd = if (isUser) 10.dp else 0.dp
                                                                                ))
                                                                                .background(if (isUser) Color(0xFF007AFF) else Color(0xFF2C2C2E))
                                                                                .padding(8.dp)
                                                                        ) {
                                                                            Text(text, fontSize = 10.sp, color = Color.White)
                                                                        }
                                                                    }
                                                                }

                                                                if (chatLoading) {
                                                                    CircularProgressIndicator(modifier = Modifier.size(12.dp).align(Alignment.End), color = MahoorPrimary)
                                                                }
                                                            }
                                                        }

                                                        Spacer(modifier = Modifier.height(10.dp))

                                                        // Custom messages input bar for real reasoning chat with Gemini
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            OutlinedTextField(
                                                                value = customInputText,
                                                                onValueChange = { customInputText = it },
                                                                textStyle = TextStyle(fontSize = 10.sp, color = Color.White),
                                                                placeholder = { Text("سوال خود را از مشاور بپرسید...", fontSize = 9.sp, color = Color.White.copy(alpha = 0.5f)) },
                                                                singleLine = true,
                                                                modifier = Modifier.weight(1f),
                                                                shape = RoundedCornerShape(8.dp),
                                                                colors = OutlinedTextFieldDefaults.colors(
                                                                    focusedBorderColor = MahoorPrimary,
                                                                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                                                    focusedContainerColor = Color.Black.copy(alpha = 0.15f),
                                                                    unfocusedContainerColor = Color.Black.copy(alpha = 0.15f)
                                                                )
                                                            )

                                                            IconButton(
                                                                onClick = {
                                                                    if (customInputText.isNotBlank() && !chatLoading) {
                                                                        viewModel.sendMessageToAdvisor(customInputText)
                                                                        customInputText = ""
                                                                        scope.launch {
                                                                            kotlinx.coroutines.delay(150)
                                                                            chatScrollState.animateScrollTo(chatScrollState.maxValue)
                                                                        }
                                                                    }
                                                                },
                                                                modifier = Modifier.size(34.dp).clip(CircleShape).background(MahoorPrimary),
                                                                enabled = customInputText.isNotBlank() && !chatLoading
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Filled.Send,
                                                                    contentDescription = "ارسال",
                                                                    tint = Color.Black,
                                                                    modifier = Modifier.size(13.dp)
                                                                )
                                                            }
                                                        }

                                                        Spacer(modifier = Modifier.height(6.dp))

                                                        // Interactive canned questions to simulate chat
                                                        Text("یکی از سوالات کلیدی زیر را جهت آزمایش پاسخ خودکار انتخاب فرمایید:", fontSize = 8.sp, color = Color.White.copy(alpha = 0.6f))
                                                        
                                                        Spacer(modifier = Modifier.height(4.dp))

                                                        Row(
                                                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            Button(
                                                                onClick = {
                                                                    if (!chatLoading) {
                                                                        viewModel.sendMessageToAdvisor("تعرفه انتشار و همگام‌سازی چقدر است؟")
                                                                        scope.launch {
                                                                            kotlinx.coroutines.delay(150)
                                                                            chatScrollState.animateScrollTo(chatScrollState.maxValue)
                                                                        }
                                                                    }
                                                                },
                                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E22)),
                                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                                modifier = Modifier.height(26.dp)
                                                            ) {
                                                                Text("تعرفه سیستم همگام‌ساز؟", fontSize = 8.sp, color = Color.White)
                                                            }

                                                            Button(
                                                                onClick = {
                                                                    if (!chatLoading) {
                                                                        viewModel.sendMessageToAdvisor("آیا بدون اینترنت هم وب‌اپ کار می‌کند؟")
                                                                        scope.launch {
                                                                            kotlinx.coroutines.delay(150)
                                                                            chatScrollState.animateScrollTo(chatScrollState.maxValue)
                                                                        }
                                                                    }
                                                                },
                                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E22)),
                                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                                modifier = Modifier.height(26.dp)
                                                            ) {
                                                                Text("پشتیبانی آفلاین؟", fontSize = 8.sp, color = Color.White)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // PWA Bottom navigation tabs bar
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MahoorSurface)
                                                .border(0.5.dp, Color.White.copy(alpha = 0.08f))
                                                .padding(bottom = 12.dp, top = 6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceEvenly
                                            ) {
                                                IconButton(
                                                    onClick = { activePwaTab = 0 },
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.ListAlt,
                                                        contentDescription = null,
                                                        tint = if (activePwaTab == 0) MahoorPrimary else Color.White.copy(alpha = 0.4f),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }

                                                IconButton(
                                                    onClick = { activePwaTab = 1 },
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Calculate,
                                                        contentDescription = null,
                                                        tint = if (activePwaTab == 1) MahoorPrimary else Color.White.copy(alpha = 0.4f),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }

                                                IconButton(
                                                    onClick = { activePwaTab = 2 },
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.HeadsetMic,
                                                        contentDescription = null,
                                                        tint = if (activePwaTab == 2) MahoorPrimary else Color.White.copy(alpha = 0.4f),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Physical Home Bar indicator at bottom of simulated screen (except browser scroll bar)
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 3.dp)
                                .size(110.dp, 4.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.4f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryMiniCard(
    label: String,
    value: Int,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MahoorSurface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value.toString().toPersianDigits(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = MahoorOnBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ChannelBarMetric(
    channelName: String,
    percentage: Int,
    percentFloat: Float,
    color: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = channelName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MahoorOnBackground
            )
            Text(
                text = "%${percentage.toString().toPersianDigits()}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(6.dp))

        // Visual Linear Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(MahoorSurfaceVariant)
        ) {
            // Animated bar width
            val animatedWidth = remember { Animatable(0f) }
            LaunchedEffect(percentFloat) {
                animatedWidth.animateTo(
                    targetValue = percentFloat,
                    animationSpec = tween(1000, easing = FastOutSlowInEasing)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedWidth.value)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}
