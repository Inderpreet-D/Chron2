package com.dragynslayr.chron.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dragynslayr.chron.R
import com.dragynslayr.chron.helper.getDateString
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.item_birthday.view.*
import java.io.Serializable

@IgnoreExtraProperties
data class Birthday(
    var name: String? = "",
    var phone: String? = "",
    var month: Int? = -1,
    var day: Int? = -1,
    var message: String? = "Happy Birthday, $name!",
    var id: String? = "",
    var lastSentYear: Int? = 0
) : Serializable {
    @Exclude
    fun upload(database: DatabaseReference) {
        val user = Firebase.auth.currentUser!!.uid
        if (id!!.isEmpty()) {
            val ref = database.child(user).push()
            id = ref.key
            ref.setValue(this)
        } else {
            database.child(user).child(id!!).setValue(this)
        }
    }

    @Exclude
    fun delete(database: DatabaseReference) {
        val user = Firebase.auth.currentUser!!.uid
        database.child(user).child(id!!).removeValue()
    }

    @Exclude
    fun sameAs(other: Birthday): Boolean {
        val nameSame = name == other.name
        val phoneSame = phone == other.phone
        val dateSame = month == other.month && day == other.day
        val messageSame = message == other.message
        return nameSame && phoneSame && dateSame && messageSame
    }

    @Exclude
    fun compare(other: Birthday): Int {
        return if (month == other.month) {
            if (day == other.day) {
                phone!!.compareTo(other.phone!!)
            } else {
                day!! - other.day!!
            }
        } else {
            month!! - other.month!!
        }
    }
}

class BirthdayListAdapter(
    private val birthdays: List<Birthday>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    lateinit var database: DatabaseReference
    lateinit var clickListener: (Birthday) -> Unit
    lateinit var longClickListener: (Birthday) -> Boolean

    override fun getItemCount(): Int {
        return birthdays.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val li = LayoutInflater.from(parent.context)
        val v = li.inflate(R.layout.item_birthday, parent, false)
        return BirthdayHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val bh = holder as BirthdayHolder
        val birthday = birthdays[position]
        bh.bind(birthday)
        bh.itemView.setOnClickListener { clickListener(birthday) }
        bh.itemView.setOnLongClickListener { longClickListener(birthday) }
    }
}

private class BirthdayHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(birthday: Birthday) {
        with(itemView) {
            birthday_name.text = birthday.name
            birthday_date.text = getDateString(birthday.month!!, birthday.day!!)
        }
    }
}

class BirthdayComparator : Comparator<Birthday> {
    override fun compare(o1: Birthday?, o2: Birthday?): Int {
        return o1!!.compare(o2!!)
    }
}