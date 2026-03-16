package com.example.alarm_jinxuan.view.stopWatch

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.alarm_jinxuan.databinding.FragmentStopWatchBinding
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

class StopWatchFragment : Fragment() {

    private var _binding : FragmentStopWatchBinding?= null

    private val binding get() = _binding!!

    private val viewModel : StopWatchViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStopWatchBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.formattedTime.observe(viewLifecycleOwner) { timeString ->
            binding.stopWatch.text = timeString
        }

        viewModel.formattedInterval.observe(viewLifecycleOwner) { value ->
            binding.breakPoint.text = value
        }

        viewModel.firstInterval.observe(viewLifecycleOwner) { value ->
            if (value) {
                binding.breakPoint.visibility = View.VISIBLE
            } else {
                binding.breakPoint.visibility = View.INVISIBLE
            }
        }

        val lapAdapter = LapAdapter()

        binding.laps.apply {
            adapter = lapAdapter
            setHasFixedSize(true)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.laps.collect { newList ->
                    lapAdapter.submitList(newList) {
                        binding.laps.scrollToPosition(0)
                    }
                }
            }
        }

        binding.composeView.apply {
            setContent {
                MaterialTheme {
                    stopWatchScreen()
                }
            }
        }
    }

    @Composable
    private fun stopWatchScreen() {
        // 其实就是填满整个盒子以及居中排列
        Box(
            modifier = Modifier.Companion.fillMaxSize(),
            contentAlignment = Alignment.Companion.Center
        ) {
            stopwatchDial()
        }
    }

    @Composable
    private fun stopwatchDial() {
        // 观察数据
        val isRunning by viewModel.isRunning.collectAsState()
        val elapsedNanos by viewModel.elapsedNanos.collectAsState()

        // 计时所用的协程
        LaunchedEffect(isRunning) {
            if (isRunning) {
                var lastFrameTime = System.nanoTime()
                while (isRunning) {
                    withFrameNanos { frameTimeNanos ->
                        val diff = frameTimeNanos - lastFrameTime
                        viewModel.addNanos(diff)
                        lastFrameTime = frameTimeNanos
                    }
                }
            }
        }

        // 当前的指针角度
        val currentAngel = (elapsedNanos / 1_000_000_000f) * 6f

        // 时钟的时间字体表示
        val textPaint = remember {
            Paint().apply {
                color = Color.BLACK
                textSize = 48f
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
                typeface = Typeface.create("宋体", Typeface.NORMAL)
            }
        }

        val density = LocalDensity.current

        val metrics = remember(density) {
            object {
                val numPadding = 40.dp.toPx(density)
                val bigLine = 15.dp.toPx(density)
                val majorLine = 10.dp.toPx(density)
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
        }

        Canvas(modifier = Modifier.Companion.size(300.dp)) {
            val center = size.center
            val radius = size.width / 2

            for (i in 0 until 300) {
                val angle = i * 1.2f
                val isMajor = i % 5 == 0
                val isBigMajor = i % 25 == 0

                val lineLen = when {
                    isBigMajor -> metrics.bigLine
                    isMajor -> metrics.majorLine
                    else -> metrics.normalLine
                }

                rotate(degrees = angle, pivot = center) {
                    drawLine(
                        color = if (isBigMajor) androidx.compose.ui.graphics.Color.Companion.Black else androidx.compose.ui.graphics.Color.Companion.LightGray,
                        start = Offset(center.x, center.y - radius + metrics.offsetTop),
                        end = Offset(center.x, center.y - radius + metrics.offsetTop + lineLen),
                        strokeWidth = metrics.strokeW
                    )
                }

                if (isBigMajor) {
                    val angleRad = Math.toRadians((angle - 90).toDouble())
                    val x = center.x + (radius - metrics.numPadding) * cos(angleRad).toFloat()
                    val y = center.y + (radius - metrics.numPadding) * sin(angleRad).toFloat()
                    val secondText = if (i == 0) "60" else (i / 5).toString()
                    drawContext.canvas.nativeCanvas.drawText(secondText, x, y + 12f, textPaint)
                }
            }

            // 1. 画蓝色秒针
            rotate(degrees = currentAngel, pivot = center) {
                // 针尖
                drawLine(
                    color = androidx.compose.ui.graphics.Color(0xFF3169EC),
                    start = Offset(center.x, center.y - metrics.needleGap),
                    end = Offset(center.x, center.y - radius + 10.dp.toPx()),
                    strokeWidth = metrics.needleWidth,
                    cap = StrokeCap.Companion.Round
                )
                // 针尾
                drawLine(
                    color = androidx.compose.ui.graphics.Color(0xFF3169EC),
                    start = Offset(center.x, center.y + metrics.needleGap),
                    end = Offset(center.x, center.y + metrics.needleTail),
                    strokeWidth = metrics.needleWidth,
                    cap = StrokeCap.Companion.Round
                )
            }

            // 2. 画轴心圆环
            drawCircle(
                color = androidx.compose.ui.graphics.Color(0xFF2979FF),
                radius = metrics.ringRadius,
                center = center,
                style = Stroke(width = metrics.ringStroke)
            )
        }
    }

    private fun Dp.toPx(density: Density) = with(density) { this@toPx.toPx() }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}