package com.example.nugasapp.data.network.retrofit
import com.example.nugasapp.BuildConfig
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Retrofit

class ApiConfig {
    companion object{
        fun getApiService(): ApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(ApiService::class.java)
        }
    }
}



