package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
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

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = RealEstateRepository(database.realEstateDao(), database.credentialDao())

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
                title = "آپارتمان مدرن ۳ خوابه نیاوران",
                description = "سازه‌ای لوکس و بی‌نظیر اثر آرشیتکت مطرح منطقه. پلان تفکیکی، متریال تماماً برند، آشپزخانه مجهز با کابینت‌های اتریشی، مشاعات هتلینگ شامل استخر، سونا، جکوزی فعال و روف‌گاردن ۴ فصل به همراه سالن اجتماعات مجلل و لابی‌من ۲۴ ساعته.",
                price = 28000000000, // 28 billion Toman
                type = "فروش مسکونی",
                location = "تهران، نیاوران",
                areaSize = 185,
                rooms = 3,
                publishToDivar = true,
                publishToSheypoor = true,
                publishToMahoor = true,
                divarId = "DIV-839218",
                sheypoorId = "SHY-773489",
                views = 142,
                clicks = 38,
                leads = 4,
                isActive = true
            )

            val sample2 = RealEstateAd(
                id = 2,
                title = "رهن و اجاره ویلای مدرن کلاردشت",
                description = "ویلای مدرن دوبلکس با چشم‌انداز خیره‌کننده کوهستان و جنگل. ۵۰۰ متر زمین، ۲۵۰ متر بنای مهندسی‌ساز، تراس بزرگ رو به طبیعت بکر، متریال عالی، امنیت فوق‌العاده در شهرکی برند همراه با نگهبانی.",
                price = 150000000, // 150 million Toman deposit (with monthly lease)
                type = "رهن و اجاره",
                location = "مازندران، کلاردشت",
                areaSize = 250,
                rooms = 4,
                publishToDivar = true,
                publishToSheypoor = false,
                publishToMahoor = true,
                divarId = "DIV-228392",
                sheypoorId = null,
                views = 93,
                clicks = 19,
                leads = 2,
                isActive = true
            )

            val sample3 = RealEstateAd(
                id = 3,
                title = "دفتر اداری موقعیت بی‌نظیر جردن",
                description = "واحد اداری سند رسمی در بهترین فرعی منطقه جردن. بازسازی صفر تا صد شده، اتاق مدیریت بزرگ، پارکینگ همکف، آسانسور، دسترسی استثنایی به بزرگراه‌ها و وسایل حمل و نقل عمومی.",
                price = 11500000000, // 11.5 billion Toman
                type = "تجاری و اداری",
                location = "تهران، جردن",
                areaSize = 95,
                rooms = 2,
                publishToDivar = false,
                publishToSheypoor = true,
                publishToMahoor = true,
                divarId = null,
                sheypoorId = "SHY-102948",
                views = 64,
                clicks = 11,
                leads = 1,
                isActive = false
            )

            database.realEstateDao().insertAd(sample1)
            database.realEstateDao().insertAd(sample2)
            database.realEstateDao().insertAd(sample3)
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
        publishMahoor: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isSyncing.value = true
            val ad = RealEstateAd(
                title = title,
                description = description,
                price = price,
                type = type,
                location = location,
                areaSize = areaSize,
                rooms = rooms,
                publishToDivar = publishDivar,
                publishToSheypoor = publishSheypoor,
                publishToMahoor = publishMahoor,
                isActive = true
            )
            repository.publishAd(ad)
            _isSyncing.value = false
        }
    }

    fun updateAd(ad: RealEstateAd) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateAd(ad)
        }
    }

    fun toggleAdStatus(ad: RealEstateAd) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateAdStatus(ad.id, !ad.isActive)
        }
    }

    fun deleteAd(ad: RealEstateAd) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAd(ad)
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
