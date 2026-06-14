package com.example.data.local

import androidx.room.*
import com.example.data.model.ChannelCredential
import com.example.data.model.RealEstateAd
import kotlinx.coroutines.flow.Flow

@Dao
interface RealEstateDao {
    @Query("SELECT * FROM real_estate_ads ORDER BY timestamp DESC")
    fun getAllAds(): Flow<List<RealEstateAd>>

    @Query("SELECT * FROM real_estate_ads WHERE id = :id")
    suspend fun getAdById(id: Int): RealEstateAd?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAd(ad: RealEstateAd): Long

    @Update
    suspend fun updateAd(ad: RealEstateAd)

    @Delete
    suspend fun deleteAd(ad: RealEstateAd)

    @Query("UPDATE real_estate_ads SET views = :views, clicks = :clicks, leads = :leads WHERE id = :id")
    suspend fun updateStats(id: Int, views: Int, clicks: Int, leads: Int)

    @Query("UPDATE real_estate_ads SET isActive = :isActive WHERE id = :id")
    suspend fun updateStatus(id: Int, isActive: Boolean)
}

@Dao
interface CredentialDao {
    @Query("SELECT * FROM channel_credentials")
    fun getAllCredentials(): Flow<List<ChannelCredential>>

    @Query("SELECT * FROM channel_credentials WHERE channelName = :channelName")
    suspend fun getCredentialByChannel(channelName: String): ChannelCredential?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCredential(credential: ChannelCredential)

    @Query("UPDATE channel_credentials SET syncStatus = :status WHERE channelName = :channelName")
    suspend fun updateSyncStatus(channelName: String, status: String)

    @Query("UPDATE channel_credentials SET isEnabled = :isEnabled WHERE channelName = :channelName")
    suspend fun updateChannelEnabled(channelName: String, isEnabled: Boolean)
}
