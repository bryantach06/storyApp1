package com.example.storyapp.api

import com.example.storyapp.responses.LoginResponse
import com.example.storyapp.responses.RegisterResponse
import com.example.storyapp.responses.StoriesResponse
import com.example.storyapp.responses.UploadStoryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @GET("stories")
    fun getStories(
        @Header("Authorization") token: String
    ): Call<StoriesResponse>

    @FormUrlEncoded
    @POST("register")
    fun onRegister(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<RegisterResponse>

    @FormUrlEncoded
    @POST("login")
    fun doLogin(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    @Multipart
    @POST("stories")
    fun uploadStory(
        @Header("Authorization") token: String,
        @Part photo: MultipartBody.Part,
        @Part("description") description: RequestBody
    ): Call<UploadStoryResponse>

}
