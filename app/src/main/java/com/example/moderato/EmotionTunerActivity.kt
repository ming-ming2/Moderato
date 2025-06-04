package com.example.moderato

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog // androidx.appcompat.app.AlertDialog로 변경
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class EmotionTunerActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var tvCurrentEmotion: TextView
    private lateinit var tvCurrentEmotionIcon: TextView
    private lateinit var tvCurrentIntensity: TextView
    private lateinit var tvTuningTitle: TextView
    private lateinit var tvTuningDescription: TextView
    private lateinit var linearTuningProgress: LinearLayout
    private lateinit var progressBarTuning: ProgressBar
    private lateinit var tvTuningStatus: TextView
    private lateinit var btnStartTuning: Button

    private var isAdvancedMode = false
    private var emotionPattern = ""
    private var emotionPolarity = ""
    private var emotionIntensity = ""
    private var therapyFocus = ""
    private var therapyTitle = ""
    private var therapyDescription = ""
    private var therapyTechniques = arrayOf<String>()
    private var therapyTime = ""
    private var totalEmotions = 0
    private var variabilityScore = 0f

    private var currentEmotionSymbol = "♪"
    private var currentEmotionName = "기쁨"
    private var currentStep = 0
    private var totalSteps = 3 // 기본값, 고급 모드에서 덮어쓰여짐
    private var isActiveTuning = false
    private var tuningType = TuningType.BALANCE // 기본값, 일반 모드에서 사용

    enum class TuningType {
        CALM, UPLIFT, STABILIZE, BALANCE, FOCUS, DEEPEN, HEALTHIFY
    }

    data class TuningPlan(
        val title: String,
        val description: String,
        val steps: Array<String>,
        val timeEstimate: String,
        val color: Int,
        val icon: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emotion_tuner)

        loadIntentData()
        initViews()
        setupClickListeners()

        if (isAdvancedMode) {
            loadAdvancedEmotion()
        } else {
            loadCurrentEmotion()
        }
    }

    private fun loadIntentData() {
        currentEmotionSymbol = intent.getStringExtra("CURRENT_EMOTION_SYMBOL") ?: "♪"
        currentEmotionName = intent.getStringExtra("CURRENT_EMOTION_NAME") ?: "기쁨"

        emotionPattern = intent.getStringExtra("EMOTION_PATTERN") ?: ""
        isAdvancedMode = emotionPattern.isNotEmpty()

        if (isAdvancedMode) {
            emotionPolarity = intent.getStringExtra("EMOTION_POLARITY") ?: ""
            emotionIntensity = intent.getStringExtra("EMOTION_INTENSITY") ?: ""
            therapyFocus = intent.getStringExtra("THERAPY_FOCUS") ?: ""
            therapyTitle = intent.getStringExtra("THERAPY_TITLE") ?: ""
            therapyDescription = intent.getStringExtra("THERAPY_DESCRIPTION") ?: ""
            therapyTechniques = intent.getStringArrayExtra("THERAPY_TECHNIQUES") ?: arrayOf()
            therapyTime = intent.getStringExtra("THERAPY_TIME") ?: ""
            totalEmotions = intent.getIntExtra("TOTAL_EMOTIONS", 0)
            variabilityScore = intent.getFloatExtra("VARIABILITY_SCORE", 0f)
        }
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvCurrentEmotion = findViewById(R.id.tvCurrentEmotion)
        tvCurrentEmotionIcon = findViewById(R.id.tvCurrentEmotionIcon)
        tvCurrentIntensity = findViewById(R.id.tvCurrentIntensity)
        tvTuningTitle = findViewById(R.id.tvTherapyDescription) // ID가 일치하는지 확인 필요, 기존 tvTuningTitle 사용 가능성
        tvTuningDescription = findViewById(R.id.tvActivityGuide) // ID가 일치하는지 확인 필요, 기존 tvTuningDescription 사용 가능성
        linearTuningProgress = findViewById(R.id.linearTuningProgress)
        progressBarTuning = findViewById(R.id.progressBarTuning)
        tvTuningStatus = findViewById(R.id.tvTuningStatus)
        btnStartTuning = findViewById(R.id.btnStartTuning)

        linearTuningProgress.visibility = View.VISIBLE
        findViewById<View>(R.id.rgTherapyMethod)?.visibility = View.GONE
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
                startTuning()
            } else {
                if (currentStep >= totalSteps) {
                    completeTuning()
                } else {
                    nextStep()
                }
            }
        }
    }

    private fun loadCurrentEmotion() {
        tvCurrentEmotion.text = currentEmotionName
        tvCurrentEmotionIcon.text = currentEmotionSymbol
        tvCurrentEmotionIcon.setTextColor(getEmotionColor(currentEmotionSymbol))

        tuningType = determineTuningType()
        val tuningPlan = getTuningPlan(tuningType)
        totalSteps = tuningPlan.steps.size

        setupTuningPlan(tuningPlan)
        updateStepDisplay()
    }

    private fun loadAdvancedEmotion() {
        tvCurrentEmotion.text = currentEmotionName
        tvCurrentEmotionIcon.text = currentEmotionSymbol
        tvCurrentEmotionIcon.setTextColor(getEmotionColor(currentEmotionSymbol))
        tvCurrentIntensity.text = getAdvancedStatusText()
        setupAdvancedTuningPlan()
        updateStepDisplay()
    }

    private fun getAdvancedStatusText(): String {
        return when(therapyFocus) {
            "AWARENESS" -> "감정 인식 필요"
            "STABILITY" -> "안정화 필요"
            "HARMONY" -> "조율 필요"
            "ENSEMBLE" -> "관계 조화 필요"
            else -> "조율 필요"
        }
    }

    private fun setupAdvancedTuningPlan() {
        tvTuningTitle.text = therapyTitle
        tvTuningDescription.text = buildString {
            append(therapyDescription)
            append("\n\n")
            append("📊 분석 결과:\n")
            append("• 총 ${totalEmotions}개 감정 기록\n")
            append("• 패턴: ${getPatternKorean(emotionPattern)}\n")
            append("• 성향: ${getPolarityKorean(emotionPolarity)}\n")
            append("• 강도: ${getIntensityKorean(emotionIntensity)}\n")
            append("• 변동성: ${"%.1f".format(variabilityScore)}\n\n")
            append("⏱️ 예상 소요시간: ${therapyTime}")
        }

        totalSteps = therapyTechniques.size
        progressBarTuning.max = totalSteps

        val progressColor = when(therapyFocus) {
            "AWARENESS" -> ContextCompat.getColor(this, R.color.primary_purple)
            "STABILITY" -> ContextCompat.getColor(this, android.R.color.holo_blue_light)
            "HARMONY" -> ContextCompat.getColor(this, R.color.primary_pink)
            "ENSEMBLE" -> ContextCompat.getColor(this, R.color.secondary_orange)
            else -> ContextCompat.getColor(this, R.color.primary_pink)
        }
        progressBarTuning.progressTintList = ColorStateList.valueOf(progressColor)
    }


    private fun determineTuningType(): TuningType {
        return when(currentEmotionSymbol) {
            "♯" -> TuningType.CALM
            "♭" -> TuningType.UPLIFT
            "𝄢" -> TuningType.STABILIZE
            "♪" -> TuningType.BALANCE
            "♫" -> TuningType.FOCUS
            "♩" -> TuningType.DEEPEN
            "♡" -> TuningType.HEALTHIFY
            else -> TuningType.BALANCE
        }
    }

    private fun getTuningPlan(type: TuningType): TuningPlan {
        return when(type) {
            TuningType.CALM -> TuningPlan(
                title = "🌊 진정 조율",
                description = "화난 마음을 차분하게 가라앉혀보겠습니다.\n깊은 호흡과 함께 마음의 평온을 되찾아보세요.",
                steps = arrayOf(
                    "4-7-8 호흡법으로 즉시 진정하기 (들이마시기 4초, 참기 7초, 내쉬기 8초)",
                    "지금 화난 감정과 실제 상황을 분리해서 생각해보기",
                    "이 상황에 대한 건설적인 해결 방법 생각해보기"
                ),
                timeEstimate = "5-8분",
                color = ContextCompat.getColor(this, android.R.color.holo_blue_light),
                icon = "🌊"
            )
            TuningType.UPLIFT -> TuningPlan(
                title = "🌈 전환 조율",
                description = "슬픈 마음을 따뜻하게 받아들이며 희망을 찾아보겠습니다.\n슬픔도 소중한 감정임을 인정하면서 천천히 전환해보세요.",
                steps = arrayOf(
                    "지금의 슬픈 마음을 있는 그대로 충분히 느끼고 받아들이기",
                    "이 슬픈 경험에서 배울 수 있는 의미나 교훈 찾아보기",
                    "작은 희망이나 감사할 수 있는 것들 천천히 떠올려보기"
                ),
                timeEstimate = "8-12분",
                color = ContextCompat.getColor(this, android.R.color.holo_purple),
                icon = "🌈"
            )
            TuningType.STABILIZE -> TuningPlan(
                title = "🧘 안정 조율",
                description = "불안한 마음을 현실과 연결하여 안정감을 찾아보겠습니다.\n지금 이 순간에 집중하며 마음의 안정을 되찾아보세요.",
                steps = arrayOf(
                    "5-4-3-2-1 기법: 보이는 것 5개, 들리는 것 4개, 만져지는 것 3개, 냄새 2개, 맛 1개 찾기",
                    "실제 위험도와 내 걱정 수준을 객관적으로 비교해보기",
                    "지금 상황에서 실제로 할 수 있는 구체적인 대처 방안 정리하기"
                ),
                timeEstimate = "6-10분",
                color = ContextCompat.getColor(this, android.R.color.holo_green_light),
                icon = "🧘"
            )
            TuningType.BALANCE -> TuningPlan(
                title = "⚖️ 균형 조율",
                description = "과도한 흥분을 지속 가능한 만족감으로 조절해보겠습니다.\n기쁨을 잃지 않으면서도 현실감각을 유지해보세요.",
                steps = arrayOf(
                    "현재 상황을 객관적으로 점검하고 현실 감각 되찾기",
                    "흥분된 에너지를 차분하고 안정적인 만족감으로 전환하기",
                    "이 좋은 기분을 오래 지속시킬 수 있는 방법 계획하기"
                ),
                timeEstimate = "4-6분",
                color = ContextCompat.getColor(this, R.color.secondary_orange),
                icon = "⚖️"
            )
            TuningType.FOCUS -> TuningPlan(
                title = "🎯 집중 조율",
                description = "산만한 설렘을 집중된 기대감으로 정리해보겠습니다.\n설레는 마음은 그대로 두면서 집중력을 회복해보세요.",
                steps = arrayOf(
                    "설레는 마음을 충분히 인정하고 받아들이기",
                    "지금 당장 해야 할 일에 마음을 집중하기",
                    "설렘을 건강하고 현실적인 기대감으로 정리하기"
                ),
                timeEstimate = "4-7분",
                color = ContextCompat.getColor(this, R.color.primary_pink),
                icon = "🎯"
            )
            TuningType.DEEPEN -> TuningPlan(
                title = "🕯️ 심화 조율",
                description = "현재의 평온함을 더욱 깊고 풍부하게 발전시켜보겠습니다.\n이 소중한 평온을 깊이 음미하고 확장해보세요.",
                steps = arrayOf(
                    "현재의 평온한 상태를 온몸으로 깊이 느끼고 음미하기",
                    "이 평온을 가능하게 해준 모든 것들에 깊이 감사하기",
                    "내면의 고요함을 더 깊이 탐색하고 확장하기"
                ),
                timeEstimate = "6-10분",
                color = ContextCompat.getColor(this, R.color.primary_purple),
                icon = "🕯️"
            )
            TuningType.HEALTHIFY -> TuningPlan(
                title = "💝 건강화 조율",
                description = "사랑의 감정을 더욱 건강하고 균형잡힌 방향으로 발전시켜보겠습니다.\n나와 상대방 모두에게 좋은 사랑으로 만들어보세요.",
                steps = arrayOf(
                    "내 사랑이 건강한 사랑인지 솔직하게 점검해보기",
                    "나 자신과 상대방 사이의 균형이 잘 맞는지 확인하기",
                    "사랑을 더 건강하고 아름다운 방식으로 표현하는 방법 계획하기"
                ),
                timeEstimate = "5-8분",
                color = ContextCompat.getColor(this, android.R.color.holo_red_light),
                icon = "💝"
            )
        }
    }

    private fun setupTuningPlan(plan: TuningPlan) {
        tvTuningTitle.text = "${plan.icon} ${plan.title}"
        tvTuningDescription.text = "${plan.description}\n\n예상 소요시간: ${plan.timeEstimate}"
        progressBarTuning.max = totalSteps
        progressBarTuning.progress = 0
        progressBarTuning.progressTintList = ColorStateList.valueOf(plan.color)
        tvCurrentIntensity.text = when(tuningType) {
            TuningType.CALM -> "진정 필요"
            TuningType.UPLIFT -> "전환 필요"
            TuningType.STABILIZE -> "안정 필요"
            TuningType.BALANCE -> "균형 필요"
            TuningType.FOCUS -> "집중 필요"
            TuningType.DEEPEN -> "심화 가능"
            TuningType.HEALTHIFY -> "건강화 필요"
        }
    }

    private fun updateStepDisplay() {
        if (isAdvancedMode) {
            if (!isActiveTuning) {
                tvTuningStatus.text = "${getTherapyIcon(therapyFocus)} 조율 준비 완료. 시작하시겠어요?"
                btnStartTuning.text = "${getTherapyIcon(therapyFocus)} 조율 시작하기"
            } else if (currentStep < therapyTechniques.size) {
                tvTuningStatus.text = "단계 ${currentStep + 1}: ${therapyTechniques[currentStep]}"
                btnStartTuning.text = "✅ 다음 단계 (${currentStep + 1}/${totalSteps})"
            } else {
                tvTuningStatus.text = "${getTherapyIcon(therapyFocus)} 모든 단계 완료! 마무리하시겠어요?"
                btnStartTuning.text = "${getTherapyIcon(therapyFocus)} 조율 완료"
            }
        } else {
            val plan = getTuningPlan(tuningType)
            if (!isActiveTuning) {
                tvTuningStatus.text = "${plan.icon} 조율 준비 완료. 시작하시겠어요?"
                btnStartTuning.text = "${plan.icon} 조율 시작하기"
            } else if (currentStep < plan.steps.size) {
                tvTuningStatus.text = "단계 ${currentStep + 1}: ${plan.steps[currentStep]}"
                btnStartTuning.text = "✅ 다음 단계 (${currentStep + 1}/${totalSteps})"
            } else {
                tvTuningStatus.text = "${plan.icon} 모든 단계 완료! 마무리하시겠어요?"
                btnStartTuning.text = "${plan.icon} 조율 완료"
            }
        }
        progressBarTuning.progress = currentStep
    }

    private fun startTuning() {
        isActiveTuning = true
        currentStep = 0
        updateStepDisplay()

        val startMessage = if (isAdvancedMode) {
            "${getTherapyIcon(therapyFocus)} ${therapyTitle}을 시작합니다"
        } else {
            val plan = getTuningPlan(tuningType)
            "${plan.icon} ${plan.title}을 시작합니다"
        }
        Toast.makeText(this, startMessage, Toast.LENGTH_SHORT).show()
    }

    private fun nextStep() {
        currentStep++
        updateStepDisplay()

        val messages = if (isAdvancedMode) {
            getEncouragementMessages()
        } else {
            when(tuningType) {
                TuningType.CALM -> arrayOf("🌊 좋아요! 마음이 차분해지고 있어요", "💙 화가 가라앉고 있어요", "🕊️ 평온함을 되찾고 있어요")
                TuningType.UPLIFT -> arrayOf("🌈 잘하고 있어요! 희망이 보이기 시작해요", "✨ 마음이 조금씩 밝아지고 있어요", "🌅 새로운 관점이 생기고 있어요")
                TuningType.STABILIZE -> arrayOf("🧘 훌륭해요! 현실감을 되찾고 있어요", "🌿 마음이 안정되어가고 있어요", "💚 불안감이 줄어들고 있어요")
                TuningType.BALANCE -> arrayOf("⚖️ 좋아요! 균형을 찾아가고 있어요", "🎯 현실감각을 되찾고 있어요", "💫 지속가능한 기쁨을 만들어가고 있어요")
                TuningType.FOCUS -> arrayOf("🎯 집중력이 돌아오고 있어요", "💎 설렘이 건강한 기대감으로 바뀌고 있어요", "🌟 마음이 정리되고 있어요")
                TuningType.DEEPEN -> arrayOf("🕯️ 평온이 더욱 깊어지고 있어요", "🙏 감사의 마음이 커지고 있어요", "💜 내면의 고요함이 확장되고 있어요")
                TuningType.HEALTHIFY -> arrayOf("💝 사랑이 더욱 건강해지고 있어요", "💕 균형잡힌 관계를 만들어가고 있어요", "🌸 아름다운 사랑으로 발전하고 있어요")
            }
        }
        val messageIndex = (currentStep - 1).coerceIn(0, messages.size - 1)
        Toast.makeText(this, messages[messageIndex], Toast.LENGTH_SHORT).show()
    }

    private fun completeTuning() {
        val completionMessage = if (isAdvancedMode) {
            when(therapyFocus) {
                "AWARENESS" -> "🎧 감정 청음 완료! 마음의 소리가 명확해졌어요"
                "STABILITY" -> "⚡ 불협화음 해결 완료! 마음이 안정되었어요"
                "HARMONY" -> "🎹 감정 조율 완료! 아름다운 하모니를 찾았어요"
                "ENSEMBLE" -> "🤝 관계 조화 완료! 건강한 소통이 가능해요"
                else -> "🎵 감정 조율 완료! 마음의 균형을 되찾았어요"
            }
        } else {
            when(tuningType) {
                TuningType.CALM -> "🌊 진정 조율 완료! 마음이 평온해졌어요"
                TuningType.UPLIFT -> "🌈 전환 조율 완료! 희망이 생겼어요"
                TuningType.STABILIZE -> "🧘 안정 조율 완료! 마음이 안정되었어요"
                TuningType.BALANCE -> "⚖️ 균형 조율 완료! 적절한 균형을 찾았어요"
                TuningType.FOCUS -> "🎯 집중 조율 완료! 마음이 정리되었어요"
                TuningType.DEEPEN -> "🕯️ 심화 조율 완료! 더 깊은 평온을 얻었어요"
                TuningType.HEALTHIFY -> "💝 건강화 조율 완료! 더 건강한 사랑이 되었어요"
            }
        }
        Toast.makeText(this, completionMessage, Toast.LENGTH_LONG).show()

        btnStartTuning.postDelayed({
            setResult(RESULT_OK)
            finish()
        }, 2000)
    }

    private fun showExitConfirmDialog() {
        // androidx.appcompat.app.AlertDialog.Builder 사용
        val builder = AlertDialog.Builder(this, R.style.DarkDialogTheme)
        builder.setTitle("🤔 조율 중단")
        builder.setMessage("조율을 중단하고 나가시겠어요?\n현재 진행상황이 저장되지 않습니다.")
        builder.setPositiveButton("나가기") { _, _ -> finish() }
        builder.setNegativeButton("계속하기", null)
        val dialog = builder.show()

        dialog.findViewById<TextView>(android.R.id.message)?.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(ContextCompat.getColor(this, R.color.primary_pink))
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

    private fun getPatternKorean(pattern: String): String {
        return when(pattern) {
            "STABLE" -> "안정적"
            "FLUCTUATING" -> "변동적"
            "CHAOTIC" -> "불안정"
            else -> "알 수 없음"
        }
    }

    private fun getPolarityKorean(polarity: String): String {
        return when(polarity) {
            "POSITIVE_DOMINANT" -> "긍정적"
            "NEGATIVE_DOMINANT" -> "부정적"
            "MIXED" -> "복합적"
            "NEUTRAL" -> "중립적"
            else -> "알 수 없음"
        }
    }

    private fun getIntensityKorean(intensity: String): String {
        return when(intensity) {
            "OVERWHELMING" -> "매우 강함"
            "HIGH" -> "강함"
            "MODERATE" -> "보통"
            "LOW" -> "약함"
            else -> "보통"
        }
    }

    private fun getTherapyIcon(focus: String): String {
        return when(focus) {
            "AWARENESS" -> "🎧"
            "STABILITY" -> "⚡"
            "HARMONY" -> "🎹"
            "ENSEMBLE" -> "🤝"
            else -> "🎵"
        }
    }

    private fun getEncouragementMessages(): Array<String> {
        return when(therapyFocus) {
            "AWARENESS" -> arrayOf(
                "🎧 좋아요! 감정을 명확하게 인식하고 있어요",
                "🌟 마음의 소리에 집중하고 있어요",
                "✨ 현재 순간과 연결되고 있어요"
            )
            "STABILITY" -> arrayOf(
                "⚡ 잘하고 있어요! 마음이 안정되고 있어요",
                "🌊 감정의 파도가 잠잠해지고 있어요",
                "💙 균형을 되찾고 있어요"
            )
            "HARMONY" -> arrayOf(
                "🎹 훌륭해요! 감정의 조화를 만들어가고 있어요",
                "🎵 아름다운 선율이 흘러나오고 있어요",
                "💫 완벽한 하모니에 가까워지고 있어요"
            )
            "ENSEMBLE" -> arrayOf(
                "🤝 좋아요! 관계의 조화를 만들어가고 있어요",
                "💕 건강한 소통이 가능해지고 있어요",
                "🌈 아름다운 인간관계를 만들어가고 있어요"
            )
            else -> arrayOf("🎵 잘하고 있어요!", "💪 계속 해보세요!", "✨ 멋져요!")
        }
    }
}