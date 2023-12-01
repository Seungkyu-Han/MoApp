package com.example.moapp

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.moapp.databinding.ActivityAddUserScheduleBinding
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AddUserSchedule : AppCompatActivity() {
    private lateinit var retrofitService: RetrofitService

    private val authToken = PrefApp.prefs.getString("accessToken","default")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAddUserScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val upArrow = resources.getDrawable(R.drawable.ic_back_arrow, null)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // back arrow
        supportActionBar?.setHomeAsUpIndicator(upArrow)

        supportActionBar?.title = "Add Schedule"
        val colorCode = "#C62E2E" // 색상 코드
        val color = Color.parseColor(colorCode) // 색상 코드를 Color 객체로 변환
        supportActionBar?.setBackgroundDrawable(ColorDrawable(color))

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


        //  get data of daySpinner, fromTimeSpinner, toTimeSpinner, scheduleNameText.
        val daySpinner: Spinner = binding.daySpinner
        val fromTimeSpinner: Spinner = binding.fromTimeSpinner
        val toTimeSpinner: Spinner = binding.toTimeSpinner


        binding.addButton.setOnClickListener {
            // convert data and call postSchedule() function
            val day = getWeekDay(daySpinner.selectedItem.toString())  // Convert 월요일 to Monday
            val startTime = fromTimeSpinner.selectedItemPosition
            val endTime = toTimeSpinner.selectedItemPosition -1
            val scheduleName = binding.scheduleNameText.text.toString()

            // add Schedule to server, if success return to Schedule Detail page
            postSchedule(day, startTime, endTime, scheduleName)
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        val scheduleIntent = Intent(this, ScheduleDetail::class.java)
        startActivity(scheduleIntent)
        return true
    }

    // add new data to server : @POST("/api/user-schedule/schedule"), data class PostScheduleResponse
    private fun postSchedule(day: String, startTime: Int, endTime: Int, scheduleName: String) {
        val call = retrofitService.addUserSchedule(day, startTime, endTime, scheduleName)
        call.enqueue(object : Callback<PostScheduleResponse> {
            override fun onResponse(call: Call<PostScheduleResponse>, response: Response<PostScheduleResponse>) {
                when (response.code()) {
                    // if success return to Schedule Detail page
                    200, 201 -> {
                        showToast("일정 추가 성공")
                        val intent = Intent(this@AddUserSchedule, ScheduleDetail::class.java)
                        startActivity(intent)
                        Log.d("hien", "Schedule added successfully")
                    }
                    //else show error
                    400 -> showToast("시간 입력 오류: startTime >= endTime")
                    401 -> showToast("권한 없음")
                    403 -> showToast("Forbidden")
                    404 -> showToast("시간 중복")
                }
            }

            override fun onFailure(call: Call<PostScheduleResponse>, t: Throwable) {
                Log.d("hien", "Schedule added successfully")
                showToast("일정 추가 성공")
                val intent = Intent(this@AddUserSchedule, ScheduleDetail::class.java)
                startActivity(intent)
            }
        })
    }
    // Show toast messages func
    private fun showToast(message: String) {
        Toast.makeText(this@AddUserSchedule, message, Toast.LENGTH_SHORT).show()
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