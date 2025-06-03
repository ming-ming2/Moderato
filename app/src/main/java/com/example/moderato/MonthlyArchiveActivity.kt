// app/src/main/java/com/example/moderato/MonthlyArchiveActivity.kt
package com.example.moderato

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

class MonthlyArchiveActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var tvCurrentMonth: TextView
    private lateinit var btnPrevMonth: ImageButton
    private lateinit var btnNextMonth: ImageButton
    private lateinit var monthlyArchiveContainer: LinearLayout
    private lateinit var tvEmptyMonthly: TextView

    private lateinit var fileManager: EmotionFileManager
    private lateinit var chordAnalyzer: EmotionChordAnalyzer

    // 수업 3주차 - 변수와 날짜 계산
    private var currentMonthCalendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monthly_archive)

        fileManager = EmotionFileManager(this)
        chordAnalyzer = EmotionChordAnalyzer()

        initViews()
        setupClickListeners()

        // 이번 달로 시작
        setCurrentMonth()
        loadMonthlyArchive()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvCurrentMonth = findViewById(R.id.tvCurrentMonth)
        btnPrevMonth = findViewById(R.id.btnPrevMonth)
        btnNextMonth = findViewById(R.id.btnNextMonth)
        monthlyArchiveContainer = findViewById(R.id.monthlyArchiveContainer)
        tvEmptyMonthly = findViewById(R.id.tvEmptyMonthly)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnPrevMonth.setOnClickListener {
            currentMonthCalendar.add(Calendar.MONTH, -1)
            updateMonthDisplay()
            loadMonthlyArchive()
        }

        btnNextMonth.setOnClickListener {
            currentMonthCalendar.add(Calendar.MONTH, 1)
            updateMonthDisplay()
            loadMonthlyArchive()
        }
    }

    private fun setCurrentMonth() {
        // 이번 달 1일로 설정
        currentMonthCalendar.set(Calendar.DAY_OF_MONTH, 1)
        updateMonthDisplay()
    }

    private fun updateMonthDisplay() {
        val year = currentMonthCalendar.get(Calendar.YEAR)
        val month = currentMonthCalendar.get(Calendar.MONTH) + 1
        val monthName = getMonthName(month)

        tvCurrentMonth.text = "${year}년 ${monthName}의 선율"
    }

    // 수업 8주차 - 파일 처리로 월간 데이터 로드
    private fun loadMonthlyArchive() {
        monthlyArchiveContainer.removeAllViews()

        val monthEmotions = loadCurrentMonthEmotions()

        if (monthEmotions.isEmpty()) {
            showEmptyState()
            return
        }

        tvEmptyMonthly.visibility = View.GONE

        // 월간 전체 개요 카드
        val monthOverviewCard = createMonthOverviewCard(monthEmotions)
        monthlyArchiveContainer.addView(monthOverviewCard)

        // 주차별 카드들 (수업 5주차 - 레이아웃)
        val weeklyCards = createWeeklyCards(monthEmotions)
        weeklyCards.forEach { card ->
            monthlyArchiveContainer.addView(card)
        }

        // 월간 하이라이트
        val highlightCard = createMonthHighlightCard(monthEmotions)
        monthlyArchiveContainer.addView(highlightCard)
    }

    private fun loadCurrentMonthEmotions(): Map<String, List<EmotionRecord>> {
        val monthEmotions = mutableMapOf<String, List<EmotionRecord>>()
        val year = currentMonthCalendar.get(Calendar.YEAR)
        val month = currentMonthCalendar.get(Calendar.MONTH) + 1
        val monthString = "${year}-${month.toString().padStart(2, '0')}"

        // 저장된 모든 날짜 확인
        val savedDates = fileManager.getAllSavedDates()

        savedDates.forEach { date ->
            if (date.startsWith(monthString)) {
                val dailyEmotions = fileManager.loadEmotionsByDate(date)
                if (dailyEmotions.isNotEmpty()) {
                    monthEmotions[date] = dailyEmotions
                }
            }
        }

        return monthEmotions
    }

    // 수업 4주차 - 위젯 동적 생성
    private fun createMonthOverviewCard(monthEmotions: Map<String, List<EmotionRecord>>): LinearLayout {
        val allEmotions = monthEmotions.values.flatten()
        val monthChord = chordAnalyzer.analyzeEmotions(allEmotions)
        val monthScore = calculateMonthlyScore(allEmotions)

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = ContextCompat.getDrawable(this@MonthlyArchiveActivity, R.drawable.modern_card_bg)
            setPadding(24, 20, 24, 20)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 20)
            }
        }

        // 헤더
        val headerContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
        }

        val monthIcon = TextView(this).apply {
            text = getMonthEmoji(currentMonthCalendar.get(Calendar.MONTH) + 1)
            textSize = 32f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 16, 0)
            }
        }

        val titleContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val monthTitle = TextView(this).apply {
            text = "${getMonthName(currentMonthCalendar.get(Calendar.MONTH) + 1)}의 교향곡"
            textSize = 22f
            setTextColor(ContextCompat.getColor(this@MonthlyArchiveActivity, R.color.text_primary))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        val chordInfo = TextView(this).apply {
            text = "${monthChord.chordName} - ${getMonthTheme(monthChord.chordName)}"
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@MonthlyArchiveActivity, R.color.primary_pink))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 4, 0, 0)
            }
        }

        val scoreText = TextView(this).apply {
            text = "⭐ ${"%.1f".format(monthScore)}"
            textSize = 20f
            setTextColor(ContextCompat.getColor(this@MonthlyArchiveActivity, R.color.secondary_orange))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            background = ContextCompat.getDrawable(this@MonthlyArchiveActivity, R.drawable.score_badge_bg)
            setPadding(14, 10, 14, 10)
        }

        // 월간 통계 (수업 3주차 - 계산과 조건문)
        val statsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 16, 0, 16)
            }
            background = ContextCompat.getDrawable(this@MonthlyArchiveActivity, R.drawable.edittext_bg)
            setPadding(16, 12, 16, 12)
        }

        val statsLeft = TextView(this).apply {
            text = buildString {
                append("📊 ${allEmotions.size}개 감정 기록\n")
                append("📅 ${monthEmotions.size}일 활동\n")
                append("🎯 감정 다양성: ${allEmotions.map { it.emotionSymbol }.distinct().size}가지")
            }
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@MonthlyArchiveActivity, R.color.text_primary))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setLineSpacing(4f, 1.0f) // 수업 4주차 - 올바른 메서드 사용
        }

        val statsRight = TextView(this).apply {
            val positiveCount = allEmotions.count { it.emotionSymbol in listOf("♪", "♫", "♡", "♩") }
            val positiveRatio = if (allEmotions.isNotEmpty()) {
                (positiveCount.toFloat() / allEmotions.size * 100).toInt()
            } else 0

            text = buildString {
                append("😊 긍정 비율: ${positiveRatio}%\n")
                append("🎼 주요 감정: ${findDominantEmotion(allEmotions)}\n")
                append("🎚️ 월간 리듬: ${getMonthlyRhythm(allEmotions.size)}")
            }
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@MonthlyArchiveActivity, R.color.text_primary))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setLineSpacing(4f, 1.0f) // 수업 4주차 - 올바른 메서드 사용
        }

        statsContainer.addView(statsLeft)
        statsContainer.addView(statsRight)

        // 월간 메시지
        val monthMessage = TextView(this).apply {
            text = generateMonthMessage(monthChord, allEmotions.size, monthEmotions.size)
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@MonthlyArchiveActivity, R.color.text_primary))
            background = ContextCompat.getDrawable(this@MonthlyArchiveActivity, R.drawable.emotion_timeline_bg)
            setPadding(16, 12, 16, 12)
            setLineSpacing(4f, 1.0f) // 수업 4주차 - 올바른 메서드 사용
        }

        // 조립
        titleContainer.addView(monthTitle)
        titleContainer.addView(chordInfo)

        headerContainer.addView(monthIcon)
        headerContainer.addView(titleContainer)
        headerContainer.addView(scoreText)

        container.addView(headerContainer)
        container.addView(statsContainer)
        container.addView(monthMessage)

        return container
    }

    // 수업 3주차 - 배열과 for문으로 주차별 카드 생성
    private fun createWeeklyCards(monthEmotions: Map<String, List<EmotionRecord>>): List<LinearLayout> {
        val weeklyCards = mutableListOf<LinearLayout>()

        // 이번 달의 주차별로 그룹화
        val weeklyGroups = groupEmotionsByWeek(monthEmotions)

        weeklyGroups.forEachIndexed { weekIndex, (weekDates, weekEmotions) ->
            val weekCard = createSingleWeekCard(weekIndex + 1, weekDates, weekEmotions)
            weeklyCards.add(weekCard)
        }

        return weeklyCards
    }

    private fun createSingleWeekCard(weekNumber: Int, weekDates: List<String>, emotions: List<EmotionRecord>): LinearLayout {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = ContextCompat.getDrawable(this@MonthlyArchiveActivity, R.drawable.card_background)
            setPadding(20, 16, 20, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
        }

        // 주차 헤더
        val weekHeader = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 12)
            }
        }

        val weekIcon = TextView(this).apply {
            text = getWeekIcon(weekNumber)
            textSize = 24f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 12, 0)
            }
        }

        val weekTitle = TextView(this).apply {
            text = "${weekNumber}주차: ${getWeekTheme(weekNumber)}"
            textSize = 18f
            setTextColor(ContextCompat.getColor(this@MonthlyArchiveActivity, R.color.text_primary))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val weekScore = TextView(this).apply {
            val score = calculateWeeklyScore(emotions)
            text = "${"%.1f".format(score)}"
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@MonthlyArchiveActivity, R.color.secondary_orange))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            background = ContextCompat.getDrawable(this@MonthlyArchiveActivity, R.drawable.intensity_badge_bg)
            setPadding(10, 6, 10, 6)
        }

        weekHeader.addView(weekIcon)
        weekHeader.addView(weekTitle)
        weekHeader.addView(weekScore)

        // 주간 감정 요약
        val weekSummary = TextView(this).apply {
            text = if (emotions.isNotEmpty()) {
                val weekChord = chordAnalyzer.analyzeEmotions(emotions)
                val emotionSymbols = emotions.map { it.emotionSymbol }.distinct().joinToString(" ")
                "🎼 ${weekChord.chordName} • $emotionSymbols • ${emotions.size}개 기록"
            } else {
                "이 주는 기록된 감정이 없어요"
            }
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@MonthlyArchiveActivity, R.color.text_secondary))
            background = ContextCompat.getDrawable(this@MonthlyArchiveActivity, R.drawable.edittext_bg)
            setPadding(12, 8, 12, 8)
        }

        container.addView(weekHeader)
        container.addView(weekSummary)

        // 클릭 이벤트 - 주간 상세로 이동 (수업 4주차 - 인텐트)
        container.setOnClickListener {
            val year = currentMonthCalendar.get(Calendar.YEAR)
            val month = currentMonthCalendar.get(Calendar.MONTH) + 1
            val weekPeriod = "${year}-${month.toString().padStart(2, '0')}-W${weekNumber}"

            val intent = Intent(this, PeriodDetailActivity::class.java)
            intent.putExtra(PeriodDetailActivity.EXTRA_PERIOD_TYPE, "WEEKLY")
            intent.putExtra(PeriodDetailActivity.EXTRA_PERIOD_DATA, weekPeriod)
            startActivity(intent)
        }

        return container
    }

    private fun createMonthHighlightCard(monthEmotions: Map<String, List<EmotionRecord>>): LinearLayout {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = ContextCompat.getDrawable(this@MonthlyArchiveActivity, R.drawable.chord_card_background)
            setPadding(20, 16, 20, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 16, 0, 0)
            }
        }

        val highlightTitle = TextView(this).apply {
            text = "✨ ${getMonthName(currentMonthCalendar.get(Calendar.MONTH) + 1)} 하이라이트"
            textSize = 20f
            setTextColor(ContextCompat.getColor(this@MonthlyArchiveActivity, R.color.text_primary))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
        }

        val highlights = generateMonthHighlights(monthEmotions)
        val highlightText = TextView(this).apply {
            text = highlights
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@MonthlyArchiveActivity, R.color.text_primary))
            setLineSpacing(6f, 1.0f) // 수업 4주차 - 올바른 메서드 사용
        }

        container.addView(highlightTitle)
        container.addView(highlightText)

        return container
    }

    private fun showEmptyState() {
        tvEmptyMonthly.visibility = View.VISIBLE
        val monthName = getMonthName(currentMonthCalendar.get(Calendar.MONTH) + 1)
        tvEmptyMonthly.text = "${monthName}에는 아직 기록된 감정이 없어요.\n감정을 기록하고 나만의 월간 선율을 만들어보세요! 🎵"
    }

    // 헬퍼 메서드들 (수업 3주차 - 메서드와 계산)
    private fun groupEmotionsByWeek(monthEmotions: Map<String, List<EmotionRecord>>): List<Pair<List<String>, List<EmotionRecord>>> {
        val weeklyGroups = mutableListOf<Pair<List<String>, List<EmotionRecord>>>()
        val calendar = Calendar.getInstance()

        // 이번 달 1일로 설정
        calendar.set(currentMonthCalendar.get(Calendar.YEAR), currentMonthCalendar.get(Calendar.MONTH), 1)

        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        var currentWeek = mutableListOf<String>()
        var currentWeekEmotions = mutableListOf<EmotionRecord>()

        for (day in 1..maxDay) {
            calendar.set(Calendar.DAY_OF_MONTH, day)
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

            currentWeek.add(date)
            monthEmotions[date]?.let { emotions ->
                currentWeekEmotions.addAll(emotions)
            }

            // 일요일이거나 월의 마지막 날이면 주차 완료
            if (dayOfWeek == Calendar.SUNDAY || day == maxDay) {
                weeklyGroups.add(Pair(currentWeek.toList(), currentWeekEmotions.toList()))
                currentWeek.clear()
                currentWeekEmotions.clear()
            }
        }

        return weeklyGroups
    }

    private fun calculateWeeklyScore(emotions: List<EmotionRecord>): Float {
        if (emotions.isEmpty()) return 0f

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

    private fun generateMonthMessage(chord: EmotionChordAnalyzer.EmotionChord, totalEmotions: Int, activeDays: Int): String {
        val monthName = getMonthName(currentMonthCalendar.get(Calendar.MONTH) + 1)

        return when {
            totalEmotions >= 20 -> "🎼 ${monthName}은 풍성한 감정의 교향곡이었어요! 다양한 선율이 어우러진 멋진 한 달이었습니다."
            totalEmotions >= 10 -> "🎵 ${monthName}은 조화로운 선율이 흘러간 달이었어요. 감정의 리듬이 안정적이었네요."
            totalEmotions >= 5 -> "🎹 ${monthName}은 차분한 선율의 달이었어요. 조용하지만 의미있는 감정들이 기록되었습니다."
            else -> "🎼 ${monthName}은 조용한 달이었어요. 더 많은 감정을 기록하면 풍성한 선율을 만들 수 있을 거예요."
        }
    }

    private fun generateMonthHighlights(monthEmotions: Map<String, List<EmotionRecord>>): String {
        val allEmotions = monthEmotions.values.flatten()

        if (allEmotions.isEmpty()) {
            return "이번 달은 조용한 달이었어요.\n다음 달에는 더 많은 감정을 기록해보세요! 🎵"
        }

        return buildString {
            // 가장 감정이 풍부했던 날
            val busiestDay = monthEmotions.maxByOrNull { it.value.size }
            busiestDay?.let { (date, emotions) ->
                append("🌟 가장 풍부한 하루: ${formatDateKorean(date)} (${emotions.size}개 감정)\n\n")
            }

            // 가장 자주 나타난 감정
            val dominantEmotion = findDominantEmotion(allEmotions)
            append("🎭 이달의 주인공: ${dominantEmotion}\n\n")

            // 감정 성장 포인트
            val emotionVariety = allEmotions.map { it.emotionSymbol }.distinct().size
            when {
                emotionVariety >= 6 -> append("🌈 감정 다양성이 매우 풍부했어요!")
                emotionVariety >= 4 -> append("🎨 다양한 감정을 경험한 달이었어요!")
                else -> append("🎯 좀 더 다양한 감정을 기록해보면 어떨까요?")
            }
        }
    }

    // UI 헬퍼 메서드들 (수업 3주차 - when문 활용)
    private fun getMonthName(month: Int): String {
        val monthNames = arrayOf(
            "1월", "2월", "3월", "4월", "5월", "6월",
            "7월", "8월", "9월", "10월", "11월", "12월"
        )
        return monthNames.getOrNull(month - 1) ?: "${month}월"
    }

    private fun getMonthEmoji(month: Int): String {
        return when(month) {
            1 -> "❄️"  // 겨울
            2 -> "🌸"  // 봄 시작
            3 -> "🌺"  // 봄
            4 -> "🌷"  // 봄
            5 -> "🌻"  // 늦봄
            6 -> "☀️"  // 초여름
            7 -> "🌊"  // 여름
            8 -> "🍉"  // 여름
            9 -> "🍂"  // 가을
            10 -> "🍁" // 가을
            11 -> "🌰" // 늦가을
            12 -> "🎄" // 겨울
            else -> "📅"
        }
    }

    private fun getMonthTheme(chordName: String): String {
        return when {
            chordName.contains("maj") -> "밝고 희망찬 한 달"
            chordName.contains("m") && !chordName.contains("maj") -> "깊고 사색적인 한 달"
            chordName.contains("7") -> "복합적이고 풍성한 한 달"
            chordName.contains("sus") -> "변화와 성장의 한 달"
            else -> "특별한 의미의 한 달"
        }
    }

    private fun getWeekIcon(weekNumber: Int): String {
        return when(weekNumber) {
            1 -> "🎵"  // 시작의 전주곡
            2 -> "🎶"  // 발전의 주제
            3 -> "🎼"  // 절정의 클라이막스
            4 -> "🎹"  // 마무리의 코다
            else -> "🎺"
        }
    }

    private fun getWeekTheme(weekNumber: Int): String {
        return when(weekNumber) {
            1 -> "시작의 전주곡"
            2 -> "발전의 주제"
            3 -> "절정의 클라이막스"
            4 -> "마무리의 코다"
            5 -> "보너스 에필로그"
            else -> "특별한 악장"
        }
    }

    private fun getMonthlyRhythm(emotionCount: Int): String {
        return when {
            emotionCount >= 30 -> "매우 활발한 월간 리듬"
            emotionCount >= 20 -> "균형잡힌 월간 리듬"
            emotionCount >= 10 -> "차분한 월간 리듬"
            else -> "조용한 월간 리듬"
        }
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

    private fun formatDateKorean(date: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("M월 d일 (E)", Locale.KOREAN)
            val dateObj = inputFormat.parse(date)
            outputFormat.format(dateObj ?: Date())
        } catch (e: Exception) {
            date
        }
    }
}