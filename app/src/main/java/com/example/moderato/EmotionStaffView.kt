package com.example.moderato

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.min
import kotlin.math.cos
import kotlin.math.sin

class EmotionStaffView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 페인트 설정들
    private val staffPaint = Paint().apply {
        color = Color.parseColor("#40FFFFFF")
        strokeWidth = 2f
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
        color = Color.parseColor("#FF6B9D")
        textSize = 60f
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
    }

    private val textPaint = Paint().apply {
        color = Color.parseColor("#FFFFFF")
        textSize = 16f
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
    }

    private val glowPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        maskFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)
    }

    // 데이터
    private var emotions = listOf<EmotionNote>()
    private var currentKey = "C Major"
    private var currentTempo = "Moderato"

    data class EmotionNote(
        val symbol: String,
        val pitch: Int,
        val time: String,
        val intensity: Int = 3,
        val timeOfDay: String = "morning"
    )

    fun setEmotions(emotions: List<EmotionNote>, key: String = "C Major", tempo: String = "Moderato") {
        this.emotions = emotions
        this.currentKey = key
        this.currentTempo = tempo
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 배경
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        val centerX = width / 2f
        val centerY = height / 2f
        val staffWidth = min(width * 0.9f, 400f)
        val staffStartX = centerX - staffWidth / 2
        val staffEndX = centerX + staffWidth / 2

        // 오선지 그리기
        drawStaff(canvas, staffStartX, staffEndX, centerY)

        // 높은음자리표
        drawTrebleClef(canvas, staffStartX, centerY)

        // 조성과 템포
        drawKeyAndTempo(canvas, staffStartX, centerY)

        // 감정 음표들
        if (emotions.isNotEmpty()) {
            drawEmotionNotes(canvas, staffStartX, staffWidth, centerY)
        }

        // 마디선
        drawBarLine(canvas, staffEndX, centerY)
    }

    // 오선지 그리기
    private fun drawStaff(canvas: Canvas, startX: Float, endX: Float, centerY: Float) {
        for (i in 0..4) {
            val y = centerY - 64f + (i * 16f)
            canvas.drawLine(startX, y, endX, y, staffPaint)
        }
    }

    // 높은음자리표
    private fun drawTrebleClef(canvas: Canvas, startX: Float, centerY: Float) {
        canvas.drawText("𝄞", startX - 50f, centerY + 15f, trebleClefPaint)
    }

    // 조성과 템포 표시
    private fun drawKeyAndTempo(canvas: Canvas, startX: Float, centerY: Float) {
        canvas.drawText(currentKey, startX + 20f, centerY - 80f, textPaint)
        canvas.drawText("♩ = $currentTempo", startX + 20f, centerY - 60f, textPaint)
    }

    // 감정 음표들 그리기
    private fun drawEmotionNotes(canvas: Canvas, startX: Float, staffWidth: Float, centerY: Float) {
        val noteSpacing = if (emotions.size > 1) staffWidth / (emotions.size + 1) else staffWidth / 2

        emotions.forEachIndexed { index, emotion ->
            val noteX = startX + noteSpacing * (index + 1)
            val noteY = getNoteY(centerY, emotion.pitch)

            // 글로우 효과
            drawNoteGlow(canvas, noteX, noteY, emotion)

            // 음표 그리기
            drawNote(canvas, noteX, noteY, emotion)

            // 시간 라벨
            drawTimeLabel(canvas, noteX, centerY + 80f, emotion.time)
        }
    }

    // 음표 위치 계산
    private fun getNoteY(centerY: Float, pitch: Int): Float {
        return when (pitch) {
            1 -> centerY + 80f  // 낮은 레 (불안)
            2 -> centerY + 64f  // 미 (슬픔)
            3 -> centerY + 48f  // 파
            4 -> centerY + 32f  // 솔 (기본)
            5 -> centerY + 16f  // 라 (평온)
            6 -> centerY + 0f   // 시 (화남/사랑)
            7 -> centerY - 16f  // 높은 도 (기쁨)
            8 -> centerY - 32f  // 높은 레 (설렘)
            else -> centerY + 32f
        }
    }

    // 글로우 효과
    private fun drawNoteGlow(canvas: Canvas, x: Float, y: Float, emotion: EmotionNote) {
        // 감정별 기본 색상
        val baseColor = when (emotion.symbol) {
            "♪" -> Color.parseColor("#FFD700") // 기쁨 - 황금
            "♩" -> Color.parseColor("#8B5CF6") // 평온 - 보라
            "♫" -> Color.parseColor("#FFB366") // 설렘 - 오렌지
            "♭" -> Color.parseColor("#6366F1") // 슬픔 - 블루
            "♯" -> Color.parseColor("#F43F5E") // 화남 - 레드
            "𝄢" -> Color.parseColor("#6B7280") // 불안 - 회색
            "♡" -> Color.parseColor("#F59E0B") // 사랑 - 주황
            else -> Color.parseColor("#FFFFFF")
        }

        // 시간대별 색온도 조정
        val adjustedColor = adjustColorForTimeOfDay(baseColor, emotion.timeOfDay)

        // 강도별 투명도 및 크기
        val alpha = when (emotion.intensity) {
            1 -> 0x40  // pp
            2 -> 0x60  // p
            3 -> 0x80  // mf
            4 -> 0xA0  // f
            5 -> 0xC0  // ff
            else -> 0x80
        }

        val glowRadius = when (emotion.intensity) {
            1 -> 15f
            2 -> 20f
            3 -> 25f
            4 -> 32f
            5 -> 40f
            else -> 25f
        }

        glowPaint.color = Color.argb(alpha, Color.red(adjustedColor), Color.green(adjustedColor), Color.blue(adjustedColor))
        canvas.drawCircle(x, y, glowRadius, glowPaint)

        // 시간대별 특수 효과
        drawTimeOfDayEffect(canvas, x, y, emotion.timeOfDay, emotion.intensity)
    }

    // 시간대별 색온도 조정
    private fun adjustColorForTimeOfDay(baseColor: Int, timeOfDay: String): Int {
        val red = Color.red(baseColor)
        val green = Color.green(baseColor)
        val blue = Color.blue(baseColor)

        return when (timeOfDay) {
            "morning" -> {
                // 아침 - 시원한 톤
                Color.rgb(
                    (red * 0.9f).toInt().coerceIn(0, 255),
                    (green * 0.95f).toInt().coerceIn(0, 255),
                    (blue * 1.1f).toInt().coerceIn(0, 255)
                )
            }
            "afternoon" -> {
                // 오후 - 밝은 톤
                Color.rgb(
                    (red * 1.1f).toInt().coerceIn(0, 255),
                    (green * 1.05f).toInt().coerceIn(0, 255),
                    blue
                )
            }
            "evening" -> {
                // 저녁 - 따뜻한 톤
                Color.rgb(
                    (red * 1.2f).toInt().coerceIn(0, 255),
                    (green * 1.1f).toInt().coerceIn(0, 255),
                    (blue * 0.8f).toInt().coerceIn(0, 255)
                )
            }
            "night" -> {
                // 밤 - 어두운 톤
                Color.rgb(
                    (red * 0.7f).toInt().coerceIn(0, 255),
                    (green * 0.8f).toInt().coerceIn(0, 255),
                    blue
                )
            }
            else -> baseColor
        }
    }

    // 시간대별 특수 효과
    private fun drawTimeOfDayEffect(canvas: Canvas, x: Float, y: Float, timeOfDay: String, intensity: Int) {
        val effectPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        val alpha = when (intensity) {
            1 -> 0x30
            2 -> 0x50
            3 -> 0x70
            4 -> 0x90
            5 -> 0xB0
            else -> 0x70
        }

        when (timeOfDay) {
            "morning" -> {
                // 아침 - 햇살 효과
                effectPaint.color = Color.argb(alpha, 255, 255, 200)
                for (i in 0..5) {
                    val angle = i * 60f
                    val startRadius = 20f
                    val endRadius = 35f
                    val startX = x + cos(Math.toRadians(angle.toDouble())).toFloat() * startRadius
                    val startY = y + sin(Math.toRadians(angle.toDouble())).toFloat() * startRadius
                    val endX = x + cos(Math.toRadians(angle.toDouble())).toFloat() * endRadius
                    val endY = y + sin(Math.toRadians(angle.toDouble())).toFloat() * endRadius
                    canvas.drawLine(startX, startY, endX, endY, effectPaint)
                }
            }
            "afternoon" -> {
                // 오후 - 원형 글로우
                effectPaint.color = Color.argb(alpha, 255, 200, 100)
                canvas.drawCircle(x, y, 30f, effectPaint)
            }
            "evening" -> {
                // 저녁 - 달무리
                effectPaint.color = Color.argb(alpha, 255, 180, 120)
                effectPaint.style = Paint.Style.FILL
                effectPaint.maskFilter = BlurMaskFilter(10f, BlurMaskFilter.Blur.NORMAL)
                canvas.drawCircle(x, y, 25f, effectPaint)
            }
            "night" -> {
                // 밤 - 별빛
                effectPaint.color = Color.argb(alpha, 200, 200, 255)
                effectPaint.style = Paint.Style.FILL
                for (i in 0..3) {
                    val starX = x + cos(i * 90.0).toFloat() * 30f
                    val starY = y + sin(i * 90.0).toFloat() * 30f
                    canvas.drawCircle(starX, starY, 1.5f, effectPaint)
                }
            }
        }
    }

    // 음표 그리기
    private fun drawNote(canvas: Canvas, x: Float, y: Float, emotion: EmotionNote) {
        val size = when (emotion.intensity) {
            1 -> 0.7f   // pp
            2 -> 0.85f  // p
            3 -> 1.0f   // mf
            4 -> 1.2f   // f
            5 -> 1.5f   // ff
            else -> 1.0f
        }

        val notePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        when (emotion.symbol) {
            "♪" -> drawJoyNote(canvas, x, y, notePaint, size, emotion.timeOfDay)
            "♩" -> drawPeaceNote(canvas, x, y, notePaint, size, emotion.timeOfDay)
            "♫" -> drawExcitementNote(canvas, x, y, notePaint, size, emotion.timeOfDay)
            "♭" -> drawSadnessNote(canvas, x, y, notePaint, size, emotion.timeOfDay)
            "♯" -> drawAngerNote(canvas, x, y, notePaint, size, emotion.timeOfDay)
            "𝄢" -> drawAnxietyNote(canvas, x, y, notePaint, size, emotion.timeOfDay)
            "♡" -> drawLoveNote(canvas, x, y, notePaint, size, emotion.timeOfDay)
            else -> drawDefaultNote(canvas, x, y, notePaint, size)
        }
    }

    // 기쁨 음표
    private fun drawJoyNote(canvas: Canvas, x: Float, y: Float, paint: Paint, size: Float, timeOfDay: String) {
        val baseColor = adjustColorForTimeOfDay(Color.parseColor("#FFD700"), timeOfDay)
        paint.color = baseColor

        val radius = 12f * size
        canvas.drawCircle(x, y, radius, paint)

        // 8분음표 꼬리
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f * size
        canvas.drawLine(x + radius, y, x + radius, y - 40f * size, paint)

        val path = Path().apply {
            moveTo(x + radius, y - 30f * size)
            quadTo(x + 25f * size, y - 20f * size, x + 20f * size, y - 10f * size)
        }
        canvas.drawPath(path, paint)
        paint.style = Paint.Style.FILL
    }

    // 평온 음표
    private fun drawPeaceNote(canvas: Canvas, x: Float, y: Float, paint: Paint, size: Float, timeOfDay: String) {
        val baseColor = adjustColorForTimeOfDay(Color.parseColor("#8B5CF6"), timeOfDay)
        paint.color = baseColor

        val radius = 10f * size
        canvas.drawCircle(x, y, radius, paint)

        // 4분음표 기둥
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f * size
        canvas.drawLine(x + radius, y, x + radius, y - 35f * size, paint)
        paint.style = Paint.Style.FILL
    }

    // 설렘 음표
    private fun drawExcitementNote(canvas: Canvas, x: Float, y: Float, paint: Paint, size: Float, timeOfDay: String) {
        val baseColor = adjustColorForTimeOfDay(Color.parseColor("#FFB366"), timeOfDay)
        paint.color = baseColor

        val radius = 8f * size
        // 두 개의 연결된 음표
        canvas.drawCircle(x - 8f * size, y, radius, paint)
        canvas.drawCircle(x + 8f * size, y + 3f * size, radius, paint)

        // 연결선
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f * size
        canvas.drawLine(x - 8f * size + radius, y, x - 8f * size + radius, y - 30f * size, paint)
        canvas.drawLine(x + 8f * size + radius, y + 3f * size, x + 8f * size + radius, y + 3f * size - 25f * size, paint)
        canvas.drawLine(x - 8f * size + radius, y - 30f * size, x + 8f * size + radius, y + 3f * size - 25f * size, paint)
        paint.style = Paint.Style.FILL
    }

    // 슬픔 음표
    private fun drawSadnessNote(canvas: Canvas, x: Float, y: Float, paint: Paint, size: Float, timeOfDay: String) {
        val baseColor = adjustColorForTimeOfDay(Color.parseColor("#6366F1"), timeOfDay)
        paint.color = baseColor

        val radius = 10f * size
        canvas.drawCircle(x, y, radius, paint)

        // 플랫 기호
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f * size
        canvas.drawLine(x - 20f * size, y - 15f * size, x - 20f * size, y + 5f * size, paint)

        val flatPath = Path().apply {
            moveTo(x - 20f * size, y - 3f * size)
            quadTo(x - 12f * size, y - 10f * size, x - 15f * size, y)
            quadTo(x - 12f * size, y + 3f * size, x - 20f * size, y)
        }
        canvas.drawPath(flatPath, paint)
        paint.style = Paint.Style.FILL
    }

    // 화남 음표
    private fun drawAngerNote(canvas: Canvas, x: Float, y: Float, paint: Paint, size: Float, timeOfDay: String) {
        val baseColor = adjustColorForTimeOfDay(Color.parseColor("#F43F5E"), timeOfDay)
        paint.color = baseColor

        val radius = 10f * size
        canvas.drawCircle(x, y, radius, paint)

        // 샵 기호
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f * size
        val offset = 20f * size
        canvas.drawLine(x - offset, y - 10f * size, x - offset, y + 10f * size, paint)
        canvas.drawLine(x - offset + 5f * size, y - 10f * size, x - offset + 5f * size, y + 10f * size, paint)
        canvas.drawLine(x - offset - 3f * size, y - 5f * size, x - offset + 8f * size, y - 3f * size, paint)
        canvas.drawLine(x - offset - 3f * size, y + 3f * size, x - offset + 8f * size, y + 5f * size, paint)
        paint.style = Paint.Style.FILL
    }

    // 불안 음표
    private fun drawAnxietyNote(canvas: Canvas, x: Float, y: Float, paint: Paint, size: Float, timeOfDay: String) {
        val baseColor = adjustColorForTimeOfDay(Color.parseColor("#6B7280"), timeOfDay)
        paint.color = baseColor

        val radius = 10f * size
        canvas.drawCircle(x, y, radius, paint)

        // 낮은음자리표
        val bassPaint = Paint().apply {
            color = baseColor
            textSize = 16f * size
            isAntiAlias = true
        }
        canvas.drawText("𝄢", x - 18f * size, y + 5f * size, bassPaint)
    }

    // 사랑 음표
    private fun drawLoveNote(canvas: Canvas, x: Float, y: Float, paint: Paint, size: Float, timeOfDay: String) {
        val baseColor = adjustColorForTimeOfDay(Color.parseColor("#F59E0B"), timeOfDay)
        paint.color = baseColor

        // 하트 모양
        val heartPath = Path().apply {
            moveTo(x, y + 6f * size)
            cubicTo(x - 15f * size, y - 6f * size, x - 25f * size, y + 3f * size, x, y + 15f * size)
            cubicTo(x + 25f * size, y + 3f * size, x + 15f * size, y - 6f * size, x, y + 6f * size)
        }
        canvas.drawPath(heartPath, paint)
    }

    // 기본 음표
    private fun drawDefaultNote(canvas: Canvas, x: Float, y: Float, paint: Paint, size: Float) {
        paint.color = Color.parseColor("#FFFFFF")
        canvas.drawCircle(x, y, 10f * size, paint)
    }

    // 시간 라벨
    private fun drawTimeLabel(canvas: Canvas, x: Float, y: Float, time: String) {
        val labelPaint = Paint().apply {
            color = Color.parseColor("#B0B3B8")
            textSize = 12f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        canvas.drawCircle(x, y, 15f, Paint().apply {
            color = Color.parseColor("#40FFFFFF")
            style = Paint.Style.FILL
            isAntiAlias = true
        })

        canvas.drawText(time, x, y + 4f, labelPaint)
    }

    // 마디선
    private fun drawBarLine(canvas: Canvas, endX: Float, centerY: Float) {
        val barPaint = Paint().apply {
            color = Color.parseColor("#60FFFFFF")
            strokeWidth = 2f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        canvas.drawLine(endX + 10f, centerY - 64f, endX + 10f, centerY + 0f, barPaint)
    }
}