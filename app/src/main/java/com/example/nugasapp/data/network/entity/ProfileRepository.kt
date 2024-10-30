package com.example.nugasapp.data.network.entity

import com.example.nugasapp.data.network.retrofit.ApiConfig
import retrofit2.HttpException


class ProfileRepository {
    suspend fun getProfile(username: String): Github {
        return try{
            val response = ApiConfig.getApiService().getDetailUser(username)
            response
        } catch(e: HttpException){
            throw Exception(e.response()?.errorBody()?.string())
        } catch (e: Exception){
            throw Exception(e.message)
        }
    }
}