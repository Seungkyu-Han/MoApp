package com.example.moapp

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moapp.databinding.ActivityCreateGroupTitleBinding
import com.example.moapp.model.groupPostReq
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Calendar

class CreateGroupTitleActivity : AppCompatActivity() {
    private val retrofit: Retrofit = Retrofit.Builder().baseUrl("https://hangang-bike.site/")
        .addConverterFactory(GsonConverterFactory.create()).build()
    private val service = retrofit.create(RetrofitService::class.java)
    private val token = PrefApp.prefs.getString("accessToken", "default")
    private lateinit var binding: ActivityCreateGroupTitleBinding
    private var clickCount = 0
    private var fromYear = 0
    private var fromMonth = 0
    private var fromDay = 0
    private var toYear = 0
    private var toMonth = 0
    private var toDay = 0
    private var toDate = ""
    private var fromDate = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateGroupTitleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Create Group"
        val colorCode = "#C62E2E" // 색상 코드
        val color = Color.parseColor(colorCode) // 색상 코드를 Color 객체로 변환
        supportActionBar?.setBackgroundDrawable(ColorDrawable(color))

        val todayCalendar = Calendar.getInstance()
        binding.calendarView.minDate = todayCalendar.timeInMillis
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            clickCount++
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            binding.calendarView.maxDate = calendar.timeInMillis  + (7 * 1000 * 60 * 60 * 24)
            if (clickCount % 2 != 0) {
                fromYear = year
                fromMonth = month + 1
                fromDay = dayOfMonth
                fromDate = "$fromYear-${String.format("%02d", fromMonth)}-${String.format("%02d", fromDay)}"
                binding.fromDate.text = fromDate
                binding.toDate.text = ""
            } else {
                toYear = year
                toMonth = month + 1
                toDay = dayOfMonth
                toDate = "$toYear-${String.format("%02d", toMonth)}-${String.format("%02d", toDay)}"
                binding.toDate.text = toDate

                val fromDateCalendar = Calendar.getInstance().apply {
                    set(fromYear, fromMonth, fromDay)
                }
                val toDateCalendar = Calendar.getInstance().apply {
                    set(toYear, toMonth, toDay)
                }

                val nextSevenDays = Calendar.getInstance().apply {
                    time = fromDateCalendar.time
                    add(Calendar.DAY_OF_MONTH, 6)
                }

                if (toDateCalendar.before(fromDateCalendar) || toDateCalendar.after(nextSevenDays)) {
                    val errorMessage = if (toDateCalendar.before(fromDateCalendar)) {
                        "에러: 시작일 > 종료일"
                    } else {
                        "시작일부터 7일 이내의 날짜를 선택하세요."
                    }
                    Toast.makeText(this@CreateGroupTitleActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    binding.toDate.text = ""
                    binding.fromDate.text = ""
                }
            }
        }


        var friends: ArrayList<User>?
        var checkedId: ArrayList<Int>

        service.getFriendList("Bearer $token").enqueue(object : Callback<ArrayList<User>> {
            override fun onResponse(call: Call<ArrayList<User>>, response: Response<ArrayList<User>>) {
                if (response.isSuccessful) {
                    friends = response.body()

                    val adapter = FriendAdapter(friends as ArrayList<User>)
                    binding.friendsList.layoutManager = LinearLayoutManager(this@CreateGroupTitleActivity)
                    binding.friendsList.adapter = adapter
                    binding.friendsList.addItemDecoration(DividerItemDecoration(this@CreateGroupTitleActivity,
                        LinearLayoutManager.VERTICAL))
                    binding.makeGroupBtn.setOnClickListener {
                        val title = binding.titleEdit.text.toString()
                        val checkedId = adapter.checkedItem
                        if (title.length <= 0) {
                            Toast.makeText(this@CreateGroupTitleActivity, "제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                        } else if (friends!!.isEmpty()) {
                            Toast.makeText(this@CreateGroupTitleActivity, "공유방 친구를 추가해주세요.", Toast.LENGTH_SHORT).show()
                        } else {
                            val groupPostReq = groupPostReq(toDate, title, fromDate, checkedId)
                            Log.d("park", "$groupPostReq")
                            service.createShareGroup(groupPostReq, "Bearer $token").
                            enqueue(object: Callback<Unit> {
                                override fun onResponse(
                                    call: Call<Unit>,
                                    response: Response<Unit>
                                ) {
                                    if (response.isSuccessful) {
                                        finish()
                                    } else {
                                        Log.d("park", response.code().toString())
                                        Toast.makeText(this@CreateGroupTitleActivity, "잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                override fun onFailure(call: Call<Unit>, t: Throwable) {
                                    Toast.makeText(this@CreateGroupTitleActivity, "잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                    }
                } else if (response.code() == 401) {
                    Toast.makeText(this@CreateGroupTitleActivity, "권한이 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ArrayList<User>>, t: Throwable) {
                Toast.makeText(this@CreateGroupTitleActivity, "잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}