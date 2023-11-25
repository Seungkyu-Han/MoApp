package com.example.moapp

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class ScheduleDetail : AppCompatActivity() {
    private lateinit var scheduleTable: TableLayout
    private lateinit var addButton: Button
    private lateinit var retrofitService: RetrofitService

    // 2D array to hold cell IDs corresponding to scheduleEvent.id
    private val cellIdTable = Array(25) { IntArray(8) { -1 } }  // init value = -1

    private val authToken = PrefApp.prefs.getString("accessToken","default")

    // create a list of 15 colors to display the annotation
    private val colorList = listOf(
        Color.parseColor("#FF0000"), Color.parseColor("#33682E"),
        Color.parseColor("#0000FF"),Color.parseColor("#FFFF00"),
        Color.parseColor("#F2B5E4"),Color.parseColor("#B740C1"),
        Color.parseColor("#FFA800"),Color.parseColor("#00FF00"),
        Color.parseColor("#00FFFF"),Color.parseColor("#8F2727"),
        Color.parseColor("#FF00C7"),Color.parseColor("#EBDFC7"),
        Color.parseColor("#D0706A"),Color.parseColor("#796312"),Color.parseColor("#CECED4")
    )
    private var colorIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule_detail)

        val toolbar: Toolbar = findViewById(R.id.toolbar1)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""
        val upArrow = resources.getDrawable(R.drawable.ic_back_arrow, null)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // back arrow
        supportActionBar?.setHomeAsUpIndicator(upArrow)

        scheduleTable = findViewById(R.id.scheduleTable)
        addButton = findViewById(R.id.addButton)

        // initiate retrofit -----------------------------------------------------------
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()

                // Add headers
                val requestBuilder = original.newBuilder()
                    .header("accept", "*/*")
                    .header("Authorization", "Bearer $authToken")

                val modifiedRequest = requestBuilder.build()
                chain.proceed(modifiedRequest)
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://hangang-bike.site/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofitService = retrofit.create(RetrofitService::class.java)
        //------------------------------------------------------------------------------

        // always display scheduleTable
        createScheduleTable()

        //automatically call the getSchedule() function when this activity is accessed
        getSchedule()

        // Navigate to AddUserSchedule activity when addButton is clicked
        addButton.setOnClickListener {
            val addIntent = Intent(this, AddUserSchedule::class.java)
            startActivity(addIntent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val mainIntent = Intent(this, MainActivity::class.java)
        startActivity(mainIntent)
        return true
    }

    //retrieve schedule data from server : @GET("/api/user-schedule/schedule"), data class GetScheduleResponse
    private fun getSchedule() {
        val call = retrofitService.getUserSchedule()
        call.enqueue(object : Callback<GetScheduleResponse> {
            override fun onResponse(call: Call<GetScheduleResponse>, response: Response<GetScheduleResponse>) {
                if (response.isSuccessful) {
                    val scheduleResponse = response.body()
                    scheduleResponse?.let { scheduleEventResponse ->
                        for (scheduleEvent in scheduleEventResponse.scheduleEvents) {
                            Log.d("hien", "ID: ${scheduleEvent.id}, Day: ${scheduleEvent.day}, Event: ${scheduleEvent.eventName}, start: ${scheduleEvent.startTime}, end: ${scheduleEvent.endTime}")

                            // Update scheduleTable with scheduleEvent data
                            val color = getColor()
                            updateTableCell(scheduleEvent,color)

                        }
                    } ?: Log.d("hien", "No schedule data received")
                } else {
                    Log.d("hien", "GET Request failed: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<GetScheduleResponse>, t: Throwable) {
                Log.d("hien", "Failed to get schedule: ${t.message}")
                t.printStackTrace()
            }
        })
    }

    //createScheduleTable(): create Table 25 rows x 8 cols, no data, empty table
    private fun createScheduleTable() {
        val headers = arrayOf("", "월", "화", "수", "목", "금", "토", "일")
        val timeList = Array(24) { "    %2d ".format(it) }

        for (i in 0 until 25) {
            val row = TableRow(this)
            for (j in 0 until 8) {
                val cellText = when {
                    i == 0 -> headers[j]
                    j == 0 -> timeList[i - 1]
                    else -> " "
                }
                val cell = TextView(this)
                cell.text = cellText
                when {
                    i == 0 && j == 0 ->{
                        val params = TableRow.LayoutParams(30, 50)
                        cell.layoutParams = params
                    }
                    i == 0 -> {
                        val params = TableRow.LayoutParams(130, 50)
                        cell.layoutParams = params
                        cell.gravity = Gravity.CENTER
                        cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11F)
                        cell.setTextColor(Color.BLACK)
                        cell.background = resources.getDrawable(R.drawable.bg_cell_table)
                    }
                    j == 0 ->{
                        cell.layoutParams = TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.MATCH_PARENT
                        )
                        cell.gravity = Gravity.TOP or Gravity.END
                        cell.setPadding(0, 0, 0, 0)
                        cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10F)
                        cell.setTextColor(Color.BLACK)
                        cell.background = resources.getDrawable(R.drawable.bg_timecell_table)
                    }
                    else -> {
                        val params = TableRow.LayoutParams(130, 60)
                        cell.layoutParams = params
                        cell.gravity = Gravity.CENTER
                        cell.background = resources.getDrawable(R.drawable.bg_cell_table)
                    }
                }
                cell.setOnClickListener{
                    //if any cell is clicked, save clickedCellId in cellIdTable[][]
                    val rowIndex = scheduleTable.indexOfChild(row)
                    val columnIndex = row.indexOfChild(cell)
                    val clickedCellId = cellIdTable[rowIndex][columnIndex]
                    Log.d("hien", "Clicked Cell ID: $clickedCellId")


                    val cellColor = (cell.background as? ColorDrawable)?.color
                    //if colored cell is clicked, open dialog. // if white cell is clicked, do nothing
                    if (cellColor in colorList) {
                        val dialogBuilder = AlertDialog.Builder(this)  // ask if update or delete
                        dialogBuilder.setMessage("일정을 수정하거나 삭제하시겠습니까?")
                            .setCancelable(true)
                            .setNegativeButton("삭제") { dialog, id ->
                                // create another dialog for checking deletion-----------------
                                val deleteDialog = AlertDialog.Builder(this)  // ask if update or delete
                                deleteDialog.setMessage("정말 삭제하시겠습니까?")
                                    .setCancelable(true)
                                    .setPositiveButton("네") { dialog, id ->
                                        deleteSchedule(clickedCellId)
                                        //reset
                                        val intent = Intent(this, ScheduleDetail::class.java)
                                        startActivity(intent)
                                    }
                                    .setNegativeButton("아니요") { dialog, id ->
                                        //do nothing
                                    }
                                val dialog = deleteDialog.create()
                                dialog.show()
                                //---------------------------------------------------------------
                            }
                            .setPositiveButton("수정") { dialog, id ->
                                val updateIntent = Intent(this, UpdateUserSchedule::class.java)
                                updateIntent.putExtra("cellId", clickedCellId) // Pass cell ID to UpdateUserSchedule
                                startActivity(updateIntent)
                            }
                        val dialog = dialogBuilder.create()
                        dialog.show()
                    }
                }
                row.addView(cell)
            }
            scheduleTable.addView(row)
        }
    }


    //updateTableCell(): insert data cell (with color) into empty table
    private fun updateTableCell(scheduleEvent: ScheduleEvent, color: Int) {
        val rowIndexStart = scheduleEvent.startTime + 1  // startTime=1 -> 1:00 -> index=2
        val rowIndexEnd = scheduleEvent.endTime + 1     // endTime=1 -> 1:59 -> index=3, but at index 3 no color
        val columnIndex = calculateColumnIndex(scheduleEvent.day)

        // Retrieve the corresponding scheduleEvent ID, then save in cellIdTable
        val scheduleEventId = scheduleEvent.id
        for (rowIndex in rowIndexStart..rowIndexEnd) {
            cellIdTable[rowIndex][columnIndex] = scheduleEventId
        }

        for (rowIndex in rowIndexStart..rowIndexEnd) {
            val cell = getCellAt(rowIndex, columnIndex)
            cell.setBackgroundColor(color)
        }
        // Pass the color used for the cell to addAnnotation
        addAnnotation(scheduleEvent, color)
    }

    //calculateColumnIndex(): used for updateTableCell() func
    private fun calculateColumnIndex(day: String): Int {
        return when (day) {
            "Monday" -> 1
            "Tuesday" -> 2
            "Wednesday" -> 3
            "Thursday" -> 4
            "Friday" -> 5
            "Saturday" -> 6
            "Sunday" -> 7
            else -> 0
        }
    }

    //getCellAt(): used for updateTableCell() func
    private fun getCellAt(rowIndex: Int, columnIndex: Int): TextView {
        val tableRow = scheduleTable.getChildAt(rowIndex) as? TableRow
        val defaultTextView = TextView(this)
        if (tableRow != null) {
            val cell = tableRow.getChildAt(columnIndex) as? TextView
            if (cell != null) {
                return cell
            }
        }
        return defaultTextView
    }



    // getColor(): choose color from colorList
    private fun getColor(): Int {
        val color = colorList[colorIndex % colorList.size]
        colorIndex++
        return color
    }

    //addAnnotation(): add scheduleName as annotation text
    private fun addAnnotation(scheduleEvent: ScheduleEvent, color: Int) {
        val annotationText = "${scheduleEvent.eventName}"
        val annotationView = createAnnotationView(annotationText, color)
        val annotationsLayout = findViewById<GridLayout>(R.id.annotationsLayout)
        annotationsLayout.addView(annotationView)
    }

    //createAnnotationView(): design layout for each annotation: a colored rectangle + name text
    private fun createAnnotationView(text: String, color: Int): GridLayout {
        val annotationLayout = GridLayout(this)
        annotationLayout.columnCount = 2 // Set the number of columns to 2

        val rectangle = GradientDrawable()
        rectangle.shape = GradientDrawable.RECTANGLE
        rectangle.setColor(color)
        rectangle.setStroke(2, Color.BLACK)
        rectangle.cornerRadius = 8F

        val colorView = View(this)
        val colorLayoutParams = GridLayout.LayoutParams().apply {
            width = 60 // Width of the colored rectangle
            height = 40 // Height of the colored rectangle
            setMargins(10, 5, 10, 5)
            columnSpec = GridLayout.spec(0) // Column 0
        }
        colorView.layoutParams = colorLayoutParams
        colorView.background = rectangle

        val textView = TextView(this)
        textView.text = text
        textView.setPadding(10, 5, 10, 5)

        val textLayoutParams = GridLayout.LayoutParams().apply {
            width = GridLayout.LayoutParams.WRAP_CONTENT
            height = GridLayout.LayoutParams.WRAP_CONTENT
            columnSpec = GridLayout.spec(1) // Column 1
        }
        textView.layoutParams = textLayoutParams

        annotationLayout.addView(colorView)
        annotationLayout.addView(textView)

        return annotationLayout
    }

    // delete data in server : @DELETE("/api/user-schedule/schedule"), data class DeleteScheduleResponse
    private fun deleteSchedule(id: Int) {
        val call = retrofitService.deleteUserSchedule(id)

        call.enqueue(object : Callback<DeleteScheduleResponse> {
            override fun onResponse(call: Call<DeleteScheduleResponse>,response: Response<DeleteScheduleResponse>) {
                when (response.code()) {
                    // if success return to Schedule Detail page
                    200-> {
                        showToast("일정 삭제 성공")
                        Log.d("hien", "Schedule is deleted successfully")
                    }
                    //else show error
                    204 -> showToast("No Content")
                    400 -> showToast("해당 스케줄이 존재하지 않습니다.")
                    401 -> showToast("Unauthorizied")
                    403 -> showToast("Forbidden")
                }
            }

            override fun onFailure(call: Call<DeleteScheduleResponse>, t: Throwable) {
                Log.d("hien", "Schedule is deleted successfully")
                showToast("일정 삭제 성공")
            }
        })
    }

    // Show toast messages func
    private fun showToast(message: String) {
        Toast.makeText(this@ScheduleDetail, message, Toast.LENGTH_SHORT).show()
    }

}
