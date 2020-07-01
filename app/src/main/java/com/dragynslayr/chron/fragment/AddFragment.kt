package com.dragynslayr.chron.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dragynslayr.chron.R
import com.dragynslayr.chron.helper.log
import kotlinx.android.synthetic.main.fragment_add.view.*

class AddFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_add, container, false)

        setupUI(v)

        return v
    }

    private fun setupUI(v: View) {
        with(v) {
            add_button.setOnClickListener { "Clicked add".log() }
            choose_button.setOnClickListener { "Clicked choose".log() }
            date_layout.editText!!.setOnClickListener { "Clicked on date".log() }
        }
    }
}
