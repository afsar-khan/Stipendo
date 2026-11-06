package com.stipendo.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.stipendo.app.data.DegreeLevel
import com.stipendo.app.data.Funding
import com.stipendo.app.data.RemoteUpdater
import com.stipendo.app.data.SavedStore
import com.stipendo.app.data.Scholarship
import com.stipendo.app.data.StudyLength
import com.stipendo.app.data.ScholarshipRepository
import com.stipendo.app.data.TeachingLanguage
import com.stipendo.app.ui.AppLanguage
import androidx.core.content.ContextCompat
import com.stipendo.app.reminders.Reminders
import com.stipendo.app.ui.DetailScreen
import com.stipendo.app.ui.SavedScreen
import com.stipendo.app.ui.daysUntil
import com.stipendo.app.ui.isRecent
import com.stipendo.app.ui.ResultsScreen
import com.stipendo.app.ui.SearchScreen
import com.stipendo.app.ui.filterScholarships
import com.stipendo.app.ui.LocalNeu
import com.stipendo.app.ui.LocalStrings
import com.stipendo.app.ui.StipendoTheme
import com.stipendo.app.ui.stringsFor
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val prefs by lazy { getSharedPreferences("stipendo", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // First launch: use the phone's language if the app supports it, otherwise English.
        val startLanguage = AppLanguage.fromCode(prefs.getString("lang", null) ?: Locale.getDefault().language)
        val startData = ScholarshipRepository.load(this)
        Reminders.rescheduleAll(this, startData)   // keeps reminders right after data updates
        setContent {
            var lang by rememberSaveable { mutableStateOf(startLanguage) }
            var scholarships by remember { mutableStateOf(startData) }

            // Show what is on the phone straight away, then check the website for newer data.
            LaunchedEffect(Unit) {
                val updated = withContext(Dispatchers.IO) { RemoteUpdater.refresh(applicationContext) }
                if (updated != null) {
                    scholarships = updated
                    Reminders.rescheduleAll(applicationContext, updated)
                }
            }
            StipendoTheme {
                StipendoApp(
                    scholarships = scholarships,
                    lang = lang,
                    onLang = {
                        lang = it
                        prefs.edit().putString("lang", it.code).apply()
                    },
                )
            }
        }
    }
}

@Composable
fun StipendoApp(scholarships: List<Scholarship>, lang: AppLanguage, onLang: (AppLanguage) -> Unit) {
    val colors = LocalNeu.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Saved scholarships (kept on the phone) and the notification permission (Android 13+).
    var saved by remember { mutableStateOf(SavedStore.load(context)) }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    fun toggleSave(item: Scholarship) {
        if (item.id in saved) {
            saved = saved - item.id
            SavedStore.save(context, saved)
            Reminders.cancel(context, item)
        } else {
            saved = saved + item.id
            SavedStore.save(context, saved)
            Reminders.schedule(context, item)
            if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // State lives here so choices and scroll position survive moving between pages.
    var onResults by rememberSaveable { mutableStateOf(false) }
    var onSaved by rememberSaveable { mutableStateOf(false) }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var degree by rememberSaveable { mutableStateOf<DegreeLevel?>(null) }
    var funding by rememberSaveable { mutableStateOf<Funding?>(null) }
    var language by rememberSaveable { mutableStateOf<TeachingLanguage?>(null) }
    var length by rememberSaveable { mutableStateOf<StudyLength?>(null) }
    val listState = rememberLazyListState()

    // Only offer choices that at least one entry has, so nobody picks a dead end.
    val degreeOptions = remember(scholarships) { DegreeLevel.entries.filter { d -> scholarships.any { d in it.degrees } } }
    val lengthOptions = remember(scholarships) { StudyLength.entries.filter { l -> scholarships.any { l in it.lengths } } }
    val fundingOptions = remember(scholarships) { Funding.entries.filter { f -> scholarships.any { it.funding == f } } }
    val languageOptions = remember(scholarships) { TeachingLanguage.entries.filter { l -> scholarships.any { l in it.languages } } }

    val matches = remember(scholarships, degree, funding, language, length) {
        filterScholarships(scholarships, degree, funding, language, length)
    }

    // Entries added in the last 7 days.
    val newItems = remember(scholarships) { scholarships.filter { isRecent(it.addedOn) }.take(3) }

    // Soonest open deadlines in the next 90 days.
    val closingSoon = remember(scholarships) {
        scholarships
            .mapNotNull { item -> daysUntil(item.deadline)?.takeIf { it in 0..90 }?.let { item to it } }
            .sortedBy { it.second }
            .take(3)
            .map { it.first }
    }

    // Back goes detail -> (saved or results) -> search.
    BackHandler(enabled = selectedId != null || onSaved || onResults) {
        when {
            selectedId != null -> selectedId = null
            onSaved -> onSaved = false
            else -> onResults = false
        }
    }

    val page = when {
        selectedId != null -> "detail:$selectedId"
        onSaved -> "saved"
        onResults -> "results"
        else -> "search"
    }

    CompositionLocalProvider(
        LocalStrings provides stringsFor(lang),
        LocalLayoutDirection provides if (lang.rtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            AnimatedContent(
                targetState = page,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(160)) },
                label = "screen",
            ) { current ->
                when {
                    current.startsWith("detail:") -> {
                        val item = scholarships.firstOrNull { it.id == current.removePrefix("detail:") }
                        if (item != null) {
                            DetailScreen(
                                item,
                                saved = item.id in saved,
                                onToggleSave = { toggleSave(item) },
                                onBack = { selectedId = null },
                            )
                        }
                    }
                    current == "saved" -> SavedScreen(
                        items = scholarships.filter { it.id in saved },
                        onBack = { onSaved = false },
                        onOpen = { selectedId = it.id },
                    )
                    current == "results" -> ResultsScreen(
                        results = matches,
                        degree = degree, funding = funding, language = language, length = length,
                        savedIds = saved,
                        listState = listState,
                        onEdit = { onResults = false },
                        onOpen = { selectedId = it.id },
                    )
                    else -> SearchScreen(
                        lang = lang, onLang = onLang,
                        degree = degree, onDegree = { degree = it },
                        funding = funding, onFunding = { funding = it },
                        language = language, onLanguage = { language = it },
                        length = length, onLength = { length = it },
                        degreeOptions = degreeOptions, lengthOptions = lengthOptions,
                        fundingOptions = fundingOptions, languageOptions = languageOptions,
                        matchCount = matches.size,
                        newItems = newItems,
                        closingSoon = closingSoon,
                        savedCount = saved.size,
                        onOpenSaved = { onSaved = true },
                        onOpen = { selectedId = it.id },
                        onGo = {
                            onResults = true
                            scope.launch { listState.scrollToItem(0) }
                        },
                    )
                }
            }
        }
    }
}
