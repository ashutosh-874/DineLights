package com.ashu.myapplication.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.room.Room.databaseBuilder
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.ashu.myapplication.R
//import com.ashu.myapplication.database.AccountDatabase
//import com.ashu.myapplication.database.AccountEntity
import com.ashu.myapplication.util.ConnectionManager
import kotlinx.android.synthetic.main.activity_registration.*
import org.json.JSONObject
import java.lang.Exception

class RegistrationActivity : AppCompatActivity() {

    lateinit var etname: EditText
    lateinit var etemail: EditText
    lateinit var btnregister: Button
    lateinit var etmobile: EditText
    lateinit var etpassword: EditText
    lateinit var etconfirmpassword: EditText
    lateinit var etaddress: EditText
    lateinit var tvGoToLogin: TextView
    lateinit var sharedpreferences: SharedPreferences
    lateinit var sharedPreferences2: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        title = "Registration"
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        etname = findViewById(R.id.etname)
        etemail = findViewById(R.id.etemail)
        btnregister = findViewById(R.id.btnRegister)
        etmobile = findViewById(R.id.etmobile)
        etaddress = findViewById(R.id.etaddress)
        etpassword = findViewById(R.id.etpassword)
        etconfirmpassword = findViewById(R.id.etconfirmpassword)
        tvGoToLogin = findViewById(R.id.txtLogin)
        sharedpreferences = getSharedPreferences("Login Preferences", Context.MODE_PRIVATE)
        sharedPreferences2 = getSharedPreferences("Mobile Preferences", Context.MODE_PRIVATE)

        txtLogin.setOnClickListener {
            val intent = Intent(this@RegistrationActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnRegister.setOnClickListener {
            val Name = etname.text.toString()
            val Email = etemail.text.toString()
            val Mobile = etmobile.text.toString()
            val Address = etaddress.text.toString()
            val Password = etpassword.text.toString()
            val ConfirmPassword = etconfirmpassword.text.toString()

            if (Name.length < 1 || Email.length < 1 || Mobile.length < 1 || Password.length < 1 || ConfirmPassword.length < 1 || Address.length < 1) {
                Toast.makeText(
                    this@RegistrationActivity,
                    "Credentials Can't Be Empty",
                    Toast.LENGTH_SHORT
                ).show()
            }

            if (Name.length <= 3){
                etname.error = "Invalid Name"
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(Email).matches()){
                etemail.error = "Invalid Email"
            }
            if (Mobile.length!=10){
                etmobile.error = "Invalid Mobile"
            }
            if (Password.length<=5){
                etpassword.error = "Password must be of atleast 6 characters "
            }
            if (Password != ConfirmPassword){
                etconfirmpassword.error = "Password Mismatch"
            }
            else if (Name.length>3 && Patterns.EMAIL_ADDRESS.matcher(Email).matches() && Mobile.length==10 && Password.length>5 && Password==ConfirmPassword && Address.length>1){

                    val queue = Volley.newRequestQueue(this@RegistrationActivity)
                    val url = "http://13.235.250.119/v2/register/fetch_result"

                    val jsonParams = JSONObject()
                    jsonParams.put("name",Name)
                    jsonParams.put("mobile_number",Mobile)
                    jsonParams.put("password",Password)
                    jsonParams.put("address",Address)
                    jsonParams.put("email",Email)


                    if (ConnectionManager().checkConnectivity(this@RegistrationActivity)) {
                       val jsonRequest =  object : JsonObjectRequest(Request.Method.POST, url, jsonParams, com.android.volley.Response.Listener {

                            try {
                                println("Response is ${it}")
                                val data = it.getJSONObject("data")
                                val success = data.getBoolean("success")
                                if (success) {
                                    val infoArray = data.getJSONObject("data")
                                    for (i in 0 until infoArray.length()) {
                                        val id = infoArray.getString("user_id")
                                        val userName = infoArray.getString("name")
                                        val email = infoArray.getString("email")
                                        val address = infoArray.getString("address")
                                        println("Response is ${id}")
                                        sharedPreferences2.edit().putString("user_id",id).apply()
                                        sharedpreferences.edit().putBoolean("isLoggedIn",true).apply()
                                        sharedPreferences2.edit().putString("mobile",Mobile).apply()
                                        sharedPreferences2.edit().putString("user_name",userName).apply()
                                        sharedPreferences2.edit().putString("email",email).apply()
                                        sharedPreferences2.edit().putString("address",address).apply()
                                        LoginActivity.DeleteAllData(this@RegistrationActivity, 1).execute().get()

                                    }
                                    Toast.makeText(
                                            this@RegistrationActivity,
                                            "Thanx For Registering",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        val intent = Intent(this@RegistrationActivity, MainActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                }
                                else{
                                    val errorMessage = data.getString("errorMessage")
                                    Toast.makeText(
                                        this@RegistrationActivity,
                                        "${errorMessage}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    this@RegistrationActivity,
                                    "Some unexpected error Occured!!!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }


                        }, com.android.volley.Response.ErrorListener {
                            Toast.makeText(this@RegistrationActivity, "Unable to fetch Data from Internet", Toast.LENGTH_SHORT)
                                .show()
                        }) {
                            override fun getHeaders(): MutableMap<String, String> {
                                val headers = HashMap<String, String>()
                                headers["Content_type"] = "application/json"
                                headers["token"] = "04d3d466f316d9"
                                return headers
                            }
                        }

                        queue.add(jsonRequest)
                    }
                    else {
                        val dialog = AlertDialog.Builder(this@RegistrationActivity)
                        dialog.setTitle("Error")
                        dialog.setMessage("Internet Connection not Found")
                        dialog.setPositiveButton("Open Settings") { text, Listener ->
                            val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                            startActivity(settingsIntent)
                            this@RegistrationActivity.finish()
                        }

                        dialog.setNegativeButton("Exit") { text, listener ->
                            ActivityCompat.finishAffinity(this@RegistrationActivity)
                        }
                        dialog.create()
                        dialog.show()
                    }
        }


        }
    }
}