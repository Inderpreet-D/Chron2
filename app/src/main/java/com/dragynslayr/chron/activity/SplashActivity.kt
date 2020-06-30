package com.dragynslayr.chron.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dragynslayr.chron.R
import com.dragynslayr.chron.helper.enableNightMode
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableNightMode()

        if (Firebase.auth.currentUser == null) {
            showLogin()
        } else {
            startMain()
        }
    }

    private fun showLogin() {
        val providers = arrayListOf(AuthUI.IdpConfig.EmailBuilder().build())
        val loginIntent =
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers)
                .setLogo(R.drawable.ic_launcher_foreground).setTheme(R.style.Theme_MyApp)
                .build()

        startActivityForResult(loginIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
            startMain()
        }
    }

    private fun startMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    companion object {
        private const val RC_SIGN_IN = 1
    }
}
