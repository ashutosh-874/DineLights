package com.ashu.myapplication.activity

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.ashu.myapplication.R
import com.ashu.myapplication.adapter.CartRecyclerAdapter
import com.ashu.myapplication.database.*
import com.ashu.myapplication.util.ConnectionManager
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject

class CartActivity : AppCompatActivity() {

    lateinit var recyclerItemList: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar
    lateinit var toolBar: Toolbar
    private var itemCostList = ArrayList<String>()
    private var itemIdList = ArrayList<String>()
    lateinit var recyclerAdapter: CartRecyclerAdapter
    lateinit var btnPlaceOrder:Button
    lateinit var txtOrderingFrom: TextView

    var itemList = listOf<CartEntity>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        progressBar = findViewById(R.id.progressBar)
        progressLayout = findViewById(R.id.progressLayout)
        recyclerItemList = findViewById(R.id.RecyclerItemList)
        layoutManager = LinearLayoutManager(applicationContext)
        recyclerAdapter = CartRecyclerAdapter(applicationContext, itemList)
        txtOrderingFrom = findViewById(R.id.txtOrder)
        recyclerItemList.adapter = recyclerAdapter
        recyclerItemList.layoutManager = layoutManager
        toolBar = findViewById(R.id.toolBar)
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder)
        setUpToolBar()

        itemList = RetrieveItems(applicationContext).execute().get()

        progressLayout.visibility = View.GONE
        recyclerAdapter = CartRecyclerAdapter(applicationContext, itemList)
        recyclerItemList.adapter =recyclerAdapter
        recyclerItemList.layoutManager = layoutManager

        val res_id = intent.getStringExtra("res_id")
        val res_name = intent.getStringExtra("res_name")

        txtOrderingFrom.text = "Ordering from : ${res_name}"

        for (elements in itemList){
            itemCostList.addAll(listOf(Gson().fromJson(elements.itemCost, String::class.java)))
        }
                var sum = 0
                        for (i in 0 until itemCostList.size) {
                             sum += itemCostList[i].toInt()
                        }
        btnPlaceOrder.text = "Place Order(Total: Rs. $sum)"


        for (element in itemList){
            itemIdList.addAll(listOf(Gson().fromJson(element.item_id.toString(),String::class.java)))
        }
                val foodArray = JSONArray()
                        for (i in 0 until itemIdList.size){
                            val foodId = JSONObject()
                            foodId.put("food_item_id", itemIdList[i])
                            foodArray.put(i, foodId)
                        }
        btnPlaceOrder.setOnClickListener {
            sendServerRequest(res_id,sum,foodArray)
        }
    }
    private fun sendServerRequest(res_id:String,sum:Int,foodArray:JSONArray){
        val queue = Volley.newRequestQueue(this@CartActivity)
        val jsonParams = JSONObject()
        jsonParams.put("user_id",this@CartActivity.getSharedPreferences("Mobile Preferences", Context.MODE_PRIVATE).getString(
            "user_id",
            null
        ) as String)
        jsonParams.put("restaurant_id",res_id)
        jsonParams.put("total_cost",sum.toString())
        jsonParams.put("food", foodArray)

        val url = "http://13.235.250.119/v2/place_order/fetch_result/"
        if (ConnectionManager().checkConnectivity(this@CartActivity)) {
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
                            DBAsyncTask(this@CartActivity, 1).execute().get()
                            val dialog = Dialog(
                                this@CartActivity,
                                android.R.style.Theme_Black_NoTitleBar_Fullscreen
                            )
                            dialog.setContentView(R.layout.order_placed)
                            dialog.show()
                            dialog.setCancelable(false)
                            val btnOk = dialog.findViewById<Button>(R.id.btnOk)
                            btnOk.setOnClickListener {
                                dialog.dismiss()
                                startActivity(Intent(this@CartActivity,MainActivity::class.java))
                                ActivityCompat.finishAffinity(this@CartActivity)}
                        } else if (success == false) {
                            Toast.makeText(
                                this@CartActivity,
                                "Something went wrong",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } catch (e: java.lang.Exception) {
                        Toast.makeText(
                            this@CartActivity,
                            "Some unexpected error Occured!!!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }


                },
                com.android.volley.Response.ErrorListener {
                    Toast.makeText(
                        this@CartActivity,
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

            val dialog = AlertDialog.Builder(this@CartActivity)
            dialog.setTitle("Error")
            dialog.setMessage("Internet Connection not Found")
            dialog.setPositiveButton("Open Settings") { text, Listener ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                this@CartActivity.finish()
            }

            dialog.setNegativeButton("Exit") { text, listener ->
                ActivityCompat.finishAffinity(this@CartActivity)
            }
            dialog.create()
            dialog.show()
        }
    }

    fun setUpToolBar() {
        setSupportActionBar(toolBar)
        supportActionBar?.title = "My Cart"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class RetrieveItems(val context: Context) : AsyncTask<Void, Void, List<CartEntity>>() {

        override fun doInBackground(vararg p0: Void?): List<CartEntity> {
            val db = Room.databaseBuilder(context, CartDatabase::class.java, "cart-db").build()

            return db.cartDao().getAllItems()
        }

    }

    class DBAsyncTask(context: Context,val mode: Int) :
        AsyncTask<Void, Void, Boolean>() {

        val db = Room.databaseBuilder(context, CartDatabase::class.java,"cart-db").build()

        override fun doInBackground(vararg params: Void?): Boolean {
            when (mode) {
                1 -> {
                    db.cartDao().deleteAllItems()
                    db.close()
                    return true
                }
            }
            return false
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}
