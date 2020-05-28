package com.ashu.myapplication.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.ashu.myapplication.R
import com.ashu.myapplication.adapter.ItemListRecyclerAdapter
import com.ashu.myapplication.database.CartDatabase
import com.ashu.myapplication.model.Items
import com.ashu.myapplication.util.ConnectionManager
import org.json.JSONException
import kotlin.collections.HashMap

class ItemListActivity : AppCompatActivity() {

    lateinit var recyclerItemList: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    val itemList = arrayListOf<Items>()
    lateinit var recyclerAdapter: ItemListRecyclerAdapter
    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar
    lateinit var toolBar: Toolbar
    var orderlist= arrayListOf<Items>()
    lateinit var btnToCart: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_list)

        progressBar = findViewById(R.id.progressBar)
        progressLayout = findViewById(R.id.progressLayout)
        btnToCart = findViewById(R.id.btnToCart)

        progressLayout.visibility = View.VISIBLE
        btnToCart.visibility = View.GONE


        recyclerItemList = findViewById(R.id.RecyclerItemList)
        layoutManager = LinearLayoutManager(applicationContext)

       recyclerAdapter = ItemListRecyclerAdapter(applicationContext, itemList,object: ItemListRecyclerAdapter.OnItemClickListener{
           override fun onAddItemClick(foodItem: Items){
               orderlist.add(foodItem)
               if (orderlist.size > 0){
                   btnToCart.visibility=View.VISIBLE
               }
           }

           override fun onRemoveItemClick(foodItem: Items){
               orderlist.remove(foodItem)
               if (orderlist.isEmpty()){
                   btnToCart.visibility=View.GONE
               }
           }
       })

        recyclerItemList.adapter = recyclerAdapter
        recyclerItemList.layoutManager = layoutManager
        toolBar = findViewById(R.id.toolBar)
        setUpToolBar()


        val res_id = intent.getStringExtra("id")
        val res_name = intent.getStringExtra("name")
        println("Response is ${res_id},${res_name}")

        btnToCart.setOnClickListener {
            intent = Intent(this@ItemListActivity,CartActivity::class.java)
            intent.putExtra("res_id",res_id)
            intent.putExtra("res_name",res_name)
            startActivity(intent)
        }

        val queue = Volley.newRequestQueue(applicationContext)

        val url = "http://13.235.250.119/v2/restaurants/fetch_result/${res_id}"

        if (ConnectionManager().checkConnectivity(applicationContext)) {

            val jsonObjectRequest =
                object : JsonObjectRequest(Request.Method.GET, url, null, Response.Listener {

                    try {
                        progressLayout.visibility = View.GONE
                        println("Response is ${it}")
                        val data = it.getJSONObject("data")
                        val success = data.getBoolean("success")
                        if (success) {
                            val itemsArray = data.getJSONArray("data")
                            for (i in 0 until itemsArray.length()) {
                                val resObject = itemsArray.getJSONObject(i)

                                val id = resObject.getString("id")
                                val name = resObject.getString("name")
                                val cost = resObject.getString("cost_for_one")
                                val resId = resObject.getString("restaurant_id")

                                val items = Items(id, name, cost, resId)

                                itemList.add(items)
                                recyclerAdapter =
                                    ItemListRecyclerAdapter(applicationContext, itemList,object: ItemListRecyclerAdapter.OnItemClickListener{
                                        override fun onAddItemClick(foodItem: Items){
                                            orderlist.add(foodItem)
                                            if (orderlist.size > 0){
                                                btnToCart.visibility=View.VISIBLE
                                            }
                                        }

                                        override fun onRemoveItemClick(foodItem: Items){
                                            orderlist.remove(foodItem)
                                            if (orderlist.isEmpty()){
                                                btnToCart.visibility=View.GONE
                                            }
                                        }
                                    })

                                recyclerItemList.adapter = recyclerAdapter
                                recyclerItemList.layoutManager = layoutManager


                            }


                        }
                    }


                    catch (e: JSONException) {
                        Toast.makeText(
                           applicationContext,
                            "Some unexpected error Occured!!!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }


                }, Response.ErrorListener {
                    if (applicationContext != null) {
                        Toast.makeText(
                            applicationContext,
                            "Volley error Occured!!!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-type"] = "application/json"
                        headers["token"] = "04d3d466f316d9"
                        return headers
                    }
                }




            queue.add(jsonObjectRequest)
        }


        else {

            val dialog = AlertDialog.Builder(this@ItemListActivity)
            dialog.setTitle("Error")
            dialog.setMessage("Internet Connection not Found")
            dialog.setPositiveButton("Open Settings") { text, Listener ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                this@ItemListActivity.finish()
            }

            dialog.setNegativeButton("Exit") { text, listener ->
                ActivityCompat.finishAffinity(this@ItemListActivity)
            }
            dialog.create()
            dialog.show()
        }


    }

    fun setUpToolBar() {
        setSupportActionBar(toolBar)
        supportActionBar?.title = intent.getStringExtra("name")
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onBackPressed()
    {
        if(DBAsyncTask(this@ItemListActivity,4).execute().get()) {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("Sure to Leave!!")
                .setMessage("Cart will be discarded")

                .setPositiveButton("Leave") { _, _ ->
                    val value = DBAsyncTask(this@ItemListActivity, 1).execute().get()
                    if (value) {
                        super.onBackPressed()
                    }
                }

                .setNegativeButton("Cancel") { _, _ ->

                }
                .create()
                .show()
        }
        else{
            super.onBackPressed()
        }
    }


    override fun onSupportNavigateUp(): Boolean {
            onBackPressed()
        return true
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
                4 -> {
                    val cart: Int =db.cartDao().getRowCount()
                    db.close()
                    if (cart>0){
                        return true
                    }
                }
            }
            return false
        }
    }
}
