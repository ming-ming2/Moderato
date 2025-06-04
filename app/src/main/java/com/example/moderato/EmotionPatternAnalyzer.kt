package com.example.moderato

import kotlin.math.sqrt

/**
 * 감정 패턴 분석기 - DBT 기반 조율을 위한 감정 상태 분석
 * 수업 3주차 - Kotlin 문법 (when문, 조건문, 계산) 활용
 */
class EmotionPatternAnalyzer {

    // 수업 3주차 - enum 클래스 활용
    enum class EmotionalPattern {
        STABLE,           // 안정적 (변동 < 1.5)
        FLUCTUATING,      // 변동적 (변동 1.5-3.0)
        CHAOTIC          // 혼란적 (변동 > 3.0)
    }

    enum class EmotionalPolarity {
        POSITIVE_DOMINANT,    // 긍정 우세 (♪♫♡♩ > 60%)
        NEGATIVE_DOMINANT,    // 부정 우세 (♭♯𝄢 > 60%)
        MIXED,               // 혼재 (40-60%)
        NEUTRAL              // 중립 (평온만 있거나 없음)
    }

    enum class IntensityLevel {
        OVERWHELMING,     // 평균 강도 > 4
        HIGH,            // 평균 강도 3.5-4
        MODERATE,        // 평균 강도 2.5-3.5
        LOW              // 평균 강도 < 2.5
    }

    // 수업 3주차 - 데이터 클래스 활용
    data class EmotionAnalysis(
        val pattern: EmotionalPattern,
        val polarity: EmotionalPolarity,
        val intensity: IntensityLevel,
        val dominantEmotion: String,
        val variabilityScore: Float,
        val totalEmotions: Int
    )

    /**
     * 오늘의 감정들을 종합 분석
     * 수업 3주차 - 메소드와 계산 활용
     */
    fun analyzeEmotions(emotions: List<EmotionRecord>): EmotionAnalysis {
        if (emotions.isEmpty()) {
            return EmotionAnalysis(
                EmotionalPattern.STABLE,
                EmotionalPolarity.NEUTRAL,
                IntensityLevel.LOW,
                "없음",
                0f,
                0
            )
        }

        val pattern = analyzePattern(emotions)
        val polarity = analyzePolarity(emotions)
        val intensity = analyzeIntensity(emotions)
        val dominantEmotion = findDominantEmotion(emotions)
        val variability = calculateVariability(emotions)

        return EmotionAnalysis(pattern, polarity, intensity, dominantEmotion, variability, emotions.size)
    }

    /**
     * 감정 변동성 분석 - 수업 3주차 계산 활용
     */
    private fun analyzePattern(emotions: List<EmotionRecord>): EmotionalPattern {
        if (emotions.size < 2) return EmotionalPattern.STABLE

        val variability = calculateVariability(emotions)
        val timeGaps = calculateTimeGaps(emotions)

        return when {
            variability > 3.0 || timeGaps.any { it < 3 } -> EmotionalPattern.CHAOTIC
            variability > 1.5 -> EmotionalPattern.FLUCTUATING
            else -> EmotionalPattern.STABLE
        }
    }

    /**
     * 감정 극성 분석 - 수업 3주차 when문 활용
     */
    private fun analyzePolarity(emotions: List<EmotionRecord>): EmotionalPolarity {
        val positiveEmotions = emotions.count { it.emotionSymbol in listOf("♪", "♫", "♡", "♩") }
        val negativeEmotions = emotions.count { it.emotionSymbol in listOf("♭", "♯", "𝄢") }
        val total = emotions.size

        val positiveRatio = positiveEmotions.toFloat() / total
        val negativeRatio = negativeEmotions.toFloat() / total

        return when {
            positiveRatio > 0.6 -> EmotionalPolarity.POSITIVE_DOMINANT
            negativeRatio > 0.6 -> EmotionalPolarity.NEGATIVE_DOMINANT
            positiveRatio > 0.1 && negativeRatio > 0.1 -> EmotionalPolarity.MIXED
            else -> EmotionalPolarity.NEUTRAL
        }
    }

    /**
     * 감정 강도 분석 - 임시로 감정별 기본 강도 사용
     * 실제로는 파일에서 강도 정보를 읽어와야 함 (수업 9주차)
     */
    private fun analyzeIntensity(emotions: List<EmotionRecord>): IntensityLevel {
        val averageIntensity = emotions.map { getEmotionIntensity(it.emotionSymbol) }.average().toFloat()

        return when {
            averageIntensity > 4.0 -> IntensityLevel.OVERWHELMING
            averageIntensity > 3.5 -> IntensityLevel.HIGH
            averageIntensity > 2.5 -> IntensityLevel.MODERATE
            else -> IntensityLevel.LOW
        }
    }

    /**
     * 주된 감정 찾기 - 수업 3주차 배열 처리
     */
    private fun findDominantEmotion(emotions: List<EmotionRecord>): String {
        val emotionCounts = emotions.groupBy { it.emotionSymbol }
            .mapValues { it.value.size }

        return emotionCounts.maxByOrNull { it.value }?.key ?: "없음"
    }

    /**
     * 감정 변동성 계산 - 수업 3주차 계산
     */
    private fun calculateVariability(emotions: List<EmotionRecord>): Float {
        if (emotions.size < 2) return 0f

        val intensities = emotions.map { getEmotionIntensity(it.emotionSymbol).toFloat() }
        val mean = intensities.average().toFloat()
        val variance = intensities.map { (it - mean) * (it - mean) }.average().toFloat()

        return sqrt(variance)
    }

    /**
     * 시간 간격 계산 - 감정 기록 사이의 시간 차이
     */
    private fun calculateTimeGaps(emotions: List<EmotionRecord>): List<Int> {
        if (emotions.size < 2) return emptyList()

        val timeOrder = mapOf("morning" to 1, "afternoon" to 2, "evening" to 3, "night" to 4)
        val sortedEmotions = emotions.sortedBy { timeOrder[it.timeOfDay] ?: 0 }

        val gaps = mutableListOf<Int>()
        for (i in 1 until sortedEmotions.size) {
            val prevTime = timeOrder[sortedEmotions[i-1].timeOfDay] ?: 0
            val currTime = timeOrder[sortedEmotions[i].timeOfDay] ?: 0
            gaps.add(currTime - prevTime)
        }

        return gaps
    }

    /**
     * 감정별 기본 강도 - 추후 파일에서 실제 강도 읽어오도록 개선 필요
     */
    private fun getEmotionIntensity(emotionSymbol: String): Int {
        return when(emotionSymbol) {
            "♪", "♫" -> 4  // 기쁨, 설렘은 강함
            "♩", "♡" -> 3  // 평온, 사랑은 보통
            "♭", "𝄢" -> 2  // 슬픔, 불안은 약함
            "♯" -> 5       // 화남은 매우 강함
            else -> 3
        }
    }

    /**
     * 감정명 변환 헬퍼 메소드
     */
    fun getEmotionNameFromSymbol(symbol: String): String {
        return when(symbol) {
            "♪" -> "기쁨"
            "♩" -> "평온"
            "♫" -> "설렘"
            "♭" -> "슬픔"
            "♯" -> "화님"
            "𝄢" -> "불안"
            "♡" -> "사랑"
            else -> "알 수 없음"
        }
    }
}