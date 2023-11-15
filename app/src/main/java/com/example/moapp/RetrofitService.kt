package com.example.moapp

import retrofit2.Call
import retrofit2.http.GET

interface RetrofitService {
    @GET("/api/auth/login")
    fun getAccessToken(): Call<String>
}