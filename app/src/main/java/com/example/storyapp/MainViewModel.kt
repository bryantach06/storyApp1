package com.example.storyapp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel(private val pref: UserPreference) : ViewModel() {

    private val _storiesResponse = MutableLiveData<StoriesResponse>()
    val storiesResponse: LiveData<StoriesResponse> = _storiesResponse

    fun getUser(): LiveData<UserModel> {
        return pref.getUser().asLiveData()
    }

    fun getStories(token: String) {
        ApiConfig.getApiService().getStories(token).enqueue(object : Callback<StoriesResponse> {
            override fun onResponse(
                call: Call<StoriesResponse>, response: Response<StoriesResponse>
            ) {
                if (response.isSuccessful) {
                    _storiesResponse.postValue(response.body())
                }
            }

            override fun onFailure(call: Call<StoriesResponse>, t: Throwable) {
                Log.e("MainViewModel", "OnFailure : ${t.message}")
            }
        })
    }

}