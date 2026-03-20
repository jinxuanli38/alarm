package com.example.alarm_jinxuan.view.alarm

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.alarm_jinxuan.databinding.ItemAlarmRingBinding
import com.example.alarm_jinxuan.model.AlarmEntity

class AlarmAdapter(
    private val onToggle: (AlarmEntity, Boolean) -> Unit, // 开关回调
    private val onClick: (AlarmEntity) -> Unit            // 点击详情回调
) : ListAdapter<AlarmEntity, AlarmAdapter.AlarmViewHolder>(AlarmDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val binding =
            ItemAlarmRingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlarmViewHolder(binding)
    }

    class AlarmViewHolder(val binding: ItemAlarmRingBinding) :
        RecyclerView.ViewHolder(binding.root)

    @SuppressLint("DefaultLocale")
    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = getItem(position)

        holder.binding.apply {
            alClock.text = String.format("%02d:%02d", alarm.hour, alarm.minute)

            period.text = alarm.period

            alarmNameWeek.text = String.format("%s，%s", alarm.label, alarm.repeatText)

            switchWorkday.setOnCheckedChangeListener(null)
            switchWorkday.isChecked = alarm.isEnabled

            // 重新绑定监听器
            switchWorkday.setOnCheckedChangeListener { _, isChecked ->
                onToggle(alarm, isChecked)
            }

            // 点击会编辑闹钟
            root.setOnClickListener {
                onClick(alarm)
            }

        }
    }
    object AlarmDiffCallback : DiffUtil.ItemCallback<AlarmEntity>() {
        override fun areItemsTheSame(oldItem: AlarmEntity, newItem: AlarmEntity) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: AlarmEntity, newItem: AlarmEntity) = oldItem == newItem
    }
}