package com.example.moapp.fragment

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.moapp.controller.ApiCallback
import com.example.moapp.controller.ApiRequestTask
import com.example.moapp.databinding.FragmentChatBinding
import com.example.moapp.databinding.ItemChatBinding
import com.example.moapp.databinding.ItemFriendsBinding
import com.example.moapp.model.ChatModel
import com.example.moapp.model.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

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
        binding.friendsItemTextviewId.id = userModels[position].id
        binding.friendsTextviewTitle.text = userModels[position].name
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

class FriendsFragment : Fragment(), ApiCallback {
    private lateinit var adapter: FriendsAdapter
    private lateinit var originalUserModel: List<User>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentChatBinding.inflate(inflater, container, false)

        // 어뎁터 초기화
        adapter = FriendsAdapter(emptyList())
        binding.recyclerView.adapter = adapter

        // 리사이클러 뷰에 LayoutManager 적용
        binding.recyclerView.layoutManager = LinearLayoutManager(activity)

        // JSON 파일에서 데이터 로드
        fetchDataFromApi()

        return binding.root
    }

    private fun fetchDataFromApi() {
        val apiUrl = "http://52.78.87.18:8080/api/friend/friend"
        val token = "eyJ0eXBlIjoiYWNjZXNzIiwiYWxnIjoiSFMyNTYifQ.eyJ1c2VySWQiOi0xMTM3NDgxMDA3LCJpYXQiOjE3MDAwNTI0MTAsImV4cCI6MTcwODY5MjQxMH0.Gb1g-_kK8cJPgh1NREZqcHg60RkJewjAVFS56Zmg8_U" // 여기에 실제 토큰을 넣어주세요

        val headers = mapOf(
            "accept" to "*/*",
            "Authorization" to "Bearer $token"
        )

        val apiRequestTask = ApiRequestTask(this)
        apiRequestTask.execute(apiUrl, headers)
    }

    override fun onSuccess(response: String?) {
        GlobalScope.launch(Dispatchers.Main) {
            response?.let {
                // API 응답을 처리하는 코드
                val userList = Gson().fromJson<List<User>>(
                    response,
                    object : TypeToken<List<User>>() {}.type
                ) ?: emptyList()
                originalUserModel = userList
                adapter.updateData(userList)
            }
        }
    }

    override fun onError(error: String) {
        // 에러 처리
        Log.e("FriendsFragment", "API request error: $error")
    }

    fun search(query: String) {
        val filteredList = originalUserModel.filter { user ->
            user.name.contains(query, true)
        }
        adapter.updateData(filteredList)
    }
}


// 정적 json
//class FriendsFragment : Fragment() {
//    private lateinit var adapter: FriendsAdapter
//    private lateinit var originalUserModel: List<User>
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val binding = FragmentChatBinding.inflate(inflater, container, false)
//
//        // JSON 파일에서 데이터 로드
//        val json = loadJsonFromAsset(requireContext(), "friends_data.json")
//
//        // User 객체로 변환
//        originalUserModel = Gson().fromJson<List<User>>(
//            json,
//            object : TypeToken<List<User>>() {}.type
//        ) ?: emptyList()
//
//        // 리사이클러 뷰에 LayoutManager, Adapter 적용
//        val layoutManager = LinearLayoutManager(activity)
//        binding.recyclerView.layoutManager = layoutManager
//
//        // 어뎁터 초기화
//        adapter = FriendsAdapter(originalUserModel)
//        binding.recyclerView.adapter = adapter
//        binding.recyclerView.addItemDecoration(FriendsDecoration(activity as Context))
//        return binding.root
//    }
//
//    private fun loadJsonFromAsset(context: Context, fileName: String): String? {
//        return try {
//            val inputStream = context.assets.open(fileName)
//            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
//            bufferedReader.use { it.readText() }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }
//    fun search(query: String) {
//        // adapter가 초기화되었는지 확인
//        if (::adapter.isInitialized) {
//            val filteredList = originalUserModel.filter { user ->
//                user.name.contains(query, true)
//            }
//            adapter.updateData(filteredList)
//        }
//    }
//
//}
