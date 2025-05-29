package com.example.moderato

import java.text.SimpleDateFormat
import java.util.*

/**
 * 감정 데이터를 분석하여 오늘의 감정 코드를 생성하는 클래스
 * 수업 3주차 Kotlin 문법 (when문, 조건문, 메소드) 활용
 */
class EmotionChordAnalyzer {

    // 감정 코드 결과 데이터 클래스
    data class EmotionChord(
        val chordName: String,        // 코드명 (예: "Cmaj7")
        val chordSymbol: String,      // 코드 기호 (예: "♪7")
        val chordFullName: String,    // 전체 이름 (예: "C Major 7th")
        val message: String,          // 감성 메시지
        val emotionCount: Int,        // 오늘 기록된 감정 개수
        val dominantEmotion: String,  // 주된 감정
        val chordColor: String,       // 코드 대표 색상
        val intensity: String         // 감정 강도 (pp, p, mf, f, ff)
    )

    // 단일 감정별 기본 코드 매핑
    private val basicEmotionChords = mapOf(
        "♪" to ChordInfo("C", "Major", "밝고 경쾌한", "#FFD700", "기쁨"),
        "♩" to ChordInfo("Am", "minor 7th", "차분하고 안정적인", "#8B5CF6", "평온"),
        "♫" to ChordInfo("G", "Major 7th", "두근거리고 설레는", "#FFB366", "설렘"),
        "♭" to ChordInfo("Dm", "minor", "애절하고 깊은", "#6366F1", "슬픔"),
        "♯" to ChordInfo("E", "7th", "날카롭고 긴장감 있는", "#F43F5E", "화남"),
        "𝄢" to ChordInfo("Bm", "minor", "불안정하고 어두운", "#6B7280", "불안"),
        "♡" to ChordInfo("F", "Major 7th", "따뜻하고 포근한", "#F59E0B", "사랑")
    )

    // 코드 정보 데이터 클래스
    private data class ChordInfo(
        val root: String,       // 근음 (C, Am, G 등)
        val quality: String,    // 코드 성질 (Major, minor, 7th 등)
        val feeling: String,    // 감정 느낌
        val color: String,      // 대표 색상
        val emotionName: String // 감정 이름
    )

    /**
     * 메인 분석 메소드 - 오늘의 감정들을 분석하여 코드 생성
     * 수업 3주차: 메소드, 조건문, 배열 처리 활용
     */
    fun analyzeEmotions(emotions: List<EmotionRecord>): EmotionChord {
        // 감정이 없는 경우
        if (emotions.isEmpty()) {
            return createEmptyChord()
        }

        // 1. 감정 통계 분석
        val emotionStats = analyzeEmotionStatistics(emotions)

        // 2. 주된 감정 파악
        val dominantEmotion = findDominantEmotion(emotionStats)

        // 3. 감정 조합 패턴 분석
        val emotionPattern = analyzeEmotionPattern(emotionStats)

        // 4. 평균 강도 계산
        val averageIntensity = calculateAverageIntensity(emotions)

        // 5. 코드 결정
        val chordInfo = determineChord(dominantEmotion, emotionPattern, averageIntensity)

        // 6. 감성 메시지 생성
        val message = generateMessage(chordInfo, emotionStats, emotions.size)

        return EmotionChord(
            chordName = chordInfo.chordName,
            chordSymbol = chordInfo.chordSymbol,
            chordFullName = chordInfo.chordFullName,
            message = message,
            emotionCount = emotions.size,
            dominantEmotion = dominantEmotion,
            chordColor = chordInfo.color,
            intensity = getIntensityText(averageIntensity)
        )
    }

    /**
     * 감정 통계 분석
     * 수업 3주차: 배열, for문, Map 활용
     */
    private fun analyzeEmotionStatistics(emotions: List<EmotionRecord>): Map<String, Int> {
        val stats = mutableMapOf<String, Int>()

        for (emotion in emotions) {
            val symbol = emotion.emotionSymbol
            stats[symbol] = stats.getOrDefault(symbol, 0) + 1
        }

        return stats
    }

    /**
     * 주된 감정 찾기
     * 수업 3주차: 조건문, 반복문 활용
     */
    private fun findDominantEmotion(emotionStats: Map<String, Int>): String {
        var maxCount = 0
        var dominantEmotion = "♪" // 기본값은 기쁨

        for ((emotion, count) in emotionStats) {
            if (count > maxCount) {
                maxCount = count
                dominantEmotion = emotion
            }
        }

        return dominantEmotion
    }

    /**
     * 감정 조합 패턴 분석
     * 수업 3주차: when문, 조건문 활용
     */
    private fun analyzeEmotionPattern(emotionStats: Map<String, Int>): String {
        val emotionTypes = emotionStats.keys.toList()
        val emotionCount = emotionTypes.size

        return when {
            emotionCount == 1 -> "단일감정"
            emotionCount == 2 -> "이중감정_${emotionTypes.sorted().joinToString("_")}"
            emotionCount <= 3 -> "복합감정_${emotionCount}개"
            else -> "다양한감정_${emotionCount}개"
        }
    }

    /**
     * 평균 강도 계산 (1-5 범위)
     * 수업 3주차: 배열 처리, 계산 활용
     */
    private fun calculateAverageIntensity(emotions: List<EmotionRecord>): Int {
        if (emotions.isEmpty()) return 3

        // 실제 앱에서는 파일에서 강도 정보를 읽어와야 함
        // 여기서는 감정별 기본 강도로 계산
        var totalIntensity = 0
        for (emotion in emotions) {
            totalIntensity += getDefaultIntensity(emotion.emotionSymbol)
        }

        return (totalIntensity / emotions.size).coerceIn(1, 5)
    }

    /**
     * 감정별 기본 강도 (실제로는 파일에서 읽어와야 함)
     */
    private fun getDefaultIntensity(emotionSymbol: String): Int {
        return when(emotionSymbol) {
            "♪", "♫" -> 4  // 기쁨, 설렘은 강함
            "♩", "♡" -> 3  // 평온, 사랑은 보통
            "♭", "𝄢" -> 2  // 슬픔, 불안은 약함
            "♯" -> 5       // 화남은 매우 강함
            else -> 3
        }
    }

    /**
     * 코드 결정 메인 로직
     * 수업 3주차: when문, 조건문의 복합 활용
     */
    private fun determineChord(dominantEmotion: String, pattern: String, intensity: Int): ChordResult {
        // 1. 단일 감정 처리
        if (pattern == "단일감정") {
            return createSingleEmotionChord(dominantEmotion, intensity)
        }

        // 2. 특별한 조합 패턴 처리
        val specialChord = checkSpecialCombinations(pattern)
        if (specialChord != null) {
            return specialChord
        }

        // 3. 일반적인 복합 감정 처리
        return createComplexEmotionChord(dominantEmotion, pattern, intensity)
    }

    /**
     * 단일 감정 코드 생성
     */
    private fun createSingleEmotionChord(emotion: String, intensity: Int): ChordResult {
        val baseChord = basicEmotionChords[emotion] ?: basicEmotionChords["♪"]!!

        // 강도에 따른 코드 변형
        val finalChord = when(intensity) {
            1, 2 -> "${baseChord.root}${if (baseChord.quality.contains("minor")) "m" else ""}" // 심플한 코드
            3 -> "${baseChord.root}${if (baseChord.quality.contains("minor")) "m7" else "maj7"}" // 7th 추가
            4, 5 -> "${baseChord.root}${if (baseChord.quality.contains("minor")) "m9" else "maj9"}" // 9th 추가
            else -> baseChord.root
        }

        return ChordResult(
            chordName = finalChord,
            chordSymbol = "$emotion${intensity}",
            chordFullName = "$finalChord (${baseChord.emotionName})",
            color = baseChord.color,
            feeling = baseChord.feeling
        )
    }

    /**
     * 특별한 감정 조합 패턴 체크
     * 수업 3주차: 문자열 처리, when문 활용
     */
    private fun checkSpecialCombinations(pattern: String): ChordResult? {
        return when {
            // 기쁨 + 평온 조합
            pattern.contains("♪") && pattern.contains("♩") ->
                ChordResult("Cmaj7", "♪♩", "C Major 7th (밝고 안정적인)", "#FFE4B5", "따뜻하고 균형잡힌")

            // 슬픔 + 희망(기쁨) 조합
            pattern.contains("♭") && pattern.contains("♪") ->
                ChordResult("Am(add9)", "♭♪", "A minor add 9 (애틋하지만 희망적인)", "#9370DB", "애틋하면서도 희망적인")

            // 화남 + 슬픔 조합
            pattern.contains("♯") && pattern.contains("♭") ->
                ChordResult("Dm7b5", "♯♭", "D minor 7 flat 5 (복잡하고 어두운)", "#8B0000", "복잡하고 무거운")

            // 설렘 + 불안 조합
            pattern.contains("♫") && pattern.contains("𝄢") ->
                ChordResult("Gsus4", "♫𝄢", "G suspended 4 (해결되지 않은 긴장감)", "#FF6347", "설레지만 불안한")

            // 기쁨 + 사랑 조합
            pattern.contains("♪") && pattern.contains("♡") ->
                ChordResult("C6/9", "♪♡", "C 6 add 9 (행복하고 따뜻한)", "#FFB6C1", "행복하고 사랑스러운")

            else -> null
        }
    }

    /**
     * 복합 감정 코드 생성
     */
    private fun createComplexEmotionChord(dominantEmotion: String, pattern: String, intensity: Int): ChordResult {
        val baseChord = basicEmotionChords[dominantEmotion] ?: basicEmotionChords["♪"]!!

        // 복잡한 감정일수록 복잡한 코드
        val complexChord = when {
            pattern.contains("3개") -> "${baseChord.root}maj7#11" // 확장 코드
            pattern.contains("4개") || pattern.contains("다양한") -> "${baseChord.root}13" // 매우 복잡한 코드
            else -> "${baseChord.root}7" // 기본 7th
        }

        return ChordResult(
            chordName = complexChord,
            chordSymbol = "$dominantEmotion+",
            chordFullName = "$complexChord (복합적인 ${baseChord.emotionName})",
            color = baseChord.color,
            feeling = "복합적이고 풍부한"
        )
    }

    /**
     * 감성 메시지 생성
     * 수업 3주차: 문자열 처리, when문 활용
     */
    private fun generateMessage(chordInfo: ChordResult, emotionStats: Map<String, Int>, totalCount: Int): String {
        val timeOfDay = getCurrentTimeOfDay()
        val emotionVariety = emotionStats.size

        val baseMessage = when {
            totalCount == 1 -> "오늘은 ${chordInfo.feeling} 단선율 같은 하루였어요 🎵"
            emotionVariety <= 2 -> "오늘은 ${chordInfo.feeling} 화음이 울려퍼진 하루였네요 🎼"
            emotionVariety >= 3 -> "오늘은 ${chordInfo.feeling} 교향곡 같은 풍성한 하루였어요 🎻"
            else -> "오늘은 ${chordInfo.feeling} 선율이 흘러간 하루였네요 ♪"
        }

        val timeMessage = when(timeOfDay) {
            "morning" -> "좋은 아침의 여운이 남아있어요 🌅"
            "afternoon" -> "한낮의 따스함이 느껴져요 ☀️"
            "evening" -> "저녁노을 같은 감성이에요 🌅"
            "night" -> "밤하늘의 별처럼 깊은 여운이 있어요 🌙"
            else -> "특별한 순간들이 기억에 남을 것 같아요 ✨"
        }

        return "$baseMessage $timeMessage"
    }

    /**
     * 현재 시간대 확인
     */
    private fun getCurrentTimeOfDay(): String {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (currentHour) {
            in 5..11 -> "morning"
            in 12..17 -> "afternoon"
            in 18..21 -> "evening"
            else -> "night"
        }
    }

    /**
     * 강도 텍스트 변환
     */
    private fun getIntensityText(intensity: Int): String {
        return when(intensity) {
            1 -> "pp (매우 여리게)"
            2 -> "p (여리게)"
            3 -> "mf (보통으로)"
            4 -> "f (세게)"
            5 -> "ff (매우 세게)"
            else -> "mf (보통으로)"
        }
    }

    /**
     * 감정이 없을 때 기본 코드
     */
    private fun createEmptyChord(): EmotionChord {
        return EmotionChord(
            chordName = "Rest",
            chordSymbol = "𝄽",
            chordFullName = "쉼표 (Rest)",
            message = "아직 오늘의 첫 음표를 기다리고 있어요. 어떤 감정으로 하루를 시작해볼까요? 🎵",
            emotionCount = 0,
            dominantEmotion = "없음",
            chordColor = "#B0B3B8",
            intensity = "silence"
        )
    }

    /**
     * 코드 결과 데이터 클래스
     */
    private data class ChordResult(
        val chordName: String,
        val chordSymbol: String,
        val chordFullName: String,
        val color: String,
        val feeling: String
    )
}