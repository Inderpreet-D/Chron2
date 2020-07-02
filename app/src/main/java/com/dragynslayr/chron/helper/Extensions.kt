package com.dragynslayr.chron.helper

import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputLayout

fun String.log() {
    Log.d(APP_TAG, this)
}

fun TextInputLayout.getText(): String {
    return this.editText!!.text.toString()
}

fun TextInputLayout.setText(msg: String) {
    this.editText!!.setText(msg, TextView.BufferType.EDITABLE)
}

fun TextInputLayout.resetText() {
    this.setText("")
}

fun TextInputLayout.resetError() {
    this.error = ""
}

private fun makeToast(context: Context, text: String, duration: Int) {
    Toast.makeText(context, text, duration).show()
}

fun AppCompatActivity.toastShort(text: String) {
    makeToast(this, text, Toast.LENGTH_SHORT)
}

fun AppCompatActivity.toastLong(text: String) {
    makeToast(this, text, Toast.LENGTH_LONG)
}

fun Fragment.toastShort(text: String) {
    makeToast(requireContext(), text, Toast.LENGTH_SHORT)
}

fun Fragment.toastLong(text: String) {
    makeToast(requireContext(), text, Toast.LENGTH_LONG)
}

fun AlertDialog.spaceButtons() {
    val layout = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )
    layout.setMargins(7, 0, 7, 0)
    arrayOf(
        DialogInterface.BUTTON_NEGATIVE,
        DialogInterface.BUTTON_POSITIVE
    ).forEach {
        this.getButton(it).layoutParams = layout
    }
}