package com.example.moapp

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MyScheduleDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_schedule_detail)

        title = "My Schedule"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val scheduleTableLayout: TableLayout = findViewById(R.id.scheduleTableDetail)

        // Retrieve data from MyDataManager
        val scheduleData = MyDataManager.getData()

        // Display data as a table
        for (entry in scheduleData.entries) {
            val key = entry.key
            val value = entry.value
            val row = TableRow(this)

            val params = TableRow.LayoutParams(100, 60)
            params.setMargins(2, 2, 2, 2)

            for (i in value.indices) {
                val cell = TextView(this)
                cell.text = value[i]
                cell.layoutParams = params
                cell.gravity = Gravity.CENTER

                if (key == "Time") {
                    cell.setTypeface(null, Typeface.BOLD)
                    cell.setBackgroundResource(R.drawable.bg_cell_table3)
                } else if (i == 0) {
                    cell.setBackgroundResource(R.drawable.bg_cell_table2)
                } else if (value[i] == "yes") {
                    cell.setTextColor(Color.parseColor("#2A9827"))
                    cell.setBackgroundResource(R.drawable.bg_cell_table4)
                } else {
                    cell.setBackgroundResource(R.drawable.bg_cell_table1)
                }

                row.addView(cell)
            }
            scheduleTableLayout.addView(row)
        }
    }
}




