package com.example.moapp

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.moapp.databinding.ActivitySettingBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SettingActivity : AppCompatActivity() {
    lateinit var changeNameDialog: Dialog

    val retrofit = Retrofit.Builder().baseUrl("https://hangang-bike.site/")
        .addConverterFactory(GsonConverterFactory.create()).build()
    val service = retrofit.create(RetrofitService::class.java)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySettingBinding.inflate(layoutInflater)

        var mainIntent = Intent(this, MainActivity::class.java)
        val scheduleIntent = Intent(this, ScheduleDetail::class.java)
        var chatListIntent = Intent(this, GroupListActivity::class.java)
        var settingIntent = Intent(this, SettingActivity::class.java)

        supportActionBar?.title = "Settings"
        val colorCode = "#C62E2E" // 색상 코드
        val color = Color.parseColor(colorCode) // 색상 코드를 Color 객체로 변환
        supportActionBar?.setBackgroundDrawable(ColorDrawable(color))

        Log.d("park", "세팅 페이지 실행됨")
        service.getFriendState("Bearer ${PrefApp.prefs.getString("accessToken", "default")}")?.enqueue(
            object: Callback<Boolean> {
                override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                    if (response.isSuccessful && response.body() == true) {
                        binding.addFriendToggler.setChecked(true)
                        Log.d("park", "true로 세팅")
                    } else if (response.isSuccessful && response.body() == false) {
                        binding.addFriendToggler.setChecked(false)
                        Log.d("park", "false로 세팅")
                    } else {
                        Log.d("park", "기본 세팅 응답 오류")
                        Log.d("park", "${response.errorBody()?.string()}")

                    }
                }

                override fun onFailure(call: Call<Boolean>, t: Throwable) {
                    Log.d("park", "기본 세팅 오류")
                }
            }
        )

        service.getAddShareState("Bearer ${PrefApp.prefs.getString("accessToken", "default")}")?.enqueue(
            object: Callback<Boolean> {
                override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                    if (response.isSuccessful && response.body() == true) {
                        binding.addShareToggler.setChecked(true)
                    } else if (response.isSuccessful && response.body() == false) {
                        binding.addShareToggler.setChecked(false)
                    } else {
                        Log.d("park", "기본 세팅 오류")
                    }
                }

                override fun onFailure(call: Call<Boolean>, t: Throwable) {
                    Log.d("park", "기본 세팅 오류")
                }
            }
        )

        setContentView(binding.root)

        changeNameDialog = Dialog(this)
        changeNameDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        changeNameDialog.setContentView(R.layout.name_change_dialog)

        binding.changeNameBtn.setOnClickListener {
            showChangeNameDialog()
        }

        binding.addFriendToggler.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                service.changeFriendState("Bearer ${PrefApp.prefs.getString("accessToken", "default")}", state = true)?.enqueue(
                    object: Callback<Unit> {
                        override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                            if (response.isSuccessful) {
                                Toast.makeText(this@SettingActivity, "친구 요청 받기가 켜졌습니다.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@SettingActivity, "친구 알림 세팅 오류 발생", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<Unit>, t: Throwable) {
                            Toast.makeText(this@SettingActivity, "친구 알림 세팅 오류 발생", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            } else {
                service.changeFriendState("Bearer ${PrefApp.prefs.getString("accessToken", "default")}", state = false)?.enqueue(
                    object: Callback<Unit> {
                        override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                            if (response.isSuccessful) {
                                Toast.makeText(this@SettingActivity, "친구 요청 받기가 꺼졌습니다.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@SettingActivity, "친구 알림 세팅 오류 발생", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<Unit>, t: Throwable) {
                            Toast.makeText(this@SettingActivity, "친구 알림 세팅 오류 발생", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }

        binding.alarmToggler.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {

            }
        }

        binding.addShareToggler.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                service.changeAddShareState("Bearer ${PrefApp.prefs.getString("accessToken", "default")}", state = true)?.enqueue(
                    object: Callback<Unit> {
                        override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                            if (response.isSuccessful) {
                                Toast.makeText(this@SettingActivity, "일정 공유 요청 받기가 켜졌습니다.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@SettingActivity, "일정 공유 요청 알림 세팅 오류 발생", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<Unit>, t: Throwable) {
                            Toast.makeText(this@SettingActivity, "일정 공유 요청 알림 세팅 오류 발생", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            } else {
                service.changeAddShareState("Bearer ${PrefApp.prefs.getString("accessToken", "default")}", state = false)?.enqueue(
                    object: Callback<Unit> {
                        override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                            if (response.isSuccessful) {
                                Toast.makeText(this@SettingActivity, "일정 공유 요청 받기가 꺼졌습니다.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@SettingActivity, "일정 공유 요청 알림 세팅 오류 발생", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<Unit>, t: Throwable) {
                            Toast.makeText(this@SettingActivity, "일정 공유 요청 알림 세팅 오류 발생", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
        binding.bottomBar.friendsBtn.setOnClickListener {
            startActivity(mainIntent)
        }
        binding.bottomBar.scheduleBtn.setOnClickListener {
            startActivity(scheduleIntent)
        }
        binding.bottomBar.groupsBtn.setOnClickListener {
            startActivity(chatListIntent)
        }
        binding.bottomBar.settingsBtn.setOnClickListener {
            startActivity(settingIntent)
        }
    }

    private fun showChangeNameDialog(): Unit {
        changeNameDialog.show()

        val nameInput: EditText = changeNameDialog.findViewById(R.id.change_name_input)
        val nameSubmit: Button = changeNameDialog.findViewById(R.id.name_submit)
        val nameCancelBtn: Button = changeNameDialog.findViewById(R.id.name_cancel_btn)

        nameSubmit.setOnClickListener {
            if (nameInput.text.toString().length > 0) {
                service.changeName("Bearer ${PrefApp.prefs.getString("accessToken", "default")}",
                    nameInput.text.toString())?.enqueue(object : Callback<Unit> {
                    override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                        if (response.isSuccessful) {
                            Log.d("park", "이름 변경 성공")
                            Toast.makeText(this@SettingActivity, "이름이 변경되었습니다.", Toast.LENGTH_SHORT).show()
                            changeNameDialog.dismiss()
                        } else {
                            Log.d("park", "동일 이름 존재")
                            Toast.makeText(this@SettingActivity, "동일한 이름이 존재합니다.", Toast.LENGTH_SHORT).show()
                            changeNameDialog.dismiss()
                        }
                    }

                    override fun onFailure(call: Call<Unit>, t: Throwable) {
                        Log.d("park", "서버 에러")
                        Toast.makeText(this@SettingActivity, "서버 에러", Toast.LENGTH_SHORT).show()
                        changeNameDialog.dismiss()
                    }
                })
            }
        }

        nameCancelBtn.setOnClickListener {
            changeNameDialog.dismiss()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()   //go back
        return true
    }
}