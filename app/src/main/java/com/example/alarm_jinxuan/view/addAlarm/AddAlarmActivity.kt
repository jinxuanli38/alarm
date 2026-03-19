package com.example.alarm_jinxuan.view.addAlarm

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.alarm_jinxuan.databinding.ActivityAddAlarmBinding
import com.example.alarm_jinxuan.databinding.LayoutAlarmDurationBinding
import com.example.alarm_jinxuan.databinding.LayoutAlarmNameDialogBinding
import com.example.alarm_jinxuan.databinding.LayoutConfirmDialogBinding
import com.example.alarm_jinxuan.databinding.LayoutIntervalDialogBinding
import com.example.alarm_jinxuan.databinding.LayoutRepeatDialogBinding
import com.example.alarm_jinxuan.model.AddAlarmClockManager
import com.example.alarm_jinxuan.model.DurationOption
import com.example.alarm_jinxuan.model.RepeatDay
import com.example.alarm_jinxuan.view.alarmClockRing.AlarmClockRingActivity

class AddAlarmActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddAlarmBinding
    private lateinit var repeatAdapter: RepeatAdapter

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

    // 小时上一次选中的位置
    private var lastHourIndex = -1

    // 分钟上一次选中的位置
    private var lastMinutePos = -1

    // 是否发生了修改
    private var isUpdateBool = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAddAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener {
            finish()
        }

        binding.wheelPeriod.apply {
            data = periodData
        }
        binding.wheelHour.apply {
            data = hourData
        }
        binding.wheelMinute.apply {
            data = minuteData
        }

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

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnConfirm.setOnClickListener {

            dialog.dismiss()
        }

        dialog.show()
    }

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

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnConfirm.setOnClickListener {
            AddAlarmClockManager.clear()

            dialog.dismiss()
            finish()
        }

        dialog.show()
    }
}