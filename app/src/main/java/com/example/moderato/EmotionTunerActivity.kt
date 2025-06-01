package com.example.moderato

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class EmotionTunerActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var tvCurrentEmotion: TextView
    private lateinit var tvCurrentEmotionIcon: TextView
    private lateinit var tvTargetEmotion: TextView
    private lateinit var tvTargetEmotionIcon: TextView

    private lateinit var seekBarTarget: SeekBar  // 현재 감정 SeekBar 제거!
    private lateinit var progressBarTuning: ProgressBar

    private lateinit var tvCurrentIntensity: TextView
    private lateinit var tvTargetIntensity: TextView
    private lateinit var tvTuningStatus: TextView

    private lateinit var btnStartTuning: Button
    private lateinit var btnSelectActivity: Button
    private lateinit var tvActivityGuide: TextView
    private lateinit var linearTuningProgress: LinearLayout

    // 조율 단계 관련 (수업 4주차 - 위젯 활용)
    private lateinit var currentStepContainer: LinearLayout
    private lateinit var tvStepTitle: TextView
    private lateinit var tvStepInstruction: TextView
    private lateinit var btnStepNext: Button
    private lateinit var btnStepComplete: Button

    // 감정 데이터
    private var currentEmotionSymbol = "♪"
    private var currentEmotionName = "기쁨"
    private var currentIntensity = 3  // 고정값! (파일에서 읽어온 실제 강도)
    private var targetEmotionSymbol = "♩"
    private var targetEmotionName = "평온"

    private val intensityLevels = arrayOf("pp", "p", "mf", "f", "ff")
    private val intensityTexts = arrayOf("매우 여리게", "여리게", "보통으로", "세게", "매우 세게")

    // 조율 단계 관련 (수업 3주차 - 배열 활용)
    private var currentStepIndex = 0
    private var tuningSteps: List<TuningStep> = listOf()  // var로 변경하여 재할당 가능
    private var isActiveTuning = false

    // 수업 3주차 - 데이터 클래스 활용
    data class TuningStep(
        val title: String,
        val instruction: String,
        val duration: String,
        val isInteractive: Boolean = true
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emotion_tuner)

        // 인텐트에서 현재 감정 정보 받기 (수업 11주차 - 액티비티 간 데이터 전달)
        currentEmotionSymbol = intent.getStringExtra("CURRENT_EMOTION_SYMBOL") ?: "♪"
        currentEmotionName = intent.getStringExtra("CURRENT_EMOTION_NAME") ?: "기쁨"

        initViews()
        setupClickListeners()
        setupSeekBars()
        loadCurrentEmotionData()  // 현재 감정을 파일에서 읽어와서 고정
        recommendTargetEmotion()
        setupTuningSteps()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvCurrentEmotion = findViewById(R.id.tvCurrentEmotion)
        tvCurrentEmotionIcon = findViewById(R.id.tvCurrentEmotionIcon)
        tvTargetEmotion = findViewById(R.id.tvTargetEmotion)
        tvTargetEmotionIcon = findViewById(R.id.tvTargetEmotionIcon)

        seekBarTarget = findViewById(R.id.seekBarTarget)  // 목표 감정만!
        progressBarTuning = findViewById(R.id.progressBarTuning)

        tvCurrentIntensity = findViewById(R.id.tvCurrentIntensity)
        tvTargetIntensity = findViewById(R.id.tvTargetIntensity)
        tvTuningStatus = findViewById(R.id.tvTuningStatus)

        btnStartTuning = findViewById(R.id.btnStartTuning)
        btnSelectActivity = findViewById(R.id.btnSelectActivity)
        tvActivityGuide = findViewById(R.id.tvActivityGuide)
        linearTuningProgress = findViewById(R.id.linearTuningProgress)

        // 동적으로 단계별 조율 UI 생성 (수업 5주차 - 동적 레이아웃)
        createStepByStepUI()
    }

    // 수업 5주차 - Kotlin 코드로 화면 만들기 응용
    private fun createStepByStepUI() {
        currentStepContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            background = ContextCompat.getDrawable(this@EmotionTunerActivity, R.drawable.card_background)
            setPadding(40, 30, 40, 30)
            visibility = android.view.View.GONE
        }

        tvStepTitle = TextView(this).apply {
            textSize = 18f
            setTextColor(ContextCompat.getColor(this@EmotionTunerActivity, R.color.text_primary))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 24 }
        }

        tvStepInstruction = TextView(this).apply {
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@EmotionTunerActivity, R.color.text_primary))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 32 }
            setLineSpacing(0f, 1.3f)  // lineSpacingMultiplier 대신 setLineSpacing 사용
        }

        val buttonContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        btnStepNext = Button(this).apply {
            text = "✅ 완료하고 다음 단계"
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@EmotionTunerActivity, R.color.text_primary))
            background = ContextCompat.getDrawable(this@EmotionTunerActivity, R.drawable.gradient_button_bg)
            layoutParams = LinearLayout.LayoutParams(
                0, 112, 1f
            ).apply {
                rightMargin = 16
            }
            setOnClickListener { proceedToNextStep() }
        }

        btnStepComplete = Button(this).apply {
            text = "🎵 조율 완료"
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@EmotionTunerActivity, R.color.text_primary))
            background = ContextCompat.getDrawable(this@EmotionTunerActivity, R.drawable.chord_button_bg)
            layoutParams = LinearLayout.LayoutParams(
                0, 112, 1f
            )
            visibility = android.view.View.GONE
            setOnClickListener { completeTuning() }
        }

        buttonContainer.addView(btnStepNext)
        buttonContainer.addView(btnStepComplete)

        currentStepContainer.addView(tvStepTitle)
        currentStepContainer.addView(tvStepInstruction)
        currentStepContainer.addView(buttonContainer)

        // 메인 레이아웃에 추가
        val mainLayout = findViewById<LinearLayout>(R.id.linearTuningProgress).parent as LinearLayout
        val insertIndex = mainLayout.indexOfChild(findViewById<LinearLayout>(R.id.linearTuningProgress)) + 1
        mainLayout.addView(currentStepContainer, insertIndex)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            if (isActiveTuning) {
                showExitConfirmDialog()  // 수업 7주차 - 대화상자 활용
            } else {
                finish()
            }
        }

        btnStartTuning.setOnClickListener {
            if (!isActiveTuning) {
                startStepByStepTuning()
            } else {
                stopTuning()
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
        // 목표 감정 강도 SeekBar만 남김
        seekBarTarget.max = 4
        seekBarTarget.progress = 2 // 기본값 mf
        seekBarTarget.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateTargetIntensityDisplay(progress)
                generateActivityGuide()
                setupTuningSteps()  // 목표가 바뀔 때마다 단계 재구성
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        progressBarTuning.max = 0  // 단계 수에 따라 동적으로 설정됨
        progressBarTuning.progress = 0
    }

    // 수업 9주차 - 파일 처리 활용
    private fun loadCurrentEmotionData() {
        // 실제로는 오늘 날짜와 시간대로 파일에서 강도를 읽어와야 함
        // 여기서는 감정별 기본 강도로 설정
        currentIntensity = when(currentEmotionSymbol) {
            "♯" -> 4  // 화남은 보통 강함
            "♭" -> 2  // 슬픔은 보통 약함
            "𝄢" -> 3  // 불안은 보통
            "♪" -> 4  // 기쁨은 강함
            "♫" -> 4  // 설렘은 강함
            "♩" -> 2  // 평온은 약함
            "♡" -> 3  // 사랑은 보통
            else -> 3
        }

        tvCurrentEmotion.text = currentEmotionName
        tvCurrentEmotionIcon.text = currentEmotionSymbol
        tvCurrentEmotionIcon.setTextColor(getEmotionColor(currentEmotionSymbol))
        tvCurrentIntensity.text = "${intensityLevels[currentIntensity - 1]} (${intensityTexts[currentIntensity - 1]})"
    }

    private fun recommendTargetEmotion() {
        // 수업 3주차 when문 활용 - 현재 감정에 따라 추천 목표 감정
        val recommendation = when(currentEmotionSymbol) {
            "♯" -> Triple("♩", "평온", 2) // 화남 → 평온 (약하게)
            "♭" -> Triple("♪", "기쁨", 3) // 슬픔 → 기쁨 (보통으로)
            "𝄢" -> Triple("♩", "평온", 3) // 불안 → 평온 (보통으로)
            "♪" -> {
                if (currentIntensity >= 4) Triple("♩", "평온", 3) // 너무 흥분 → 평온
                else Triple("♪", "기쁨", currentIntensity) // 적당한 기쁨 유지
            }
            "♫" -> Triple("♩", "평온", 2) // 설렘 → 평온 (진정)
            "♩" -> Triple("♩", "평온", 3) // 이미 평온 (약간 강화)
            "♡" -> Triple("♡", "사랑", 3) // 사랑은 유지
            else -> Triple("♩", "평온", 3)
        }

        targetEmotionSymbol = recommendation.first
        targetEmotionName = recommendation.second
        seekBarTarget.progress = recommendation.third - 1  // SeekBar는 0부터 시작

        tvTargetEmotion.text = targetEmotionName
        tvTargetEmotionIcon.text = targetEmotionSymbol
        tvTargetEmotionIcon.setTextColor(getEmotionColor(targetEmotionSymbol))

        updateTargetIntensityDisplay(seekBarTarget.progress)
        generateActivityGuide()
        setupTuningSteps()
    }

    private fun updateTargetIntensityDisplay(progress: Int) {
        tvTargetIntensity.text = "${intensityLevels[progress]} (${intensityTexts[progress]})"
    }

    // 수업 3주차 - when문과 배열 활용한 조율 단계 구성
    private fun setupTuningSteps() {
        val targetIntensity = seekBarTarget.progress + 1

        tuningSteps = when {
            currentEmotionSymbol == "♯" && targetEmotionSymbol == "♩" -> {
                // 화남 → 평온 조율 단계
                listOf(
                    TuningStep(
                        "1단계: 현재 감정 인식",
                        "지금 화가 났다는 것을 인정해보세요.\n'나는 지금 화가 나 있구나'라고 속으로 말해보세요.\n\n화를 느끼는 것은 자연스러운 일입니다.",
                        "자유롭게"
                    ),
                    TuningStep(
                        "2단계: 4-7-8 호흡법",
                        "• 4초 동안 코로 숨을 들이마세요\n• 7초 동안 숨을 참으세요\n• 8초 동안 입으로 천천히 내쉬세요\n\n이것을 3회 반복해주세요.",
                        "약 2분"
                    ),
                    TuningStep(
                        "3단계: 신체 이완",
                        "어깨와 목의 긴장을 풀어주세요.\n\n• 어깨를 올렸다 내리기 (5회)\n• 목을 좌우로 천천히 돌리기\n• 주먹을 꽉 쥐었다 펴기 (5회)",
                        "약 1분"
                    ),
                    TuningStep(
                        "4단계: 생각 정리",
                        "화의 원인을 객관적으로 생각해보세요.\n\n• 정말 화낼 만한 일인가요?\n• 이 감정이 나에게 도움이 될까요?\n• 더 좋은 해결책은 없을까요?",
                        "자유롭게"
                    ),
                    TuningStep(
                        "5단계: 평온 상상",
                        "마음이 평온한 상태를 상상해보세요.\n\n차분한 호수나 조용한 숲을 떠올리며,\n그 평온함이 마음 속으로 스며드는 것을 느껴보세요.",
                        "약 1분"
                    )
                )
            }
            currentEmotionSymbol == "♭" && targetEmotionSymbol == "♪" -> {
                // 슬픔 → 기쁨 조율 단계
                listOf(
                    TuningStep(
                        "1단계: 슬픔 받아들이기",
                        "지금 슬픈 마음을 있는 그대로 받아들여보세요.\n슬픔도 소중한 감정 중 하나입니다.\n\n억지로 밀어내지 말고 잠시 함께 있어보세요.",
                        "자유롭게"
                    ),
                    TuningStep(
                        "2단계: 감사한 일 떠올리기",
                        "힘들지만 감사한 일 3가지를 떠올려보세요.\n\n작은 것이라도 괜찮습니다:\n• 오늘 마신 따뜻한 차\n• 안부를 묻는 사람\n• 지금 이 순간",
                        "약 2분"
                    ),
                    TuningStep(
                        "3단계: 좋은 기억 소환",
                        "기분 좋았던 기억을 하나 떠올려보세요.\n\n그때의 느낌, 소리, 냄새까지\n생생하게 기억해보세요.\n그 기쁨이 지금도 가능함을 느껴보세요.",
                        "약 2분"
                    ),
                    TuningStep(
                        "4단계: 미소 짓기",
                        "거울을 보며 (또는 상상으로)\n작은 미소를 지어보세요.\n\n억지로라도 미소를 지으면\n뇌가 기쁨을 느끼기 시작합니다.",
                        "약 1분"
                    )
                )
            }
            currentEmotionSymbol == "𝄢" && targetEmotionSymbol == "♩" -> {
                // 불안 → 평온 조율 단계
                listOf(
                    TuningStep(
                        "1단계: 현재에 집중 (5-4-3-2-1)",
                        "지금 이 순간에 집중해보세요:\n\n• 보이는 것 5가지\n• 들리는 것 4가지\n• 만져지는 것 3가지\n• 냄새나는 것 2가지\n• 맛나는 것 1가지",
                        "약 3분"
                    ),
                    TuningStep(
                        "2단계: 복식호흡",
                        "배로 숨쉬기를 연습해보세요.\n\n• 한 손은 가슴에, 한 손은 배에\n• 배가 올라오도록 깊게 들이마시기\n• 천천히 내쉬면서 배가 들어가게\n\n5회 반복해주세요.",
                        "약 2분"
                    ),
                    TuningStep(
                        "3단계: 안전 확인",
                        "지금 이 순간 당신은 안전합니다.\n\n주변을 둘러보고 확인해보세요:\n• 위험한 것이 있나요?\n• 지금 당장 해결해야 할 일이 있나요?\n\n'지금 여기는 안전하다'고 말해보세요.",
                        "자유롭게"
                    ),
                    TuningStep(
                        "4단계: 점진적 이완",
                        "발끝부터 머리까지 차례로 힘을 빼보세요.\n\n• 발가락에서 힘 빼기\n• 다리에서 힘 빼기\n• 허리, 어깨에서 힘 빼기\n• 얼굴 근육 이완하기",
                        "약 2분"
                    )
                )
            }
            currentIntensity > targetIntensity -> {
                // 강도 낮추기 (일반적인 경우)
                listOf(
                    TuningStep(
                        "1단계: 현재 강도 인식",
                        "지금 감정의 강도가 ${intensityLevels[currentIntensity-1]}라는 것을 인식해보세요.\n\n이 강도를 ${intensityLevels[targetIntensity-1]}로 낮춰보겠습니다.",
                        "자유롭게"
                    ),
                    TuningStep(
                        "2단계: 깊은 호흡",
                        "천천히 깊게 호흡하여 긴장을 풀어보세요.\n\n• 5초 들이마시기\n• 5초 참기\n• 5초 내쉬기\n\n3회 반복해주세요.",
                        "약 1분"
                    ),
                    TuningStep(
                        "3단계: 신체 이완",
                        "몸의 긴장을 풀어주세요.\n\n• 어깨 힘 빼기\n• 얼굴 근육 이완\n• 손과 발의 힘 빼기",
                        "약 1분"
                    )
                )
            }
            else -> {
                // 기본 단계
                listOf(
                    TuningStep(
                        "1단계: 현재 감정 확인",
                        "지금 ${currentEmotionName} 상태라는 것을 확인해보세요.\n\n이 감정을 ${targetEmotionName}으로 조율해보겠습니다.",
                        "자유롭게"
                    ),
                    TuningStep(
                        "2단계: 목표 상태 상상",
                        "${targetEmotionName} 상태가 어떤 느낌인지 상상해보세요.\n\n그 상태에서의 호흡, 자세, 표정을 떠올려보세요.",
                        "약 1분"
                    ),
                    TuningStep(
                        "3단계: 점진적 전환",
                        "천천히 목표 감정으로 마음을 이끌어보세요.\n\n급하지 않게, 자연스럽게 변화해보세요.",
                        "자유롭게"
                    )
                )
            }
        }

        progressBarTuning.max = tuningSteps.size
    }

    private fun startStepByStepTuning() {
        isActiveTuning = true
        currentStepIndex = 0

        btnStartTuning.text = "❌ 조율 중단"
        btnStartTuning.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light))

        linearTuningProgress.visibility = android.view.View.VISIBLE
        currentStepContainer.visibility = android.view.View.VISIBLE

        showCurrentStep()

        Toast.makeText(this, "🎼 단계별 감정 조율을 시작합니다. 천천히 따라해보세요!", Toast.LENGTH_LONG).show()
    }

    private fun showCurrentStep() {
        if (currentStepIndex < tuningSteps.size) {
            val step = tuningSteps[currentStepIndex]

            tvStepTitle.text = step.title
            tvStepInstruction.text = "${step.instruction}\n\n⏱️ 예상 소요시간: ${step.duration}"

            progressBarTuning.progress = currentStepIndex + 1
            tvTuningStatus.text = "${currentStepIndex + 1} / ${tuningSteps.size} 단계 진행 중"

            // 마지막 단계인지 확인
            if (currentStepIndex == tuningSteps.size - 1) {
                btnStepNext.visibility = android.view.View.GONE
                btnStepComplete.visibility = android.view.View.VISIBLE
            } else {
                btnStepNext.visibility = android.view.View.VISIBLE
                btnStepComplete.visibility = android.view.View.GONE
            }
        }
    }

    private fun proceedToNextStep() {
        currentStepIndex++

        if (currentStepIndex < tuningSteps.size) {
            showCurrentStep()
            Toast.makeText(this, "✅ ${currentStepIndex}단계 완료! 다음 단계로 넘어갑니다.", Toast.LENGTH_SHORT).show()
        } else {
            completeTuning()
        }
    }

    private fun completeTuning() {
        isActiveTuning = false

        btnStartTuning.text = "🎼 조율 시작"
        btnStartTuning.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_pink))

        currentStepContainer.visibility = android.view.View.GONE
        tvTuningStatus.text = "🎵 조율이 완료되었습니다!"
        progressBarTuning.progress = progressBarTuning.max

        // 수업 7주차 Toast 활용 - 성공 메시지
        Toast.makeText(this,
            "🎊 ${currentEmotionName}(${intensityLevels[currentIntensity-1]})에서 ${targetEmotionName}(${intensityLevels[seekBarTarget.progress]})으로 조율 완료!\n\n기분이 어떠신가요?",
            Toast.LENGTH_LONG).show()

        // 조율 결과 저장 제안
        showSaveResultDialog()
    }

    private fun stopTuning() {
        isActiveTuning = false

        btnStartTuning.text = "🎼 조율 시작"
        btnStartTuning.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_pink))

        currentStepContainer.visibility = android.view.View.GONE
        tvTuningStatus.text = "⏸️ 조율이 중단되었습니다"
        progressBarTuning.progress = 0
        currentStepIndex = 0

        Toast.makeText(this, "조율이 중단되었습니다. 언제든 다시 시작하세요!", Toast.LENGTH_SHORT).show()
    }

    // 수업 7주차 - 대화상자 활용
    private fun showExitConfirmDialog() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("🤔 조율 중단")
        builder.setMessage("조율을 중단하고 나가시겠어요?\n현재 진행상황이 저장되지 않습니다.")
        builder.setPositiveButton("나가기") { _, _ ->
            finish()
        }
        builder.setNegativeButton("계속하기", null)
        builder.show()
    }

    private fun generateActivityGuide() {
        // 기존 코드와 동일하지만 더 구체적으로
        val guide = when {
            currentEmotionSymbol == "♯" && targetEmotionSymbol == "♩" -> {
                "🔥➜🌊 화남을 평온으로 조절하기\n\n단계별로 차근차근 진행하면 화를 평온하게 가라앉힐 수 있어요.\n\n예상 소요시간: 5-10분"
            }
            currentEmotionSymbol == "♭" && targetEmotionSymbol == "♪" -> {
                "😢➜😊 슬픔을 기쁨으로 바꾸기\n\n작은 감사함부터 시작해서 조금씩 기쁨을 찾아보겠습니다.\n\n예상 소요시간: 5-8분"
            }
            currentEmotionSymbol == "𝄢" && targetEmotionSymbol == "♩" -> {
                "😰➜😌 불안을 평온으로 진정시키기\n\n현재 순간에 집중하며 안전감을 되찾아보겠습니다.\n\n예상 소요시간: 8-12분"
            }
            else -> {
                "✨ ${currentEmotionName}에서 ${targetEmotionName}으로\n\n단계적으로 감정 상태를 조율해보겠습니다.\n\n예상 소요시간: 3-5분"
            }
        }

        tvActivityGuide.text = guide
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
            setupTuningSteps()  // 목표 감정이 바뀌면 조율 단계도 재구성

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
                append("시작 감정: $currentEmotionSymbol $currentEmotionName (${intensityLevels[currentIntensity-1]})\n")
                append("목표 감정: $targetEmotionSymbol $targetEmotionName (${intensityLevels[seekBarTarget.progress]})\n")
                append("조율 단계: ${tuningSteps.size}단계 완료\n")
                append("완료 여부: 성공\n")
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
        // 더 이상 Handler가 없으므로 정리할 것 없음
    }
}