package com.example.moapp

import android.app.ActionBar.LayoutParams
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.example.moapp.databinding.ActivityGroupScheduleBinding
import com.example.moapp.model.Group
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class GroupScheduleActivity : AppCompatActivity() {
    private val retrofit: Retrofit = Retrofit.Builder().baseUrl("https://hangang-bike.site/")
        .addConverterFactory(GsonConverterFactory.create()).build()
    private val service = retrofit.create(RetrofitService::class.java)
    private val token = PrefApp.prefs.getString("accessToken", "default")
    lateinit var groupInfo: Group
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityGroupScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val groupId = intent.getIntExtra("groupId", -1)
        var calDate: Int

        supportActionBar?.title = "그룹 일정"
        val colorCode = "#C62E2E" // 색상 코드
        val color = Color.parseColor(colorCode) // 색상 코드를 Color 객체로 변환
        supportActionBar?.setBackgroundDrawable(ColorDrawable(color))

        service.getGroupInfo(groupId, "Bearer $token").enqueue(object : Callback<Group> {
            override fun onResponse(call: Call<Group>, response: Response<Group>) {
                if (response.isSuccessful) {
                    groupInfo = response.body() as Group
                    val startDate = SimpleDateFormat("yyyy-MM-dd").parse(groupInfo.startDate)
                    val endDate = SimpleDateFormat("yyyy-MM-dd").parse(groupInfo.endDate)
                    calDate = ((endDate.time - startDate.time) / (1000 * 60 * 60 * 24)).toInt()

                    // Table Initialize
                    val dateArray = ArrayList<String>()
                    val row = TableRow(this@GroupScheduleActivity)

                    val textView = TextView(this@GroupScheduleActivity)
                    val params = TableRow.LayoutParams(120,50)
                    textView.layoutParams = params
                    textView.setText("")
                    textView.background = resources.getDrawable(R.drawable.bg_cell_table)
                    textView.gravity = Gravity.CENTER
                    row.addView(textView)

                    for ( i: Int in 0..(calDate - 1)) {
                        val date = Calendar.getInstance()
                        date.set(startDate.year + 1900, startDate.month + 1, startDate.date)
                        date.add(Calendar.DAY_OF_MONTH, i)

                        var month = 0
                        if (date.get(Calendar.MONTH).toInt() == 0) {
                            month = 12
                        } else {
                            month = date.get(Calendar.MONTH)
                        }
                        dateArray.add("${month}/${date.get(Calendar.DAY_OF_MONTH)}")


                        val textView = TextView(this@GroupScheduleActivity)
                        val params = TableRow.LayoutParams(120,50)
                        textView.layoutParams = params
                        textView.setText(dateArray[i])
                        textView.background = resources.getDrawable(R.drawable.bg_cell_table)
                        textView.gravity = Gravity.CENTER
                        row.addView(textView)
                    }
                    binding.schedule.addView(row)

                    for (i: Int in 0..23) {
                        val row = TableRow(this@GroupScheduleActivity)
                        val textView = TextView(this@GroupScheduleActivity)
                        val params = TableRow.LayoutParams(120,50)
                        textView.layoutParams = params
                        textView.setText(resources.getStringArray(R.array.times_array)[i].toString())
                        textView.background = resources.getDrawable(R.drawable.bg_cell_table)
                        textView.gravity = Gravity.CENTER
                        row.addView(textView)
                        for (j in dateArray) {
                            val textView = TextView(this@GroupScheduleActivity)
                            textView.layoutParams = params
                            textView.setText(" ")
                            textView.background = resources.getDrawable(R.drawable.bg_cell_table)
                            row.addView(textView)
                        }
                        binding.schedule.addView(row)
                    }


                service.getGroupSchedule(groupId, "Bearer $token").enqueue(object : Callback<ArrayList<ArrayList<Boolean>>> {
                    override fun onResponse(
                        call: Call<ArrayList<ArrayList<Boolean>>>,
                        response: Response<ArrayList<ArrayList<Boolean>>>
                    ) {
                        if (response.isSuccessful) {
                            val flag: ArrayList<ArrayList<Boolean>> = response.body() as ArrayList<ArrayList<Boolean>>
                            for (i in 1..24) {
                                for (j in 1 .. calDate) {
                                    if (!(flag[j - 1][i - 1])) {
                                        val row = binding.schedule.getChildAt(i) as TableRow
                                        val target = row.getChildAt(j) as TextView
                                        target.setBackgroundColor(Color.RED)
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(this@GroupScheduleActivity, "스케쥴 정보를 가져올 수 없습니다.",
                                Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ArrayList<ArrayList<Boolean>>>, t: Throwable) {
                        Toast.makeText(this@GroupScheduleActivity, "잠시 후 다시 이용해주세요.",
                            Toast.LENGTH_SHORT).show()
                    }
                })


                } else {
                    Toast.makeText(this@GroupScheduleActivity, "서버 오류", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Group>, t: Throwable) {
                Log.e("park", t.toString())
                Toast.makeText(this@GroupScheduleActivity, "잠시후 다시 이용해주세요.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}