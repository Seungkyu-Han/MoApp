package com.example.moapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.moapp.databinding.ActivityLoginBinding
import com.example.moapp.databinding.ActivityMainBinding
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val retrofit = Retrofit.Builder().baseUrl("https://hangang-bike.site/")
            .addConverterFactory(GsonConverterFactory.create()).build()
        val service = retrofit.create(RetrofitService::class.java)


        KakaoSdk.init(this, "19f26b64744912066f59c25751bdfd57")

        Log.d("park", "keyhash: ${Utility.getKeyHash(this)}")

        binding.loginBtn.setOnClickListener {
            Log.d("park", "Clicked Login Btn")

            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    Log.e("park", "로그인 실패", error)
                }
                else if (token != null) {
                    Log.i("park", "로그인 성공 ${token.accessToken}")

                    service.login(token.accessToken)?.enqueue(object : Callback<AccessToken> {
                        override fun onResponse(call: Call<AccessToken>, response: Response<AccessToken>) {
                            Log.d("park", "서버통신")
                            if (response.isSuccessful) {
                                var result: AccessToken? = response.body()
                                Log.d("park", "토큰 로그인 성공 ${result?.toString()}")
                                PrefApp.prefs.setString("accessToken", result?.accessToken.toString())
                                finish()
                            } else {
                                Log.e("park", "토큰 로그인 실패, 관리자에게 문의 ${response.errorBody()?.string()}")
                            }
                        }
                        override fun onFailure(call: Call<AccessToken>, t: Throwable) {
                            Log.e("park", "예외 발생")
                        }
                    })
                }
            }
        }
    }


}