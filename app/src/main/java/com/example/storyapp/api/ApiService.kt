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
    fun getStoriesWithLocation(
        @Header("Authorization") token: String,
        @Query("location") includeLocation: Int? = null
    ): Call<StoriesResponse>

    @GET("stories")
    suspend fun getStoriesPaging(
        @Header("Authorization") token: String,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
    ): StoriesResponse

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
        @Part("description") description: RequestBody,
        @Part("lat") lat: Double? = null,
        @Part("lon") lon: Double? = null,
    ): Call<UploadStoryResponse>

}
