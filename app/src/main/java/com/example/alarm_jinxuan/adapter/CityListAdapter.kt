package com.example.alarm_jinxuan.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.alarm_jinxuan.databinding.ItemCityListBinding
import com.example.alarm_jinxuan.model.WorldClockEntity

class CityListAdapter(
    private val onItemClick: (WorldClockEntity) -> Unit
) : RecyclerView.Adapter<CityListAdapter.CityViewHolder>() {

    private var cities = listOf<WorldClockEntity>()

    inner class CityViewHolder(private val binding: ItemCityListBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(cities[position])
                }
            }
        }

        fun bind(city: WorldClockEntity) {
            binding.cityName.text = city.cityName
            binding.cityInfo.text = "${city.timeOffset} ${city.cityEnglishName}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val binding = ItemCityListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        holder.bind(cities[position])
    }

    override fun getItemCount(): Int = cities.size

    fun updateCities(newCities: List<WorldClockEntity>) {
        val diffCallback = CityDiffCallback(cities, newCities)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        cities = newCities
        diffResult.dispatchUpdatesTo(this)
    }
}

class CityDiffCallback(
    private val oldList: List<WorldClockEntity>,
    private val newList: List<WorldClockEntity>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].cityEnglishName == newList[newItemPosition].cityEnglishName
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldCity = oldList[oldItemPosition]
        val newCity = newList[newItemPosition]

        return oldCity.cityName == newCity.cityName &&
                oldCity.timeOffset == newCity.timeOffset &&
                oldCity.cityEnglishName == newCity.cityEnglishName
    }
}