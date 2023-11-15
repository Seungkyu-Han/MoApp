package com.example.moapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Calendar

class AddMyScheduleActivity : AppCompatActivity() {
    lateinit var scheduleTable: TableLayout
    lateinit var calendarView: CalendarView
    lateinit var selectedDates: MutableList<String>
    val scheduleData = mutableListOf<MutableList<String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_my_schedule)

        title = "Add Schedule"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        scheduleTable = findViewById(R.id.scheduleTable)
        calendarView = findViewById(R.id.calendarView)
        selectedDates = mutableListOf()

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)

            val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
            val selectedDate = dateFormat.format(calendar.time)
            if (!selectedDates.contains(selectedDate)) {
                selectedDates.add(selectedDate)
                addDateToHeader(selectedDate)
            }
        }

        val addButton: Button = findViewById(R.id.addButton)
        addButton.setOnClickListener {
            scheduleTable.removeAllViews()
            generateScheduleTable()
        }

        val saveButton: Button = findViewById(R.id.saveButton)
        saveButton.setOnClickListener {
            // Save the data in MyDataManager
            for (rowIndex in 1 until scheduleTable.childCount) {
                val row = scheduleTable.getChildAt(rowIndex) as TableRow
                val rowData = mutableListOf<String>()

                for (cellIndex in 0 until row.childCount) {
                    val cell = row.getChildAt(cellIndex) as TextView
                    rowData.add(cell.text.toString())
                }
                scheduleData.add(rowData)
            }
            MyDataManager.saveData(selectedDates, scheduleData)


            // go to MyScheduleDetailActivity
            val intent = Intent(this, MyScheduleDetailActivity::class.java)
            startActivity(intent)
        }
    }


    private fun addDateToHeader(date: String) {
        // Add a column for the selected date
        val headerRow = scheduleTable.getChildAt(0) as TableRow
        val dateCell = TextView(this)
        dateCell.text = date
        dateCell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
        dateCell.setBackgroundResource(R.drawable.bg_cell_table3)
        dateCell.gravity = Gravity.CENTER
        val params = TableRow.LayoutParams(120, 80)
        params.setMargins(2, 2, 2, 2)
        dateCell.layoutParams = params
        headerRow.addView(dateCell, headerRow.childCount)
    }


    private fun generateScheduleTable() {
        //Create header row with Time and selected dates
        val headerRow = TableRow(this)
        val headerCell = TextView(this)
        headerCell.text = "Time"
        headerCell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
        headerCell.setBackgroundResource(R.drawable.bg_cell_table3)
        headerCell.gravity = Gravity.CENTER
        val params = TableRow.LayoutParams(120, 80)
        params.setMargins(2, 2, 2, 2)
        headerCell.layoutParams = params
        headerRow.addView(headerCell)

        for (date in selectedDates) {
            val dateCell = TextView(this)
            dateCell.text = date
            dateCell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            dateCell.setBackgroundResource(R.drawable.bg_cell_table3)
            dateCell.gravity = Gravity.CENTER
            dateCell.layoutParams = params
            headerRow.addView(dateCell)
        }

        scheduleTable.addView(headerRow)

        // Add time cell
        for (hour in 0..23) {
            val row = TableRow(this)
            val timeCell = TextView(this)
            timeCell.text = String.format("%02d:00", hour)
            timeCell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            timeCell.setBackgroundResource(R.drawable.bg_cell_table2)
            timeCell.gravity = Gravity.CENTER
            timeCell.layoutParams = params
            row.addView(timeCell)

            for (date in selectedDates) {
                val scheduleCell = TextView(this)
                scheduleCell.text = ""
                scheduleCell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                scheduleCell.setBackgroundResource(R.drawable.bg_cell_table1)
                scheduleCell.gravity = Gravity.CENTER
                scheduleCell.layoutParams = params
                row.addView(scheduleCell)

                // Add OnClickListener to each scheduleCell
                scheduleCell.setOnClickListener {
                    // Change background color to green(bg4)
                    scheduleCell.setBackgroundResource(R.drawable.bg_cell_table4)
                    scheduleCell.text = "yes"
                    scheduleCell.setTextColor(Color.parseColor("#2A9827"))
                }
            }
            scheduleTable.addView(row)
        }
    }

}