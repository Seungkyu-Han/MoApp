package com.example.moapp

import com.kakao.sdk.user.model.User
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RetrofitService {
    @GET("/api/auth/login-kakaoAccess")
    fun login(@Query("token") token: String): Call<AccessToken>
}