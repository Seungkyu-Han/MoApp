package com.example.moapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.moapp.databinding.ActivityUpdateUserScheduleBinding
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UpdateUserSchedule : AppCompatActivity() {
    private lateinit var retrofitService: RetrofitService

    private var cellId = -1
    private lateinit var daySpinner: Spinner
    private lateinit var fromTimeSpinner: Spinner
    private lateinit var toTimeSpinner: Spinner
    private lateinit var scheduleNameText: EditText

    private lateinit var days: Array<String>
    private lateinit var times: Array<String>

    private val authToken = PrefApp.prefs.getString("accessToken","default")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityUpdateUserScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = findViewById(R.id.toolbar3)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        val upArrow = resources.getDrawable(R.drawable.ic_back_arrow, null)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // back arrow
        supportActionBar?.setHomeAsUpIndicator(upArrow)

        days = resources.getStringArray(R.array.days_array)
        times = resources.getStringArray(R.array.times_array)

        daySpinner = findViewById(R.id.daySpinner)
        fromTimeSpinner = findViewById(R.id.fromTimeSpinner)
        toTimeSpinner = findViewById(R.id.toTimeSpinner)
        scheduleNameText= findViewById(R.id.scheduleNameText)

        // initiate retrofit ------------------------------------------------------------------
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()

                // Add headers
                val requestBuilder = original.newBuilder()
                    .header("accept", "*/*")
                    .header("Authorization", "Bearer $authToken")

                val modifiedRequest = requestBuilder.build()
                chain.proceed(modifiedRequest)
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://hangang-bike.site/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofitService = retrofit.create(RetrofitService::class.java)
        //--------------------------------------------------------------------------------------


        // get passed values from the ScheduleDetail page
        cellId = intent.getIntExtra("cellId", -1)
        if (cellId != -1) {
            Log.d("update", "UpdatePage Received cellId: $cellId")
        } else {
            Log.e("update", "UpdatePage Error: cellId not received")
        }

        binding.updateButton.setOnClickListener {
            // convert data and call putSchedule() function
            val id = cellId   //cellId == schedule ID
            val day = getWeekDay(daySpinner.selectedItem.toString())  // Convert 월요일 to Monday
            val startTime = fromTimeSpinner.selectedItemPosition
            val endTime = toTimeSpinner.selectedItemPosition -1
            val scheduleName = scheduleNameText.text.toString()

            // update schedule info in server, if success return to Schedule Detail page
            patchSchedule(id, day, startTime, endTime, scheduleName)
        }
        getSchedule()
    }

    override fun onSupportNavigateUp(): Boolean {
        val scheduleIntent = Intent(this, ScheduleDetail::class.java)
        startActivity(scheduleIntent)
        return true
    }

    //retrieve schedule data from server : @GET("/api/user-schedule/schedule"), data class GetScheduleResponse
    private fun getSchedule() {
        val call = retrofitService.getUserSchedule()
        call.enqueue(object : Callback<GetScheduleResponse> {
            override fun onResponse(call: Call<GetScheduleResponse>, response: Response<GetScheduleResponse>) {
                if (response.isSuccessful) {
                    val scheduleResponse = response.body()
                    scheduleResponse?.let { scheduleEventResponse ->
                        for (scheduleEvent in scheduleEventResponse.scheduleEvents) {
                            Log.d("update", "ID: ${scheduleEvent.id}, Day: ${scheduleEvent.day}, Event: ${scheduleEvent.eventName}, start: ${scheduleEvent.startTime}, end: ${scheduleEvent.endTime}")
                            if(cellId == scheduleEvent.id){
                                //get default values
                                val defaultDay = scheduleEvent.day
                                val defaultStartTime = scheduleEvent.startTime
                                val defaultEndTime = scheduleEvent.endTime
                                val defaultScheduleName = scheduleEvent.eventName
                                Log.d("default","default values: $cellId, $defaultDay, $defaultStartTime, $defaultEndTime, $defaultScheduleName")

                                //set defaulDay
                                val koreanDay = when (defaultDay) {
                                    "Sunday" -> "일요일"
                                    "Monday" -> "월요일"
                                    "Tuesday" -> "화요일"
                                    "Wednesday" -> "수요일"
                                    "Thursday" -> "목요일"
                                    "Friday" -> "금요일"
                                    "Saturday" -> "토요일"
                                    else -> ""
                                }
                                val daysArray = resources.getStringArray(R.array.days_array)
                                val dayIndex = daysArray.indexOf(koreanDay)
                                daySpinner.setSelection(dayIndex)

                                //set defaulScheduleName
                                scheduleNameText.setText(defaultScheduleName)

                                //set defaultTime
                                val timesArray = resources.getStringArray(R.array.times_array)
                                val defaultSTime = "$defaultStartTime:00"
                                val timeIndexS = timesArray.indexOf(defaultSTime)
                                fromTimeSpinner.setSelection(timeIndexS)

                                val defaultTTime = "${defaultEndTime+1}:00"
                                val timeIndexT = timesArray.indexOf(defaultTTime)
                                toTimeSpinner.setSelection(timeIndexT)
                            }
                        }
                    }
                }
            }
            override fun onFailure(call: Call<GetScheduleResponse>, t: Throwable) {
                Log.d("update", "Failed to get schedule: ${t.message}")
                t.printStackTrace()
            }
        })
    }

    // update data in server : @PATCH("/api/user-schedule/schedule"), data class PatchScheduleResponse
    private fun patchSchedule(id: Int, day: String, startTime: Int, endTime: Int, scheduleName: String) {
        val call = retrofitService.updateUserSchedule(id, day, startTime, endTime, scheduleName)

        call.enqueue(object : Callback<PatchScheduleResponse> {
            override fun onResponse(call: Call<PatchScheduleResponse>, response: Response<PatchScheduleResponse>) {
                Log.d("update", "Schedule is updated successfully")
                when (response.code()) {
                    // if success return to Schedule Detail page
                    200, 201 -> {
                        showToast("일정 수정 성공")
                        val intent = Intent(this@UpdateUserSchedule, ScheduleDetail::class.java)
                        startActivity(intent)
                    }
                    //else show error
                    204 -> showToast("No Content")
                    400 -> showToast("시간 입력 오류: startTime >= endTime")
                    401 -> showToast("Unauthorizied")
                    403 -> showToast("유저가 생성한 스케줄이 아님")
                    404 -> showToast("해당 유저가 없음/해당 시간 불가능")
                }
            }
            override fun onFailure(call: Call<PatchScheduleResponse>, t: Throwable) {
                Log.d("update", "Schedule is updated successfully")
                showToast("일정 수정 성공")
                val intent = Intent(this@UpdateUserSchedule, ScheduleDetail::class.java)
                startActivity(intent)
            }
        })
    }
    // Show toast messages func
    private fun showToast(message: String) {
        Toast.makeText(this@UpdateUserSchedule, message, Toast.LENGTH_SHORT).show()
    }

    private fun getWeekDay(day: String): String {
        return when (day) {
            "월요일" -> "Monday"
            "화요일" -> "Tuesday"
            "수요일" -> "Wednesday"
            "목요일" -> "Thursday"
            "금요일" -> "Friday"
            "토요일" -> "Saturday"
            "일요일" -> "Sunday"
            else -> ""
        }
    }

}