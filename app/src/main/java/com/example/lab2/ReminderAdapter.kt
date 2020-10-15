package com.example.lab2

import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import androidx.recyclerview.widget.SortedListAdapterCallback
import com.example.lab2.models.Reminder
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ReminderAdapter(private val mainActivity: MainActivity) :
    RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {
    private val reminderSortedList: SortedList<Reminder>

    init {
        reminderSortedList = SortedList(Reminder::class.java, object :
            SortedListAdapterCallback<Reminder>(this) {
            override fun compare(o1: Reminder?, o2: Reminder?): Int {
                if (o1 != null && o2 != null)
                    return o1.dateTime.compareTo(o2.dateTime)
                if (o1 == null)
                    return -1
                return 1
            }

            override fun areContentsTheSame(oldItem: Reminder?, newItem: Reminder?): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(item1: Reminder?, item2: Reminder?): Boolean {
                return item1?.id == item2?.id
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        LayoutInflater.from(parent.context).inflate(R.layout.reminder_row, parent, false).let {
            return ReminderViewHolder(it, mainActivity).apply {
                itemView.setOnClickListener(this)
            }
        }
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        reminderSortedList[position].also { reminder ->
            holder.setReminder(reminder)
        }
    }

    override fun getItemCount(): Int = reminderSortedList.size()

    fun setReminders(reminders: ArrayList<Reminder>) {
        reminderSortedList.clear()
        reminderSortedList.addAll(reminders)
    }

    fun addReminder(reminder: Reminder) {
        reminderSortedList.add(reminder)
    }

    fun addReminders(reminders: List<Reminder>) {
        reminderSortedList.addAll(reminders)
    }

    fun removeReminderAt(index: Int) {
        if (reminderSortedList.size() == 0) {
            return
        }
        val reminder = reminderSortedList[index]
        mainActivity.removeReminder(reminder)
        reminderSortedList.removeItemAt(index)
    }

    fun removeReminder(reminder: Reminder) {
        if (reminderSortedList.size() == 0) {
            return
        }
        reminderSortedList.remove(reminder)
        mainActivity.removeReminder(reminder)
    }

    fun clearReminders() {
        reminderSortedList.clear()
    }

    fun updateItemAt(position: Int) {
        reminderSortedList.recalculatePositionOfItemAt(position)
    }

    class ReminderViewHolder(view: View, private val mainActivity: MainActivity) :
        RecyclerView.ViewHolder(view), View.OnClickListener {
        private val timeTextView: TextView = view.findViewById(R.id.time)
        private val nameTextView: TextView = view.findViewById(R.id.name)
        private val descriptionTextView: TextView = view.findViewById(R.id.description)
        private lateinit var reminder: Reminder

        fun setReminder(reminder: Reminder) {
            this.reminder = reminder
            SimpleDateFormat("HH:mm").also {
                timeTextView.text = it.format(
                    Calendar.getInstance().apply { timeInMillis = reminder.dateTime }.time
                )
            }
            nameTextView.text = reminder.name
            descriptionTextView.text = reminder.description
            if (reminder.description.isNullOrBlank())
                descriptionTextView.visibility = View.GONE
            else
                descriptionTextView.visibility = View.VISIBLE
        }

        override fun onClick(v: View?) {
            Intent(mainActivity, AddReminderActivity::class.java).apply {
                putExtra(MainActivity.KEY_REMINDER, reminder)
                putExtra(KEY_POSITION, adapterPosition)
            }.also {
                mainActivity.startActivityForResult(it, MainActivity.REQUEST_EDIT_REMINDER)
            }
        }
    }

    companion object {
        const val KEY_POSITION = "KEY_POSITION"
    }
}