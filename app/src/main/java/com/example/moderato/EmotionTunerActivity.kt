package com.example.moderato

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class EmotionTunerActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var tvCurrentEmotion: TextView
    private lateinit var tvCurrentEmotionIcon: TextView
    private lateinit var tvTargetEmotion: TextView
    private lateinit var tvTargetEmotionIcon: TextView

    private lateinit var seekBarCurrent: SeekBar
    private lateinit var seekBarTarget: SeekBar
    private lateinit var progressBarTuning: ProgressBar

    private lateinit var tvCurrentIntensity: TextView
    private lateinit var tvTargetIntensity: TextView
    private lateinit var tvTuningStatus: TextView

    private lateinit var btnStartTuning: Button
    private lateinit var btnSelectActivity: Button
    private lateinit var tvActivityGuide: TextView
    private lateinit var linearTuningProgress: LinearLayout

    // 감정 데이터
    private var currentEmotionSymbol = "♪"
    private var currentEmotionName = "기쁨"
    private var targetEmotionSymbol = "♩"
    private var targetEmotionName = "평온"

    private val intensityLevels = arrayOf("pp", "p", "mf", "f", "ff")
    private val intensityTexts = arrayOf("매우 여리게", "여리게", "보통으로", "세게", "매우 세게")

    // 타이머 관련
    private var tuningHandler: Handler? = null
    private var currentStep = 0
    private var totalSteps = 10
    private var istuning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emotion_tuner)

        // 인텐트에서 현재 감정 정보 받기
        currentEmotionSymbol = intent.getStringExtra("CURRENT_EMOTION_SYMBOL") ?: "♪"
        currentEmotionName = intent.getStringExtra("CURRENT_EMOTION_NAME") ?: "기쁨"

        initViews()
        setupClickListeners()
        setupSeekBars()
        loadEmotionData()
        recommendTargetEmotion()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvCurrentEmotion = findViewById(R.id.tvCurrentEmotion)
        tvCurrentEmotionIcon = findViewById(R.id.tvCurrentEmotionIcon)
        tvTargetEmotion = findViewById(R.id.tvTargetEmotion)
        tvTargetEmotionIcon = findViewById(R.id.tvTargetEmotionIcon)

        seekBarCurrent = findViewById(R.id.seekBarCurrent)
        seekBarTarget = findViewById(R.id.seekBarTarget)
        progressBarTuning = findViewById(R.id.progressBarTuning)

        tvCurrentIntensity = findViewById(R.id.tvCurrentIntensity)
        tvTargetIntensity = findViewById(R.id.tvTargetIntensity)
        tvTuningStatus = findViewById(R.id.tvTuningStatus)

        btnStartTuning = findViewById(R.id.btnStartTuning)
        btnSelectActivity = findViewById(R.id.btnSelectActivity)
        tvActivityGuide = findViewById(R.id.tvActivityGuide)
        linearTuningProgress = findViewById(R.id.linearTuningProgress)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnStartTuning.setOnClickListener {
            if (!istuning) {
                startEmotionTuning()
            } else {
                stopEmotionTuning()
            }
        }

        btnSelectActivity.setOnClickListener {
            showActivitySelectionDialog()
        }

        // 목표 감정 아이콘 클릭으로 변경
        tvTargetEmotionIcon.setOnClickListener {
            showTargetEmotionSelectionDialog()
        }
    }

    private fun setupSeekBars() {
        // 현재 감정 강도 SeekBar (수업 6주차 내용)
        seekBarCurrent.max = 4
        seekBarCurrent.progress = 2 // 기본값 mf
        seekBarCurrent.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateCurrentIntensityDisplay(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // 목표 감정 강도 SeekBar
        seekBarTarget.max = 4
        seekBarTarget.progress = 2 // 기본값 mf
        seekBarTarget.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateTargetIntensityDisplay(progress)
                generateActivityGuide()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // 조율 진행도 ProgressBar 초기화
        progressBarTuning.max = totalSteps
        progressBarTuning.progress = 0
    }

    private fun loadEmotionData() {
        // 현재 감정 표시
        tvCurrentEmotion.text = currentEmotionName
        tvCurrentEmotionIcon.text = currentEmotionSymbol
        tvCurrentEmotionIcon.setTextColor(getEmotionColor(currentEmotionSymbol))

        updateCurrentIntensityDisplay(seekBarCurrent.progress)
        updateTargetIntensityDisplay(seekBarTarget.progress)
    }

    private fun recommendTargetEmotion() {
        // 수업 3주차 when문 활용 - 현재 감정에 따라 추천 목표 감정
        val recommendation = when(currentEmotionSymbol) {
            "♯" -> Pair("♩", "평온") // 화남 → 평온
            "♭" -> Pair("♪", "기쁨") // 슬픔 → 기쁨
            "𝄢" -> Pair("♩", "평온") // 불안 → 평온
            "♪" -> Pair("♩", "평온") // 기쁨 → 평온 (너무 흥분 상태)
            "♫" -> Pair("♩", "평온") // 설렘 → 평온
            "♩" -> Pair("♩", "평온") // 이미 평온
            "♡" -> Pair("♡", "사랑") // 사랑은 유지
            else -> Pair("♩", "평온")
        }

        targetEmotionSymbol = recommendation.first
        targetEmotionName = recommendation.second

        tvTargetEmotion.text = targetEmotionName
        tvTargetEmotionIcon.text = targetEmotionSymbol
        tvTargetEmotionIcon.setTextColor(getEmotionColor(targetEmotionSymbol))

        generateActivityGuide()
    }

    private fun updateCurrentIntensityDisplay(progress: Int) {
        tvCurrentIntensity.text = "${intensityLevels[progress]} (${intensityTexts[progress]})"
    }

    private fun updateTargetIntensityDisplay(progress: Int) {
        tvTargetIntensity.text = "${intensityLevels[progress]} (${intensityTexts[progress]})"
    }

    private fun generateActivityGuide() {
        // 수업 3주차 when문과 문자열 처리 활용
        val currentIntensity = seekBarCurrent.progress
        val targetIntensity = seekBarTarget.progress

        val guide = when {
            currentEmotionSymbol == "♯" && targetEmotionSymbol == "♩" -> {
                // 화남 → 평온
                buildString {
                    append("🔥➜🌊 화남을 평온으로 조절하기\n\n")
                    append("1️⃣ 4-7-8 호흡법 (5분)\n")
                    append("2️⃣ 차가운 물로 손목 식히기\n")
                    append("3️⃣ 클래식 음악 감상\n")
                    append("4️⃣ 천천히 걷기\n\n")
                    append("💡 격한 감정을 차분히 가라앉히는 시간이에요")
                }
            }
            currentEmotionSymbol == "♭" && targetEmotionSymbol == "♪" -> {
                // 슬픔 → 기쁨
                buildString {
                    append("😢➜😊 슬픔을 기쁨으로 바꾸기\n\n")
                    append("1️⃣ 감사한 일 3가지 떠올리기\n")
                    append("2️⃣ 밝은 음악 듣기\n")
                    append("3️⃣ 좋아하는 사람에게 연락\n")
                    append("4️⃣ 따뜻한 차 마시기\n\n")
                    append("💡 작은 기쁨들을 하나씩 모아보세요")
                }
            }
            currentEmotionSymbol == "𝄢" && targetEmotionSymbol == "♩" -> {
                // 불안 → 평온
                buildString {
                    append("😰➜😌 불안을 평온으로 진정시키기\n\n")
                    append("1️⃣ 5-4-3-2-1 그라운딩 기법\n")
                    append("2️⃣ 복식호흡 연습\n")
                    append("3️⃣ 부드러운 스트레칭\n")
                    append("4️⃣ 현재 순간에 집중하기\n\n")
                    append("💡 지금 여기에 안전하게 머물러보세요")
                }
            }
            currentIntensity > targetIntensity -> {
                // 강도 낮추기
                "🎚️ 감정 강도를 ${intensityLevels[targetIntensity]}로 낮춰보세요\n\n" +
                        "• 깊게 숨쉬기\n• 어깨 힘 빼기\n• 편안한 자세 취하기"
            }
            currentIntensity < targetIntensity -> {
                // 강도 높이기
                "⚡ 감정 에너지를 ${intensityLevels[targetIntensity]}로 높여보세요\n\n" +
                        "• 활발한 움직임\n• 깊게 스트레칭\n• 좋아하는 음악"
            }
            else -> {
                "✨ 현재 감정 상태가 좋아보여요!\n균형을 유지해주세요."
            }
        }

        tvActivityGuide.text = guide
    }

    private fun startEmotionTuning() {
        istuning = true
        currentStep = 0
        btnStartTuning.text = "⏸️ 조율 중단"
        btnStartTuning.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light))

        linearTuningProgress.visibility = android.view.View.VISIBLE
        tvTuningStatus.text = "🎼 감정 조율을 시작합니다..."

        // 수업 6주차 Handler 활용 - 1초마다 진행
        tuningHandler = Handler(Looper.getMainLooper())
        startTuningStep()
    }

    private fun startTuningStep() {
        tuningHandler?.postDelayed({
            if (istuning && currentStep < totalSteps) {
                currentStep++
                progressBarTuning.progress = currentStep

                // 단계별 메시지 (수업 3주차 when문 활용)
                val statusMessage = when(currentStep) {
                    1 -> "🌟 조율 준비 중..."
                    2 -> "🧘‍♀️ 현재 감정 인식하기"
                    3 -> "💨 깊게 숨쉬기"
                    4 -> "🎯 목표 감정 설정"
                    5 -> "🔄 감정 전환 시작"
                    6 -> "⚖️ 균형 맞추기"
                    7 -> "🎼 감정 화음 조절"
                    8 -> "✨ 새로운 감정 안정화"
                    9 -> "🎵 최종 조율 중"
                    10 -> "🎊 조율 완료!"
                    else -> "🎼 조율 진행 중..."
                }

                tvTuningStatus.text = "$statusMessage ($currentStep/$totalSteps)"

                if (currentStep < totalSteps) {
                    startTuningStep() // 재귀적으로 다음 단계 진행
                } else {
                    finishEmotionTuning()
                }
            }
        }, 3000) // 3초마다 단계 진행
    }

    private fun finishEmotionTuning() {
        istuning = false
        btnStartTuning.text = "🎼 조율 시작"
        btnStartTuning.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_pink))

        tvTuningStatus.text = "✨ 감정 조율이 완료되었습니다!"

        // 성공 메시지 표시 (수업 7주차 Toast 활용)
        Toast.makeText(this,
            "🎵 ${currentEmotionName}에서 ${targetEmotionName}으로 조율 완료! 기분이 어떠신가요?",
            Toast.LENGTH_LONG).show()

        // 조율 결과 저장 제안
        showSaveResultDialog()
    }

    private fun stopEmotionTuning() {
        istuning = false
        tuningHandler?.removeCallbacksAndMessages(null)

        btnStartTuning.text = "🎼 조율 시작"
        btnStartTuning.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_pink))

        tvTuningStatus.text = "⏸️ 조율이 중단되었습니다"
        progressBarTuning.progress = 0
        currentStep = 0

        Toast.makeText(this, "조율이 중단되었습니다. 언제든 다시 시작하세요!", Toast.LENGTH_SHORT).show()
    }

    private fun showTargetEmotionSelectionDialog() {
        // 수업 7주차 AlertDialog 활용
        val emotions = arrayOf("♪ 기쁨", "♩ 평온", "♫ 설렘", "♭ 슬픔", "♯ 화남", "𝄢 불안", "♡ 사랑")
        val emotionSymbols = arrayOf("♪", "♩", "♫", "♭", "♯", "𝄢", "♡")
        val emotionNames = arrayOf("기쁨", "평온", "설렘", "슬픔", "화남", "불안", "사랑")

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("🎯 목표 감정 선택")
        builder.setItems(emotions) { _, which ->
            targetEmotionSymbol = emotionSymbols[which]
            targetEmotionName = emotionNames[which]

            tvTargetEmotion.text = targetEmotionName
            tvTargetEmotionIcon.text = targetEmotionSymbol
            tvTargetEmotionIcon.setTextColor(getEmotionColor(targetEmotionSymbol))

            generateActivityGuide()

            Toast.makeText(this, "목표 감정이 ${targetEmotionName}으로 설정되었습니다", Toast.LENGTH_SHORT).show()
        }
        builder.show()
    }

    private fun showActivitySelectionDialog() {
        // 추가 조절 활동 선택
        val activities = arrayOf(
            "🧘‍♀️ 3분 명상",
            "🎵 음악 감상",
            "🚶‍♀️ 가벼운 산책",
            "💨 호흡 운동",
            "✍️ 감정 일기",
            "🎨 간단한 그리기"
        )

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("🎭 추가 조절 활동")
        builder.setItems(activities) { _, which ->
            val selectedActivity = activities[which]
            Toast.makeText(this, "${selectedActivity}을(를) 시작해보세요!", Toast.LENGTH_SHORT).show()

            // 실제로는 각 활동별 상세 가이드나 타이머 실행
            when(which) {
                0 -> startMeditationTimer()
                1 -> suggestMusic()
                2 -> suggestWalk()
                3 -> startBreathingExercise()
                4 -> openEmotionDiary()
                5 -> openDrawingActivity()
            }
        }
        builder.show()
    }

    private fun showSaveResultDialog() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("📝 조율 결과 저장")
        builder.setMessage("이번 감정 조율 경험을 기록하시겠어요?\n나중에 비슷한 상황에서 도움이 될 수 있어요.")
        builder.setPositiveButton("저장하기") { _, _ ->
            saveTuningResult()
        }
        builder.setNegativeButton("건너뛰기", null)
        builder.show()
    }

    private fun saveTuningResult() {
        // 수업 9주차 파일 처리 활용 - 조율 결과 저장
        try {
            val fileName = "tuning_history.txt"
            val content = buildString {
                append("날짜: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}\n")
                append("시작 감정: $currentEmotionSymbol $currentEmotionName (${intensityLevels[seekBarCurrent.progress]})\n")
                append("목표 감정: $targetEmotionSymbol $targetEmotionName (${intensityLevels[seekBarTarget.progress]})\n")
                append("조율 완료: 성공\n")
                append("소요 시간: ${totalSteps * 3}초\n")
                append("---\n")
            }

            val fileOutput = openFileOutput(fileName, android.content.Context.MODE_APPEND)
            fileOutput.write(content.toByteArray())
            fileOutput.close()

            Toast.makeText(this, "✅ 조율 결과가 저장되었습니다!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "저장 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
        }
    }

    // 각 활동별 구현 (간단한 예시)
    private fun startMeditationTimer() {
        Toast.makeText(this, "🧘‍♀️ 3분 명상을 시작합니다. 편안히 앉아서 호흡에 집중해보세요.", Toast.LENGTH_LONG).show()
    }

    private fun suggestMusic() {
        val musicSuggestion = when(targetEmotionSymbol) {
            "♩" -> "차분한 클래식이나 자연 소리를 들어보세요 🎼"
            "♪" -> "밝고 경쾌한 팝송이나 재즈를 들어보세요 🎵"
            "♡" -> "따뜻한 발라드나 로맨틱한 음악을 들어보세요 💕"
            else -> "좋아하는 음악을 들으며 마음을 다스려보세요 🎶"
        }
        Toast.makeText(this, musicSuggestion, Toast.LENGTH_LONG).show()
    }

    private fun suggestWalk() {
        Toast.makeText(this, "🚶‍♀️ 10분 정도 천천히 걸으며 주변을 관찰해보세요. 신선한 공기를 마셔보세요!", Toast.LENGTH_LONG).show()
    }

    private fun startBreathingExercise() {
        Toast.makeText(this, "💨 4초 들이쉬고, 7초 참고, 8초 내쉬는 호흡을 5회 반복해보세요.", Toast.LENGTH_LONG).show()
    }

    private fun openEmotionDiary() {
        Toast.makeText(this, "✍️ 지금 느끼는 감정을 자유롭게 글로 표현해보세요.", Toast.LENGTH_LONG).show()
    }

    private fun openDrawingActivity() {
        Toast.makeText(this, "🎨 현재 기분을 색깔과 모양으로 그려보세요. 정답은 없어요!", Toast.LENGTH_LONG).show()
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

    override fun onDestroy() {
        super.onDestroy()
        tuningHandler?.removeCallbacksAndMessages(null)
    }
}