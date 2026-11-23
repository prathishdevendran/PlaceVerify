package com.example.placeverify.data

enum class UserRole {
    STUDENT,
    PLACEMENT_OFFICER,
    SUPER_ADMIN
}

data class PlacementOpportunity(
    val id: String,
    val title: String,
    val companyName: String,
    val location: String,
    val ctc: String,
    val roleType: String,
    val deadline: String,
    val credibilityScore: Int, // e.g., 95 out of 100
    val isVerified: Boolean,
    val verificationEvidence: String,
    val eligibilityCriteria: String,
    val description: String,
    val appliedStatus: String? = null, // "Applied", "Shortlisted", "Interview", "Offered"
    val isSaved: Boolean = false,
    val logoUrl: String = ""
)

data class ApplicationItem(
    val id: String,
    val opportunityId: String,
    val opportunityTitle: String,
    val companyName: String,
    val appliedDate: String,
    val status: String, // "Applied", "Shortlisted", "Interview", "Offered", "Rejected"
    val ctc: String
)

data class MisinformationReport(
    val id: String,
    val title: String,
    val companyName: String,
    val reportedBy: String,
    val reason: String,
    val status: String, // "Pending Review", "Verified Fake", "Dismissed"
    val date: String
)

data class VerificationRequest(
    val id: String,
    val title: String,
    val companyName: String,
    val submittedBy: String,
    val date: String,
    val status: String, // "Pending", "Approved", "Rejected"
    val credibilityScoreCheck: Int
)

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val isRead: Boolean,
    val type: String // "VERIFIED", "ALERT", "APPLICATION"
)
