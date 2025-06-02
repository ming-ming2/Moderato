package com.example.moderato

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlin.math.min

class EmotionTunerActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var tvCurrentEmotion: TextView
    private lateinit var tvCurrentEmotionIcon: TextView
    private lateinit var tvTargetEmotion: TextView
    private lateinit var tvTargetEmotionIcon: TextView

    private lateinit var seekBarTarget: SeekBar
    private lateinit var progressBarTuning: ProgressBar

    private lateinit var tvCurrentIntensity: TextView
    private lateinit var tvTargetIntensity: TextView
    private lateinit var tvTuningStatus: TextView

    private lateinit var btnStartTuning: Button
    private lateinit var tvActivityGuide: TextView
    private lateinit var linearTuningProgress: LinearLayout

    // 원래 치료법 선택 관련 위젯들
    private lateinit var rgTherapyMethod: RadioGroup
    private lateinit var tvTherapyDescription: TextView
    private lateinit var therapySelectionContainer: LinearLayout

    // 동적으로 생성되는 새로운 방법 선택 위젯
    private var dynamicMethodGroup: RadioGroup? = null

    // 조율 단계 관련
    private lateinit var currentStepContainer: LinearLayout
    private lateinit var tvStepTitle: TextView
    private lateinit var tvStepInstruction: TextView
    private lateinit var btnStepNext: Button
    private lateinit var btnStepComplete: Button

    // 감정 데이터
    private var currentEmotionSymbol = "♪"
    private var currentEmotionName = "기쁨"
    private var currentIntensity = 3
    private var targetEmotionSymbol = "♩"
    private var targetEmotionName = "평온"

    // 선택된 방법 저장
    private var selectedMethod = "DEFAULT"
    private var emotionNeedType = EmotionNeedType.NEEDS_CALMING

    private val intensityLevels = arrayOf("pp", "p", "mf", "f", "ff")
    private val intensityTexts = arrayOf("매우 여리게", "여리게", "보통으로", "세게", "매우 세게")

    // 조율 단계 관련
    private var currentStepIndex = 0
    private var tuningSteps: List<TuningStep> = listOf()
    private var isActiveTuning = false

    // CBT 관련 변수들
    private var userNegativeThought = ""
    private var userAlternativeThought = ""
    private var userBalancedThought = ""

    // 수업 3주차 - 데이터 클래스와 enum 활용
    enum class EmotionNeedType {
        NEEDS_CALMING,     // 진정 필요 (화남)
        NEEDS_UPLIFTING,   // 기분 전환 필요 (슬픔)
        NEEDS_STABILIZING, // 안정화 필요 (불안)
        NEEDS_MODERATION,  // 절제 필요 (과한 기쁨)
        NEEDS_GROUNDING,   // 차분함 필요 (과한 설렘)
        CAN_ENHANCE,       // 강화 가능 (평온)
        CAN_DEEPEN,        // 심화 가능 (사랑)
        CAN_AMPLIFY,       // 확대 가능 (기쁨)
        ALREADY_BALANCED   // 이미 균형잡힌 상태
    }

    data class TuningStep(
        val title: String,
        val instruction: String,
        val duration: String,
        val isInteractive: Boolean = true
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emotion_tuner)

        // 인텐트에서 현재 감정 정보 받기
        currentEmotionSymbol = intent.getStringExtra("CURRENT_EMOTION_SYMBOL") ?: "♪"
        currentEmotionName = intent.getStringExtra("CURRENT_EMOTION_NAME") ?: "기쁨"

        initViews()
        loadCurrentEmotionData()

        // 핵심: 감정 유형 분류
        emotionNeedType = classifyEmotionNeed()

        // 감정 유형에 따라 UI 적응
        adaptUIForEmotionType()

        setupClickListeners()
        setupSeekBars()
        setupTuningSteps()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvCurrentEmotion = findViewById(R.id.tvCurrentEmotion)
        tvCurrentEmotionIcon = findViewById(R.id.tvCurrentEmotionIcon)
        tvTargetEmotion = findViewById(R.id.tvTargetEmotion)
        tvTargetEmotionIcon = findViewById(R.id.tvTargetEmotionIcon)

        seekBarTarget = findViewById(R.id.seekBarTarget)
        progressBarTuning = findViewById(R.id.progressBarTuning)

        tvCurrentIntensity = findViewById(R.id.tvCurrentIntensity)
        tvTargetIntensity = findViewById(R.id.tvTargetIntensity)
        tvTuningStatus = findViewById(R.id.tvTuningStatus)

        btnStartTuning = findViewById(R.id.btnStartTuning)
        tvActivityGuide = findViewById(R.id.tvActivityGuide)
        linearTuningProgress = findViewById(R.id.linearTuningProgress)

        // 치료법/방법 선택 관련
        rgTherapyMethod = findViewById(R.id.rgTherapyMethod)
        tvTherapyDescription = findViewById(R.id.tvTherapyDescription)

        // 치료법 선택 컨테이너 찾기 (부모 레이아웃)
        therapySelectionContainer = rgTherapyMethod.parent as LinearLayout

        createStepByStepUI()
    }

    // 수업 3주차 - when문을 활용한 감정 분류
    private fun classifyEmotionNeed(): EmotionNeedType {
        return when {
            // 부정적 감정 - 조율 필요
            currentEmotionSymbol == "♯" -> EmotionNeedType.NEEDS_CALMING
            currentEmotionSymbol == "♭" -> EmotionNeedType.NEEDS_UPLIFTING
            currentEmotionSymbol == "𝄢" -> EmotionNeedType.NEEDS_STABILIZING

            // 긍정적이지만 과도한 경우 - 조절 필요
            currentEmotionSymbol == "♪" && currentIntensity >= 4 -> EmotionNeedType.NEEDS_MODERATION
            currentEmotionSymbol == "♫" && currentIntensity >= 4 -> EmotionNeedType.NEEDS_GROUNDING

            // 좋은 감정이지만 더 발전 가능 - 강화 추천
            currentEmotionSymbol == "♩" && currentIntensity <= 3 -> EmotionNeedType.CAN_ENHANCE
            currentEmotionSymbol == "♡" && currentIntensity <= 3 -> EmotionNeedType.CAN_DEEPEN
            currentEmotionSymbol == "♪" && currentIntensity <= 3 -> EmotionNeedType.CAN_AMPLIFY

            // 이미 완벽한 상태 - 유지만 필요
            else -> EmotionNeedType.ALREADY_BALANCED
        }
    }

    // 수업 3주차 - when문과 메소드 활용으로 UI 적응
    private fun adaptUIForEmotionType() {
        when (emotionNeedType) {
            EmotionNeedType.NEEDS_CALMING,
            EmotionNeedType.NEEDS_UPLIFTING,
            EmotionNeedType.NEEDS_STABILIZING,
            EmotionNeedType.NEEDS_MODERATION,
            EmotionNeedType.NEEDS_GROUNDING -> {
                showTuningInterface()
                recommendTargetEmotion()
            }

            EmotionNeedType.CAN_ENHANCE,
            EmotionNeedType.CAN_DEEPEN,
            EmotionNeedType.CAN_AMPLIFY -> {
                showEnhancementInterface()
                setEnhancementTarget()
            }

            EmotionNeedType.ALREADY_BALANCED -> {
                showMaintenanceInterface()
                setMaintenanceTarget()
            }
        }

        generateActivityGuide()
    }

    // 조율이 필요한 경우의 UI
    private fun showTuningInterface() {
        rgTherapyMethod.visibility = View.VISIBLE
        btnStartTuning.text = "🎚️ 감정 조율 시작"

        tvTherapyDescription.text = when(emotionNeedType) {
            EmotionNeedType.NEEDS_CALMING -> "💪 화난 감정을 평온하게 조율해보겠습니다."
            EmotionNeedType.NEEDS_UPLIFTING -> "🌈 슬픈 마음을 밝게 전환해보겠습니다."
            EmotionNeedType.NEEDS_STABILIZING -> "🧘 불안한 마음을 안정시켜보겠습니다."
            EmotionNeedType.NEEDS_MODERATION -> "⚖️ 과도한 감정을 적절히 조절해보겠습니다."
            EmotionNeedType.NEEDS_GROUNDING -> "🌱 들뜬 마음을 차분하게 안정시켜보겠습니다."
            else -> "현재 감정을 더 균형잡힌 상태로 조율해보겠습니다."
        }

        // 목표 감정 선택 가능
        tvTargetEmotionIcon.isClickable = true
        tvTargetEmotionIcon.background = ContextCompat.getDrawable(this, R.drawable.chord_button_bg)
    }

    // 강화가 가능한 경우의 UI
    private fun showEnhancementInterface() {
        hideOriginalTherapySelection()
        showEnhancementMethods()

        btnStartTuning.text = "✨ 감정 강화하기"

        tvTherapyDescription.text = when(emotionNeedType) {
            EmotionNeedType.CAN_ENHANCE -> "🧘 평온한 마음을 더욱 깊고 풍부하게 발전시켜보겠습니다."
            EmotionNeedType.CAN_DEEPEN -> "💝 사랑의 감정을 더욱 깊이 있게 확장해보겠습니다."
            EmotionNeedType.CAN_AMPLIFY -> "🌟 기쁜 마음을 더욱 밝고 아름답게 키워보겠습니다."
            else -> "좋은 감정을 더욱 풍부하고 깊게 발전시켜보겠습니다."
        }

        // 목표는 고정 (현재 감정의 강화된 버전)
        tvTargetEmotionIcon.isClickable = false
        tvTargetEmotionIcon.background = null
    }

    // 이미 균형잡힌 경우의 UI
    private fun showMaintenanceInterface() {
        hideOriginalTherapySelection()
        showMaintenanceMethods()

        btnStartTuning.text = "💫 현재 감정 음미하기"

        tvTherapyDescription.text = "🎵 현재의 완벽한 감정 상태를 깊이 느끼고 간직하는 시간을 가져보겠습니다."

        // 목표를 현재 감정과 동일하게 설정
        tvTargetEmotionIcon.isClickable = false
        tvTargetEmotionIcon.background = null
    }

    // 수업 4주차 - 위젯 동적 생성
    private fun showEnhancementMethods() {
        val enhancementMethods = when(currentEmotionSymbol) {
            "♩" -> arrayOf(
                "🧘 깊은 명상으로 평온 심화하기",
                "🌸 감사 연습으로 평온 확장하기",
                "🎵 음악으로 평온 강화하기",
                "🍃 자연과 함께 평온 깊이 느끼기"
            )
            "♡" -> arrayOf(
                "💝 사랑 표현으로 감정 심화하기",
                "🤗 친밀감 강화 활동하기",
                "💌 감사 편지 쓰기",
                "🫂 따뜻한 기억 되새기기"
            )
            "♪" -> arrayOf(
                "🎉 기쁨 공유하며 확산시키기",
                "💃 몸짓으로 기쁨 표현하기",
                "🎨 창작으로 기쁨 확장하기",
                "🌟 성취감 깊이 음미하기"
            )
            else -> arrayOf("✨ 현재 감정 강화하기")
        }

        createDynamicMethodSelection(enhancementMethods)
    }

    private fun showMaintenanceMethods() {
        val maintenanceMethods = arrayOf(
            "🍃 현재 감정 깊이 느끼고 간직하기",
            "📿 마음챙김으로 현재 순간 머물기",
            "📝 감정 일기 쓰며 소중히 기록하기",
            "🎶 현재 기분에 맞는 음악 듣기",
            "🌅 고요한 시간 갖기"
        )

        createDynamicMethodSelection(maintenanceMethods)
    }

    // 수업 5주차 - Kotlin 코드로 화면 만들기
    private fun createDynamicMethodSelection(methods: Array<String>) {
        // 기존 동적 그룹이 있다면 제거
        dynamicMethodGroup?.let { therapySelectionContainer.removeView(it) }

        // 새로운 라디오그룹 동적 생성
        dynamicMethodGroup = RadioGroup(this).apply {
            orientation = RadioGroup.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 32
            }
        }

        methods.forEachIndexed { index, method ->
            val radioButton = RadioButton(this).apply {
                id = View.generateViewId()
                text = method
                textSize = 14f
                setTextColor(ContextCompat.getColor(this@EmotionTunerActivity, R.color.text_primary))
                background = ContextCompat.getDrawable(this@EmotionTunerActivity, R.drawable.radio_button_bg)
                layoutParams = RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.MATCH_PARENT, 120
                ).apply {
                    bottomMargin = 16
                }
                setPadding(32, 0, 32, 0)

                if (index == 0) isChecked = true // 첫 번째 선택
            }
            dynamicMethodGroup!!.addView(radioButton)
        }

        // 설명 텍스트 뒤에 추가
        val descriptionIndex = therapySelectionContainer.indexOfChild(tvTherapyDescription)
        therapySelectionContainer.addView(dynamicMethodGroup!!, descriptionIndex + 1)

        // 리스너 설정
        dynamicMethodGroup!!.setOnCheckedChangeListener { _, checkedId ->
            val selectedButton = findViewById<RadioButton>(checkedId)
            selectedMethod = selectedButton.text.toString()
            generateActivityGuide()
            setupTuningSteps()

            Toast.makeText(this, "✨ ${selectedButton.text}가 선택되었습니다", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hideOriginalTherapySelection() {
        rgTherapyMethod.visibility = View.GONE
    }

    private fun setEnhancementTarget() {
        when(currentEmotionSymbol) {
            "♩" -> {
                targetEmotionSymbol = "♪"
                targetEmotionName = "깊은 기쁨"
                seekBarTarget.progress = min(currentIntensity, 3) // 적당한 강도로
            }
            "♡" -> {
                targetEmotionSymbol = "♡"
                targetEmotionName = "깊은 사랑"
                seekBarTarget.progress = min(currentIntensity + 1, 4) // 한 단계 강화
            }
            "♪" -> {
                targetEmotionSymbol = "♪"
                targetEmotionName = "더 밝은 기쁨"
                seekBarTarget.progress = min(currentIntensity + 1, 4)
            }
        }
        updateTargetDisplay()
    }

    private fun setMaintenanceTarget() {
        // 목표를 현재 감정과 동일하게 설정
        targetEmotionSymbol = currentEmotionSymbol
        targetEmotionName = "${currentEmotionName} (음미하기)"
        seekBarTarget.progress = currentIntensity - 1 // 현재 강도 유지
        seekBarTarget.isEnabled = false // 강도 변경 불가

        updateTargetDisplay()
    }

    private fun updateTargetDisplay() {
        tvTargetEmotion.text = targetEmotionName
        tvTargetEmotionIcon.text = targetEmotionSymbol
        tvTargetEmotionIcon.setTextColor(getEmotionColor(targetEmotionSymbol))
        updateTargetIntensityDisplay(seekBarTarget.progress)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            if (isActiveTuning) {
                showExitConfirmDialog()
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

        // 목표 감정 아이콘 클릭 (조율 모드에서만)
        tvTargetEmotionIcon.setOnClickListener {
            if (tvTargetEmotionIcon.isClickable) {
                showTargetEmotionSelectionDialog()
            }
        }

        // 원래 치료법 선택 리스너
        rgTherapyMethod.setOnCheckedChangeListener { _, checkedId ->
            selectedMethod = when(checkedId) {
                R.id.rbDBT -> "DBT"
                R.id.rbCBT -> "CBT"
                R.id.rbACT -> "ACT"
                R.id.rbDefault -> "DEFAULT"
                else -> "DEFAULT"
            }

            updateTherapyDescription()
            setupTuningSteps()
            generateActivityGuide()
        }
    }

    private fun updateTherapyDescription() {
        val descriptions = mapOf(
            "DBT" to "💪 DBT 볼륨 조절법: 감정의 강도를 조절하여 압도되지 않도록 도와드립니다.",
            "CBT" to "🧠 CBT 조성 바꾸기: 상황을 바라보는 관점을 바꿔서 감정의 색깔을 바꿔봅니다.",
            "ACT" to "🌊 ACT 자연스러운 전조: 감정을 억지로 바꾸려 하지 않고 자연스럽게 흘러가도록 도와드립니다.",
            "DEFAULT" to "💡 기본 조율법: 누구나 쉽게 따라할 수 있는 단계별 감정 조율 방법입니다."
        )

        tvTherapyDescription.text = descriptions[selectedMethod]
    }

    // 나머지 메소드들은 기존과 유사하지만 감정 유형에 따라 적절히 수정
    private fun loadCurrentEmotionData() {
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
        val recommendation = when(emotionNeedType) {
            EmotionNeedType.NEEDS_CALMING -> Triple("♩", "평온", 2)
            EmotionNeedType.NEEDS_UPLIFTING -> Triple("♪", "기쁨", 3)
            EmotionNeedType.NEEDS_STABILIZING -> Triple("♩", "평온", 3)
            EmotionNeedType.NEEDS_MODERATION -> Triple("♩", "평온", 3)
            EmotionNeedType.NEEDS_GROUNDING -> Triple("♩", "평온", 2)
            else -> Triple("♩", "평온", 3)
        }

        targetEmotionSymbol = recommendation.first
        targetEmotionName = recommendation.second
        seekBarTarget.progress = recommendation.third - 1

        updateTargetDisplay()
    }

    private fun generateActivityGuide() {
        val guide = when(emotionNeedType) {
            EmotionNeedType.NEEDS_CALMING -> "🔥➜🌊 화남을 평온으로 조절하기\n\n단계별로 차근차근 진행하면 화를 평온하게 가라앉힐 수 있어요.\n\n예상 소요시간: 5-10분"
            EmotionNeedType.NEEDS_UPLIFTING -> "😢➜😊 슬픔을 기쁨으로 전환하기\n\n마음의 색깔을 조금씩 밝게 바꿔보겠습니다.\n\n예상 소요시간: 6-12분"
            EmotionNeedType.NEEDS_STABILIZING -> "😰➜😌 불안을 안정으로 전환하기\n\n마음을 차분하게 안정시켜보겠습니다.\n\n예상 소요시간: 5-8분"
            EmotionNeedType.NEEDS_MODERATION -> "😆➜😊 과한 감정을 적절히 조절하기\n\n좋은 감정을 더 지속 가능하게 만들어보겠습니다.\n\n예상 소요시간: 3-5분"
            EmotionNeedType.NEEDS_GROUNDING -> "🤩➜😌 들뜬 마음을 차분히 안정시키기\n\n설레는 마음을 안정적으로 다스려보겠습니다.\n\n예상 소요시간: 4-6분"
            EmotionNeedType.CAN_ENHANCE -> "✨ ${currentEmotionName} ➜ 더 깊은 ${currentEmotionName}\n\n현재의 좋은 감정을 더욱 풍부하고 깊게 발전시켜보겠습니다.\n\n예상 소요시간: 5-10분"
            EmotionNeedType.CAN_DEEPEN -> "💝 ${currentEmotionName} ➜ 더 깊은 ${currentEmotionName}\n\n사랑의 감정을 더욱 깊이 있게 확장해보겠습니다.\n\n예상 소요시간: 5-10분"
            EmotionNeedType.CAN_AMPLIFY -> "🌟 ${currentEmotionName} ➜ 더 밝은 ${currentEmotionName}\n\n기쁜 마음을 더욱 아름답게 키워보겠습니다.\n\n예상 소요시간: 4-8분"
            EmotionNeedType.ALREADY_BALANCED -> "💫 현재의 완벽한 ${currentEmotionName} 음미하기\n\n지금 이 순간의 소중한 감정을 깊이 느끼고 간직해보겠습니다.\n\n예상 소요시간: 3-7분"
        }

        tvActivityGuide.text = guide
    }

    // 나머지 메소드들 (기존과 동일하지만 일부 수정)
    private fun setupSeekBars() {
        seekBarTarget.max = 4
        seekBarTarget.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateTargetIntensityDisplay(progress)
                generateActivityGuide()
                setupTuningSteps()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        progressBarTuning.max = 0
        progressBarTuning.progress = 0
    }

    private fun updateTargetIntensityDisplay(progress: Int) {
        tvTargetIntensity.text = "${intensityLevels[progress]} (${intensityTexts[progress]})"
    }

    // 조율 단계 설정 (간단화)
    private fun setupTuningSteps() {
        tuningSteps = when(emotionNeedType) {
            EmotionNeedType.NEEDS_CALMING -> createCalmingSteps()
            EmotionNeedType.NEEDS_UPLIFTING -> createUpliftingSteps()
            EmotionNeedType.NEEDS_STABILIZING -> createStabilizingSteps()
            EmotionNeedType.NEEDS_MODERATION -> createModerationSteps()
            EmotionNeedType.NEEDS_GROUNDING -> createGroundingSteps()
            EmotionNeedType.CAN_ENHANCE -> createEnhancementSteps()
            EmotionNeedType.CAN_DEEPEN -> createDeepeningSteps()
            EmotionNeedType.CAN_AMPLIFY -> createAmplificationSteps()
            EmotionNeedType.ALREADY_BALANCED -> createMaintenanceSteps()
        }

        progressBarTuning.max = tuningSteps.size
    }

    // 각 감정 유형별 단계 생성 (간단화된 버전)
    private fun createCalmingSteps(): List<TuningStep> {
        return listOf(
            TuningStep("1단계: 현재 감정 인식", "지금 화가 났다는 것을 인정해보세요.", "자유롭게"),
            TuningStep("2단계: 깊은 호흡", "4초 들이마시고, 7초 참고, 8초 내쉬기를 3회 반복하세요.", "약 2분"),
            TuningStep("3단계: 신체 이완", "어깨와 목의 긴장을 풀어주세요.", "약 1분"),
            TuningStep("4단계: 평온 상상", "마음이 평온한 상태를 상상해보세요.", "약 1분")
        )
    }

    private fun createUpliftingSteps(): List<TuningStep> {
        return listOf(
            TuningStep("1단계: 슬픔 수용", "지금 슬픈 마음을 인정하고 받아들여보세요.", "자유롭게"),
            TuningStep("2단계: 좋은 기억 떠올리기", "행복했던 순간들을 천천히 떠올려보세요.", "약 2분"),
            TuningStep("3단계: 감사 찾기", "지금 상황에서도 감사할 수 있는 작은 것들을 찾아보세요.", "약 2분"),
            TuningStep("4단계: 희망 품기", "내일은 더 나은 하루가 될 것임을 상상해보세요.", "약 1분")
        )
    }

    private fun createStabilizingSteps(): List<TuningStep> {
        return listOf(
            TuningStep("1단계: 불안 인식", "지금 불안한 마음을 있는 그대로 느껴보세요.", "자유롭게"),
            TuningStep("2단계: 5-4-3-2-1 기법", "보이는 것 5개, 들리는 것 4개, 만져지는 것 3개, 냄새 2개, 맛 1개를 찾아보세요.", "약 3분"),
            TuningStep("3단계: 현실 체크", "지금 이 순간 실제로 위험한 것이 있는지 확인해보세요.", "약 1분"),
            TuningStep("4단계: 안전감 느끼기", "지금 이곳이 안전하다는 것을 확인하고 안정감을 느껴보세요.", "약 1분")
        )
    }

    private fun createModerationSteps(): List<TuningStep> {
        return listOf(
            TuningStep("1단계: 과도한 감정 인식", "지금 감정이 조금 과도할 수 있음을 인정해보세요.", "자유롭게"),
            TuningStep("2단계: 천천히 호흡", "깊고 천천히 호흡하며 마음을 차분하게 만들어보세요.", "약 2분"),
            TuningStep("3단계: 균형 찾기", "적당한 수준의 좋은 감정을 상상해보세요.", "약 1분"),
            TuningStep("4단계: 지속 가능한 기쁨", "오래 지속될 수 있는 안정적인 기쁨을 느껴보세요.", "약 1분")
        )
    }

    private fun createGroundingSteps(): List<TuningStep> {
        return listOf(
            TuningStep("1단계: 들뜬 마음 인식", "지금 마음이 들떠있음을 알아차려보세요.", "자유롭게"),
            TuningStep("2단계: 발바닥 느끼기", "발바닥이 바닥에 닿아있는 감각을 느껴보세요.", "약 1분"),
            TuningStep("3단계: 천천히 움직이기", "의도적으로 천천히 움직이며 마음을 진정시켜보세요.", "약 2분"),
            TuningStep("4단계: 안정된 설렘", "차분하면서도 설레는 적절한 상태를 찾아보세요.", "약 1분")
        )
    }

    private fun createEnhancementSteps(): List<TuningStep> {
        return listOf(
            TuningStep("1단계: 현재 감정 깊이 느끼기", "지금의 좋은 감정을 온전히 경험해보세요.", "자유롭게"),
            TuningStep("2단계: 감정 확장하기", "이 감정이 몸 전체로 퍼져나가는 것을 상상해보세요.", "약 2분"),
            TuningStep("3단계: 감정 심화하기", "더 깊고 풍부한 감정으로 발전시켜보세요.", "약 2분"),
            TuningStep("4단계: 감정 간직하기", "이 아름다운 감정을 마음 깊이 간직해보세요.", "약 1분")
        )
    }

    private fun createDeepeningSteps(): List<TuningStep> {
        return listOf(
            TuningStep("1단계: 사랑의 근원 찾기", "이 사랑이 어디서 오는지 느껴보세요.", "자유롭게"),
            TuningStep("2단계: 사랑 표현하기", "마음속으로 사랑을 표현해보세요.", "약 2분"),
            TuningStep("3단계: 사랑 확장하기", "더 많은 대상으로 사랑을 확장해보세요.", "약 2분"),
            TuningStep("4단계: 무조건적 사랑", "조건 없는 따뜻한 사랑을 느껴보세요.", "약 1분")
        )
    }

    private fun createAmplificationSteps(): List<TuningStep> {
        return listOf(
            TuningStep("1단계: 기쁨의 원인 떠올리기", "무엇이 이렇게 기쁘게 하는지 생각해보세요.", "자유롭게"),
            TuningStep("2단계: 기쁨 나누기", "이 기쁨을 누구와 나누고 싶은지 상상해보세요.", "약 2분"),
            TuningStep("3단계: 기쁨 증폭하기", "기쁨이 더욱 밝고 크게 자라나는 것을 느껴보세요.", "약 2분"),
            TuningStep("4단계: 영원한 기쁨", "이 기쁨이 오래도록 지속되기를 바라며 간직해보세요.", "약 1분")
        )
    }

    private fun createMaintenanceSteps(): List<TuningStep> {
        return listOf(
            TuningStep("1단계: 현재 순간 음미", "지금 이 완벽한 감정을 깊이 음미해보세요.", "자유롭게"),
            TuningStep("2단계: 감정 탐색", "이 감정의 모든 면을 세심하게 탐색해보세요.", "약 2분"),
            TuningStep("3단계: 감사하기", "이런 감정을 느낄 수 있음에 감사해보세요.", "약 2분"),
            TuningStep("4단계: 기억 저장", "이 순간을 소중한 기억으로 저장해보세요.", "약 1분")
        )
    }

    // 수업 5주차 - Kotlin 코드로 화면 만들기
    private fun createStepByStepUI() {
        currentStepContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            background = ContextCompat.getDrawable(this@EmotionTunerActivity, R.drawable.card_background)
            setPadding(40, 30, 40, 30)
            visibility = View.GONE
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
            setLineSpacing(0f, 1.3f)
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
            text = "🎵 완료"
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@EmotionTunerActivity, R.color.text_primary))
            background = ContextCompat.getDrawable(this@EmotionTunerActivity, R.drawable.chord_button_bg)
            layoutParams = LinearLayout.LayoutParams(
                0, 112, 1f
            )
            visibility = View.GONE
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

    private fun startStepByStepTuning() {
        isActiveTuning = true
        currentStepIndex = 0

        val actionText = when(emotionNeedType) {
            EmotionNeedType.NEEDS_CALMING,
            EmotionNeedType.NEEDS_UPLIFTING,
            EmotionNeedType.NEEDS_STABILIZING,
            EmotionNeedType.NEEDS_MODERATION,
            EmotionNeedType.NEEDS_GROUNDING -> "❌ 조율 중단"
            EmotionNeedType.CAN_ENHANCE,
            EmotionNeedType.CAN_DEEPEN,
            EmotionNeedType.CAN_AMPLIFY -> "❌ 강화 중단"
            EmotionNeedType.ALREADY_BALANCED -> "❌ 음미 중단"
        }

        btnStartTuning.text = actionText
        btnStartTuning.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light))

        linearTuningProgress.visibility = View.VISIBLE
        currentStepContainer.visibility = View.VISIBLE

        showCurrentStep()

        val startMessage = when(emotionNeedType) {
            EmotionNeedType.NEEDS_CALMING,
            EmotionNeedType.NEEDS_UPLIFTING,
            EmotionNeedType.NEEDS_STABILIZING,
            EmotionNeedType.NEEDS_MODERATION,
            EmotionNeedType.NEEDS_GROUNDING -> "🎼 단계별 감정 조율을 시작합니다."
            EmotionNeedType.CAN_ENHANCE,
            EmotionNeedType.CAN_DEEPEN,
            EmotionNeedType.CAN_AMPLIFY -> "✨ 감정 강화 과정을 시작합니다."
            EmotionNeedType.ALREADY_BALANCED -> "💫 감정 음미 시간을 시작합니다."
        }

        Toast.makeText(this, "$startMessage 천천히 따라해보세요!", Toast.LENGTH_LONG).show()
    }

    private fun showCurrentStep() {
        if (currentStepIndex < tuningSteps.size) {
            val step = tuningSteps[currentStepIndex]

            tvStepTitle.text = step.title
            tvStepInstruction.text = "${step.instruction}\n\n⏱️ 예상 소요시간: ${step.duration}"

            progressBarTuning.progress = currentStepIndex + 1

            val statusText = when(emotionNeedType) {
                EmotionNeedType.NEEDS_CALMING,
                EmotionNeedType.NEEDS_UPLIFTING,
                EmotionNeedType.NEEDS_STABILIZING,
                EmotionNeedType.NEEDS_MODERATION,
                EmotionNeedType.NEEDS_GROUNDING -> "🎼 조율 진행 중"
                EmotionNeedType.CAN_ENHANCE,
                EmotionNeedType.CAN_DEEPEN,
                EmotionNeedType.CAN_AMPLIFY -> "✨ 강화 진행 중"
                EmotionNeedType.ALREADY_BALANCED -> "💫 음미 진행 중"
            }

            tvTuningStatus.text = "$statusText: ${currentStepIndex + 1} / ${tuningSteps.size} 단계"

            // 마지막 단계 체크
            if (currentStepIndex == tuningSteps.size - 1) {
                btnStepNext.visibility = View.GONE
                btnStepComplete.visibility = View.VISIBLE

                btnStepComplete.text = when(emotionNeedType) {
                    EmotionNeedType.NEEDS_CALMING,
                    EmotionNeedType.NEEDS_UPLIFTING,
                    EmotionNeedType.NEEDS_STABILIZING,
                    EmotionNeedType.NEEDS_MODERATION,
                    EmotionNeedType.NEEDS_GROUNDING -> "🎵 조율 완료"
                    EmotionNeedType.CAN_ENHANCE,
                    EmotionNeedType.CAN_DEEPEN,
                    EmotionNeedType.CAN_AMPLIFY -> "✨ 강화 완료"
                    EmotionNeedType.ALREADY_BALANCED -> "💫 음미 완료"
                }
            } else {
                btnStepNext.visibility = View.VISIBLE
                btnStepComplete.visibility = View.GONE
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

        val originalText = when(emotionNeedType) {
            EmotionNeedType.NEEDS_CALMING,
            EmotionNeedType.NEEDS_UPLIFTING,
            EmotionNeedType.NEEDS_STABILIZING,
            EmotionNeedType.NEEDS_MODERATION,
            EmotionNeedType.NEEDS_GROUNDING -> "🎚️ 감정 조율 시작"
            EmotionNeedType.CAN_ENHANCE,
            EmotionNeedType.CAN_DEEPEN,
            EmotionNeedType.CAN_AMPLIFY -> "✨ 감정 강화하기"
            EmotionNeedType.ALREADY_BALANCED -> "💫 현재 감정 음미하기"
        }

        btnStartTuning.text = originalText
        btnStartTuning.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_pink))

        currentStepContainer.visibility = View.GONE
        progressBarTuning.progress = progressBarTuning.max

        val completionMessage = when(emotionNeedType) {
            EmotionNeedType.NEEDS_CALMING -> "🎚️ 감정 조율 완료!\n${currentEmotionName}에서 ${targetEmotionName}으로 조율되었어요!"
            EmotionNeedType.NEEDS_UPLIFTING -> "🌈 감정 전환 완료!\n마음이 한결 밝아졌어요!"
            EmotionNeedType.NEEDS_STABILIZING -> "🧘 감정 안정화 완료!\n마음이 차분해졌어요!"
            EmotionNeedType.NEEDS_MODERATION -> "⚖️ 감정 조절 완료!\n적절한 수준으로 조절되었어요!"
            EmotionNeedType.NEEDS_GROUNDING -> "🌱 감정 안정화 완료!\n차분하고 안정된 상태가 되었어요!"
            EmotionNeedType.CAN_ENHANCE -> "✨ 감정 강화 완료!\n${currentEmotionName}이 더욱 풍부해졌어요!"
            EmotionNeedType.CAN_DEEPEN -> "💝 감정 심화 완료!\n${currentEmotionName}이 더욱 깊어졌어요!"
            EmotionNeedType.CAN_AMPLIFY -> "🌟 감정 증폭 완료!\n${currentEmotionName}이 더욱 밝아졌어요!"
            EmotionNeedType.ALREADY_BALANCED -> "💫 감정 음미 완료!\n소중한 시간이었어요!"
        }

        tvTuningStatus.text = completionMessage

        Toast.makeText(this, completionMessage, Toast.LENGTH_LONG).show()
    }

    private fun stopTuning() {
        isActiveTuning = false

        val originalText = when(emotionNeedType) {
            EmotionNeedType.NEEDS_CALMING,
            EmotionNeedType.NEEDS_UPLIFTING,
            EmotionNeedType.NEEDS_STABILIZING,
            EmotionNeedType.NEEDS_MODERATION,
            EmotionNeedType.NEEDS_GROUNDING -> "🎚️ 감정 조율 시작"
            EmotionNeedType.CAN_ENHANCE,
            EmotionNeedType.CAN_DEEPEN,
            EmotionNeedType.CAN_AMPLIFY -> "✨ 감정 강화하기"
            EmotionNeedType.ALREADY_BALANCED -> "💫 현재 감정 음미하기"
        }

        btnStartTuning.text = originalText
        btnStartTuning.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_pink))

        currentStepContainer.visibility = View.GONE
        tvTuningStatus.text = "⏸️ 중단되었습니다"
        progressBarTuning.progress = 0
        currentStepIndex = 0

        Toast.makeText(this, "중단되었습니다. 언제든 다시 시작하세요!", Toast.LENGTH_SHORT).show()
    }

    // 수업 7주차 - 대화상자 활용
    private fun showExitConfirmDialog() {
        val actionName = when(emotionNeedType) {
            EmotionNeedType.NEEDS_CALMING,
            EmotionNeedType.NEEDS_UPLIFTING,
            EmotionNeedType.NEEDS_STABILIZING,
            EmotionNeedType.NEEDS_MODERATION,
            EmotionNeedType.NEEDS_GROUNDING -> "조율"
            EmotionNeedType.CAN_ENHANCE,
            EmotionNeedType.CAN_DEEPEN,
            EmotionNeedType.CAN_AMPLIFY -> "강화"
            EmotionNeedType.ALREADY_BALANCED -> "음미"
        }

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("🤔 ${actionName} 중단")
        builder.setMessage("${actionName}을 중단하고 나가시겠어요?\n현재 진행상황이 저장되지 않습니다.")
        builder.setPositiveButton("나가기") { _, _ ->
            finish()
        }
        builder.setNegativeButton("계속하기", null)
        builder.show()
    }

    private fun showTargetEmotionSelectionDialog() {
        val emotions = arrayOf("♪ 기쁨", "♩ 평온", "♫ 설렘", "♭ 슬픔", "♯ 화남", "𝄢 불안", "♡ 사랑")
        val emotionSymbols = arrayOf("♪", "♩", "♫", "♭", "♯", "𝄢", "♡")
        val emotionNames = arrayOf("기쁨", "평온", "설렘", "슬픔", "화남", "불안", "사랑")

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("🎯 목표 감정 선택")
        builder.setItems(emotions) { _, which ->
            targetEmotionSymbol = emotionSymbols[which]
            targetEmotionName = emotionNames[which]

            updateTargetDisplay()
            generateActivityGuide()
            setupTuningSteps()

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
}