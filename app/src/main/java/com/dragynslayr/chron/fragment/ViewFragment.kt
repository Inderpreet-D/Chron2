package com.dragynslayr.chron.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dragynslayr.chron.R
import com.dragynslayr.chron.data.Birthday
import com.dragynslayr.chron.data.BirthdayComparator
import com.dragynslayr.chron.data.BirthdayListAdapter
import com.dragynslayr.chron.helper.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.dialog_edit.view.*
import kotlinx.android.synthetic.main.fragment_view.view.*

class ViewFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var birthdays: ArrayList<Birthday>
    private lateinit var adapter: BirthdayListAdapter
    private val comparator = BirthdayComparator()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_view, container, false)

        database = Firebase.database.reference

        setupUI(v)

        return v
    }

    private fun setupUI(v: View) {
        birthdays = arrayListOf()
        adapter = BirthdayListAdapter(birthdays)
        adapter.database = database
        adapter.clickListener = { birthday -> clickListener(birthday) }
        adapter.longClickListener = { birthday -> longClickListener(birthday) }

        with(v) {
            view_list.adapter = adapter
            view_list.layoutManager = LinearLayoutManager(requireContext())

            setupUpdates(view_list)
        }
    }

    @SuppressLint("InflateParams")
    private fun clickListener(birthday: Birthday) {
        val v = layoutInflater.inflate(R.layout.dialog_edit, null)
        val dialog = AlertDialog.Builder(context).setTitle(R.string.edit_title)
            .setPositiveButton(getString(R.string.edit_text)) { _, _ ->
                with(v) {
                    val name = name_layout.getText()
                    val phone = phone_layout.getText()
                    val date = parseDate(date_layout.getText())
                    val day = date.day
                    val month = date.month
                    val message = message_layout.getText()
                    val id = birthday.id!!
                    val newBirthday = Birthday(name, phone, month, day, message, id)
                    if (!birthday.sameAs(newBirthday)) {
                        newBirthday.upload(database)
                        toastShort("Saved changes")
                    }
                }
            }.setNegativeButton(getString(R.string.cancel_text)) { _, _ -> }.setView(v).create()
        dialog.setOnShowListener {
            dialog.spaceButtons()
            with(v) {
                name_layout.setText(birthday.name!!)
                date_layout.setText(getDateString(birthday.month!!, birthday.day!!))
                date_layout.editText!!.setOnClickListener {
                    DatePickerFragment { month, day ->
                        date_layout.setText(getDateString(month, day))
                    }.show(requireActivity().supportFragmentManager, "datePicker")
                }
                phone_layout.setText(birthday.phone!!)
                message_layout.setText(birthday.message!!)
            }
        }
        dialog.show()
    }

    private fun longClickListener(birthday: Birthday): Boolean {
        val dialog =
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.remove_title, birthday.name!!))
                .setPositiveButton(R.string.delete_text) { _, _ ->
                    birthday.delete(database)
                    toastShort("Deleted ${birthday.name!!}")
                }.setNegativeButton(R.string.cancel_text) { _, _ -> }.create()
        dialog.setOnShowListener {
            dialog.spaceButtons()
        }
        dialog.show()
        return true
    }

    private fun setupUpdates(recycler: RecyclerView) {
        val user = Firebase.auth.currentUser!!.uid
        database.child(user).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                val size = birthdays.size
                birthdays.clear()
                adapter.notifyItemRangeRemoved(0, size)

                if (snapshot.exists()) {
                    val temp = arrayListOf<Birthday>()
                    snapshot.children.forEach {
                        temp.add(it.getValue<Birthday>()!!)
                    }

                    val lists = splitListByDate(temp.sortedWith(comparator))
                    birthdays.addAll(lists.first)
                    birthdays.addAll(lists.second)

                    adapter.notifyItemRangeInserted(0, birthdays.size)
                    recycler.scrollToPosition(0)
                }
            }
        })
    }

    private fun splitListByDate(list: List<Birthday>): Pair<List<Birthday>, List<Birthday>> {
        val currentDate = getCurrentDate()
        val currentDay = currentDate.day
        val currentMonth = currentDate.month

        var idx = 0
        for (i in 0..list.size) {
            val bd = list[i]
            if (bd.month!! >= currentMonth && bd.day!! >= currentDay) {
                idx = i
                break
            }
        }

        val first = list.subList(idx, list.size)
        val second = list.subList(0, idx)
        return Pair(first, second)
    }
}