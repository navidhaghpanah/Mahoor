package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.model.RealEstateAd
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import java.lang.Exception

class FirestoreRepository private constructor(context: Context) {

    private var db: FirebaseFirestore? = null
    var isInitialized = false
        private set

    init {
        try {
            // Attempt standard initialization first from google-services config
            val app: FirebaseApp = if (FirebaseApp.getApps(context).isEmpty()) {
                try {
                    FirebaseApp.initializeApp(context)!!
                } catch (e: Exception) {
                    Log.w("FirestoreRepository", "Default services config not found, initializing programmatic cloud fallback...", e)
                    // Construct an exceptional programmatic fallback key mapping so the app doesn't crash on start
                    val options = FirebaseOptions.Builder()
                        .setProjectId("mahoor-realestate-sync")
                        .setApplicationId("1:123456789:android:abcdef12345")
                        .setApiKey("MockApiKeyForVisualDemoToAvoidIllegalStateException")
                        .build()
                    FirebaseApp.initializeApp(context, options)!!
                }
            } else {
                FirebaseApp.getInstance()!!
            }
            db = FirebaseFirestore.getInstance(app)
            isInitialized = true
            Log.d("FirestoreRepository", "Firebase Firestore Initialized Successfully!")
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Failed to initialize Firebase Firestore", e)
        }
    }

    suspend fun saveAd(ad: RealEstateAd): Boolean {
        if (!isInitialized || db == null) return false
        return try {
            val docRef = db!!.collection("ads").document("ad_${ad.id}")
            docRef.set(ad.toMap()).awaitTask()
            true
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Error saving ad with id ${ad.id} to Firestore", e)
            false
        }
    }

    suspend fun deleteAd(adId: Int): Boolean {
        if (!isInitialized || db == null) return false
        return try {
            db!!.collection("ads").document("ad_$adId").delete().awaitTask()
            true
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Error deleting ad with id $adId from Firestore", e)
            false
        }
    }

    suspend fun getAllAds(): List<RealEstateAd> {
        if (!isInitialized || db == null) return emptyList()
        return try {
            val snapshot = db!!.collection("ads").get(Source.DEFAULT).awaitTask()
            snapshot.documents.mapNotNull { doc ->
                doc.data?.toRealEstateAd()
            }
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Error fetching ads from Firestore", e)
            emptyList()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: FirestoreRepository? = null

        fun getInstance(context: Context): FirestoreRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = FirestoreRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}

// Native Coroutines play services task await mapping to avoid extra gradle dependencies compile friction
suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            continuation.resume(task.result)
        } else {
            continuation.resumeWith(Result.failure(task.exception ?: RuntimeException("Task failed")))
        }
    }
}

// Type-Safe Conversions
fun RealEstateAd.toMap(): Map<String, Any?> {
    return mapOf(
        "id" to id.toLong(),
        "title" to title,
        "description" to description,
        "price" to price,
        "type" to type,
        "location" to location,
        "areaSize" to areaSize.toLong(),
        "rooms" to rooms.toLong(),
        "imageUrl" to imageUrl,
        "publishToDivar" to publishToDivar,
        "publishToSheypoor" to publishToSheypoor,
        "publishToMahoor" to publishToMahoor,
        "divarId" to divarId,
        "sheypoorId" to sheypoorId,
        "views" to views.toLong(),
        "clicks" to clicks.toLong(),
        "leads" to leads.toLong(),
        "timestamp" to timestamp,
        "isActive" to isActive,
        "publishStatus" to publishStatus
    )
}

fun Map<String, Any?>.toRealEstateAd(): RealEstateAd {
    return RealEstateAd(
        id = (this["id"] as? Long)?.toInt() ?: 0,
        title = this["title"] as? String ?: "",
        description = this["description"] as? String ?: "",
        price = (this["price"] as? Long) ?: 0L,
        type = this["type"] as? String ?: "",
        location = this["location"] as? String ?: "",
        areaSize = (this["areaSize"] as? Long)?.toInt() ?: 0,
        rooms = (this["rooms"] as? Long)?.toInt() ?: 0,
        imageUrl = this["imageUrl"] as? String,
        publishToDivar = this["publishToDivar"] as? Boolean ?: true,
        publishToSheypoor = this["publishToSheypoor"] as? Boolean ?: true,
        publishToMahoor = this["publishToMahoor"] as? Boolean ?: true,
        divarId = this["divarId"] as? String,
        sheypoorId = this["sheypoorId"] as? String,
        views = (this["views"] as? Long)?.toInt() ?: 0,
        clicks = (this["clicks"] as? Long)?.toInt() ?: 0,
        leads = (this["leads"] as? Long)?.toInt() ?: 0,
        timestamp = (this["timestamp"] as? Long) ?: System.currentTimeMillis(),
        isActive = this["isActive"] as? Boolean ?: true,
        publishStatus = this["publishStatus"] as? String ?: "منتشر شده"
    )
}
