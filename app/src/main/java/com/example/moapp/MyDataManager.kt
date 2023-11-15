package com.example.moapp

object MyDataManager {
    private val scheduleData = mutableMapOf<String, MutableList<String>>()

    init {
        scheduleData["Time"] = mutableListOf("Time")
        for (hour in 0..23) {
            scheduleData[String.format("%02d:00", hour)] = mutableListOf(String.format("%02d:00", hour))
        }
    }

    fun saveData(selectedDates: List<String>, scheduleTableData: List<List<String>>) {
        scheduleData["Time"]?.addAll(selectedDates)

        for (hour in 0..23) {
            val hourKey = String.format("%02d:00", hour)
            if (scheduleData.containsKey(hourKey)) {
                val rowData = mutableListOf<String>()
                rowData.add(hourKey)
                for (dateIndex in selectedDates.indices) {
                    val schedule = scheduleTableData[hour][dateIndex+1]
                    rowData.add(schedule)
                }
                scheduleData[hourKey]?.addAll(rowData.subList(1, rowData.size))
            }
        }
    }

    fun getData(): Map<String, List<String>> {
        return scheduleData
    }
}

