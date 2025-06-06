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

    // 파일 매니저 추가
    private lateinit var fileManager: EmotionFileManager

    // 수정 모드 관련 변수
    private var isEditMode = false
    private var editDate = ""
    private var editTimeOfDay = ""

    private val intensityLevels = arrayOf("매우 약함 (pp)", "약함 (p)", "보통 (mf)", "강함 (f)", "매우 강함 (ff)")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emotion_input)

        // 파일 매니저 초기화
        fileManager = EmotionFileManager(this)

        // 수정 모드 확인
        checkEditMode()

        initViews()
        setupClickListeners()
        setupSeekBar()
        setDefaultSelections()

        if (!isEditMode) {
            // 새로운 기록 모드에서는 기존 데이터 체크하지 않음
            // checkExistingData() 제거
        }
    }

    // 수정 모드인지 확인
    private fun checkEditMode() {
        isEditMode = intent.getBooleanExtra("EDIT_MODE", false)
        if (isEditMode) {
            editDate = intent.getStringExtra("EDIT_DATE") ?: ""
            editTimeOfDay = intent.getStringExtra("EDIT_TIME_OF_DAY") ?: ""
        }
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

        // 수정 모드에 따라 버튼 텍스트 변경
        if (isEditMode) {
            btnSave.text = "♪ 감정 기록 수정"
            loadExistingDataForEdit()
        } else {
            btnSave.text = "♪ 감정 기록 완료"
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }
        // 🌅아침, 🌞오후, 🌙저녁, 🌃밤 4타임 선택
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
        // 시간대는 기본 선택하지 않음 (사용자가 직접 선택하도록)
        // 별도의 제한 없이 자유롭게 선택 가능
    }

    // 수정을 위해 기존 데이터 로드
    private fun loadExistingDataForEdit() {
        if (editDate.isNotEmpty() && editTimeOfDay.isNotEmpty()) {
            val fileName = "${editDate}_${editTimeOfDay}.txt"
            try {
                val fileInput = openFileInput(fileName)
                val content = fileInput.bufferedReader().use { it.readText() }
                fileInput.close()

                // 파일 내용을 파싱해서 UI에 설정
                parseAndSetData(content)

            } catch (e: Exception) {
                Toast.makeText(this, "기존 데이터를 불러올 수 없습니다", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    // 파일 내용을 파싱해서 UI에 설정
    private fun parseAndSetData(content: String) {
        try {
            val lines = content.split("\n")
            val dataMap = mutableMapOf<String, String>()

            for (line in lines) {
                if (line.contains(":")) {
                    val parts = line.split(":", limit = 2)
                    if (parts.size == 2) {
                        dataMap[parts[0].trim()] = parts[1].trim()
                    }
                }
            }

            // 감정 설정
            val emotionText = dataMap["감정"] ?: ""
            if (emotionText.isNotEmpty()) {
                val emotionParts = emotionText.split(" ")
                val emotionSymbol = emotionParts.getOrNull(0) ?: ""

                when (emotionSymbol) {
                    "♪" -> findViewById<RadioButton>(R.id.rbJoy).isChecked = true
                    "♩" -> findViewById<RadioButton>(R.id.rbPeace).isChecked = true
                    "♫" -> findViewById<RadioButton>(R.id.rbExcitement).isChecked = true
                    "♭" -> findViewById<RadioButton>(R.id.rbSadness).isChecked = true
                    "♯" -> findViewById<RadioButton>(R.id.rbAnger).isChecked = true
                    "𝄢" -> findViewById<RadioButton>(R.id.rbAnxiety).isChecked = true
                    "♡" -> findViewById<RadioButton>(R.id.rbLove).isChecked = true
                }
            }

            // 강도 설정
            val intensityText = dataMap["강도"] ?: ""
            val intensity = when {
                intensityText.contains("pp") -> 0
                intensityText.contains("p") -> 1
                intensityText.contains("mf") -> 2
                intensityText.contains("f") -> 3
                intensityText.contains("ff") -> 4
                else -> 2
            }
            seekBarIntensity.progress = intensity
            tvIntensityLevel.text = intensityLevels[intensity]

            // 시간대 설정
            when (editTimeOfDay) {
                "morning" -> findViewById<RadioButton>(R.id.rbMorning).isChecked = true
                "afternoon" -> findViewById<RadioButton>(R.id.rbAfternoon).isChecked = true
                "evening" -> findViewById<RadioButton>(R.id.rbEvening).isChecked = true
                "night" -> findViewById<RadioButton>(R.id.rbNight).isChecked = true
            }

            // 태그 설정
            val tagsText = dataMap["태그"] ?: ""
            if (tagsText.isNotEmpty()) {
                val tags = tagsText.split(", ").map { it.replace("#", "") }
                cbWork.isChecked = tags.contains("업무")
                cbExercise.isChecked = tags.contains("운동")
                cbRest.isChecked = tags.contains("휴식")
                cbMeeting.isChecked = tags.contains("모임")
                cbFamily.isChecked = tags.contains("가족")
                cbStudy.isChecked = tags.contains("공부")
            }

            // 메모 설정
            val memo = dataMap["메모"] ?: ""
            etMemo.setText(memo)

        } catch (e: Exception) {
            Toast.makeText(this, "데이터 파싱 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    // 기존 감정 데이터가 있는지 확인하고 로드
    // 이제 이 메서드는 사용하지 않음 (자유로운 덮어쓰기 허용)

    private fun getCurrentTimeOfDay(): String {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (currentHour) {
            in 5..11 -> "morning"
            in 12..17 -> "afternoon"
            in 18..21 -> "evening"
            else -> "night"
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

        // 4. 파일 저장
        val saveSuccess = fileManager.saveEmotionData(emotionData)

        if (saveSuccess) {
            // 5. 성공 메시지
            val timeText = getTimeOfDayKorean(emotionData.timeOfDay)
            val message = if (isEditMode) {
                "$timeText 감정이 수정되었어요! ♪"
            } else {
                "$timeText 감정이 저장되었어요! ♪"
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

            // 6. 화면 닫기
            setResult(RESULT_OK) // MainActivity에 저장 완료 알림
            finish()
        } else {
            Toast.makeText(this, "저장 중 오류가 발생했습니다 😢", Toast.LENGTH_SHORT).show()
        }
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

        // 현재 날짜 (수정 모드면 수정할 날짜 사용)
        val currentDate = if (isEditMode) editDate else SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
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