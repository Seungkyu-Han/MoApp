package com.example.moapp

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.moapp.databinding.ActivitySettingBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import android.provider.OpenableColumns
import com.bumptech.glide.Glide
import java.io.FileOutputStream

data class UserInfo(
    val name: String,
    val img: String
)

class SettingActivity : AppCompatActivity() {
    lateinit var changeNameDialog: Dialog
    lateinit var binding: ActivitySettingBinding

    val retrofit = Retrofit.Builder().baseUrl("https://hangang-bike.site/")
        .addConverterFactory(GsonConverterFactory.create()).build()
    val service = retrofit.create(RetrofitService::class.java)

    private val PICK_IMAGE_REQUEST = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val backArrow = resources.getDrawable(R.drawable.ic_back_arrow, null)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // back arrow
        supportActionBar?.setHomeAsUpIndicator(backArrow)
        //
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


        binding.selectImage.setOnClickListener{
            Log.d("hien", "Change profile image button clicked")
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryIntent.type = "image/*"

            // Check if there is an application available to handle the intent
            if (galleryIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST)
                Log.d("hien", "Starting gallery intent")
            } else {
                Log.d("hien", "No app available to handle this action")
            }
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

        binding.logout.setOnClickListener {
            PrefApp.prefs.setString("accessToken", "")
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }

        service.getUserInfo("Bearer ${PrefApp.prefs.getString("accessToken", "default")}")?.enqueue(
            object : Callback<UserInfo> {
                override fun onResponse(call: Call<UserInfo>, response: Response<UserInfo>) {
                    if (response.isSuccessful) {
                        val userInfo = response.body()
                        userInfo?.let {
                            binding.userNameTextView.text = it.name
                            Glide.with(this@SettingActivity)
                                .load(it.img)
                                .into(binding.userImageView)
                        }
                    } else {
                        Log.d("hien", "Get User Info Error: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<UserInfo>, t: Throwable) {
                    Log.d("hien", "Get User Info Failure: ${t.message}")
                }
            }
        )
//충돌지점
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
                            binding.userNameTextView.text = nameInput.text.toString()
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
        val mainIntent = Intent(this, MainActivity::class.java)
        startActivity(mainIntent)
        return true
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            val selectedImageUri: Uri = data.data!!

            try {
                contentResolver.query(selectedImageUri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    val fileName = cursor.getString(nameIndex)
                    val inputStream = contentResolver.openInputStream(selectedImageUri)

                    inputStream?.use { input ->
                        val file = File(cacheDir, fileName)
                        FileOutputStream(file).use { output ->
                            input.copyTo(output)
                        }

                        //change image in setting page
                        Glide.with(this@SettingActivity)
                            .load(file)
                            .into(binding.userImageView)

                        val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                        val imagePart = MultipartBody.Part.createFormData("multipartFile", file.name, requestBody)
                        service.changeImage("Bearer ${PrefApp.prefs.getString("accessToken", "default")}", imagePart)
                            .enqueue(object : Callback<Unit> {
                                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                                    if (response.isSuccessful) {
                                        Log.d("hien", "Image upload successful")
                                        Toast.makeText(this@SettingActivity, "프로필 사진이 변경되었습니다", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Log.d("hien", "Image upload failed with code: ${response.code()}")
                                        Toast.makeText(this@SettingActivity, "Image upload failed", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                override fun onFailure(call: Call<Unit>, t: Throwable) {
                                    Log.d("hien", "Image upload failed: ${t.message}")
                                    Toast.makeText(this@SettingActivity, "Image upload failed", Toast.LENGTH_SHORT).show()
                                }
                            })
                    }
                }
            } catch (e: Exception) {
                Log.e("hien", "Exception: ${e.message}")
                e.printStackTrace()
            }
        }
    }

}



