package com.example.moapp

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.moapp.databinding.ActivityFriendsBinding
import com.example.moapp.databinding.ItemFriendsBinding
import com.example.moapp.fragment.ProfileFragment
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FriendsViewHolder(val binding: ItemFriendsBinding) :
    RecyclerView.ViewHolder(binding.root)
class FriendsAdapter(var userModels: List<User>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // 항목의 개수를 판단하기 위해 자동 호출
    override fun getItemCount(): Int {
        return userModels.size
    }

    // 항목 뷰를 가지는 뷰 홀더를 준비하기 위해 자동 호출
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            RecyclerView.ViewHolder =
        FriendsViewHolder(
            ItemFriendsBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )

    // RecyclerViewAdapter 내의 onBindViewHolder 메서드에서의 코드 일부분
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // 여러 사용자에 대한 처리
        val binding = (holder as FriendsViewHolder).binding

        // 데이터 바인딩 및 처리 코드 추가
        binding.friendsTextviewTitle.text = userModels[position].name
        binding.friendsTextviewId.text = "id : " + userModels[position].id.toString()
        Glide.with(binding.friendsItemImageview.context)
            .load(userModels[position].img)
            .into(binding.friendsItemImageview)

        // 아이템 클릭 시 프로필 팝업 표시
        holder.itemView.setOnClickListener {
            val fragmentManager = (holder.itemView.context as FragmentActivity).supportFragmentManager
            val profileFragment = ProfileFragment()

            val args = Bundle()
            args.putInt(ProfileFragment.ARG_USER_ID, userModels[position].id)
            args.putString(ProfileFragment.ARG_USER_NAME, userModels[position].name)
            args.putString(ProfileFragment.ARG_USER_IMG, userModels[position].img)
            profileFragment.arguments = args

            profileFragment.show(fragmentManager, ProfileFragment::class.java.simpleName)
        }
    }
    fun updateData(newList: List<User>) {
        userModels = newList
        notifyDataSetChanged()
    }
}

class FriendsDecoration(val context: Context): RecyclerView.ItemDecoration() {
    // 각 항목을 꾸미기 위해 호출
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.set(10, 10, 10, 0)
        view.setBackgroundColor(Color.parseColor("#EEE8B7"))
        ViewCompat.setElevation(view, 20.0f)
    }
}
//
//class FriendsActivity : AppCompatActivity() { // FragmentActivity에서 AppCompatActivity로 변경
//    private lateinit var adapter: FriendsAdapter
//    private lateinit var originalUserModel: List<User>
//    private val authToken = PrefApp.prefs.getString("accessToken", "default")
//    private lateinit var retrofitService: RetrofitService
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        supportActionBar?.title = "Friends"
//        supportActionBar?.setDisplayHomeAsUpEnabled(true) // back arrow
//
//        val binding = ActivityFriendsBinding.inflate(layoutInflater) // 레이아웃 인플레이터 변경
//
//        // 어뎁터 초기화
//        adapter = FriendsAdapter(emptyList())
//        binding.recyclerView.adapter = adapter
//
//        // 리사이클러 뷰에 LayoutManager 적용
//        binding.recyclerView.layoutManager = LinearLayoutManager(this) // activity를 사용하므로 this 사용
//
//        // initiate retrofit -----------------------------------------------------------
//        val okHttpClient = OkHttpClient.Builder()
//            .addInterceptor { chain ->
//                val original = chain.request()
//
//                // Add headers
//                val requestBuilder = original.newBuilder()
//                    .header("accept", "*/*")
//                    .header("Authorization", "Bearer $authToken")
//
//                val modifiedRequest = requestBuilder.build()
//                chain.proceed(modifiedRequest)
//            }
//            .build()
//
//        val retrofit = Retrofit.Builder()
//            .baseUrl("https://hangang-bike.site/")
//            .client(okHttpClient)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//
//        retrofitService = retrofit.create(RetrofitService::class.java)
//        //------------------------------------------------------------------------------
//        getFriends()
//        setContentView(binding.root)
//    }
//
//    private fun getFriends() {
//        // Retrofit을 사용하여 API 호출
//        val call = retrofitService.getFriendsList()
//        call.enqueue(object : Callback<List<User>> {
//            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
//                if (response.isSuccessful) {
//                    val userList = response.body()
//                    userList?.let { users ->
//                        // API 응답을 처리하는 코드
//                        originalUserModel = users
//                        adapter.updateData(users)
//                    }
//                } else {
//                    // 에러 처리
//                    Log.e("henry", "getFriends API request error: ${response.message()}")
//                }
//            }
//
//            override fun onFailure(call: Call<List<User>>, t: Throwable) {
//                // 에러 처리
//                Log.e("henry", "getFriends API request failure: ${t.message}")
//            }
//        })
//    }
//
//    override fun onSupportNavigateUp(): Boolean {
//        startActivity(Intent(this, MainActivity::class.java))
//        return true
//    }
//}