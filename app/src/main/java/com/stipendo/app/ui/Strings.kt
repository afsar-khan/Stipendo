package com.stipendo.app.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.stipendo.app.data.DegreeLevel
import com.stipendo.app.data.Funding
import com.stipendo.app.data.StudyLength
import com.stipendo.app.data.TeachingLanguage

enum class AppLanguage(val code: String, val nativeName: String, val rtl: Boolean) {
    EN("en", "English", false),
    UR("ur", "اردو", true),
    AR("ar", "العربية", true),
    RU("ru", "Русский", false),
    FR("fr", "Français", false);

    companion object {
        fun fromCode(code: String?): AppLanguage = entries.firstOrNull { it.code == code } ?: EN
    }
}

data class Strings(
    val tagline: String,
    val chooseTitle: String,
    val showResults: (Int) -> String,
    val noMatchButton: String,
    val editSearch: String,
    val yourSearch: String,
    val duration: String,
    val lengthLabels: Map<StudyLength, String>,
    val closed: String,
    val perYear: (String) -> String,
    val save: String,
    val saveWithReminder: String,
    val unsave: String,
    val saved: String,
    val noSaved: String,
    val closingSoon: String,
    val daysLeft: (Int) -> String,
    val reminderText: (Int, String) -> String,
    val channelName: String,
    val newMark: String,
    val updatedMark: String,
    val newThisWeek: String,
    val dataNotice: String,
    val degree: String,
    val funding: String,
    val teachingLanguage: String,
    val all: String,
    val count: (Int) -> String,
    val noMatch: String,
    val back: String,
    val openOfficial: String,
    val openCsc: String,
    val noOfficialYet: String,
    val reportWrong: String,
    val language: String,
    val city: String,
    val tuition: String,
    val housing: String,
    val covered: String,
    val notCovered: String,
    val monthlyStipend: String,
    val languageRequirement: String,
    val deadline: String,
    val cscDeadline: String,
    val agencyNumber: String,
    val provider: String,
    val lastChecked: String,
    val notStated: String,
    val notConfirmed: String,
    val notCheckedYet: String,
    val confirmNote: String,
    val statusChecked: String,
    val statusReported: String,
    val listSeparator: String,
    val deadlineLine: (String) -> String,
    val degrees: Map<DegreeLevel, String>,
    val fundingChip: Map<Funding, String>,
    val fundingLine: Map<Funding, String>,
    val teaching: Map<TeachingLanguage, String>,
)

val LocalStrings = staticCompositionLocalOf { EnglishStrings }

fun stringsFor(lang: AppLanguage): Strings = when (lang) {
    AppLanguage.EN -> EnglishStrings
    AppLanguage.UR -> UrduStrings
    AppLanguage.AR -> ArabicStrings
    AppLanguage.RU -> RussianStrings
    AppLanguage.FR -> FrenchStrings
}

// Translations below are a first draft: have a native speaker review each before release.

val EnglishStrings = Strings(
    tagline = "Scholarships at Chinese universities. Free, with links to official pages only.",
    chooseTitle = "What are you looking for?",
    showResults = { n -> if (n == 1) "Show 1 scholarship" else "Show $n scholarships" },
    noMatchButton = "No matches. Change your choices.",
    editSearch = "Edit search",
    yourSearch = "Your search",
    duration = "Duration",
    lengthLabels = mapOf(StudyLength.SIX_MONTHS to "6 months", StudyLength.ONE_YEAR to "1 year"),
    closed = "Closed",
    perYear = { x -> "RMB $x per year" },
    save = "Save",
    saveWithReminder = "Save and remind me before the deadline",
    unsave = "Remove from saved",
    saved = "Saved",
    noSaved = "Nothing saved yet. Open a scholarship and press Save.",
    closingSoon = "Closing soon",
    daysLeft = { n -> when (n) { 0 -> "Last day today"; 1 -> "1 day left"; else -> "$n days left" } },
    reminderText = { days, name -> if (days == 1) "Closes tomorrow: $name" else "Closes in $days days: $name" },
    channelName = "Deadline reminders",
    newMark = "New",
    updatedMark = "Updated",
    newThisWeek = "New this week",
    dataNotice = "Some listings are not yet checked on the official page and are marked as such. Always confirm there before applying.",
    degree = "Degree",
    funding = "Funding",
    teachingLanguage = "Teaching language",
    all = "All",
    count = { n -> if (n == 1) "1 scholarship" else "$n scholarships" },
    noMatch = "Nothing matches these filters. Choose All on one of them to widen the search.",
    back = "Back",
    openOfficial = "Open official page",
    openCsc = "CSC application website",
    noOfficialYet = "Official page not added yet. Check the university’s international admissions website.",
    reportWrong = "Report wrong info",
    language = "Language",
    city = "City",
    tuition = "Tuition",
    housing = "Housing",
    covered = "Covered",
    notCovered = "Not covered",
    monthlyStipend = "Monthly stipend",
    languageRequirement = "Language requirement",
    deadline = "Application deadline",
    cscDeadline = "CSC website deadline",
    agencyNumber = "CSC agency number",
    provider = "Provider",
    lastChecked = "Last checked",
    notStated = "Not stated",
    notConfirmed = "Not confirmed",
    notCheckedYet = "Not checked yet",
    confirmNote = "Always confirm details on the official page before applying.",
    statusChecked = "Checked on official page",
    statusReported = "Not yet checked on official page",
    listSeparator = ", ",
    deadlineLine = { d -> "Deadline $d" },
    degrees = mapOf(
        DegreeLevel.BACHELOR to "Bachelor", DegreeLevel.MASTER to "Master",
        DegreeLevel.PHD to "PhD", DegreeLevel.LANGUAGE to "Language course",
    ),
    fundingChip = mapOf(Funding.FULL to "Full", Funding.PARTIAL to "Partial", Funding.VARIES to "Varies", Funding.SELF_FUNDED to "Self-funded"),
    fundingLine = mapOf(Funding.FULL to "Full funding", Funding.PARTIAL to "Partial funding", Funding.VARIES to "Funding varies", Funding.SELF_FUNDED to "Self-funded (you pay the tuition)"),
    teaching = mapOf(TeachingLanguage.ENGLISH to "English-taught", TeachingLanguage.CHINESE to "Chinese-taught"),
)

val UrduStrings = Strings(
    tagline = "چینی جامعات میں اسکالرشپس تلاش کریں۔ مفت، اور صرف سرکاری صفحات کے لنکس کے ساتھ۔",
    chooseTitle = "آپ کیا تلاش کر رہے ہیں؟",
    showResults = { n -> if (n == 1) "1 اسکالرشپ دکھائیں" else "$n اسکالرشپس دکھائیں" },
    noMatchButton = "کوئی نتیجہ نہیں۔ انتخاب بدلیں۔",
    editSearch = "تلاش میں ترمیم کریں",
    yourSearch = "آپ کی تلاش",
    duration = "مدت",
    lengthLabels = mapOf(StudyLength.SIX_MONTHS to "6 ماہ", StudyLength.ONE_YEAR to "1 سال"),
    closed = "بند",
    perYear = { x -> "سالانہ RMB $x" },
    save = "محفوظ کریں",
    saveWithReminder = "محفوظ کریں اور آخری تاریخ سے پہلے یاد دہانی دیں",
    unsave = "محفوظ شدہ سے ہٹائیں",
    saved = "محفوظ شدہ",
    noSaved = "ابھی کچھ محفوظ نہیں۔ کوئی اسکالرشپ کھولیں اور «محفوظ کریں» دبائیں۔",
    closingSoon = "جلد بند ہونے والی",
    daysLeft = { n -> when (n) { 0 -> "آج آخری دن ہے"; 1 -> "1 دن باقی"; else -> "$n دن باقی" } },
    reminderText = { days, name -> if (days == 1) "کل بند ہو رہی ہے: $name" else "$days دن میں بند ہو رہی ہے: $name" },
    channelName = "آخری تاریخ کی یاد دہانیاں",
    newMark = "نیا",
    updatedMark = "اپ ڈیٹ ہوا",
    newThisWeek = "اس ہفتے نیا",
    dataNotice = "کچھ اندراجات ابھی سرکاری صفحے پر چیک نہیں ہوئے اور ان پر اسی طرح نشان لگا ہے۔ درخواست دینے سے پہلے وہاں ضرور تصدیق کریں۔",
    degree = "ڈگری",
    funding = "فنڈنگ",
    teachingLanguage = "ذریعۂ تعلیم",
    all = "سب",
    count = { n -> if (n == 1) "1 اسکالرشپ" else "$n اسکالرشپس" },
    noMatch = "ان فلٹرز سے کوئی نتیجہ نہیں ملا۔ تلاش وسیع کرنے کے لیے کسی فلٹر میں «سب» منتخب کریں۔",
    back = "واپس",
    openOfficial = "سرکاری صفحہ کھولیں",
    openCsc = "CSC درخواست ویب سائٹ",
    noOfficialYet = "سرکاری صفحہ ابھی شامل نہیں ہوا۔ یونیورسٹی کی انٹرنیشنل ایڈمیشن ویب سائٹ دیکھیں۔",
    reportWrong = "غلط معلومات کی اطلاع دیں",
    language = "زبان",
    city = "شہر",
    tuition = "ٹیوشن فیس",
    housing = "رہائش",
    covered = "شامل",
    notCovered = "شامل نہیں",
    monthlyStipend = "ماہانہ وظیفہ",
    languageRequirement = "زبان کی شرط",
    deadline = "درخواست کی آخری تاریخ",
    cscDeadline = "CSC ویب سائٹ کی آخری تاریخ",
    agencyNumber = "CSC ایجنسی نمبر",
    provider = "فراہم کنندہ",
    lastChecked = "آخری بار چیک کیا گیا",
    notStated = "درج نہیں",
    notConfirmed = "غیر تصدیق شدہ",
    notCheckedYet = "ابھی چیک نہیں ہوا",
    confirmNote = "درخواست دینے سے پہلے سرکاری صفحے پر تفصیلات ضرور دیکھ لیں۔",
    statusChecked = "سرکاری صفحے پر چیک کیا گیا",
    statusReported = "سرکاری صفحے پر ابھی چیک نہیں ہوا",
    listSeparator = "، ",
    deadlineLine = { d -> "آخری تاریخ: $d" },
    degrees = mapOf(
        DegreeLevel.BACHELOR to "بیچلر", DegreeLevel.MASTER to "ماسٹرز",
        DegreeLevel.PHD to "پی ایچ ڈی", DegreeLevel.LANGUAGE to "زبان کا کورس",
    ),
    fundingChip = mapOf(Funding.FULL to "مکمل", Funding.PARTIAL to "جزوی", Funding.VARIES to "مختلف", Funding.SELF_FUNDED to "خود فنانس"),
    fundingLine = mapOf(Funding.FULL to "مکمل فنڈنگ", Funding.PARTIAL to "جزوی فنڈنگ", Funding.VARIES to "فنڈنگ مختلف ہے", Funding.SELF_FUNDED to "خود فنانس (فیس آپ ادا کرتے ہیں)"),
    teaching = mapOf(TeachingLanguage.ENGLISH to "انگریزی میں", TeachingLanguage.CHINESE to "چینی میں"),
)

private fun arabicCount(n: Int) = when {
    n == 1 -> "منحة واحدة"
    n == 2 -> "منحتان"
    n in 3..10 -> "$n منح"
    else -> "$n منحة"
}

val ArabicStrings = Strings(
    tagline = "ابحث عن منح دراسية في الجامعات الصينية. مجاني، مع روابط الصفحات الرسمية فقط.",
    chooseTitle = "ماذا تبحث عنه؟",
    showResults = { n ->
        when {
            n == 1 -> "عرض منحة واحدة"
            n == 2 -> "عرض منحتين"
            n in 3..10 -> "عرض $n منح"
            else -> "عرض $n منحة"
        }
    },
    noMatchButton = "لا توجد نتائج. غيّر اختياراتك.",
    editSearch = "تعديل البحث",
    yourSearch = "بحثك",
    duration = "المدة",
    lengthLabels = mapOf(StudyLength.SIX_MONTHS to "6 أشهر", StudyLength.ONE_YEAR to "سنة واحدة"),
    closed = "مغلق",
    perYear = { x -> "$x RMB سنويًا" },
    save = "حفظ",
    saveWithReminder = "احفظ وذكّرني قبل الموعد النهائي",
    unsave = "إزالة من المحفوظات",
    saved = "المحفوظات",
    noSaved = "لا شيء محفوظ بعد. افتح منحة واضغط «حفظ».",
    closingSoon = "تغلق قريبًا",
    daysLeft = { n ->
        when {
            n == 0 -> "آخر يوم اليوم"
            n == 1 -> "بقي يوم واحد"
            n == 2 -> "بقي يومان"
            n in 3..10 -> "بقيت $n أيام"
            else -> "بقي $n يومًا"
        }
    },
    reminderText = { days, name ->
        when {
            days == 1 -> "تغلق غدًا: $name"
            days == 2 -> "تغلق بعد يومين: $name"
            days in 3..10 -> "تغلق خلال $days أيام: $name"
            else -> "تغلق خلال $days يومًا: $name"
        }
    },
    channelName = "تذكيرات المواعيد النهائية",
    newMark = "جديد",
    updatedMark = "محدَّث",
    newThisWeek = "جديد هذا الأسبوع",
    dataNotice = "بعض القوائم لم يُتحقق منها بعد في الصفحة الرسمية، وهي مُعلَّمة بذلك. تأكد منها هناك قبل التقديم.",
    degree = "الدرجة العلمية",
    funding = "التمويل",
    teachingLanguage = "لغة التدريس",
    all = "الكل",
    count = ::arabicCount,
    noMatch = "لا توجد نتائج مطابقة لهذه المرشحات. اختر «الكل» في أحدها لتوسيع البحث.",
    back = "رجوع",
    openOfficial = "افتح الصفحة الرسمية",
    openCsc = "موقع التقديم على CSC",
    noOfficialYet = "لم تتم إضافة الصفحة الرسمية بعد. راجع موقع القبول الدولي للجامعة.",
    reportWrong = "الإبلاغ عن معلومات خاطئة",
    language = "اللغة",
    city = "المدينة",
    tuition = "الرسوم الدراسية",
    housing = "السكن",
    covered = "مشمول",
    notCovered = "غير مشمول",
    monthlyStipend = "المخصص الشهري",
    languageRequirement = "شرط اللغة",
    deadline = "الموعد النهائي للتقديم",
    cscDeadline = "الموعد النهائي على موقع CSC",
    agencyNumber = "رقم الجهة في CSC",
    provider = "الجهة المانحة",
    lastChecked = "آخر تحقق",
    notStated = "غير مذكور",
    notConfirmed = "غير مؤكد",
    notCheckedYet = "لم يُتحقق منه بعد",
    confirmNote = "تأكد دائمًا من التفاصيل في الصفحة الرسمية قبل التقديم.",
    statusChecked = "تم التحقق منه في الصفحة الرسمية",
    statusReported = "لم يُتحقق منه بعد في الصفحة الرسمية",
    listSeparator = "، ",
    deadlineLine = { d -> "الموعد النهائي: $d" },
    degrees = mapOf(
        DegreeLevel.BACHELOR to "بكالوريوس", DegreeLevel.MASTER to "ماجستير",
        DegreeLevel.PHD to "دكتوراه", DegreeLevel.LANGUAGE to "دورة لغة",
    ),
    fundingChip = mapOf(Funding.FULL to "كامل", Funding.PARTIAL to "جزئي", Funding.VARIES to "متفاوت", Funding.SELF_FUNDED to "تمويل ذاتي"),
    fundingLine = mapOf(Funding.FULL to "تمويل كامل", Funding.PARTIAL to "تمويل جزئي", Funding.VARIES to "التمويل متفاوت", Funding.SELF_FUNDED to "تمويل ذاتي (تدفع الرسوم بنفسك)"),
    teaching = mapOf(TeachingLanguage.ENGLISH to "بالإنجليزية", TeachingLanguage.CHINESE to "بالصينية"),
)

private fun russianCount(n: Int): String {
    val m10 = n % 10
    val m100 = n % 100
    val word = when {
        m10 == 1 && m100 != 11 -> "стипендия"
        m10 in 2..4 && m100 !in 12..14 -> "стипендии"
        else -> "стипендий"
    }
    return "$n $word"
}

private fun russianShow(n: Int): String {
    val m10 = n % 10
    val m100 = n % 100
    val word = when {
        m10 == 1 && m100 != 11 -> "стипендию"
        m10 in 2..4 && m100 !in 12..14 -> "стипендии"
        else -> "стипендий"
    }
    return "Показать $n $word"
}

val RussianStrings = Strings(
    tagline = "Стипендии в китайских университетах. Бесплатно, со ссылками только на официальные страницы.",
    chooseTitle = "Что вы ищете?",
    showResults = ::russianShow,
    noMatchButton = "Ничего не найдено. Измените выбор.",
    editSearch = "Изменить поиск",
    yourSearch = "Ваш поиск",
    duration = "Срок обучения",
    lengthLabels = mapOf(StudyLength.SIX_MONTHS to "6 месяцев", StudyLength.ONE_YEAR to "1 год"),
    closed = "Закрыто",
    perYear = { x -> "$x RMB в год" },
    save = "Сохранить",
    saveWithReminder = "Сохранить и напомнить перед сроком",
    unsave = "Убрать из сохранённых",
    saved = "Сохранённые",
    noSaved = "Пока ничего не сохранено. Откройте стипендию и нажмите «Сохранить».",
    closingSoon = "Скоро закрываются",
    daysLeft = { n -> if (n == 0) "Сегодня последний день" else "Осталось: $n дн." },
    reminderText = { days, name -> if (days == 1) "Срок истекает завтра: $name" else "Срок истекает через $days дн.: $name" },
    channelName = "Напоминания о сроках",
    newMark = "Новое",
    updatedMark = "Обновлено",
    newThisWeek = "Новое за неделю",
    dataNotice = "Некоторые записи ещё не проверены на официальной странице и помечены. Перед подачей заявки обязательно уточните данные там.",
    degree = "Степень",
    funding = "Финансирование",
    teachingLanguage = "Язык обучения",
    all = "Все",
    count = ::russianCount,
    noMatch = "По этим фильтрам ничего не найдено. Выберите «Все» в одном из них, чтобы расширить поиск.",
    back = "Назад",
    openOfficial = "Открыть официальную страницу",
    openCsc = "Сайт подачи заявок CSC",
    noOfficialYet = "Официальная страница ещё не добавлена. Проверьте сайт приёма иностранных студентов вуза.",
    reportWrong = "Сообщить об ошибке",
    language = "Язык",
    city = "Город",
    tuition = "Обучение",
    housing = "Жильё",
    covered = "Оплачивается",
    notCovered = "Не оплачивается",
    monthlyStipend = "Ежемесячная стипендия",
    languageRequirement = "Требования к языку",
    deadline = "Срок подачи заявки",
    cscDeadline = "Срок подачи на сайте CSC",
    agencyNumber = "Номер агентства CSC",
    provider = "Организатор",
    lastChecked = "Последняя проверка",
    notStated = "Не указано",
    notConfirmed = "Не подтверждено",
    notCheckedYet = "Ещё не проверено",
    confirmNote = "Перед подачей заявки всегда проверяйте детали на официальной странице.",
    statusChecked = "Проверено на официальной странице",
    statusReported = "Ещё не проверено на официальной странице",
    listSeparator = ", ",
    deadlineLine = { d -> "Срок: $d" },
    degrees = mapOf(
        DegreeLevel.BACHELOR to "Бакалавриат", DegreeLevel.MASTER to "Магистратура",
        DegreeLevel.PHD to "Докторантура", DegreeLevel.LANGUAGE to "Языковые курсы",
    ),
    fundingChip = mapOf(Funding.FULL to "Полное", Funding.PARTIAL to "Частичное", Funding.VARIES to "По-разному", Funding.SELF_FUNDED to "За свой счёт"),
    fundingLine = mapOf(Funding.FULL to "Полное финансирование", Funding.PARTIAL to "Частичное финансирование", Funding.VARIES to "Финансирование зависит от программы", Funding.SELF_FUNDED to "За свой счёт (вы платите за обучение)"),
    teaching = mapOf(TeachingLanguage.ENGLISH to "На английском", TeachingLanguage.CHINESE to "На китайском"),
)

val FrenchStrings = Strings(
    tagline = "Bourses dans les universités chinoises. Gratuit, avec des liens vers les pages officielles uniquement.",
    chooseTitle = "Que recherchez-vous ?",
    showResults = { n -> if (n <= 1) "Afficher $n bourse" else "Afficher $n bourses" },
    noMatchButton = "Aucun résultat. Modifiez vos choix.",
    editSearch = "Modifier la recherche",
    yourSearch = "Votre recherche",
    duration = "Durée",
    lengthLabels = mapOf(StudyLength.SIX_MONTHS to "6 mois", StudyLength.ONE_YEAR to "1 an"),
    closed = "Clôturé",
    perYear = { x -> "$x RMB par an" },
    save = "Enregistrer",
    saveWithReminder = "Enregistrer et me rappeler avant la date limite",
    unsave = "Retirer des enregistrées",
    saved = "Enregistrées",
    noSaved = "Rien d’enregistré pour l’instant. Ouvrez une bourse et appuyez sur « Enregistrer ».",
    closingSoon = "Bientôt clôturées",
    daysLeft = { n -> when (n) { 0 -> "Dernier jour aujourd’hui"; 1 -> "1 jour restant"; else -> "$n jours restants" } },
    reminderText = { days, name -> if (days == 1) "Clôture demain : $name" else "Clôture dans $days jours : $name" },
    channelName = "Rappels de dates limites",
    newMark = "Nouveau",
    updatedMark = "Mis à jour",
    newThisWeek = "Nouveautés de la semaine",
    dataNotice = "Certaines fiches ne sont pas encore vérifiées sur la page officielle et sont signalées comme telles. Confirmez-les toujours là-bas avant de postuler.",
    degree = "Niveau",
    funding = "Financement",
    teachingLanguage = "Langue d’enseignement",
    all = "Tous",
    count = { n -> if (n <= 1) "$n bourse" else "$n bourses" },
    noMatch = "Aucun résultat avec ces filtres. Choisissez « Tous » dans l’un d’eux pour élargir la recherche.",
    back = "Retour",
    openOfficial = "Ouvrir la page officielle",
    openCsc = "Site de candidature du CSC",
    noOfficialYet = "Page officielle pas encore ajoutée. Consultez le site d’admission internationale de l’université.",
    reportWrong = "Signaler une erreur",
    language = "Langue",
    city = "Ville",
    tuition = "Frais de scolarité",
    housing = "Logement",
    covered = "Pris en charge",
    notCovered = "Non pris en charge",
    monthlyStipend = "Allocation mensuelle",
    languageRequirement = "Niveau de langue requis",
    deadline = "Date limite de candidature",
    cscDeadline = "Date limite sur le site du CSC",
    agencyNumber = "Numéro d’agence CSC",
    provider = "Organisme",
    lastChecked = "Dernière vérification",
    notStated = "Non précisé",
    notConfirmed = "Non confirmé",
    notCheckedYet = "Pas encore vérifié",
    confirmNote = "Confirmez toujours les détails sur la page officielle avant de postuler.",
    statusChecked = "Vérifié sur la page officielle",
    statusReported = "Pas encore vérifié sur la page officielle",
    listSeparator = ", ",
    deadlineLine = { d -> "Date limite : $d" },
    degrees = mapOf(
        DegreeLevel.BACHELOR to "Licence", DegreeLevel.MASTER to "Master",
        DegreeLevel.PHD to "Doctorat", DegreeLevel.LANGUAGE to "Cours de langue",
    ),
    fundingChip = mapOf(Funding.FULL to "Complet", Funding.PARTIAL to "Partiel", Funding.VARIES to "Variable", Funding.SELF_FUNDED to "Autofinancé"),
    fundingLine = mapOf(Funding.FULL to "Financement complet", Funding.PARTIAL to "Financement partiel", Funding.VARIES to "Financement variable", Funding.SELF_FUNDED to "Autofinancé (vous payez les frais)"),
    teaching = mapOf(TeachingLanguage.ENGLISH to "En anglais", TeachingLanguage.CHINESE to "En chinois"),
)
