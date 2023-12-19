package com.example.moapp

import android.app.DatePickerDialog
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.moapp.databinding.ActivityRequestBinding
import com.example.moapp.model.Group
import com.example.moapp.model.shareSchedulePostReq
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class RequestActivity : AppCompatActivity() {
    private val retrofit: Retrofit = Retrofit.Builder().baseUrl("https://hangang-bike.site/")
        .addConverterFactory(GsonConverterFactory.create()).build()
    private val service = retrofit.create(RetrofitService::class.java)
    private val token = PrefApp.prefs.getString("accessToken", "default")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val groupId = intent.getIntExtra("groupId", -1)

        var selectedDate: String

        service.getGroupInfo(groupId, "Bearer $token").enqueue(object : Callback<Group> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<Group>, response: Response<Group>) {
                if (response.isSuccessful) {
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

                    val startDate = LocalDate.parse(response.body()?.startDate, formatter)
                    val endDate = LocalDate.parse(response.body()?.endDate, formatter)
                    val startCal = Calendar.getInstance()
                    val endCal = Calendar.getInstance()


                    startCal.set(Calendar.YEAR, startDate.year)
                    startCal.set(Calendar.MONTH, startDate.monthValue - 1)
                    startCal.set(Calendar.DAY_OF_MONTH, startDate.dayOfMonth)
                    endCal.set(Calendar.YEAR, endDate.year)
                    endCal.set(Calendar.MONTH, endDate.monthValue - 1)
                    endCal.set(Calendar.DAY_OF_MONTH, endDate.dayOfMonth)

                    selectedDate = "${startCal.get(Calendar.YEAR)}-${startCal.get(Calendar.MONTH) + 1}-${startCal.get(
                        Calendar.DAY_OF_MONTH)}"

                    binding.dateBtn.setOnClickListener {
                        val cal = Calendar.getInstance()
                        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                            if (month < 9) {
                                if (dayOfMonth < 10) {
                                    selectedDate = "${year}-0${month + 1}-0${dayOfMonth}"
                                } else {
                                    selectedDate = "${year}-0${month + 1}-${dayOfMonth}"
                                }
                            } else {
                                if (dayOfMonth < 10) {
                                    selectedDate = "${year}-${month + 1}-0${dayOfMonth}"
                                } else {
                                    selectedDate = "${year}-${month + 1}-${dayOfMonth}"
                                }
                            }
                            binding.selectedDate.text = selectedDate
                        }
                        val datePickerDialog = DatePickerDialog(this@RequestActivity, dateSetListener, cal.get(
                            Calendar.YEAR),
                            cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH))
                        datePickerDialog.apply {
                            datePicker.minDate = startCal.timeInMillis
                            datePicker.maxDate = endCal.timeInMillis
                        }
                        datePickerDialog.show()
                    }

                    binding.reqButton.setOnClickListener {
                        val startTime = binding.fromTimeSpinner.selectedItemPosition
                        val endTime = binding.toTimeSpinner.selectedItemPosition - 1

                        val shareSchedulePostReq = shareSchedulePostReq(
                            selectedDate,
                            endTime,
                            groupId,
                            startTime
                        )

                        Log.d("park", shareSchedulePostReq.toString())

                        service.requestSchedule(shareSchedulePostReq, "Bearer $token").enqueue(
                            object : Callback<Unit> {
                                override fun onResponse(
                                    call: Call<Unit>,
                                    response: Response<Unit>
                                ) {
                                    if (response.isSuccessful) {
                                        finish()
                                    } else {
                                        Toast.makeText(this@RequestActivity, "불가능한 시간입니다.",
                                            Toast.LENGTH_SHORT).show()
                                        Log.d("park", response.code().toString())
                                        Log.d("park", response.errorBody()!!.string())
                                    }
                                }

                                override fun onFailure(call: Call<Unit>, t: Throwable) {
                                    Toast.makeText(this@RequestActivity, "잠시후 다시 이용해주세요.",
                                        Toast.LENGTH_SHORT).show()
                                    Log.d("park", response.code().toString())
                                }
                            }
                        )


                    }

                    val calStart = Calendar.getInstance()
                    Log.d("park", "${calStart.get(Calendar.YEAR)}-${calStart.get(Calendar.MONTH)}-${calStart.get(
                        Calendar.DAY_OF_MONTH)}")
                } else {
                    Toast.makeText(this@RequestActivity, "그룹 정보를 가져올 수 없습니다.",
                        Toast.LENGTH_SHORT).show()
                    Log.d("park", response.code().toString())
                }
            }

            override fun onFailure(call: Call<Group>, t: Throwable) {
                Toast.makeText(this@RequestActivity, "그룹 정보를 가져올 수 없습니다.",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }
}