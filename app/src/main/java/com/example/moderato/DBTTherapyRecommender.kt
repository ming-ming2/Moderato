package com.example.moderato

/**
 * DBT 기반 조율 추천 시스템
 * 수업 3주차 - when문, 조건문, 메소드 활용
 */
class DBTTherapyRecommender {

    // 수업 3주차 - enum 클래스로 조율 방향 정의
    enum class TherapyFocus {
        AWARENESS,        // 감정 청음 (Mindfulness)
        STABILITY,        // 불협화음 해결 (Distress Tolerance)
        HARMONY,          // 감정 조율 (Emotion Regulation)
        ENSEMBLE          // 하모니 만들기 (Interpersonal Effectiveness)
    }

    // 수업 3주차 - 데이터 클래스로 조율 계획 정의
    data class TherapyPlan(
        val focus: TherapyFocus,
        val title: String,
        val icon: String,
        val description: String,
        val techniques: List<String>,
        val estimatedTime: String,
        val priority: Float  // 우선순위 점수
    )

    // 수업 3주차 - 데이터 클래스로 DBT 필요도 정의
    data class DBTNeedAssessment(
        val mindfulnessNeed: Float,      // 감정 인식 필요도 (0.0 ~ 1.0)
        val distressToleranceNeed: Float, // 고통 견디기 필요도
        val emotionRegulationNeed: Float, // 감정 조절 필요도
        val interpersonalNeed: Float      // 대인관계 필요도
    )

    /**
     * 감정 분석 결과를 바탕으로 조율 방법 추천
     * 수업 3주차 - 메소드와 조건문 활용
     */
    fun recommendTherapy(analysis: EmotionPatternAnalyzer.EmotionAnalysis): TherapyPlan {
        // 1. DBT 4영역 필요도 계산
        val needs = assessDBTNeeds(analysis)

        // 2. 가장 필요한 영역 결정
        val focus = determinePrimaryFocus(needs)

        // 3. 구체적인 조율 계획 생성
        return generateTherapyPlan(focus, analysis, needs)
    }

    /**
     * DBT 4영역별 필요도 계산
     * 수업 3주차 - when문과 계산 활용
     */
    private fun assessDBTNeeds(analysis: EmotionPatternAnalyzer.EmotionAnalysis): DBTNeedAssessment {
        // 마음챙김 필요도 - 패턴이 혼란스러울수록 높음
        val mindfulnessNeed = when(analysis.pattern) {
            EmotionPatternAnalyzer.EmotionalPattern.CHAOTIC -> 0.9f
            EmotionPatternAnalyzer.EmotionalPattern.FLUCTUATING -> 0.6f
            EmotionPatternAnalyzer.EmotionalPattern.STABLE -> 0.3f
        }

        // 고통 견디기 필요도 - 부정 감정이 강할수록 높음
        val distressToleranceNeed = when {
            analysis.polarity == EmotionPatternAnalyzer.EmotionalPolarity.NEGATIVE_DOMINANT &&
                    analysis.intensity == EmotionPatternAnalyzer.IntensityLevel.OVERWHELMING -> 0.9f
            analysis.polarity == EmotionPatternAnalyzer.EmotionalPolarity.NEGATIVE_DOMINANT -> 0.7f
            analysis.intensity == EmotionPatternAnalyzer.IntensityLevel.OVERWHELMING -> 0.6f
            else -> 0.3f
        }

        // 감정 조절 필요도 - 변동성이나 강도가 클 때 높음
        val emotionRegulationNeed = when {
            analysis.pattern == EmotionPatternAnalyzer.EmotionalPattern.CHAOTIC -> 0.8f
            analysis.intensity == EmotionPatternAnalyzer.IntensityLevel.OVERWHELMING -> 0.7f
            analysis.polarity == EmotionPatternAnalyzer.EmotionalPolarity.MIXED -> 0.6f
            else -> 0.4f
        }

        // 대인관계 필요도 - 화남, 슬픔이 있으면 관계 영향 가능성
        val interpersonalNeed = when {
            analysis.dominantEmotion in listOf("♯", "♭") -> 0.6f
            analysis.pattern == EmotionPatternAnalyzer.EmotionalPattern.CHAOTIC -> 0.5f
            else -> 0.3f
        }

        return DBTNeedAssessment(mindfulnessNeed, distressToleranceNeed,
            emotionRegulationNeed, interpersonalNeed)
    }

    /**
     * 우선순위가 가장 높은 조율 영역 결정
     * 수업 3주차 - 조건문과 배열 처리
     */
    private fun determinePrimaryFocus(needs: DBTNeedAssessment): TherapyFocus {
        val needsList = listOf(
            TherapyFocus.AWARENESS to needs.mindfulnessNeed,
            TherapyFocus.STABILITY to needs.distressToleranceNeed,
            TherapyFocus.HARMONY to needs.emotionRegulationNeed,
            TherapyFocus.ENSEMBLE to needs.interpersonalNeed
        )

        return needsList.maxByOrNull { it.second }?.first ?: TherapyFocus.HARMONY
    }

    /**
     * 구체적인 조율 계획 생성
     * 수업 3주차 - when문과 문자열 처리
     */
    private fun generateTherapyPlan(
        focus: TherapyFocus,
        analysis: EmotionPatternAnalyzer.EmotionAnalysis,
        needs: DBTNeedAssessment
    ): TherapyPlan {
        return when(focus) {
            TherapyFocus.AWARENESS -> createAwarenessTherapy(analysis)
            TherapyFocus.STABILITY -> createStabilityTherapy(analysis)
            TherapyFocus.HARMONY -> createHarmonyTherapy(analysis)
            TherapyFocus.ENSEMBLE -> createEnsembleTherapy(analysis)
        }
    }

    /**
     * 감정 청음 (마음챙김) 조율 계획
     */
    private fun createAwarenessTherapy(analysis: EmotionPatternAnalyzer.EmotionAnalysis): TherapyPlan {
        val techniques = when(analysis.pattern) {
            EmotionPatternAnalyzer.EmotionalPattern.CHAOTIC -> listOf(
                "🎧 지금 마음에서 들리는 모든 감정을 하나씩 구별해보세요",
                "🔊 가장 큰 소리(강한 감정)부터 차례로 들어보세요",
                "🎵 각 감정의 음색과 크기를 판단해보세요",
                "⏸️ 잠시 멈춰서 현재 이 순간에만 집중해보세요"
            )
            EmotionPatternAnalyzer.EmotionalPattern.FLUCTUATING -> listOf(
                "🎼 감정이 어떤 리듬으로 변하는지 관찰해보세요",
                "𝄽 각 변화 사이의 쉼표(휴식)를 찾아보세요",
                "📊 변화의 패턴을 악보처럼 그려보세요"
            )
            else -> listOf(
                "🧘 현재의 안정된 상태를 깊이 느껴보세요",
                "🌟 이 평온함을 유지하는 방법을 생각해보세요"
            )
        }

        return TherapyPlan(
            focus = TherapyFocus.AWARENESS,
            title = "🎧 감정 청음 - 마음의 소리 듣기",
            icon = "🎧",
            description = "혼란스러운 감정들을 하나씩 명확하게 인식하고 현재 순간에 집중하는 연습을 해보겠습니다.",
            techniques = techniques,
            estimatedTime = "5-8분",
            priority = 0.9f
        )
    }

    /**
     * 불협화음 해결 (고통 견디기) 조율 계획
     */
    private fun createStabilityTherapy(analysis: EmotionPatternAnalyzer.EmotionAnalysis): TherapyPlan {
        val techniques = when {
            analysis.intensity == EmotionPatternAnalyzer.IntensityLevel.OVERWHELMING -> listOf(
                "⏹️ 𝄽♩👁♪ 기법: 일시정지 → 호흡 박자 → 현재 화음 확인 → 새 선율",
                "🔄 감정의 포르테시모(ff)를 메조포르테(mf)로 줄여보세요",
                "🎵 불협화음도 음악의 일부임을 인정하고 해결음을 찾아보세요",
                "🌊 감정의 파도가 지나가기를 기다려보세요"
            )
            analysis.polarity == EmotionPatternAnalyzer.EmotionalPolarity.NEGATIVE_DOMINANT -> listOf(
                "❄️ 찬물로 세수하거나 차가운 것을 만져보세요",
                "🫁 4-7-8 호흡법: 4초 들이마시기, 7초 참기, 8초 내쉬기",
                "🏃 제자리에서 가볍게 운동해보세요",
                "🎭 이 감정이 지나갈 것임을 믿어보세요"
            )
            else -> listOf(
                "😤 깊게 숨을 쉬며 마음을 진정시켜보세요",
                "🤲 현재 상황을 있는 그대로 받아들여보세요"
            )
        }

        return TherapyPlan(
            focus = TherapyFocus.STABILITY,
            title = "⚡ 불협화음 해결 - 감정 안정화",
            icon = "⚡",
            description = "강렬한 부정적 감정을 안전하게 견디고 점진적으로 안정화시키는 기법을 연습해보겠습니다.",
            techniques = techniques,
            estimatedTime = "6-10분",
            priority = 0.9f
        )
    }

    /**
     * 감정 조율 (감정 조절) 조율 계획
     */
    private fun createHarmonyTherapy(analysis: EmotionPatternAnalyzer.EmotionAnalysis): TherapyPlan {
        val dominantEmotionName = EmotionPatternAnalyzer().getEmotionNameFromSymbol(analysis.dominantEmotion)

        val techniques = when(analysis.pattern) {
            EmotionPatternAnalyzer.EmotionalPattern.CHAOTIC -> listOf(
                "🔄 반대 화성 연주: ${getOppositeEmotionAction(analysis.dominantEmotion)}",
                "⏰ 템포 조절: 급격한 변화를 안단테(천천히)로 바꿔보세요",
                "🔍 화음 체크: 지금 상황이 정말 이 감정에 맞는 화음인가요?",
                "🎹 감정의 볼륨을 적절히 조절해보세요"
            )
            else -> listOf(
                "🎨 지금 감정에 어울리는 색깔로 생각을 바꿔보세요",
                "📝 이 감정이 무엇을 말하려는지 들어보세요",
                "⚖️ 감정과 이성의 균형을 맞춰보세요"
            )
        }

        return TherapyPlan(
            focus = TherapyFocus.HARMONY,
            title = "🎹 감정 조율 - 마음의 하모니",
            icon = "🎹",
            description = "감정의 강도와 방향을 적절히 조절하여 상황에 맞는 균형잡힌 상태를 만들어보겠습니다.",
            techniques = techniques,
            estimatedTime = "4-7분",
            priority = 0.8f
        )
    }

    /**
     * 하모니 만들기 (대인관계) 조율 계획
     */
    private fun createEnsembleTherapy(analysis: EmotionPatternAnalyzer.EmotionAnalysis): TherapyPlan {
        val techniques = listOf(
            "🤝 지금 상태에서 다른 사람과 어떻게 소통할지 생각해보세요",
            "💬 감정을 표현할 때 사용할 적절한 톤을 선택해보세요",
            "👂 상대방의 입장에서 생각해보는 시간을 가져보세요",
            "🎭 관계에서 나의 역할과 책임을 점검해보세요"
        )

        return TherapyPlan(
            focus = TherapyFocus.ENSEMBLE,
            title = "🤝 하모니 만들기 - 관계 조율",
            icon = "🤝",
            description = "현재 감정 상태에서도 건강한 관계를 유지하고 효과적으로 소통하는 방법을 연습해보겠습니다.",
            techniques = techniques,
            estimatedTime = "5-8분",
            priority = 0.7f
        )
    }

    /**
     * 반대 감정 행동 제안 - DBT의 Opposite Action 기법
     */
    private fun getOppositeEmotionAction(emotionSymbol: String): String {
        return when(emotionSymbol) {
            "♭" -> "밝은 음악 들으며 활동적인 행동하기"
            "♯" -> "부드럽고 친절한 말투로 대화하기"
            "𝄢" -> "용기를 내어 한 걸음씩 앞으로 나아가기"
            "♪" -> "차분하게 현실을 점검하고 안정화하기"
            "♫" -> "천천히 호흡하며 마음을 진정시키기"
            else -> "현재와 반대되는 건강한 행동 선택하기"
        }
    }
}