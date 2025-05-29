// app/src/main/java/com/example/moderato/MainActivity.kt
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

    private lateinit var btnNotification: ImageButton
    private lateinit var btnSettings: ImageButton
    private lateinit var emotionStaffView: EmotionStaffView
    private lateinit var emotionTimelineContainer: LinearLayout
    private lateinit var tvEmptyMessage: TextView
    private lateinit var btnAddEmotion: Button

    private lateinit var todayChordCard: LinearLayout
    private lateinit var tvChordSymbol: TextView
    private lateinit var tvChordName: TextView
    private lateinit var tvChordFullName: TextView
    private lateinit var tvIntensity: TextView
    private lateinit var tvChordMessage: TextView
    private lateinit var tvEmotionCount: TextView
    private lateinit var tvDominantEmotion: TextView
    private lateinit var btnPlayChord: Button
    private lateinit var btnShareChord: Button

    private lateinit var fileManager: EmotionFileManager
    private lateinit var chordAnalyzer: EmotionChordAnalyzer
    private val emotionData = mutableListOf<EmotionRecord>()

    companion object {
        private const val EMOTION_INPUT_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fileManager = EmotionFileManager(this)
        chordAnalyzer = EmotionChordAnalyzer()

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
        btnNotification = findViewById(R.id.btnNotification)
        btnSettings = findViewById(R.id.btnSettings)
        emotionStaffView = findViewById(R.id.emotionStaffView)
        emotionTimelineContainer = findViewById(R.id.emotionTimelineContainer)
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage)
        btnAddEmotion = findViewById(R.id.btnAddEmotion)
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
        btnPlayChord = findViewById(R.id.btnPlayChord)
        btnShareChord = findViewById(R.id.btnShareChord)
    }

    private fun setupClickListeners() {
        btnNotification.setOnClickListener {
            showNotificationMenu()
        }

        btnSettings.setOnClickListener {
            showSettingsMenu()
        }

        btnAddEmotion.setOnClickListener {
            if (isAllTimeSlotsRecorded()) {
                Toast.makeText(this, "오늘의 모든 감정이 이미 기록되었어요! 🎵\n내일 또 만나요!", Toast.LENGTH_LONG).show()
            } else {
                val intent = Intent(this, EmotionInputActivity::class.java)
                startActivityForResult(intent, EMOTION_INPUT_REQUEST)
            }
        }

        updateAddEmotionButton()
    }

    private fun setupChordClickListeners() {
        btnPlayChord.setOnClickListener {
            handlePlayChord()
        }

        btnShareChord.setOnClickListener {
            handleShareChord()
        }

        todayChordCard.setOnClickListener {
            showChordDetails()
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

        if (chord.emotionCount == 0) {
            btnPlayChord.text = "🎵 첫 감정 기록하기"
            btnShareChord.visibility = View.GONE
        } else {
            btnPlayChord.text = "♪ 코드 듣기"
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

    private fun handlePlayChord() {
        val currentChord = chordAnalyzer.analyzeEmotions(emotionData)

        if (currentChord.emotionCount == 0) {
            val intent = Intent(this, EmotionInputActivity::class.java)
            startActivityForResult(intent, EMOTION_INPUT_REQUEST)
        } else {
            showChordPlayMessage(currentChord)
        }
    }

    private fun showChordPlayMessage(chord: EmotionChordAnalyzer.EmotionChord) {
        val message = "${chord.chordName} 코드가 연주됩니다 🎵\n${chord.message}"
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
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

        val builder = AlertDialog.Builder(this)
        builder.setTitle("🎵 ${currentChord.chordName}")
        builder.setMessage(detailMessage)
        builder.setPositiveButton("확인", null)
        builder.show()
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

            setOnClickListener {
                editEmotion(emotion)
            }
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
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 4, 0, 0)
            }
        }

        val emotionIcon = TextView(this).apply {
            text = emotion.emotionSymbol
            textSize = 20f
            setTextColor(getEmotionColor(emotion.emotionSymbol))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 0, 8, 0)
            }
        }

        val emotionText = TextView(this).apply {
            text = emotion.emotionText
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.text_secondary))
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
        }

        val editHint = TextView(this).apply {
            text = "✏️"
            textSize = 12f
            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.text_secondary))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(8, 0, 0, 0)
            }
        }

        emotionContainer.addView(emotionIcon)
        emotionContainer.addView(emotionText)
        emotionContainer.addView(editHint)

        contentContainer.addView(timeText)
        contentContainer.addView(emotionContainer)

        container.addView(timeIcon)
        container.addView(contentContainer)

        return container
    }

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

    private fun showNotificationMenu() {
        val savedDates = fileManager.getAllSavedDates()
        if (savedDates.isEmpty()) {
            Toast.makeText(this, "아직 저장된 감정 기록이 없어요", Toast.LENGTH_SHORT).show()
            return
        }

        val message = "총 ${savedDates.size}일의 감정이 기록되어 있어요!\n최근: ${savedDates.firstOrNull() ?: "없음"}"
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showSettingsMenu() {
        val options = arrayOf("전체 기록 보기", "데이터 정리", "도움말")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("설정")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> showAllRecords()
                1 -> showDataCleanup()
                2 -> showHelp()
            }
        }
        builder.show()
    }

    private fun showAllRecords() {
        val savedDates = fileManager.getAllSavedDates()
        if (savedDates.isEmpty()) {
            Toast.makeText(this, "저장된 기록이 없습니다", Toast.LENGTH_SHORT).show()
            return
        }

        val message = buildString {
            append("📊 전체 감정 기록\n\n")
            savedDates.take(10).forEach { date ->
                val emotions = fileManager.loadEmotionsByDate(date)
                append("📅 $date: ${emotions.size}개 감정\n")
                emotions.forEach { emotion ->
                    append("  ${getTimeOfDayKorean(emotion.timeOfDay)}: ${emotion.emotionSymbol} ${emotion.emotionText}\n")
                }
                append("\n")
            }
            if (savedDates.size > 10) {
                append("... 외 ${savedDates.size - 10}일 더")
            }
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("전체 기록")
        builder.setMessage(message)
        builder.setPositiveButton("확인", null)
        builder.show()
    }

    private fun showDataCleanup() {
        val savedDates = fileManager.getAllSavedDates()
        if (savedDates.isEmpty()) {
            Toast.makeText(this, "정리할 데이터가 없습니다", Toast.LENGTH_SHORT).show()
            return
        }

        val message = "총 ${savedDates.size}일의 기록이 있습니다.\n정말로 모든 데이터를 삭제하시겠어요?"

        val builder = AlertDialog.Builder(this)
        builder.setTitle("데이터 정리")
        builder.setMessage(message)
        builder.setPositiveButton("삭제") { _, _ ->
            Toast.makeText(this, "데이터 삭제 기능은 추후 구현 예정입니다", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("취소", null)
        builder.show()
    }

    private fun showHelp() {
        val helpMessage = buildString {
            append("🎵 Moderato 사용법\n\n")
            append("1. '+ 감정 기록하기' 버튼으로 감정을 기록하세요\n")
            append("2. 시간대별로 다른 감정을 기록할 수 있어요\n")
            append("3. 감정 강도와 태그를 설정해보세요\n")
            append("4. 기록된 감정은 악보로 표현됩니다\n")
            append("5. 타임라인에서 감정을 클릭하면 수정할 수 있어요\n")
            append("6. 오늘의 감정 코드를 확인하고 공유해보세요\n\n")
            append("💾 모든 감정은 자동으로 저장됩니다!")
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("도움말")
        builder.setMessage(helpMessage)
        builder.setPositiveButton("확인", null)
        builder.show()
    }
}