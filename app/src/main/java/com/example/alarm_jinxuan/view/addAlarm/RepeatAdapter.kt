package com.example.alarm_jinxuan.view.addAlarm

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.alarm_jinxuan.databinding.ItemDialogRepeatBinding
import com.example.alarm_jinxuan.model.RepeatDay

class RepeatAdapter(
    private val dataList: List<RepeatDay>, // 直接持有引用的数据源
    private val onItemClick: (Int) -> Unit // 点击回调
) : RecyclerView.Adapter<RepeatAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemDialogRepeatBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDialogRepeatBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.binding.apply {
            tvName.text = item.name
            cbDay.isChecked = item.isChecked // 这里强制同步状态

            root.setOnClickListener {
                // 直接回调位置
                onItemClick(position)
            }
        }
    }

    override fun getItemCount() = dataList.size
}