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

    private val symbolPaint = Paint().apply {
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        color = Color.parseColor("#FFFFFF")
        textSize = 16f  // 크기 키움
        typeface = Typeface.DEFAULT_BOLD
        setShadowLayer(5f, 1f, 1f, Color.parseColor("#FF000000"))  // 그림자 더 강하게
    }

    private val percentagePaint = Paint().apply {
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        color = Color.parseColor("#FFFFFF")
        textSize = 12f  // 크기 키움
        typeface = Typeface.DEFAULT_BOLD
        setShadowLayer(4f, 1f, 1f, Color.parseColor("#FF000000"))  // 그림자 더 강하게
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
        // 실제 데이터가 있는 감정만 저장 (count가 0보다 큰 것만)
        this.emotionData = data.filter { it.count > 0 }
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
        val radius = (minOf(width, height) / 2f) * 0.85f  // 0.7f → 0.85f로 키움
        val innerRadius = radius * 0.35f  // 0.4f → 0.35f로 줄여서 차트 더 넓게

        // 도넛 차트 그리기
        drawDonutChart(canvas, centerX, centerY, radius, innerRadius)

        // 중앙 텍스트
        drawCenterText(canvas, centerX, centerY)

        // 범례 그리기 (차트 아래쪽에)
        if (emotionData.isNotEmpty()) {
            drawLegend(canvas, centerX, centerY + radius + 50f)  // 여백 늘림
        }
    }

    private fun drawDonutChart(canvas: Canvas, centerX: Float, centerY: Float, radius: Float, innerRadius: Float) {
        var startAngle = -90f // 12시 방향부터 시작

        val rect = RectF(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        // 실제 데이터가 있는 감정만 그리기
        emotionData.forEach { data ->
            // count가 0인 데이터는 건너뛰기
            if (data.count <= 0) return@forEach

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

        // 감정명 표시 (각 섹션 중앙에) - 5% 이상일 때만 (더 관대하게)
        val percentage = (sweepAngle / 360f) * 100f
        if (percentage >= 5f) {  // 10% → 5%로 더 낮춤
            drawEmotionSymbol(canvas, rect, startAngle, sweepAngle, emotion, percentage)
        }

        // 원래 설정 복원
        piePaint.style = Paint.Style.FILL
    }

    /**
     * 각 섹션에 감정명과 퍼센트 표시
     */
    private fun drawEmotionSymbol(canvas: Canvas, rect: RectF, startAngle: Float, sweepAngle: Float, emotion: String, percentage: Float) {
        val midAngle = startAngle + sweepAngle / 2
        val radius = (rect.width() + rect.height()) / 4f * 0.65f // 중간 지점

        val textX = rect.centerX() + cos(Math.toRadians(midAngle.toDouble())).toFloat() * radius
        val textY = rect.centerY() + sin(Math.toRadians(midAngle.toDouble())).toFloat() * radius

        // 감정명 그리기 (기호 대신 한글로)
        canvas.drawText(emotion, textX, textY - 8f, symbolPaint)

        // 퍼센트 표시 (15% 이상일 때만)
        if (percentage >= 15f) {  // 20% → 15%로 낮춤
            val percentText = "${percentage.toInt()}%"
            canvas.drawText(percentText, textX, textY + 12f, percentagePaint)
        }
    }

    /**
     * 감정명에서 기호 추출
     */
    private fun getEmotionSymbol(emotion: String): String {
        return when(emotion) {
            "기쁨" -> "♪"
            "평온" -> "♩"
            "설렘" -> "♫"
            "슬픔" -> "♭"
            "화남" -> "♯"
            "불안" -> "𝄢"
            "사랑" -> "♡"
            else -> "♪"
        }
    }

    /**
     * 범례 그리기 - 모든 감정 데이터 표시 (누락 방지)
     */
    private fun drawLegend(canvas: Canvas, centerX: Float, startY: Float) {
        val legendPaint = Paint().apply {
            isAntiAlias = true
            textSize = 13f  // 조금 키움
            color = Color.parseColor("#FFFFFF")
        }

        val itemHeight = 20f  // 간격 늘림
        val circleRadius = 7f  // 원 크기 키움
        var currentY = startY

        // 디버깅: 모든 감정 데이터 강제 표시
        if (emotionData.isEmpty()) {
            canvas.drawText("감정 데이터가 없습니다", centerX, currentY, legendPaint)
            return
        }

        // 모든 감정 데이터 표시
        emotionData.forEach { data ->
            val color = emotionColors[data.emotion] ?: Color.parseColor("#FFFFFF")

            // 색상 원 그리기
            piePaint.color = color
            piePaint.style = Paint.Style.FILL
            canvas.drawCircle(centerX - 100f, currentY, circleRadius, piePaint)

            // 감정명과 통계 그리기
            val text = "${data.emotion} ${String.format("%.1f", data.percentage)}% (${data.count}회)"
            legendPaint.textAlign = Paint.Align.LEFT
            canvas.drawText(text, centerX - 85f, currentY + 5f, legendPaint)

            currentY += itemHeight
        }

        // Paint 스타일 복원
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
        val radius = (minOf(width, height) / 2f) * 0.85f  // 빈 차트도 같은 크기로

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