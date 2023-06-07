package com.example.storyapp.activities

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.example.storyapp.databinding.ActivityDetailStoryBinding
import com.example.storyapp.responses.ListStoryItem
import java.util.Locale

class DetailStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailStoryBinding

    companion object {
        const val EXTRA_NAME = "extra_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
    }

    @SuppressLint("SetTextI18n")
    private fun setupView(){
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
        val detail = intent.getParcelableExtra<ListStoryItem>(EXTRA_NAME) as ListStoryItem
        binding.apply {
            tvDetailName.text = detail.name
            tvDetailStory.text = detail.description

            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
            val date = inputFormat.parse(detail.createdAt)
            val formattedDateTimeString = outputFormat.format(date)

            tvDetailCreatedAt.text = formattedDateTimeString
            Glide.with(this@DetailStoryActivity)
                .load(detail.photoUrl)
                .into(ivDetailStory)
        }
    }
}
