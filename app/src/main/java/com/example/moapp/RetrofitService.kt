package com.example.moapp

import com.kakao.sdk.user.model.User
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
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

    @GET("/api/user-schedule/schedule")
    fun getUserSchedule(): Call<GetScheduleResponse>

    @POST("/api/user-schedule/schedule")
    fun addUserSchedule(
        @Query("day") day: String,
        @Query("startTime") startTime: Int,
        @Query("endTime") endTime: Int,
        @Query("scheduleName") scheduleName: String
    ): Call<PostScheduleResponse>

    @PATCH("/api/user-schedule/schedule")
    fun updateUserSchedule(
        @Query("id") id: Int,
        @Query("day") day: String,
        @Query("startTime") startTime: Int,
        @Query("endTime") endTime: Int,
        @Query("scheduleName") scheduleName: String
    ): Call<PatchScheduleResponse>

    @DELETE("/api/user-schedule/schedule")
    fun deleteUserSchedule(
        @Query("id") id: Int
    ): Call<DeleteScheduleResponse>

    @GET("/api/friend/friend")
    fun getFriendsList(): Call<List<com.example.moapp.User>>

    @DELETE("/api/friend/friend")
    fun deleteFriend(
        @Query("id") id: Int
    ): Call<Unit>

    @GET("/api/friend/add-friend")
    fun getRequestFriend(): Call<List<com.example.moapp.User>>

    @POST("/api/friend/add-friend")
    fun postAddFriend(
        @Query("name") name: String,
    ): Call<Unit>

    @POST("/api/friend/add-friend")
    fun postAcceptFriend(
        @Query("id") id: Int,
    ): Call<Unit>

    @DELETE("/api/friend/add-friend")
    fun deleteRequestedFriend(
        @Query("id") id: Int
    ): Call<Unit>

    @GET("/api/share/share")
    fun getShareList(): Call<List<ShareRes>>


}