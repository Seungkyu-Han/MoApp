package com.example.moapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moapp.databinding.ActivityCreateGroupTitleBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CreateGroupTitleActivity : AppCompatActivity() {
    private val retrofit: Retrofit = Retrofit.Builder().baseUrl("https://hangang-bike.site/")
        .addConverterFactory(GsonConverterFactory.create()).build()
    private val service = retrofit.create(RetrofitService::class.java)
    private val token = PrefApp.prefs.getString("accessToken", "default")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityCreateGroupTitleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fromDate = "${intent.getIntExtra("fromYear", 0)}-" +
                "${intent.getIntExtra("fromMonth", 0)}-" +
                "${intent.getIntExtra("fromDay", 0)}"

        val toDate = "${intent.getIntExtra("toYear", 0)}-" +
                "${intent.getIntExtra("toMonth", 0)}-" +
                "${intent.getIntExtra("toDay", 0)}"

        binding.fromDate.text = fromDate
        binding.toDate.text = toDate

        var friends: ArrayList<User>?

        service.getFriendList("Bearer $token").enqueue(object : Callback<ArrayList<User>> {
            override fun onResponse(call: Call<ArrayList<User>>, response: Response<ArrayList<User>>) {
                if (response.isSuccessful) {
                    friends = response.body()
                    binding.friendsList.layoutManager = LinearLayoutManager(this@CreateGroupTitleActivity)
                    binding.friendsList.adapter = FriendAdapter(friends as ArrayList<User>)
                    binding.friendsList.addItemDecoration(DividerItemDecoration(this@CreateGroupTitleActivity,
                        LinearLayoutManager.VERTICAL))
                    binding.makeGroupBtn.setOnClickListener {
                        val title = binding.titleEdit.text.toString()
                        if (title.length <= 0) {
                            Toast.makeText(this@CreateGroupTitleActivity, "제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                        } else if (friends!!.isEmpty()) {
                            Toast.makeText(this@CreateGroupTitleActivity, "공유방 친구를 추가해주세요.", Toast.LENGTH_SHORT).show()
                        } else {
                            service.createShareGroup(toDate, title, fromDate, friends as ArrayList<User>, token).
                            enqueue(object: Callback<Unit> {
                                override fun onResponse(
                                    call: Call<Unit>,
                                    response: Response<Unit>
                                ) {
                                    if (response.isSuccessful) {
                                        finish()
                                    } else {
                                        Toast.makeText(this@CreateGroupTitleActivity, "잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                override fun onFailure(call: Call<Unit>, t: Throwable) {
                                    Toast.makeText(this@CreateGroupTitleActivity, "잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                    }
                } else if (response.code() == 401) {
                    Toast.makeText(this@CreateGroupTitleActivity, "권한이 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ArrayList<User>>, t: Throwable) {
                Toast.makeText(this@CreateGroupTitleActivity, "잠시후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            }
        })


    }
}