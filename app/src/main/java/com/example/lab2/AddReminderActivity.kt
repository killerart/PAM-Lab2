package com.example.lab2

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.lab2.models.Reminder
import java.sql.Time
import java.util.*
import kotlin.collections.ArrayList

class AddReminderActivity : AppCompatActivity() {
    private var selectedDay: Long = 0
    private lateinit var nameEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var timePicker: TimePicker
    private var oldReminder: Reminder? = null
    private var position: Int? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_reminder)

        selectedDay = intent.getLongExtra(KEY_DATE, 0)

        nameEditText = findViewById(R.id.editTextReminderName)
        descriptionEditText = findViewById(R.id.editTextDescription)
        timePicker = findViewById<TimePicker>(R.id.editTime).apply {
            setIs24HourView(true)
        }

        if (selectedDay == 0L) {
            val reminder = intent.getParcelableExtra<Reminder>(MainActivity.KEY_REMINDER)
            position = intent.getIntExtra(ReminderAdapter.KEY_POSITION, -1)
            if (position == -1)
                finish()
            if (reminder != null) {
                selectedDay = reminder.dayInMillis
                nameEditText.text.append(reminder.name)
                descriptionEditText.text.append(reminder.description)
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = reminder.dateTime
                }
                timePicker.hour = calendar[Calendar.HOUR_OF_DAY]
                timePicker.minute = calendar[Calendar.MINUTE]
            }
            oldReminder = reminder
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun save(view: View?) {
        val name = nameEditText.text.toString().also {
            if (it.isBlank()) {
                Toast.makeText(this, "Enter reminder name", Toast.LENGTH_SHORT).apply {
                    show()
                }
                return
            }
        }
        val description = descriptionEditText.text.toString()
        val reminder =
            Reminder(
                Calendar.getInstance().apply {
                    timeInMillis = selectedDay
                    set(Calendar.HOUR_OF_DAY, timePicker.hour)
                    set(Calendar.MINUTE, timePicker.minute)
                }.timeInMillis,
                selectedDay,
                name,
                description
            ).apply {
                val tempReminder = oldReminder
                if (tempReminder != null) id = tempReminder.id
            }
        Intent().apply {
            putExtra(KEY_DATE, selectedDay)
            putExtra(MainActivity.KEY_REMINDER, reminder)
            if (position != null)
                putExtra(ReminderAdapter.KEY_POSITION, position!!)
        }.also {
            setResult(RESULT_OK, it)
        }
        finish()
    }

    companion object {
        const val KEY_DATE = "KEY_DATE"
    }
}