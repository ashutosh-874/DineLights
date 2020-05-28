package com.ashu.myapplication.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ashu.myapplication.R

class ProfileFragment : Fragment() {

    lateinit var txtAccountname: TextView
    lateinit var txtEmail: TextView
    lateinit var txtMobile: TextView
    lateinit var txtAddress: TextView
    lateinit var sharedPreferences: SharedPreferences


    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        txtAccountname = view.findViewById(R.id.txtAccountName)
        txtEmail = view.findViewById(R.id.txtAccountEmail)
        txtMobile = view.findViewById(R.id.txtAccountMobile)
        txtAddress = view.findViewById(R.id.txtAccountAddress)

        sharedPreferences =context!!.getSharedPreferences("Mobile Preferences",Context.MODE_PRIVATE)
        txtMobile.text ="  ${sharedPreferences.getString("mobile","00000000")}"
        txtAccountname.text ="  ${sharedPreferences.getString("user_name","00000000")}"
        txtEmail.text ="  ${sharedPreferences.getString("email","00000000")}"
        txtAddress.text ="  ${sharedPreferences.getString("address","00000000")}"

        return view
    }

}
