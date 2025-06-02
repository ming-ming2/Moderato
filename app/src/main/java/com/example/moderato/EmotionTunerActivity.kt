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
    private lateinit var tvActivityGuide: TextView
    private lateinit var linearTuningProgress: LinearLayout

    // 치료법 선택 관련 위젯들 (새로 추가)
    private lateinit var rgTherapyMethod: RadioGroup
    private lateinit var tvTherapyDescription: TextView

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

    // 선택된 치료법 저장 (새로 추가)
    private var selectedTherapyMethod = "DEFAULT"

    private val intensityLevels = arrayOf("pp", "p", "mf", "f", "ff")
    private val intensityTexts = arrayOf("매우 여리게", "여리게", "보통으로", "세게", "매우 세게")

    // 치료법별 설명 (수업 3주차 - 배열 활용)
    private val therapyDescriptions = mapOf(
        "DBT" to "💪 DBT 볼륨 조절법: 감정의 강도를 조절하여 압도되지 않도록 도와드립니다. 특히 강한 감정을 다루는 데 효과적입니다.",
        "CBT" to "🧠 CBT 조성 바꾸기: 상황을 바라보는 관점을 바꿔서 감정의 색깔을 바꿔봅니다. 부정적 생각을 균형잡힌 시각으로 전환합니다.",
        "ACT" to "🌊 ACT 자연스러운 전조: 감정을 억지로 바꾸려 하지 않고 자연스럽게 흘러가도록 도와드립니다. 감정과 평화롭게 공존하는 법을 배웁니다.",
        "DEFAULT" to "💡 기본 조율법: 누구나 쉽게 따라할 수 있는 단계별 감정 조율 방법입니다."
    )

    // 조율 단계 관련 (수업 3주차 - 배열 활용)
    private var currentStepIndex = 0
    private var tuningSteps: List<TuningStep> = listOf()  // var로 변경하여 재할당 가능
    private var isActiveTuning = false

    // CBT 인지 재구조화 관련 변수들 추가
    private var userNegativeThought = ""      // 1단계: 사용자가 입력한 부정적 생각
    private var userAlternativeThought = ""   // 3단계: 사용자가 입력한 대안적 생각
    private var userBalancedThought = ""      // 4단계: 최종 균형잡힌 생각
    private var isCBTInteractive = false      // CBT 대화상자 진행 중인지 확인

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
        tvActivityGuide = findViewById(R.id.tvActivityGuide)
        linearTuningProgress = findViewById(R.id.linearTuningProgress)

        // 치료법 선택 위젯들 추가
        rgTherapyMethod = findViewById(R.id.rgTherapyMethod)
        tvTherapyDescription = findViewById(R.id.tvTherapyDescription)

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

        // 목표 감정 아이콘 클릭으로 변경
        tvTargetEmotionIcon.setOnClickListener {
            showTargetEmotionSelectionDialog()
        }

        // 치료법 선택 리스너 (수업 4주차 - RadioGroup 활용)
        rgTherapyMethod.setOnCheckedChangeListener { _, checkedId ->
            selectedTherapyMethod = when(checkedId) {
                R.id.rbDBT -> "DBT"
                R.id.rbCBT -> "CBT"
                R.id.rbACT -> "ACT"
                R.id.rbDefault -> "DEFAULT"
                else -> "DEFAULT"
            }

            // 선택에 따라 설명 업데이트
            tvTherapyDescription.text = therapyDescriptions[selectedTherapyMethod]

            // 조율 단계 재구성
            setupTuningSteps()
            generateActivityGuide()

            // 선택 피드백
            val methodName = when(selectedTherapyMethod) {
                "DBT" -> "볼륨 조절법"
                "CBT" -> "조성 바꾸기"
                "ACT" -> "자연스러운 전조"
                else -> "기본 조율법"
            }
            Toast.makeText(this, "🎼 ${methodName}이 선택되었습니다", Toast.LENGTH_SHORT).show()
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
            // 먼저 치료법별로 분기 (수업 3주차 - when문 활용)
            selectedTherapyMethod == "DBT" -> createDBTSteps()
            selectedTherapyMethod == "CBT" -> createCBTSteps()
            selectedTherapyMethod == "ACT" -> createACTSteps()

            // 기존 감정별 분기 (DEFAULT 또는 치료법 선택 안됨)
            currentEmotionSymbol == "♯" && targetEmotionSymbol == "♩" -> {
                createDefaultAngerToCalm()
            }
            currentEmotionSymbol == "♭" && targetEmotionSymbol == "♪" -> {
                createDefaultSadnessToJoy()
            }
            currentEmotionSymbol == "𝄢" && targetEmotionSymbol == "♩" -> {
                createDefaultAnxietyToCalm()
            }
            currentIntensity > targetIntensity -> {
                createDefaultIntensityReduction()
            }
            else -> {
                createDefaultGeneralSteps()
            }
        }

        progressBarTuning.max = tuningSteps.size
    }

    // 치료법별 조율 단계 생성 메소드들 (수업 3주차 - 메소드와 when문 활용)

    private fun createDBTSteps(): List<TuningStep> {
        return when {
            currentEmotionSymbol == "♯" -> createDBTAngerSteps()
            currentEmotionSymbol == "♭" -> createDBTSadnessSteps()
            currentEmotionSymbol == "𝄢" -> createDBTAnxietySteps()
            else -> createDBTGeneralSteps()
        }
    }

    private fun createCBTSteps(): List<TuningStep> {
        return when {
            currentEmotionSymbol == "♯" -> createCBTAngerSteps()
            currentEmotionSymbol == "♭" -> createCBTSadnessSteps()
            currentEmotionSymbol == "𝄢" -> createCBTAnxietySteps()
            else -> createCBTGeneralSteps()
        }
    }

    private fun createACTSteps(): List<TuningStep> {
        return when {
            currentEmotionSymbol == "♯" -> createACTAngerSteps()
            currentEmotionSymbol == "♭" -> createACTSadnessSteps()
            currentEmotionSymbol == "𝄢" -> createACTAnxietySteps()
            else -> createACTGeneralSteps()
        }
    }

    // DBT 화남 조율 (볼륨 조절법)
    private fun createDBTAngerSteps(): List<TuningStep> {
        return listOf(
            TuningStep(
                "1단계: 🛑 페르마타 (감정 멈춤)",
                "지금 이 순간 화난 감정을 인식하고 잠시 멈춰봅시다.\n\n'내가 지금 화가 나 있구나'라고 속으로 말해보세요.\n\n🎼 페르마타처럼 이 순간을 길게 유지해보세요.",
                "자유롭게"
            ),
            TuningStep(
                "2단계: 🎚️ 감정 볼륨 낮추기 (TIP 기법)",
                "감정의 볼륨을 물리적으로 낮춰봅시다:\n\n• 차가운 물로 얼굴 씻기 (Temperature)\n• 제자리에서 30초 뛰기 (Intense exercise)\n• 4-7-8 호흡 3회 (Paced breathing)\n• 주먹 쥐었다 펴기 5회 (Paired muscle)",
                "약 3분"
            ),
            TuningStep(
                "3단계: 🎼 반대 행동하기 (Opposite Action)",
                "화남과 반대되는 행동을 해봅시다:\n\n• 화날 때 → 부드럽게 말하기\n• 소리치고 싶을 때 → 속삭이기\n• 공격하고 싶을 때 → 감사 표현하기\n\n🎵 분노의 포르테를 피아노로 바꿔보세요.",
                "약 2분"
            ),
            TuningStep(
                "4단계: 🎚️ 볼륨 점검하기",
                "지금 감정의 볼륨이 어느 정도인지 확인해보세요.\n\n처음 ${intensityLevels[currentIntensity-1]}에서 얼마나 낮아졌나요?\n\n목표는 ${intensityLevels[seekBarTarget.progress]}입니다.",
                "자유롭게"
            )
        )
    }

    // CBT 전용 단계 생성 메소드들 (Enhanced with Interactive Features)
    private fun createCBTAngerSteps(): List<TuningStep> {
        return listOf(
            TuningStep(
                "1단계: 🎼 부정적 생각 포착하기",
                "화가 나게 만드는 생각을 찾아봅시다.\n\n'어떤 생각이 지금 화나게 하고 있나요?'\n\n생각을 입력해보세요.",
                "자유롭게"
            ),
            TuningStep(
                "2단계: 🧠 생각 검증하기",
                "입력하신 생각을 함께 검토해봅시다.\n\n이 생각이 정말 100% 사실인지 확인해보겠습니다.",
                "약 2분"
            ),
            TuningStep(
                "3단계: 🎵 대안적 관점 찾기",
                "같은 상황을 다르게 해석할 수 있는 방법을 찾아봅시다.\n\n다른 가능성이나 관점을 생각해보세요.",
                "약 3분"
            ),
            TuningStep(
                "4단계: 🎼 균형잡힌 생각 완성",
                "새로운 관점으로 상황을 재조율해봅시다.\n\n더 현실적이고 도움되는 생각으로 바꿔보겠습니다.",
                "약 2분"
            ),
            TuningStep(
                "5단계: 🌈 새로운 조성으로 연주",
                "바뀐 생각으로 감정이 어떻게 달라졌는지 확인해봅시다.\n\n단조에서 장조로 조성이 바뀌었나요?",
                "자유롭게"
            )
        )
    }

    private fun createCBTSadnessSteps(): List<TuningStep> {
        return listOf(
            TuningStep(
                "1단계: 🎼 슬픈 생각 포착하기",
                "슬픔을 만드는 생각을 찾아봅시다.\n\n'어떤 생각이 슬프게 만드나요?'",
                "자유롭게"
            ),
            TuningStep(
                "2단계: 🧠 현실성 검토하기",
                "이 생각이 얼마나 현실적인지 살펴봅시다.",
                "약 2분"
            ),
            TuningStep(
                "3단계: 🎵 희망적 관점 찾기",
                "같은 상황에서 희망을 찾을 수 있는 관점을 생각해봅시다.",
                "약 3분"
            ),
            TuningStep(
                "4단계: 🌈 밝은 생각으로 전환",
                "더 희망적이고 건설적인 생각으로 바꿔봅시다.",
                "약 2분"
            )
        )
    }

    private fun createCBTAnxietySteps(): List<TuningStep> {
        return listOf(
            TuningStep(
                "1단계: 🎼 불안한 생각 포착하기",
                "불안을 만드는 생각을 찾아봅시다.\n\n'무엇이 걱정되나요?'",
                "자유롭게"
            ),
            TuningStep(
                "2단계: 🧠 확률적 사고하기",
                "걱정하는 일이 실제로 일어날 확률을 생각해봅시다.",
                "약 2분"
            ),
            TuningStep(
                "3단계: 🎵 대처 가능성 찾기",
                "설령 일어나더라도 대처할 수 있는 방법을 생각해봅시다.",
                "약 3분"
            ),
            TuningStep(
                "4단계: 🌈 현실적 관점 완성",
                "더 현실적이고 안정적인 생각으로 바꿔봅시다.",
                "약 2분"
            )
        )
    }

    private fun createCBTGeneralSteps(): List<TuningStep> {
        return listOf(
            TuningStep("CBT 1단계: 생각 포착하기", "어떤 생각이 이 감정을 만드나요?", "2분"),
            TuningStep("CBT 2단계: 생각 검토하기", "이 생각이 도움이 되나요? 현실적인가요?", "3분"),
            TuningStep("CBT 3단계: 새로운 관점", "더 균형잡힌 생각으로 바꿔봅시다.", "2분")
        )
    }

    // ACT 화남 조율 (자연스러운 전조)
    private fun createACTAngerSteps(): List<TuningStep> {
        return listOf(
            TuningStep(
                "1단계: 🌊 감정 파도 관찰하기",
                "화남이라는 감정을 파도처럼 관찰해봅시다.\n\n바꾸려 하지 말고, 판단하지 말고,\n그저 '아, 지금 화남이라는 파도가 왔구나' 하고 지켜봅시다.\n\n🎵 음악의 크레센도처럼 자연스러운 흐름입니다.",
                "약 2분"
            ),
            TuningStep(
                "2단계: 🎶 감정과 나 분리하기",
                "'나는 화가 나 있다'가 아니라\n'지금 화남이라는 감정을 경험하고 있다'고 말해보세요.\n\n당신은 화남 그 자체가 아닙니다.\n화남을 경험하는 관찰자입니다.\n\n🎼 연주자와 음악이 다르듯이요.",
                "약 2분"
            ),
            TuningStep(
                "3단계: 🎵 가치 기반 행동하기",
                "지금 화가 나더라도 중요한 가치에 따라 행동해봅시다:\n\n• 가족과의 관계가 중요하다면?\n• 성장이 중요하다면?\n• 평화가 중요하다면?\n\n감정 상태와 관계없이 가치에 따라 움직여보세요.",
                "약 3분"
            ),
            TuningStep(
                "4단계: 🌊 자연스러운 전조 완성",
                "화남에서 평온으로 억지로 바꾸지 않았지만,\n자연스럽게 변화가 일어났나요?\n\n🎶 강제적인 전조가 아닌 자연스러운 화성 진행처럼\n감정도 자연스럽게 흘러갑니다.",
                "자유롭게"
            )
        )
    }

    // 간단한 예시들 (나머지 감정들)
    private fun createDBTSadnessSteps(): List<TuningStep> {
        return listOf(
            TuningStep("1단계: 슬픔 볼륨 인식", "DBT 방식으로 슬픔의 강도를 확인해봅시다.", "1분"),
            TuningStep("2단계: PLEASE 기법", "기본 욕구 충족으로 감정 조절력을 높여봅시다.\n• 충분한 수면\n• 균형잡힌 식사\n• 운동하기", "3분"),
            TuningStep("3단계: 반대 행동", "슬플 때는 기분 좋아지는 활동을 해봅시다.", "2분")
        )
    }

    private fun createACTSadnessSteps(): List<TuningStep> {
        return listOf(
            TuningStep("1단계: 슬픔 수용하기", "슬픔도 인간의 자연스러운 감정입니다.", "2분"),
            TuningStep("2단계: 슬픔과 함께 걷기", "슬픔을 밀어내지 말고 함께 있어봅시다.", "3분"),
            TuningStep("3단계: 가치 중심 행동", "슬프더라도 중요한 일은 계속해봅시다.", "2분")
        )
    }

    // 나머지 메소드들도 비슷하게 구현...
    private fun createDBTAnxietySteps(): List<TuningStep> = createDBTGeneralSteps()
    private fun createACTAnxietySteps(): List<TuningStep> = createACTGeneralSteps()

    private fun createDBTGeneralSteps(): List<TuningStep> {
        return listOf(
            TuningStep("DBT 1단계: 현재 감정 강도 확인", "지금 감정의 볼륨을 체크해봅시다.", "1분"),
            TuningStep("DBT 2단계: 강도 조절 기법", "TIP 기법으로 감정 볼륨을 낮춰봅시다.", "3분"),
            TuningStep("DBT 3단계: 목표 강도 달성", "원하는 볼륨에 도달했는지 확인해봅시다.", "1분")
        )
    }

    private fun createACTGeneralSteps(): List<TuningStep> {
        return listOf(
            TuningStep("ACT 1단계: 감정 관찰하기", "감정을 바꾸려 하지 말고 관찰해봅시다.", "2분"),
            TuningStep("ACT 2단계: 수용하기", "이 감정도 자연스러운 인간 경험입니다.", "2분"),
            TuningStep("ACT 3단계: 가치 기반 행동", "감정과 관계없이 중요한 가치를 위해 행동해봅시다.", "3분")
        )
    }

    // 기존 메소드들을 래핑
    private fun createDefaultAngerToCalm(): List<TuningStep> {
        return listOf(
            TuningStep("1단계: 현재 감정 인식", "지금 화가 났다는 것을 인정해보세요.", "자유롭게"),
            TuningStep("2단계: 4-7-8 호흡법", "4초 들이마시고, 7초 참고, 8초 내쉬기를 3회 반복하세요.", "약 2분"),
            TuningStep("3단계: 신체 이완", "어깨와 목의 긴장을 풀어주세요.", "약 1분"),
            TuningStep("4단계: 생각 정리", "화의 원인을 객관적으로 생각해보세요.", "자유롭게"),
            TuningStep("5단계: 평온 상상", "마음이 평온한 상태를 상상해보세요.", "약 1분")
        )
    }

    private fun createDefaultSadnessToJoy(): List<TuningStep> = createDefaultGeneralSteps()
    private fun createDefaultAnxietyToCalm(): List<TuningStep> = createDefaultGeneralSteps()
    private fun createDefaultIntensityReduction(): List<TuningStep> = createDefaultGeneralSteps()

    private fun createDefaultGeneralSteps(): List<TuningStep> {
        return listOf(
            TuningStep("1단계: 현재 감정 확인", "지금 감정 상태를 확인해보세요.", "자유롭게"),
            TuningStep("2단계: 목표 상태 상상", "원하는 감정 상태를 상상해보세요.", "약 1분"),
            TuningStep("3단계: 점진적 전환", "천천히 목표 감정으로 이끌어보세요.", "자유롭게")
        )
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

    // showCurrentStep() 메소드 수정 - CBT 대화상자 추가
    private fun showCurrentStep() {
        if (currentStepIndex < tuningSteps.size) {
            val step = tuningSteps[currentStepIndex]

            tvStepTitle.text = step.title
            tvStepInstruction.text = "${step.instruction}\n\n⏱️ 예상 소요시간: ${step.duration}"

            progressBarTuning.progress = currentStepIndex + 1
            tvTuningStatus.text = "${currentStepIndex + 1} / ${tuningSteps.size} 단계 진행 중"

            // CBT 특별 처리 - 단계별 대화상자
            if (selectedTherapyMethod == "CBT") {
                when (currentStepIndex) {
                    0 -> showCBTThoughtCaptureDialog()     // 1단계: 생각 포착
                    1 -> showCBTThoughtValidationDialog()  // 2단계: 생각 검증
                    2 -> showCBTAlternativeDialog()        // 3단계: 대안 찾기
                    3 -> showCBTBalancedThoughtDialog()    // 4단계: 균형잡힌 생각
                    4 -> showCBTCompletionDialog()         // 5단계: 완성 확인
                }
            }

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

    // CBT 1단계: 부정적 생각 포착 대화상자
    private fun showCBTThoughtCaptureDialog() {
        val input = EditText(this).apply {
            hint = when(currentEmotionSymbol) {
                "♯" -> "예: 이건 말이 안 돼, 완전히 불공평해"
                "♭" -> "예: 나는 실패작이야, 아무것도 잘 안 돼"
                "𝄢" -> "예: 큰일 날 것 같아, 통제할 수 없어"
                else -> "지금 어떤 생각이 드나요?"
            }
            setPadding(40, 30, 40, 30)
            maxLines = 5
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@EmotionTunerActivity, android.R.color.black))
            setHintTextColor(ContextCompat.getColor(this@EmotionTunerActivity, android.R.color.darker_gray))
            background = ContextCompat.getDrawable(this@EmotionTunerActivity, android.R.drawable.edit_text)
        }

        val questionText = when(currentEmotionSymbol) {
            "♯" -> "🔥 화가 나게 하는 생각은 무엇인가요?"
            "♭" -> "😢 슬프게 만드는 생각은 무엇인가요?"
            "𝄢" -> "😰 불안하게 만드는 생각은 무엇인가요?"
            else -> "🤔 어떤 생각이 이 감정을 만들고 있나요?"
        }

        val messageText = "$questionText\n\n자유롭게 떠오르는 생각을 적어보세요.\n\n📝 어떤 생각이든 괜찮습니다. 솔직하게 써보세요."

        val builder = android.app.AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
        builder.setTitle("🎼 CBT 1단계: 생각 포착")
        builder.setMessage(messageText)
        builder.setView(input)
        builder.setPositiveButton("다음 단계로") { _, _ ->
            userNegativeThought = input.text.toString().trim()
            if (userNegativeThought.isEmpty()) {
                userNegativeThought = "특별한 생각이 떠오르지 않음"
            }
            isCBTInteractive = true
            Toast.makeText(this, "💭 생각이 포착되었습니다!\n\"$userNegativeThought\"", Toast.LENGTH_LONG).show()
        }
        builder.setNegativeButton("건너뛰기") { _, _ ->
            userNegativeThought = "건너뜀"
            Toast.makeText(this, "다음 단계로 넘어갑니다", Toast.LENGTH_SHORT).show()
        }
        builder.setCancelable(false)

        val dialog = builder.create()
        dialog.show()

        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark))
        dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)?.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
    }

    // CBT 2단계: 생각 검증 대화상자
    private fun showCBTThoughtValidationDialog() {
        val questions = when(currentEmotionSymbol) {
            "♯" -> arrayOf(
                "이 생각이 100% 사실인가요?",
                "상대방이 정말 일부러 그런 걸까요?",
                "내가 놓친 부분은 없을까요?",
                "가장 친한 친구라면 뭐라고 할까요?"
            )
            "♭" -> arrayOf(
                "이 생각이 100% 사실인가요?",
                "내가 정말 모든 면에서 실패한 걸까요?",
                "좋았던 순간들은 없었나요?",
                "사랑하는 사람이라면 뭐라고 할까요?"
            )
            "𝄢" -> arrayOf(
                "이 생각이 100% 사실인가요?",
                "정말 그런 일이 일어날 확률이 높을까요?",
                "설령 일어나더라도 해결할 방법이 없을까요?",
                "과거에 비슷한 걱정이 현실이 된 적이 있나요?"
            )
            else -> arrayOf(
                "이 생각이 100% 사실인가요?",
                "다른 관점에서 볼 여지는 없을까요?",
                "이 생각이 도움이 되나요?",
                "더 현실적인 생각은 무엇일까요?"
            )
        }

        val message = buildString {
            append("💭 포착된 생각:\n")
            append("\"$userNegativeThought\"\n\n")
            append("🔍 이 생각을 함께 검토해봅시다:\n\n")
            questions.forEachIndexed { index, question ->
                append("${index + 1}. $question\n\n")
            }
            append("⏰ 천천히 각 질문에 대해 생각해보세요...")
        }

        val builder = android.app.AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
        builder.setTitle("🧠 CBT 2단계: 생각 검증")
        builder.setMessage(message)
        builder.setPositiveButton("검토 완료했어요") { _, _ ->
            Toast.makeText(this, "🔍 생각 검증이 완료되었습니다!\n다음 단계로 넘어갑니다.", Toast.LENGTH_SHORT).show()
        }
        builder.setCancelable(false)

        val dialog = builder.create()
        dialog.show()

        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark))
    }

    // CBT 3단계: 대안적 관점 대화상자
    private fun showCBTAlternativeDialog() {
        val input = EditText(this).apply {
            hint = when(currentEmotionSymbol) {
                "♯" -> "예: 실수일 수도 있고, 나름의 이유가 있을 수도 있어"
                "♭" -> "예: 힘든 시기이지만 좋은 면도 있고, 성장의 기회야"
                "𝄢" -> "예: 확률은 낮고, 일어나더라도 대처할 수 있어"
                else -> "다른 관점에서는 어떻게 보일까요?"
            }
            setPadding(40, 30, 40, 30)
            maxLines = 5
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@EmotionTunerActivity, android.R.color.black))
            setHintTextColor(ContextCompat.getColor(this@EmotionTunerActivity, android.R.color.darker_gray))
            background = ContextCompat.getDrawable(this@EmotionTunerActivity, android.R.drawable.edit_text)
        }

        val suggestionText = when(currentEmotionSymbol) {
            "♯" -> "💡 이런 관점은 어떨까요?\n• 상대방 입장에서 생각해보기\n• 선의로 해석할 여지 찾기\n• 내 기분 상태 고려하기"
            "♭" -> "💡 이런 관점은 어떨까요?\n• 성공한 경험들 떠올리기\n• 배움의 기회로 보기\n• 일시적 상황임을 인식하기"
            "𝄢" -> "💡 이런 관점은 어떨까요?\n• 실제 확률 계산해보기\n• 대처 방법 생각해보기\n• 과거 극복 경험 떠올리기"
            else -> "💡 이런 관점은 어떨까요?\n• 다른 사람의 관점에서 보기\n• 긍정적 측면 찾아보기\n• 학습 기회로 여기기"
        }

        val messageText = "💭 원래 생각:\n\"$userNegativeThought\"\n\n🌈 같은 상황을 다른 관점에서 바라본다면?\n\n$suggestionText\n\n📝 새로운 관점을 써보세요:"

        val builder = android.app.AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
        builder.setTitle("🎵 CBT 3단계: 대안 관점")
        builder.setMessage(messageText)
        builder.setView(input)
        builder.setPositiveButton("다음 단계로") { _, _ ->
            userAlternativeThought = input.text.toString().trim()
            if (userAlternativeThought.isEmpty()) {
                userAlternativeThought = "다른 관점이 있을 수 있음"
            }
            Toast.makeText(this, "🌈 대안적 관점을 찾았습니다!\n\"$userAlternativeThought\"", Toast.LENGTH_LONG).show()
        }
        builder.setNegativeButton("건너뛰기") { _, _ ->
            userAlternativeThought = "건너뜀"
            Toast.makeText(this, "다음 단계로 넘어갑니다", Toast.LENGTH_SHORT).show()
        }
        builder.setCancelable(false)

        val dialog = builder.create()
        dialog.show()

        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark))
        dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)?.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
    }

    // CBT 4단계: 균형잡힌 생각 완성 대화상자
    private fun showCBTBalancedThoughtDialog() {
        val input = EditText(this).apply {
            hint = when(currentEmotionSymbol) {
                "♯" -> "예: 화는 나지만 이해할 여지도 있고, 건설적으로 해결해보자"
                "♭" -> "예: 지금은 힘들지만 이것도 지나갈 것이고, 배울 점이 있어"
                "𝄢" -> "예: 걱정되긴 하지만 확률은 낮고, 충분히 대처할 수 있어"
                else -> "더 균형잡히고 현실적인 생각을 써보세요"
            }
            setPadding(40, 30, 40, 30)
            maxLines = 6
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@EmotionTunerActivity, android.R.color.black))
            setHintTextColor(ContextCompat.getColor(this@EmotionTunerActivity, android.R.color.darker_gray))
            background = ContextCompat.getDrawable(this@EmotionTunerActivity, android.R.drawable.edit_text)
        }

        val message = buildString {
            append("🎼 지금까지의 과정:\n\n")
            append("1️⃣ 원래 생각:\n\"$userNegativeThought\"\n\n")
            append("2️⃣ 대안 관점:\n\"$userAlternativeThought\"\n\n")
            append("3️⃣ 이제 둘을 종합해서 더 균형잡힌 생각을 만들어봅시다!\n\n")
            append("💡 균형잡힌 생각의 특징:\n")
            append("• 현실적이면서도 희망적\n")
            append("• 극단적이지 않고 중간적\n")
            append("• 도움이 되고 건설적\n\n")
            append("📝 최종 균형잡힌 생각을 써보세요:")
        }

        val builder = android.app.AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
        builder.setTitle("🎼 CBT 4단계: 균형잡힌 생각")
        builder.setMessage(message)
        builder.setView(input)
        builder.setPositiveButton("완성했어요!") { _, _ ->
            userBalancedThought = input.text.toString().trim()
            if (userBalancedThought.isEmpty()) {
                userBalancedThought = "더 균형잡힌 관점으로 보기"
            }
            Toast.makeText(this, "✨ 균형잡힌 생각이 완성되었습니다!\n\"$userBalancedThought\"", Toast.LENGTH_LONG).show()
        }
        builder.setNegativeButton("건너뛰기") { _, _ ->
            userBalancedThought = "건너뜀"
            Toast.makeText(this, "다음 단계로 넘어갑니다", Toast.LENGTH_SHORT).show()
        }
        builder.setCancelable(false)

        val dialog = builder.create()
        dialog.show()

        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark))
        dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)?.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
    }

    // CBT 5단계: 완성 확인 대화상자
    private fun showCBTCompletionDialog() {
        val emotionChange = when(currentEmotionSymbol) {
            "♯" -> "화남이 줄어들고 이해심이 생겼나요?"
            "♭" -> "슬픔이 덜해지고 희망이 보이나요?"
            "𝄢" -> "불안이 줄어들고 안정감이 느껴지나요?"
            else -> "감정에 변화가 있나요?"
        }

        val message = buildString {
            append("🎊 CBT 인지 재구조화 완성!\n\n")
            append("🎼 생각의 조성 변화:\n\n")
            append("🔴 처음 생각 (단조):\n\"$userNegativeThought\"\n\n")
            append("🟢 새로운 생각 (장조):\n\"$userBalancedThought\"\n\n")
            append("💫 감정 체크: $emotionChange\n\n")
            append("🎵 단조에서 장조로 조성이 바뀌듯,\n생각이 바뀌면 감정도 따라 바뀝니다!\n\n")
            append("✨ 새로운 관점으로 하루를 연주해보세요!")
        }

        val builder = android.app.AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
        builder.setTitle("🌈 CBT 조성 바꾸기 완료!")
        builder.setMessage(message)
        builder.setPositiveButton("정말 좋아졌어요! 😊") { _, _ ->
            Toast.makeText(this, "🎊 CBT 조성 바꾸기 대성공!\n새로운 관점으로 세상을 바라보세요!", Toast.LENGTH_LONG).show()
        }
        builder.setNeutralButton("조금 나아졌어요 🙂") { _, _ ->
            Toast.makeText(this, "🎵 조금씩 변화하는 것도 큰 발전이에요!\n계속 연습해보세요.", Toast.LENGTH_LONG).show()
        }
        builder.setNegativeButton("아직 잘 모르겠어요 😐") { _, _ ->
            Toast.makeText(this, "🤗 괜찮습니다! 변화는 천천히 나타날 수 있어요.\n시간을 두고 지켜봐주세요.", Toast.LENGTH_LONG).show()
        }
        builder.setCancelable(false)

        val dialog = builder.create()
        dialog.show()

        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
        dialog.getButton(android.app.AlertDialog.BUTTON_NEUTRAL)?.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark))
        dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)?.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
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

        // 성공 메시지 - 선택된 치료법에 따라 다르게
        val completionMessage = when(selectedTherapyMethod) {
            "DBT" -> "🎚️ 볼륨 조절법 완료!\n${currentEmotionName}(${intensityLevels[currentIntensity-1]})에서 ${targetEmotionName}(${intensityLevels[seekBarTarget.progress]})으로 조율 완료!"
            "CBT" -> "🎼 조성 바꾸기 완료!\n새로운 관점으로 상황을 바라볼 수 있게 되었어요!"
            "ACT" -> "🌊 자연스러운 전조 완료!\n감정과 평화롭게 공존하는 법을 연습했어요!"
            else -> "🎵 기본 조율 완료!\n${currentEmotionName}에서 ${targetEmotionName}으로 조율 완료!"
        }

        Toast.makeText(this, completionMessage, Toast.LENGTH_LONG).show()
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
        // 치료법별로 다른 가이드 생성
        val guide = when(selectedTherapyMethod) {
            "DBT" -> when {
                currentEmotionSymbol == "♯" && targetEmotionSymbol == "♩" -> {
                    "🎚️ DBT 볼륨 조절법 - 화남→평온\n\n감정의 강도를 물리적으로 낮춰서 평온함에 도달합니다.\nTIP 기법과 반대 행동을 통해 볼륨을 조절해요.\n\n예상 소요시간: 5-8분"
                }
                else -> {
                    "🎚️ DBT 볼륨 조절법\n\n감정의 강도를 효과적으로 조절하여 원하는 수준에 도달합니다.\n강한 감정을 다루는 데 특히 효과적이에요.\n\n예상 소요시간: 3-6분"
                }
            }
            "CBT" -> when {
                currentEmotionSymbol == "♯" && targetEmotionSymbol == "♩" -> {
                    "🎼 CBT 조성 바꾸기 - 화남→평온\n\n화가 나게 하는 생각을 다른 관점으로 바꿔서 마음을 평온하게 만듭니다.\n단조에서 장조로 조성을 바꾸듯이요.\n\n예상 소요시간: 6-10분"
                }
                else -> {
                    "🎼 CBT 조성 바꾸기\n\n상황을 바라보는 관점을 바꿔서 감정의 색깔을 변화시킵니다.\n생각이 감정을 만든다는 원리를 활용해요.\n\n예상 소요시간: 4-8분"
                }
            }
            "ACT" -> when {
                currentEmotionSymbol == "♯" && targetEmotionSymbol == "♩" -> {
                    "🌊 ACT 자연스러운 전조 - 화남→평온\n\n화남을 억지로 바꾸려 하지 않고 자연스럽게 흘러가도록 도와드립니다.\n감정 파도를 관찰하며 가치 기반 행동을 연습해요.\n\n예상 소요시간: 6-12분"
                }
                else -> {
                    "🌊 ACT 자연스러운 전조\n\n감정을 바꾸려 하지 않고 수용하면서 자연스러운 변화를 경험합니다.\n감정과 평화롭게 공존하는 법을 배워요.\n\n예상 소요시간: 5-10분"
                }
            }
            else -> {
                "🎶 기본 조율법 - ${currentEmotionName}→${targetEmotionName}\n\n단계적으로 감정 상태를 조율해보겠습니다.\n누구나 쉽게 따라할 수 있는 방법이에요.\n\n예상 소요시간: 3-5분"
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