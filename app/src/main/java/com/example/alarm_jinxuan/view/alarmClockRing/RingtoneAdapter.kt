package com.example.alarm_jinxuan.view.alarmClockRing

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.alarm_jinxuan.databinding.ItemRingtoneBinding
import com.example.alarm_jinxuan.model.AddAlarmClockManager
import com.example.alarm_jinxuan.model.RingtoneOption

class RingtoneAdapter(
    private val ringtoneList: List<RingtoneOption>,
    private val onItemSelected: (RingtoneOption) -> Unit
) : RecyclerView.Adapter<RingtoneAdapter.RingtoneViewHolder>() {

    // 记录当前选中的位置
    private var selectedPosition: Int = ringtoneList.indexOfFirst { 
        it.id == AddAlarmClockManager.tempRingtoneId
    }.let { if (it == -1) 0 else it }

    inner class RingtoneViewHolder(val binding: ItemRingtoneBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RingtoneViewHolder {
        val binding = ItemRingtoneBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, 
            false
        )
        return RingtoneViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RingtoneViewHolder, position: Int) {
        val ringtone = ringtoneList[position]
        
        holder.binding.apply {
            tvRingtoneName.text = ringtone.name
            rbSelect.isChecked = (position == selectedPosition)

            recommendRingContainer.setOnClickListener {
                val clickedPosition = holder.adapterPosition

                // 局部精准刷新
                val oldPosition = selectedPosition
                selectedPosition = clickedPosition
                
                notifyItemChanged(oldPosition)
                notifyItemChanged(selectedPosition)

                onItemSelected(ringtone)
            }
        }
    }

    override fun getItemCount() = ringtoneList.size
}