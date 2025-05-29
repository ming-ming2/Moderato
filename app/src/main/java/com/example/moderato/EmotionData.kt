package com.example.moderato

// 감정 타입 (음표 + 이름)
data class EmotionType(
    val symbol: String,    // 음표 기호 (♪, ♩, ♫ 등)
    val name: String       // 감정 이름 (기쁨, 슬픔 등)
)

// 감정 입력 데이터 (파일 저장용)
data class EmotionInputData(
    val emotion: EmotionType,     // 선택된 감정
    val intensity: Int,           // 강도 (1~5)
    val timeOfDay: String,        // 시간대 (morning, afternoon, evening, night)
    val tags: List<String>,       // 태그 목록
    val memo: String,             // 메모
    val date: String,             // 날짜 (yyyy-MM-dd)
    val time: String              // 시간 (HH:mm)
)

// 감정 기록 (화면 표시용)
data class EmotionRecord(
    val timeOfDay: String,        // 시간대
    val emotionSymbol: String,    // 감정 음표
    val emotionText: String,      // 감정 텍스트
    val date: String              // 날짜
)