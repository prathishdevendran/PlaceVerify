package com.example.placeverify.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.placeverify.data.PlacementOpportunity
import com.example.placeverify.data.ApplicationItem
import com.example.placeverify.data.repository.PlaceVerifyRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.placeverify.ui.theme.DeepNavy
import com.example.placeverify.ui.theme.SuccessGreen
import com.example.placeverify.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpportunityDetailScreen(
    opportunityId: String,
    opportunities: List<PlacementOpportunity>,
    onBack: () -> Unit,
    onApply: (String) -> Unit
) {
    val opportunity = opportunities.find { it.id == opportunityId }
    if (opportunity == null) {
        Column { Text("Announcement is unavailable or still loading."); TextButton(onClick = onBack) { Text("Back") } }
        return
    }
    val repository = remember { PlaceVerifyRepository() }
    val scope = rememberCoroutineScope()
    var isSaved by remember(opportunityId) { mutableStateOf(false) }
    var applied by remember(opportunityId) { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(opportunityId) {
        try {
            isSaved = opportunityId in repository.savedPlacementIds()
            applied = repository.getApplications().any { it.opportunityId == opportunityId }
        } catch (e: Exception) { error = e.localizedMessage ?: "Unable to load your saved data." }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Opportunity Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(enabled = !busy, onClick = {
                        scope.launch {
                            busy = true; error = null
                            try { repository.setPlacementSaved(opportunity.id, !isSaved); isSaved = !isSaved }
                            catch (e: Exception) { error = e.localizedMessage ?: "Unable to save this announcement." }
                            finally { busy = false }
                        }
                    }) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Save",
                            tint = if (isSaved) DeepNavy else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                busy = true; error = null
                                try {
                                    val uid = repository.getCurrentUserId() ?: throw IllegalStateException("Please sign in.")
                                    repository.addApplication(ApplicationItem(id = "${uid}_${opportunity.id}", opportunityId = opportunity.id,
                                        opportunityTitle = opportunity.title, companyName = opportunity.companyName,
                                        appliedDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()), ctc = opportunity.ctc))
                                    applied = true
                                    onApply(opportunity.id)
                                } catch (e: Exception) { error = e.localizedMessage ?: "Unable to save the application." }
                                finally { busy = false }
                            }
                        },
                        enabled = !applied && !busy,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeepNavy,
                            disabledContainerColor = SuccessGreen
                        )
                    ) {
                        Text(
                            text = if (applied) "Application Submitted Successfully ✓" else "Apply Now",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            // Company & Title
            Text(
                text = opportunity.companyName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = opportunity.title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = DeepNavy
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Credibility Score & Trust Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (opportunity.isVerified) SuccessGreen.copy(alpha = 0.1f) else WarningAmber.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (opportunity.isVerified) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = "Trust Status",
                        tint = if (opportunity.isVerified) SuccessGreen else WarningAmber,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Credibility Score: ${opportunity.credibilityScore}%",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (opportunity.isVerified) SuccessGreen else WarningAmber
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = opportunity.verificationEvidence,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Key Details Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailMetricCard(title = "CTC Offer", value = opportunity.ctc, modifier = Modifier.weight(1f))
                DetailMetricCard(title = "Job Location", value = opportunity.location, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailMetricCard(title = "Role Type", value = opportunity.roleType, modifier = Modifier.weight(1f))
                DetailMetricCard(title = "Deadline", value = opportunity.deadline, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Job Description
            Text(
                text = "Job Description",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = DeepNavy
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = opportunity.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Eligibility Criteria
            Text(
                text = "Eligibility Criteria",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = DeepNavy
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = opportunity.eligibilityCriteria,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DetailMetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = DeepNavy
            )
        }
    }
}
