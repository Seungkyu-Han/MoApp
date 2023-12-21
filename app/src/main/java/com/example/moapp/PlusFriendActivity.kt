package com.example.moapp

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.moapp.databinding.ActivityPlusFriendBinding
import com.example.moapp.databinding.ItemRequestFriendBinding
import com.example.moapp.fragment.ProfileFragment
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RequestViewHolder(val binding: ItemRequestFriendBinding) : RecyclerView.ViewHolder(binding.root)

interface FriendRequestActionListener {
    fun onAcceptFriendRequest(user: User)
    fun onRejectFriendRequest(user: User)
}

interface NavigateToActivityListener {
    fun navigateToActivity(intent: Intent)
}

class FriendRequestAdapter(
    var userModels: List<User>
, private val listener: FriendRequestActionListener
, private val navigateToActivityListener: NavigateToActivityListener
) : RecyclerView.Adapter<RequestViewHolder>() {
    override fun getItemCount(): Int = userModels.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder =
        RequestViewHolder(
            ItemRequestFriendBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val binding = holder.binding

        // 데이터 바인딩 및 처리 코드 추가
        // binding.requestItemTextviewId.text = "id : " + userModels[position].id.toString()
        binding.requestTextviewTitle.text = userModels[position].name
        Glide.with(binding.requestItemImageview.context)
            .load(userModels[position].img)
            .into(binding.requestItemImageview)

        // 수락 버튼 클릭
        binding.acceptFriendBtn.setOnClickListener {
            listener.onAcceptFriendRequest(userModels[position])
            val intent = Intent(holder.itemView.context, PlusFriendActivity::class.java)
            navigateToActivityListener.navigateToActivity(intent)
        }

        // 거절 버튼 클릭
        binding.rejectFriendBtn.setOnClickListener {
            listener.onRejectFriendRequest(userModels[position])
            val intent = Intent(holder.itemView.context, PlusFriendActivity::class.java)
            navigateToActivityListener.navigateToActivity(intent)
        }

    }

    fun updateData(newList: List<User>) {
        userModels = newList
        notifyDataSetChanged()
    }
}

class PlusFriendActivity : AppCompatActivity(), FriendRequestActionListener, NavigateToActivityListener {
    private lateinit var requestAdapter: FriendRequestAdapter
    private lateinit var originalUserModel: List<User>
    private val authToken = PrefApp.prefs.getString("accessToken", "default")
    private lateinit var retrofitService: RetrofitService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.title = "Add Friends"
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // back arrow
        val colorCode = "#C62E2E" // 색상 코드
        val color = Color.parseColor(colorCode) // 색상 코드를 Color 객체로 변환
        supportActionBar?.setBackgroundDrawable(ColorDrawable(color))

        val binding = ActivityPlusFriendBinding.inflate(layoutInflater)

        // 어뎁터 초기화
        requestAdapter = FriendRequestAdapter(emptyList(), this, this)
        binding.requestRecyclerView.adapter = requestAdapter

        // LayoutManager
        binding.requestRecyclerView.layoutManager = LinearLayoutManager(this)

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

        // 요청온 리스트 api 호출
        requestedFriend()

        val searchView = binding.searchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // 사용자가 검색 버튼을 눌렀을 때의 동작
                if (!query.isNullOrBlank()) {
                    // 검색어가 비어 있지 않으면 검색 함수 호출
                    //searchFriend(query)
                    addFriend(query)
                    finish()
                    startActivity(intent)
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                // 사용자가 검색어를 입력할 때의 동작
                return true
            }
        })

        setContentView(binding.root)

    }
    //--------------- 요청 받은 친구 리스트 api ---------------
    private fun requestedFriend() {
        val call = retrofitService.getRequestFriend()
        call.enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {

                if (response.isSuccessful) {
                    val userList = response.body()
                    userList?.let { users ->
                        // API 응답을 처리하는 코드
                        originalUserModel = users
                        requestAdapter.updateData(users)
                    }
                } else {
                    // 에러 처리
                    Log.e("henry", "requestFriend API request error: ${response.message()}")

                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                // 에러 처리
                Log.e("henry", "requestFriend API request failure: ${t.message}")
                t.printStackTrace()
            }
        })
    }

    //--------------- 친구 검색 api ---------------
    private fun addFriend(name: String) {
        val call = retrofitService.postAddFriend(name)
        call.enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                Log.d("henry", "addFriend API response: ${response.code()}")
                when (response.code()) {
                    200-> {showToast("친구 요청을 보냈습니다.") }
                    201 -> showToast("${name}님에게 친구 요청을 보냈습니다.")
                    //else show error
                    400 -> showToast("${name}님은 친구 요청을 거절한 상태입니다.")
                    401 -> showToast("권한 없음")
                    403 -> showToast("Forbidden")
                    404 -> showToast("${name}님을 찾을 수 없습니다.")
                    409 -> showToast("${name}님과 이미 친구 상태입니다.")
                    else -> Log.e("henry", "addFriend API error response: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Log.e("henry", "addFriend API request failure: ${t.message}")
                t.printStackTrace()
            }
        })
    }

    //--------------- 친구 수락 버튼 통신 ---------------
    override fun onAcceptFriendRequest(user: User) {
        val call = retrofitService.postAcceptFriend(user.id)
        call.enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                when (response.code()) {
                    200 -> showToast("${user.name}님의 친구 요청을 수락했습니다.")
                    201 -> showToast("${user.name}님의 친구 요청을 수락했습니다.")
                    //else show error
                    400 -> showToast("잘못된 요청입니다.")
                    401 -> showToast("권한 없음")
                    403 -> showToast("Forbidden")
                    404 -> showToast("Not Found")
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Log.e("henry", "onAcceptFriendRequest API request failure: ${t.message}")
                t.printStackTrace()
            }
        })
    }


    //--------------- 친구 거절 버튼 통신 ---------------
    override fun onRejectFriendRequest(user: User) {
        val call = retrofitService.deleteRequestedFriend(user.id)
        call.enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                when (response.code()) {
                    200 -> showToast("${user.name}님의 친구 요청을 거절했습니다.")
                    204 -> showToast("${user.name}님의 친구 요청을 거절했습니다.")
                    //else show error
                    400 -> showToast("삭제하려는 친구가 존재하지 않거나, 친구 신청관계가 아닙니다.")
                    401 -> showToast("권한 없음")
                    403 -> showToast("Forbidden")
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Log.e("henry", "onRejectFriendRequest API request failure: ${t.message}")
            }
        })
    }
    private fun showToast(message: String) {
        Toast.makeText(this@PlusFriendActivity, message, Toast.LENGTH_SHORT).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        startActivity(Intent(this, MainActivity::class.java))
        return true
    }

    override fun navigateToActivity(intent: Intent) {
        startActivity(Intent(this, PlusFriendActivity::class.java))
    }
}