package com.example.moapp

data class ScheduleEvent(   //represent scheduleEvents[] in server
    val id: Int,
    val day: String,
    val startTime: Int,
    val endTime: Int,
    val eventName: String
)

data class nearEvent(   //represent scheduleEvents[] in server
    val id: Int,
    val date: String,
    val startTime: Int,
    val endTime: Int,
    val name: String
)

data class GetScheduleResponse(
    val scheduleEvents: List<ScheduleEvent>
)

data class PostScheduleResponse(
    val id: Int,
    val day: String,
    val startTime: Int,
    val endTime: Int,
    val scheduleName: String,
)

data class PatchScheduleResponse(
    val id: Int,
    val day: String,
    val startTime: Int,
    val endTime: Int,
    val scheduleName: String,
)

data class DeleteScheduleResponse(
    val id: Int
)

