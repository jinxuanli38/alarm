package com.example.alarm_jinxuan.utils

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

object SharedClockComponents {

    /**
     * 可复用的闹钟绘制组件
     * @param modifier 绘制区域的修饰符
     * @param clockSize 闹钟的尺寸
     */
    @Composable
    fun ClockView(
        modifier: Modifier = Modifier,
        clockSize: Dp = 300.dp
    ) {
        // 控制显示模式：true=钟表模式，false=电子时间模式
        var showAnalogClock by remember { mutableStateOf(true) }

        // 用于消除点击水波纹效果的交互源
        val interactionSource = remember { MutableInteractionSource() }

        var timeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

        // 实时更新时间
        LaunchedEffect(Unit) {
            while (true) {
                timeMillis = System.currentTimeMillis()
                // 计算到下一秒还差多少毫秒，精准对齐系统时间
                val sleepTime = 1000 - (System.currentTimeMillis() % 1000)
                delay(sleepTime)
            }
        }

        Box(
            modifier = modifier
                .size(clockSize)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    // 点击切换显示模式
                    showAnalogClock = !showAnalogClock
                },
            contentAlignment = Alignment.Center
        ) {
            // 模拟钟表显示（带缩放动画）
            AnimatedVisibility(
                visible = showAnalogClock,
                enter = fadeIn(animationSpec = tween(durationMillis = 200)) + scaleIn(
                    animationSpec = tween(durationMillis = 200),
                    initialScale = 0.7f
                ),
                exit = fadeOut(animationSpec = tween(durationMillis = 200)) + scaleOut(
                    animationSpec = tween(durationMillis = 200),
                    targetScale = 0.7f
                )
            ) {
                AnalogClockDisplay(timeMillis, clockSize)
            }

            // 电子时间显示（带缩放动画）
            AnimatedVisibility(
                visible = !showAnalogClock,
                enter = fadeIn(animationSpec = tween(durationMillis = 200)) + scaleIn(
                    animationSpec = tween(durationMillis = 200),
                    initialScale = 0.7f
                ),
                exit = fadeOut(animationSpec = tween(durationMillis = 200)) + scaleOut(
                    animationSpec = tween(durationMillis = 200),
                    targetScale = 0.7f
                )
            ) {
                DigitalTimeDisplay(timeMillis)
            }
        }
    }

    /**
     * 模拟时钟显示
     */
    @Composable
    private fun AnalogClockDisplay(
        timeMillis: Long,
        clockSize: Dp
    ) {
        // 角度计算（为了丝滑，必须把毫秒算进去）
        val cal = Calendar.getInstance().apply { timeInMillis = timeMillis }
        val millis = cal.get(Calendar.MILLISECOND)
        val second = cal.get(Calendar.SECOND)
        val minute = cal.get(Calendar.MINUTE)
        val hour = cal.get(Calendar.HOUR)

        // 秒针角度：当前秒 + (毫秒/1000)
        val secondAngle = (second + millis / 1000f) * 6f

        // 分针角度：当前分 + (秒/60)
        val minuteAngle = (minute + second / 60f) * 6f

        // 时针角度：当前时 + (分/60)
        val hourAngle = (hour % 12 + minute / 60f) * 30f

        val textPaint = remember {
            Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 48f
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
                typeface = Typeface.create("宋体", Typeface.NORMAL)
            }
        }

        val density = LocalDensity.current

        // 度量值对象
        val metrics = remember(density) {
            ClockMetrics(density)
        }

        val clockRadius = with(density) { 150.dp.toPx() }

        // 时针路径
        val hourHandPath = remember(density, metrics.ringRadius) {
            createHourHandPath(clockRadius, metrics, density)
        }

        // 分针路径
        val minuteHandPath = remember(density, metrics.ringRadius) {
            createMinuteHandPath(clockRadius, metrics, density)
        }

        // 绘制时钟
        Canvas(modifier = Modifier.size(clockSize)) {
            val radius = size.width / 2
            val center = size.center

            drawClockTicks(radius, center, metrics, textPaint)
            drawHands(center, hourAngle, minuteAngle, secondAngle, hourHandPath, minuteHandPath, metrics, radius, density)
        }
    }

    /**
     * 电子时间显示
     */
    @Composable
    private fun DigitalTimeDisplay(timeMillis: Long) {
        val cal = Calendar.getInstance().apply { timeInMillis = timeMillis }
        val hourOfDay = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val second = cal.get(Calendar.SECOND)

        // 判断上午/下午（0-12是上午，12-24是下午）
        val amPm = if (hourOfDay < 12) "上午" else "下午"

        // 12小时制的小时数
        val hour12 = if (hourOfDay == 0) 12 else (if (hourOfDay > 12) hourOfDay - 12 else hourOfDay)

        // 格式化时间字符串，保证两位数显示
        val timeString = "%02d:%02d:%02d".format(hour12, minute, second)

        Box(
            contentAlignment = Alignment.Center
        ) {
            Column {
                Text(
                    text = timeString,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Black
                )
                Text(
                    text = amPm,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
            }
        }
    }

    /**
     * 度量值配置类
     */
    private data class ClockMetrics(
        private val density: Density
    ) {
        val numPadding = 40.dp.toPx(density)
        val bigLine = 15.dp.toPx(density)
        val normalLine = 8.dp.toPx(density)
        val strokeW = 1.dp.toPx(density)
        val offsetTop = 10.dp.toPx(density)

        // 指针相关
        val needleGap = 3.5.dp.toPx(density)
        val needleTail = 10.dp.toPx(density)
        val needleWidth = 1.5.dp.toPx(density)
        val ringRadius = 3.dp.toPx(density)
        val ringStroke = 1.5.dp.toPx(density)
    }

    /**
     * 创建时针路径
     */
    private fun createHourHandPath(clockRadius: Float, metrics: ClockMetrics, density: Density): Path {
        return Path().apply {
            fillType = PathFillType.EvenOdd

            val widthBase = 5.dp.toPx(density)
            val totalLength = clockRadius * 0.55f
            val cornerRadius = 2.dp.toPx(density)
            val startY = -metrics.ringRadius

            moveTo(-widthBase / 2f, startY)
            lineTo(-widthBase / 2f, -totalLength + cornerRadius)
            arcTo(
                rect = Rect(
                    left = -widthBase / 2,
                    top = -totalLength,
                    right = widthBase / 2,
                    bottom = -totalLength + cornerRadius * 2
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false
            )
            lineTo(widthBase / 2f, startY)
            close()

            val hollowWidth = widthBase * 0.5f
            val topOffset = 0.1.dp.toPx(density)
            val hollowTopY = -(totalLength - cornerRadius - topOffset)
            val hollowBottomY = -(totalLength * 0.75f)

            val hollowRect = Rect(
                left = -hollowWidth / 2f,
                top = hollowTopY,
                right = hollowWidth / 2f,
                bottom = hollowBottomY
            )

            val hollowCornerRadius = hollowWidth / 2f

            addRoundRect(
                RoundRect(
                    rect = hollowRect,
                    cornerRadius = CornerRadius(hollowCornerRadius, hollowCornerRadius)
                )
            )
        }
    }

    /**
     * 创建分针路径
     */
    private fun createMinuteHandPath(clockRadius: Float, metrics: ClockMetrics, density: Density): Path {
        return Path().apply {
            fillType = PathFillType.EvenOdd

            val widthBase = 4.dp.toPx(density)
            val totalLength = clockRadius * 0.8f
            val cornerRadius = 1.5.dp.toPx(density)
            val startY = -metrics.ringRadius

            moveTo(-widthBase / 2f, startY)
            lineTo(-widthBase / 2f, -totalLength + cornerRadius)
            arcTo(
                rect = Rect(
                    left = -widthBase / 2f,
                    top = -totalLength,
                    right = widthBase / 2f,
                    bottom = -totalLength + cornerRadius * 2f
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false
            )
            lineTo(widthBase / 2f, startY)
            close()

            val hollowWidth = widthBase * 0.5f
            val topOffset = 0.1.dp.toPx(density)
            val hollowTopY = -(totalLength - cornerRadius - topOffset)
            val hollowBottomY = -(totalLength * 0.8f)

            val hollowRect = Rect(
                left = -hollowWidth / 2f,
                top = hollowTopY,
                right = hollowWidth / 2f,
                bottom = hollowBottomY
            )

            val hollowCornerRadius = hollowWidth / 2f
            addRoundRect(
                RoundRect(
                    rect = hollowRect,
                    cornerRadius = CornerRadius(hollowCornerRadius, hollowCornerRadius)
                )
            )
        }
    }

    /**
     * 绘制时钟刻度
     */
    private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawClockTicks(
        radius: Float,
        center: Offset,
        metrics: ClockMetrics,
        textPaint: Paint
    ) {
        val totalTicks = 60
        for (i in 0 until totalTicks) {
            val angle = i * (360f / totalTicks) // 每个小格 6 度
            val isHour = i % 5 == 0 // 每 5 小格是一个小时刻度

            val lineLen = if (isHour) metrics.bigLine else metrics.normalLine

            rotate(degrees = angle, pivot = center) {
                drawLine(
                    color = if (isHour) Color.Black else Color.LightGray,
                    start = Offset(center.x, center.y - radius + metrics.offsetTop),
                    end = Offset(center.x, center.y - radius + metrics.offsetTop + lineLen),
                    strokeWidth = metrics.strokeW
                )
            }

            // 画数字
            if (isHour) {
                val angleRad = Math.toRadians((angle - 90).toDouble())
                val x = center.x + (radius - metrics.numPadding) * cos(angleRad).toFloat()
                val y = center.y + (radius - metrics.numPadding) * sin(angleRad).toFloat()

                val hourText = if (i == 0) "12" else (i / 5).toString()
                drawContext.canvas.nativeCanvas.drawText(hourText, x, y + 10f, textPaint)
            }
        }
    }

    /**
     * 绘制时针、分针、秒针
     */
    private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHands(
        center: Offset,
        hourAngle: Float,
        minuteAngle: Float,
        secondAngle: Float,
        hourHandPath: Path,
        minuteHandPath: Path,
        metrics: ClockMetrics,
        radius: Float,
        density: Density
    ) {
        // 绘制时针
        rotate(degrees = hourAngle, pivot = center) {
            withTransform({
                translate(left = center.x, top = center.y)
            }) {
                // 创建实心遮挡层（不包含空心部分）
                val solidPath = Path().apply {
                    fillType = PathFillType.NonZero
                    val widthBase = 5.dp.toPx(density)
                    val totalLength = radius * 0.55f
                    val cornerRadius = 2.dp.toPx(density)
                    val startY = -metrics.ringRadius

                    moveTo(-widthBase / 2f, startY)
                    lineTo(-widthBase / 2f, -totalLength + cornerRadius)
                    arcTo(
                        rect = Rect(
                            left = -widthBase / 2,
                            top = -totalLength,
                            right = widthBase / 2,
                            bottom = -totalLength + cornerRadius * 2
                        ),
                        startAngleDegrees = 180f,
                        sweepAngleDegrees = 180f,
                        forceMoveTo = false
                    )
                    lineTo(widthBase / 2f, startY)
                    close()
                }

                // 先绘制实心背景遮挡数字
                drawPath(path = solidPath, color = Color.White)
                // 再绘制实际的空心指针
                drawPath(path = hourHandPath, color = Color.Black)
            }
        }

        // 绘制分针
        rotate(degrees = minuteAngle, pivot = center) {
            withTransform({
                translate(left = center.x, top = center.y)
            }) {
                // 创建实心遮挡层（不包含空心部分）
                val solidPath = Path().apply {
                    fillType = PathFillType.NonZero
                    val widthBase = 4.dp.toPx(density)
                    val totalLength = radius * 0.8f
                    val cornerRadius = 1.5.dp.toPx(density)
                    val startY = -metrics.ringRadius

                    moveTo(-widthBase / 2f, startY)
                    lineTo(-widthBase / 2f, -totalLength + cornerRadius)
                    arcTo(
                        rect = Rect(
                            left = -widthBase / 2f,
                            top = -totalLength,
                            right = widthBase / 2f,
                            bottom = -totalLength + cornerRadius * 2f
                        ),
                        startAngleDegrees = 180f,
                        sweepAngleDegrees = 180f,
                        forceMoveTo = false
                    )
                    lineTo(widthBase / 2f, startY)
                    close()
                }

                // 先绘制实心背景遮挡数字
                drawPath(path = solidPath, color = Color.White)
                // 再绘制实际的空心指针
                drawPath(path = minuteHandPath, color = Color.Black)
            }
        }

        // 画蓝色秒针
        rotate(degrees = secondAngle, pivot = center) {
            // 针尖
            drawLine(
                color = Color(0xFF3169EC),
                start = Offset(center.x, center.y - metrics.needleGap),
                end = Offset(center.x, center.y - radius + 10.dp.toPx()),
                strokeWidth = metrics.needleWidth,
                cap = StrokeCap.Round
            )
            // 针尾
            drawLine(
                color = Color(0xFF3169EC),
                start = Offset(center.x, center.y + metrics.needleGap),
                end = Offset(center.x, center.y + metrics.needleTail),
                strokeWidth = metrics.needleWidth,
                cap = StrokeCap.Round
            )
        }

        // 画轴心圆环
        drawCircle(
            color = Color(0xFF2979FF),
            radius = metrics.ringRadius,
            center = center,
            style = Stroke(width = metrics.ringStroke)
        )
    }

    private fun Dp.toPx(density: Density) = with(density) { this@toPx.toPx() }
}