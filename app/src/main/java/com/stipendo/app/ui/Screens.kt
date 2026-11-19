package com.stipendo.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stipendo.app.data.CSC_PORTAL_URL
import com.stipendo.app.data.DegreeLevel
import com.stipendo.app.data.Funding
import com.stipendo.app.data.Scholarship
import com.stipendo.app.data.StudyLength
import com.stipendo.app.data.TeachingLanguage
import com.stipendo.app.data.Verification
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// TODO: replace with your real feedback address once stipendo.app email exists.
private const val REPORT_URL = "mailto:hello@example.com?subject=Stipendo%20data%20report"

/** True when a deadline (ISO date) is before today. ISO dates compare correctly as text. */
fun isClosed(deadline: String?): Boolean {
    if (deadline == null) return false
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    return deadline < today
}

/** Whole days from today to an ISO date (0 = today, negative = already passed). Null if no date. */
fun daysUntil(deadline: String?): Int? {
    if (deadline == null) return null
    val target = try { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(deadline) } catch (_: Exception) { null } ?: return null
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    return Math.round((target.time - today.timeInMillis) / 86_400_000.0).toInt()
}

/** True when an ISO date is within the last 7 days (today included). */
fun isRecent(date: String?): Boolean = daysUntil(date)?.let { it in -7..0 } == true

/** Shared by the search page (live count) and the results page. Open deadlines come first. */
fun filterScholarships(
    all: List<Scholarship>,
    degree: DegreeLevel?,
    funding: Funding?,
    language: TeachingLanguage?,
    length: StudyLength?,
): List<Scholarship> = all.filter {
    (degree == null || degree in it.degrees) &&
        (funding == null || it.funding == funding) &&
        (language == null || language in it.languages) &&
        (length == null || length in it.lengths)
}.sortedBy { if (isClosed(it.deadline)) 1 else 0 }

/** Page 1: choose what you want. The button stays pinned to the bottom so it is always visible. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    lang: AppLanguage, onLang: (AppLanguage) -> Unit,
    degree: DegreeLevel?, onDegree: (DegreeLevel?) -> Unit,
    funding: Funding?, onFunding: (Funding?) -> Unit,
    language: TeachingLanguage?, onLanguage: (TeachingLanguage?) -> Unit,
    length: StudyLength?, onLength: (StudyLength?) -> Unit,
    degreeOptions: List<DegreeLevel>,
    lengthOptions: List<StudyLength>,
    fundingOptions: List<Funding>,
    languageOptions: List<TeachingLanguage>,
    matchCount: Int,
    newItems: List<Scholarship>,
    closingSoon: List<Scholarship>,
    savedCount: Int,
    themeMode: ThemeMode, onThemeMode: (ThemeMode) -> Unit,
    onOpenSaved: () -> Unit,
    onOpen: (Scholarship) -> Unit,
    onGo: () -> Unit,
) {
    val t = LocalStrings.current
    val colors = LocalNeu.current
    var showLanguages by rememberSaveable { mutableStateOf(false) }
    val anyFilter = degree != null || length != null || funding != null || language != null

    Column(modifier = Modifier.fillMaxSize()) {
        // Scrollable part
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            // 1. Header: wordmark on the left, round buttons on the right
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StipendoWordmark(fontSize = 30.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        NeuRoundButton(themeMode.icon, description = "Theme: ${themeMode.label}") { onThemeMode(themeMode.next()) }
                        NeuRoundButton("\u6587", description = t.language, selected = showLanguages) { showLanguages = !showLanguages }
                        NeuRoundButton("\u2661", description = t.saved, badge = savedCount, onClick = onOpenSaved)
                    }
                }
                AnimatedVisibility(
                    visible = showLanguages,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Column(modifier = Modifier.padding(vertical = 10.dp)) {
                        NeuCard {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text(t.language, style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
                                NeuOptionGrid(
                                    options = AppLanguage.entries.toList(),
                                    selected = lang,
                                    label = { it?.nativeName ?: "" },
                                    onSelect = { if (it != null) onLang(it) },
                                )
                            }
                        }
                    }
                }
                Text(t.tagline, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
            }

            // 2. Closing soon: a sideways rail of equal cards
            if (closingSoon.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(t.closingSoon, style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        contentPadding = PaddingValues(start = 4.dp, end = 4.dp, top = 12.dp, bottom = 18.dp),
                    ) {
                        items(closingSoon.take(5)) { item ->
                            val days = daysUntil(item.deadline) ?: 0
                            NeuCard(modifier = Modifier.width(164.dp), onClick = { onOpen(item) }) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    NeuGauge(days.toString(), urgent = days <= 45, description = t.daysLeft(days))
                                    Text(
                                        item.university,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = colors.textPrimary,
                                        textAlign = TextAlign.Center,
                                        maxLines = 3,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. New this week: one card, rows separated by grooves
            if (newItems.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(t.newThisWeek, style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
                    NeuCard {
                        newItems.take(3).forEachIndexed { i, item ->
                            if (i > 0) NeuGroove()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(role = Role.Button) { onOpen(item) }
                                    .padding(vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    item.university,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.textPrimary,
                                    modifier = Modifier.weight(1f),
                                )
                                Spacer(Modifier.width(12.dp))
                                NeuTag(t.fundingChip.getValue(item.funding), highlight = item.funding == Funding.FULL)
                            }
                        }
                    }
                }
            }

            // 4. Filters: four groups, every button on the same 3-column grid
            Text(t.chooseTitle, style = MaterialTheme.typography.headlineSmall, color = colors.textPrimary)
            NeuCard {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    FilterGroup(t.degree, degreeOptions, degree, { t.degrees.getValue(it) }, onDegree)
                    FilterGroup(t.duration, lengthOptions, length, { t.lengthLabels.getValue(it) }, onLength)
                    FilterGroup(t.funding, fundingOptions, funding, { t.fundingChip.getValue(it) }, onFunding)
                    FilterGroup(t.teachingLanguage, languageOptions, language, { t.teaching.getValue(it) }, onLanguage)
                }
            }
        }

        // Pinned action bar: reset (only when a filter is on) + the main button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (anyFilter) {
                NeuRoundButton("\u21BA", description = "Reset") {
                    onDegree(null); onLength(null); onFunding(null); onLanguage(null)
                }
            }
            NeuButton(
                text = if (matchCount == 0) t.noMatchButton else t.showResults(matchCount),
                emphasized = matchCount > 0,
                enabled = matchCount > 0,
                onClick = onGo,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** Page 2: the matching scholarships. */
@Composable
fun ResultsScreen(
    results: List<Scholarship>,
    degree: DegreeLevel?,
    funding: Funding?,
    language: TeachingLanguage?,
    length: StudyLength?,
    savedIds: Set<String>,
    listState: LazyListState,
    onEdit: () -> Unit,
    onOpen: (Scholarship) -> Unit,
) {
    val t = LocalStrings.current
    val colors = LocalNeu.current
    val summary = listOfNotNull(
        degree?.let { t.degrees.getValue(it) },
        length?.let { t.lengthLabels.getValue(it) },
        funding?.let { t.fundingChip.getValue(it) },
        language?.let { t.teaching.getValue(it) },
    ).ifEmpty { listOf(t.all) }.joinToString(t.listSeparator)

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(26.dp),
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth(0.6f)) {
                NeuButton(t.editSearch, onClick = onEdit)
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(t.yourSearch, style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
                Text(summary, style = MaterialTheme.typography.headlineSmall, color = colors.textPrimary)
                Text(t.count(results.size), style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
            }
        }
        item {
            NeuCard {
                Text(t.dataNotice, style = MaterialTheme.typography.bodyMedium, color = colors.accent)
            }
        }
        if (results.isEmpty()) {
            item {
                NeuCard {
                    Text(t.noMatch, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                }
            }
        } else {
            items(results, key = { it.id }) { s ->
                ScholarshipCard(s, saved = s.id in savedIds, onClick = { onOpen(s) })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T : Any> FilterGroup(
    title: String,
    options: List<T>,
    selected: T?,
    label: (T) -> String,
    onSelect: (T?) -> Unit,
) {
    val t = LocalStrings.current
    val colors = LocalNeu.current
    if (options.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
        NeuOptionGrid(
            options = listOf<T?>(null) + options,
            selected = selected,
            label = { o -> if (o == null) t.all else label(o) },
            onSelect = onSelect,
        )
    }
}

@Composable
private fun StatusBadge(verification: Verification) {
    val t = LocalStrings.current
    val colors = LocalNeu.current
    val checked = verification == Verification.OFFICIAL_CHECKED
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(12.dp)
                .neuSurface(
                    cornerRadius = 6.dp,
                    elevation = 2.dp,
                    inset = !checked,
                    base = if (checked) colors.accent else null,
                ),
        )
        Text(
            text = if (checked) t.statusChecked else t.statusReported,
            style = MaterialTheme.typography.labelLarge,
            color = if (checked) colors.accent else colors.textSecondary,
        )
    }
}

@Composable
private fun ScholarshipCard(s: Scholarship, saved: Boolean, onClick: () -> Unit) {
    val t = LocalStrings.current
    val colors = LocalNeu.current
    NeuCard(onClick = onClick) {
        Text(s.university, style = MaterialTheme.typography.headlineSmall, color = colors.textPrimary)
        s.universityZh?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            s.city + t.listSeparator + s.degrees.joinToString(t.listSeparator) { t.degrees.getValue(it) },
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
        )
        Spacer(Modifier.height(14.dp))
        Text(t.fundingLine.getValue(s.funding), style = MaterialTheme.typography.titleMedium, color = colors.accent)
        Spacer(Modifier.height(2.dp))
        Text(
            t.deadlineLine(s.deadline ?: t.notConfirmed) + if (isClosed(s.deadline)) " (${t.closed})" else "",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
        )
        Spacer(Modifier.height(16.dp))
        StatusBadge(s.verification)
        val marks = listOfNotNull(
            if (isRecent(s.addedOn)) t.newMark else if (isRecent(s.updatedOn)) t.updatedMark else null,
            if (saved) t.saved else null,
        )
        if (marks.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(marks.joinToString(t.listSeparator), style = MaterialTheme.typography.labelLarge, color = colors.accent)
        }
    }
}

/** The scholarships the student saved. */
@Composable
fun SavedScreen(
    items: List<Scholarship>,
    onBack: () -> Unit,
    onOpen: (Scholarship) -> Unit,
) {
    val t = LocalStrings.current
    val colors = LocalNeu.current
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(26.dp),
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth(0.4f)) { NeuButton(t.back, onClick = onBack) }
        }
        item {
            Text(t.saved, style = MaterialTheme.typography.headlineLarge, color = colors.textPrimary)
        }
        if (items.isEmpty()) {
            item {
                NeuCard { Text(t.noSaved, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary) }
            }
        } else {
            items(items, key = { it.id }) { s ->
                ScholarshipCard(s, saved = true, onClick = { onOpen(s) })
            }
        }
    }
}

@Composable
fun DetailScreen(s: Scholarship, saved: Boolean, onToggleSave: () -> Unit, onBack: () -> Unit) {
    val t = LocalStrings.current
    val colors = LocalNeu.current
    val uriHandler = LocalUriHandler.current

    fun yesNo(value: Boolean?) = when (value) {
        true -> t.covered
        false -> t.notCovered
        null -> t.notStated
    }

    val stipend = if (s.stipendRmbByDegree.isEmpty()) {
        t.notStated
    } else {
        s.stipendRmbByDegree.entries.joinToString("\n") {
            "${t.degrees.getValue(it.key)}: RMB ${String.format(Locale.US, "%,d", it.value)}"
        }
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(26.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth(0.4f)) {
            NeuButton(t.back, onClick = onBack)
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(s.university, style = MaterialTheme.typography.headlineLarge, color = colors.textPrimary)
            s.universityZh?.let {
                Text(it, style = MaterialTheme.typography.titleMedium, color = colors.textSecondary)
            }
            Text(s.name, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
        }

        NeuCard {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Fact(t.city, s.city)
                Fact(t.degree, s.degrees.joinToString(t.listSeparator) { t.degrees.getValue(it) })
                Fact(
                    t.teachingLanguage,
                    if (s.languages.isEmpty()) t.notStated
                    else s.languages.joinToString(t.listSeparator) { t.teaching.getValue(it) },
                )
                if (s.lengths.isNotEmpty()) {
                    Fact(t.duration, s.lengths.joinToString(t.listSeparator) { t.lengthLabels.getValue(it) })
                }
                Fact(t.funding, t.fundingLine.getValue(s.funding))
                Fact(
                    t.tuition,
                    s.tuitionRmbPerYear?.let { t.perYear(String.format(Locale.US, "%,d", it)) } ?: yesNo(s.coversTuition),
                )
                Fact(t.housing, yesNo(s.coversHousing))
                Fact(t.monthlyStipend, stipend)
                Fact(t.languageRequirement, s.languageRequirement ?: t.notStated)
            }
        }

        NeuCard {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Fact(
                    t.deadline,
                    s.deadline?.let { it + if (isClosed(it)) " (${t.closed})" else "" } ?: t.notConfirmed,
                )
                Fact(t.cscDeadline, s.cscDeadline ?: t.notStated)
                Fact(t.agencyNumber, s.cscAgencyNumber ?: t.notStated)
                Fact(t.provider, s.provider)
                StatusBadge(s.verification)
                Fact(t.lastChecked, s.lastChecked ?: t.notCheckedYet)
                Text(t.confirmNote, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
            val canRemind = s.deadline != null && !isClosed(s.deadline)
            NeuButton(
                text = when {
                    saved -> t.unsave
                    canRemind -> t.saveWithReminder
                    else -> t.save
                },
                emphasized = !saved,
                onClick = onToggleSave,
            )
            if (s.officialUrl != null) {
                NeuButton(t.openOfficial, onClick = { uriHandler.openUri(s.officialUrl) })
            } else {
                Text(t.noOfficialYet, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
            }
            NeuButton(t.openCsc, onClick = { uriHandler.openUri(CSC_PORTAL_URL) })
            NeuButton(t.reportWrong, onClick = { uriHandler.openUri(REPORT_URL) })
        }
    }
}

@Composable
private fun Fact(label: String, value: String) {
    val colors = LocalNeu.current
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
        Text(value, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
    }
}
