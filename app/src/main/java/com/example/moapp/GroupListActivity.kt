package com.example.moapp

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.moapp.databinding.ActivityChatListBinding
import com.example.moapp.databinding.ItemChatBinding
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ShareViewHolder(val binding: ItemChatBinding) :
    RecyclerView.ViewHolder(binding.root)
class ShareAdapter(var shareRes: List<ShareRes>, private val onItemClick: (Int) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // 항목의 개수를 판단하기 위해 자동 호출
    override fun getItemCount(): Int {
        return shareRes.size
    }

    // 항목 뷰를 가지는 뷰 홀더를 준비하기 위해 자동 호출
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            RecyclerView.ViewHolder =
        ShareViewHolder(
            ItemChatBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )

    // RecyclerViewAdapter 내의 onBindViewHolder 메서드에서의 코드 일부분
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // 여러 그룹 대한 처리
        val binding = (holder as ShareViewHolder).binding

        // 데이터 바인딩 및 처리 코드 추가
        binding.chatTextviewTitle.text = shareRes[position].name
        binding.chatItemStartDate.text = shareRes[position].startDate
        binding.chatItemEndDate.text = shareRes[position].endDate
        binding.chatItemTextviewUserlist.text = shareRes[position].userInfoResList.joinToString(" ") { it.name }
        binding.chatNum.text = (shareRes[position].userInfoResList).size.toString()

        // 그룹 이미지는 첫번째 유저의 프로필 사진
        val groupImageUrl = shareRes[position].userInfoResList[0].img

        groupImageUrl?.let {
            Glide.with(binding.chatItemImageview.context)
                .load(it)
                .into(binding.chatItemImageview)
        }

        // Set click listener for each item in the RecyclerView
        holder.itemView.setOnClickListener {
            onItemClick.invoke(shareRes[position].id) // Pass groupId to onItemClick
        }
    }
    fun updateData(newList: List<ShareRes>) {
        shareRes = newList
        notifyDataSetChanged()
    }
}

class GroupListActivity : AppCompatActivity() { // FragmentActivity에서 AppCompatActivity로 변경
    private lateinit var adapter: ShareAdapter
    private lateinit var originalChatList: List<ShareRes>
    private val authToken = PrefApp.prefs.getString("accessToken", "default")
    private lateinit var retrofitService: RetrofitService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.title = "Group"
        val colorCode = "#C62E2E" // 색상 코드
        val color = Color.parseColor(colorCode) // 색상 코드를 Color 객체로 변환
        supportActionBar?.setBackgroundDrawable(ColorDrawable(color))

        val binding = ActivityChatListBinding.inflate(layoutInflater) // 레이아웃 인플레이터 변경

        var mainIntent = Intent(this, MainActivity::class.java)
        val scheduleIntent = Intent(this, ScheduleDetail::class.java)
        var chatListIntent = Intent(this, GroupListActivity::class.java)
        var settingIntent = Intent(this, SettingActivity::class.java)


        // 리사이클러 뷰에 LayoutManager 적용
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this) // activity를 사용하므로 this 사용


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
        getChatList()
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

        adapter = ShareAdapter(emptyList()) { groupId ->
            val groupScheduleIntent = Intent(this, GroupScheduleActivity::class.java)
            groupScheduleIntent.putExtra("groupId", groupId) // Pass groupId to GroupScheduleActivity
            startActivity(groupScheduleIntent)
        }
        // Set the adapter for the RecyclerView
        binding.chatRecyclerView.adapter = adapter
    }

    private fun getChatList() {
        // Retrofit을 사용하여 API 호출
        val call = retrofitService.getShareList()
        call.enqueue(object : Callback<List<ShareRes>> {
            override fun onResponse(call: Call<List<ShareRes>>, response: Response<List<ShareRes>>) {
                if (response.isSuccessful) {
                    val chatList = response.body()
                    chatList?.let { shareRes ->
                        // API 응답을 처리하는 코드
                        originalChatList = shareRes
                        adapter.updateData(shareRes)
                    }
                    Log.d("henry","success get Chat List")
                } else {
                    // 에러 처리
                    Log.e("henry", "getChatList API request error: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<ShareRes>>, t: Throwable) {
                // 에러 처리
                Log.e("henry", "getChatList API request failure: ${t.message}")
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        startActivity(Intent(this, MainActivity::class.java))
        return true
    }

    // action bar - add group button
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.add_group_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_group -> {
                // click on add group, go to CreateGroupFromActivity.kt
                var addGroupIntent = Intent(this, CreateGroupFromActivity::class.java)
                startActivity(addGroupIntent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

}