package com.example.alarm_jinxuan.view.stopWatch

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.alarm_jinxuan.databinding.ItemQuickNoteBinding
import com.example.alarm_jinxuan.model.LapRecord

class LapAdapter() : ListAdapter<LapRecord, LapAdapter.ViewHolder>(DiffCallback()) {
    class ViewHolder(val binding: ItemQuickNoteBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemQuickNoteBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val item = getItem(position)
        holder.binding.apply {
            tvLapId.text = "计时 ${item.id}"
            tvLapTotal.text = item.formattedTotalTime
            tvLapDuration.text = item.formattedDuration
        }
    }


    class DiffCallback : DiffUtil.ItemCallback<LapRecord>() {
        override fun areItemsTheSame(
            oldItem: LapRecord,
            newItem: LapRecord
        ): Boolean {
            return newItem.id == oldItem.id
        }

        override fun areContentsTheSame(
            oldItem: LapRecord,
            newItem: LapRecord
        ): Boolean {
            return newItem == oldItem
        }

    }
}