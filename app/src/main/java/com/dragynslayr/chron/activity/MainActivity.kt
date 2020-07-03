package com.dragynslayr.chron.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.dragynslayr.chron.R
import com.dragynslayr.chron.fragment.AddFragment
import com.dragynslayr.chron.fragment.ViewFragment
import com.dragynslayr.chron.helper.enableNightMode
import com.dragynslayr.chron.helper.log
import com.dragynslayr.chron.helper.spaceButtons
import com.dragynslayr.chron.helper.toastLong
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var database: DatabaseReference
    private lateinit var userId: String

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

        database = Firebase.database.reference

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                add<ViewFragment>(R.id.content_frame, null, intent.extras)
            }
        }


        userId = Firebase.auth.currentUser!!.uid
        startListener()
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
        Firebase.auth.signOut()
        toastLong("Successfully logged out")
        startSplash()
    }

    private fun startSplash() {
        val intent = Intent(this, SplashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun startListener() {
        database.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.children.forEach {
                        "Person $it".log()
                    }
                } else {
                    "No data".log()
                }
            }
        })
    }
}
