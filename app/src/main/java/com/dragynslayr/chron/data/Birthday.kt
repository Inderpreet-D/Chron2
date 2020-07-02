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
    var id: String? = ""
) : Serializable {
    @Exclude
    fun upload(database: DatabaseReference) {
        val user = Firebase.auth.currentUser!!.uid
        val ref = database.child(user).push()
        id = ref.key
        ref.setValue(this)
    }

    @Exclude
    fun formatMessage(newMessage: String) {
        message = newMessage.replace("NAME", name!!)
    }

    @Exclude
    fun getMessageFormat(): String {
        return message!!.replace(name!!, "NAME")
    }
}

class BirthdayListAdapter(
    private val birthdays: List<Birthday>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    lateinit var clickListener: (Int) -> Unit
    lateinit var longClickListener: (Int) -> Boolean

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
        bh.bind(birthdays[position])
        bh.itemView.setOnClickListener { clickListener(position) }
        bh.itemView.setOnLongClickListener { longClickListener(position) }
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