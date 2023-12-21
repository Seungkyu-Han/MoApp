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
import com.kakao.sdk.user.UserApi
import com.kakao.sdk.user.UserApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

class LoginActivity : AppCompatActivity() {

    private val retrofit: Retrofit = Retrofit.Builder().baseUrl("https://hangang-bike.site/")
        .addConverterFactory(GsonConverterFactory.create()).build()

    private val service = retrofit.create(RetrofitService::class.java)

    val callback:(OAuthToken?, Throwable?)-> Unit = {token, error ->
        if(error != null)
            Log.e("park", "로그인 실패", error)
        else if(token != null) {
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()


        KakaoSdk.init(this, "19f26b64744912066f59c25751bdfd57")

        Log.d("park", "keyhash: ${Utility.getKeyHash(this)}")

        binding.loginBtn.setOnClickListener {
            Log.d("park", "Clicked Login Btn")


            if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
                UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
                    if (error != null) {
                        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                            return@loginWithKakaoAccount
                        }

                        Log.d("sksk", "웹으로 로그인 시도")
                        UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
                            if (error != null) {
                                Log.e("sksksk", "카카오계정으로 로그인 실패", error)
                            } else if (token != null) {
                                Log.i("sksksk", "카카오계정으로 로그인 성공 ${token.accessToken}")
                            }
                        }
                    } else if (token != null) {
                        Log.i("park", "로그인 성공 ${token.accessToken}")
                        callback(token, null)
                    }
                }
                /**
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
                }**/
            }
        }

    }
}