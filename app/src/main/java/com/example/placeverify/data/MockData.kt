package com.example.placeverify.data

object MockData {
    val opportunities = mutableListOf(
        PlacementOpportunity(
            id = "1",
            title = "Software Engineer - Android/Backend",
            companyName = "Google India",
            location = "Bangalore / Hyderabad",
            ctc = "₹ 28.5 LPA",
            roleType = "Full-time",
            deadline = "25 Apr 2025",
            credibilityScore = 99,
            isVerified = true,
            verificationEvidence = "Direct university partnership portal key verified via corporate domain authentication (@google.com)",
            eligibilityCriteria = "B.Tech / M.Tech in CS/IT/ECE with CGPA >= 8.0. No active backlogs.",
            description = "Work with world-class engineering teams to build scalable mobile and cloud infrastructures powering billions of users worldwide.",
            appliedStatus = "Shortlisted",
            isSaved = true
        ),
        PlacementOpportunity(
            id = "2",
            title = "Graduate Engineer Trainee - Full Stack",
            companyName = "Microsoft IDC",
            location = "Hyderabad",
            ctc = "₹ 24.0 LPA",
            roleType = "Full-time",
            deadline = "30 Apr 2025",
            credibilityScore = 98,
            isVerified = true,
            verificationEvidence = "Authorized campus recruitment letter signed by Talent Acquisition Lead & verified via TPO portal.",
            eligibilityCriteria = "B.Tech (CS, IT, ECE) with 7.5+ CGPA across all semesters.",
            description = "Design, develop and ship high-performance distributed systems, cloud services, and AI-driven features.",
            appliedStatus = null,
            isSaved = false
        ),
        PlacementOpportunity(
            id = "3",
            title = "Cloud Solutions Associate",
            companyName = "Amazon Development Center",
            location = "Chennai / Bangalore",
            ctc = "₹ 22.0 LPA",
            roleType = "Full-time",
            deadline = "05 May 2025",
            credibilityScore = 96,
            isVerified = true,
            verificationEvidence = "Cryptographically signed job ID and university career cell clearance hash.",
            eligibilityCriteria = "All Engineering streams with 7.0+ CGPA and strong problem-solving skills.",
            description = "Architect and optimize cloud workloads on AWS for enterprise global customers.",
            appliedStatus = "Applied",
            isSaved = true
        ),
        PlacementOpportunity(
            id = "4",
            title = "Data Analyst Intern / FTE",
            companyName = "Goldman Sachs",
            location = "Bengaluru",
            ctc = "₹ 20.0 LPA",
            roleType = "Internship + FTE",
            deadline = "10 May 2025",
            credibilityScore = 94,
            isVerified = true,
            verificationEvidence = "Official HR TPO handshake confirmation with secure API token auth.",
            eligibilityCriteria = "Mathematics, Statistics, CS or related disciplines with exceptional quantitative aptitude.",
            description = "Analyze complex financial datasets and build robust algorithmic trading analytics tools.",
            appliedStatus = null,
            isSaved = false
        ),
        PlacementOpportunity(
            id = "5",
            title = "Work-from-Home Software Intern (Suspicious Alert)",
            companyName = "Global Tech Recruiters (Unverified)",
            location = "Remote",
            ctc = "₹ 15.0 LPA",
            roleType = "Internship",
            deadline = "Immediate",
            credibilityScore = 32,
            isVerified = false,
            verificationEvidence = "Flagged by PlaceVerify Super Admin: Domain mismatch, registration fee requested via WhatsApp link.",
            eligibilityCriteria = "Open to all, registration fee of ₹999 required.",
            description = "Be your own boss and earn high stipends working from home! (Flagged as recruitment scam).",
            appliedStatus = null,
            isSaved = false
        )
    )

    val applications = mutableListOf(
        ApplicationItem(
            id = "app_1",
            opportunityId = "1",
            opportunityTitle = "Software Engineer - Android/Backend",
            companyName = "Google India",
            appliedDate = "10 Apr 2025",
            status = "Shortlisted",
            ctc = "₹ 28.5 LPA"
        ),
        ApplicationItem(
            id = "app_2",
            opportunityId = "3",
            opportunityTitle = "Cloud Solutions Associate",
            companyName = "Amazon Development Center",
            appliedDate = "12 Apr 2025",
            status = "Applied",
            ctc = "₹ 22.0 LPA"
        )
    )

    val misinformationReports = mutableListOf(
        MisinformationReport(
            id = "rep_1",
            title = "Fake Google Job Offer WhatsApp Forward",
            companyName = "Google India (Imposter)",
            reportedBy = "Rahul Sharma (Student)",
            reason = "Asking for ₹1,500 security deposit via UPI QR code for interview slot.",
            status = "Verified Fake",
            date = "14 Apr 2025"
        ),
        MisinformationReport(
            id = "rep_2",
            title = "Expired Microsoft Hackathon Link circulating on Telegram",
            companyName = "Microsoft IDC",
            reportedBy = "Priya Patel (Student)",
            reason = "Link is active from 2023 batch recruitment drive being passed off as current.",
            status = "Pending Review",
            date = "15 Apr 2025"
        )
    )

    val verificationRequests = mutableListOf(
        VerificationRequest(
            id = "ver_1",
            title = "Software Engineer 2026 Batch Drive",
            companyName = "Adobe Systems",
            submittedBy = "Dr. R. K. Verma (Placement Officer)",
            date = "15 Apr 2025",
            status = "Pending",
            credibilityScoreCheck = 97
        ),
        VerificationRequest(
            id = "ver_2",
            title = "Product Management Associate",
            companyName = "Flipkart",
            submittedBy = "Prof. Ananya Roy (Placement Officer)",
            date = "14 Apr 2025",
            status = "Approved",
            credibilityScoreCheck = 99
        )
    )

    val notifications = mutableListOf(
        NotificationItem(
            id = "notif_1",
            title = "Application Shortlisted! 🎉",
            message = "Google India has shortlisted your profile for the Software Engineer interview round.",
            time = "2 hours ago",
            isRead = false,
            type = "APPLICATION"
        ),
        NotificationItem(
            id = "notif_2",
            title = "Scam Alert Flagged",
            message = "The announcement by 'Global Tech Recruiters' was verified as a fraudulent recruitment attempt.",
            time = "1 day ago",
            isRead = true,
            type = "ALERT"
        ),
        NotificationItem(
            id = "notif_3",
            title = "New Verified Drive Added",
            message = "Microsoft IDC has posted a new Graduate Engineer Trainee opportunity.",
            time = "2 days ago",
            isRead = true,
            type = "VERIFIED"
        )
    )
}
