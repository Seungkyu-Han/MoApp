package com.example.moapp

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.example.moapp.databinding.ActivityCreateGroupToBinding
import java.util.Calendar

class CreateGroupToActivity : AppCompatActivity() {
    var sYear: Int = 0
    var sMonth: Int = 0
    var sDay: Int = 0
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityCreateGroupToBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setTitle("종료일 설정")

        val fromYear = intent.getIntExtra("fromYear", 0)
        val fromMonth = intent.getIntExtra("fromMonth", 0)
        val fromDay = intent.getIntExtra("fromDay", 0)

        val calendar = Calendar.getInstance()
        calendar.set(fromYear, fromMonth, fromDay)

        val datePicker = binding.datePicker
        datePicker.minDate = calendar.timeInMillis
        datePicker.maxDate = calendar.timeInMillis + (7 * 1000 * 60 * 60 * 24)

        datePicker.setOnDateChangedListener { view, year, monthOfYear, dayOfMonth ->
            sYear = year
            sMonth = monthOfYear
            sDay = dayOfMonth
        }

        binding.setToBtn.setOnClickListener {
            if (sYear != 0 || sMonth != 0 || sDay != 0) {
                val intent = Intent(this, CreateGroupTitleActivity::class.java)
                intent.putExtra("fromYear", fromYear)
                intent.putExtra("fromMonth", fromMonth)
                intent.putExtra("fromDay", fromDay)
                intent.putExtra("toYear", sYear)
                intent.putExtra("toMonth", sMonth)
                intent.putExtra("toDay", sDay)
                startActivity(intent)
                finish()
            }
        }
    }
}