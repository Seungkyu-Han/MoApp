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

data class shareSchedulePostReq(
    val date: String,
    val endTime: Int,
    val share_id: Int,
    val startTime: Int,
)

data class requstInfo(
    val startTime: Int,
    val endTime: Int,
    val date: String,
    val state: String
)

data class MeetingInfo(
    val startTime: Int,
    val endTime: Int,
    val date: String,
    val state: String
)