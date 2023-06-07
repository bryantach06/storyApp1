package com.example.storyapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyapp.data.StoryRepository
import com.example.storyapp.responses.UploadStoryResponse
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AddStoryViewModel(private val pref: StoryRepository): ViewModel() {
    val fileUploadResponse: LiveData<UploadStoryResponse> = pref.uploadStoryResponse

    fun uploadStory(token: String, file: MultipartBody.Part, description: RequestBody, lat: Double?, lon: Double?) {
        viewModelScope.launch {
            pref.uploadStory(token, file, description, lat, lon)
        }
    }
}