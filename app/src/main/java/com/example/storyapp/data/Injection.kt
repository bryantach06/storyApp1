package com.example.storyapp.data

import android.content.Context
import com.example.storyapp.api.ApiConfig

object Injection {
    fun provideRepository(context: Context): StoryRepository {
        val apiService = ApiConfig.getApiService()
        return StoryRepository(apiService, context)
    }
}