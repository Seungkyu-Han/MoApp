package com.example.moapp

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.moapp.databinding.ActivityCreateGroupBinding
import com.example.moapp.databinding.ActivityCreateGroupToBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Month
import java.time.Year

class CreateGroupFromActivity : AppCompatActivity() {

    private val retrofit: Retrofit = Retrofit.Builder().baseUrl("https://hangang-bike.site/")
        .addConverterFactory(GsonConverterFactory.create()).build()
    private val service = retrofit.create(RetrofitService::class.java)
    private val token = PrefApp.prefs.getString("accessToken", "default")

    var sYear: Int = 0
    var sMonth: Int = 0
    var sDay: Int = 0
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityCreateGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setTitle("시작일 설정")

        val datePicker = binding.datePicker
        datePicker.minDate = System.currentTimeMillis()

        datePicker.setOnDateChangedListener { view, year, monthOfYear, dayOfMonth ->
            sYear = year.toInt()
            sMonth = monthOfYear.toInt()
            sDay = dayOfMonth.toInt()
        }

        binding.setFromBtn.setOnClickListener {
            if (sYear != 0 || sMonth != 0 || sDay != 0) {
                val intent = Intent(this, CreateGroupToActivity::class.java)
                intent.putExtra("fromYear", sYear)
                intent.putExtra("fromMonth", sMonth)
                intent.putExtra("fromDay", sDay)
                startActivity(intent)
                finish()
            }
        }
    }
}