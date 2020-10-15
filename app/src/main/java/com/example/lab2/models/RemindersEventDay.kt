package com.example.lab2.models

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi
import com.applandeo.materialcalendarview.EventDay
import kotlinx.serialization.Serializable
import java.util.*
import kotlin.collections.ArrayList

class RemindersEventDay(
    day: Calendar,
    imageResource: Int
) : EventDay(day, imageResource) {
    var reminders: ArrayList<Reminder> = ArrayList()
}