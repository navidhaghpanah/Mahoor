package com.example.data.repository

import com.example.data.local.AgentProfileDao
import com.example.data.local.CredentialDao
import com.example.data.local.RealEstateDao
import com.example.data.model.AgentProfile
import com.example.data.model.ChannelCredential
import com.example.data.model.RealEstateAd
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import java.io.IOException
import kotlin.random.Random

class RealEstateRepository(
    private val realEstateDao: RealEstateDao,
    private val credentialDao: CredentialDao,
    private val agentProfileDao: AgentProfileDao
) {
    val allAds: Flow<List<RealEstateAd>> = realEstateDao.getAllAds()
    val allCredentials: Flow<List<ChannelCredential>> = credentialDao.getAllCredentials()
    val agentProfile: Flow<AgentProfile?> = agentProfileDao.getProfileFlow()

    suspend fun getAdById(id: Int): RealEstateAd? = realEstateDao.getAdById(id)

    suspend fun saveProfile(profile: AgentProfile) {
        agentProfileDao.insertOrUpdateProfile(profile)
    }

    // Simulates publishing an ad across several portals
    suspend fun publishAd(ad: RealEstateAd): Long {
        if (!ad.isManagerApproved) {
            val initialAd = ad.copy(
                publishStatus = "در انتظار تایید",
                divarId = null,
                sheypoorId = null
            )
            return realEstateDao.insertAd(initialAd)
        }

        // 1. First insert into SQLite with initial "در حال بررسی" state to let user see it immediately
        val initialAd = ad.copy(
            publishStatus = "در حال بررسی",
            divarId = null,
            sheypoorId = null
        )
        val insertedId = realEstateDao.insertAd(initialAd)

        // 2. Perform live network simulation delays
        delay(1800)

        // 3. Determine if sync succeeds (15% random failure simulate, or if none are selected)
        val hasSelection = ad.publishToDivar || ad.publishToSheypoor || ad.publishToMahoor
        val isSuccessful = hasSelection && (Random.nextFloat() > 0.15f)

        val divarId = if (isSuccessful && ad.publishToDivar) "DIV-${Random.nextInt(100000, 999999)}" else null
        val sheypoorId = if (isSuccessful && ad.publishToSheypoor) "SHY-${Random.nextInt(100000, 999999)}" else null
        val finalStatus = if (isSuccessful) "منتشر شده" else "خطا در ارسال"

        val finalizedAd = initialAd.copy(
            id = insertedId.toInt(),
            divarId = divarId,
            sheypoorId = sheypoorId,
            views = if (isSuccessful && ad.isActive) Random.nextInt(4, 10) else 0,
            clicks = if (isSuccessful && ad.isActive) Random.nextInt(1, 3) else 0,
            publishStatus = finalStatus
        )
        realEstateDao.updateAd(finalizedAd)
        return insertedId
    }

    suspend fun approveAd(ad: RealEstateAd) {
        val approvedAd = ad.copy(
            isManagerApproved = true,
            publishStatus = "در حال بررسی"
        )
        realEstateDao.updateAd(approvedAd)

        // Perform live network simulation delays for Divar/Sheypoor
        delay(1600)

        val isSuccessful = Random.nextFloat() > 0.10f // 90% success on manager approved
        val divarId = if (isSuccessful && ad.publishToDivar) "DIV-${Random.nextInt(100000, 999999)}" else null
        val sheypoorId = if (isSuccessful && ad.publishToSheypoor) "SHY-${Random.nextInt(100000, 999999)}" else null
        val finalStatus = if (isSuccessful) "منتشر شده" else "خطا در ارسال"

        realEstateDao.updateAd(approvedAd.copy(
            divarId = divarId,
            sheypoorId = sheypoorId,
            publishStatus = finalStatus,
            views = if (isSuccessful) Random.nextInt(5, 12) else 0,
            clicks = if (isSuccessful) Random.nextInt(1, 4) else 0
        ))
    }

    suspend fun updateAd(ad: RealEstateAd) {
        val divarId = if (ad.publishToDivar && ad.divarId == null) "DIV-${Random.nextInt(100000, 999999)}" else if (ad.publishToDivar) ad.divarId else null
        val sheypoorId = if (ad.publishToSheypoor && ad.sheypoorId == null) "SHY-${Random.nextInt(100000, 999999)}" else if (ad.publishToSheypoor) ad.sheypoorId else null
        
        // Reset status to "منتشر شده" upon explicit edit
        val hasSelection = ad.publishToDivar || ad.publishToSheypoor || ad.publishToMahoor
        val status = if (hasSelection) "منتشر شده" else "خطا در ارسال"
        
        realEstateDao.updateAd(ad.copy(divarId = divarId, sheypoorId = sheypoorId, publishStatus = status))
    }

    suspend fun deleteAd(ad: RealEstateAd) {
        realEstateDao.deleteAd(ad)
    }

    suspend fun updateAdStatus(id: Int, isActive: Boolean) {
        realEstateDao.updateStatus(id, isActive)
    }

    suspend fun updateAdStats(id: Int, views: Int, clicks: Int, leads: Int) {
        realEstateDao.updateStats(id, views, clicks, leads)
    }

    // High performance simulations: generate real-time interaction updates
    suspend fun simulateRealTimeTraffic(adsList: List<RealEstateAd>) {
        if (adsList.isEmpty()) return
        
        // Randomly choose 1 to 2 ads and bump views, clicks or leads
        val activeAds = adsList.filter { it.isActive }
        if (activeAds.isEmpty()) return

        val adToUpdate = activeAds.random()
        val newViews = adToUpdate.views + Random.nextInt(1, 4)
        val newClicks = adToUpdate.clicks + if (Random.nextFloat() > 0.4f) Random.nextInt(0, 2) else 0
        val newLeads = adToUpdate.leads + if (Random.nextFloat() > 0.85f) Random.nextInt(0, 1) else 0

        realEstateDao.updateStats(adToUpdate.id, newViews, newClicks, newLeads)
    }

    // Credentials logic
    suspend fun saveCredential(credential: ChannelCredential) {
        credentialDao.insertCredential(credential)
    }

    suspend fun updateSyncStatus(channelName: String, status: String) {
        credentialDao.updateSyncStatus(channelName, status)
    }

    suspend fun updateChannelEnabled(channelName: String, isEnabled: Boolean) {
        credentialDao.updateChannelEnabled(channelName, isEnabled)
    }

    suspend fun saveAdLocally(ad: RealEstateAd) {
        realEstateDao.insertAd(ad)
    }

    // Prepopulate some default platforms, keys and properties to offer an amazing out-of-the-box user experience
    suspend fun initializeDefaults() {
        val divarCreds = credentialDao.getCredentialByChannel("divar")
        if (divarCreds == null) {
            credentialDao.insertCredential(
                ChannelCredential(
                    channelName = "divar",
                    isEnabled = true,
                    apiKey = "divar_token_live_mahoor_99x81",
                    phoneNumber = "09111134767",
                    syncStatus = "متصل"
                )
            )
        }

        val sheypoorCreds = credentialDao.getCredentialByChannel("sheypoor")
        if (sheypoorCreds == null) {
            credentialDao.insertCredential(
                ChannelCredential(
                    channelName = "sheypoor",
                    isEnabled = true,
                    apiKey = "sheypoor_key_auth_44a218_mahoor",
                    phoneNumber = "09111134767",
                    syncStatus = "متصل"
                )
            )
        }

        val mahoorCreds = credentialDao.getCredentialByChannel("mahoor")
        if (mahoorCreds == null) {
            credentialDao.insertCredential(
                ChannelCredential(
                    channelName = "mahoor",
                    isEnabled = true,
                    apiKey = "mahoor_portal_key_internal",
                    phoneNumber = "09111134767",
                    syncStatus = "متصل"
                )
            )
        }

        val arvanCreds = credentialDao.getCredentialByChannel("arvan_mysql")
        if (arvanCreds == null) {
            credentialDao.insertCredential(
                ChannelCredential(
                    channelName = "arvan_mysql",
                    isEnabled = true,
                    apiKey = "host=3da6f77c410e4c00b2b8c85eed95cd72.db.arvandbaas.ir;port=3306;pass=Xb0E_KKsTU3n_L#W3kNMHeE1",
                    phoneNumber = "base-user",
                    syncStatus = "متصل"
                )
            )
        }

        val apifyCreds = credentialDao.getCredentialByChannel("apify_divar")
        if (apifyCreds == null) {
            credentialDao.insertCredential(
                ChannelCredential(
                    channelName = "apify_divar",
                    isEnabled = true,
                    apiKey = "https://apify.com/conspiratorial_quantum/divar-real-state/api",
                    phoneNumber = "apify-actor",
                    syncStatus = "متصل"
                )
            )
        }

        val apifySheypoorCreds = credentialDao.getCredentialByChannel("apify_sheypoor")
        if (apifySheypoorCreds == null) {
            credentialDao.insertCredential(
                ChannelCredential(
                    channelName = "apify_sheypoor",
                    isEnabled = true,
                    apiKey = "https://apify.com/web_scraper/sheypoor-real-estate/api",
                    phoneNumber = "apify-sheypoor-actor",
                    syncStatus = "متصل"
                )
            )
        }

        // Unconditionally set or update default profile to ensure user-specified details are refreshed
        agentProfileDao.insertOrUpdateProfile(
            AgentProfile(
                id = 1,
                fullName = "محمد مهدی آزاد (مدیر ارشد املاک ماهور)",
                agencyName = "مجموعه تخصصی املاک ماهور",
                licenseNumber = "م-۱۵۹۸",
                phoneNumber = "۰۹۱۱۳۲۷۶۶۴۷",
                email = "info@mahoorrlste.ir",
                agencyAddress = "محمودآباد، خیابان امام، بعد از نسیم ۶۹/۱ — روبروی پارکینگ قزوینی‌پور",
                currentPlan = "سرویس ویژه پلاتینیوم (کنترل و ارزیابی کل)",
                planExpiryDate = "۱۴۰۷/۱۲/۲۹",
                adsLimitRemaining = 120,
                totalAdsAllowed = 150,
                directSyncLimitRemaining = 950,
                totalDirectSyncLimit = 1000
            )
        )
    }
}
