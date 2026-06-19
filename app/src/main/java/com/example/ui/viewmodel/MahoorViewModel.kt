package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AgentProfile
import com.example.data.model.ChannelCredential
import com.example.data.model.RealEstateAd
import com.example.data.repository.RealEstateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

class MahoorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RealEstateRepository
    val uiState: StateFlow<List<RealEstateAd>>
    val credentials: StateFlow<List<ChannelCredential>>
    val agentProfile: StateFlow<AgentProfile?>

    private val firestoreRepository = com.example.data.repository.FirestoreRepository.getInstance(application)
    
    private val _firestoreInitialized = MutableStateFlow(firestoreRepository.isInitialized)
    val firestoreInitialized = _firestoreInitialized.asStateFlow()

    private val _isFirestoreSyncRunning = MutableStateFlow(false)
    val isFirestoreSyncRunning = _isFirestoreSyncRunning.asStateFlow()

    private val _firestoreSyncStatusMsg = MutableStateFlow("سامانه ابری فایربیس فعال است")
    val firestoreSyncStatusMsg = _firestoreSyncStatusMsg.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    private val _advisorChatMessages = MutableStateFlow<List<Pair<String, Boolean>>>(
        listOf(
            "سلام! به سامانه پشتیبانی اختصاصی و مشاور هوشمند ارشد املاک ماهور خوش آمدید." to false,
            "هر سوالی در مورد قیمت‌گذاری، همگام‌سازی با دیوار و شیپور، محاسبات قانونی کمیسیون یا قوانین معاملات ملکی دارید بپرسید؛ با قدرت پردازش فوق پیشرفته (Thinking Cloud) در خدمت شما هستم." to false
        )
    )
    val advisorChatMessages = _advisorChatMessages.asStateFlow()

    private val _advisorChatLoading = MutableStateFlow(false)
    val advisorChatLoading = _advisorChatLoading.asStateFlow()

    fun sendMessageToAdvisor(message: String) {
        viewModelScope.launch {
            if (message.isBlank()) return@launch
            
            // Add user message to list
            val current = _advisorChatMessages.value.toMutableList()
            current.add(message to true)
            _advisorChatMessages.value = current
            _advisorChatLoading.value = true

            // Launch AI generation
            val response = com.example.data.repository.GeminiRepository.getAdvisorResponse(
                userMessage = message,
                conversationHistory = current.drop(2) // Drop introductory messages to keep context concise
            )

            val updated = _advisorChatMessages.value.toMutableList()
            updated.add(response to false)
            _advisorChatMessages.value = updated
            _advisorChatLoading.value = false
        }
    }

    init {
        val database = AppDatabase.getDatabase(application)
        repository = RealEstateRepository(
            database.realEstateDao(),
            database.credentialDao(),
            database.agentProfileDao()
        )

        uiState = repository.allAds.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        credentials = repository.allCredentials.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        agentProfile = repository.agentProfile.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        // Initialize defaults in database
        viewModelScope.launch(Dispatchers.IO) {
            repository.initializeDefaults()
            prepopulateDefaultAdsIfNeeded()
            startRealTimeTrafficSimulation()
        }
    }

    private suspend fun prepopulateDefaultAdsIfNeeded() {
        // Collect current list once or verify length
        // We'll insert a couple if none exist to make the dashboard stunning out of the box
        val database = AppDatabase.getDatabase(getApplication())
        val count = database.realEstateDao().getAdById(1)
        
        // Just checking if any ads exist database-side
        // To be safe, we can inspect a test query or check first item
        // Let's do a fast query or wait for flow. For safety, we can do list inspection:
        val existing = database.realEstateDao().getAdById(1)
        if (existing == null) {
            val sample1 = RealEstateAd(
                id = 1,
                title = "آپارتمان نوساز ۳ خوابه محمودآباد",
                description = "آپارتمان نوساز ۱۲۰ متری، ۳ خوابه همراه با پارکینگ اختصاصی و متریال درجه یک. سازه‌ای شیک و مدرن در بهترین نقطه محمودآباد نزدیک خیابان امام با دسترسی بیداد.",
                price = 2500000000L, // 2.5 billion Toman
                type = "فروش مسکونی",
                location = "محمودآباد، خیابان امام",
                areaSize = 120,
                rooms = 3,
                publishToDivar = true,
                publishToSheypoor = true,
                publishToMahoor = true,
                divarId = "DIV-839218",
                sheypoorId = "SHY-773489",
                views = 142,
                clicks = 38,
                leads = 4,
                isActive = true,
                publishStatus = "منتشر شده",
                advisorName = "آقای راعی",
                isManagerApproved = true
            )

            val sample2 = RealEstateAd(
                id = 2,
                title = "ویلای دوبلکس ساحلی محمودآباد",
                description = "ویلای دوبلکس ساحلی مدرن با ۲۰۰ متر زمین و ۱۸۰ متر بنای عالی، ۴ خواب مستر و حیاط بزرگ شیک رو به جنگل و ساحل در منطقه زیبای نسیم.",
                price = 5800000000L, // 5.8 billion Toman
                type = "فروش مسکونی",
                location = "محمودآباد، نسیم",
                areaSize = 180,
                rooms = 4,
                publishToDivar = true,
                publishToSheypoor = true,
                publishToMahoor = true,
                divarId = null,
                sheypoorId = null,
                views = 12,
                clicks = 3,
                leads = 1,
                isActive = true,
                publishStatus = "در انتظار تایید",
                advisorName = "خانم حیدری",
                isManagerApproved = false
            )

            val sample3 = RealEstateAd(
                id = 3,
                title = "آپارتمان ۸۵ متری مبله شیک",
                description = "واحد ۸۵ متری، ۲ خواب کاملاً مبله با دکوراسیون و تجهیزات کامل لوکس نزدیک خیابان ساحل محمودآباد با پارکینگ اختصاصی.",
                price = 5000000, // 5 million Toman lease
                type = "رهن و اجاره",
                location = "محمودآباد، خیابان ساحل",
                areaSize = 85,
                rooms = 2,
                publishToDivar = false,
                publishToSheypoor = true,
                publishToMahoor = true,
                divarId = null,
                sheypoorId = null,
                views = 0,
                clicks = 0,
                leads = 0,
                isActive = false,
                publishStatus = "خطا در ارسال",
                advisorName = "خانم حیدری",
                isManagerApproved = true
            )

            val sample4 = RealEstateAd(
                id = 4,
                title = "زمین ساحلی الحاق به بافت",
                description = "زمین ۴۵۰ متری خوش قواره، دور دیوار کشی شده با سند تک برگ آماده انتقال، انشعابات روی زمین در محدوده جنگلی و ساحلی شهر محمودآباد.",
                price = 9000000000L, // 9 billion Toman
                type = "خرید/فروش زمین",
                location = "محمودآباد، بلوار معلم",
                areaSize = 450,
                rooms = 0,
                publishToDivar = true,
                publishToSheypoor = true,
                publishToMahoor = true,
                divarId = null,
                sheypoorId = null,
                views = 4,
                clicks = 1,
                leads = 0,
                isActive = true,
                publishStatus = "در انتظار تایید",
                advisorName = "آقای راعی",
                isManagerApproved = false
            )

            val sample5 = RealEstateAd(
                id = 5,
                title = "ویلا مدرن استخردار در ایزدشهر",
                description = "ویلای ۳۵۰ متری دوبلکس، نمای سنگ رومی، جکوزی و استخر بزرگ ۴ فصل فعال، طراحی داخلی استثنایی و دکوراتیو در محمودآباد مرز ایزدشهر.",
                price = 14500000000L, // 14.5 billion Toman
                type = "فروش مسکونی",
                location = "محمودآباد، ایزدشهر",
                areaSize = 350,
                rooms = 5,
                publishToDivar = true,
                publishToSheypoor = false,
                publishToMahoor = true,
                divarId = "DIV-294012",
                sheypoorId = null,
                views = 98,
                clicks = 24,
                leads = 3,
                isActive = true,
                publishStatus = "منتشر شده",
                advisorName = "خانم حیدری",
                isManagerApproved = true
            )

            database.realEstateDao().insertAd(sample1)
            database.realEstateDao().insertAd(sample2)
            database.realEstateDao().insertAd(sample3)
            database.realEstateDao().insertAd(sample4)
            database.realEstateDao().insertAd(sample5)
        }
    }

    private fun startRealTimeTrafficSimulation() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                delay(Random.nextLong(4000, 7000)) // Every 4 to 7 seconds
                val currentAds = uiState.value
                if (currentAds.isNotEmpty()) {
                    repository.simulateRealTimeTraffic(currentAds)
                }
            }
        }
    }

    fun insertAd(
        title: String,
        description: String,
        price: Long,
        type: String,
        location: String,
        areaSize: Int,
        rooms: Int,
        publishDivar: Boolean,
        publishSheypoor: Boolean,
        publishMahoor: Boolean,
        imageUrl: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isSyncing.value = true
            val currentAgent = agentProfile.value
            val isApproved = currentAgent?.isManager() == true
            val advisor = currentAgent?.fullName ?: "خانم حیدری"

            val ad = RealEstateAd(
                title = title,
                description = description,
                price = price,
                type = type,
                location = location,
                areaSize = areaSize,
                rooms = rooms,
                imageUrl = imageUrl,
                publishToDivar = publishDivar,
                publishToSheypoor = publishSheypoor,
                publishToMahoor = publishMahoor,
                isActive = true,
                advisorName = advisor,
                isManagerApproved = isApproved
            )
            val insertedId = repository.publishAd(ad)
            val updatedAd = repository.getAdById(insertedId.toInt())
            if (updatedAd != null) {
                firestoreRepository.saveAd(updatedAd)
            }
            _isSyncing.value = false
        }
    }

    fun approveAd(ad: RealEstateAd) {
        viewModelScope.launch(Dispatchers.IO) {
            _isSyncing.value = true
            repository.approveAd(ad)
            val updatedAd = repository.getAdById(ad.id)
            if (updatedAd != null) {
                firestoreRepository.saveAd(updatedAd)
            }
            _isSyncing.value = false
        }
    }

    fun updateAd(ad: RealEstateAd) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateAd(ad)
            val updatedAd = repository.getAdById(ad.id)
            if (updatedAd != null) {
                firestoreRepository.saveAd(updatedAd)
            }
        }
    }

    fun toggleAdStatus(ad: RealEstateAd) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateAdStatus(ad.id, !ad.isActive)
            val updatedAd = repository.getAdById(ad.id)
            if (updatedAd != null) {
                firestoreRepository.saveAd(updatedAd)
            }
        }
    }

    fun deleteAd(ad: RealEstateAd) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAd(ad)
            firestoreRepository.deleteAd(ad.id)
        }
    }

    fun syncAllWithFirestore() {
        viewModelScope.launch(Dispatchers.IO) {
            _isFirestoreSyncRunning.value = true
            _firestoreSyncStatusMsg.value = "درحال ارسال آگهی‌های محلی به فایربیس..."
            val localAds = uiState.value
            var uploadCount = 0
            localAds.forEach { ad ->
                val success = firestoreRepository.saveAd(ad)
                if (success) {
                    uploadCount++
                }
                delay(100)
            }

            _firestoreSyncStatusMsg.value = "درحال دریافت آگهی‌های ابری جدید..."
            var downloadCount = 0
            try {
                val remoteAds = firestoreRepository.getAllAds()
                remoteAds.forEach { remoteAd ->
                    val localAd = repository.getAdById(remoteAd.id)
                    if (localAd == null) {
                        repository.saveAdLocally(remoteAd)
                        downloadCount++
                    } else if (remoteAd.timestamp > localAd.timestamp || 
                               remoteAd.views != localAd.views || 
                               remoteAd.clicks != localAd.clicks || 
                               remoteAd.leads != localAd.leads ||
                               remoteAd.isActive != localAd.isActive ||
                               remoteAd.publishStatus != localAd.publishStatus) {
                        repository.saveAdLocally(remoteAd)
                        downloadCount++
                    }
                }
            } catch (e: Exception) {
                Log.e("MahoorViewModel", "Error downloading Firestore ads during sync", e)
            }

            _isFirestoreSyncRunning.value = false
            _firestoreInitialized.value = firestoreRepository.isInitialized
            if (firestoreRepository.isInitialized) {
                _firestoreSyncStatusMsg.value = "همگام‌سازی موفق: $uploadCount ارسال شد، $downloadCount آگهی جدید دریافت شد"
            } else {
                _firestoreSyncStatusMsg.value = "خطا در اتصال به فایربیس"
            }
        }
    }

    fun updateChannelCredentials(channel: String, enabled: Boolean, apiKey: String, phone: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val status = if (enabled) "متصل" else "غیرفعال"
            repository.saveCredential(
                ChannelCredential(
                    channelName = channel,
                    isEnabled = enabled,
                    apiKey = apiKey,
                    phoneNumber = phone,
                    syncStatus = status
                )
            )
        }
    }

    fun updateAgentProfile(profile: AgentProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveProfile(profile)
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MahoorViewModel::class.java)) {
                return MahoorViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
