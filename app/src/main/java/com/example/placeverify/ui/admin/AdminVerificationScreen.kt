package com.example.placeverify.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.placeverify.data.VerificationRequest
import com.example.placeverify.data.repository.PlaceVerifyRepository
import kotlinx.coroutines.launch
import com.example.placeverify.ui.theme.DeepNavy
import com.example.placeverify.ui.theme.SuccessGreen

@Composable
fun AdminVerificationScreen(
    requests: List<VerificationRequest>
) {
    val repository = remember { PlaceVerifyRepository() }
    val scope = rememberCoroutineScope()
    var notes by remember { mutableStateOf(mapOf<String, String>()) }
    var reviewed by remember { mutableStateOf(setOf<String>()) }
    var pending by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val requestList = requests.filterNot { it.id in reviewed }
    fun review(id: String, approved: Boolean) {
        scope.launch {
            pending = id
            error = null
            try {
                repository.reviewDrive(id, approved, notes[id].orEmpty())
                reviewed = reviewed + id
            } catch (e: Exception) { error = e.localizedMessage ?: "Unable to review this announcement." }
            finally { pending = null }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Verification Queue",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = DeepNavy
        )
        Text(
            text = "Review and approve campus recruitment announcements",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Spacer(modifier = Modifier.height(16.dp))

        if (requestList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    text = "No pending verification requests.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(requestList) { req ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = req.companyName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = DeepNavy
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SuccessGreen.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${req.credibilityScoreCheck}% AI Score",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = SuccessGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = req.title,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Submitted by: ${req.submittedBy}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = notes[req.id].orEmpty(),
                                onValueChange = { notes = notes + (req.id to it) },
                                label = { Text("Supporting evidence or rejection reason") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(onClick = { review(req.id, false) }, enabled = pending == null && notes[req.id].orEmpty().isNotBlank(), modifier = Modifier.weight(1f)) { Text("Reject") }
                                Button(onClick = { review(req.id, true) }, enabled = pending == null && notes[req.id].orEmpty().isNotBlank(), modifier = Modifier.weight(1f)) { Text("Approve") }
                            }
                        }
                    }
                }
            }
        }
    }
}
