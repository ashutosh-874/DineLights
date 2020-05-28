package com.ashu.myapplication.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

import com.ashu.myapplication.R
import com.ashu.myapplication.adapter.HomePageRecyclerAdapter
import com.ashu.myapplication.database.CartDatabase
import com.ashu.myapplication.model.Restaurants
import com.ashu.myapplication.util.ConnectionManager
import org.json.JSONException
import java.util.*
import kotlin.collections.HashMap

class HomepageFragment : Fragment() {

    lateinit var recyclerHomepage: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter: HomePageRecyclerAdapter
    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar
    private var checkedItem: Int = -1

    val restaurantList = arrayListOf<Restaurants>()

    var ratingComparator = Comparator<Restaurants>{res1 , res2 ->
        if(res1.rating.compareTo(res2.rating,true)==0){
            res1.name.compareTo(res2.name,true)
        }
        else{
            res1.rating.compareTo(res2.rating,true)
        }
    }
    var costComparator = Comparator<Restaurants> { res1, res2 ->
        val costOne = res1.cost_for_one.toInt()
        val costTwo = res2.cost_for_one.toInt()
        if (costOne.compareTo(costTwo) == 0) {
            ratingComparator.compare(res1, res2)
        } else {
            costOne.compareTo(costTwo)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_homepage, container, false)

        setHasOptionsMenu(true)
        recyclerHomepage = view.findViewById(R.id.RecyclerHomePage)
        progressBar = view.findViewById(R.id.progressBar)
        progressLayout = view.findViewById(R.id.progressLayout)
        progressLayout.visibility = View.VISIBLE
        layoutManager = LinearLayoutManager(activity)

        recyclerAdapter = HomePageRecyclerAdapter(activity as Context, restaurantList)

        recyclerHomepage.adapter = recyclerAdapter
        recyclerHomepage.layoutManager = layoutManager
        DBAsyncTask(
            activity as Context,
            1
        ).execute().get()

        val queue = Volley.newRequestQueue(activity as Context)

        val url = "http://13.235.250.119/v2/restaurants/fetch_result/"



        if (ConnectionManager().checkConnectivity(activity as Context)) {

            val jsonObjectRequest =
                object : JsonObjectRequest(Request.Method.GET, url, null, Response.Listener {

                    try {
                        progressLayout.visibility = View.GONE
                        val data = it.getJSONObject("data")
                        val success = data.getBoolean("success")
                        if (success) {
                            val resArray = data.getJSONArray("data")
                            for (i in 0 until resArray.length()) {
                                val resObject = resArray.getJSONObject(i)

                                val id = resObject.getString("id")
                                val name = resObject.getString("name")
                                val rating = resObject.getString("rating")
                                val cost = resObject.getString("cost_for_one")
                                val image = resObject.getString("image_url")

                                val restaurant = Restaurants(id, name, rating, cost, image)



                                restaurantList.add(restaurant)
                                recyclerAdapter =
                                    HomePageRecyclerAdapter(activity as Context, restaurantList)

                                recyclerHomepage.adapter = recyclerAdapter
                                recyclerHomepage.layoutManager = layoutManager
                            }
                        }
                    }
                    catch (e: JSONException) {
                        Toast.makeText(
                            activity as Context,
                            "Some unexpected error Occured!!!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }


                }, Response.ErrorListener {
                    if (activity != null) {
                        Toast.makeText(
                            activity as Context,
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
        } else {

            val dialog = AlertDialog.Builder(activity as Context)
            dialog.setTitle("Error")
            dialog.setMessage("Internet Connection not Found")
            dialog.setPositiveButton("Open Settings") { text, Listener ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                activity?.finish()
            }

            dialog.setNegativeButton("Exit") { text, listener ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            dialog.create()
            dialog.show()
        }
        return view
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
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        activity?.menuInflater?.inflate(R.menu.dashboard_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_sort -> showDialog(context as Context)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showDialog(context: Context) {

        val builder: androidx.appcompat.app.AlertDialog.Builder? = androidx.appcompat.app.AlertDialog.Builder(context)
        builder?.setTitle("Sort By?")
        builder?.setSingleChoiceItems(R.array.filters, checkedItem) { _, isChecked ->
            checkedItem = isChecked
        }
        builder?.setPositiveButton("Ok") { _, _ ->

            when (checkedItem) {
                0 -> {
                    Collections.sort(restaurantList, costComparator)
                }
                1 -> {
                    Collections.sort(restaurantList, costComparator)
                    restaurantList.reverse()
                }
                2 -> {
                    Collections.sort(restaurantList, ratingComparator)
                    restaurantList.reverse()
                }
            }
            recyclerAdapter.notifyDataSetChanged()
        }
        builder?.setNegativeButton("Cancel") { _, _ ->

        }
        builder?.create()
        builder?.show()
    }

}