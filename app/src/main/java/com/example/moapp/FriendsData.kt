package com.example.moapp

import org.w3c.dom.Comment
import java.text.DateFormat

data class User(
    val id: Int,
    val name: String,
    val img: String
)

data class ShareRes(
    val endDate:String,
    val id:Int,
    val name: String,
    val startDate: String,
    val userInfoResList:List<User>
)
