package com.dragynslayr.chron.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dragynslayr.chron.R
import com.dragynslayr.chron.helper.enableNightMode

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableNightMode()
    }
}
