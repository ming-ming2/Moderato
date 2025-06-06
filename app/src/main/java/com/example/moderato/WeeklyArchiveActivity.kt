// app/src/main/java/com/example/moderato/WeeklyArchiveActivity.kt
package com.example.moderato

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

class WeeklyArchiveActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var tvCurrentWeek: TextView
    private lateinit var btnPrevWeek: ImageButton
    private lateinit var btnNextWeek: ImageButton
    private lateinit var weeklyArchiveContainer: LinearLayout
    private lateinit var tvEmptyWeekly: TextView

    private lateinit var fileManager: EmotionFileManager
    private lateinit var chordAnalyzer: EmotionChordAnalyzer

    // 수업 3주차 - 변수와 날짜 계산
    private var currentWeekCalendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly_archive)

        fileManager = EmotionFileManager(this)
        chordAnalyzer = EmotionChordAnalyzer()

        initViews()
        setupClickListeners()

        // 이번 주로 시작
        setCurrentWeek()
        loadWeeklyArchives()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvCurrentWeek = findViewById(R.id.tvCurrentWeek)
        btnPrevWeek = findViewById(R.id.btnPrevWeek)
        btnNextWeek = findViewById(R.id.btnNextWeek)
        weeklyArchiveContainer = findViewById(R.id.weeklyArchiveContainer)
        tvEmptyWeekly = findViewById(R.id.tvEmptyWeekly)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnPrevWeek.setOnClickListener {
            currentWeekCalendar.add(Calendar.WEEK_OF_YEAR, -1)
            updateWeekDisplay()
            loadWeeklyArchives()
        }

        btnNextWeek.setOnClickListener {
            currentWeekCalendar.add(Calendar.WEEK_OF_YEAR, 1)
            updateWeekDisplay()
            loadWeeklyArchives()
        }
    }

    private fun setCurrentWeek() {
        // 현재 주의 시작을 월요일로 설정
        currentWeekCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        updateWeekDisplay()
    }

    private fun updateWeekDisplay() {
        val year = currentWeekCalendar.get(Calendar.YEAR)
        val month = currentWeekCalendar.get(Calendar.MONTH) + 1
        val weekOfMonth = currentWeekCalendar.get(Calendar.WEEK_OF_MONTH)

        tvCurrentWeek.text = "${year}년 ${month}월 ${weekOfMonth}주차의 하모니"
    }

    // 수업 8주차 - 파일 처리로 주간 데이터 로드
    private fun loadWeeklyArchives() {
        weeklyArchiveContainer.removeAllViews()

        val weekEmotions = loadCurrentWeekEmotions()

        if (weekEmotions.isEmpty()) {
            showEmptyState()
            return
        }

        tvEmptyWeekly.visibility = View.GONE

        // 주간 전체 카드 생성
        val weekCard = createWeekOverviewCard(weekEmotions)
        weeklyArchiveContainer.addView(weekCard)

        // 일별 상세 카드들 생성 (수업 5주차 - 동적 레이아웃)
        val dailyCards = createDailyCards(weekEmotions)
        dailyCards.forEach { card ->
            weeklyArchiveContainer.addView(card)
        }
    }

    private fun loadCurrentWeekEmotions(): Map<String, List<EmotionRecord>> {
        val weekEmotions = mutableMapOf<String, List<EmotionRecord>>()
        val startOfWeek = currentWeekCalendar.clone() as Calendar

        // 월요일부터 일요일까지 7일간 데이터 수집
        for (dayOffset in 0..6) {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(startOfWeek.time)
            val dailyEmotions = fileManager.loadEmotionsByDate(date)

            if (dailyEmotions.isNotEmpty()) {
                weekEmotions[date] = dailyEmotions
            }

            startOfWeek.add(Calendar.DAY_OF_MONTH, 1)
        }

        return weekEmotions
    }

    // 수업 4주차 - 위젯 동적 생성
    private fun createWeekOverviewCard(weekEmotions: Map<String, List<EmotionRecord>>): LinearLayout {
        val allEmotions = weekEmotions.values.flatten()
        val weekChord = chordAnalyzer.analyzeEmotions(allEmotions)
        val weekScore = calculateWeeklyScore(allEmotions)

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = ContextCompat.getDrawable(this@WeeklyArchiveActivity, R.drawable.chord_card_background)
            setPadding(24, 20, 24, 20)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 20)
            }
        }

        // 헤더 (수업 4주차 - 레이아웃 배치)
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

        val weekIcon = TextView(this).apply {
            text = "🎼"
            textSize = 28f
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

        val weekTitle = TextView(this).apply {
            text = "이번 주의 하모니"
            textSize = 20f
            setTextColor(ContextCompat.getColor(this@WeeklyArchiveActivity, R.color.text_primary))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        val chordInfo = TextView(this).apply {
            text = "${weekChord.chordName} - ${getChordDescription(weekChord.chordName)}"
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@WeeklyArchiveActivity, R.color.primary_pink))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 4, 0, 0)
            }
        }

        val scoreText = TextView(this).apply {
            text = "⭐ ${"%.1f".format(weekScore)}"
            textSize = 18f
            setTextColor(ContextCompat.getColor(this@WeeklyArchiveActivity, R.color.secondary_orange))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            background = ContextCompat.getDrawable(this@WeeklyArchiveActivity, R.drawable.score_badge_bg)
            setPadding(12, 8, 12, 8)
        }

        // 주간 미니 악보 (수업 9주차 - 커스텀 뷰)
        val miniStaffContainer = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                300
            ).apply {
                setMargins(0, 16, 0, 16)
            }
            background = ContextCompat.getDrawable(this@WeeklyArchiveActivity, R.drawable.mini_staff_bg)
            gravity = android.view.Gravity.CENTER
        }

        val miniStaffView = EmotionMiniStaffView(this)
        miniStaffView.setEmotions(allEmotions)
        miniStaffContainer.addView(miniStaffView)

        // 통계 정보
        val statsText = TextView(this).apply {
            text = buildWeekStatsText(allEmotions, weekChord)
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@WeeklyArchiveActivity, R.color.text_primary))
            background = ContextCompat.getDrawable(this@WeeklyArchiveActivity, R.drawable.edittext_bg)
            setPadding(16, 12, 16, 12)
            setLineSpacing(4f, 1.0f) // 수업 4주차 - 올바른 메서드 사용
        }

        // 조립
        titleContainer.addView(weekTitle)
        titleContainer.addView(chordInfo)

        headerContainer.addView(weekIcon)
        headerContainer.addView(titleContainer)
        headerContainer.addView(scoreText)

        container.addView(headerContainer)
        container.addView(miniStaffContainer)
        container.addView(statsText)

        return container
    }

    // 수업 3주차 - 배열과 for문 활용
    private fun createDailyCards(weekEmotions: Map<String, List<EmotionRecord>>): List<LinearLayout> {
        val dailyCards = mutableListOf<LinearLayout>()
        val startOfWeek = currentWeekCalendar.clone() as Calendar

        // 요일 배열 (수업 3주차 - 배열 활용)
        val dayNames = arrayOf("월요일", "화요일", "수요일", "목요일", "금요일", "토요일", "일요일")
        val dayEmojis = arrayOf("🌅", "🌤️", "☀️", "🌞", "🎆", "🌙", "🌃")

        for (dayIndex in 0..6) {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(startOfWeek.time)
            val dayEmotions = weekEmotions[date] ?: emptyList()

            val dayCard = createSingleDayCard(
                dayNames[dayIndex],
                dayEmojis[dayIndex],
                date,
                dayEmotions
            )
            dailyCards.add(dayCard)

            startOfWeek.add(Calendar.DAY_OF_MONTH, 1)
        }

        return dailyCards
    }

    private fun createSingleDayCard(dayName: String, dayEmoji: String, date: String, emotions: List<EmotionRecord>): LinearLayout {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            background = ContextCompat.getDrawable(this@WeeklyArchiveActivity, R.drawable.emotion_timeline_bg)
            setPadding(16, 12, 16, 12)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 12)
            }
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        // 요일 아이콘
        val dayIcon = TextView(this).apply {
            text = dayEmoji
            textSize = 24f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 16, 0)
            }
        }

        // 요일 정보
        val dayInfoContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val dayTitle = TextView(this).apply {
            text = "${dayName} (${formatDateShort(date)})"
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@WeeklyArchiveActivity, R.color.text_primary))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        val emotionSummary = TextView(this).apply {
            text = if (emotions.isNotEmpty()) {
                val emotionSymbols = emotions.joinToString(" ") { it.emotionSymbol }
                "$emotionSymbols (${emotions.size}개 기록)"
            } else {
                "기록 없음"
            }
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@WeeklyArchiveActivity, R.color.text_secondary))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 4, 0, 0)
            }
        }

        // 하루 대표 감정 (수업 3주차 - when문 활용)
        val dayMoodIcon = TextView(this).apply {
            text = when {
                emotions.isEmpty() -> "💤"
                emotions.any { it.emotionSymbol == "♪" } -> "😊"
                emotions.any { it.emotionSymbol == "♫" } -> "😍"
                emotions.any { it.emotionSymbol == "♡" } -> "🥰"
                emotions.any { it.emotionSymbol == "♩" } -> "😌"
                emotions.any { it.emotionSymbol == "♭" } -> "😢"
                emotions.any { it.emotionSymbol == "♯" } -> "😠"
                emotions.any { it.emotionSymbol == "𝄢" } -> "😰"
                else -> "😐"
            }
            textSize = 20f
            background = ContextCompat.getDrawable(this@WeeklyArchiveActivity, R.drawable.chord_button_bg)
            setPadding(12, 8, 12, 8)
        }

        dayInfoContainer.addView(dayTitle)
        dayInfoContainer.addView(emotionSummary)

        container.addView(dayIcon)
        container.addView(dayInfoContainer)
        container.addView(dayMoodIcon)

        // 클릭 이벤트 (수업 4주차 - 이벤트 처리)
        container.setOnClickListener {
            if (emotions.isNotEmpty()) {
                showDayDetail(dayName, date, emotions)
            } else {
                Toast.makeText(this, "이 날은 기록된 감정이 없어요", Toast.LENGTH_SHORT).show()
            }
        }

        return container
    }

    // 수업 7주차 - 대화상자 활용
    private fun showDayDetail(dayName: String, date: String, emotions: List<EmotionRecord>) {
        val dayChord = chordAnalyzer.analyzeEmotions(emotions)

        val message = buildString {
            append("🎵 ${dayName} 감정 상세\n\n")
            append("📅 ${formatDateKorean(date)}\n")
            append("🎼 하루의 코드: ${dayChord.chordName}\n")
            append("📊 기록된 감정: ${emotions.size}개\n\n")

            emotions.forEachIndexed { index, emotion ->
                val timeKorean = when(emotion.timeOfDay) {
                    "morning" -> "아침"
                    "afternoon" -> "오후"
                    "evening" -> "저녁"
                    "night" -> "밤"
                    else -> "기타"
                }
                append("${index + 1}. ${emotion.emotionSymbol} ${timeKorean} - ${getEmotionNameFromSymbol(emotion.emotionSymbol)}\n")
            }

            append("\n💭 ${dayChord.message}")
        }

        val builder = AlertDialog.Builder(this, R.style.DarkDialogTheme)
        builder.setTitle("📖 ${dayName} 일기")
        builder.setMessage(message)
        builder.setPositiveButton("확인", null)
        // 🔧 수정하기 버튼 제거 - setNeutralButton 삭제
        val dialog = builder.show()

        dialog.findViewById<TextView>(android.R.id.message)?.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(ContextCompat.getColor(this, R.color.primary_pink))
        // 🔧 수정하기 버튼 스타일링 코드도 제거
    }

    private fun showEmptyState() {
        tvEmptyWeekly.visibility = View.VISIBLE
        tvEmptyWeekly.text = "이번 주는 아직 기록된 감정이 없어요.\n감정을 기록하고 나만의 주간 하모니를 만들어보세요! 🎵"
    }

    // 헬퍼 메서드들 (수업 3주차 - 메서드 활용)
    private fun calculateWeeklyScore(emotions: List<EmotionRecord>): Float {
        if (emotions.isEmpty()) return 0f

        val diversityScore = (emotions.map { it.emotionSymbol }.distinct().size / 7f) * 2f
        val consistencyScore = if (emotions.size >= 4) 2f else emotions.size * 0.5f
        val balanceScore = calculateEmotionBalance(emotions)

        return ((diversityScore + consistencyScore + balanceScore) / 3f * 5f).coerceIn(0f, 5f)
    }

    private fun calculateEmotionBalance(emotions: List<EmotionRecord>): Float {
        val positiveCount = emotions.count { it.emotionSymbol in listOf("♪", "♫", "♡", "♩") }
        val negativeCount = emotions.count { it.emotionSymbol in listOf("♭", "♯", "𝄢") }
        val total = emotions.size

        return if (total == 0) 0f else (1f - kotlin.math.abs(positiveCount - negativeCount).toFloat() / total) * 2f
    }

    private fun buildWeekStatsText(emotions: List<EmotionRecord>, chord: EmotionChordAnalyzer.EmotionChord): String {
        return buildString {
            append("📊 이번 주 하모니 분석\n\n")
            append("• 총 감정 기록: ${emotions.size}개\n")
            append("• 주간 대표 코드: ${chord.chordName}\n")
            append("• 감정 다양성: ${emotions.map { it.emotionSymbol }.distinct().size}가지\n")

            val positiveCount = emotions.count { it.emotionSymbol in listOf("♪", "♫", "♡", "♩") }
            val positiveRatio = if (emotions.isNotEmpty()) {
                (positiveCount.toFloat() / emotions.size * 100).toInt()
            } else 0
            append("• 긍정 감정 비율: ${positiveRatio}%\n")
            append("• 주간 리듬: ${getWeeklyRhythm(emotions)}")
        }
    }

    private fun getWeeklyRhythm(emotions: List<EmotionRecord>): String {
        return when {
            emotions.size >= 14 -> "매우 활발한 리듬 🎶"
            emotions.size >= 7 -> "균형잡힌 리듬 🎵"
            emotions.size >= 3 -> "차분한 리듬 🎼"
            else -> "조용한 리듬 🎹"
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

    private fun formatDateShort(date: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("M.d", Locale.getDefault())
            val dateObj = inputFormat.parse(date)
            outputFormat.format(dateObj ?: Date())
        } catch (e: Exception) {
            date.substring(5).replace("-", ".")
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