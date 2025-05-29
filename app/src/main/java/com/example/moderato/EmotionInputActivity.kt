package com.example.moderato

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class EmotionInputActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var rgEmotions: RadioGroup
    private lateinit var seekBarIntensity: SeekBar
    private lateinit var tvIntensityLevel: TextView
    private lateinit var rgTimeOfDay: RadioGroup
    private lateinit var cbWork: CheckBox
    private lateinit var cbExercise: CheckBox
    private lateinit var cbRest: CheckBox
    private lateinit var cbMeeting: CheckBox
    private lateinit var cbFamily: CheckBox
    private lateinit var cbStudy: CheckBox
    private lateinit var etMemo: EditText
    private lateinit var btnSave: Button

    private val intensityLevels = arrayOf("매우 약함 (pp)", "약함 (p)", "보통 (mf)", "강함 (f)", "매우 강함 (ff)")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emotion_input)

        initViews()
        setupClickListeners()
        setupSeekBar()
        setDefaultSelections()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        rgEmotions = findViewById(R.id.rgEmotions)
        seekBarIntensity = findViewById(R.id.seekBarIntensity)
        tvIntensityLevel = findViewById(R.id.tvIntensityLevel)
        rgTimeOfDay = findViewById(R.id.rgTimeOfDay)
        cbWork = findViewById(R.id.cbWork)
        cbExercise = findViewById(R.id.cbExercise)
        cbRest = findViewById(R.id.cbRest)
        cbMeeting = findViewById(R.id.cbMeeting)
        cbFamily = findViewById(R.id.cbFamily)
        cbStudy = findViewById(R.id.cbStudy)
        etMemo = findViewById(R.id.etMemo)
        btnSave = findViewById(R.id.btnSave)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnSave.setOnClickListener {
            saveEmotionRecord()
        }
    }

    private fun setupSeekBar() {
        seekBarIntensity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvIntensityLevel.text = intensityLevels[progress]
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setDefaultSelections() {
        // 현재 시간에 따라 기본 시간대 선택
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (currentHour) {
            in 5..11 -> findViewById<RadioButton>(R.id.rbMorning).isChecked = true
            in 12..17 -> findViewById<RadioButton>(R.id.rbAfternoon).isChecked = true
            in 18..21 -> findViewById<RadioButton>(R.id.rbEvening).isChecked = true
            else -> findViewById<RadioButton>(R.id.rbNight).isChecked = true
        }
    }

    private fun saveEmotionRecord() {
        // 1. 감정 선택 확인
        val selectedEmotionId = rgEmotions.checkedRadioButtonId
        if (selectedEmotionId == -1) {
            Toast.makeText(this, "감정을 선택해주세요!", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. 시간대 선택 확인
        val selectedTimeId = rgTimeOfDay.checkedRadioButtonId
        if (selectedTimeId == -1) {
            Toast.makeText(this, "시간대를 선택해주세요!", Toast.LENGTH_SHORT).show()
            return
        }

        // 3. 데이터 수집
        val emotionData = collectEmotionData()

        // 4. 파일 저장 (다음 단계에서 구현)
        // saveToFile(emotionData)

        // 5. 성공 메시지
        Toast.makeText(this, "감정이 기록되었어요! ♪", Toast.LENGTH_SHORT).show()

        // 6. 화면 닫기
        finish()
    }

    private fun collectEmotionData(): EmotionInputData {
        // 선택된 감정
        val selectedEmotion = when (rgEmotions.checkedRadioButtonId) {
            R.id.rbJoy -> EmotionType("♪", "기쁨")
            R.id.rbPeace -> EmotionType("♩", "평온")
            R.id.rbExcitement -> EmotionType("♫", "설렘")
            R.id.rbSadness -> EmotionType("♭", "슬픔")
            R.id.rbAnger -> EmotionType("♯", "화남")
            R.id.rbAnxiety -> EmotionType("𝄢", "불안")
            R.id.rbLove -> EmotionType("♡", "사랑")
            else -> EmotionType("♪", "기쁨")
        }

        // 강도
        val intensity = seekBarIntensity.progress + 1

        // 시간대
        val timeOfDay = when (rgTimeOfDay.checkedRadioButtonId) {
            R.id.rbMorning -> "morning"
            R.id.rbAfternoon -> "afternoon"
            R.id.rbEvening -> "evening"
            R.id.rbNight -> "night"
            else -> "morning"
        }

        // 태그들
        val tags = mutableListOf<String>()
        if (cbWork.isChecked) tags.add("업무")
        if (cbExercise.isChecked) tags.add("운동")
        if (cbRest.isChecked) tags.add("휴식")
        if (cbMeeting.isChecked) tags.add("모임")
        if (cbFamily.isChecked) tags.add("가족")
        if (cbStudy.isChecked) tags.add("공부")

        // 메모
        val memo = etMemo.text.toString().trim()

        // 현재 날짜
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        return EmotionInputData(
            emotion = selectedEmotion,
            intensity = intensity,
            timeOfDay = timeOfDay,
            tags = tags,
            memo = memo,
            date = currentDate,
            time = currentTime
        )
    }
}

data class EmotionType(
    val symbol: String,
    val name: String
)

data class EmotionInputData(
    val emotion: EmotionType,
    val intensity: Int,
    val timeOfDay: String,
    val tags: List<String>,
    val memo: String,
    val date: String,
    val time: String
)