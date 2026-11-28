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
    var skills by remember { mutableStateOf("") }
    var recruitmentDate by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var publishing by remember { mutableStateOf(false) }
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
            text = "Submit a placement announcement for Super Admin review",
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
                label = { Text("CTC (e.g. â‚¹ 20 LPA)") },
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

        OutlinedTextField(value = skills, onValueChange = { skills = it }, label = { Text("Required skills") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = recruitmentDate, onValueChange = { recruitmentDate = it }, label = { Text("Recruitment date") }, modifier = Modifier.fillMaxWidth())
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (companyName.isNotBlank() && title.isNotBlank()) {
                    coroutineScope.launch {
                        publishing = true
                        error = null
                        try {
                            repository.publishDrive(mapOf("companyName" to companyName, "role" to title, "location" to location, "package" to ctc, "deadline" to deadline, "eligibility" to eligibility, "skills" to skills, "recruitmentDate" to recruitmentDate, "details" to description))
                            posted = true
                            onPostSuccess()
                        } catch (e: Exception) {
                            error = e.localizedMessage ?: "Could not publish the drive."
                        } finally { publishing = false }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !publishing && companyName.isNotBlank() && title.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
        ) {
            Text(
                text = if (posted) "Submitted for Review âœ“" else "Submit for Verification",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = androidx.compose.ui.graphics.Color.White
            )
        }
    }
}

