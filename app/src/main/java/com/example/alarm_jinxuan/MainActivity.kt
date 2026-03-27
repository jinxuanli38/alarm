package com.example.alarm_jinxuan

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.alarm_jinxuan.databinding.ActivityMainBinding
import com.example.alarm_jinxuan.repository.AlarmRepository
import com.example.alarm_jinxuan.view.alarm.AlarmFragment
import com.example.alarm_jinxuan.view.stopWatch.StopWatchFragment
import com.example.alarm_jinxuan.view.timer.TimerFragment
import com.example.alarm_jinxuan.view.worldClock.WorldClockFragment
import com.example.alarm_jinxuan.utils.PermissionUtils

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val tagAlarm = "alarm"
    private val tagWorldClock = "worldClock"
    private val tagStopWatch = "stopWatch"
    private val tagTimer = "timer"
    private var currentFragment = tagAlarm

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AlarmRepository.init(this)

        // 检查并请求闹钟相关权限
        PermissionUtils.checkAndRequestPermissions(this) { allGranted ->
            // 可以在这里添加权限授予后的处理逻辑
            if (!allGranted) {
                // 用户没有授予权限，可以记录日志或显示提示
                Toast.makeText(this,"相关权限并未全部开启，会影响后续闹钟体验", Toast.LENGTH_SHORT)
            }
        }

        // 默认显示首页
        if (savedInstanceState == null) {
            switchFragment(tagAlarm)
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

        transaction.commit()
    }

}