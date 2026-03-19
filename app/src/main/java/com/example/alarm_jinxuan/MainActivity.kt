package com.example.alarm_jinxuan

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.alarm_jinxuan.databinding.ActivityMainBinding
import com.example.alarm_jinxuan.view.alarm.AlarmFragment
import com.example.alarm_jinxuan.view.stopWatch.StopWatchFragment
import com.example.alarm_jinxuan.view.timer.TimerFragment
import com.example.alarm_jinxuan.view.worldClock.WorldClockFragment
import com.example.alarm_jinxuan.utils.GlideUtil
import com.example.alarm_jinxuan.view.addAlarm.AddAlarmActivity
import com.example.alarm_jinxuan.view.addCity.AddCityActivity
import com.example.alarm_jinxuan.view.stopWatch.StopWatchViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val tagAlarm = "alarm"
    private val tagWorldClock = "worldClock"
    private val tagStopWatch = "stopWatch"
    private val tagTimer = "timer"
    private var currentFragment = tagAlarm
    private val stopWatchViewModel : StopWatchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 默认显示首页
        if (savedInstanceState == null) {
            switchFragment(tagAlarm)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                stopWatchViewModel.isRunning.collect { running ->
                    if (running) {
                        if (currentFragment == tagStopWatch) {
                            GlideUtil.createGlideUtil().loadImage(this@MainActivity,R.drawable.ic_pause,binding.add)
                        }
                        binding.btnLeftStopWatch.apply {
                            alpha = 0.5f
                            isEnabled = false
                        }
                        binding.btnRightStopWatch.apply {
                            alpha = 1f
                            isEnabled = true
                        }
                        // 秒表在跑的期间就可以计表了，但是跑的期间不能重置
                    } else {
                        if (currentFragment == tagStopWatch) {
                            GlideUtil.createGlideUtil().loadImage(this@MainActivity,R.drawable.ic_begin,binding.add)
                        }
                        // 暂停状态下可以进行重置时间了
                        binding.btnLeftStopWatch.apply {
                            alpha = 1f
                            isEnabled = true
                        }
                        binding.btnRightStopWatch.apply {
                            alpha = 0.4f
                            isEnabled = false
                        }
                    }
                }
            }
        }

        binding.btnLeftStopWatch.setOnClickListener {
            // 重置时间
            stopWatchViewModel.reset()
            binding.btnLeftStopWatch.apply {
                alpha = 0.5f
                isEnabled = false
            }
            // 删除所有快记时间
            stopWatchViewModel.deleteLapRecord()
            // 将间隔时间清零重来
            stopWatchViewModel.intervalReset()
            // 间隔时间不显示
            stopWatchViewModel.firstInterval.value = false
        }

        binding.btnRightStopWatch.setOnClickListener {
            // 添加快记数据列表
            stopWatchViewModel.addLap()
            // 将间隔时间清零重来
            stopWatchViewModel.intervalReset()
            // 显示间隔时间
            stopWatchViewModel.firstInterval.value = true
        }

        binding.navFooter.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.alarm_ic -> switchFragment(tagAlarm)
                R.id.world_clock -> switchFragment(tagWorldClock)
                R.id.stop_watch -> switchFragment(tagStopWatch)
                R.id.timer -> switchFragment(tagTimer)
            }
            true
        }

        binding.add.setOnClickListener {
            if (currentFragment == tagAlarm) {
                val intent = Intent(this, AddAlarmActivity::class.java)
                startActivity(intent)
            } else if (currentFragment == tagWorldClock) {
                val intent = Intent(this, AddCityActivity::class.java)
                startActivity(intent)
            } else if (currentFragment == tagStopWatch) {
                stopWatchViewModel.toggle()
            } else {

            }
        }

    }

    private fun switchFragment(tag: String) {
        val transaction = supportFragmentManager.beginTransaction()

        // 寻找是否已经创建过这个 Fragment
        var fragment = supportFragmentManager.findFragmentByTag(tag)

        // 隐藏当前正在显示的 Fragment
        val currentFragment = supportFragmentManager.fragments.find { it.isVisible }
        currentFragment?.let { transaction.hide(it) }

        if (fragment == null) {
            // 如果没创建过，才根据 tag 创建实例
            fragment = when (tag) {
                tagAlarm -> {
                    AlarmFragment()
                }

                tagWorldClock -> {
                    WorldClockFragment()
                }

                tagStopWatch -> {
                    StopWatchFragment()
                }

                tagTimer -> {
                    TimerFragment()
                }

                else -> {
                    AlarmFragment()
                }
            }
            transaction.add(R.id.main, fragment, tag)
        } else {
            // 4. 如果创建过了，直接显示
            transaction.show(fragment)
        }

        this@MainActivity.currentFragment = tag
        updateUIAnimate(tag)

        transaction.commit()
    }

    private fun updateUIAnimate(tag: String) {
        when (tag) {
            tagAlarm -> toggleFab(true, isTimer = false)
            tagWorldClock -> toggleFab(true, isTimer = false)
            tagStopWatch -> toggleFab(false, isTimer = false)
            tagTimer -> toggleFab(false, isTimer = true)
        }
    }

    private fun toggleFab(isExpanded: Boolean, isTimer: Boolean) {
        val distance = 250f // 弹出距离，根据实际效果调整

        if (!isExpanded) {
            // 弹出动画
            GlideUtil.createGlideUtil().loadImage(this,R.drawable.ic_begin,binding.add)

            binding.btnLeftStopWatch.animate()
                .translationX(-distance)
                .alpha(0.5f)
                .setDuration(300)
                .start()

            if (isTimer) {
                binding.btnRightTimer.visibility = View.VISIBLE
                binding.btnRightTimer.animate()
                    .translationX(distance)
                    .alpha(1f)
                    .setDuration(300)
                    .start()

                binding.btnRightStopWatch.animate()
                    .translationX(distance)
                    .alpha(0f)
                    .setDuration(300)
                    .start()

                binding.btnRightStopWatch.visibility = View.GONE

                binding.add.imageAlpha = 127
            } else {
                binding.btnRightTimer.animate()
                    .translationX(distance)
                    .alpha(0f)
                    .setDuration(300)
                    .start()
                binding.btnRightTimer.visibility = View.GONE

                binding.btnRightStopWatch.visibility = View.VISIBLE

                binding.btnRightStopWatch.animate()
                    .translationX(distance)
                    .alpha(0.4f)
                    .setDuration(300)
                    .start()

                binding.add.imageAlpha = 255
            }
        } else {
            // 收回动画
            GlideUtil.createGlideUtil().loadImage(this,R.drawable.ic_add,binding.add)

            binding.add.imageAlpha = 255

            binding.btnLeftStopWatch.animate()
                .translationX(0f)
                .alpha(0f)
                .setDuration(300)
                .start()

            binding.btnRightStopWatch.animate()
                .translationX(0f)
                .alpha(0f)
                .setDuration(300)
                .start()

            binding.btnRightTimer.animate()
                .translationX(0f)
                .alpha(0f)
                .setDuration(300)
                .start()

            binding.btnRightStopWatch.animate()
                .translationX(0f)
                .alpha(0f)
                .setDuration(300)
                .start()

        }
    }
}