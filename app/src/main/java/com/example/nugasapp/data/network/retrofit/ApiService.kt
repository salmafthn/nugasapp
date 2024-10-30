package com.example.nugasapp.data.network.retrofit

import com.example.nugasapp.data.network.entity.Github
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("users/{username}")
    suspend fun getDetailUser(@Path("username") username: String): Github

}