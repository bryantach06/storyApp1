package com.example.storyapp.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.example.storyapp.api.ApiService
import com.example.storyapp.responses.ListStoryItem
import com.example.storyapp.responses.StoriesResponse
import com.example.storyapp.responses.UploadStoryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StoryRepository(private val apiService: ApiService, private var context: Context) {

    private val _showLoading = MutableLiveData<Boolean>()
    val showLoading: LiveData<Boolean> = _showLoading

    private val _getAllStoriesResponse = MutableLiveData<StoriesResponse>()
    val getAllStoriesResponse: LiveData<StoriesResponse> = _getAllStoriesResponse

    private val _uploadStoryResponse = MutableLiveData<UploadStoryResponse>()
    val uploadStoryResponse: LiveData<UploadStoryResponse> = _uploadStoryResponse

    fun getStory(): LiveData<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            pagingSourceFactory = {
                StoryPagingSource(apiService, context)
            }
        ).liveData
    }

    fun uploadStory(
        token: String,
        file: MultipartBody.Part,
        description: RequestBody,
        lat: Double?,
        lon: Double?
    ) {
        _showLoading.value = true
        val client = apiService.uploadStory(token, file, description, lat, lon)
        Log.d("TOKEN", token)

        client.enqueue(object : Callback<UploadStoryResponse> {
            override fun onResponse(
                call: Call<UploadStoryResponse>,
                response: Response<UploadStoryResponse>
            ) {
                _showLoading.value = false
                if (response.isSuccessful && response.body() != null) {
                    _uploadStoryResponse.value = response.body()
                } else {
                    Log.e(
                        "StoryRepo.uploadStory",
                        "onFailure: ${response.message()}, ${response.body()?.message.toString()}"
                    )
                }
            }

            override fun onFailure(call: Call<UploadStoryResponse>, t: Throwable) {
                Log.d("error upload", t.message.toString())
            }

        }
        )
    }

    fun getStoriesWithLocation(token: String): LiveData<StoriesResponse> {
        _showLoading.value = true
        val client = apiService.getStoriesWithLocation(token)
        Log.d("TOKEN", token)

        client.enqueue(object : Callback<StoriesResponse> {
            override fun onResponse(
                call: Call<StoriesResponse>,
                response: Response<StoriesResponse>
            ) {
                _showLoading.value = false
                if (response.isSuccessful && response.body() != null) {
                    _getAllStoriesResponse.value = response.body()
                } else {
                    Log.e(
                        "StoryRepo",
                        "onFailure: ${response.message()}, ${response.body()?.message.toString()}"
                    )
                }
            }

            override fun onFailure(call: Call<StoriesResponse>, t: Throwable) {
                Log.e("StoryRepo", "onFailure: ${t.message.toString()}")
            }
        })

        return _getAllStoriesResponse
    }

}