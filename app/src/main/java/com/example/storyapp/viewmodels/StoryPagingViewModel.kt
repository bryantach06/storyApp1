package com.example.storyapp.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.storyapp.data.Injection
import com.example.storyapp.data.StoryRepository
import com.example.storyapp.responses.ListStoryItem

class StoryPagingViewModel(storyRepository: StoryRepository): ViewModel() {
    val allStories: LiveData<PagingData<ListStoryItem>> =
        storyRepository.getStory().cachedIn(viewModelScope)

    class PagingViewModelFactory(private var context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StoryPagingViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return StoryPagingViewModel(Injection.provideRepository(context)) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}