package com.example.placeverify.ui.officer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.placeverify.ui.theme.DeepNavy

data class StudentRecord(val name: String, val branch: String, val cgpa: String, val status: String)

@Composable
fun StudentEligibilityScreen() {
    val students = listOf(
        StudentRecord("Rahul Sharma", "Computer Science", "8.9 CGPA", "Eligible"),
        StudentRecord("Priya Patel", "Information Technology", "9.2 CGPA", "Eligible"),
        StudentRecord("Amit Kumar", "Electronics", "7.4 CGPA", "Eligible"),
        StudentRecord("Neha Singh", "Mechanical", "6.8 CGPA", "Not Eligible (CGPA < 7.0)")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Student Eligibility Manager",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = DeepNavy
        )
        Text(
            text = "Review batch-wise CGPA and academic criteria matching",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(students) { student ->
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
                                text = student.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = DeepNavy
                            )
                            Text(
                                text = student.cgpa,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Department: ${student.branch}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = student.status,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (student.status.startsWith("Eligible")) com.example.placeverify.ui.theme.SuccessGreen else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
