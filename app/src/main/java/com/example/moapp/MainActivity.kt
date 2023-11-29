package com.example.moapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.moapp.databinding.ActivityFriendsBinding
import com.example.moapp.databinding.ActivityMainBinding
import com.kakao.sdk.common.util.Utility
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    val retrofit = Retrofit.Builder().baseUrl("https://hangang-bike.site/")
        .addConverterFactory(GsonConverterFactory.create()).build()
    val service = retrofit.create(RetrofitService::class.java)
    private val authToken = PrefApp.prefs.getString("accessToken", "default")
    private lateinit var retrofitService: RetrofitService

    private lateinit var userInfo:User
    private lateinit var adapter: FriendsAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater) // 레이아웃 인플레이터 변경
        supportActionBar?.title = "Friends"

        var loginIntent = Intent(this, LoginActivity::class.java)

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
        setContentView(binding.root)

        binding.bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_schedule -> {
                    startActivity(Intent(this, ScheduleDetail::class.java))
                    true
                }
                R.id.navigation_chat -> {
                    startActivity(Intent(this, GroupListActivity::class.java))
                    true
                }
                R.id.navigation_settings -> {
                    startActivity(Intent(this, SettingActivity::class.java))
                    true
                }
                else -> false
            }
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
        binding.userId.text = userInfo.id.toString()
        Glide.with(binding.userImageview.context)
            .load(userInfo.img)
            .into(binding.userImageview)
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