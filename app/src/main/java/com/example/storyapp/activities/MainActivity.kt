package com.example.storyapp.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storyapp.*
import com.example.storyapp.adapters.LoadingStateAdapter
import com.example.storyapp.adapters.MainAdapter
import com.example.storyapp.api.ApiConfig
import com.example.storyapp.api.ApiService
import com.example.storyapp.data.StoryRepository
import com.example.storyapp.databinding.ActivityMainBinding
import com.example.storyapp.models.UserPreference
import com.example.storyapp.responses.ListStoryItem
import com.example.storyapp.viewmodels.MainViewModel
import com.example.storyapp.viewmodels.StoryPagingViewModel
import com.example.storyapp.viewmodels.ViewModelFactory

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : AppCompatActivity() {

    private lateinit var storyRepo: StoryRepository
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MainAdapter
    private lateinit var viewModel: MainViewModel
    private val pagingStoriesViewModel: StoryPagingViewModel by viewModels {
        StoryPagingViewModel.PagingViewModelFactory(this)
    }
    private lateinit var apiService: ApiService
    private lateinit var context: Context

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = MainAdapter()
        adapter.notifyDataSetChanged()

        adapter.setOnItemClickCallback(object : MainAdapter.OnItemClickCallback {
            override fun onItemClicked(data: ListStoryItem) {
                val intent = Intent(this@MainActivity, DetailStoryActivity::class.java)
                intent.putExtra(DetailStoryActivity.EXTRA_NAME, data)
                startActivity(intent)
            }

        })

        showLoading(true)
        pagingStoriesViewModel.allStories.observe(this) {
            adapter.submitData(lifecycle, it)
            showLoading(false)
        }

        apiService = ApiConfig.getApiService()
        context = this@MainActivity

        storyRepo = StoryRepository(apiService, context)

        viewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore), storyRepo)
        )[MainViewModel::class.java]

        viewModel.story.observe(this) {
            adapter.submitData(lifecycle, it)
        }
//paging not working
        viewModel.getUserToken().observe(this) { token ->
            if (token.isNotEmpty()) {
                viewModel.getStories("Bearer $token")
                showLoading(true)
                viewModel.storiesResponse.observe(this) {
                    if (it != null) {
//                        adapter.setListStories(it.listStory)
                        getData()
                        showLoading(false)
                    }
                }
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        binding.fabAddStory.setOnClickListener {
            val intent = Intent(this@MainActivity, AddStoryActivity::class.java)
            startActivity(intent)

            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.fabLogout.setOnClickListener {
            AlertDialog.Builder(this@MainActivity).apply {
                setTitle("Logout")
                setMessage("Apakah anda yakin ingin logout?")
                setPositiveButton("Ya") { _, _ ->
                    viewModel.logout()
                    finish()

                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    startActivity(intent)
                }
                create()
                show()
            }
        }

        binding.fabMaps.setOnClickListener {
            val intent = Intent(this@MainActivity, MapsActivity::class.java)
            startActivity(intent)

            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        setupView()
    }

    private fun getData() {
        val adapter = MainAdapter()
        binding.rvAllStories.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter {
                adapter.retry()
            }
        )
        binding.rvAllStories.layoutManager = LinearLayoutManager(this@MainActivity)
        binding.rvAllStories.setHasFixedSize(true)
        viewModel.story.observe(this) {
            adapter.submitData(lifecycle, it)
        }
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar2.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}