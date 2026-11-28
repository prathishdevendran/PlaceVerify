package com.example.placeverify.ui.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.placeverify.ui.theme.DeepNavy
import com.example.placeverify.data.MisinformationReport
import com.example.placeverify.data.repository.PlaceVerifyRepository
import kotlinx.coroutines.launch
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportSuspiciousScreen(
    onBack: () -> Unit,
    onSubmitSuccess: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    var submitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val repository = remember { PlaceVerifyRepository() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Suspicious Announcement") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Help protect your peers from placement scams, fake fee demands, and imposter links.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Announcement Title / Source Link") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = company,
                onValueChange = { company = it },
                label = { Text("Imposter Company / Organization Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("Why is this suspicious? (e.g. asking for money)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(
                enabled = !submitting && title.isNotBlank() && company.isNotBlank() && reason.isNotBlank(),
                onClick = {
                    scope.launch {
                        submitting = true
                        error = null
                        try {
                            repository.addMisinformationReport(MisinformationReport(
                                id = UUID.randomUUID().toString(), title = title.trim(), companyName = company.trim(),
                                reason = reason.trim(), date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                            ))
                            submitted = true
                            onSubmitSuccess()
                        } catch (e: Exception) { error = e.localizedMessage ?: "Could not submit your report." }
                        finally { submitting = false }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
            ) {
                Text(
                    text = if (submitted) "Report Submitted Successfully ✓" else "Submit Report to Admin",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = androidx.compose.ui.graphics.Color.White
                )
            }
        }
    }
}
