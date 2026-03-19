package com.example.alarm_jinxuan.view.vibration

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.alarm_jinxuan.databinding.ItemVibrationBinding
import com.example.alarm_jinxuan.model.VibrationOption

class VibrationAdapter(
    private val options: List<VibrationOption>,
    private val onItemSelected: (VibrationOption) -> Unit
) : RecyclerView.Adapter<VibrationAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemVibrationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemVibrationBinding.inflate(
            LayoutInflater.from(parent.context), 
            parent, 
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = options[position]
        
        holder.binding.apply {
            tvVibrationName.text = item.name
            
            rbSelect.isChecked = item.isSelected

            root.setOnClickListener {
                onItemSelected(item)

                if (!item.isSelected) {
                    options.forEachIndexed { index, option ->
                        if (option.isSelected) {
                            option.isSelected = false
                            notifyItemChanged(index)
                        }
                    }
                    
                    item.isSelected = true
                    notifyItemChanged(holder.adapterPosition)
                }
            }
        }
    }

    override fun getItemCount(): Int = options.size
}