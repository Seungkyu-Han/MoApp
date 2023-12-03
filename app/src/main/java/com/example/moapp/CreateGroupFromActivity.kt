package com.example.moapp

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.moapp.databinding.ActivityCreateGroupBinding
import com.example.moapp.databinding.ActivityCreateGroupToBinding
import java.time.Month
import java.time.Year

class CreateGroupFromActivity : AppCompatActivity() {
    var sYear: Int = 0
    var sMonth: Int = 0
    var sDay: Int = 0
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityCreateGroupBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_create_group)

        supportActionBar?.setTitle("시작일 설정")

        val datePicker = binding.datePicker
        datePicker.minDate = System.currentTimeMillis()

        datePicker.setOnDateChangedListener { view, year, monthOfYear, dayOfMonth ->
            sYear = year.toInt()
            sMonth = monthOfYear.toInt()
            sDay = dayOfMonth.toInt()
            Log.d("park", "$sYear $sMonth $sDay")
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