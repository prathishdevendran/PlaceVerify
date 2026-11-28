package com.example.placeverify.data.repository

import com.example.placeverify.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.text.Normalizer
import java.util.Locale

class PlaceVerifyRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val functions = FirebaseFunctions.getInstance("us-central1")

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
        val verifiedRole = try {
            roleFromClaims(auth.currentUser?.getIdToken(true)?.await()?.claims?.get("role"))
        } catch (_: Exception) { "STUDENT" }
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            val profile = doc.toObject(UserProfile::class.java)?.copy(uid = uid, role = verifiedRole)
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
                    role = verifiedRole,
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
                    role = verifiedRole,
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
        var signedInForRegistration = false
        var profileAlreadyExists = false
        return try {
            require(role == "STUDENT") { "Privileged accounts must be approved by an administrator." }
            require(name.isNotBlank() && institution.isNotBlank()) { "Enter your name and institution." }
            val result = try {
                auth.createUserWithEmailAndPassword(email.trim(), pass).await()
            } catch (_: FirebaseAuthUserCollisionException) {
                // An earlier attempt may have created Auth but failed to create the profile.
                // Authenticate first; never replace an existing profile.
                auth.signInWithEmailAndPassword(email.trim(), pass).await()
            }
            signedInForRegistration = true
            val user = result.user ?: throw IllegalStateException("Could not establish your account session.")
            val uid = user.uid
            val verifiedEmail = user.email ?: throw IllegalStateException("Your account has no email address.")
            val userData = mapOf(
                "uid" to uid, "name" to name.trim(), "email" to verifiedEmail,
                "role" to "STUDENT", "institution" to institution.trim(),
                "createdAt" to System.currentTimeMillis()
            )
            val ref = firestore.collection("users").document(uid)
            firestore.runTransaction { transaction ->
                if (transaction.get(ref).exists()) {
                    profileAlreadyExists = true
                    throw IllegalStateException("This account already has a profile. Please sign in instead.")
                }
                transaction.set(ref, userData)
            }.await()
            cachedProfile = UserProfile(uid = uid, name = name.trim(), email = verifiedEmail,
                role = "STUDENT", institution = institution.trim())
            Result.success(uid)
        } catch (e: Exception) {
            if (signedInForRegistration) {
                signOut()
                if (!profileAlreadyExists) {
                    return Result.failure(IllegalStateException(
                        "Your account was created, but profile setup could not be completed. Retry Create Account with the same email and password.", e))
                }
            }
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
    suspend fun publishDrive(data: Map<String, String>) {
        functions.getHttpsCallable("publishPlacementDrive").call(data).await()
    }

    suspend fun reviewDrive(driveId: String, approved: Boolean, note: String) {
        functions.getHttpsCallable("reviewPlacementDrive").call(mapOf(
            "driveId" to driveId, "decision" to if (approved) "APPROVED" else "REJECTED",
            "evidence" to if (approved) note else "", "rejectionReason" to if (approved) "" else note
        )).await()
    }

    suspend fun requestKit(companyId: String, role: String): String {
        val result = functions.getHttpsCallable("requestPreparationKit")
            .call(mapOf("companyId" to companyId, "role" to role)).await().getData() as Map<*, *>
        return result["kitId"] as String
    }

    fun preparationKitId(companyId: String, role: String): String {
        val normalized = Normalizer.normalize(role, Normalizer.Form.NFKC).lowercase(Locale.ROOT)
            .replace(Regex("[^\\p{L}\\p{N}]+"), " ").trim().replace(Regex("\\s+"), " ")
        return MessageDigest.getInstance("SHA-256").digest("$companyId|$normalized".toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it.toInt() and 0xff) }
    }

    fun database() = firestore
    suspend fun collegeId(): String = auth.currentUser?.getIdToken(false)?.await()?.claims?.get("collegeId") as? String
        ?: throw IllegalStateException("Your college membership has not been assigned by an administrator.")

    suspend fun getInterviewCompanies(): List<PlacementOpportunity> {
        val snapshot = firestore.collection("opportunities").whereEqualTo("collegeId", collegeId()).whereEqualTo("isVerified", true).get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(PlacementOpportunity::class.java)?.copy(id = doc.id)
        }.filter { it.isVerified && it.companyName.isNotBlank() }.sortedBy { it.companyName.lowercase() }
    }

    suspend fun getInterviewResearch(opportunityId: String): InterviewResearch? =
        firestore.collection("interviewResearch").document(opportunityId).get().await()
            .toObject(InterviewResearch::class.java)

    suspend fun getOpportunities(): List<PlacementOpportunity> {
        return try {
            val snapshot = firestore.collection("opportunities").whereEqualTo("collegeId", collegeId()).whereEqualTo("isVerified", true).get().await()
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

    suspend fun savedPlacementIds(): Set<String> {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Please sign in.")
        return firestore.collection("users").document(uid).collection("savedPlacements").get().await().documents.map { it.id }.toSet()
    }

    suspend fun setPlacementSaved(opportunityId: String, saved: Boolean) {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Please sign in.")
        val ref = firestore.collection("users").document(uid).collection("savedPlacements").document(opportunityId)
        if (saved) ref.set(mapOf("savedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp())).await()
        else ref.delete().await()
    }

    // Applications
    suspend fun getApplications(): List<ApplicationItem> {
        return try {
            val snapshot = firestore.collection("applications").whereEqualTo("collegeId", collegeId()).whereEqualTo("userId", auth.currentUser?.uid).get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(ApplicationItem::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addApplication(application: ApplicationItem) {
        try {
            val uid = auth.currentUser?.uid ?: throw IllegalStateException("Please sign in.")
            firestore.collection("applications").document(application.id).set(mapOf(
                "userId" to uid, "collegeId" to collegeId(), "opportunityId" to application.opportunityId,
                "opportunityTitle" to application.opportunityTitle, "companyName" to application.companyName,
                "appliedDate" to application.appliedDate, "status" to "Applied", "ctc" to application.ctc
            )).await()
        } catch (e: Exception) {
            throw e
        }
    }

    // Misinformation Reports
    suspend fun getMisinformationReports(): List<MisinformationReport> {
        return try {
            val snapshot = firestore.collection("misinformationReports").whereEqualTo("collegeId", collegeId()).get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(MisinformationReport::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addMisinformationReport(report: MisinformationReport) {
        try {
            val uid = auth.currentUser?.uid ?: throw IllegalStateException("Please sign in.")
            firestore.collection("misinformationReports").document(report.id).set(mapOf(
                "collegeId" to collegeId(), "title" to report.title, "companyName" to report.companyName,
                "reportedBy" to uid, "reason" to report.reason, "status" to "Pending", "date" to report.date
            )).await()
        } catch (e: Exception) {
            throw e
        }
    }

    // Verification Requests
    suspend fun getVerificationRequests(): List<VerificationRequest> {
        return try {
            val snapshot = firestore.collection("verificationRequests").whereEqualTo("collegeId", collegeId()).whereEqualTo("status", "Pending").get().await()
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
            val snapshot = firestore.collection("notifications").whereEqualTo("collegeId", collegeId()).whereEqualTo("userId", auth.currentUser?.uid).get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(NotificationItem::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
