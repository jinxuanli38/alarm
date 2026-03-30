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
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.Permission
import com.hjq.permissions.OnPermissionCallback

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

        // 使用 XXPermissions 申请悬浮窗和通知权限
        requestXXPermissions()

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

    /**
     * 使用 XXPermissions 申请权限
     */
    private fun requestXXPermissions() {
        XXPermissions.with(this)
            .permission(
                Permission.POST_NOTIFICATIONS,      // 通知权限 (Android 13+)
                Permission.SYSTEM_ALERT_WINDOW       // 悬浮窗权限
            )
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, all: Boolean) {
                    if (all) {
                        Toast.makeText(this@MainActivity, "权限申请成功", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MainActivity, "部分权限未授予，可能影响功能", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onDenied(permissions: MutableList<String>, never: Boolean) {
                    if (never) {
                        Toast.makeText(this@MainActivity, "权限被永久拒绝，请去设置中开启", Toast.LENGTH_LONG).show()
                        // 如果是被永久拒绝就跳转到应用设置页面
                        XXPermissions.startPermissionActivity(this@MainActivity, permissions)
                    } else {
                        Toast.makeText(this@MainActivity, "权限被拒绝", Toast.LENGTH_SHORT).show()
                    }
                }
            })
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
