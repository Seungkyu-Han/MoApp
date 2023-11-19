package com.example.moapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.moapp.controller.ApiCallback
import com.example.moapp.controller.ApiRequestTask
import com.example.moapp.controller.SearchApiCallback
import com.example.moapp.databinding.ActivityPlusFriendBinding
import com.example.moapp.databinding.ItemRequestFriendBinding
import com.example.moapp.fragment.ProfileFragment
import com.example.moapp.model.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RequestViewHolder(val binding: ItemRequestFriendBinding) : RecyclerView.ViewHolder(binding.root)

class FriendRequestAdapter(var userModels: List<User>) : RecyclerView.Adapter<RequestViewHolder>() {
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
        binding.requestItemTextviewId.id = userModels[position].id
        binding.requestTextviewTitle.text = userModels[position].name
        Glide.with(binding.requestItemImageview.context)
            .load(userModels[position].img)
            .into(binding.requestItemImageview)

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

interface RequestFriendCallback {
    fun onRequestSuccess(response: String?)
    fun onRequestError(error: String)
}

interface SearchFriendCallback {
    fun onSearchSuccess(response: String?)
    fun onSearchError(error: String)
}
class PlusFriendActivity : AppCompatActivity(),ApiCallback, SearchApiCallback {
    private lateinit var requestAdapter: FriendRequestAdapter
    private lateinit var originalUserModel: List<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityPlusFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 어뎁터 초기화
        requestAdapter = FriendRequestAdapter(emptyList())
        binding.requestRecyclerView.adapter = requestAdapter

        // LayoutManager
        binding.requestRecyclerView.layoutManager = LinearLayoutManager(this)

        // 요청온 리스트 api 호출
        requestFriend()

        val searchView = binding.searchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // 사용자가 검색 버튼을 눌렀을 때의 동작
                if (!query.isNullOrBlank()) {
                    // 검색어가 비어 있지 않으면 검색 함수 호출
                    //searchFriend(query)
                    searchFriend(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // 사용자가 검색어를 입력할 때의 동작
                return true
            }
        })
    }

    private fun requestFriend() {
        val apiUrl = "http://52.78.87.18:8080/api/friend/add-friend"
        val token =
            "eyJ0eXBlIjoiYWNjZXNzIiwiYWxnIjoiSFMyNTYifQ.eyJ1c2VySWQiOi0xMTM3NDgxMDA3LCJpYXQiOjE3MDAwNTI0MTAsImV4cCI6MTcwODY5MjQxMH0.Gb1g-_kK8cJPgh1NREZqcHg60RkJewjAVFS56Zmg8_U"  // 실제 토큰으로 교체

        val headers = mapOf(
            "accept" to "*/*",
            "Authorization" to "Bearer $token"
        )

        val apiRequestTask = ApiRequestTask(callback = this@PlusFriendActivity)
        apiRequestTask.execute(apiUrl, headers)
    }

    // 요청온 리스트 api 처리
    override fun onRequestSuccess(response: String?) {
        // 성공적인 응답 처리
        GlobalScope.launch(Dispatchers.Main) {
            response?.let {
                // API 응답을 처리하는 코드
                val userList = Gson().fromJson<List<User>>(
                    response,
                    object : TypeToken<List<User>>() {}.type
                ) ?: emptyList()
                originalUserModel = userList
                requestAdapter.updateData(userList)
            }
        }
    }

    override fun onRequestError(error: String) {
        // error
        Log.e("RequestFriends", "API request error: $error")
    }


    // 친구 검색 api
    private fun searchFriend(query: String) {
        val apiUrl = "http://52.78.87.18:8080/api/friend/search?query=$query"
        val token =
            "eyJ0eXBlIjoiYWNjZXNzIiwiYWxnIjoiSFMyNTYifQ.eyJ1c2VySWQiOi0xMTM3NDgxMDA3LCJpYXQiOjE3MDAwNTI0MTAsImV4cCI6MTcwODY5MjQxMH0.Gb1g-_kK8cJPgh1NREZqcHg60RkJewjAVFS56Zmg8_U"  // 실제 토큰으로 교체

        val headers = mapOf(
            "accept" to "*/*",
            "Authorization" to "Bearer $token"
        )

        val apiRequestTask = ApiRequestTask(callback = SearchFriendCallback)
        apiRequestTask.execute(apiUrl, headers)
    }
    override fun onSearchSuccess(response: String?) {
        // 성공적인 응답 처리
        Toast.makeText(this, "친구 요청을 보냈습니다", Toast.LENGTH_SHORT).show()
    }

    override fun onSearchError(error: String) {
        // 오류 처리
        Log.e("PlusFriendActivity", "API request error: $error")

        when {
            error.contains("400") -> {
                Toast.makeText(this, "잘못된 요청: $error", Toast.LENGTH_SHORT).show()
            }
            error.contains("401") -> {
                Toast.makeText(this, "인증 실패: $error", Toast.LENGTH_SHORT).show()
            }
            // 추가적인 오류 처리 로직을 여기에 추가
            else -> {
                Toast.makeText(this, "알 수 없는 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }
}