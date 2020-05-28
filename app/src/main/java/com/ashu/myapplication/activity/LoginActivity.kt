package com.ashu.myapplication.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.ashu.myapplication.R
//import com.ashu.myapplication.database.AccountDatabase
import com.ashu.myapplication.database.ResDatabase
import com.ashu.myapplication.util.ConnectionManager
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    lateinit var etMobileNumber: EditText
    lateinit var etPassword: EditText
    lateinit var btnSignIn: Button
    lateinit var txtForgotPassword: TextView
    lateinit var txtRegister: TextView
    lateinit var sharedpreferences: SharedPreferences
    lateinit var sharedPreferences2: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etMobileNumber = findViewById(R.id.etMobileNumber)
        etPassword = findViewById(R.id.etPassword)
        btnSignIn = findViewById(R.id.btnSignIn)
        txtForgotPassword = findViewById(R.id.txtForgotPassword)
        txtRegister = findViewById(R.id.txtRegister)

        sharedpreferences = getSharedPreferences("Login Preferences", Context.MODE_PRIVATE)
        sharedPreferences2 = getSharedPreferences("Mobile Preferences", Context.MODE_PRIVATE)

        val isLoggedIn = sharedpreferences.getBoolean("isLoggedIn", false)
        val id = sharedPreferences2.getString("user_id", "21434234")
        val mobile = sharedPreferences2.getString("mobile", "21434234")
        val userName = sharedPreferences2.getString("user_name","Abcdefegh")
        val userEmail =sharedPreferences2.getString("email","00000000")
        val userAddress =sharedPreferences2.getString("address","00000000")
        if (isLoggedIn) {
            sharedPreferences2.edit().putString("user_id",id).apply()
            sharedPreferences2.edit().putString("mobile", mobile).apply()
            sharedPreferences2.edit().putString("user_name",userName).apply()
            sharedPreferences2.edit().putString("email",userEmail).apply()
            sharedPreferences2.edit().putString("address",userAddress).apply()
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        txtRegister.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegistrationActivity::class.java)
            startActivity(intent)
            finish()
        }
        txtForgotPassword.setOnClickListener {
            val intent = Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
            startActivity(intent)
            finish()
        }


        btnSignIn.setOnClickListener {

            DeleteAllData(this@LoginActivity, 1).execute().get()
            val mobile = etMobileNumber.text.toString()
            val password = etPassword.text.toString()

            if (mobile.length < 1 || password.length < 1) {
                Toast.makeText(
                    this@LoginActivity,
                    "Credentials Can't Be Empty",
                    Toast.LENGTH_SHORT
                ).show()
            }
            if(mobile.length !=10) {
                etMobileNumber.error = "Invalid MobileNumber"
            }
            if (password.length <6 ){
                etPassword.error = "Invalid Password"
            }
            else if(password.length>=6 && mobile.length == 10)
             {
                val queue = Volley.newRequestQueue(this@LoginActivity)
                val url = "http://13.235.250.119/v2/login/fetch_result"

                val jsonParams = JSONObject()
                jsonParams.put("mobile_number", mobile)
                jsonParams.put("password", password)

                if (ConnectionManager().checkConnectivity(this@LoginActivity)) {
                    val jsonRequest = object : JsonObjectRequest(
                        Request.Method.POST,
                        url,
                        jsonParams,
                        com.android.volley.Response.Listener {

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
                                        sharedPreferences2.edit().putString("user_id",id).apply()
                                        sharedPreferences2.edit().putString("user_name",userName).apply()
                                        sharedPreferences2.edit().putString("email",email).apply()
                                        sharedPreferences2.edit().putString("address",address).apply()
                                        sharedpreferences.edit().putBoolean("isLoggedIn", true).apply()
                                        sharedPreferences2.edit().putString("mobile", mobile).apply()
                                    }
                                    val intent =
                                        Intent(this@LoginActivity, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else if (success == false) {
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Invalid Mobile or Password",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } catch (e: java.lang.Exception) {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Some unexpected error Occured!!!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }


                        },
                        com.android.volley.Response.ErrorListener {
                            Toast.makeText(
                                this@LoginActivity,
                                "Unable to fetch Data from Server",
                                Toast.LENGTH_SHORT
                            )
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
                    val dialog = AlertDialog.Builder(this@LoginActivity)
                    dialog.setTitle("Error")
                    dialog.setMessage("Internet Connection not Found")
                    dialog.setPositiveButton("Open Settings") { text, Listener ->
                        val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                        startActivity(settingsIntent)
                        this@LoginActivity.finish()
                    }

                    dialog.setNegativeButton("Exit") { text, listener ->
                        ActivityCompat.finishAffinity(this@LoginActivity)
                    }
                    dialog.create()
                    dialog.show()
                }
            }
        }
    }

    class DeleteAllData(val context: Context, val mode: Int) :
        AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg params: Void?): Boolean {
            val db =
                Room.databaseBuilder(context, ResDatabase::class.java, "res-db").build()
            when (mode) {
                1 -> {
                    db.resDao().deleteAllRestaurants()
                    db.close()
                    return true
                }
            }
            return false
        }
    }
}
