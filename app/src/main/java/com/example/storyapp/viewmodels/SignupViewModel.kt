package com.example.storyapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyapp.models.UserModel
import com.example.storyapp.models.UserPreference
import kotlinx.coroutines.launch

class SignupViewModel (private val pref: UserPreference) : ViewModel() {
    fun saveUser(user: UserModel) {
        viewModelScope.launch {
            pref.saveUser(user)
        }
    }
}