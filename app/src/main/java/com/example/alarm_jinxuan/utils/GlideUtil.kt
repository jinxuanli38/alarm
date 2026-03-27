package com.example.alarm_jinxuan.utils

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide

object GlideUtil {
    // 加载图片
    fun loadImage(context: Context,resource: Int,view: ImageView) {
        Glide.with(context)
            .load(resource)
            .into(view)
    }
}