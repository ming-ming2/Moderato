package com.example.moderato

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

class PeriodDetailActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var tvPeriodTitle: TextView
    private lateinit var tvPeriodSubtitle: TextView
    private lateinit var tvPeriodScore: TextView

    private lateinit var tvMainChordSymbol: TextView
    private lateinit var tvMainChordName: TextView
    private lateinit var tvMainChordFullName: TextView
    private lateinit var tvMainIntensity: TextView
    private lateinit var tvMainChordMessage: TextView
    private lateinit var tvEmotionCount: TextView
    private lateinit var tvDominantEmotion: TextView

    private lateinit var emotionStaffView: EmotionStaffView
    private lateinit var emotionPieChart: EmotionPieChartView
    private lateinit var tvStatistics: TextView
    private lateinit var dailyEmotionsContainer: LinearLayout
    private lateinit var tvEmptyDaily: TextView
    private lateinit var btnShare: Button

    private lateinit var fileManager: EmotionFileManager
    private lateinit var chordAnalyzer: EmotionChordAnalyzer

    // 수업 3주차 - enum과 데이터 전달
    private var periodType: PeriodType = PeriodType.WEEKLY
    private var periodData: String = ""
    private val emotionData = mutableListOf<EmotionRecord>()

    enum class PeriodType {
        WEEKLY, MONTHLY
    }

    companion object {
        const val EXTRA_PERIOD_TYPE = "PERIOD_TYPE"
        const val EXTRA_PERIOD_DATA = "PERIOD_DATA"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_period_detail)

        fileManager = EmotionFileManager(this)
        chordAnalyzer = EmotionChordAnalyzer()

        // 인텐트에서 데이터 받기
        periodType = if (intent.getStringExtra(EXTRA_PERIOD_TYPE) == "MONTHLY") {
            PeriodType.MONTHLY
        } else {
            PeriodType.WEEKLY
        }
        periodData = intent.getStringExtra(EXTRA_PERIOD_DATA) ?: ""

        initViews()
        setupClickListeners()
        loadPeriodData()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvPeriodTitle = findViewById(R.id.tvPeriodTitle)
        tvPeriodSubtitle = findViewById(R.id.tvPeriodSubtitle)
        tvPeriodScore = findViewById(R.id.tvPeriodScore)

        tvMainChordSymbol = findViewById(R.id.tvMainChordSymbol)
        tvMainChordName = findViewById(R.id.tvMainChordName)
        tvMainChordFullName = findViewById(R.id.tvMainChordFullName)
        tvMainIntensity = findViewById(R.id.tvMainIntensity)
        tvMainChordMessage = findViewById(R.id.tvMainChordMessage)
        tvEmotionCount = findViewById(R.id.tvEmotionCount)
        tvDominantEmotion = findViewById(R.id.tvDominantEmotion)

        emotionStaffView = findViewById(R.id.emotionStaffView)
        emotionPieChart = findViewById(R.id.emotionPieChart)
        tvStatistics = findViewById(R.id.tvStatistics)
        dailyEmotionsContainer = findViewById(R.id.dailyEmotionsContainer)
        tvEmptyDaily = findViewById(R.id.tvEmptyDaily)
        btnShare = findViewById(R.id.btnShare)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnShare.setOnClickListener {
            shareEmotionScore()
        }
    }

    private fun loadPeriodData() {
        // 수업 8주차 - 파일 처리로 기간별 데이터 로드
        emotionData.clear()

        when (periodType) {
            PeriodType.WEEKLY -> loadWeeklyData()
            PeriodType.MONTHLY -> loadMonthlyData()
        }

        updateUI()
    }

    // 수업 8주차 - 파일 처리와 날짜 계산
    private fun loadWeeklyData() {
        // periodData 형식: "2025-06-W3"
        val parts = periodData.split("-")
        if (parts.size >= 3) {
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val week = parts[2].replace("W", "").toInt()

            val calendar = Calendar.getInstance()
            calendar.set(year, month - 1, 1)
            calendar.set(Calendar.WEEK_OF_MONTH, week)
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

            // 일주일간 데이터 수집
            repeat(7) { day ->
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                val dailyEmotions = fileManager.loadEmotionsByDate(date)
                emotionData.addAll(dailyEmotions)
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            // UI 제목 설정
            val monthName = getMonthName(month)
            tvPeriodTitle.text = "${year}년 ${monthName} ${week}주차"
            tvPeriodSubtitle.text = "주간 감정 악보"
        }
    }

    private fun loadMonthlyData() {
        // periodData 형식: "2025-06"
        val savedDates = fileManager.getAllSavedDates()

        savedDates.forEach { date ->
            if (date.startsWith(periodData)) {
                val dailyEmotions = fileManager.loadEmotionsByDate(date)
                emotionData.addAll(dailyEmotions)
            }
        }

        // UI 제목 설정
        val parts = periodData.split("-")
        if (parts.size >= 2) {
            val year = parts[0]
            val month = parts[1].toIntOrNull() ?: 1
            val monthName = getMonthName(month)
            tvPeriodTitle.text = "${year}년 ${monthName}"
            tvPeriodSubtitle.text = "월간 감정 악보"
        }
    }

    private fun updateUI() {
        if (emotionData.isEmpty()) {
            showEmptyState()
            return
        }

        // 감정 분석
        val periodChord = chordAnalyzer.analyzeEmotions(emotionData)
        val periodScore = calculatePeriodScore()

        // 메인 카드 업데이트
        updateMainChordCard(periodChord)

        // 점수 업데이트
        tvPeriodScore.text = "⭐ ${"%.1f".format(periodScore)}"

        // 큰 악보 업데이트
        updateEmotionStaff()

        // 통계 업데이트
        updateStatistics(periodChord, periodScore)

        // 일별 기록 업데이트
        updateDailyEmotions()
    }

    private fun updateMainChordCard(chord: EmotionChordAnalyzer.EmotionChord) {
        tvMainChordSymbol.text = chord.chordSymbol
        tvMainChordName.text = chord.chordName
        tvMainChordFullName.text = chord.chordFullName
        tvMainIntensity.text = chord.intensity.split(" ")[0]
        tvMainChordMessage.text = chord.message

        tvEmotionCount.text = "${chord.emotionCount}개 감정 기록"
        tvDominantEmotion.text = "주요: ${chord.dominantEmotion}"

        // 코드 색상 적용
        try {
            val chordColor = Color.parseColor(chord.chordColor)
            tvMainChordName.setTextColor(chordColor)
            tvMainChordSymbol.setTextColor(chordColor)
        } catch (e: Exception) {
            tvMainChordName.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        }
    }

    private fun updateEmotionStaff() {
        // 수업 9주차 - 커스텀 뷰 활용
        val emotionNotes = emotionData.map { emotion ->
            EmotionStaffView.EmotionNote(
                symbol = emotion.emotionSymbol,
                pitch = getEmotionPitch(emotion.emotionSymbol),
                time = when(emotion.timeOfDay) {
                    "morning" -> "AM"
                    "afternoon" -> "PM"
                    "evening" -> "EV"
                    "night" -> "NT"
                    else -> ""
                },
                intensity = getEmotionIntensity(emotion.date, emotion.timeOfDay),
                timeOfDay = emotion.timeOfDay
            )
        }

        val key = determineKey(emotionData)
        val tempo = determineTempo(emotionData)

        emotionStaffView.setEmotions(emotionNotes, key, tempo)
    }

    private fun updateStatistics(chord: EmotionChordAnalyzer.EmotionChord, score: Float) {
        // 파이 차트 데이터 준비
        val emotionCounts = emotionData.groupBy { it.emotionSymbol }
            .mapValues { it.value.size }

        val chartData = emotionCounts.map { (symbol, count) ->
            val emotionName = getEmotionNameFromSymbol(symbol)
            val percentage = (count.toFloat() / emotionData.size) * 100f
            EmotionPieChartView.EmotionChartData(emotionName, count, percentage)
        }

        emotionPieChart.setEmotionData(chartData)

        // 통계 텍스트 생성
        val statistics = buildString {
            append("• 총 감정 기록: ${emotionData.size}개\n")
            append("• 가장 많은 감정: ${chord.dominantEmotion} (${emotionCounts.values.maxOrNull() ?: 0}회)\n")
            append("• 감정 다양성: ${emotionCounts.size}종류\n")
            append("• 평균 강도: ${chord.intensity}\n")

            val positiveCount = emotionData.count { it.emotionSymbol in listOf("♪", "♫", "♡", "♩") }
            val positiveRatio = if (emotionData.isNotEmpty()) {
                (positiveCount.toFloat() / emotionData.size * 100).toInt()
            } else 0
            append("• 긍정 비율: ${positiveRatio}%\n")

            val periodText = if (periodType == PeriodType.WEEKLY) "주간" else "월간"
            append("• ${periodText} 점수: ${"%.1f".format(score)}/5.0")
        }

        tvStatistics.text = statistics
    }

    private fun updateDailyEmotions() {
        dailyEmotionsContainer.removeAllViews()

        if (emotionData.isEmpty()) {
            tvEmptyDaily.visibility = View.VISIBLE
            return
        }

        tvEmptyDaily.visibility = View.GONE

        // 날짜별로 그룹화
        val dailyGroups = emotionData.groupBy { it.date }
            .toSortedMap()

        dailyGroups.forEach { (date, emotions) ->
            val dailyCard = createDailyEmotionCard(date, emotions)
            dailyEmotionsContainer.addView(dailyCard)
        }
    }

    // 수업 4주차 - 동적 UI 생성
    private fun createDailyEmotionCard(date: String, emotions: List<EmotionRecord>): LinearLayout {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = ContextCompat.getDrawable(this@PeriodDetailActivity, R.drawable.emotion_timeline_bg)
            setPadding(16, 12, 16, 12)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 12)
            }
        }

        // 날짜 헤더
        val dateHeader = TextView(this).apply {
            text = formatDate(date)
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@PeriodDetailActivity, R.color.text_primary))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 8)
            }
        }

        // 감정 목록
        val emotionsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        emotions.forEach { emotion ->
            val emotionChip = TextView(this).apply {
                text = "${emotion.emotionSymbol} ${getTimeOfDayKorean(emotion.timeOfDay)}"
                textSize = 14f
                setTextColor(ContextCompat.getColor(this@PeriodDetailActivity, R.color.text_primary))
                background = ContextCompat.getDrawable(this@PeriodDetailActivity, R.drawable.chord_button_bg)
                setPadding(12, 8, 12, 8)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 8, 0)
                }
            }
            emotionsContainer.addView(emotionChip)
        }

        container.addView(dateHeader)
        container.addView(emotionsContainer)

        return container
    }

    private fun shareEmotionScore() {
        val periodText = if (periodType == PeriodType.WEEKLY) "주간" else "월간"
        val chord = chordAnalyzer.analyzeEmotions(emotionData)
        val score = calculatePeriodScore()

        val shareText = buildString {
            append("🎵 ${tvPeriodTitle.text}의 감정 악보\n\n")
            append("${chord.chordName} (${chord.chordFullName})\n")
            append("${chord.message}\n\n")
            append("📊 ${periodText} 통계:\n")
            append("• 총 ${emotionData.size}개 감정 기록\n")
            append("• 주요 감정: ${chord.dominantEmotion}\n")
            append("• ${periodText} 점수: ${"%.1f".format(score)}/5.0\n")
            append("• 강도: ${chord.intensity}\n\n")
            append("#Moderato #감정악보 #${periodText}기록")
        }

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        startActivity(Intent.createChooser(shareIntent, "${periodText} 감정 악보 공유하기"))
    }

    private fun showEmptyState() {
        tvMainChordSymbol.text = "𝄽"
        tvMainChordName.text = "Rest"
        tvMainChordFullName.text = "쉼표"
        tvMainIntensity.text = "silence"
        tvMainChordMessage.text = "이 기간에는 기록된 감정이 없어요. 감정을 기록하고 나만의 선율을 만들어보세요! 🎵"

        tvEmotionCount.text = "0개 감정 기록"
        tvDominantEmotion.text = "주요: 없음"
        tvPeriodScore.text = "⭐ 0.0"

        tvStatistics.text = "이 기간에는 기록된 감정이 없습니다.\n감정을 기록하면 상세한 분석을 볼 수 있어요!"

        tvEmptyDaily.visibility = View.VISIBLE
    }

    // 헬퍼 메서드들
    private fun calculatePeriodScore(): Float {
        if (emotionData.isEmpty()) return 0f

        // 수업 3주차 - 배열 처리와 계산
        val diversityScore = (emotionData.map { it.emotionSymbol }.distinct().size / 7f) * 2f
        val consistencyScore = if (emotionData.size >= 4) 2f else emotionData.size * 0.5f
        val balanceScore = calculateEmotionBalance(emotionData)

        return ((diversityScore + consistencyScore + balanceScore) / 3f * 5f).coerceIn(0f, 5f)
    }

    private fun calculateEmotionBalance(emotions: List<EmotionRecord>): Float {
        val positiveCount = emotions.count { it.emotionSymbol in listOf("♪", "♫", "♡", "♩") }
        val negativeCount = emotions.count { it.emotionSymbol in listOf("♭", "♯", "𝄢") }
        val total = emotions.size

        return if (total == 0) 0f else (1f - kotlin.math.abs(positiveCount - negativeCount).toFloat() / total) * 2f
    }

    private fun getEmotionIntensity(date: String, timeOfDay: String): Int {
        return try {
            val fileName = "${date}_${timeOfDay}.txt"
            val fileInput = openFileInput(fileName)
            val content = fileInput.bufferedReader().use { it.readText() }
            fileInput.close()

            val lines = content.split("\n")
            for (line in lines) {
                if (line.startsWith("강도:")) {
                    val intensityText = line.substringAfter("강도:").trim()
                    return when {
                        intensityText.contains("pp") -> 1
                        intensityText.contains("p") && !intensityText.contains("pp") -> 2
                        intensityText.contains("mf") -> 3
                        intensityText.contains("f") && !intensityText.contains("ff") -> 4
                        intensityText.contains("ff") -> 5
                        else -> 3
                    }
                }
            }
            3
        } catch (e: Exception) {
            3
        }
    }

    private fun getEmotionPitch(symbol: String): Int {
        return when(symbol) {
            "♪" -> 7  // 높은 도
            "♩" -> 5  // 솔
            "♫" -> 8  // 높은 레
            "♭" -> 2  // 레
            "♯" -> 6  // 라
            "𝄢" -> 1  // 도
            "♡" -> 6  // 라
            else -> 4 // 파
        }
    }

    private fun determineKey(emotions: List<EmotionRecord>): String {
        val happyCount = emotions.count { it.emotionSymbol == "♪" || it.emotionSymbol == "♫" || it.emotionSymbol == "♡" }
        val sadCount = emotions.count { it.emotionSymbol == "♭" || it.emotionSymbol == "𝄢" }

        return if (happyCount > sadCount) "C Major" else "A Minor"
    }

    private fun determineTempo(emotions: List<EmotionRecord>): String {
        return when {
            emotions.size <= 2 -> "Andante"
            emotions.size <= 5 -> "Moderato"
            emotions.size <= 10 -> "Allegro"
            else -> "Vivace"
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

    private fun getTimeOfDayKorean(timeOfDay: String): String {
        return when (timeOfDay) {
            "morning" -> "아침"
            "afternoon" -> "오후"
            "evening" -> "저녁"
            "night" -> "밤"
            else -> "기타"
        }
    }

    private fun getMonthName(month: Int): String {
        val monthNames = arrayOf(
            "1월", "2월", "3월", "4월", "5월", "6월",
            "7월", "8월", "9월", "10월", "11월", "12월"
        )
        return monthNames.getOrNull(month - 1) ?: "${month}월"
    }

    private fun formatDate(date: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MM월 dd일 (E)", Locale.KOREAN)
            val dateObj = inputFormat.parse(date)
            outputFormat.format(dateObj ?: Date())
        } catch (e: Exception) {
            date
        }
    }
}