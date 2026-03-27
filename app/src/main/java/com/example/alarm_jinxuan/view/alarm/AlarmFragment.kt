package com.example.alarm_jinxuan.view.alarm

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
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
import com.example.alarm_jinxuan.utils.AlarmManagerUtils
import com.example.alarm_jinxuan.utils.PermissionUtils
import com.example.alarm_jinxuan.utils.SharedClockComponents
import com.example.alarm_jinxuan.utils.StringUtils
import com.example.alarm_jinxuan.view.addAlarm.AddAlarmActivity
import com.example.alarm_jinxuan.view.addAlarm.AddAlarmViewModel
import com.example.alarm_jinxuan.view.addCity.AddCityActivity
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

        binding.edit.setOnClickListener { view ->
            // 1. 实例化 PopupMenu，绑定在当前点击的 view 上
            val popup = PopupMenu(requireContext(), view)

            // 2. 动态添加菜单项：参数分别为 (组ID, 项目ID, 排序, 标题文本)
            popup.menu.add(0, 0, 0, "跳转“自启动”")

            // 3. 处理菜单点击事件
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    0 -> {
                        // --- 这里放入你写好的 Intent 跳转逻辑 ---
                        startAutoStartActivity()
                        true
                    }
                    else -> false
                }
            }

            // 4. 展示弹窗
            popup.show()
        }

        // 添加闹钟
        binding.add.setOnClickListener {
            val intent = Intent(requireContext(), AddAlarmActivity::class.java)
            startActivity(intent)
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

    private fun startAutoStartActivity() {
        PermissionUtils.openHonorPowerDetail(requireContext())
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
                    val intent = Intent(requireContext(), AddAlarmActivity::class.java).apply {
                        putExtra("ALARM_ID",alarm.id)
                    }
                    startActivity(intent)
                },
                onToggle = { alarm, isEnabled ->
                    viewModel.updateAlarmEnabled(alarm, isEnabled)

                    if (isEnabled) {
                        val triggerTime = AlarmManagerUtils.calculateNextTriggerTime(alarm)
                        val (d, h, m) = AlarmManagerUtils.getRemainingTime(triggerTime)
                        // 弹窗提示用户
                        val time = StringUtils.formatRemainingTime(d,h,m)
                        showSmartToast(time)
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