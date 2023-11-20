package com.example.moapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.kakao.sdk.common.util.Utility
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    val retrofit = Retrofit.Builder().baseUrl("https://hangang-bike.site/")
        .addConverterFactory(GsonConverterFactory.create()).build()
    val service = retrofit.create(RetrofitService::class.java)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var settingIntent = Intent(this, SettingActivity::class.java)
        var loginIntent = Intent(this, LoginActivity::class.java)

        service.loginCheck("Bearer ${PrefApp.prefs.getString("accessToken", "default")}")?.enqueue(
            object : Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (response.isSuccessful) {
                        Log.d("park", "자동 로그인 되었습니다.")
                        startActivity(settingIntent)
                    }
                    else {
                        Log.d("park", "자동 로그인 안됨.")
                        Log.d("park", "${response.errorBody()?.string()}")
                        startActivity(loginIntent)
                        startActivity(settingIntent)
                    }
                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    Log.d("park", "자동 로그인 오류.")
                }
            }
        )
    }
}