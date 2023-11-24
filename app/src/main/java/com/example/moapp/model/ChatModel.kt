package com.example.moapp.model

data class Comment(val message: String)

data class ChatModel(
    val users: List<User>,
    val roomId: String, // 추가: 채팅방 식별자
    val comments: Map<String, Comment>
)