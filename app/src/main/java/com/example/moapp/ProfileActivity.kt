package com.example.moapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        supportActionBar?.title = "Profile"

        var button: Button = findViewById(R.id.goToScheduleDetail)

        button.setOnClickListener{//go to ScheduleDetail page
            val scheduleIntent = Intent(this, ScheduleDetail::class.java)
            startActivity(scheduleIntent)
        }
    }
}