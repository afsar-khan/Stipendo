package com.stipendo.app.data

enum class DegreeLevel { BACHELOR, MASTER, PHD, LANGUAGE }

enum class Funding { FULL, PARTIAL, VARIES, SELF_FUNDED }

enum class TeachingLanguage { ENGLISH, CHINESE }

/** For language programs: about 6 months (one semester) or about 1 year. Empty for degree programs. */
enum class StudyLength { SIX_MONTHS, ONE_YEAR }

/**
 * How far an entry can be trusted:
 *  - OFFICIAL_CHECKED: every field was read on the university's own official page
 *  - REPORTED: taken from a third-party guide, NOT yet confirmed on the official page
 * The app shows this on every card, so students always know which one they are looking at.
 */
enum class Verification { OFFICIAL_CHECKED, REPORTED }

/**
 * One scholarship at one university (mirrors the "Scholarships" spreadsheet table).
 * Null / empty means "not stated": the UI says so instead of guessing.
 */
data class Scholarship(
    val id: String,
    val university: String,
    val universityZh: String?,
    val city: String,
    val name: String,
    val provider: String,
    val degrees: List<DegreeLevel>,
    val languages: List<TeachingLanguage>,          // empty = not stated
    val lengths: List<StudyLength>,                 // language programs only; empty otherwise
    val funding: Funding,
    val coversTuition: Boolean?,
    val coversHousing: Boolean?,
    val tuitionRmbPerYear: Int?,                    // fee-paying programs; null = not stated
    val stipendRmbByDegree: Map<DegreeLevel, Int>,  // empty = not stated
    val languageRequirement: String?,
    val deadline: String?,                          // ISO date, university application deadline
    val cscDeadline: String?,                       // ISO date, second step on the CSC website
    val cscAgencyNumber: String?,
    val officialUrl: String?,                       // the university's official page, if confirmed
    val sourceUrl: String,                          // where the facts came from
    val verification: Verification,
    val lastChecked: String?,                       // ISO date of the last official-page check
    val addedOn: String? = null,                    // set by csv_to_json.py; drives the "New" mark
    val updatedOn: String? = null,                  // set by csv_to_json.py; drives the "Updated" mark
)

const val CSC_PORTAL_URL = "https://studyinchina.csc.edu.cn/"
