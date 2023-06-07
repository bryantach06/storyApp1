package com.example.storyapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyapp.data.StoryRepository
import kotlinx.coroutines.launch

class MapsViewModel(private val pref: StoryRepository): ViewModel() {
    val getAllStoriesResponse get() = pref.getAllStoriesResponse

    fun getStoriesWithLocation(token: String) {
        viewModelScope.launch {
            pref.getStoriesWithLocation(token)
        }
    }
}