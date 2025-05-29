package com.example.moderato

import android.content.Context
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class EmotionFileManager(private val context: Context) {

    // 감정 데이터를 파일로 저장
    fun saveEmotionData(emotionData: EmotionInputData): Boolean {
        return try {
            val fileName = "${emotionData.date}_${emotionData.timeOfDay}.txt"
            val fileOutput: FileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)

            // 감정 데이터를 문자열로 변환
            val dataString = buildString {
                append("날짜: ${emotionData.date}\n")
                append("시간: ${emotionData.time}\n")
                append("시간대: ${getTimeOfDayKorean(emotionData.timeOfDay)}\n")
                append("감정: ${emotionData.emotion.symbol} ${emotionData.emotion.name}\n")
                append("강도: ${getIntensityText(emotionData.intensity)}\n")
                append("태그: ${emotionData.tags.joinToString(", ") { "#$it" }}\n")
                append("메모: ${emotionData.memo}\n")
                append("저장시각: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
            }

            fileOutput.write(dataString.toByteArray())
            fileOutput.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // 특정 날짜의 모든 감정 데이터 불러오기 (강도 정보 포함)
    fun loadEmotionsByDate(date: String): List<EmotionRecord> {
        val emotions = mutableListOf<EmotionRecord>()
        val timeSlots = arrayOf("morning", "afternoon", "evening", "night")

        for (timeSlot in timeSlots) {
            val fileName = "${date}_${timeSlot}.txt"
            val emotionData = readEmotionFile(fileName)
            if (emotionData != null) {
                emotions.add(EmotionRecord(
                    timeOfDay = timeSlot,
                    emotionSymbol = emotionData.emotion.symbol,
                    emotionText = "${emotionData.emotion.name} (${getIntensityText(emotionData.intensity)})",
                    date = date
                ))
            }
        }

        return emotions
    }

    // 파일에서 감정 데이터 읽기
    private fun readEmotionFile(fileName: String): EmotionInputData? {
        return try {
            val fileInput: FileInputStream = context.openFileInput(fileName)
            val content = fileInput.bufferedReader().use { it.readText() }
            fileInput.close()

            // 파일 내용을 파싱해서 EmotionInputData로 변환
            parseEmotionData(content)
        } catch (e: Exception) {
            null
        }
    }

    // 문자열 데이터를 EmotionInputData 객체로 파싱
    private fun parseEmotionData(content: String): EmotionInputData? {
        return try {
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

            // 감정 파싱
            val emotionText = dataMap["감정"] ?: return null
            val emotionParts = emotionText.split(" ")
            val emotionSymbol = emotionParts.getOrNull(0) ?: "♪"
            val emotionName = emotionParts.getOrNull(1) ?: "기쁨"

            // 태그 파싱
            val tagsText = dataMap["태그"] ?: ""
            val tags = if (tagsText.isNotEmpty()) {
                tagsText.split(", ").map { it.replace("#", "") }
            } else {
                emptyList()
            }

            // 강도 파싱
            val intensityText = dataMap["강도"] ?: "보통 (mf)"
            val intensity = when {
                intensityText.contains("pp") -> 1
                intensityText.contains("p") -> 2
                intensityText.contains("mf") -> 3
                intensityText.contains("f") -> 4
                intensityText.contains("ff") -> 5
                else -> 3
            }

            // 시간대 파싱
            val timeOfDayKorean = dataMap["시간대"] ?: "아침"
            val timeOfDay = when (timeOfDayKorean) {
                "아침" -> "morning"
                "오후" -> "afternoon"
                "저녁" -> "evening"
                "밤" -> "night"
                else -> "morning"
            }

            EmotionInputData(
                emotion = EmotionType(emotionSymbol, emotionName),
                intensity = intensity,
                timeOfDay = timeOfDay,
                tags = tags,
                memo = dataMap["메모"] ?: "",
                date = dataMap["날짜"] ?: "",
                time = dataMap["시간"] ?: ""
            )
        } catch (e: Exception) {
            null
        }
    }

    // 전체 저장된 파일 목록 가져오기
    fun getAllSavedDates(): List<String> {
        val dates = mutableSetOf<String>()
        try {
            val files = context.fileList()
            for (fileName in files) {
                if (fileName.contains("_") && fileName.endsWith(".txt")) {
                    val date = fileName.split("_")[0]
                    dates.add(date)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return dates.sorted().reversed() // 최신순 정렬
    }

    // 특정 날짜의 감정 데이터 존재 여부 확인
    fun hasEmotionData(date: String, timeOfDay: String): Boolean {
        val fileName = "${date}_${timeOfDay}.txt"
        return context.fileList().contains(fileName)
    }

    // 특정 파일 삭제
    fun deleteEmotionData(date: String, timeOfDay: String): Boolean {
        return try {
            val fileName = "${date}_${timeOfDay}.txt"
            context.deleteFile(fileName)
        } catch (e: Exception) {
            false
        }
    }

    // 헬퍼 함수들
    private fun getTimeOfDayKorean(timeOfDay: String): String {
        return when (timeOfDay) {
            "morning" -> "아침"
            "afternoon" -> "오후"
            "evening" -> "저녁"
            "night" -> "밤"
            else -> "기타"
        }
    }

    private fun getIntensityText(intensity: Int): String {
        return when (intensity) {
            1 -> "매우 약함 (pp)"
            2 -> "약함 (p)"
            3 -> "보통 (mf)"
            4 -> "강함 (f)"
            5 -> "매우 강함 (ff)"
            else -> "보통 (mf)"
        }
    }
}