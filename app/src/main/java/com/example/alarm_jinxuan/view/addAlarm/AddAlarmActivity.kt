package com.example.alarm_jinxuan.view.addAlarm

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.alarm_jinxuan.databinding.ActivityAddAlarmBinding
import com.example.alarm_jinxuan.databinding.LayoutAlarmDurationBinding
import com.example.alarm_jinxuan.databinding.LayoutAlarmNameDialogBinding
import com.example.alarm_jinxuan.databinding.LayoutConfirmDialogBinding
import com.example.alarm_jinxuan.databinding.LayoutIntervalDialogBinding
import com.example.alarm_jinxuan.databinding.LayoutRepeatDialogBinding
import com.example.alarm_jinxuan.model.AddAlarmClockManager
import com.example.alarm_jinxuan.model.AlarmEntity
import com.example.alarm_jinxuan.model.DurationOption
import com.example.alarm_jinxuan.model.RepeatDay
import com.example.alarm_jinxuan.view.alarmClockRing.AlarmClockRingActivity
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.getValue

class AddAlarmActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddAlarmBinding
    private lateinit var repeatAdapter: RepeatAdapter
    private val viewModel: AddAlarmViewModel by viewModels()

    private val dataList = listOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")
        .mapIndexed { index, name -> RepeatDay(index, name) }

    private val minutesList = listOf(1, 5, 10, 15, 20, 30)

    private val durationData = minutesList.mapIndexed { index, min ->
        DurationOption(
            id = index,
            minute = min,
            label = "$min 分钟",
            isSelected = (min == 5)
        )
    }

    // 1. 上午/下午 数据
    val periodData = listOf("上午", "下午")

    // 2. 小时数据 (1-12)
    val hourData = (1..12).map { String.format("%02d", it) }

    // 3. 分钟数据 (00-59)
    val minuteData = (0..59).map { String.format("%02d", it) }

    // 1. 小时数据 (1..12) 的 Int 集合
    val hourDataInt = (1..12).toList()

    // 2. 分钟数据 (0..59) 的 Int 集合
    val minuteDataInt = (0..59).toList()

    // 当前的响铃时长
    private var currentRingMinute = 5

    // 当前的响铃间隔时间（分钟）
    private var intervalRingValue = 10

    // 当前的重复响铃次数
    private var repeatRingValue = 3

    // 是否发生了修改
    private var isUpdateBool = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAddAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.wheelPeriod.apply {
            data = periodData
        }
        binding.wheelHour.apply {
            data = hourData
        }
        binding.wheelMinute.apply {
            data = minuteData
        }

        // 更新当前选择时间
        selectWheel()

        // 修改星期几
        binding.itemRepeat.setOnClickListener { repeatDialog() }

        // 修改闹钟名弹窗
        binding.itemLabel.setOnClickListener { updateAlarmName() }

        // 响铃时长弹窗
        binding.itemRingDuration.setOnClickListener {
            selectedDuration()
        }

        // 打开再响间隔
        binding.itemBeepInterval.setOnClickListener {
            slideBeepInterval()
        }

        // 跳转到闹钟铃声
        binding.itemRingtone.setOnClickListener {
            isUpdateBool = true
            val intent = Intent(this, AlarmClockRingActivity::class.java)
            startActivity(intent)
        }

        // 返回弹窗
        binding.back.setOnClickListener {
            if (isUpdateBool) {
                popConfirmDialog()
            } else {
                finish()
            }
        }

        // 保存数据
        binding.success.setOnClickListener {
            save()
        }
    }

    private fun selectWheel() {
        val calendar = Calendar.getInstance()
        // 获取上午、下午
        val amPm = calendar.get(Calendar.AM_PM)
        binding.wheelPeriod.selectedItemPosition = amPm
        // 获取12小时制的时间
        var hour12 = calendar.get(Calendar.HOUR)
        // 0点换成12点
        if (hour12 == 0) hour12 = 12

        binding.wheelHour.selectedItemPosition = hour12 - 1

        val minute = calendar.get(Calendar.MINUTE)
        binding.wheelMinute.selectedItemPosition = minute
    }

    /**
     * 星期几重复弹窗
     */
    private fun repeatDialog() {
        isUpdateBool = true
        val dialog = Dialog(this)

        // 2. 拿到你的 ViewBinding
        val dialogBinding = LayoutRepeatDialogBinding.inflate(layoutInflater)

        // 3. 把布局塞进去
        dialog.setContentView(dialogBinding.root)

        dialog.window?.apply {
            // 让背景透明（这样你的 CardView 圆角才能露出来）
            setBackgroundDrawableResource(android.R.color.transparent)

            // 设置弹窗的位置和宽度
            attributes?.apply {
                gravity = Gravity.BOTTOM // 贴在底部（如果你想居中就用 Gravity.CENTER）
                width = WindowManager.LayoutParams.MATCH_PARENT // 宽度撑满
                height = WindowManager.LayoutParams.WRAP_CONTENT // 高度自适应
            }
        }
        dialog.setCanceledOnTouchOutside(false)

        repeatAdapter = RepeatAdapter(dataList) { position ->
            dataList[position].isChecked = !dataList[position].isChecked

            repeatAdapter.notifyItemChanged(position)

            // 全部选择需要更改为每天
            if (dataList.all { it.isChecked }) {
                binding.tvRepeatValue.text = "每天"
            }
        }

        dialogBinding.rvRepeat.apply {
            adapter = repeatAdapter
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnConfirm.setOnClickListener {
            val selectedResult = dataList.filter { it.isChecked }.joinToString(" ") { it.name }
            binding.tvRepeatValue.text = selectedResult.ifEmpty { "不重复" }

            dialog.dismiss()
        }
        dialog.show()

    }

    /**
     * 闹钟名弹窗
     */
    private fun updateAlarmName() {
        isUpdateBool = true
        val dialog = Dialog(this)

        val dialogBinding = LayoutAlarmNameDialogBinding.inflate(layoutInflater)

        dialog.setContentView(dialogBinding.root)

        dialog.window?.apply {
            // 让背景透明（这样你的 CardView 圆角才能露出来）
            setBackgroundDrawableResource(android.R.color.transparent)

            // 设置弹窗的位置和宽度
            attributes?.apply {
                gravity = Gravity.BOTTOM // 贴在底部（如果你想居中就用 Gravity.CENTER）
                width = WindowManager.LayoutParams.MATCH_PARENT // 宽度撑满
                height = WindowManager.LayoutParams.WRAP_CONTENT // 高度自适应
            }
        }
        dialog.setCanceledOnTouchOutside(false)

        // 假设你的 EditText id 是 input
        dialogBinding.input.apply {
            // 1. 获取焦点（锁定光标）
            requestFocus()

            setText(binding.alarmName.text)
            // 2. 全选已有内容
            // 注意：必须先 setText 再 setSelection，或者直接调用 selectAll()
            selectAll()

            // 3. 弹出软键盘
            // 这是一个经典坑：有时候 View 还没贴到窗口上，键盘弹不出来，所以稍微推迟一点点
            postDelayed({
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            }, 200) // 延迟 200 毫秒最稳
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnConfirm.setOnClickListener {
            binding.alarmName.text = dialogBinding.input.text
            dialog.dismiss()
        }

        dialog.show()
    }

    /**
     * 响铃时长弹窗
     */
    private fun selectedDuration() {
        isUpdateBool = true
        val dialog = Dialog(this)

        val dialogBinding = LayoutAlarmDurationBinding.inflate(layoutInflater)

        dialog.setContentView(dialogBinding.root)

        dialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)

            attributes?.apply {
                gravity = Gravity.BOTTOM // 贴在底部（如果你想居中就用 Gravity.CENTER）
                width = WindowManager.LayoutParams.MATCH_PARENT // 宽度撑满
                height = WindowManager.LayoutParams.WRAP_CONTENT // 高度自适应
            }
        }
        dialog.setCanceledOnTouchOutside(false)

        val durationAdapter = DurationAdapter(durationData) { selectedOption ->
            binding.textDuration.text = selectedOption.label

            // 更改当前响铃时长
            currentRingMinute = selectedOption.minute

            dialog.dismiss()
        }

        dialogBinding.rvRepeat.apply {
            adapter = durationAdapter
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    /**
     * 再响间隔弹窗
     */
    private fun slideBeepInterval() {
        isUpdateBool = true
        val dialog = Dialog(this)

        val dialogBinding = LayoutIntervalDialogBinding.inflate(layoutInflater)

        dialog.setContentView(dialogBinding.root)

        dialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)

            attributes?.apply {
                gravity = Gravity.BOTTOM // 贴在底部（如果你想居中就用 Gravity.CENTER）
                width = WindowManager.LayoutParams.MATCH_PARENT // 宽度撑满
                height = WindowManager.LayoutParams.WRAP_CONTENT // 高度自适应
            }
        }
        dialog.setCanceledOnTouchOutside(false)
        // 沿用上次选择数据
        dialogBinding.sliderInterval.value = intervalRingValue.toFloat()
        dialogBinding.sliderRepeat.value = repeatRingValue.toFloat()

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnConfirm.setOnClickListener {
            intervalRingValue = dialogBinding.sliderInterval.value.toInt()
            repeatRingValue = dialogBinding.sliderRepeat.value.toInt()

            // 进行格式化展示
            val result = String.format("%d 分钟，%d 次", intervalRingValue, repeatRingValue)
            binding.beepIntervalValue.text = result

            dialog.dismiss()
        }

        dialog.show()
    }

    // 确认是否保存的弹窗
    private fun popConfirmDialog() {
        val dialog = Dialog(this)

        val dialogBinding = LayoutConfirmDialogBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)

            attributes?.apply {
                gravity = Gravity.BOTTOM // 贴在底部（如果你想居中就用 Gravity.CENTER）
                width = WindowManager.LayoutParams.MATCH_PARENT // 宽度撑满
                height = WindowManager.LayoutParams.WRAP_CONTENT // 高度自适应
            }
        }
        dialog.setCanceledOnTouchOutside(false)

        // 放弃保存
        dialogBinding.btnCancel.setOnClickListener {
            // 必须恢复初始状态
            AddAlarmClockManager.clear()

            dialog.dismiss()
            finish()
        }

        // 确认保存
        dialogBinding.btnConfirm.setOnClickListener {
            AddAlarmClockManager.clear()

            dialog.dismiss()
            // 同时需要保存到数据库
            save()
        }

        dialog.show()
    }

    /**
     * 保存到数据库
     */
    private fun save() {
        // 先整理时间
        val periodPosition = binding.wheelPeriod.selectedItemPosition
        val period = periodData[periodPosition]
        val hourPosition = binding.wheelHour.selectedItemPosition
        val hour = hourDataInt[hourPosition]
        val minutePosition = binding.wheelMinute.selectedItemPosition
        val minute = minuteDataInt[minutePosition]

        // 整理时间称呼的同时完成24小时转换
        val (displayPeriod, h24) = if (period == "上午") {
            when (hour) {
                12 -> "半夜" to 0
                in 1..4 -> "凌晨" to hour
                in 5..6 -> "清晨" to hour
                in 7..8 -> "早上" to hour
                else -> "上午" to hour
            }
        } else {
            when (hour) {
                12 -> "中午" to 12
                in 1..4 -> "下午" to hour + 12
                in 5..6 -> "傍晚" to hour + 12
                in 7..10 -> "晚上" to hour + 12
                else -> "半夜" to 23
            }
        }
        // 整理星期
        val repeatDataString = dataList.joinToString(",") { if (it.isChecked) "1" else "0" }
        // 整理显示文字
        val checkedNames = dataList.filter { it.isChecked }.map { it.name }
        val repeatText = when {
            checkedNames.size == 7 -> "每天"
            checkedNames.isEmpty() -> "不重复"
            else -> checkedNames.joinToString(", ")
        }
        // 整理对象
        val newAlarm = AlarmEntity(
            period = displayPeriod,
            hour = hour,
            minute = minute,
            hour24 = h24,
            isEnabled = true, // 默认为打开状态

            repeatText = repeatText,
            repeatData = repeatDataString,

            ringtoneName = AddAlarmClockManager.tempRingtoneName,
            ringtoneFileName = AddAlarmClockManager.tempRingtoneFileName,
            vibrationName = AddAlarmClockManager.tempVibrationName,
            vibrationId = AddAlarmClockManager.tempVibrationId,

            ringDuration = currentRingMinute,
            snoozeInterval = intervalRingValue,
            snoozeCount = repeatRingValue,

            label = "闹钟"
        )
        // 存储到数据库
        lifecycleScope.launch {
            val rowId = viewModel.insertAlarm(newAlarm)

            if (rowId != -1L) {
                val triggerTime = viewModel.calculateNextTriggerTime(newAlarm)
                val (h, m) = viewModel.getRemainingTime(triggerTime)

                Toast.makeText(applicationContext, "$h 小时 $m 分钟后响铃", Toast.LENGTH_SHORT).show()

                // 返回页面
                finish()
            } else {
                Toast.makeText(applicationContext, "保存失败，请重试", Toast.LENGTH_SHORT).show()
            }

        }
    }

    override fun onResume() {
        super.onResume()
        binding.ringToneName.text = AddAlarmClockManager.tempRingtoneName
    }

}