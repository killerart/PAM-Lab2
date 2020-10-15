package com.example.lab2

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.*
import com.applandeo.materialcalendarview.CalendarView
import com.example.lab2.callbacks.SwipeToDeleteCallback
import com.example.lab2.models.Reminder
import com.example.lab2.models.RemindersEventDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {
    private lateinit var calendarView: CalendarView
    private lateinit var recyclerView: RecyclerView
    private lateinit var reminderAdapter: ReminderAdapter

    private var selectedDay: Long = Calendar.getInstance().timeInMillis
    private var days: HashMap<Long, RemindersEventDay> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        readDataFromFile()

        reminderAdapter = ReminderAdapter(this)
        calendarView = findViewById<CalendarView>(R.id.calendarView).apply {
            selectedDay = firstSelectedDate.timeInMillis
            days[selectedDay]?.let { reminderEventDay ->
                reminderAdapter.setReminders(reminderEventDay.reminders)
            }
            setEvents(days.values.toList())
            setOnDayClickListener {
                if (it.calendar.get(Calendar.MONTH) == calendarView.firstSelectedDate.get(Calendar.MONTH)) {
                    selectedDay = it.calendar.timeInMillis
                    if (it is RemindersEventDay) {
                        reminderAdapter.setReminders(it.reminders)
                    } else {
                        reminderAdapter.clearReminders()
                    }
                }
            }
        }

        val viewManager = LinearLayoutManager(this)
        recyclerView = findViewById<RecyclerView>(R.id.recyclerView).apply {
            layoutManager = viewManager
            addItemDecoration(
                DividerItemDecoration(
                    this@MainActivity,
                    LinearLayoutManager.VERTICAL
                )
            )
            adapter = reminderAdapter
        }

        val itemTouchHelper = ItemTouchHelper(object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                reminderAdapter.removeReminderAt(viewHolder.adapterPosition)
                lifecycleScope.launch(Dispatchers.Default) {
                    writeDataToFile()
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    fun addReminder(view: View?) {
        Intent(this, AddReminderActivity::class.java).apply {
            putExtra(AddReminderActivity.KEY_DATE, selectedDay)
        }.also {
            startActivityForResult(it, REQUEST_ADD_REMINDER)
        }
    }

    private fun addReminderToMap(day: Long, reminder: Reminder) {
        var remindersEventDay = days[day]
        if (remindersEventDay == null) {
            remindersEventDay = RemindersEventDay(
                Calendar.getInstance().apply {
                    timeInMillis = day
                },
                R.drawable.ic_baseline_calendar_today_24
            )
            remindersEventDay.reminders.add(reminder)
            days[day] = remindersEventDay
        } else {
            remindersEventDay.reminders.add(reminder)
        }
    }

    private fun addReminderToDay(day: Long, reminder: Reminder) {
        addReminderToMap(day, reminder)
        lifecycleScope.launch(Dispatchers.Default) {
            writeDataToFile()
        }
        calendarView.setEvents(days.values.toList())
        reminderAdapter.addReminder(reminder)
    }

    private fun editReminderToDay(day: Long, newReminder: Reminder, position: Int) {
        days[day]?.let { remindersEventDay ->
            remindersEventDay.reminders.find {
                it.id == newReminder.id
            }?.let {
                it.name = newReminder.name
                it.dateTime = newReminder.dateTime
                it.description = newReminder.description
            }
            reminderAdapter.notifyItemChanged(position)
            reminderAdapter.updateItemAt(position)
            lifecycleScope.launch(Dispatchers.Default) {
                writeDataToFile()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ADD_REMINDER -> {
                if (resultCode == RESULT_OK && data != null) {
                    val day = data.getLongExtra(AddReminderActivity.KEY_DATE, 0)
                    val reminder = data.getParcelableExtra<Reminder>(KEY_REMINDER)
                    if (reminder != null) {
                        addReminderToDay(day, reminder)
                    }
                }
            }
            REQUEST_EDIT_REMINDER -> {
                if (resultCode == RESULT_OK && data != null) {
                    val day = data.getLongExtra(AddReminderActivity.KEY_DATE, 0)
                    val reminder = data.getParcelableExtra<Reminder>(KEY_REMINDER)
                    val position = data.getIntExtra(ReminderAdapter.KEY_POSITION, -1)
                    if (reminder != null && position != -1) {
                        editReminderToDay(day, reminder, position)
                    }
                }
            }
        }
    }

    fun removeReminder(reminder: Reminder) {
        days[selectedDay]?.run {
            reminders.remove(reminder)
            if (reminders.isEmpty()) {
                days.remove(selectedDay)
                calendarView.setEvents(days.values.toList())
                lifecycleScope.launch(Dispatchers.Default) {
                    writeDataToFile()
                }
            }
        }
    }

    private fun readDataFromFile() {
        val file = File(filesDir.absolutePath + File.separator + FILENAME)
//        file.delete()
        try {
            val inputStream = FileInputStream(file)
            val length = file.length().toInt()
            val byteArray = ByteArray(length)
            inputStream.read(byteArray, 0, length)
            inputStream.close()
            try {
                val jsonString = String(byteArray)
//                Log.d("Read JSON", jsonString)
                val allReminders = Json.decodeFromString<ArrayList<Reminder>>(jsonString)
                for (reminder in allReminders) {
                    addReminderToMap(reminder.dayInMillis, reminder)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        } catch (ex: FileNotFoundException) {
            file.createNewFile()
            lifecycleScope.launch(Dispatchers.Default) {
                writeDataToFile()
            }   
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    private fun writeDataToFile() {
        val file = File(filesDir.absolutePath + File.separator + FILENAME)
//        file.delete()
        try {
            val outputStream = FileOutputStream(file)
            val allReminders = arrayListOf<Reminder>()
            for (reminders in days.values.map { it.reminders }) {
                allReminders.addAll(reminders)
            }
            val jsonString = Json.encodeToString(allReminders)
//            Log.d("Write JSON", jsonString)
            outputStream.write(jsonString.toByteArray())
            outputStream.close()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    companion object {
        const val FILENAME = "reminders_data.json"
        const val REQUEST_ADD_REMINDER = 100
        const val REQUEST_EDIT_REMINDER = 101
        const val KEY_REMINDER = "KEY_REMINDER"
    }
}