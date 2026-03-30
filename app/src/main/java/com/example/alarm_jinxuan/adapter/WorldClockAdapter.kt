package com.example.alarm_jinxuan.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.alarm_jinxuan.R
import com.example.alarm_jinxuan.databinding.ItemWorldClockBinding
import com.example.alarm_jinxuan.model.WorldClockEntity
import com.example.alarm_jinxuan.view.worldClock.WorldClockViewModel
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class WorldClockAdapter(
    private val viewModel: WorldClockViewModel,
    private val onDeleteClick: (Long) -> Unit
) : RecyclerView.Adapter<WorldClockAdapter.WorldClockViewHolder>() {

    private var clocks = listOf<WorldClockEntity>()

    inner class WorldClockViewHolder(private val binding: ItemWorldClockBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.deleteBtn.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick(clocks[position].id.toLong())
                }
            }
        }

        fun bind(clock: WorldClockEntity) {
            // 更新时间
            try {
                val zoneId = ZoneId.of(clock.zoneId)
                val cityTime = ZonedDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(clock.currentTimeMills),
                    zoneId
                )

                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                binding.timeText.text = cityTime.format(timeFormatter)

                // 设置天数差异
                val dayStatusText = viewModel.getDayStatusText(clock.dayStatus)
                if (dayStatusText.isNotEmpty()) {
                    binding.dayStatus.text = dayStatusText
                    binding.dayStatus.visibility = android.view.View.VISIBLE
                } else {
                    binding.dayStatus.visibility = android.view.View.GONE
                }

                binding.cityName.text = clock.cityName
                binding.cityInfo.text = "${clock.timeOffset} ${clock.cityEnglishName}"

            } catch (e: Exception) {
                binding.timeText.text = "00:00"
                binding.cityName.text = clock.cityName
                binding.cityInfo.text = "${clock.timeOffset} ${clock.cityEnglishName}"
                binding.dayStatus.visibility = android.view.View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorldClockViewHolder {
        val binding = ItemWorldClockBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WorldClockViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorldClockViewHolder, position: Int) {
        holder.bind(clocks[position])
    }

    override fun getItemCount(): Int = clocks.size

    fun updateClocks(newClocks: List<WorldClockEntity>) {
        val diffCallback = WorldClockDiffCallback(clocks, newClocks)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        clocks = newClocks
        diffResult.dispatchUpdatesTo(this)
    }
}

class WorldClockDiffCallback(
    private val oldList: List<WorldClockEntity>,
    private val newList: List<WorldClockEntity>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldClock = oldList[oldItemPosition]
        val newClock = newList[newItemPosition]

        return oldClock.cityName == newClock.cityName &&
                oldClock.timeOffset == newClock.timeOffset &&
                oldClock.currentTimeMills == newClock.currentTimeMills &&
                oldClock.dayStatus == newClock.dayStatus
    }
}