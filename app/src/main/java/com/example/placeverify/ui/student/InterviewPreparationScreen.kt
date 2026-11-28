package com.example.placeverify.ui.student

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.placeverify.data.repository.PlaceVerifyRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import java.text.DateFormat

private data class CompanyOption(val id: String, val name: String, val roles: List<String>)
private data class KitQuestion(val id: String, val text: String, val category: String, val round: String, val difficulty: String, val answer: String, val origin: String, val url: String, val title: String, val sourceCount: Long)
private data class KitExperience(val id: String, val text: String, val url: String, val title: String)

@Composable
fun InterviewPreparationScreen(repository: PlaceVerifyRepository) {
    var companies by remember { mutableStateOf<List<CompanyOption>>(emptyList()) }
    var company by remember { mutableStateOf<CompanyOption?>(null) }
    var companySearch by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var kitId by remember { mutableStateOf("") }
    var jobId by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var resultDate by remember { mutableStateOf<java.util.Date?>(null) }
    var retryDate by remember { mutableStateOf<java.util.Date?>(null) }
    var resultIsCached by remember { mutableStateOf(false) }
    var questions by remember { mutableStateOf<List<KitQuestion>>(emptyList()) }
    var experiences by remember { mutableStateOf<List<KitExperience>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var collegeId by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val uri = LocalUriHandler.current
    val db = repository.database()
    LaunchedEffect(Unit) { try { collegeId = repository.collegeId() } catch (e: Exception) { error = e.message } }
    DisposableEffect(collegeId) {
        var listener: ListenerRegistration? = null
        if (collegeId.isNotBlank()) listener = db.collection("companies").whereEqualTo("collegeId", collegeId).whereEqualTo("approved", true).addSnapshotListener { snapshot, e ->
            if (e != null) error = "Unable to load companies: ${e.localizedMessage}"
            else companies = snapshot?.documents.orEmpty().map { d ->
                val roles = (d.get("roles") as? Map<*, *>)?.values?.filterIsInstance<String>().orEmpty().sorted()
                CompanyOption(d.id, d.getString("name").orEmpty(), roles)
            }.sortedBy { it.name.lowercase() }
        }
        onDispose { listener?.remove() }
    }
    DisposableEffect(kitId) {
        val listener = if (kitId.isNotBlank()) db.collection("preparationKits").document(kitId).addSnapshotListener { doc, e ->
            if (e != null) error = "Unable to load research status: ${e.localizedMessage}"
            else {
                status = doc?.getString("status").orEmpty()
                val activeJob = doc?.getString("jobId").orEmpty()
                jobId = doc?.getString("resultJobId") ?: activeJob
                resultDate = (doc?.getTimestamp("resultUpdatedAt") ?: doc?.getTimestamp("completedAt"))?.toDate()
                retryDate = doc?.getTimestamp("retryAt")?.toDate()
                resultIsCached = jobId.isNotBlank() && (jobId != activeJob || status in listOf("COMPLETED", "PARTIAL", "QUOTA_EXHAUSTED", "FAILED"))
                error = doc?.getString("error")
            }
        } else null
        onDispose { listener?.remove() }
    }
    DisposableEffect(kitId, jobId) {
        val listeners = mutableListOf<ListenerRegistration>()
        if (kitId.isNotBlank() && jobId.isNotBlank()) {
            val kit = db.collection("preparationKits").document(kitId)
            listeners += kit.collection("interviewQuestions").whereEqualTo("jobId", jobId).addSnapshotListener { snap, e ->
                if (e != null) error = "Unable to load questions: ${e.localizedMessage}"
                else questions = snap?.documents.orEmpty().map { d -> KitQuestion(d.id,d.getString("text").orEmpty(),d.getString("category").orEmpty(),d.getString("round").orEmpty(),d.getString("difficulty").orEmpty(),d.getString("answer").orEmpty(),d.getString("origin").orEmpty(),d.getString("sourceUrl").orEmpty(),d.getString("sourceTitle").orEmpty(),d.getLong("sourceCount") ?: 0) }
            }
            listeners += kit.collection("interviewExperiences").whereEqualTo("jobId", jobId).addSnapshotListener { snap, e ->
                if (e != null) error = "Unable to load experiences: ${e.localizedMessage}"
                else experiences = snap?.documents.orEmpty().map { d -> KitExperience(d.id,d.getString("text").orEmpty(),d.getString("sourceUrl").orEmpty(),d.getString("sourceTitle").orEmpty()) }
            }
        }
        onDispose { listeners.forEach { it.remove() } }
    }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("AI Interview Preparation", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text("Public reports are not verified company questions. AI practice questions are generated separately.") }
        item { OutlinedTextField(companySearch, { companySearch = it; company = null; role = ""; kitId = ""; jobId = ""; questions = emptyList(); experiences = emptyList() }, label = { Text("Search companies") }, modifier = Modifier.fillMaxWidth()) }
        if (company == null) items(companies.filter { it.name.contains(companySearch, true) }, key = { it.id }) { option ->
            ListItem(headlineContent = { Text(option.name) }, modifier = Modifier.clickable { company = option; companySearch = option.name })
        }
        if (company != null) {
            item { Text("Select a job role", style = MaterialTheme.typography.titleMedium) }
            items(company!!.roles) { r -> FilterChip(selected = role == r, onClick = { role = r; jobId = ""; status = ""; resultDate = null; retryDate = null; questions = emptyList(); experiences = emptyList(); kitId = repository.preparationKitId(company!!.id, r) }, label = { Text(r) }) }
            item { Button(enabled = role.isNotBlank() && !busy, onClick = {
                scope.launch { busy = true; error = null; try { kitId = repository.requestKit(company!!.id, role) } catch (e: Exception) { error = e.localizedMessage ?: "Could not start research." }; busy = false }
            }) { Text(if (busy) "Requesting…" else "Generate Preparation Kit") } }
        }
        error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
        if (kitId.isNotBlank()) {
            item {
                Text(if (status.isBlank()) "No saved research yet." else "Research status: $status")
                if (status in listOf("QUEUED", "SEARCHING", "EXTRACTING", "DEDUPLICATING", "GENERATING_EXPLANATIONS")) LinearProgressIndicator(Modifier.fillMaxWidth())
                if (resultIsCached && (questions.isNotEmpty() || experiences.isNotEmpty())) Text("Showing saved research")
                resultDate?.let { Text("Last research: ${DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(it)}") }
                if (status == "QUOTA_EXHAUSTED") Text(retryDate?.let { "Research can be retried after ${DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(it)}." } ?: "Research is temporarily unavailable. Please try again later.")
                Text("${questions.size} questions collected")
                if (status == "PARTIAL") Text("Research is incomplete; fewer supported results may be available.")
            }
            item { OutlinedTextField(query, { query = it }, label = { Text("Search questions") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(filter, { filter = it }, label = { Text("Filter topic, round or difficulty") }, modifier = Modifier.fillMaxWidth()) }
            items(questions.filter { it.text.contains(query, true) && listOf(it.category,it.round,it.difficulty).any { v -> v.contains(filter, true) } }, key = { it.id }) { q ->
                Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) {
                    Text(q.origin, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    if (q.sourceCount > 0) Text("Reported in ${q.sourceCount} retrieved source links; not a verified company statistic.", style = MaterialTheme.typography.bodySmall)
                    Text(q.text, fontWeight = FontWeight.SemiBold)
                    Text(listOf(q.category,q.round,q.difficulty).filter { it.isNotBlank() }.joinToString(" · "))
                    if (q.answer.isNotBlank()) Text("AI-generated answer: ${q.answer}")
                    if (q.url.startsWith("https://") || q.url.startsWith("http://")) TextButton(onClick = { runCatching { uri.openUri(q.url) } }) { Text("Source: ${q.title}") }
                    ProgressControls(repository, kitId, q.id)
                } }
            }
            if (experiences.isNotEmpty()) item { Text("Publicly reported experiences", style = MaterialTheme.typography.titleLarge) }
            items(experiences, key = { it.id }) { x -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) {
                Text(x.text); if (x.url.startsWith("https://") || x.url.startsWith("http://")) TextButton(onClick = { runCatching { uri.openUri(x.url) } }) { Text("Open source: ${x.title}") }
            } } }
        }
    }
}

@Composable
private fun ProgressControls(repository: PlaceVerifyRepository, kitId: String, questionId: String) {
    val uid = repository.getCurrentUserId() ?: return
    val ref = remember(uid, kitId, questionId) { repository.database().collection("studentPreparationProgress").document(uid).collection("questions").document("${kitId}_$questionId") }
    var bookmarked by remember(ref) { mutableStateOf(false) }; var completed by remember(ref) { mutableStateOf(false) }
    DisposableEffect(ref) { val l = ref.addSnapshotListener { doc, _ -> bookmarked = doc?.getBoolean("bookmarked") == true; completed = doc?.getBoolean("completed") == true }; onDispose { l.remove() } }
    fun toggle(field: String) {
        repository.database().runTransaction { transaction ->
            val current = transaction.get(ref)
            val saved = current.getBoolean("bookmarked") == true
            val done = current.getBoolean("completed") == true
            transaction.set(ref, mapOf("bookmarked" to if (field == "bookmarked") !saved else saved,
                "completed" to if (field == "completed") !done else done,
                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()))
        }
    }
    Row { TextButton(onClick = { toggle("bookmarked") }) { Text(if (bookmarked) "Bookmarked" else "Bookmark") }
        TextButton(onClick = { toggle("completed") }) { Text(if (completed) "Completed" else "Mark complete") } }
}
