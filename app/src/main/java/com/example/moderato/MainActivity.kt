package com.example.moderato

import android.app.AlertDialog
import android.content.Intent
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

    // 파일 매니저 추가
    private lateinit var fileManager: EmotionFileManager
    private val emotionData = mutableListOf<EmotionRecord>()

    companion object {
        private const val EMOTION_INPUT_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 파일 매니저 초기화
        fileManager = EmotionFileManager(this)

        initViews()
        setupClickListeners()
        loadTodayEmotions()
        updateEmotionDisplay()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == EMOTION_INPUT_REQUEST && resultCode == RESULT_OK) {
            // 감정 입력 완료 후 데이터 새로고침
            loadTodayEmotions()
            updateEmotionDisplay()
            updateAddEmotionButton() // 버튼 상태도 업데이트
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

    // 오늘 모든 시간대가 기록되었는지 확인
    private fun isAllTimeSlotsRecorded(): Boolean {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val timeSlots = arrayOf("morning", "afternoon", "evening", "night")

        return timeSlots.all { timeSlot ->
            fileManager.hasEmotionData(today, timeSlot)
        }
    }

    // 감정 기록 버튼 상태 업데이트
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

        // 파일에서 오늘의 감정 데이터 불러오기
        val savedEmotions = fileManager.loadEmotionsByDate(today)
        emotionData.addAll(savedEmotions)

        // 데이터가 없으면 샘플 데이터는 추가하지 않음 (실제 기록만 표시)
    }

    private fun updateEmotionDisplay() {
        updateEmotionStaff()
        updateEmotionTimeline()
        updateAddEmotionButton() // 여기도 추가
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

            // 클릭 시 수정 가능하도록
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

        // 편집 힌트 추가
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
                intensity = getEmotionIntensity(emotion.date, emotion.timeOfDay), // 파일에서 강도 읽어오기
                timeOfDay = emotion.timeOfDay
            )
        }

        val key = determineKey(emotionData)
        val tempo = determineTempo(emotionData)

        emotionStaffView.setEmotions(emotionNotes, key, tempo)
    }

    // 파일에서 감정 강도 읽어오기
    private fun getEmotionIntensity(date: String, timeOfDay: String): Int {
        return try {
            val fileName = "${date}_${timeOfDay}.txt"
            val fileInput = openFileInput(fileName)
            val content = fileInput.bufferedReader().use { it.readText() }
            fileInput.close()

            // 강도 정보 파싱
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
            3 // 기본값
        } catch (e: Exception) {
            3 // 오류 시 기본값
        }
    }

    private fun getEmotionPitch(symbol: String): Int {
        return when(symbol) {
            "♪" -> 7    // 기쁨 - 높은 시 (밝고 경쾌한 느낌)
            "♩" -> 5    // 평온 - 라 (안정적이고 편안한 느낌)
            "♫" -> 8    // 설렘 - 높은 도 (두근거리는 높은 음)
            "♭" -> 2    // 슬픔 - 미 (애절하고 낮은 느낌)
            "♯" -> 6    // 화남 - 시 (날카롭고 강한 느낌)
            "𝄢" -> 1    // 불안 - 레 (불안정하고 낮은 느낌)
            "♡" -> 6    // 사랑 - 시 (따뜻하고 중간 높은 음)
            else -> 4   // 기본값 - 솔
        }
    }

    private fun getEmotionColor(symbol: String): Int {
        return when(symbol) {
            "♪" -> ContextCompat.getColor(this, R.color.primary_pink)      // 기쁨
            "♩" -> ContextCompat.getColor(this, R.color.primary_purple)    // 평온
            "♫" -> ContextCompat.getColor(this, R.color.secondary_orange)  // 설렘
            "♭" -> ContextCompat.getColor(this, android.R.color.holo_blue_dark)  // 슬픔
            "♯" -> ContextCompat.getColor(this, android.R.color.holo_red_dark)   // 화남
            "𝄢" -> ContextCompat.getColor(this, android.R.color.darker_gray)      // 불안
            "♡" -> ContextCompat.getColor(this, android.R.color.holo_red_light)  // 사랑
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
            // TODO: 전체 데이터 삭제 기능 (추후 구현)
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
            append("5. 타임라인에서 감정을 클릭하면 수정할 수 있어요\n\n")
            append("💾 모든 감정은 자동으로 저장됩니다!")
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("도움말")
        builder.setMessage(helpMessage)
        builder.setPositiveButton("확인", null)
        builder.show()
    }
}