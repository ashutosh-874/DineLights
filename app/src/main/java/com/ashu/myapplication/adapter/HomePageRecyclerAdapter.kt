package com.ashu.myapplication.adapter

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.ashu.myapplication.R
import com.ashu.myapplication.activity.ItemListActivity
import com.ashu.myapplication.database.ResDatabase
import com.ashu.myapplication.database.ResEntity
import com.ashu.myapplication.model.Restaurants
import com.squareup.picasso.Picasso

class HomePageRecyclerAdapter(val context: Context,val itemList: ArrayList<Restaurants>): RecyclerView.Adapter<HomePageRecyclerAdapter.HomePageViewHolder>() {


    override fun onBindViewHolder(holder: HomePageViewHolder, position: Int) {

        val restaurants = itemList[position]
        holder.res_id =restaurants.id.toInt()
        holder.txtResName.text = restaurants.name
        holder.txtResRating.text = restaurants.rating
        holder.txtcost.text = "  Rs. ${restaurants.cost_for_one}/Person"
        Picasso.get().load(restaurants.image_url).error(R.drawable.dine_lights).into(holder.imgRes)

        holder.llcontent.setOnClickListener {
            val intent = Intent(context,ItemListActivity::class.java)
            intent.putExtra("name",restaurants.name)
            intent.putExtra("id",restaurants.id)
            context.startActivity(intent)
        }

        val res =ResEntity(restaurants.id.toInt(),restaurants.name,restaurants.rating,restaurants.cost_for_one,restaurants.image_url)

        if(!DBAsyncTask(context,res,1).execute().get()){
                holder.btnHeart.setBackgroundResource(R.drawable.ic_heart)

        }
        else if (DBAsyncTask(context,res,1).execute().get()){
            holder.btnHeart.setBackgroundResource(R.drawable.ic_heart1)
        }
        holder.btnHeart.setOnClickListener {
            if(!DBAsyncTask(context,res,1).execute().get()){
                val async = DBAsyncTask(context,res,2).execute()
                val result = async.get()
                if(result){
                    Toast.makeText(context,"Added to Favourites",Toast.LENGTH_SHORT).show()
                    holder.btnHeart.setBackgroundResource(R.drawable.ic_heart1)
                }
            }
            else{
                val async = DBAsyncTask(context,res,3).execute()
                val result = async.get()
                if(result){
                    Toast.makeText(context,"Removed from Favourites",Toast.LENGTH_SHORT).show()
                    holder.btnHeart.setBackgroundResource(R.drawable.ic_heart)
                }
            }
        }

    }

    class HomePageViewHolder(view: View): RecyclerView.ViewHolder(view){
        var res_id:Int = 100
        val txtResName: TextView = view.findViewById(R.id.txtResName)
        val txtResRating: TextView = view.findViewById(R.id.txtResRating)
        val txtcost: TextView = view.findViewById(R.id.txtCost)
        val imgRes: ImageView = view.findViewById(R.id.imgResImage)
        val btnHeart:ImageButton = view.findViewById(R.id.btnheart)
        val llcontent:LinearLayout  = view.findViewById(R.id.llcontent)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomePageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_homepage_singlerow,parent,false)
        return HomePageViewHolder(view)
    }




    override fun getItemCount(): Int {
        return itemList.size
    }


    class DBAsyncTask(context: Context, val resEntity: ResEntity, val mode: Int) :
        AsyncTask<Void, Void, Boolean>() {

        val db = Room.databaseBuilder(context, ResDatabase::class.java,"res-db").build()

        override fun doInBackground(vararg params: Void?): Boolean{
            when(mode){
                1 ->{
                    val account: ResEntity? =db.resDao().getRestaurantsById(resEntity.res_id.toString() )
                    db.close()
                    return account!=null
                }
                2 ->{
                    db.resDao().insertRestaurant(resEntity)
                    db.close()
                    return true
                }
                3 ->{
                    db.resDao().deleteRestaurant(resEntity)
                    db.close()
                    return true
                }
            }
            return false
        }


    }









}