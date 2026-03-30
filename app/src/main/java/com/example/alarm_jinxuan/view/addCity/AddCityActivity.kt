package com.example.alarm_jinxuan.view.addCity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alarm_jinxuan.adapter.CityListAdapter
import com.example.alarm_jinxuan.databinding.ActivityAddCityBinding
import com.example.alarm_jinxuan.view.worldClock.WorldClockViewModel
import kotlinx.coroutines.launch

class AddCityActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddCityBinding
    private val viewModel: WorldClockViewModel by viewModels()
    private lateinit var adapter: CityListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityAddCityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearch()
        observeViewModel()

        // 初始搜索
        viewModel.searchCities("")
    }

    private fun setupRecyclerView() {
        adapter = CityListAdapter { city ->
            // 在协程中添加城市，等待数据库操作完成
            lifecycleScope.launch {
                try {
                    viewModel.addWorldClock(city)
                    // 添加成功后返回
                    finish()
                } catch (e: Exception) {
                    // 添加失败，显示错误提示
                }
            }
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@AddCityActivity)
            adapter = this@AddCityActivity.adapter
        }
    }

    private fun setupSearch() {
        // 设置搜索框
        binding.searchEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchCities(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // 设置返回按钮
        binding.arrow.setOnClickListener {
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { uiState ->
            // 更新进度条
            binding.progressBar.visibility = if (uiState.isLoading) View.VISIBLE else View.GONE

            // 更新空提示
            binding.emptyText.visibility = if (uiState.availableCities.isEmpty() && !uiState.isLoading) {
                View.VISIBLE
            } else {
                View.GONE
            }

            // 更新列表
            adapter.updateCities(uiState.availableCities)
        }
    }

    override fun onResume() {
        super.onResume()
        // 恢复时刷新数据，显示所有可用城市
        viewModel.searchCities(binding.searchEdit.text.toString())
    }
}