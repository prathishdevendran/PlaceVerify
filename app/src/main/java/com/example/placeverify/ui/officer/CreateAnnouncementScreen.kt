package com.example.placeverify.ui.officer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.placeverify.data.PlacementOpportunity
import com.example.placeverify.data.repository.PlaceVerifyRepository
import com.example.placeverify.ui.theme.DeepNavy
import kotlinx.coroutines.launch

@Composable
fun CreateAnnouncementScreen(
    onPostSuccess: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var ctc by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var eligibility by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var posted by remember { mutableStateOf(false) }

    val repository = remember { PlaceVerifyRepository() }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Post Recruitment Drive",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = DeepNavy
        )
        Text(
            text = "Submit a verified placement announcement for students",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = companyName,
            onValueChange = { companyName = it },
            label = { Text("Company Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Job Title / Role") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = ctc,
                onValueChange = { ctc = it },
                label = { Text("CTC (e.g. ₹ 20 LPA)") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        OutlinedTextField(
            value = deadline,
            onValueChange = { deadline = it },
            label = { Text("Application Deadline") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = eligibility,
            onValueChange = { eligibility = it },
            label = { Text("Eligibility Criteria") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Job Description") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (companyName.isNotBlank() && title.isNotBlank()) {
                    coroutineScope.launch {
                        val newOpp = PlacementOpportunity(
                            id = System.currentTimeMillis().toString(),
                            title = title,
                            companyName = companyName,
                            location = location.ifBlank { "Remote / Multi-location" },
                            ctc = ctc.ifBlank { "₹ 18.0 LPA" },
                            roleType = "Full-time",
                            deadline = deadline.ifBlank { "30 May 2025" },
                            credibilityScore = 98,
                            isVerified = true,
                            verificationEvidence = "Verified by Placement Officer campus portal handshake.",
                            eligibilityCriteria = eligibility.ifBlank { "All streams CGPA >= 7.0" },
                            description = description.ifBlank { "Exciting career opportunity verified by university TPO." }
                        )
                        repository.addOpportunity(newOpp)
                        posted = true
                        onPostSuccess()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
        ) {
            Text(
                text = if (posted) "Drive Posted Successfully ✓" else "Publish Verified Announcement",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = androidx.compose.ui.graphics.Color.White
            )
        }
    }
}
