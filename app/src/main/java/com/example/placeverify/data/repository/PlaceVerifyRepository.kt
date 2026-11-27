package com.example.placeverify.data.repository

import com.example.placeverify.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PlaceVerifyRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        var cachedProfile: UserProfile? = null
    }

    // Auth & Session
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    suspend fun getCurrentUserProfile(): UserProfile? {
        cachedProfile?.let { return it }
        val uid = auth.currentUser?.uid ?: return null
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            val profile = doc.toObject(UserProfile::class.java)?.copy(uid = uid)
            if (profile != null && profile.name.isNotBlank()) {
                cachedProfile = profile
                profile
            } else {
                val user = auth.currentUser
                val derivedName = user?.displayName?.ifBlank { null }
                    ?: user?.email?.substringBefore("@")?.replace(".", " ")?.replaceFirstChar { it.uppercase() }
                    ?: "User"
                val fallback = UserProfile(
                    uid = uid,
                    name = derivedName,
                    email = user?.email ?: "",
                    role = "STUDENT",
                    institution = "Campus Member"
                )
                cachedProfile = fallback
                fallback
            }
        } catch (e: Exception) {
            val user = auth.currentUser
            user?.let {
                val derivedName = it.displayName?.ifBlank { null }
                    ?: it.email?.substringBefore("@")?.replace(".", " ")?.replaceFirstChar { it.uppercase() }
                    ?: "User"
                val fallback = UserProfile(
                    uid = it.uid,
                    name = derivedName,
                    email = it.email ?: "",
                    role = "STUDENT",
                    institution = "Campus Member"
                )
                cachedProfile = fallback
                fallback
            }
        }
    }

    fun signOut() {
        cachedProfile = null
        auth.signOut()
    }

    suspend fun registerUser(name: String, email: String, pass: String, role: String, institution: String): Result<String> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val uid = authResult.user?.uid ?: System.currentTimeMillis().toString()
            val profile = UserProfile(
                uid = uid,
                name = name,
                email = email,
                role = role,
                institution = institution
            )
            val userData = mapOf(
                "uid" to uid,
                "name" to name,
                "email" to email,
                "role" to role,
                "institution" to institution,
                "createdAt" to System.currentTimeMillis()
            )
            firestore.collection("users").document(uid).set(userData).await()
            cachedProfile = profile
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, pass: String): Result<String> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, pass).await()
            val uid = authResult.user?.uid ?: ""
            cachedProfile = null
            getCurrentUserProfile() // Preload into memory cache immediately
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Opportunities
    suspend fun getOpportunities(): List<PlacementOpportunity> {
        return try {
            val snapshot = firestore.collection("opportunities").get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(PlacementOpportunity::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addOpportunity(opportunity: PlacementOpportunity) {
        try {
            firestore.collection("opportunities").document(opportunity.id).set(opportunity).await()
        } catch (e: Exception) {
            throw e
        }
    }

    // Applications
    suspend fun getApplications(): List<ApplicationItem> {
        return try {
            val snapshot = firestore.collection("applications").get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(ApplicationItem::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addApplication(application: ApplicationItem) {
        try {
            firestore.collection("applications").document(application.id).set(application).await()
        } catch (e: Exception) {
            throw e
        }
    }

    // Misinformation Reports
    suspend fun getMisinformationReports(): List<MisinformationReport> {
        return try {
            val snapshot = firestore.collection("misinformationReports").get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(MisinformationReport::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addMisinformationReport(report: MisinformationReport) {
        try {
            firestore.collection("misinformationReports").document(report.id).set(report).await()
        } catch (e: Exception) {
            throw e
        }
    }

    // Verification Requests
    suspend fun getVerificationRequests(): List<VerificationRequest> {
        return try {
            val snapshot = firestore.collection("verificationRequests").get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(VerificationRequest::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateVerificationRequestStatus(id: String, status: String) {
        try {
            firestore.collection("verificationRequests").document(id).update("status", status).await()
        } catch (e: Exception) {
            throw e
        }
    }

    // Notifications
    suspend fun getNotifications(): List<NotificationItem> {
        return try {
            val snapshot = firestore.collection("notifications").get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(NotificationItem::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
