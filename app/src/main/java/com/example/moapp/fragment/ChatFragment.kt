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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.moapp.databinding.FragmentChatBinding
import com.example.moapp.databinding.ItemChatBinding
import com.example.moapp.model.ChatModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader

class MyViewHolder(val binding: ItemChatBinding) :
    RecyclerView.ViewHolder(binding.root)
class MyAdapter(var chatModels: List<ChatModel>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // 항목의 개수를 판단하기 위해 자동 호출
    override fun getItemCount(): Int {
        return chatModels.size
    }
    // 항목 뷰를 가지는 뷰 홀더를 준비하기 위해 자동 호출
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            RecyclerView.ViewHolder =
        MyViewHolder(
            ItemChatBinding.inflate(LayoutInflater.from(
                parent.context), parent, false))
    // RecyclerViewAdapter 내의 onBindViewHolder 메서드에서의 코드 일부분
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // 여러 사용자에 대한 처리
        val binding = (holder as com.example.moapp.fragment.MyViewHolder).binding
        val usersStringBuilder = StringBuilder()
        for (user in chatModels[position].users) {
            usersStringBuilder.append(user.name).append(", ")
        }

        val usersText = usersStringBuilder.toString().removeSuffix(", ") // 마지막 ", " 제거

        // 데이터 바인딩 및 처리 코드 추가
        binding.chatTextviewTitle.text = "$usersText 님과의 채팅"
        val lastMessageKey = chatModels[position].comments.keys.lastOrNull()
        binding.chatItemTextviewLastmessage.text =
            chatModels[position].comments[lastMessageKey]?.message ?: ""

        // 채팅의 마지막 사용자 이미지를 사용
        val lastUserIndex = chatModels[position].users.size - 1
        Glide.with(binding.chatItemImageview.context)
            .load(chatModels[position].users[lastUserIndex].img)
            .into(binding.chatItemImageview)
    }
    // 데이터 업데이트를 위한 메서드
    fun updateData(newList: List<ChatModel>) {
        chatModels = newList
        notifyDataSetChanged()
    }
}

class MyDecoration(val context: Context): RecyclerView.ItemDecoration() {
    // 각 항목을 꾸미기 위해 호출
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.set(10, 10, 10, 0)
        view.setBackgroundColor(Color.parseColor("#A4C1EE"))
        ViewCompat.setElevation(view, 20.0f)
    }
}

class ChatFragment : Fragment() {
    private lateinit var adapter: MyAdapter
    private lateinit var originalChatModel: List<ChatModel>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentChatBinding.inflate(inflater, container, false)

        // JSON 파일에서 데이터 로드
        val json = loadJsonFromAsset(requireContext(), "chat_data.json")

        // chatModel이 null인 경우 기본값으로 초기화
        originalChatModel = Gson().fromJson<List<ChatModel>>(
            json,
            object : TypeToken<List<ChatModel>>() {}.type
        ) ?: emptyList()

        // 리사이클러 뷰에 LayoutManager, Adapter 적용
        val layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.layoutManager = layoutManager

        // 어댑터 초기화
        adapter = MyAdapter(originalChatModel)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(MyDecoration(activity as Context))
        return binding.root
    }
    private fun loadJsonFromAsset(context: Context, fileName: String): String? {
        return try {
            val inputStream = context.assets.open(fileName)
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            bufferedReader.use { it.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 검색 기능을 처리하는 메서드
    fun search(query: String) {
        val filteredList = originalChatModel.filter { chat ->
            chat.roomId.contains(query, true)
        }
        adapter.updateData(filteredList)
    }

}
