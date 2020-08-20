package com.dragynslayr.chron.activity

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.telephony.SmsManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.postDelayed
import com.dragynslayr.chron.R
import com.dragynslayr.chron.data.Birthday
import com.dragynslayr.chron.helper.*
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*

class AlarmReceiver : BroadcastReceiver() {

    private lateinit var calendar: Calendar
    private lateinit var context: Context
    private lateinit var database: DatabaseReference
    private lateinit var user: String


    override fun onReceive(c: Context, i: Intent) {
        try {
            calendar = Calendar.getInstance()
            calendar.time = Date()

            context = c

            FirebaseApp.initializeApp(context)
            database = Firebase.database.reference
            getUser()

            database.child(DB_USERS).child(user).child("WeirdVal").setValue("Weird")

            getBirthdays()
        } catch (e: Exception) {
            e.toString().log()
        } finally {
            startAlarm(c)
        }
    }

    private fun getUser() {
        val sharedPreferences =
            context.getSharedPreferences(
                context.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            )
        val userToken =
            sharedPreferences.getString(context.getString(R.string.user_token_key), null)
        if (userToken != null) {
            val split = userToken.split(",")
            user = split[0]
        }
    }

    private fun getBirthdays() {
        database.child(DB_BIRTHDAYS).child(user)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val birthdays = arrayListOf<Birthday>()
                        snapshot.children.forEach {
                            birthdays.add(it.getValue<Birthday>()!!)
                        }
                        checkBirthdays(birthdays)
                    }
                }
            })
    }

    private fun checkBirthdays(allBirthdays: ArrayList<Birthday>) {
        val today = getCurrentDate()

        val birthdays = arrayListOf<Birthday>()
        var birthdayMsg = StringBuilder()

        allBirthdays.forEach {
            if (it.month == today.month && it.day == today.day) {
                birthdays.add(it)
                birthdayMsg.append(it.name).append("\n")
            }
        }

        birthdayMsg = if (birthdayMsg.isEmpty()) {
            StringBuilder("None")
        } else {
            StringBuilder(birthdayMsg.toString().trim())
        }

        val todayString = getDateString(today.month, today.day)
        makeNotification("Birthdays on $todayString", birthdayMsg.toString())
        sendMessages(birthdays)
    }

    private fun makeNotification(title: String, msg: String) {
        val builder =
            NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(R.drawable.ic_chron_notif)
                .setContentTitle(title).setContentText(msg)
                .setStyle(NotificationCompat.BigTextStyle().bigText(msg))
        builder.priority = NotificationCompat.PRIORITY_DEFAULT

        val manager = NotificationManagerCompat.from(context)
        manager.notify(NOTIFICATION_ID, builder.build())
        NOTIFICATION_ID++
    }

    private fun sendMessages(birthdays: ArrayList<Birthday>) {
        val sms = SmsManager.getDefault()
        val currentYear = calendar.get(Calendar.YEAR)
        birthdays.forEachIndexed { i, birthday ->
            if (birthday.lastSentYear!! < currentYear) {
                Handler().postDelayed(i * 500L) {
                    sms.sendTextMessage(birthday.phone!!, null, birthday.message!!, null, null)
                    birthday.lastSentYear = currentYear
                    birthday.upload(database, user)
                }
            }
        }
    }

    companion object {
        private var NOTIFICATION_ID = 0
        private const val REQUEST_TIMER = 1

        fun startAlarm(context: Context) {
            val intent = Intent(context, this::class.java)
            val pendingIntent =
                PendingIntent.getBroadcast(context, REQUEST_TIMER, intent, FLAG_CANCEL_CURRENT)
            val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val alarmTime = LocalTime.of(0, 1)
            var now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
            val nowTime = now.toLocalTime()

            if (nowTime == alarmTime || nowTime.isAfter(alarmTime)) {
                now = now.plusDays(1)
            }
            now = now.withHour(alarmTime.hour).withMinute(alarmTime.minute)

            val utc = now.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime()
            val startMillis = utc.atZone(ZoneOffset.UTC)!!.toInstant()!!.toEpochMilli()

            "Alarm will trigger in ${(startMillis - System.currentTimeMillis()) / 1000}s".log()

            val windowMillis = 15L * 60L * 1000L
            alarm.setWindow(AlarmManager.RTC_WAKEUP, startMillis, windowMillis, pendingIntent)
        }
    }
}