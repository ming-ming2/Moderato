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

class MainActivity : AppCompatActivity() {

    private lateinit var btnNotification: ImageButton
    private lateinit var btnSettings: ImageButton
    private lateinit var emotionStaffView: EmotionStaffView
    private lateinit var emotionTimelineContainer: LinearLayout
    private lateinit var tvEmptyMessage: TextView
    private lateinit var btnAddEmotion: Button

    private val emotionData = mutableListOf<EmotionRecord>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupClickListeners()
        loadTodayEmotions()
        updateEmotionDisplay()
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
            Toast.makeText(this, "알림 설정", Toast.LENGTH_SHORT).show()
        }

        btnSettings.setOnClickListener {
            Toast.makeText(this, "설정", Toast.LENGTH_SHORT).show()
        }

        btnAddEmotion.setOnClickListener {
            val intent = Intent(this, EmotionInputActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadTodayEmotions() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        emotionData.add(EmotionRecord("morning", "♪", "기분 좋음", today))
        emotionData.add(EmotionRecord("afternoon", "♩", "평온함", today))
        emotionData.add(EmotionRecord("evening", "♫", "설렘", today))
    }

    private fun updateEmotionDisplay() {
        updateEmotionStaff()
        updateEmotionTimeline()
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
            gravity = android.view.Gravity.CENTER_VERTICAL
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
                else -> "기타"
            }
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.text_primary))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        val emotionContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 4, 0, 0)
            }
        }

        val emotionIcon = TextView(this).apply {
            text = emotion.emotionSymbol
            textSize = 20f
            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.primary_pink))
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

        emotionContainer.addView(emotionIcon)
        emotionContainer.addView(emotionText)

        contentContainer.addView(timeText)
        contentContainer.addView(emotionContainer)

        container.addView(timeIcon)
        container.addView(contentContainer)

        return container
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
                    else -> ""
                }
            )
        }

        val key = determineKey(emotionData)
        val tempo = determineTempo(emotionData)

        emotionStaffView.setEmotions(emotionNotes, key, tempo)
    }

    private fun getEmotionPitch(symbol: String): Int {
        return when(symbol) {
            "♪" -> 6
            "♩" -> 4
            "♫" -> 7
            "♭" -> 2
            "♯" -> 8
            else -> 4
        }
    }

    private fun determineKey(emotions: List<EmotionRecord>): String {
        val happyCount = emotions.count { it.emotionSymbol == "♪" || it.emotionSymbol == "♫" }
        val sadCount = emotions.count { it.emotionSymbol == "♭" }

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
}

data class EmotionRecord(
    val timeOfDay: String,
    val emotionSymbol: String,
    val emotionText: String,
    val date: String
)