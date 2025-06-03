package com.example.moderato

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class EmotionMiniStaffView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val staffPaint = Paint().apply {
        color = Color.parseColor("#40FFFFFF")
        strokeWidth = 2f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val notePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val textPaint = Paint().apply {
        color = Color.parseColor("#FFFFFF")
        textSize = 12f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    private var emotions = listOf<EmotionRecord>()

    fun setEmotions(emotions: List<EmotionRecord>) {
        this.emotions = emotions
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (width == 0 || height == 0) return

        val centerY = height / 2f
        val staffStartX = 40f
        val staffEndX = width - 40f
        val staffWidth = staffEndX - staffStartX

        // 미니 오선지 그리기 (3줄만)
        drawMiniStaff(canvas, staffStartX, staffEndX, centerY)

        // 고음 클레프 그리기
        drawTrebleClef(canvas, staffStartX - 30f, centerY)

        // 감정 음표들 그리기
        if (emotions.isNotEmpty()) {
            drawEmotionNotes(canvas, staffStartX, staffWidth, centerY)
        }
    }

    private fun drawMiniStaff(canvas: Canvas, startX: Float, endX: Float, centerY: Float) {
        val lineSpacing = 16f

        // 3줄의 오선지
        for (i in -1..1) {
            val y = centerY + (i * lineSpacing)
            canvas.drawLine(startX, y, endX, y, staffPaint)
        }
    }

    private fun drawTrebleClef(canvas: Canvas, x: Float, centerY: Float) {
        val trebleClefPaint = Paint().apply {
            color = Color.parseColor("#FF6B9D")
            textSize = 24f
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("𝄞", x, centerY + 8f, trebleClefPaint)
    }

    private fun drawEmotionNotes(canvas: Canvas, startX: Float, staffWidth: Float, centerY: Float) {
        val maxNotesToShow = 7 // 일주일치만 표시
        val visibleEmotions = emotions.take(maxNotesToShow)

        if (visibleEmotions.isEmpty()) return

        val noteSpacing = staffWidth / (visibleEmotions.size + 1)

        visibleEmotions.forEachIndexed { index, emotion ->
            val noteX = startX + noteSpacing * (index + 1)
            val noteY = getNoteY(centerY, emotion.emotionSymbol)

            // 감정별 색상 설정
            notePaint.color = getEmotionColor(emotion.emotionSymbol)

            // 음표 그리기 (간단한 원형)
            canvas.drawCircle(noteX, noteY, 8f, notePaint)

            // 감정 기호 그리기
            textPaint.color = Color.WHITE
            canvas.drawText(emotion.emotionSymbol, noteX, noteY + 4f, textPaint)

            // 요일 표시 (작게)
            val dayText = getDayAbbreviation(emotion.timeOfDay)
            textPaint.textSize = 10f
            textPaint.color = Color.parseColor("#B0B3B8")
            canvas.drawText(dayText, noteX, centerY + 40f, textPaint)
            textPaint.textSize = 12f // 원래 크기로 복원
        }
    }

    private fun getNoteY(centerY: Float, emotionSymbol: String): Float {
        val lineSpacing = 16f

        return when(emotionSymbol) {
            "♪" -> centerY - lineSpacing * 1.5f  // 높은 위치 (기쁨)
            "♫" -> centerY - lineSpacing * 0.5f  // 중간 높은 위치 (설렘)
            "♡" -> centerY                       // 중간 위치 (사랑)
            "♩" -> centerY + lineSpacing * 0.5f  // 중간 낮은 위치 (평온)
            "♭" -> centerY + lineSpacing * 1.0f  // 낮은 위치 (슬픔)
            "♯" -> centerY - lineSpacing * 1.0f  // 높은 위치 (화남)
            "𝄢" -> centerY + lineSpacing * 1.5f  // 가장 낮은 위치 (불안)
            else -> centerY
        }
    }

    private fun getEmotionColor(symbol: String): Int {
        return when(symbol) {
            "♪" -> Color.parseColor("#FFD700")  // 금색 (기쁨)
            "♩" -> Color.parseColor("#8B5CF6")  // 보라색 (평온)
            "♫" -> Color.parseColor("#FFB366")  // 주황색 (설렘)
            "♭" -> Color.parseColor("#6366F1")  // 파란색 (슬픔)
            "♯" -> Color.parseColor("#F43F5E")  // 빨간색 (화남)
            "𝄢" -> Color.parseColor("#6B7280")  // 회색 (불안)
            "♡" -> Color.parseColor("#F59E0B")  // 황금색 (사랑)
            else -> Color.parseColor("#FFFFFF")
        }
    }

    private fun getDayAbbreviation(timeOfDay: String): String {
        return when(timeOfDay) {
            "morning" -> "AM"
            "afternoon" -> "PM"
            "evening" -> "EV"
            "night" -> "NT"
            else -> "??"
        }
    }
}