package com.example.alarm_jinxuan.view.addAlarm

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.alarm_jinxuan.databinding.ItemDialogDurationBinding
import com.example.alarm_jinxuan.model.DurationOption

class DurationAdapter(
    private val dataList: List<DurationOption>,
    private val onItemSelected: (DurationOption) -> Unit // 回调选中的整个对象，方便拿数据
) : RecyclerView.Adapter<DurationAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemDialogDurationBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDialogDurationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.binding.apply {
            tvDuration.text = item.label

            rbSelect.isChecked = item.isSelected

            root.setOnClickListener {
                if (!item.isSelected) {
                    dataList.forEachIndexed { index, option ->
                        if (option.isSelected) {
                            option.isSelected = false
                            notifyItemChanged(index)
                        }
                    }

                    item.isSelected = true
                    notifyItemChanged(holder.adapterPosition)

                    onItemSelected(item)
                }
            }
        }
    }

    override fun getItemCount() = dataList.size
}