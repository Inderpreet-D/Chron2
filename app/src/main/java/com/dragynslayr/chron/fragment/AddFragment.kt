package com.dragynslayr.chron.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dragynslayr.chron.R
import com.dragynslayr.chron.data.Birthday
import com.dragynslayr.chron.data.User
import com.dragynslayr.chron.helper.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_add.view.*

class AddFragment : Fragment() {

    private lateinit var v: View
    private lateinit var database: DatabaseReference
    private lateinit var user: User

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_add, container, false)

        user =
            activity?.intent?.extras?.getSerializable(getString(R.string.user_object_key)) as User

        database = Firebase.database.reference

        setupUI()

        return v
    }

    private fun setupUI() {
        with(v) {
            date_layout.editText!!.setOnClickListener { openDateDialog() }
            add_button.setOnClickListener { addPerson() }
            choose_button.setOnClickListener { openChooseDialog() }
        }
    }

    private fun openDateDialog() {
        with(requireView()) {
            DatePickerFragment { month, day ->
                date_layout.setText(getDateString(month, day))
            }.show(requireActivity().supportFragmentManager, "datePicker")
        }

    }

    private fun openChooseDialog() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
        startActivityForResult(intent, SELECT_CONTACT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SELECT_CONTACT && resultCode == RESULT_OK) {
            val contact = extractContactDetails(data, requireContext())

            with(requireView()) {
                name_layout.setText(contact.name)
                phone_layout.setText(contact.number)
            }
        }
    }

    private fun addPerson() {
        resetErrors()
        var hasErrors = false
        with(v) {
            val name = name_layout.getText()
            if (name.isEmpty()) {
                name_layout.error = REQUIRED
                hasErrors = true
            }

            val phone = phone_layout.getText()
            if (phone.isEmpty()) {
                phone_layout.error = REQUIRED
                hasErrors = true
            }

            val dateString = date_layout.getText()
            if (dateString.isEmpty()) {
                date_layout.error = REQUIRED
                hasErrors = true
            }

            if (!hasErrors) {
                val date = parseDate(dateString)
                Birthday(name, phone, date.month, date.day).upload(database, user)
                toastShort("Added $name")
                resetTexts()
            }
        }
    }

    private fun resetErrors() {
        with(v) {
            name_layout.resetError()
            phone_layout.resetError()
            date_layout.resetError()
        }
    }

    private fun resetTexts() {
        with(v) {
            name_layout.resetText()
            phone_layout.resetText()
            date_layout.resetText()
        }
    }

    companion object {
        private const val REQUIRED = "Required"
        private const val SELECT_CONTACT = 1
    }
}
