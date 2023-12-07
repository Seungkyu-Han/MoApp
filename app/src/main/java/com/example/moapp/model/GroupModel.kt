package com.example.moapp.model

import com.example.moapp.User
import com.google.gson.annotations.SerializedName

data class Group(
    val endDate: String,
    val id: Int,
    val name: String,
    val startDate: String,
    val userInfoResList: ArrayList<User>
)

data class groupPostReq(
    val endDate: String,
    val name: String,
    val startDate: String,
    val userIdList: ArrayList<Int>
)

data class shareSchedulePostUserScheduleReq(
    val date: String,
    val endTime: Int,
    val id: Int,
    val startTime: Int,
)
