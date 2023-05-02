package com.example.storyapp.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storyapp.*
import com.example.storyapp.databinding.ActivityMainBinding
import com.example.storyapp.models.UserPreference
import com.example.storyapp.responses.ListStoryItem
import com.example.storyapp.viewmodels.MainViewModel
import com.example.storyapp.viewmodels.ViewModelFactory

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MainAdapter
    private lateinit var viewModel: MainViewModel

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

        binding.apply {
            rvAllStories.layoutManager = LinearLayoutManager(this@MainActivity)
            rvAllStories.adapter = adapter
            rvAllStories.setHasFixedSize(true)
        }

        viewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore))
        )[MainViewModel::class.java]

        val session = LoginSession(this)
        val token = session.passToken().toString()
        viewModel.getStories("Bearer $token")
        showLoading(true)
        viewModel.storiesResponse.observe(this) {
            if (it != null) {
                adapter.setListStories(it.listStory)
                showLoading(false)
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
                setPositiveButton("Ya") {_, _ ->
                    session.logoutSession()
                    finish()

                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    startActivity(intent)
                }
                create()
                show()
            }
        }

        setupView()
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