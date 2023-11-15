package com.example.moapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.moapp.databinding.ActivityLoginBinding
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val retrofit = Retrofit.Builder().baseUrl("http://52.78.87.18:8080/")
            .addConverterFactory(GsonConverterFactory.create()).build()
        val service = retrofit.create(RetrofitService::class.java)

        KakaoSdk.init(this, "8741da3f615e0e9329815dfaa4a36510")

        Log.d("park", "keyhash: ${Utility.getKeyHash(this)}")

        binding.loginBtn.setOnClickListener {
            Log.d("park", "Clicked Login Btn")
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
                UserApiClient.instance.loginWithKakaoTalk(this) {token, error ->
                    if (error != null) {
                        Log.e("park", "로그인 실패", error)
                    }
                    else if (token != null) {
                        Log.i("park", "로그인 성공 ${token.accessToken}")
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }
                }
            } else {
                Log.d("park", "카톡 없음")
            }
        }
    }
}