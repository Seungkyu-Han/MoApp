package com.example.moapp

import com.kakao.sdk.user.model.User
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface RetrofitService {
    @GET("/api/auth/login-kakaoAccess")
    fun login(@Query("token") token: String): Call<AccessToken>

    @GET("/api/auth/check")
    fun loginCheck(@Header("Authorization") accessToken: String): Call<Unit>

    @PATCH("/api/user/info")
    fun changeName(
        @Header("Authorization") accessToken: String,
        @Query("name") name: String
    ): Call<Unit>

    @GET("/api/user/add-friend")
    fun getFriendState(
        @Header("Authorization") accessToken: String,
    ): Call<Boolean>

    @PUT("/api/user/add-friend")
    fun changeFriendState(
        @Header("Authorization") accessToken: String,
        @Query("state") state: Boolean
    ): Call<Unit>

    @GET("/api/user/add-share")
    fun getAddShareState(
        @Header("Authorization") accessToken: String
    ): Call<Boolean>

    @PUT("/api/user/add-share")
    fun changeAddShareState(
        @Header("Authorization") accessToken: String,
        @Query("state") state: Boolean
    ): Call<Unit>
}