package com.example.moapp

import android.app.ActionBar.LayoutParams
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
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
import com.example.moapp.model.requstInfo
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
    private var groupId: Int = 0
    lateinit var groupInfo: Group
    var calDate: Int = 0
    var dateArray = ArrayList<String>()
    lateinit var binding: ActivityGroupScheduleBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        groupId = intent.getIntExtra("groupId", -1)

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
                    binding.title.text = groupInfo.name

                    // Table Initialize
                    val row = TableRow(this@GroupScheduleActivity)

                    val textView = TextView(this@GroupScheduleActivity)
                    val params = TableRow.LayoutParams(120,50)
                    textView.layoutParams = params
                    textView.setText("")
                    textView.background = resources.getDrawable(R.drawable.bg_cell_table)
                    textView.gravity = Gravity.CENTER
                    row.addView(textView)

                    for ( i: Int in 0..calDate) {
                        val date = Calendar.getInstance()
                        date.set(startDate.year + 1900, startDate.month + 1, startDate.date)
                        date.add(Calendar.DAY_OF_MONTH, i)

                        var month = 0
                        if (date.get(Calendar.MONTH) == 0) {
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

                    refreshSchedule(calDate, binding)

                    service.getRequestedSchedule(groupId, "Bearer $token").enqueue(object : Callback<requstInfo> {
                        override fun onResponse(
                            call: Call<requstInfo>,
                            response: Response<requstInfo>
                        ) {
                            if (response.isSuccessful) {
                                val start = response.body()?.startTime
                                val end = response.body()?.endTime
                                val getDate = response.body()?.date

                                val reqDate = Calendar.getInstance()

                                val dateFormat = SimpleDateFormat("yyyy-MM-dd")
                                val date = dateFormat.parse(getDate)

                                reqDate.time = date

                                var month = reqDate.get(Calendar.MONTH) + 1

                                var idx: Int = -1
                                for (i: Int in 0..calDate) {
                                    if (dateArray[i] == "${month}/${reqDate.get(Calendar.DAY_OF_MONTH)}") {
                                        idx = i
                                        break
                                    }
                                }
                                Log.d("park", "start = $start end = $end")
                                Log.d("park", "state ${response.body()?.state}")

                                if (response.body()?.state == "Req") {
                                    if (start != null && end != null) {
                                        for (i: Int in start..end) {
                                            val row = binding.schedule.getChildAt(i + 1) as TableRow
                                            val target = row.getChildAt(idx + 1) as TextView
                                            target.setBackgroundColor(Color.YELLOW)
                                        }
                                    }



                                    binding.addBtn.visibility = View.GONE
                                    binding.reqBtn.text = "일정 확인"
                                    binding.reqBtn.setOnClickListener {
                                        val builder = AlertDialog.Builder(this@GroupScheduleActivity)
                                            .setTitle("확인")
                                            .setMessage("일정을 수락하시겠습니까?")
                                            .setPositiveButton("수락", DialogInterface.OnClickListener { dialog, which ->
                                                service.reqResponse(true, groupId, "Bearer $token").enqueue(object : Callback<Unit> {
                                                    override fun onResponse(
                                                        call: Call<Unit>,
                                                        response: Response<Unit>
                                                    ) {
                                                        if (response.isSuccessful) {
                                                            finish()
                                                        } else {
                                                            Toast.makeText(this@GroupScheduleActivity,
                                                                "서버 오류입니다.", Toast.LENGTH_SHORT).show()
                                                            Log.d("park", response.code().toString())
                                                        }
                                                    }

                                                    override fun onFailure(
                                                        call: Call<Unit>,
                                                        t: Throwable
                                                    ) {
                                                        Toast.makeText(this@GroupScheduleActivity,
                                                            "일시적 오류입니다.", Toast.LENGTH_SHORT).show()
                                                    }
                                                })
                                            })
                                            .setNegativeButton("거절", DialogInterface.OnClickListener { dialog, which ->
                                                service.reqResponse(false, groupId, "Bearer $token").enqueue(object : Callback<Unit> {
                                                    override fun onResponse(
                                                        call: Call<Unit>,
                                                        response: Response<Unit>
                                                    ) {
                                                        if (response.isSuccessful) {
                                                            finish()
                                                        } else {
                                                            Toast.makeText(this@GroupScheduleActivity,
                                                                "서버 오류입니다.", Toast.LENGTH_SHORT).show()
                                                            Log.d("park", response.code().toString())
                                                        }
                                                    }

                                                    override fun onFailure(
                                                        call: Call<Unit>,
                                                        t: Throwable
                                                    ) {
                                                        Toast.makeText(this@GroupScheduleActivity,
                                                            "일시적 오류입니다.", Toast.LENGTH_SHORT).show()
                                                    }
                                                })
                                            })
                                        builder.show()
                                    }
                                } else if (response.body()?.state == "Active") {
                                    if (start != null && end != null) {
                                        for (i: Int in start..end) {
                                            val row = binding.schedule.getChildAt(i + 1) as TableRow
                                            val target = row.getChildAt(idx) as TextView
                                            target.setBackgroundColor(Color.GREEN)
                                        }
                                    }

                                    binding.addBtn.visibility = View.GONE
                                    binding.reqBtn.visibility = View.GONE
                                }
                                else {
                                    binding.addBtn.setOnClickListener {
                                        val intent = Intent(this@GroupScheduleActivity, AddGroupScheduleActivity::class.java)
                                        intent.putExtra("groupId", groupId)
                                        startActivity(intent)
                                        refreshSchedule(calDate, binding)
                                    }

                                    binding.reqBtn.setOnClickListener {
                                        val intent = Intent(this@GroupScheduleActivity, RequestActivity::class.java)
                                        intent.putExtra("groupId", groupId)
                                        startActivity(intent)
                                        refreshSchedule(calDate, binding)
                                    }
                                }
                            }
                        }

                        override fun onFailure(call: Call<requstInfo>, t: Throwable) {
                            Toast.makeText(this@GroupScheduleActivity, "잠시후 다시 이용해주세요.", Toast.LENGTH_SHORT).show()
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

    override fun onResume() {
        super.onResume()
        refreshSchedule(calDate, binding)
    }

    fun refreshSchedule(calDate: Int, binding: ActivityGroupScheduleBinding) {
        service.getGroupSchedule(groupId, "Bearer $token").enqueue(object : Callback<ArrayList<ArrayList<Boolean>>> {
            override fun onResponse(
                call: Call<ArrayList<ArrayList<Boolean>>>,
                response: Response<ArrayList<ArrayList<Boolean>>>
            ) {
                if (response.isSuccessful) {
                    val flag: ArrayList<ArrayList<Boolean>> = response.body() as ArrayList<ArrayList<Boolean>>
                    for (i in 1..24) {
                        for (j in 1 .. calDate + 1) {
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
    }
}