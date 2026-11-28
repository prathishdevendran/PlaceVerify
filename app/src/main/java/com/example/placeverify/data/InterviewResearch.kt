package com.example.placeverify.data

// These records are populated by a trusted research service, not generated on the device.
data class InterviewQuestion(
    val text: String = "",
    val topic: String = "",
    val round: String = "",
    val difficulty: String = "",
    val explanation: String = "",
    val sourceUrls: List<String> = emptyList()
)

data class InterviewExperience(
    val summary: String = "",
    val sourceUrl: String = "",
    val publicationDate: String = ""
)

data class InterviewResearch(
    val status: String = "",
    val message: String = "",
    val questions: List<InterviewQuestion> = emptyList(),
    val experiences: List<InterviewExperience> = emptyList(),
    val roadmap: String = ""
)
