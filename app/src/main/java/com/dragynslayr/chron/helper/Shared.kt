package com.dragynslayr.chron.helper

import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import androidx.appcompat.app.AppCompatDelegate
import java.util.*

const val APP_TAG = "CHRON-APP"
const val CHANNEL_ID = "ChronNotify"

const val DB_USERS = "users"
const val DB_BIRTHDAYS = "birthdays"

private val MONTHS = arrayOf(
    "January",
    "February",
    "March",
    "April",
    "May",
    "June",
    "July",
    "August",
    "September",
    "October",
    "November",
    "December"
)

fun enableNightMode() {
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
}

fun getDateString(month: Int, day: Int): String {
    return "${MONTHS[month]} $day"
}

fun parseDate(date: String): DayMonth {
    val split = date.split(" ")
    val month = MONTHS.indexOf(split[0])
    val day = Integer.parseInt(split[1])
    return DayMonth(month, day)
}

fun getCurrentDate(): DayMonth {
    val c = Calendar.getInstance()
    c.time = Date()
    val month = c.get(Calendar.MONTH)
    val day = c.get(Calendar.DAY_OF_MONTH)
    return DayMonth(month, day)
}

data class DayMonth(val month: Int, val day: Int)

fun extractContactDetails(data: Intent?, context: Context): Contact {
    var name = ""
    var number = ""
    val projection = arrayOf(
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER
    )

    data!!.data?.let {
        with(context.contentResolver.query(it, projection, null, null, null)) {
            if (this != null && this.moveToFirst()) {
                val nameIdx = this.getColumnIndex(projection[0])
                val numberIdx = this.getColumnIndex(projection[1])

                name = this.getString(nameIdx).split(" ")[0]
                number = this.getString(numberIdx)
            }
        }
    }

    return Contact(name, number)
}

data class Contact(val name: String, val number: String)