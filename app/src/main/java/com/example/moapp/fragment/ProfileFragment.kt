package com.example.moapp.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.moapp.MainActivity
import com.example.moapp.PrefApp
import com.example.moapp.RetrofitService
import com.example.moapp.databinding.FragmentProfileBinding
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ProfileFragment : DialogFragment() {
    private val authToken = PrefApp.prefs.getString("accessToken", "default")
    private lateinit var retrofitService: RetrofitService

    companion object {
        const val ARG_USER_ID = "user_id"
        const val ARG_USER_NAME = "user_name"
        const val ARG_USER_IMG = "user_img"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentProfileBinding.inflate(inflater, container, false)

        arguments?.let {
            val userId = it.getInt(ARG_USER_ID, 0)
            val userName = it.getString(ARG_USER_NAME, "")
            val userImg = it.getString(ARG_USER_IMG, "")

            // 프로필 정보를 화면에 표시
            binding.profileTextviewId.text = "ID: $userId"
            binding.profileTextviewName.text = "Name: $userName"
            Glide.with(binding.profileImageview.context)
                .load(userImg)
                .into(binding.profileImageview)
            binding.removeFriendBtn.setOnClickListener{
                Log.d("henry", "${userName} 친구 삭제")
                removeFriend(userId, userName)
                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
            }
        }

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

        return binding.root
    }
    private fun removeFriend(id: Int, name:String) {
        val call = retrofitService.deleteFriend(id)
        call.enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                when (response.code()) {
                    200 -> showToast("${name}님을 삭제했습니다.")
                    204 -> showToast("${name}님을 삭제했습니다.")
                    //else show error
                    400 -> showToast("삭제하려는 친구가 존재하지 않거나, 이미 친구관계가 아닙니다.")
                    401 -> showToast("권한 없음")
                    403 -> showToast("Forbidden")
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Log.e("henry", "removeFriend API request failure: ${t.message}")
            }
        })
    }
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}
