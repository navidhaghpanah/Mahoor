package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "real_estate_ads")
data class RealEstateAd(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val price: Long,
    val type: String, // e.g., "فروش مسکونی", "رهن و اجاره", "خرید/فروش زمین"
    val location: String, // e.g., "نیاوران، تهران"
    val areaSize: Int, // Square meters
    val rooms: Int, // Number of rooms
    val imageUrl: String? = null,
    val publishToDivar: Boolean = true,
    val publishToSheypoor: Boolean = true,
    val publishToMahoor: Boolean = true,
    val divarId: String? = null, // Simulated Ad ID from Divar API
    val sheypoorId: String? = null, // Simulated Ad ID from Sheypoor API
    val views: Int = 0,
    val clicks: Int = 0,
    val leads: Int = 0, // Calls/contacts
    val timestamp: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

@Entity(tableName = "channel_credentials")
data class ChannelCredential(
    @PrimaryKey val channelName: String, // "divar" or "sheypoor" or "mahoor"
    val isEnabled: Boolean = true,
    val apiKey: String = "",
    val phoneNumber: String = "",
    val syncStatus: String = "متصل" // "متصل", "غیرفعال", "خطای اتصال"
)
