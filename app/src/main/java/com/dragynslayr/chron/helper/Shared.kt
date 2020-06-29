package com.dragynslayr.chron.helper

import androidx.appcompat.app.AppCompatDelegate

const val APP_TAG = "CHRON-APP"
const val CHANNEL_ID = "ChronNotify"

fun enableNightMode() {
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
}