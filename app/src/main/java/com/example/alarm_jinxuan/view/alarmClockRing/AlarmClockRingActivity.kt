package com.example.alarm_jinxuan.view.alarmClockRing

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.alarm_jinxuan.R
import com.example.alarm_jinxuan.databinding.ActivityAlarmClockRingBinding
import com.example.alarm_jinxuan.model.AddAlarmClockManager
import com.example.alarm_jinxuan.model.RingtoneOption
import com.example.alarm_jinxuan.view.vibration.VibrationActivity

class AlarmClockRingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAlarmClockRingBinding

    // 播放器
    private var mediaPlayer: MediaPlayer? = null

    private lateinit var ringtoneOptionAdapter: RingtoneAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAlarmClockRingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener {
            finish()
        }

        val ringtoneList = generateRingtoneList()

        ringtoneOptionAdapter = RingtoneAdapter(ringtoneList) { onItemSelected ->
            // 播放音乐
            startRingtonePreview(onItemSelected.resId)

            AddAlarmClockManager.tempRingtoneId = onItemSelected.id
            AddAlarmClockManager.tempRingtoneName = onItemSelected.name
        }

        binding.listRingtone.apply {
            adapter = ringtoneOptionAdapter
        }

        binding.itemVibration.setOnClickListener {
            val intent = Intent(this, VibrationActivity::class.java)
            startActivity(intent)
        }

    }

    /**
     * 核心播放方法：传入 raw 资源 ID 即可响起来
     */
    private fun startRingtonePreview(resId: Int) {
        try {
            // 彻底清理上一个播放器（这是防卡顿、防重叠的关键）
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
            mediaPlayer = null

            mediaPlayer = MediaPlayer.create(this, resId).apply {
                // 开启循环
                isLooping = true

                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun generateRingtoneList(): List<RingtoneOption> {
        val list = mutableListOf<RingtoneOption>()

        // 1. 获取 raw 文件夹下所有的资源 ID
        // 我们通过反射拿到 R.raw 类里所有的变量
        val fields = R.raw::class.java.fields

        var idCounter = 0
        for (field in fields) {
            val fileName = field.name // 比如 "alarm_awakening"

            // 2. 只过滤出以 "alarm_" 开头的文件
            if (fileName.startsWith("alarm_")) {
                try {
                    val resId = field.getInt(null)

                    // 3. 将文件名转成好看的显示名称
                    // 比如 "alarm_morning_light" -> "Morning Light"
                    val displayName = fileName
                        .replace("alarm_", "")
                        .replace("_", " ")
                        .replaceFirstChar { it.uppercase() }

                    list.add(RingtoneOption(idCounter++, displayName, resId))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // 建议排个序，不然反射出来的顺序可能是乱的
        return list.sortedBy { it.name }
    }

    override fun onResume() {
        super.onResume()
        binding.tvVibration.text = AddAlarmClockManager.tempVibrationName
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        mediaPlayer = null
    }

}