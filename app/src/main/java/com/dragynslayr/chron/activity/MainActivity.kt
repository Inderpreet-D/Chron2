package com.dragynslayr.chron.activity

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.dragynslayr.chron.R
import com.dragynslayr.chron.data.User
import com.dragynslayr.chron.fragment.AddFragment
import com.dragynslayr.chron.fragment.ViewFragment
import com.dragynslayr.chron.helper.enableNightMode
import com.dragynslayr.chron.helper.spaceButtons
import com.dragynslayr.chron.helper.toastLong
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableNightMode()

        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.drawer_layout)
        toggle = ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }

        nav_view.setNavigationItemSelectedListener(this)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                add<ViewFragment>(R.id.content_frame, null, intent.extras)
            }
        }

        user = intent.extras?.getSerializable(getString(R.string.user_object_key)) as User
        nav_view.menu.getItem(0).title = user.username!!

        checkSMSPermission()
        AlarmReceiver.startAlarm(applicationContext)
    }

    private fun checkSMSPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.SEND_SMS),
                SEND_SMS_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode != SEND_SMS_PERMISSION) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        } else if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder(this).setCancelable(false).setTitle("SMS Permission not granted")
                .setMessage("This app will not be able to automatically text people without this permission")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.ok, null).show()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        drawer.closeDrawer(GravityCompat.START)
        when (item.itemId) {
            R.id.nav_item_view -> moveTo<ViewFragment>()
            R.id.nav_item_add -> moveTo<AddFragment>()
            R.id.nav_item_logout -> showLogoutDialog()
        }
        return true
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private inline fun <reified F : Fragment> moveTo() {
        supportFragmentManager.commit {
            replace<F>(R.id.content_frame, null, intent.extras)
            addToBackStack(null)
        }
    }

    private fun showLogoutDialog() {
        val dialog =
            AlertDialog.Builder(this).setMessage(getString(R.string.logout_prompt))
                .setNegativeButton(getString(R.string.dialog_cancel)) { _: DialogInterface, _: Int -> }
                .setPositiveButton(getString(R.string.nav_logout)) { _: DialogInterface, _: Int -> logout() }
                .create()
        dialog.setOnShowListener {
            dialog.spaceButtons()
        }
        dialog.show()
    }

    private fun logout() {
        clearToken()
        val intent = Intent(applicationContext, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        toastLong("Successfully logged out")
        startActivity(intent)
    }

    private fun clearToken() {
        val sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            remove(getString(R.string.user_token_key))
            commit()
        }
    }

    companion object {
        private const val SEND_SMS_PERMISSION = 1
    }
}
