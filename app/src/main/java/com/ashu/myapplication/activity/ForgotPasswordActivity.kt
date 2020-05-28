package com.ashu.myapplication.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
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
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.ashu.myapplication.R
//import com.ashu.myapplication.database.AccountDatabase
//import com.ashu.myapplication.database.AccountEntity
import com.ashu.myapplication.fragments.ProfileFragment
import com.ashu.myapplication.util.ConnectionManager
import org.json.JSONObject

class ForgotPasswordActivity : AppCompatActivity() {

    lateinit var etmobile: EditText
    lateinit var etemail: EditText
    lateinit var btncontinue: Button
    lateinit var txtLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        etmobile = findViewById(R.id.etmobile)
        etemail = findViewById(R.id.etemail)
        btncontinue = findViewById(R.id.btnContinue)
        txtLogin = findViewById(R.id.txtLogin)

        txtLogin.setOnClickListener {
            val intent = Intent(this@ForgotPasswordActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }


        btncontinue.setOnClickListener {
            val mobile = etmobile.text.toString()
            val email = etemail.text.toString()

            if (mobile.length < 1 || email.length < 1) {
                Toast.makeText(
                    this@ForgotPasswordActivity,
                    "Credentials Can't Be Empty",
                    Toast.LENGTH_SHORT
                ).show()
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                etemail.error = "Invalid Email"
            }
            if (mobile.length!=10){
                etmobile.error = "Invalid Mobile"
            }
            else if (Patterns.EMAIL_ADDRESS.matcher(email).matches() && mobile.length==10){
                val queue = Volley.newRequestQueue(this@ForgotPasswordActivity)
                val url = "http://13.235.250.119/v2/forgot_password/fetch_result"

                val jsonParams = JSONObject()
                jsonParams.put("mobile_number", mobile)
                jsonParams.put("email",email)

                if (ConnectionManager().checkConnectivity(this@ForgotPasswordActivity)) {
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
                                    Toast.makeText(this@ForgotPasswordActivity,"OTP successfully sent to your registered Email Id",Toast.LENGTH_SHORT).show()
                                    val intent =
                                        Intent(this@ForgotPasswordActivity, ResetPasswordActivity::class.java)
                                    intent.putExtra("mobile",mobile)
                                    startActivity(intent)
                                    finish()
                                } else{
                                    val msg = data.getString("errorMessage")
                                    Toast.makeText(
                                        this@ForgotPasswordActivity,
                                        "${msg}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } catch (e: java.lang.Exception) {
                                Toast.makeText(
                                    this@ForgotPasswordActivity,
                                    "Some unexpected error Occured!!!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }


                        },
                        com.android.volley.Response.ErrorListener {
                            Toast.makeText(
                                this@ForgotPasswordActivity,
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

                    val dialog = AlertDialog.Builder(this@ForgotPasswordActivity)
                    dialog.setTitle("Error")
                    dialog.setMessage("Internet Connection not Found")
                    dialog.setPositiveButton("Open Settings") { text, Listener ->
                        val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                        startActivity(settingsIntent)
                        this@ForgotPasswordActivity.finish()
                    }

                    dialog.setNegativeButton("Exit") { text, listener ->
                        ActivityCompat.finishAffinity(this@ForgotPasswordActivity)
                    }
                    dialog.create()
                    dialog.show()
                }
            }

        }

    }

}
