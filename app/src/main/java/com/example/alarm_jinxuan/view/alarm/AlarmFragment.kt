package com.example.alarm_jinxuan.view.alarm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.alarm_jinxuan.databinding.FragmentAlarmBinding
import com.example.alarm_jinxuan.model.AlarmEntity
import com.example.alarm_jinxuan.utils.SharedClockComponents
import com.example.alarm_jinxuan.view.addAlarm.AddAlarmViewModel
import kotlinx.coroutines.launch

class AlarmFragment : Fragment() {

    private var _binding: FragmentAlarmBinding? = null

    private val binding get() = _binding!!

    private val viewModel: AddAlarmViewModel by viewModels()

    private lateinit var alarmAdapter: AlarmAdapter

    private var lastToastTime = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlarmBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 描绘闹钟相关UI
        binding.composeView.setContent {
            MaterialTheme {
                drawAlarm()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.allAlarms.collect { alarmLists ->
                        updateUI(alarmLists)
                    }
                }

                launch {
                    // 设置闹钟倒计时
                    viewModel.alarmCountdownFlow.collect { str ->
                        binding.ringTime.text = str
                    }
                }

            }
        }
    }

    private fun showSmartToast(message: String) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastToastTime > 2000) { // 2秒间隔
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            lastToastTime = currentTime
        }
    }

    @Composable
    private fun drawAlarm() {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            // 使用共享的闹钟组件
            SharedClockComponents.ClockView()
        }
    }

    private fun updateUI(alarmLists: List<AlarmEntity>) {
        // 如果列表为空则显示对应UI
        if (alarmLists.isEmpty()) {
            // 隐藏recyclerview和相应时间
            binding.ringTime.visibility = View.GONE
            binding.clockRingTime.visibility = View.GONE

            binding.emptyView.visibility = View.VISIBLE
        } else {
            binding.emptyView.visibility = View.GONE

            binding.ringTime.visibility = View.VISIBLE
            binding.clockRingTime.visibility = View.VISIBLE
            // 设置适配器
            alarmAdapter = AlarmAdapter(
                onClick = { alarm ->
                    // Todo
                },
                onToggle = { alarm, isEnabled ->
                    viewModel.updateAlarmEnabled(alarm.id, isEnabled)

                    if (isEnabled) {
                        val triggerTime = viewModel.calculateNextTriggerTime(alarm)
                        val (h, m) = viewModel.getRemainingTime(triggerTime)
                        // 弹窗提示用户
                        showSmartToast("$h 小时 $m 分钟后响铃")
                    }
                }
            )

            alarmAdapter.submitList(alarmLists)

            binding.clockRingTime.apply {
                adapter = alarmAdapter
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}