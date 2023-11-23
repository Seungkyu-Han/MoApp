package com.example.moapp

data class User(
    val id: Int,
    val name: String,
    val img: String)

data class FriendsResponse(
    val friends: List<User>
)

data class Comment(val message: String)

data class ChatModel(
    val users: List<User>,
    val roomId: String, // 추가: 채팅방 식별자
    val comments: Map<String, Comment>
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
