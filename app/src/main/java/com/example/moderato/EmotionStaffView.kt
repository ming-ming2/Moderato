package com.example.moderato

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.min

class EmotionStaffView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val staffPaint = Paint().apply {
        shader = LinearGradient(0f, 0f, 100f, 0f,
            Color.parseColor("#40FFFFFF"),
            Color.parseColor("#20FFFFFF"),
            Shader.TileMode.CLAMP)
        strokeWidth = 3f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val backgroundPaint = Paint().apply {
        shader = LinearGradient(0f, 0f, 0f, 200f,
            Color.parseColor("#2A2D3A"),
            Color.parseColor("#1A1B23"),
            Shader.TileMode.CLAMP)
        style = Paint.Style.FILL
    }

    private val trebleClefPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.primary_pink)
        textSize = 80f
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
        setShadowLayer(4f, 0f, 3f, Color.parseColor("#40000000"))
    }

    private val keySignaturePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.text_primary)
        textSize = 28f
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
    }

    private val tempoPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.text_secondary)
        textSize = 18f
        isAntiAlias = true
        typeface = Typeface.DEFAULT
    }

    private val glowPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        maskFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)
    }

    private var emotions = listOf<EmotionNote>()
    private var currentKey = "C Major"
    private var currentTempo = "Moderato"

    data class EmotionNote(
        val symbol: String,
        val pitch: Int,
        val time: String
    )

    fun setEmotions(emotions: List<EmotionNote>, key: String = "C Major", tempo: String = "Moderato") {
        this.emotions = emotions
        this.currentKey = key
        this.currentTempo = tempo
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 배경 그라데이션
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        val centerX = width / 2f
        val centerY = height / 2f
        val staffSpacing = 32f
        val staffWidth = min(width * 0.95f, 450f)
        val staffStartX = centerX - staffWidth / 2
        val staffEndX = centerX + staffWidth / 2

        // 고급스러운 오선지 그리기
        drawStaffLines(canvas, staffStartX, staffEndX, centerY, staffSpacing)

        // 높은음자리표 (더 예쁘게)
        drawTrebleClef(canvas, staffStartX, centerY)

        // 조성과 템포 표시
        drawKeyAndTempo(canvas, staffStartX, centerY)

        // 감정 음표들 그리기 (업그레이드)
        if (emotions.isNotEmpty()) {
            drawEmotionNotes(canvas, staffStartX, staffWidth, centerY, staffSpacing)
        }

        // 마디선 (더 멋지게)
        drawBarLine(canvas, staffEndX, centerY, staffSpacing)
    }

    private fun drawStaffLines(canvas: Canvas, startX: Float, endX: Float, centerY: Float, spacing: Float) {
        for (i in 0..4) {
            val y = centerY - 64f + (i * spacing)

            // 그라데이션 라인으로 더 고급스럽게
            val linePaint = Paint(staffPaint)
            canvas.drawLine(startX, y, endX, y, linePaint)

            // 미세한 글로우 효과
            val glowLine = Paint().apply {
                color = Color.parseColor("#30FFFFFF")
                strokeWidth = 6f
                style = Paint.Style.STROKE
                isAntiAlias = true
                maskFilter = BlurMaskFilter(3f, BlurMaskFilter.Blur.NORMAL)
            }
            canvas.drawLine(startX, y, endX, y, glowLine)
        }
    }

    private fun drawTrebleClef(canvas: Canvas, startX: Float, centerY: Float) {
        // 글로우 효과
        glowPaint.color = Color.parseColor("#60FF6B9D")
        canvas.drawText("𝄞", startX - 60f, centerY + 20f, glowPaint)

        // 메인 높은음자리표
        canvas.drawText("𝄞", startX - 60f, centerY + 20f, trebleClefPaint)
    }

    private fun drawKeyAndTempo(canvas: Canvas, startX: Float, centerY: Float) {
        canvas.drawText(currentKey, startX + 40f, centerY - 90f, keySignaturePaint)
        canvas.drawText("♩ = $currentTempo", startX + 40f, centerY - 60f, tempoPaint)
    }

    private fun drawEmotionNotes(canvas: Canvas, startX: Float, staffWidth: Float, centerY: Float, spacing: Float) {
        val noteSpacing = staffWidth / (emotions.size + 1)

        emotions.forEachIndexed { index, emotion ->
            val noteX = startX + noteSpacing * (index + 1)
            val noteY = getNoteY(centerY, emotion.pitch, spacing)

            // 글로우 효과 먼저
            drawNoteGlow(canvas, noteX, noteY, emotion.symbol)

            // 실제 음표
            drawBeautifulNote(canvas, noteX, noteY, emotion.symbol)

            // 시간 라벨 (더 예쁘게)
            drawTimeLabel(canvas, noteX, centerY, emotion.time)
        }
    }

    private fun drawNoteGlow(canvas: Canvas, x: Float, y: Float, symbol: String) {
        val glowColor = when (symbol) {
            "♪" -> Color.parseColor("#60FF6B9D") // 기쁨 - 핑크 글로우
            "♩" -> Color.parseColor("#608B5CF6") // 평온 - 퍼플 글로우
            "♫" -> Color.parseColor("#60FFB366") // 설렘 - 오렌지 글로우
            "♭" -> Color.parseColor("#606366F1") // 슬픔 - 블루 글로우
            "♯" -> Color.parseColor("#60F43F5E") // 화남 - 레드 글로우
            else -> Color.parseColor("#60FFFFFF")
        }

        glowPaint.color = glowColor
        canvas.drawCircle(x, y, 25f, glowPaint)
    }

    private fun drawBeautifulNote(canvas: Canvas, x: Float, y: Float, symbol: String) {
        val notePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            setShadowLayer(2f, 1f, 1f, Color.parseColor("#40000000"))
        }

        when (symbol) {
            "♪" -> {
                // 기쁨 - 황금색 온음표 (MEGA SIZE!)
                notePaint.shader = RadialGradient(x, y, 16f,
                    Color.parseColor("#FFD700"),
                    Color.parseColor("#FF6B9D"),
                    Shader.TileMode.CLAMP)
                canvas.drawCircle(x, y, 14f, notePaint)

                // 반짝이는 하이라이트
                val highlight = Paint().apply {
                    color = Color.parseColor("#90FFFFFF")
                    style = Paint.Style.FILL
                }
                canvas.drawCircle(x - 4f, y - 4f, 6f, highlight)
            }
            "♩" -> {
                // 평온 - 그라데이션 4분음표 (MEGA SIZE!)
                notePaint.shader = RadialGradient(x, y, 14f,
                    Color.parseColor("#8B5CF6"),
                    Color.parseColor("#6366F1"),
                    Shader.TileMode.CLAMP)
                canvas.drawCircle(x, y, 12f, notePaint)

                // 기둥 (더 굵고 길게)
                val stemPaint = Paint(notePaint).apply {
                    shader = null
                    strokeWidth = 4f
                    style = Paint.Style.STROKE
                }
                canvas.drawLine(x + 12f, y, x + 12f, y - 50f, stemPaint)
            }
            "♫" -> {
                // 설렘 - 연결된 8분음표 (MEGA SIZE!)
                notePaint.color = Color.parseColor("#FFB366")
                canvas.drawCircle(x - 8f, y, 10f, notePaint)
                canvas.drawCircle(x + 8f, y + 6f, 10f, notePaint)

                // 연결선 (더 굵게)
                val beamPaint = Paint(notePaint).apply { strokeWidth = 6f }
                canvas.drawLine(x - 3f, y - 10f, x + 13f, y - 4f, beamPaint)
            }
            "♭" -> {
                // 슬픔 - 차분한 블루 톤 (MEGA SIZE!)
                notePaint.shader = RadialGradient(x, y, 14f,
                    Color.parseColor("#6366F1"),
                    Color.parseColor("#3B82F6"),
                    Shader.TileMode.CLAMP)
                canvas.drawCircle(x, y, 12f, notePaint)
            }
            "♯" -> {
                // 화남 - 강렬한 레드 톤 (MEGA SIZE!)
                notePaint.shader = RadialGradient(x, y, 14f,
                    Color.parseColor("#F43F5E"),
                    Color.parseColor("#DC2626"),
                    Shader.TileMode.CLAMP)
                canvas.drawCircle(x, y, 12f, notePaint)
            }
            else -> {
                notePaint.color = Color.parseColor("#FFFFFF")
                canvas.drawCircle(x, y, 12f, notePaint)
            }
        }
    }

    private fun drawTimeLabel(canvas: Canvas, x: Float, centerY: Float, time: String) {
        val labelPaint = Paint().apply {
            color = Color.parseColor("#B0B3B8")
            textSize = 16f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        // 배경 원형 태그 (더 크게)
        val bgPaint = Paint().apply {
            color = Color.parseColor("#50FFFFFF")
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        canvas.drawCircle(x, centerY + 90f, 20f, bgPaint)
        canvas.drawText(time, x, centerY + 96f, labelPaint)
    }

    private fun drawBarLine(canvas: Canvas, endX: Float, centerY: Float, spacing: Float) {
        val barPaint = Paint().apply {
            shader = LinearGradient(0f, centerY - 64f, 0f, centerY + 64f,
                Color.parseColor("#80FFFFFF"),
                Color.parseColor("#30FFFFFF"),
                Shader.TileMode.CLAMP)
            strokeWidth = 4f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        canvas.drawLine(endX + 25f, centerY - 64f, endX + 25f, centerY + 64f, barPaint)
    }

    private fun getNoteY(centerY: Float, pitch: Int, spacing: Float): Float {
        return when (pitch) {
            0 -> centerY + 96f  // 낮은 도
            1 -> centerY + 80f  // 레
            2 -> centerY + 64f  // 미 (첫 번째 선)
            3 -> centerY + 48f  // 파
            4 -> centerY + 32f  // 솔 (두 번째 선)
            5 -> centerY + 16f  // 라
            6 -> centerY + 0f   // 시 (세 번째 선)
            7 -> centerY - 16f  // 높은 도
            8 -> centerY - 32f  // 높은 레
            else -> centerY + 32f
        }
    }
}