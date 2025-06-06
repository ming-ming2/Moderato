package com.example.moderato

import kotlin.math.sqrt

class EmotionPatternAnalyzer {

    enum class EmotionalPattern {
        STABLE,
        FLUCTUATING,
        CHAOTIC
    }

    enum class EmotionalPolarity {
        POSITIVE_DOMINANT,
        NEGATIVE_DOMINANT,
        MIXED,
        NEUTRAL
    }

    enum class IntensityLevel {
        OVERWHELMING,
        HIGH,
        MODERATE,
        LOW
    }

    data class EmotionAnalysis(
        val pattern: EmotionalPattern,
        val polarity: EmotionalPolarity,
        val intensity: IntensityLevel,
        val dominantEmotion: String,
        val variabilityScore: Float,
        val totalEmotions: Int
    )

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
        val variability = calculateEmotionVariability(emotions)

        return EmotionAnalysis(pattern, polarity, intensity, dominantEmotion, variability, emotions.size)
    }

    private fun analyzePattern(emotions: List<EmotionRecord>): EmotionalPattern {
        if (emotions.size < 2) return EmotionalPattern.STABLE

        val emotionTypes = emotions.map { it.emotionSymbol }.distinct()
        val emotionDiversity = emotionTypes.size.toFloat() / emotions.size
        val polarityChanges = calculatePolarityChanges(emotions)
        val intensityVariability = calculateIntensityVariability(emotions)
        val timeDistribution = calculateTimeDistribution(emotions)

        return when {
            emotionDiversity > 0.6 && polarityChanges >= 3 && intensityVariability > 2.0 -> EmotionalPattern.CHAOTIC
            polarityChanges >= 2 || intensityVariability > 1.5 -> EmotionalPattern.FLUCTUATING
            emotionDiversity > 0.4 && polarityChanges <= 1 -> EmotionalPattern.FLUCTUATING
            else -> EmotionalPattern.STABLE
        }
    }

    private fun calculatePolarityChanges(emotions: List<EmotionRecord>): Int {
        if (emotions.size < 2) return 0

        var changes = 0
        var previousPolarity = getEmotionPolarity(emotions[0].emotionSymbol)

        for (i in 1 until emotions.size) {
            val currentPolarity = getEmotionPolarity(emotions[i].emotionSymbol)
            if (currentPolarity != previousPolarity && currentPolarity != 0 && previousPolarity != 0) {
                changes++
            }
            previousPolarity = currentPolarity
        }

        return changes
    }

    private fun getEmotionPolarity(emotionSymbol: String): Int {
        return when(emotionSymbol) {
            "♪", "♫", "♡" -> 1
            "♩" -> 0
            "♭", "♯", "𝄢" -> -1
            else -> 0
        }
    }

    private fun calculateIntensityVariability(emotions: List<EmotionRecord>): Float {
        if (emotions.size < 2) return 0f

        val intensities = emotions.map { getEmotionIntensity(it.emotionSymbol).toFloat() }
        val mean = intensities.average().toFloat()
        val variance = intensities.map { (it - mean) * (it - mean) }.average().toFloat()
        return sqrt(variance)
    }

    private fun calculateTimeDistribution(emotions: List<EmotionRecord>): Float {
        val timeSlots = emotions.map { it.timeOfDay }.distinct().size
        return timeSlots.toFloat() / 4.0f
    }

    private fun analyzePolarity(emotions: List<EmotionRecord>): EmotionalPolarity {
        val positiveEmotions = emotions.count { it.emotionSymbol in listOf("♪", "♫", "♡") }
        val neutralEmotions = emotions.count { it.emotionSymbol == "♩" }
        val negativeEmotions = emotions.count { it.emotionSymbol in listOf("♭", "♯", "𝄢") }
        val total = emotions.size

        val positiveRatio = positiveEmotions.toFloat() / total
        val negativeRatio = negativeEmotions.toFloat() / total

        return when {
            positiveRatio >= 0.6 -> EmotionalPolarity.POSITIVE_DOMINANT
            negativeRatio >= 0.6 -> EmotionalPolarity.NEGATIVE_DOMINANT
            positiveRatio >= 0.2 && negativeRatio >= 0.2 -> EmotionalPolarity.MIXED
            else -> EmotionalPolarity.NEUTRAL
        }
    }

    private fun analyzeIntensity(emotions: List<EmotionRecord>): IntensityLevel {
        val averageIntensity = emotions.map { getEmotionIntensity(it.emotionSymbol) }.average().toFloat()

        return when {
            averageIntensity >= 4.5 -> IntensityLevel.OVERWHELMING
            averageIntensity >= 3.5 -> IntensityLevel.HIGH
            averageIntensity >= 2.5 -> IntensityLevel.MODERATE
            else -> IntensityLevel.LOW
        }
    }

    private fun findDominantEmotion(emotions: List<EmotionRecord>): String {
        val emotionCounts = emotions.groupBy { it.emotionSymbol }
            .mapValues { it.value.size }

        return emotionCounts.maxByOrNull { it.value }?.key ?: "없음"
    }

    private fun calculateEmotionVariability(emotions: List<EmotionRecord>): Float {
        if (emotions.size < 2) return 0f

        val emotionTypes = emotions.map { it.emotionSymbol }.distinct().size
        val typeVariability = emotionTypes.toFloat() / 7.0f
        val polarityChanges = calculatePolarityChanges(emotions)
        val polarityVariability = polarityChanges.toFloat() / (emotions.size - 1)
        val intensityVariability = calculateIntensityVariability(emotions)

        return ((typeVariability * 2) + (polarityVariability * 2) + (intensityVariability / 2)).coerceIn(0f, 5f)
    }

    private fun getEmotionIntensity(emotionSymbol: String): Int {
        return when(emotionSymbol) {
            "♪" -> 4
            "♫" -> 4
            "♡" -> 3
            "♩" -> 2
            "♭" -> 3
            "♯" -> 5
            "𝄢" -> 4
            else -> 3
        }
    }

    fun getEmotionNameFromSymbol(symbol: String): String {
        return when(symbol) {
            "♪" -> "기쁨"
            "♩" -> "평온"
            "♫" -> "설렘"
            "♭" -> "슬픔"
            "♯" -> "화남"
            "𝄢" -> "불안"
            "♡" -> "사랑"
            else -> "알 수 없음"
        }
    }
}