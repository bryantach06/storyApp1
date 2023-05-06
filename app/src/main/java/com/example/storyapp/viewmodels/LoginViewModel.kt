package com.example.storyapp.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.example.storyapp.models.UserModel
import com.example.storyapp.models.UserPreference
import com.example.storyapp.api.ApiConfig
import com.example.storyapp.responses.LoginResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel (private val pref: UserPreference) : ViewModel() {

    private val _userLogin = MutableLiveData<LoginResponse>()
    val userLogin: LiveData<LoginResponse> = _userLogin

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun getUser(): LiveData<UserModel> {
        return pref.getUser().asLiveData()
    }

    fun login() {
        viewModelScope.launch {
            pref.login()
        }
    }

    fun doLogin(email: String, password: String) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().doLogin(email, password)
        client.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                if (response.isSuccessful) {
                    val result = response.body()?.loginResult?.token
                    val responseBody = response.body()
                    result?.let { saveUserToken(it) }
                    _isLoading.postValue(false)
                    if (responseBody != null && !responseBody.error) {
                        _userLogin.postValue(response.body())
                    }
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.d("LoginViewModel", "onFailure: ${t.message.toString()}")
                _isLoading.postValue(false)
            }
        })
    }

    private fun saveUserToken(key: String) {
        viewModelScope.launch {
            pref.saveToken(key)
        }
    }
}
