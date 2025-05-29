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
            "♪" -> Color.parseColor("#80FFD700") // 기쁨 - 황금 글로우
            "♩" -> Color.parseColor("#608B5CF6") // 평온 - 보라 글로우
            "♫" -> Color.parseColor("#80FFB366") // 설렘 - 오렌지 글로우 (더 밝게)
            "♭" -> Color.parseColor("#606366F1") // 슬픔 - 블루 글로우
            "♯" -> Color.parseColor("#80F43F5E") // 화남 - 레드 글로우 (더 밝게)
            "𝄢" -> Color.parseColor("#606B7280") // 불안 - 회색 글로우
            "♡" -> Color.parseColor("#80F59E0B") // 사랑 - 따뜻한 주황 글로우
            else -> Color.parseColor("#60FFFFFF")
        }

        glowPaint.color = glowColor

        // 감정에 따라 글로우 크기도 다르게
        val glowRadius = when (symbol) {
            "♪" -> 30f  // 기쁨 - 큰 글로우 (활기차다)
            "♩" -> 25f  // 평온 - 중간 글로우 (안정감)
            "♫" -> 35f  // 설렘 - 가장 큰 글로우 (두근거림)
            "♭" -> 20f  // 슬픔 - 작은 글로우 (침울함)
            "♯" -> 28f  // 화남 - 큰 글로우 (강렬함)
            "𝄢" -> 18f  // 불안 - 가장 작은 글로우 (위축됨)
            "♡" -> 32f  // 사랑 - 큰 글로우 (따뜻함)
            else -> 25f
        }

        canvas.drawCircle(x, y, glowRadius, glowPaint)
    }

    private fun drawBeautifulNote(canvas: Canvas, x: Float, y: Float, symbol: String) {
        val notePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            setShadowLayer(3f, 1f, 2f, Color.parseColor("#40000000"))
        }

        when (symbol) {
            "♪" -> {
                // 기쁨 - 황금빛 8분음표 (활기차고 밝게)
                notePaint.shader = RadialGradient(x, y, 18f,
                    Color.parseColor("#FFD700"),
                    Color.parseColor("#FF6B9D"),
                    Shader.TileMode.CLAMP)
                canvas.drawCircle(x, y, 16f, notePaint)

                // 음표 기둥 (위로 향하는 활기찬 느낌)
                val stemPaint = Paint(notePaint).apply {
                    shader = LinearGradient(x, y, x, y - 60f,
                        Color.parseColor("#FF6B9D"),
                        Color.parseColor("#FFD700"),
                        Shader.TileMode.CLAMP)
                    strokeWidth = 5f
                    style = Paint.Style.STROKE
                }
                canvas.drawLine(x + 16f, y, x + 16f, y - 60f, stemPaint)

                // 8분음표 꼬리 (경쾌한 곡선)
                val path = Path().apply {
                    moveTo(x + 16f, y - 50f)
                    quadTo(x + 35f, y - 40f, x + 30f, y - 20f)
                }
                canvas.drawPath(path, stemPaint)

                // 반짝이는 하이라이트
                val highlight = Paint().apply {
                    color = Color.parseColor("#90FFFFFF")
                    style = Paint.Style.FILL
                }
                canvas.drawCircle(x - 6f, y - 6f, 8f, highlight)
            }
            "♩" -> {
                // 평온 - 부드러운 4분음표 (안정적이고 차분하게)
                notePaint.shader = RadialGradient(x, y, 16f,
                    Color.parseColor("#8B5CF6"),
                    Color.parseColor("#6366F1"),
                    Shader.TileMode.CLAMP)
                canvas.drawCircle(x, y, 14f, notePaint)

                // 차분한 기둥
                val stemPaint = Paint(notePaint).apply {
                    shader = null
                    color = Color.parseColor("#6366F1")
                    strokeWidth = 4f
                    style = Paint.Style.STROKE
                }
                canvas.drawLine(x + 14f, y, x + 14f, y - 45f, stemPaint)
            }
            "♫" -> {
                // 설렘 - 연결된 16분음표 (두근거리는 느낌)
                notePaint.color = Color.parseColor("#FFB366")

                // 두 개의 음표 머리 (두근두근)
                canvas.drawCircle(x - 10f, y - 5f, 12f, notePaint)
                canvas.drawCircle(x + 10f, y + 5f, 12f, notePaint)

                // 연결 기둥들
                val stemPaint = Paint(notePaint).apply {
                    strokeWidth = 4f
                    style = Paint.Style.STROKE
                }
                canvas.drawLine(x - 10f + 12f, y - 5f, x - 10f + 12f, y - 50f, stemPaint)
                canvas.drawLine(x + 10f + 12f, y + 5f, x + 10f + 12f, y - 40f, stemPaint)

                // 두근거리는 연결선 (2개)
                canvas.drawLine(x - 10f + 12f, y - 50f, x + 10f + 12f, y - 40f, stemPaint)
                canvas.drawLine(x - 10f + 12f, y - 45f, x + 10f + 12f, y - 35f, stemPaint)
            }
            "♭" -> {
                // 슬픔 - 플랫 기호와 함께 낮은 음표 (애절하게)
                notePaint.shader = RadialGradient(x, y, 16f,
                    Color.parseColor("#6366F1"),
                    Color.parseColor("#1E40AF"),
                    Shader.TileMode.CLAMP)
                canvas.drawCircle(x, y, 14f, notePaint)

                // 플랫 기호 그리기
                val flatPaint = Paint().apply {
                    color = Color.parseColor("#1E40AF")
                    strokeWidth = 3f
                    style = Paint.Style.STROKE
                    isAntiAlias = true
                }
                canvas.drawLine(x - 25f, y - 20f, x - 25f, y + 10f, flatPaint)

                val flatPath = Path().apply {
                    moveTo(x - 25f, y - 5f)
                    quadTo(x - 15f, y - 15f, x - 18f, y)
                    quadTo(x - 15f, y + 5f, x - 25f, y)
                }
                canvas.drawPath(flatPath, flatPaint)
            }
            "♯" -> {
                // 화남 - 샵 기호와 강렬한 음표 (날카롭게)
                notePaint.shader = RadialGradient(x, y, 16f,
                    Color.parseColor("#F43F5E"),
                    Color.parseColor("#DC2626"),
                    Shader.TileMode.CLAMP)
                canvas.drawCircle(x, y, 14f, notePaint)

                // 샵 기호 그리기 (날카롭게)
                val sharpPaint = Paint().apply {
                    color = Color.parseColor("#DC2626")
                    strokeWidth = 4f
                    style = Paint.Style.STROKE
                    isAntiAlias = true
                }
                // 세로선 2개
                canvas.drawLine(x - 30f, y - 15f, x - 30f, y + 15f, sharpPaint)
                canvas.drawLine(x - 22f, y - 15f, x - 22f, y + 15f, sharpPaint)
                // 가로선 2개 (약간 기울어진)
                canvas.drawLine(x - 35f, y - 8f, x - 17f, y - 5f, sharpPaint)
                canvas.drawLine(x - 35f, y + 5f, x - 17f, y + 8f, sharpPaint)
            }
            "𝄢" -> {
                // 불안 - 낮은음자리표와 함께 (불안정하게)
                notePaint.shader = RadialGradient(x, y, 16f,
                    Color.parseColor("#6B7280"),
                    Color.parseColor("#374151"),
                    Shader.TileMode.CLAMP)
                canvas.drawCircle(x, y, 14f, notePaint)

                // 낮은음자리표 기호
                val bassPaint = Paint().apply {
                    color = Color.parseColor("#374151")
                    textSize = 24f
                    isAntiAlias = true
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("𝄢", x - 25f, y + 8f, bassPaint)
            }
            "♡" -> {
                // 사랑 - 하트 모양 음표 (따뜻하게)
                notePaint.shader = RadialGradient(x, y, 16f,
                    Color.parseColor("#F59E0B"),
                    Color.parseColor("#F43F5E"),
                    Shader.TileMode.CLAMP)

                // 하트 모양으로 그리기
                val heartPath = Path().apply {
                    moveTo(x, y + 8f)
                    cubicTo(x - 20f, y - 8f, x - 35f, y + 5f, x, y + 20f)
                    cubicTo(x + 35f, y + 5f, x + 20f, y - 8f, x, y + 8f)
                }
                canvas.drawPath(heartPath, notePaint)

                // 하트 안에 작은 하이라이트
                val highlight = Paint().apply {
                    color = Color.parseColor("#90FFFFFF")
                    style = Paint.Style.FILL
                }
                canvas.drawCircle(x - 5f, y - 2f, 4f, highlight)
            }
            else -> {
                // 기본 음표
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
            1 -> centerY + 80f  // 낮은 레 (불안 - 아래쪽)
            2 -> centerY + 64f  // 미 (슬픔 - 첫 번째 선)
            3 -> centerY + 48f  // 파
            4 -> centerY + 32f  // 솔 (기본 - 두 번째 선)
            5 -> centerY + 16f  // 라 (평온 - 세 번째 선 위)
            6 -> centerY + 0f   // 시 (화남/사랑 - 세 번째 선)
            7 -> centerY - 16f  // 높은 도 (기쁨 - 위쪽)
            8 -> centerY - 32f  // 높은 레 (설렘 - 가장 위)
            else -> centerY + 32f // 기본값
        }
    }
}