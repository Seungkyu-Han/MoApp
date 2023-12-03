package com.example.moapp.model

import com.example.moapp.User

data class Group(
    val endDate: String,
    val id: Int,
    val name: String,
    val startDate: String,
    val userInfoResList: ArrayList<User>
)
