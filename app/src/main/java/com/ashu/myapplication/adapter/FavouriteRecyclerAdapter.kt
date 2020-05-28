package com.ashu.myapplication.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.ashu.myapplication.R
import com.ashu.myapplication.activity.ItemListActivity
import com.ashu.myapplication.database.ResEntity
import com.squareup.picasso.Picasso

class FavouriteRecyclerAdapter(val context: Context, val resList : List<ResEntity>):
    RecyclerView.Adapter<FavouriteRecyclerAdapter.FavouriteViewHolder>() {


    class FavouriteViewHolder(view: View): RecyclerView.ViewHolder(view) {

        val txtResName: TextView = view.findViewById(R.id.txtFavResTitle)
        val txtResRating: TextView = view.findViewById(R.id.txtFavResRating)
        val txtRescost: TextView = view.findViewById(R.id.txtFavResCost)
        val imgRes: ImageView = view.findViewById(R.id.imgFavResImage)
        val btnHeart: ImageButton = view.findViewById(R.id.btnHeart)
        val llFavContent:LinearLayout = view.findViewById(R.id.llFavContent)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_favourite_single_row,parent,false)

        return FavouriteViewHolder(view)
    }

    override fun getItemCount(): Int {
        return resList.size
    }

    override fun onBindViewHolder(holder: FavouriteViewHolder, position: Int) {
        val restaurant = resList[position]

        holder.txtResName.text = restaurant.resName
        holder.txtResRating.text = restaurant.resRating
        holder.txtRescost.text = "Rs. ${restaurant.resCost}"
        Picasso.get().load(restaurant.resImage).error(R.drawable.dine_lights).into(holder.imgRes)

        holder.llFavContent.setOnClickListener {
            val intent = Intent(context, ItemListActivity::class.java)
            intent.putExtra("name",restaurant.resName)
            intent.putExtra("id",restaurant.res_id.toString())
            context.startActivity(intent)
        }


        val res =ResEntity(restaurant.res_id,restaurant.resName,restaurant.resRating,restaurant.resCost,restaurant.resImage)

        if(!HomePageRecyclerAdapter.DBAsyncTask(context, res, 1).execute().get()){
            holder.btnHeart.setBackgroundResource(R.drawable.ic_heart)

        }
        else if (HomePageRecyclerAdapter.DBAsyncTask(context, res, 1).execute().get()){
            holder.btnHeart.setBackgroundResource(R.drawable.ic_heart1)
        }

        holder.btnHeart.setOnClickListener {
            if(!HomePageRecyclerAdapter.DBAsyncTask(context, res, 1).execute().get()){
                val async = HomePageRecyclerAdapter.DBAsyncTask(context, res, 2).execute()
                val result = async.get()
                if(result){
                    Toast.makeText(context,"Added to Favourites",Toast.LENGTH_SHORT).show()
                    holder.btnHeart.setBackgroundResource(R.drawable.ic_heart1)
                    notifyDataSetChanged()
                }

            }
            else{
                val async = HomePageRecyclerAdapter.DBAsyncTask(context, res, 3).execute()
                val result = async.get()
                if(result) {
                    Toast.makeText(context, "Removed from Favourites", Toast.LENGTH_SHORT).show()
                    holder.btnHeart.setBackgroundResource(R.drawable.ic_heart)
                }
            }
        }
    }


}