package com.example.placeverify.data

enum class UserRole {
    STUDENT,
    PLACEMENT_OFFICER,
    SUPER_ADMIN
}

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "STUDENT",
    val institution: String = ""
)

data class PlacementOpportunity(
    val id: String = "",
    val title: String = "",
    val companyName: String = "",
    val location: String = "",
    val ctc: String = "",
    val roleType: String = "",
    val deadline: String = "",
    val credibilityScore: Int = 0,
    val isVerified: Boolean = false,
    val verificationEvidence: String = "",
    val eligibilityCriteria: String = "",
    val description: String = "",
    val appliedStatus: String? = null,
    val isSaved: Boolean = false,
    val logoUrl: String = ""
)

data class ApplicationItem(
    val id: String = "",
    val opportunityId: String = "",
    val opportunityTitle: String = "",
    val companyName: String = "",
    val appliedDate: String = "",
    val status: String = "",
    val ctc: String = ""
)

data class MisinformationReport(
    val id: String = "",
    val title: String = "",
    val companyName: String = "",
    val reportedBy: String = "",
    val reason: String = "",
    val status: String = "",
    val date: String = ""
)

data class VerificationRequest(
    val id: String = "",
    val title: String = "",
    val companyName: String = "",
    val submittedBy: String = "",
    val date: String = "",
    val status: String = "",
    val credibilityScoreCheck: Int = 0
)

data class NotificationItem(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val time: String = "",
    val isRead: Boolean = false,
    val type: String = ""
)
