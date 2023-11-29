package com.example.moapp

import org.w3c.dom.Comment
import java.text.DateFormat

data class User(
    val id: Int,
    val name: String,
    val img: String
)

data class UserInfoRes(
    val id: Int,
    val name: String
)

data class ShareRes(
    val endDate:String,
    val id:Int,
    val name: String,
    val startDate: String,
    val userInfoResList:List<UserInfoRes>
)

data class PostAddFriendResponse(
    val name: String,
)

data class PostAcceptFriendResponse(
    val id: Int,
)

data class DeleteRequestFriendResponse(
    val id: Int,
)

data class DeleteFriendResponse(
    val id: Int,
)
