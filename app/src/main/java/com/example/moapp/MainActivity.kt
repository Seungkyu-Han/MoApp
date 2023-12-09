package com.example.moapp

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.moapp.databinding.ActivityMainBinding
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    val retrofit = Retrofit.Builder().baseUrl("https://hangang-bike.site/")
        .addConverterFactory(GsonConverterFactory.create()).build()
    val service = retrofit.create(RetrofitService::class.java)
    private val authToken = PrefApp.prefs.getString("accessToken", "default")
    private lateinit var retrofitService: RetrofitService

    private lateinit var userInfo:User
    private lateinit var adapter: FriendsAdapter
    private lateinit var nearSchedule:ScheduleEvent


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater) // 레이아웃 인플레이터 변경
        supportActionBar?.title = "Friends"
        val colorCode = "#C62E2E" // 색상 코드
        val color = Color.parseColor(colorCode) // 색상 코드를 Color 객체로 변환
        supportActionBar?.setBackgroundDrawable(ColorDrawable(color))

        var loginIntent = Intent(this, LoginActivity::class.java)

        var mainIntent = Intent(this, MainActivity::class.java)
        val scheduleIntent = Intent(this, ScheduleDetail::class.java)
        var chatListIntent = Intent(this, GroupListActivity::class.java)
        var settingIntent = Intent(this, SettingActivity::class.java)

        service.loginCheck("Bearer ${PrefApp.prefs.getString("accessToken", "default")}")?.enqueue(
            object : Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (response.isSuccessful) {
                        Log.d("park", "자동 로그인 되었습니다.")
//                        button2.setOnClickListener{
//                            startActivity(settingIntent)
//                        }
                    }
                    else {
                        Log.d("park", "자동 로그인 안됨.")
                        Log.d("park", "${response.errorBody()?.string()}")
                        startActivity(loginIntent)
                    }
                }
                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    Log.d("park", "자동 로그인 오류.")
                }
            }
        )

        // 어뎁터 초기화
        adapter = FriendsAdapter(emptyList())
        binding.recyclerView.adapter = adapter

        // 리사이클러 뷰에 LayoutManager 적용
        binding.recyclerView.layoutManager = LinearLayoutManager(this) // activity를 사용하므로 this 사용

        // initiate retrofit -----------------------------------------------------------
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
        //------------------------------------------------------------------------------
        getFriends()
        getUserInfo()
        getNearSchedule()
        setContentView(binding.root)

        binding.bottomBar.friendsBtn.setOnClickListener {
            startActivity(mainIntent)
        }
        binding.bottomBar.scheduleBtn.setOnClickListener {
            startActivity(scheduleIntent)
        }
        binding.bottomBar.groupsBtn.setOnClickListener {
            startActivity(chatListIntent)
        }
        binding.bottomBar.settingsBtn.setOnClickListener {
            startActivity(settingIntent)
        }
    }

    // action bar - plus friends btn
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.plus_friend_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_friend -> {
                // 버튼 클릭 시 다른 액티비티로 이동
                var plusfriendIntent = Intent(this, PlusFriendActivity::class.java)
                startActivity(plusfriendIntent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun getUserInfo() {
        val call = retrofitService.getUserInfo()
        call.enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {

                if (response.isSuccessful) {
                    val info = response.body()
                    info?.let { user ->
                        // API 응답을 처리하는 코드
                        userInfo =user
                        updateUserInfoViews()
                    }
                } else {
                    // 에러 처리
                    Log.e("henry", "getUserInfo API request error: ${response.message()}")

                }
            }
            override fun onFailure(call: Call<User>, t: Throwable) {
                // 에러 처리
                Log.e("henry", "requestFriend API request failure: ${t.message}")
                t.printStackTrace()
            }
        })
    }

    private fun updateUserInfoViews() {
        // 사용자 정보를 뷰에 업데이트하는 코드
        binding.userName.text = userInfo.name
//        binding.userId.text = userInfo.id.toString()
        Glide.with(binding.userImageview.context)
            .load(userInfo.img)
            .into(binding.userImageview)
    }
    private fun getNearSchedule() {
        val call = retrofitService.getNearSchedule()
        call.enqueue(object : Callback<ScheduleEvent> {
            override fun onResponse(call: Call<ScheduleEvent>, response: Response<ScheduleEvent>) {

                if (response.isSuccessful) {
                    val info = response.body()
                    info?.let { schedule ->
                        // API 응답을 처리하는 코드
                        nearSchedule =schedule
                        updateUserNearSchedule()
                    }
                } else {
                    // 에러 처리
                    Log.e("henry", "getNearSchedule API request error: ${response.message()}")

                }
            }
            override fun onFailure(call: Call<ScheduleEvent>, t: Throwable) {
                // 에러 처리
                Log.e("henry", "getNearSchedule API request failure: ${t.message}")
                t.printStackTrace()
            }
        })
    }

    private fun updateUserNearSchedule() {
        // 사용자 정보를 뷰에 업데이트하는 코드
        // 날짜 및 시간 형식 변경
        val inputDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputDateFormat = SimpleDateFormat("MM월 d일 (E)", Locale.getDefault())

        val date: Date? = inputDateFormat.parse(nearSchedule.day)
        val formattedDate = date?.let { outputDateFormat.format(it) } ?: "날짜 형식 오류"

        val formattedStartTime = String.format("%02d:00", nearSchedule.startTime)
        val formattedEndTime = String.format("%02d:59", nearSchedule.endTime)

        binding.nearScheduleDate.text = formattedDate
        binding.nearScheduleStarttime.text = formattedStartTime
        binding.nearScheduleStarttime.text = formattedEndTime
        binding.nearScheduleContent.text = nearSchedule.eventName
    }

    private fun getFriends() {
        // Retrofit을 사용하여 API 호출
        val call = retrofitService.getFriendsList()
        call.enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    val userList = response.body()
                    userList?.let { users ->
                        // API 응답을 처리하는 코드
                        adapter.updateData(users)
                    }
                } else {
                    // 에러 처리
                    Log.e("henry", "getFriends API request error: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                // 에러 처리
                Log.e("henry", "getFriends API request failure: ${t.message}")
            }
        })
    }
}