package com.example.alarm_jinxuan.view.worldClock

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.fragment.app.Fragment
import com.example.alarm_jinxuan.databinding.FragmentWorldClockBinding
import com.example.alarm_jinxuan.utils.SharedClockComponents
import com.example.alarm_jinxuan.view.addAlarm.AddAlarmActivity
import com.example.alarm_jinxuan.view.addCity.AddCityActivity

class WorldClockFragment : Fragment() {
    private var _binding: FragmentWorldClockBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorldClockBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeView.setContent {
            MaterialTheme {
                worldClockScreen()
            }
        }

        // 添加世界时间
        binding.add.setOnClickListener {
            val intent = Intent(requireContext(), AddCityActivity::class.java)
            startActivity(intent)
        }
    }

    @Composable
    private fun worldClockScreen() {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            // 使用共享的闹钟组件
            SharedClockComponents.ClockView()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}