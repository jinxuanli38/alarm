package com.example.alarm_jinxuan.view.worldClock

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.fragment.app.Fragment
import com.example.alarm_jinxuan.databinding.FragmentWorldClockBinding

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

            }
        }
    }

    @Composable
    private fun worldClockScreen() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}