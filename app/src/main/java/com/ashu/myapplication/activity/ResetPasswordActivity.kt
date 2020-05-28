package com.ashu.myapplication.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.ashu.myapplication.R
import com.ashu.myapplication.util.ConnectionManager
import kotlinx.android.synthetic.main.activity_reset_password.*
import org.json.JSONObject

class ResetPasswordActivity : AppCompatActivity() {

    lateinit var etOTP: EditText
    lateinit var etPassword: EditText
    lateinit var etConfirmPassword: EditText
    lateinit var btnSubmit:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        etOTP = findViewById(R.id.etOtp)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etconfirmpassword)
        btnSubmit = findViewById(R.id.btnSubmit)

        val mobile = intent.getStringExtra("mobile")

        btnSubmit.setOnClickListener {
            val otp = etOTP.text.toString()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (password.length < 1 || confirmPassword.length < 1 || otp.length<1) {
                Toast.makeText(
                    this@ResetPasswordActivity,
                    "Credentials Can't Be Empty",
                    Toast.LENGTH_SHORT
                ).show()
            }
            if (otp.length != 4){
                etOTP.error = "Invalid OTP"
            }
            if (password.length<=5){
                etPassword.error = "Password must be of atleast 6 characters "
            }
            if (password != confirmPassword){
                etConfirmPassword.error = "Password Mismatch"
            }
            else if (otp.length ==4 && password.length>5 && password==confirmPassword){
                val queue = Volley.newRequestQueue(this@ResetPasswordActivity)
                val url = "http://13.235.250.119/v2/reset_password/fetch_result"

                val jsonParams = JSONObject()
                jsonParams.put("mobile_number", mobile)
                jsonParams.put("password", password)
                jsonParams.put("otp",otp)

                if (ConnectionManager().checkConnectivity(this@ResetPasswordActivity)) {
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
                                    val successmessage = data.getString("successMessage")
                                    Toast.makeText(
                                        this@ResetPasswordActivity,
                                        "${successmessage}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    val intent =
                                        Intent(this@ResetPasswordActivity, LoginActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else{
                                    val msg = data.getString("errorMessage")
                                   Toast.makeText(
                                        this@ResetPasswordActivity,
                                         "${msg}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } catch (e: java.lang.Exception) {
                                Toast.makeText(
                                    this@ResetPasswordActivity,
                                    "Some unexpected error Occured!!!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }


                        },
                        com.android.volley.Response.ErrorListener {
                            Toast.makeText(
                                this@ResetPasswordActivity,
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

                    val dialog = AlertDialog.Builder(this@ResetPasswordActivity)
                    dialog.setTitle("Error")
                    dialog.setMessage("Internet Connection not Found")
                    dialog.setPositiveButton("Open Settings") { text, Listener ->
                        val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                        startActivity(settingsIntent)
                        this@ResetPasswordActivity.finish()
                    }

                    dialog.setNegativeButton("Exit") { text, listener ->
                        ActivityCompat.finishAffinity(this@ResetPasswordActivity)
                    }
                    dialog.create()
                    dialog.show()
                }

            }

        }

    }
}
