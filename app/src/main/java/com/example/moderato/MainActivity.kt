package com.example.moderato

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var emotionStaffView: EmotionStaffView
    private lateinit var emotionTimelineContainer: LinearLayout
    private lateinit var tvEmptyMessage: TextView
    private lateinit var btnAddEmotion: Button
    private lateinit var btnEmotionTuner: Button

    private lateinit var todayChordCard: LinearLayout
    private lateinit var tvChordSymbol: TextView
    private lateinit var tvChordName: TextView
    private lateinit var tvChordFullName: TextView
    private lateinit var tvIntensity: TextView
    private lateinit var tvChordMessage: TextView
    private lateinit var tvEmotionCount: TextView
    private lateinit var tvDominantEmotion: TextView
    private lateinit var btnShareChord: Button

    private lateinit var btnEmotionArchive: Button

    private lateinit var fileManager: EmotionFileManager
    private lateinit var chordAnalyzer: EmotionChordAnalyzer
    private lateinit var chordHistoryManager: ChordHistoryManager
    private val emotionData = mutableListOf<EmotionRecord>()

    companion object {
        private const val EMOTION_INPUT_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fileManager = EmotionFileManager(this)
        chordAnalyzer = EmotionChordAnalyzer()
        chordHistoryManager = ChordHistoryManager(this)

        initViews()
        initChordViews()
        setupClickListeners()
        setupChordClickListeners()
        loadTodayEmotions()
        updateEmotionDisplay()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == EMOTION_INPUT_REQUEST && resultCode == RESULT_OK) {
            loadTodayEmotions()
            updateEmotionDisplay()
            updateAddEmotionButton()
        }
    }

    private fun initViews() {
        emotionStaffView = findViewById(R.id.emotionStaffView)
        emotionTimelineContainer = findViewById(R.id.emotionTimelineContainer)
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage)
        btnAddEmotion = findViewById(R.id.btnAddEmotion)
        btnEmotionTuner = findViewById(R.id.btnEmotionTuner)
        btnEmotionArchive = findViewById(R.id.btnEmotionArchive)
    }

    private fun initChordViews() {
        todayChordCard = findViewById(R.id.todayChordCard)
        tvChordSymbol = findViewById(R.id.tvChordSymbol)
        tvChordName = findViewById(R.id.tvChordName)
        tvChordFullName = findViewById(R.id.tvChordFullName)
        tvIntensity = findViewById(R.id.tvIntensity)
        tvChordMessage = findViewById(R.id.tvChordMessage)
        tvEmotionCount = findViewById(R.id.tvEmotionCount)
        tvDominantEmotion = findViewById(R.id.tvDominantEmotion)
        btnShareChord = findViewById(R.id.btnShareChord)
    }

    private fun setupClickListeners() {
        btnAddEmotion.setOnClickListener {
            if (isAllTimeSlotsRecorded()) {
                Toast.makeText(this, "오늘의 모든 감정이 이미 기록되었어요! 🎵\n내일 또 만나요!", Toast.LENGTH_LONG).show()
            } else {
                val intent = Intent(this, EmotionInputActivity::class.java)
                startActivityForResult(intent, EMOTION_INPUT_REQUEST)
            }
        }

        btnEmotionTuner.setOnClickListener {
            startEmotionTuner()
        }

        btnEmotionArchive.setOnClickListener {
            val intent = Intent(this, EmotionArchiveActivity::class.java)
            startActivity(intent)
        }

        updateAddEmotionButton()
    }

    private fun setupChordClickListeners() {
        btnShareChord.setOnClickListener {
            handleShareChord()
        }

        todayChordCard.setOnClickListener {
            showChordDetails()
        }
    }

    private fun startEmotionTuner() {
        if (emotionData.isEmpty()) {
            Toast.makeText(this, "먼저 현재 감정을 기록해주세요!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, EmotionInputActivity::class.java)
            startActivityForResult(intent, EMOTION_INPUT_REQUEST)
            return
        }

        val latestEmotion = emotionData.lastOrNull()
        if (latestEmotion != null) {
            val intent = Intent(this, EmotionTunerActivity::class.java)
            intent.putExtra("CURRENT_EMOTION_SYMBOL", latestEmotion.emotionSymbol)
            intent.putExtra("CURRENT_EMOTION_NAME", getEmotionNameFromSymbol(latestEmotion.emotionSymbol))
            startActivity(intent)
        } else {
            Toast.makeText(this, "감정 데이터를 불러올 수 없습니다", Toast.LENGTH_SHORT).show()
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

    private fun isAllTimeSlotsRecorded(): Boolean {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val timeSlots = arrayOf("morning", "afternoon", "evening", "night")

        return timeSlots.all { timeSlot ->
            fileManager.hasEmotionData(today, timeSlot)
        }
    }

    private fun updateAddEmotionButton() {
        if (isAllTimeSlotsRecorded()) {
            btnAddEmotion.text = "🎼 오늘 연주는 끝났어요!"
            btnAddEmotion.alpha = 0.5f
            btnAddEmotion.isEnabled = false
        } else {
            btnAddEmotion.text = "+ 감정 기록하기"
            btnAddEmotion.alpha = 1.0f
            btnAddEmotion.isEnabled = true
        }
    }

    private fun loadTodayEmotions() {
        emotionData.clear()

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val savedEmotions = fileManager.loadEmotionsByDate(today)
        emotionData.addAll(savedEmotions)
    }

    private fun updateEmotionDisplay() {
        updateEmotionStaff()
        updateEmotionTimeline()
        updateTodayChord()
        updateAddEmotionButton()
    }

    private fun updateTodayChord() {
        val todayChord = chordAnalyzer.analyzeEmotions(emotionData)
        displayChord(todayChord)

        if (todayChord.emotionCount > 0) {
            chordHistoryManager.saveChordHistory(todayChord)
        }
    }

    private fun displayChord(chord: EmotionChordAnalyzer.EmotionChord) {
        tvChordSymbol.text = chord.chordSymbol
        tvChordName.text = chord.chordName
        tvChordFullName.text = chord.chordFullName
        tvIntensity.text = chord.intensity.split(" ")[0]
        tvChordMessage.text = chord.message

        tvEmotionCount.text = "${chord.emotionCount}개 감정 기록"
        tvDominantEmotion.text = "주요: ${chord.dominantEmotion}"

        try {
            val chordColor = Color.parseColor(chord.chordColor)
            tvChordName.setTextColor(chordColor)
            tvChordSymbol.setTextColor(chordColor)
        } catch (e: Exception) {
            tvChordName.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        }

        // 공유 버튼 표시 여부만 결정 (코드 듣기 버튼 제거됨)
        if (chord.emotionCount == 0) {
            btnShareChord.visibility = View.GONE
        } else {
            btnShareChord.visibility = View.VISIBLE
        }

        animateChordCard()
    }

    private fun animateChordCard() {
        todayChordCard.alpha = 0f
        todayChordCard.scaleX = 0.8f
        todayChordCard.scaleY = 0.8f

        todayChordCard.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(500)
            .start()
    }

    private fun handleShareChord() {
        val currentChord = chordAnalyzer.analyzeEmotions(emotionData)
        val today = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault()).format(Date())

        val shareText = buildString {
            append("🎵 ${today}의 감정 코드\n\n")
            append("${currentChord.chordName} (${currentChord.chordFullName})\n")
            append("${currentChord.message}\n\n")
            append("📊 ${currentChord.emotionCount}개 감정 기록\n")
            append("🎼 주요 감정: ${currentChord.dominantEmotion}\n")
            append("🎚️ 강도: ${currentChord.intensity}\n\n")
            append("#Moderato #감정코드 #${currentChord.chordName}")
        }

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        startActivity(Intent.createChooser(shareIntent, "오늘의 감정 코드 공유하기"))
    }

    private fun showChordDetails() {
        val currentChord = chordAnalyzer.analyzeEmotions(emotionData)

        val detailMessage = buildString {
            append("🎼 ${currentChord.chordName} 상세 정보\n\n")
            append("📝 정식 명칭: ${currentChord.chordFullName}\n")
            append("🎵 코드 기호: ${currentChord.chordSymbol}\n")
            append("🎚️ 감정 강도: ${currentChord.intensity}\n")
            append("📊 기록된 감정: ${currentChord.emotionCount}개\n")
            append("🎯 주요 감정: ${currentChord.dominantEmotion}\n\n")
            append("💭 오늘의 감정 해석:\n${currentChord.message}")
        }

        // 수업 7주차 - 커스텀 대화상자 스타일 적용
        val builder = AlertDialog.Builder(this, R.style.DarkDialogTheme)
        builder.setTitle("🎵 ${currentChord.chordName}")
        builder.setMessage(detailMessage)
        builder.setPositiveButton("확인", null)
        val dialog = builder.show()

        // 추가 텍스트 색상 보정
        dialog.findViewById<TextView>(android.R.id.message)?.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(ContextCompat.getColor(this, R.color.primary_pink))
    }

    private fun updateEmotionTimeline() {
        emotionTimelineContainer.removeAllViews()

        if (emotionData.isEmpty()) {
            tvEmptyMessage.visibility = View.VISIBLE
            return
        }

        tvEmptyMessage.visibility = View.GONE

        emotionData.forEach { emotion ->
            val timelineItem = createTimelineItem(emotion)
            emotionTimelineContainer.addView(timelineItem)
        }
    }

    private fun createTimelineItem(emotion: EmotionRecord): LinearLayout {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
            setPadding(16, 12, 16, 12)
            background = ContextCompat.getDrawable(this@MainActivity, R.drawable.emotion_timeline_bg)
        }

        val timeIcon = TextView(this).apply {
            text = when(emotion.timeOfDay) {
                "morning" -> "🌅"
                "afternoon" -> "🌞"
                "evening" -> "🌙"
                "night" -> "🌃"
                else -> "⏰"
            }
            textSize = 24f
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 0, 16, 0)
            }
        }

        val contentContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val timeText = TextView(this).apply {
            text = when(emotion.timeOfDay) {
                "morning" -> "아침"
                "afternoon" -> "오후"
                "evening" -> "저녁"
                "night" -> "밤"
                else -> "기타"
            }
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.text_primary))
            typeface = Typeface.DEFAULT_BOLD
        }

        val emotionContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 4, 0, 0)
            }
        }

        val emotionIcon = TextView(this).apply {
            text = emotion.emotionSymbol
            textSize = 20f
            setTextColor(getEmotionColor(emotion.emotionSymbol))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 8, 0)
            }
        }

        val emotionText = TextView(this).apply {
            text = emotion.emotionText
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.text_secondary))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
        }

        val buttonContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        val editButton = TextView(this).apply {
            text = "✏️"
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.text_secondary))
            background = ContextCompat.getDrawable(this@MainActivity, R.drawable.chord_button_bg)
            setPadding(12, 8, 12, 8)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)

            setOnClickListener {
                editEmotion(emotion)
            }
        }

        emotionContainer.addView(emotionIcon)
        emotionContainer.addView(emotionText)

        buttonContainer.addView(editButton)

        contentContainer.addView(timeText)
        contentContainer.addView(emotionContainer)

        container.addView(timeIcon)
        container.addView(contentContainer)
        container.addView(buttonContainer)

        container.setOnClickListener {
            showEmotionDetail(emotion)
        }

        return container
    }

    private fun showEmotionDetail(emotion: EmotionRecord) {
        val timeKorean = getTimeOfDayKorean(emotion.timeOfDay)
        val emotionName = getEmotionNameFromSymbol(emotion.emotionSymbol)

        // 수업 8주차 - 파일에서 상세 정보 읽어오기
        val detailInfo = getEmotionDetailFromFile(emotion.date, emotion.timeOfDay)

        val message = buildString {
            append("🎵 ${timeKorean} 감정 기록\n\n")
            append("감정: ${emotion.emotionSymbol} ${emotionName}\n")
            append("강도: ${detailInfo.intensity}\n")
            append("기록 날짜: ${emotion.date}\n")
            append("시간대: ${timeKorean}\n\n")

            // 태그 정보 추가
            if (detailInfo.tags.isNotEmpty()) {
                append("🏷️ 상황: ${detailInfo.tags}\n")
            }

            // 메모 정보 추가
            if (detailInfo.memo.isNotEmpty()) {
                append("📝 한줄 기록: ${detailInfo.memo}\n")
            }

            append("\n💡 이 감정을 수정하려면 '✏️' 버튼을 눌러주세요.")
        }

        // 수업 7주차 - 커스텀 대화상자 스타일 적용
        val builder = AlertDialog.Builder(this, R.style.DarkDialogTheme)
        builder.setTitle("📋 감정 기록 상세")
        builder.setMessage(message)
        builder.setPositiveButton("확인", null)
        builder.setNeutralButton("수정하기") { _, _ ->
            editEmotion(emotion)
        }
        val dialog = builder.show()

        // 추가 텍스트 및 버튼 색상 보정
        dialog.findViewById<TextView>(android.R.id.message)?.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(ContextCompat.getColor(this, R.color.primary_pink))
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL)?.setTextColor(ContextCompat.getColor(this, R.color.secondary_orange))
    }

    // 수업 8주차 - 파일에서 상세 정보 읽어오는 메서드
    private fun getEmotionDetailFromFile(date: String, timeOfDay: String): EmotionDetailInfo {
        return try {
            val fileName = "${date}_${timeOfDay}.txt"
            val fileInput = openFileInput(fileName)
            val content = fileInput.bufferedReader().use { it.readText() }
            fileInput.close()

            // 수업 3주차 - 문자열 처리와 조건문 활용
            val lines = content.split("\n")
            var intensity = "보통 (mf)"
            var tags = ""
            var memo = ""

            for (line in lines) {
                when {
                    line.startsWith("강도:") -> {
                        intensity = line.substringAfter("강도:").trim()
                    }
                    line.startsWith("태그:") -> {
                        tags = line.substringAfter("태그:").trim()
                        if (tags.isEmpty()) tags = "없음"
                    }
                    line.startsWith("메모:") -> {
                        memo = line.substringAfter("메모:").trim()
                        if (memo.isEmpty()) memo = "없음"
                    }
                }
            }

            EmotionDetailInfo(intensity, tags, memo)
        } catch (e: Exception) {
            // 파일 읽기 실패 시 기본값 반환
            EmotionDetailInfo("보통 (mf)", "없음", "없음")
        }
    }

    // 수업 3주차 - 데이터 클래스 활용
    data class EmotionDetailInfo(
        val intensity: String,
        val tags: String,
        val memo: String
    )

    private fun editEmotion(emotion: EmotionRecord) {
        Toast.makeText(this, "${getTimeOfDayKorean(emotion.timeOfDay)} 감정을 수정해요!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, EmotionInputActivity::class.java)
        intent.putExtra("EDIT_MODE", true)
        intent.putExtra("EDIT_DATE", emotion.date)
        intent.putExtra("EDIT_TIME_OF_DAY", emotion.timeOfDay)
        startActivityForResult(intent, EMOTION_INPUT_REQUEST)
    }

    private fun updateEmotionStaff() {
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
            "♪" -> 7
            "♩" -> 5
            "♫" -> 8
            "♭" -> 2
            "♯" -> 6
            "𝄢" -> 1
            "♡" -> 6
            else -> 4
        }
    }

    private fun getEmotionColor(symbol: String): Int {
        return when(symbol) {
            "♪" -> ContextCompat.getColor(this, R.color.primary_pink)
            "♩" -> ContextCompat.getColor(this, R.color.primary_purple)
            "♫" -> ContextCompat.getColor(this, R.color.secondary_orange)
            "♭" -> ContextCompat.getColor(this, android.R.color.holo_blue_dark)
            "♯" -> ContextCompat.getColor(this, android.R.color.holo_red_dark)
            "𝄢" -> ContextCompat.getColor(this, android.R.color.darker_gray)
            "♡" -> ContextCompat.getColor(this, android.R.color.holo_red_light)
            else -> ContextCompat.getColor(this, R.color.text_primary)
        }
    }

    private fun determineKey(emotions: List<EmotionRecord>): String {
        val happyCount = emotions.count { it.emotionSymbol == "♪" || it.emotionSymbol == "♫" || it.emotionSymbol == "♡" }
        val sadCount = emotions.count { it.emotionSymbol == "♭" || it.emotionSymbol == "𝄢" }

        return if (happyCount > sadCount) "C Major" else "A Minor"
    }

    private fun determineTempo(emotions: List<EmotionRecord>): String {
        return when(emotions.size) {
            0 -> "Andante"
            1 -> "Moderato"
            2 -> "Allegro"
            else -> "Vivace"
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
}