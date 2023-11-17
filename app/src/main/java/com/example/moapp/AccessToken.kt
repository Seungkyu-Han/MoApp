package com.example.moapp

import com.google.gson.annotations.SerializedName

data class AccessToken (
    val name: String,
    val id: Int,
    val accessToken: String
)