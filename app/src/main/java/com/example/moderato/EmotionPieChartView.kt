package com.example.moderato

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class EmotionPieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val piePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val strokePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.parseColor("#40FFFFFF")
    }

    private val centerTextPaint = Paint().apply {
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        color = Color.parseColor("#FFFFFF")
        textSize = 16f
        typeface = Typeface.DEFAULT_BOLD
    }

    private val centerSubTextPaint = Paint().apply {
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        color = Color.parseColor("#B0B3B8")
        textSize = 12f
    }

    private val glowPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        maskFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)
    }

    private var emotionData = listOf<EmotionChartData>()
    private val emotionColors = mapOf(
        "기쁨" to Color.parseColor("#FF6B9D"),
        "평온" to Color.parseColor("#8B5CF6"),
        "설렘" to Color.parseColor("#FFB366"),
        "슬픔" to Color.parseColor("#6366F1"),
        "화남" to Color.parseColor("#F43F5E"),
        "불안" to Color.parseColor("#10B981"),
        "사랑" to Color.parseColor("#F59E0B")
    )

    data class EmotionChartData(
        val emotion: String,
        val count: Int,
        val percentage: Float
    )

    fun setEmotionData(data: List<EmotionChartData>) {
        this.emotionData = data
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (emotionData.isEmpty()) {
            drawEmptyChart(canvas)
            return
        }

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (minOf(width, height) / 2f) * 0.7f
        val innerRadius = radius * 0.4f

        // 도넛 차트 그리기
        drawDonutChart(canvas, centerX, centerY, radius, innerRadius)

        // 중앙 텍스트
        drawCenterText(canvas, centerX, centerY)
    }

    private fun drawDonutChart(canvas: Canvas, centerX: Float, centerY: Float, radius: Float, innerRadius: Float) {
        var startAngle = -90f // 12시 방향부터 시작

        val rect = RectF(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        emotionData.forEach { data ->
            val sweepAngle = (data.percentage / 100f) * 360f

            // 글로우 효과
            drawGlowArc(canvas, rect, startAngle, sweepAngle, data.emotion)

            // 메인 아크
            drawMainArc(canvas, rect, startAngle, sweepAngle, data.emotion)

            startAngle += sweepAngle
        }

        // 내부 원 (도넛 구멍)
        piePaint.color = Color.parseColor("#1A1B23")
        canvas.drawCircle(centerX, centerY, innerRadius, piePaint)

        // 내부 원 테두리
        canvas.drawCircle(centerX, centerY, innerRadius, strokePaint)
    }

    private fun drawGlowArc(canvas: Canvas, rect: RectF, startAngle: Float, sweepAngle: Float, emotion: String) {
        val glowColor = emotionColors[emotion] ?: Color.parseColor("#FFFFFF")
        glowPaint.color = Color.argb(60, Color.red(glowColor), Color.green(glowColor), Color.blue(glowColor))

        val path = Path()
        path.addArc(rect, startAngle, sweepAngle)
        canvas.drawPath(path, glowPaint)
    }

    private fun drawMainArc(canvas: Canvas, rect: RectF, startAngle: Float, sweepAngle: Float, emotion: String) {
        val baseColor = emotionColors[emotion] ?: Color.parseColor("#FFFFFF")

        // 그라데이션 생성
        val midAngle = startAngle + sweepAngle / 2
        val gradientEndX = (rect.centerX() + cos(Math.toRadians(midAngle.toDouble())) * rect.width() / 4).toFloat()
        val gradientEndY = (rect.centerY() + sin(Math.toRadians(midAngle.toDouble())) * rect.height() / 4).toFloat()

        piePaint.shader = RadialGradient(
            gradientEndX, gradientEndY, rect.width() / 3,
            baseColor,
            Color.argb(180, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor)),
            Shader.TileMode.CLAMP
        )

        canvas.drawArc(rect, startAngle, sweepAngle, true, piePaint)

        // 테두리
        piePaint.shader = null
        piePaint.style = Paint.Style.STROKE
        piePaint.color = Color.parseColor("#40FFFFFF")
        piePaint.strokeWidth = 2f
        canvas.drawArc(rect, startAngle, sweepAngle, true, piePaint)

        // 원래 설정 복원
        piePaint.style = Paint.Style.FILL
    }

    private fun drawCenterText(canvas: Canvas, centerX: Float, centerY: Float) {
        val totalCount = emotionData.sumOf { it.count }
        val dominantEmotion = emotionData.maxByOrNull { it.count }?.emotion ?: "감정"

        canvas.drawText(dominantEmotion, centerX, centerY - 5f, centerTextPaint)
        canvas.drawText("총 ${totalCount}회", centerX, centerY + 15f, centerSubTextPaint)
    }

    private fun drawEmptyChart(canvas: Canvas) {
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (minOf(width, height) / 2f) * 0.7f

        // 빈 원
        piePaint.color = Color.parseColor("#2A2D3A")
        canvas.drawCircle(centerX, centerY, radius, piePaint)

        // 테두리
        canvas.drawCircle(centerX, centerY, radius, strokePaint)

        // 내부 원
        piePaint.color = Color.parseColor("#1A1B23")
        canvas.drawCircle(centerX, centerY, radius * 0.4f, piePaint)
        canvas.drawCircle(centerX, centerY, radius * 0.4f, strokePaint)

        // 빈 상태 텍스트
        canvas.drawText("아직", centerX, centerY - 5f, centerTextPaint)
        canvas.drawText("감정 없음", centerX, centerY + 15f, centerSubTextPaint)
    }
}