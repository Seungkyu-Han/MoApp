package com.example.moapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.moapp.databinding.ActivityMainBinding
import com.example.moapp.model.MeetingInfo
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    val retrofit = Retrofit.Builder().baseUrl("https://hangang-bike.site/")
        .addConverterFactory(GsonConverterFactory.create()).build()
    val service = retrofit.create(RetrofitService::class.java)
    private val authToken = PrefApp.prefs.getString("accessToken", "default")
    private lateinit var retrofitService: RetrofitService

    private lateinit var userInfo:User
    private lateinit var adapter: FriendsAdapter
    private lateinit var nearSchedule:nearEvent


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                        getChatList()   // to show meeting noti
                        requestedFriend()  // to show friend noti
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
        call.enqueue(object : Callback<nearEvent> {
            override fun onResponse(call: Call<nearEvent>, response: Response<nearEvent>) {

                if (response.isSuccessful) {
                    val info = response.body()
                    info?.let { schedule ->
                        // API 응답을 처리하는 코드
                        Log.d("henry", "뭔가 했음")
                        nearSchedule =schedule
                        Log.e("henry", "${nearSchedule.toString()}")
                        updateUserNearSchedule()
                    }
                } else {
                    // 에러 처리
                    Log.e("henry", "getNearSchedule API request error: ${response.message()}")

                }
            }
            override fun onFailure(call: Call<nearEvent>, t: Throwable) {
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

        val date: Date? = inputDateFormat.parse(nearSchedule.date)
        val formattedDate = date?.let { outputDateFormat.format(it) } ?: "날짜 형식 오류"

        val formattedStartTime = String.format("%02d:00", nearSchedule.startTime)
        val formattedEndTime = String.format("%02d:00", nearSchedule.endTime + 1)


        binding.nearScheduleDate.text = formattedDate
        binding.nearScheduleStarttime.text = formattedStartTime
        binding.nearScheduleEndtime.text = formattedEndTime
        binding.nearScheduleContent.text = nearSchedule.name
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

    //add friend notifications
    private fun requestedFriend() {
        val call = retrofitService.getRequestFriend()
        call.enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    val userList = response.body()
                    Log.d("hien", "Received List: $userList")
                    if (userList != null) {
                        friendNoti(userList)
                    }
                } else {
                    Log.e("hien", "requestFriend API request error: ${response.message()}")

                }
            }
            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Log.e("hien", "requestFriend API request failure: ${t.message}")
                t.printStackTrace()
            }
        })
    }

    fun friendNoti(userList: List<User>) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        for (user in userList) {
            val builder: NotificationCompat.Builder
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // 26 버전 이상
                val channelId = "add-friend-request-channel"
                val channelName = "MoApp Add Friend Noti"
                val channel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    setShowBadge(true)
                    enableVibration(false)
                    setSound(null, null)
                }
                // 채널을 NotificationManager에 등록
                manager.createNotificationChannel(channel)
                // 채널을 이용하여 builder 생성
                builder = NotificationCompat.Builder(this, channelId)
            } else {
                // 26 버전 이하
                builder = NotificationCompat.Builder(this)
            }

            // 알림의 기본 정보
            builder.run {
                setWhen(System.currentTimeMillis())
                setContentTitle("Friend Request")
                setContentText("${user.name}님이 친구 추가를 요청했습니다")
                setSmallIcon(R.drawable.logo)
                priority = NotificationCompat.PRIORITY_DEFAULT
                setDefaults(0)
                setAutoCancel(true)// Close the notification when clicked
                manager.notify(user.id.hashCode(), builder.build())
                Log.d("hien", "noti: received add friend request from user: ${user.name}")
            }
        }
    }

    //add meeting notifications
    private fun getChatList() {
        val call = retrofitService.getShareList()
        call.enqueue(object : Callback<List<ShareRes>> {
            override fun onResponse(call: Call<List<ShareRes>>, response: Response<List<ShareRes>>) {
                if (response.isSuccessful) {
                    val chatList = response.body()
                    chatList?.let { list ->
                        for (item in list) {
                            Log.d("hien", "Group ID: ${item.id}")
                        }
                        getMeetingInfoForChatList(list)
                    } ?: Log.e("hien", "Chat List is null")
                } else {
                    Log.e("hien", "getChatList API request error: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<ShareRes>>, t: Throwable) {
                Log.e("hien", "getChatList API request failure: ${t.message}")
            }
        })
    }

    private fun getMeetingInfoForChatList(chatList: List<ShareRes>) {
        chatList.forEach { shareRes ->
            val groupId = shareRes.id
            val groupName = shareRes.name

            retrofitService.getMeetingInfo(groupId, "Bearer $authToken")
                .enqueue(object : Callback<MeetingInfo> {
                    override fun onResponse(call: Call<MeetingInfo>, response: Response<MeetingInfo>) {
                        if (response.isSuccessful) {
                            val meetingInfo = response.body()
                            meetingInfo?.let {
                                Log.d("hien", "Meeting State: $groupId: ${it.state}")
                                // Check if meeting's state is "Req"
                                if (it.state == "Req") {
                                    meetingNoti(groupName)
                                }
                            }
                        } else {
                            Log.e("hien", "getMeetingInfo API request error: ${response.message()}")
                        }
                    }

                    override fun onFailure(call: Call<MeetingInfo>, t: Throwable) {
                        Log.e("hien", "getMeetingInfo API request failure: ${t.message}")
                    }
                })
        }
    }


    fun meetingNoti(groupName: String) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val builder: NotificationCompat.Builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 26 버전 이상
            val channelId = "meeting-request-channel"
            val channelName = "MoApp Meeting Noti"
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                setShowBadge(true)
                enableVibration(false)
                setSound(null, null)
            }
            // 채널을 NotificationManager에 등록
            manager.createNotificationChannel(channel)
            // 채널을 이용하여 builder 생성
            builder = NotificationCompat.Builder(this, channelId)
        } else {
            // 26 버전 이하
            builder = NotificationCompat.Builder(this)
        }

        // 알림의 기본 정보
        builder.run {
            setWhen(System.currentTimeMillis())
            setContentTitle("Meeting Request")
            setContentText("$groupName: 새로운 미팅 요청이 있습니다")
            setSmallIcon(R.drawable.logo)
            priority = NotificationCompat.PRIORITY_DEFAULT
            setDefaults(0)
            setAutoCancel(true)// Close the notification when clicked
            manager.notify(groupName.hashCode(), builder.build())
            Log.d("hien","send meeting nofi")
        }
    }
}