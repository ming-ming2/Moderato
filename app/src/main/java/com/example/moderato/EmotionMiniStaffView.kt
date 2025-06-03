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
        color = Color.parseColor("#60FFFFFF")
        strokeWidth = 3f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val notePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val textPaint = Paint().apply {
        color = Color.parseColor("#FFFFFF")
        textSize = 16f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    private val trebleClefPaint = Paint().apply {
        color = Color.parseColor("#FF6B9D")
        textSize = 36f
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
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
        val staffStartX = 60f
        val staffEndX = width - 60f
        val staffWidth = staffEndX - staffStartX

        // 미니 오선지 그리기 (5줄로 확장)
        drawMiniStaff(canvas, staffStartX, staffEndX, centerY)

        // 고음 클레프 그리기 (더 크게)
        drawTrebleClef(canvas, staffStartX - 45f, centerY)

        // 감정 음표들 그리기
        if (emotions.isNotEmpty()) {
            drawEmotionNotes(canvas, staffStartX, staffWidth, centerY)
        }
    }

    private fun drawMiniStaff(canvas: Canvas, startX: Float, endX: Float, centerY: Float) {
        val lineSpacing = 20f

        // 5줄의 오선지로 확장
        for (i in -2..2) {
            val y = centerY + (i * lineSpacing)
            canvas.drawLine(startX, y, endX, y, staffPaint)
        }
    }

    private fun drawTrebleClef(canvas: Canvas, x: Float, centerY: Float) {
        canvas.drawText("𝄞", x, centerY + 12f, trebleClefPaint)
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

            // 음표 그리기 (더 크게)
            canvas.drawCircle(noteX, noteY, 12f, notePaint)

            // 감정 기호 그리기 (더 크게)
            textPaint.color = Color.WHITE
            textPaint.textSize = 16f
            canvas.drawText(emotion.emotionSymbol, noteX, noteY + 6f, textPaint)

            // 요일 표시 (크게)
            val dayText = getDayAbbreviation(emotion.timeOfDay)
            textPaint.textSize = 12f
            textPaint.color = Color.parseColor("#B0B3B8")
            canvas.drawText(dayText, noteX, centerY + 55f, textPaint)
        }
    }

    private fun getNoteY(centerY: Float, emotionSymbol: String): Float {
        val lineSpacing = 20f

        return when(emotionSymbol) {
            "♪" -> centerY - lineSpacing * 2.5f  // 매우 높은 위치 (기쁨)
            "♫" -> centerY - lineSpacing * 1.5f  // 높은 위치 (설렘)
            "♡" -> centerY - lineSpacing * 0.5f  // 중간 높은 위치 (사랑)
            "♩" -> centerY + lineSpacing * 0.5f  // 중간 낮은 위치 (평온)
            "♯" -> centerY - lineSpacing * 1.0f  // 중간 높은 위치 (화남)
            "♭" -> centerY + lineSpacing * 1.5f  // 낮은 위치 (슬픔)
            "𝄢" -> centerY + lineSpacing * 2.0f  // 가장 낮은 위치 (불안)
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