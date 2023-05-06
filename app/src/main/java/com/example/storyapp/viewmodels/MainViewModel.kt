package com.example.storyapp.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.example.storyapp.models.UserPreference
import com.example.storyapp.api.ApiConfig
import com.example.storyapp.responses.StoriesResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel(private val pref: UserPreference) : ViewModel() {

    private val _storiesResponse = MutableLiveData<StoriesResponse>()
    val storiesResponse: LiveData<StoriesResponse> = _storiesResponse

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

    fun getUserToken() = pref.getToken().asLiveData()

    fun logout() = deleteUserToken()

    private fun deleteUserToken(){
        viewModelScope.launch {
            pref.deleteToken()
        }
    }
}