package com.example.moderato

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

class EmotionArchiveActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var thisWeekCard: LinearLayout
    private lateinit var tvWeeklyPeriod: TextView
    private lateinit var tvWeeklyScore: TextView
    private lateinit var tvWeeklyChord: TextView
    private lateinit var weeklyStaffContainer: LinearLayout
    private lateinit var btnWeeklyArchive: Button
    private lateinit var btnMonthlyArchive: Button
    private lateinit var recentArchivesContainer: LinearLayout
    private lateinit var tvEmptyMessage: TextView

    private lateinit var fileManager: EmotionFileManager
    private lateinit var chordAnalyzer: EmotionChordAnalyzer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emotion_archive)

        fileManager = EmotionFileManager(this)
        chordAnalyzer = EmotionChordAnalyzer()

        initViews()
        setupClickListeners()
        loadArchiveData()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        thisWeekCard = findViewById(R.id.thisWeekCard)
        tvWeeklyPeriod = findViewById(R.id.tvWeeklyPeriod)
        tvWeeklyScore = findViewById(R.id.tvWeeklyScore)
        tvWeeklyChord = findViewById(R.id.tvWeeklyChord)
        weeklyStaffContainer = findViewById(R.id.weeklyStaffContainer)
        btnWeeklyArchive = findViewById(R.id.btnWeeklyArchive)
        btnMonthlyArchive = findViewById(R.id.btnMonthlyArchive)
        recentArchivesContainer = findViewById(R.id.recentArchivesContainer)
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        thisWeekCard.setOnClickListener {
            openWeeklyDetail(getCurrentWeek())
        }

        btnWeeklyArchive.setOnClickListener {
            openWeeklyArchive()
        }

        btnMonthlyArchive.setOnClickListener {
            openMonthlyArchive()
        }
    }

    private fun loadArchiveData() {
        // 수업 8주차 - 파일 처리로 이번 주 데이터 로드
        val thisWeekData = loadThisWeekData()
        displayThisWeekCard(thisWeekData)

        // 최근 기록들 로드
        val recentArchives = loadRecentArchives()
        displayRecentArchives(recentArchives)
    }

    private fun loadThisWeekData(): WeeklyEmotionData {
        val calendar = Calendar.getInstance()
        val emotions = mutableListOf<EmotionRecord>()

        // 이번 주 월요일부터 일요일까지 데이터 수집
        val startOfWeek = getStartOfWeek(calendar)

        for (i in 0..6) {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(startOfWeek.time)
            val dailyEmotions = fileManager.loadEmotionsByDate(date)
            emotions.addAll(dailyEmotions)
            startOfWeek.add(Calendar.DAY_OF_MONTH, 1)
        }

        // 주간 감정 분석
        val weeklyChord = chordAnalyzer.analyzeEmotions(emotions)
        val weeklyScore = calculateWeeklyScore(emotions)

        return WeeklyEmotionData(
            period = getCurrentWeekPeriod(),
            emotions = emotions,
            chord = weeklyChord,
            score = weeklyScore
        )
    }

    private fun displayThisWeekCard(weekData: WeeklyEmotionData) {
        tvWeeklyPeriod.text = weekData.period
        tvWeeklyScore.text = "⭐ ${"%.1f".format(weekData.score)}"

        if (weekData.emotions.isNotEmpty()) {
            tvWeeklyChord.text = "${weekData.chord.chordName} - ${getChordDescription(weekData.chord.chordName)}"

            // 미니 악보 표시
            displayMiniStaff(weekData.emotions)
        } else {
            tvWeeklyChord.text = "아직 이번 주 감정이 기록되지 않았어요"
            weeklyStaffContainer.removeAllViews()

            val emptyText = TextView(this).apply {
                text = "🎵 감정을 기록하면\n이번 주의 선율이 나타납니다"
                textSize = 14f
                setTextColor(ContextCompat.getColor(this@EmotionArchiveActivity, R.color.text_secondary))
                gravity = android.view.Gravity.CENTER
            }
            weeklyStaffContainer.addView(emptyText)
        }
    }

    private fun displayMiniStaff(emotions: List<EmotionRecord>) {
        weeklyStaffContainer.removeAllViews()

        // 수업 9주차 - 커스텀 뷰 활용한 미니 악보
        val miniStaffView = EmotionMiniStaffView(this)
        miniStaffView.setEmotions(emotions)

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        miniStaffView.layoutParams = layoutParams

        weeklyStaffContainer.addView(miniStaffView)
    }

    private fun loadRecentArchives(): List<ArchiveItem> {
        val archives = mutableListOf<ArchiveItem>()
        val savedDates = fileManager.getAllSavedDates()

        // 최근 10개 날짜의 데이터를 주간/월간으로 그룹화
        val recentWeeks = getRecentWeeks(4) // 최근 4주
        val recentMonths = getRecentMonths(3) // 최근 3개월

        // 주간 아카이브 추가
        recentWeeks.forEach { weekPeriod ->
            val weekEmotions = getWeekEmotions(weekPeriod)
            if (weekEmotions.isNotEmpty()) {
                archives.add(ArchiveItem(
                    type = ArchiveType.WEEKLY,
                    period = weekPeriod,
                    emotionCount = weekEmotions.size,
                    dominantEmotion = findDominantEmotion(weekEmotions),
                    score = calculateWeeklyScore(weekEmotions)
                ))
            }
        }

        // 월간 아카이브 추가 (최근 순으로 정렬)
        recentMonths.forEach { monthPeriod ->
            val monthEmotions = getMonthEmotions(monthPeriod)
            if (monthEmotions.isNotEmpty()) {
                archives.add(ArchiveItem(
                    type = ArchiveType.MONTHLY,
                    period = monthPeriod,
                    emotionCount = monthEmotions.size,
                    dominantEmotion = findDominantEmotion(monthEmotions),
                    score = calculateMonthlyScore(monthEmotions)
                ))
            }
        }

        return archives.sortedByDescending { it.period }
    }

    private fun displayRecentArchives(archives: List<ArchiveItem>) {
        recentArchivesContainer.removeAllViews()

        if (archives.isEmpty()) {
            tvEmptyMessage.visibility = View.VISIBLE
            return
        }

        tvEmptyMessage.visibility = View.GONE

        archives.forEach { archive ->
            val archiveCard = createArchiveCard(archive)
            recentArchivesContainer.addView(archiveCard)
        }
    }

    private fun createArchiveCard(archive: ArchiveItem): LinearLayout {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            background = ContextCompat.getDrawable(this@EmotionArchiveActivity, R.drawable.card_background)
            setPadding(20, 16, 20, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
        }

        // 아이콘
        val iconText = TextView(this).apply {
            text = if (archive.type == ArchiveType.WEEKLY) "📅" else "🌙"
            textSize = 24f
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 0, 16, 0)
            }
        }

        // 내용 컨테이너
        val contentContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        // 제목
        val titleText = TextView(this).apply {
            text = formatArchiveTitle(archive)
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@EmotionArchiveActivity, R.color.text_primary))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        // 부제목
        val subtitleText = TextView(this).apply {
            text = "${archive.emotionCount}개 감정 • 주요: ${archive.dominantEmotion}"
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@EmotionArchiveActivity, R.color.text_secondary))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 4, 0, 0)
            }
        }

        // 점수
        val scoreText = TextView(this).apply {
            text = "⭐ ${"%.1f".format(archive.score)}"
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@EmotionArchiveActivity, R.color.secondary_orange))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            background = ContextCompat.getDrawable(this@EmotionArchiveActivity, R.drawable.score_badge_bg)
            setPadding(12, 8, 12, 8)
        }

        contentContainer.addView(titleText)
        contentContainer.addView(subtitleText)

        container.addView(iconText)
        container.addView(contentContainer)
        container.addView(scoreText)

        // 클릭 이벤트
        container.setOnClickListener {
            when (archive.type) {
                ArchiveType.WEEKLY -> openWeeklyDetail(archive.period)
                ArchiveType.MONTHLY -> openMonthlyDetail(archive.period)
            }
        }

        return container
    }

    // 헬퍼 메서드들
    private fun getCurrentWeek(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH)
        return "${year}-${month.toString().padStart(2, '0')}-W${weekOfMonth}"
    }

    private fun getCurrentWeekPeriod(): String {
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH) + 1
        val weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH)
        val year = calendar.get(Calendar.YEAR)
        return "${year}년 ${month}월 ${weekOfMonth}주차"
    }

    private fun getStartOfWeek(calendar: Calendar): Calendar {
        val startOfWeek = calendar.clone() as Calendar
        startOfWeek.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        return startOfWeek
    }

    private fun calculateWeeklyScore(emotions: List<EmotionRecord>): Float {
        if (emotions.isEmpty()) return 0f

        // 수업 3주차 - 배열 처리와 계산
        val diversityScore = (emotions.map { it.emotionSymbol }.distinct().size / 7f) * 2f
        val consistencyScore = if (emotions.size >= 4) 2f else emotions.size * 0.5f
        val balanceScore = calculateEmotionBalance(emotions)

        return ((diversityScore + consistencyScore + balanceScore) / 3f * 5f).coerceIn(0f, 5f)
    }

    private fun calculateMonthlyScore(emotions: List<EmotionRecord>): Float {
        if (emotions.isEmpty()) return 0f

        val weeklyScores = emotions.chunked(7).map { calculateWeeklyScore(it) }
        return weeklyScores.average().toFloat()
    }

    private fun calculateEmotionBalance(emotions: List<EmotionRecord>): Float {
        val positiveCount = emotions.count { it.emotionSymbol in listOf("♪", "♫", "♡", "♩") }
        val negativeCount = emotions.count { it.emotionSymbol in listOf("♭", "♯", "𝄢") }
        val total = emotions.size

        return if (total == 0) 0f else (1f - kotlin.math.abs(positiveCount - negativeCount).toFloat() / total) * 2f
    }

    private fun findDominantEmotion(emotions: List<EmotionRecord>): String {
        return emotions.groupBy { it.emotionSymbol }
            .maxByOrNull { it.value.size }
            ?.let { getEmotionNameFromSymbol(it.key) }
            ?: "없음"
    }

    private fun getEmotionNameFromSymbol(symbol: String): String {
        return when(symbol) {
            "♪" -> "기쁨"
            "♩" -> "평온"
            "♫" -> "설렘"
            "♭" -> "슬픔"
            "♯" -> "화남"
            "𝄢" -> "불안"
            "♡" -> "사랑"
            else -> "알 수 없음"
        }
    }

    private fun getChordDescription(chordName: String): String {
        return when {
            chordName.contains("maj") -> "밝고 안정적인"
            chordName.contains("m") -> "차분하고 깊은"
            chordName.contains("7") -> "복합적이고 풍부한"
            chordName.contains("sus") -> "긴장감 있는"
            else -> "특별한"
        }
    }

    private fun formatArchiveTitle(archive: ArchiveItem): String {
        return when (archive.type) {
            ArchiveType.WEEKLY -> {
                val parts = archive.period.split("-")
                if (parts.size >= 3) {
                    val month = parts[1].toIntOrNull() ?: 1
                    val week = parts[2].replace("W", "")
                    val monthName = getMonthName(month)
                    "${monthName} ${week}주차"
                } else {
                    archive.period
                }
            }
            ArchiveType.MONTHLY -> {
                val parts = archive.period.split("-")
                if (parts.size >= 2) {
                    val year = parts[0]
                    val month = parts[1].toIntOrNull() ?: 1
                    val monthName = getMonthName(month)
                    "${year}년 ${monthName}"
                } else {
                    archive.period
                }
            }
        }
    }

    private fun getMonthName(month: Int): String {
        val monthNames = arrayOf(
            "1월", "2월", "3월", "4월", "5월", "6월",
            "7월", "8월", "9월", "10월", "11월", "12월"
        )
        return monthNames.getOrNull(month - 1) ?: "${month}월"
    }

    private fun getRecentWeeks(count: Int): List<String> {
        val weeks = mutableListOf<String>()
        val calendar = Calendar.getInstance()

        repeat(count) {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH)
            weeks.add("${year}-${month.toString().padStart(2, '0')}-W${weekOfMonth}")
            calendar.add(Calendar.WEEK_OF_YEAR, -1)
        }

        return weeks
    }

    private fun getRecentMonths(count: Int): List<String> {
        val months = mutableListOf<String>()
        val calendar = Calendar.getInstance()

        repeat(count) {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            months.add("${year}-${month.toString().padStart(2, '0')}")
            calendar.add(Calendar.MONTH, -1)
        }

        return months
    }

    private fun getWeekEmotions(weekPeriod: String): List<EmotionRecord> {
        // weekPeriod 형식: "2025-06-W3"
        val emotions = mutableListOf<EmotionRecord>()

        // 해당 주의 시작일과 종료일 계산
        val parts = weekPeriod.split("-")
        if (parts.size >= 3) {
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val week = parts[2].replace("W", "").toInt()

            val calendar = Calendar.getInstance()
            calendar.set(year, month - 1, 1)
            calendar.set(Calendar.WEEK_OF_MONTH, week)
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

            repeat(7) { day ->
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                emotions.addAll(fileManager.loadEmotionsByDate(date))
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        return emotions
    }

    private fun getMonthEmotions(monthPeriod: String): List<EmotionRecord> {
        // monthPeriod 형식: "2025-06"
        val emotions = mutableListOf<EmotionRecord>()
        val savedDates = fileManager.getAllSavedDates()

        savedDates.forEach { date ->
            if (date.startsWith(monthPeriod)) {
                emotions.addAll(fileManager.loadEmotionsByDate(date))
            }
        }

        return emotions
    }

    private fun openWeeklyDetail(weekPeriod: String) {
        val intent = Intent(this, PeriodDetailActivity::class.java)
        intent.putExtra(PeriodDetailActivity.EXTRA_PERIOD_TYPE, "WEEKLY")
        intent.putExtra(PeriodDetailActivity.EXTRA_PERIOD_DATA, weekPeriod)
        startActivity(intent)
    }

    private fun openMonthlyDetail(monthPeriod: String) {
        val intent = Intent(this, PeriodDetailActivity::class.java)
        intent.putExtra(PeriodDetailActivity.EXTRA_PERIOD_TYPE, "MONTHLY")
        intent.putExtra(PeriodDetailActivity.EXTRA_PERIOD_DATA, monthPeriod)
        startActivity(intent)
    }

    private fun openWeeklyArchive() {
        Toast.makeText(this, "주간 아카이브 목록을 불러오는 중...", Toast.LENGTH_SHORT).show()
        // TODO: 주간 아카이브 목록 화면 구현 예정
    }

    private fun openMonthlyArchive() {
        Toast.makeText(this, "월간 아카이브 목록을 불러오는 중...", Toast.LENGTH_SHORT).show()
        // TODO: 월간 아카이브 목록 화면 구현 예정
    }

    // 데이터 클래스들
    data class WeeklyEmotionData(
        val period: String,
        val emotions: List<EmotionRecord>,
        val chord: EmotionChordAnalyzer.EmotionChord,
        val score: Float
    )

    data class ArchiveItem(
        val type: ArchiveType,
        val period: String,
        val emotionCount: Int,
        val dominantEmotion: String,
        val score: Float
    )

    enum class ArchiveType {
        WEEKLY, MONTHLY
    }
}