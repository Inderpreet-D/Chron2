package com.dragynslayr.chron.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dragynslayr.chron.R
import com.dragynslayr.chron.helper.log

class ViewFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_view, container, false)
        "View fragment".log()
        return v
    }

}
