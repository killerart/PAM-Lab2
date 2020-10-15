package com.example.lab2.models

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.*

@Serializable
data class Reminder(
    var dateTime: Long,
    var dayInMillis: Long,
    var name: String?,
    var description: String?
) : Parcelable {
    var id = UUID.randomUUID().toString()

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readLong(),
        parcel.readString(),
        parcel.readString()
    ) {
        id = parcel.readString().toString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(dateTime)
        parcel.writeLong(dayInMillis)
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeString(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (other is Reminder) {
            return id == other.id && dateTime == other.dateTime && name == other.name && description == other.description
        }
        return false
    }

    companion object CREATOR : Parcelable.Creator<Reminder> {
        override fun createFromParcel(parcel: Parcel): Reminder {
            return Reminder(parcel)
        }

        override fun newArray(size: Int): Array<Reminder?> {
            return arrayOfNulls(size)
        }
    }
}