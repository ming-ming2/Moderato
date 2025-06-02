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

    private val staffPaint = Paint().apply {
        color = Color.parseColor("#60FFFFFF")
        strokeWidth = 3f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val backgroundPaint = Paint().apply {
        shader = LinearGradient(0f, 0f, 0f, 300f,
            Color.parseColor("#2A2D3A"),
            Color.parseColor("#1A1B23"),
            Shader.TileMode.CLAMP)
        style = Paint.Style.FILL
    }

    private val trebleClefPaint = Paint().apply {
        color = Color.parseColor("#FF6B9D")
        textSize = 90f
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
    }

    private val textPaint = Paint().apply {
        color = Color.parseColor("#FFFFFF")
        textSize = 24f
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
    }

    private val glowPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        maskFilter = BlurMaskFilter(12f, BlurMaskFilter.Blur.NORMAL)
    }

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

        backgroundPaint.shader = LinearGradient(0f, 0f, 0f, height.toFloat(),
            Color.parseColor("#2A2D3A"),
            Color.parseColor("#1A1B23"),
            Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        val centerX = width / 2f
        val centerY = height / 2f
        val staffWidth = min(width * 0.9f, 600f)
        val staffStartX = centerX - staffWidth / 2
        val staffEndX = centerX + staffWidth / 2

        drawStaff(canvas, staffStartX, staffEndX, centerY)

        drawTrebleClef(canvas, staffStartX, centerY)

        drawKeyAndTempo(canvas, staffStartX, centerY)

        if (emotions.isNotEmpty()) {
            drawEmotionNotes(canvas, staffStartX, staffWidth, centerY)
        }

        drawBarLine(canvas, staffEndX, centerY)
    }

    private fun drawStaff(canvas: Canvas, startX: Float, endX: Float, centerY: Float) {
        val lineSpacing = 22f
        val staffTopY = centerY - (lineSpacing * 2)
        for (i in 0..4) {
            val y = staffTopY + (i * lineSpacing)
            canvas.drawLine(startX, y, endX, y, staffPaint)
        }
    }

    private fun drawTrebleClef(canvas: Canvas, startX: Float, centerY: Float) {
        canvas.drawText("𝄞", startX - 40f, centerY + 10f, trebleClefPaint)
    }

    private fun drawKeyAndTempo(canvas: Canvas, startX: Float, centerY: Float) {
        val lineSpacing = 22f
        val staffTopY = centerY - (lineSpacing * 2)
        canvas.drawText(currentKey, startX + 20f, staffTopY - 15f, textPaint)
        canvas.drawText("♩ = $currentTempo", startX + 20f, staffTopY + 10f, textPaint)
    }

    private fun drawEmotionNotes(canvas: Canvas, startX: Float, staffWidth: Float, centerY: Float) {
        val noteSpacing = if (emotions.size > 1) staffWidth / (emotions.size + 1) else staffWidth / 2

        emotions.forEachIndexed { index, emotion ->
            val noteX = startX + noteSpacing * (index + 1)
            val noteY = getNoteY(centerY, emotion.pitch)

            drawNoteGlow(canvas, noteX, noteY, emotion)

            drawNote(canvas, noteX, noteY, emotion)

            drawTimeLabel(canvas, noteX, centerY + 80f, emotion.time)
        }
    }

    private fun getNoteY(centerY: Float, pitch: Int): Float {
        val lineSpacing = 22f

        return when (pitch) {
            1 -> centerY + (lineSpacing * 3) + 11f
            2 -> centerY + (lineSpacing * 2) + 11f
            3 -> centerY + lineSpacing + 11f
            4 -> centerY + 11f
            5 -> centerY - lineSpacing + 11f
            6 -> centerY - (lineSpacing * 2) + 11f
            7 -> centerY - (lineSpacing * 3) + 11f
            8 -> centerY - (lineSpacing * 4) + 11f
            else -> centerY + 11f
        }
    }

    private fun drawNoteGlow(canvas: Canvas, x: Float, y: Float, emotion: EmotionNote) {
        val baseColor = when (emotion.symbol) {
            "♪" -> Color.parseColor("#FFD700")
            "♩" -> Color.parseColor("#8B5CF6")
            "♫" -> Color.parseColor("#FFB366")
            "♭" -> Color.parseColor("#6366F1")
            "♯" -> Color.parseColor("#F43F5E")
            "𝄢" -> Color.parseColor("#6B7280")
            "♡" -> Color.parseColor("#F59E0B")
            else -> Color.parseColor("#FFFFFF")
        }

        val adjustedColor = adjustColorForTimeOfDay(baseColor, emotion.timeOfDay)

        val alpha = when (emotion.intensity) {
            1 -> 0x40
            2 -> 0x60
            3 -> 0x80
            4 -> 0xA0
            5 -> 0xC0
            else -> 0x80
        }

        val glowRadius = when (emotion.intensity) {
            1 -> 12f
            2 -> 16f
            3 -> 20f
            4 -> 25f
            5 -> 30f
            else -> 20f
        }

        glowPaint.color = Color.argb(alpha, Color.red(adjustedColor), Color.green(adjustedColor), Color.blue(adjustedColor))
        canvas.drawCircle(x, y, glowRadius, glowPaint)

        drawTimeOfDayEffect(canvas, x, y, emotion.timeOfDay, emotion.intensity)
    }

    private fun adjustColorForTimeOfDay(baseColor: Int, timeOfDay: String): Int {
        val red = Color.red(baseColor)
        val green = Color.green(baseColor)
        val blue = Color.blue(baseColor)

        return when (timeOfDay) {
            "morning" -> {
                Color.rgb(
                    (red * 0.9f).toInt().coerceIn(0, 255),
                    (green * 0.95f).toInt().coerceIn(0, 255),
                    (blue * 1.1f).toInt().coerceIn(0, 255)
                )
            }
            "afternoon" -> {
                Color.rgb(
                    (red * 1.1f).toInt().coerceIn(0, 255),
                    (green * 1.05f).toInt().coerceIn(0, 255),
                    blue
                )
            }
            "evening" -> {
                Color.rgb(
                    (red * 1.2f).toInt().coerceIn(0, 255),
                    (green * 1.1f).toInt().coerceIn(0, 255),
                    (blue * 0.8f).toInt().coerceIn(0, 255)
                )
            }
            "night" -> {
                Color.rgb(
                    (red * 0.7f).toInt().coerceIn(0, 255),
                    (green * 0.8f).toInt().coerceIn(0, 255),
                    blue
                )
            }
            else -> baseColor
        }
    }

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

        val baseEffectRadius = 15f + (intensity - 1) * 2f
        when (timeOfDay) {
            "morning" -> {
                effectPaint.color = Color.argb(alpha, 255, 255, 200)
                for (i in 0..5) {
                    val angle = i * 60f
                    val startRadius = baseEffectRadius * 0.8f
                    val endRadius = baseEffectRadius * 1.2f
                    val startX = x + cos(Math.toRadians(angle.toDouble())).toFloat() * startRadius
                    val startY = y + sin(Math.toRadians(angle.toDouble())).toFloat() * startRadius
                    val endX = x + cos(Math.toRadians(angle.toDouble())).toFloat() * endRadius
                    val endY = y + sin(Math.toRadians(angle.toDouble())).toFloat() * endRadius
                    canvas.drawLine(startX, startY, endX, endY, effectPaint)
                }
            }
            "afternoon" -> {
                effectPaint.color = Color.argb(alpha, 255, 200, 100)
                canvas.drawCircle(x, y, baseEffectRadius, effectPaint)
            }
            "evening" -> {
                effectPaint.color = Color.argb(alpha, 255, 180, 120)
                effectPaint.style = Paint.Style.FILL
                effectPaint.maskFilter = BlurMaskFilter(baseEffectRadius * 0.5f, BlurMaskFilter.Blur.NORMAL)
                canvas.drawCircle(x, y, baseEffectRadius * 0.8f, effectPaint)
            }
            "night" -> {
                effectPaint.color = Color.argb(alpha, 200, 200, 255)
                effectPaint.style = Paint.Style.FILL
                for (i in 0..3) {
                    val starX = x + cos(i * 90.0).toFloat() * (baseEffectRadius + 5f)
                    val starY = y + sin(i * 90.0).toFloat() * (baseEffectRadius + 5f)
                    canvas.drawCircle(starX, starY, baseEffectRadius * 0.1f, effectPaint)
                }
            }
        }
    }

    private fun drawNote(canvas: Canvas, x: Float, y: Float, emotion: EmotionNote) {
        val size = when (emotion.intensity) {
            1 -> 0.7f
            2 -> 0.85f
            3 -> 1.0f
            4 -> 1.2f
            5 -> 1.5f
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

    private fun drawJoyNote(canvas: Canvas, x: Float, y: Float, paint: Paint, size: Float, timeOfDay: String) {
        val baseColor = adjustColorForTimeOfDay(Color.parseColor("#FFD700"), timeOfDay)
        paint.color = baseColor

        val radius = 16f * size
        canvas.drawCircle(x, y, radius, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3.5f * size
        canvas.drawLine(x + radius, y, x + radius, y - 50f * size, paint)

        val path = Path().apply {
            moveTo(x + radius, y - 37f * size)
            quadTo(x + 28f * size, y - 25f * size, x + 25f * size, y - 12f * size)
        }
        canvas.drawPath(path, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawPeaceNote(canvas: Canvas, x: Float, y: Float, paint: Paint, size: Float, timeOfDay: String) {
        val baseColor = adjustColorForTimeOfDay(Color.parseColor("#8B5CF6"), timeOfDay)
        paint.color = baseColor

        val radius = 13f * size
        canvas.drawCircle(x, y, radius, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3.5f * size
        canvas.drawLine(x + radius, y, x + radius, y - 43f * size, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawExcitementNote(canvas: Canvas, x: Float, y: Float, paint: Paint, size: Float, timeOfDay: String) {
        val baseColor = adjustColorForTimeOfDay(Color.parseColor("#FFB366"), timeOfDay)
        paint.color = baseColor

        val radius = 10.5f * size
        canvas.drawCircle(x - 10.5f * size, y, radius, paint)
        canvas.drawCircle(x + 10.5f * size, y + 4f * size, radius, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.5f * size
        canvas.drawLine(x - 10.5f * size + radius, y, x - 10.5f * size + radius, y - 38f * size, paint)
        canvas.drawLine(x + 10.5f * size + radius, y + 4f * size, x + 10.5f * size + radius, y + 4f * size - 33f * size, paint)
        canvas.drawLine(x - 10.5f * size + radius, y - 38f * size, x + 10.5f * size + radius, y + 4f * size - 33f * size, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawSadnessNote(canvas: Canvas, x: Float, y: Float, paint: Paint, size: Float, timeOfDay: String) {
        val baseColor = adjustColorForTimeOfDay(Color.parseColor("#6366F1"), timeOfDay)
        paint.color = baseColor

        val radius = 13f * size
        canvas.drawCircle(x, y, radius, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.5f * size
        canvas.drawLine(x - 25f * size, y - 18f * size, x - 25f * size, y + 6f * size, paint)

        val flatPath = Path().apply {
            moveTo(x - 25f * size, y - 4f * size)
            quadTo(x - 15f * size, y - 12f * size, x - 19f * size, y)
            quadTo(x - 15f * size, y + 4f * size, x - 25f * size, y)
        }
        canvas.drawPath(flatPath, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawAngerNote(canvas: Canvas, x: Float, y: Float, paint: Paint, size: Float, timeOfDay: String) {
        val baseColor = adjustColorForTimeOfDay(Color.parseColor("#F43F5E"), timeOfDay)
        paint.color = baseColor

        val radius = 13f * size
        canvas.drawCircle(x, y, radius, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.5f * size
        val offset = 25f * size
        canvas.drawLine(x - offset, y - 12f * size, x - offset, y + 12f * size, paint)
        canvas.drawLine(x - offset + 7f * size, y - 12f * size, x - offset + 7f * size, y + 12f * size, paint)
        canvas.drawLine(x - offset - 4f * size, y - 6f * size, x - offset + 10f * size, y - 4f * size, paint)
        canvas.drawLine(x - offset - 4f * size, y + 4f * size, x - offset + 10f * size, y + 6f * size, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawAnxietyNote(canvas: Canvas, x: Float, y: Float, paint: Paint, size: Float, timeOfDay: String) {
        val baseColor = adjustColorForTimeOfDay(Color.parseColor("#6B7280"), timeOfDay)
        paint.color = baseColor

        val radius = 13f * size
        canvas.drawCircle(x, y, radius, paint)

        val bassPaint = Paint().apply {
            color = baseColor
            textSize = 20f * size
            isAntiAlias = true
        }
        canvas.drawText("𝄢", x - 22f * size, y + 6f * size, bassPaint)
    }

    private fun drawLoveNote(canvas: Canvas, x: Float, y: Float, paint: Paint, size: Float, timeOfDay: String) {
        val baseColor = adjustColorForTimeOfDay(Color.parseColor("#F59E0B"), timeOfDay)
        paint.color = baseColor

        val heartPath = Path().apply {
            moveTo(x, y + 7f * size)
            cubicTo(x - 18f * size, y - 7f * size, x - 30f * size, y + 4f * size, x, y + 18f * size)
            cubicTo(x + 30f * size, y + 4f * size, x + 18f * size, y - 7f * size, x, y + 7f * size)
        }
        canvas.drawPath(heartPath, paint)
    }

    private fun drawDefaultNote(canvas: Canvas, x: Float, y: Float, paint: Paint, size: Float) {
        paint.color = Color.parseColor("#FFFFFF")
        canvas.drawCircle(x, y, 13f * size, paint)
    }

    private fun drawTimeLabel(canvas: Canvas, x: Float, y: Float, time: String) {
        val labelPaint = Paint().apply {
            color = Color.parseColor("#B0B3B8")
            textSize = 16f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        canvas.drawCircle(x, y, 18f, Paint().apply {
            color = Color.parseColor("#40FFFFFF")
            style = Paint.Style.FILL
            isAntiAlias = true
        })

        canvas.drawText(time, x, y + 5f, labelPaint)
    }

    private fun drawBarLine(canvas: Canvas, endX: Float, centerY: Float) {
        val barPaint = Paint().apply {
            color = Color.parseColor("#60FFFFFF")
            strokeWidth = 3f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        val lineSpacing = 22f
        val staffTopY = centerY - (lineSpacing * 2)
        val staffBottomY = centerY + (lineSpacing * 2)
        canvas.drawLine(endX + 10f, staffTopY, endX + 10f, staffBottomY, barPaint)
    }
}