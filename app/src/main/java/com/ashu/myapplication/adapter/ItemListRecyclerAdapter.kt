package com.ashu.myapplication.adapter

import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.ashu.myapplication.R
import com.ashu.myapplication.database.CartDatabase
import com.ashu.myapplication.database.CartEntity
import com.ashu.myapplication.database.ResDatabase
import com.ashu.myapplication.database.ResEntity
import com.ashu.myapplication.model.FoodItem
import com.ashu.myapplication.model.Items
import com.ashu.myapplication.model.MenuData

class ItemListRecyclerAdapter(val context: Context, val itemList: ArrayList<Items>,val listener:OnItemClickListener
) : RecyclerView.Adapter<ItemListRecyclerAdapter.ItemListViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_list_singlerow,parent,false)
        return ItemListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return  itemList.size
    }


    interface OnItemClickListener{
        fun onAddItemClick(foodItem: Items)
        fun onRemoveItemClick(foodItem: Items)
    }

    override fun onBindViewHolder(holder: ItemListViewHolder, position: Int) {
        val items = itemList[position]
        holder.item_id.text  =(position+1).toString()
        holder.txtItemName.text = items.name
        holder.txtItemcost.text = "Rs. ${items.cost_for_one}"

        val cart  = CartEntity(items.id.toInt(),items.name,items.cost_for_one)

        if(!ItemListRecyclerAdapter.DBAsyncTask(context, cart, 1).execute().get()){
            holder.btnAddRemove.setBackgroundResource(R.drawable.button_shape2)
        }
        else if (ItemListRecyclerAdapter.DBAsyncTask(context, cart, 1).execute().get()){
            holder.btnAddRemove.setBackgroundResource(R.drawable.button_shape)
        }

        holder.btnAddRemove.setOnClickListener {
            if(!ItemListRecyclerAdapter.DBAsyncTask(context, cart, 1).execute().get()){
                val async = ItemListRecyclerAdapter.DBAsyncTask(context, cart, 2).execute()
                val result = async.get()
                if(result){
                    holder.btnAddRemove.setBackgroundResource(R.drawable.button_shape)
                    holder.btnAddRemove.text = "Remove"
                    listener.onAddItemClick(items)
                }
            }
            else{
                val async = ItemListRecyclerAdapter.DBAsyncTask(context, cart, 3).execute()
                val result = async.get()
                if(result){
                    holder.btnAddRemove.setBackgroundResource(R.drawable.button_shape2)
                    holder.btnAddRemove.text = "Add"
                    listener.onRemoveItemClick(items)
                }
            }
        }


    }



    class ItemListViewHolder(view: View): RecyclerView.ViewHolder(view){
        var item_id:TextView = view.findViewById(R.id.txtItemCount)
        val txtItemName: TextView = view.findViewById(R.id.txtItemName)
        val txtItemcost: TextView = view.findViewById(R.id.txtItemCost)
        val btnAddRemove: Button = view.findViewById(R.id.btnAddRemove)
    }






    class DBAsyncTask(context: Context, val cartEntity: CartEntity, val mode: Int) :
        AsyncTask<Void, Void, Boolean>() {

        val db = Room.databaseBuilder(context, CartDatabase::class.java,"cart-db").build()

        override fun doInBackground(vararg params: Void?): Boolean {
            when (mode) {
                1 -> {
                    val cart: CartEntity? =
                        db.cartDao().getItemById(cartEntity.item_id.toString())
                    db.close()
                    return cart!= null
                }
                2 -> {
                    db.cartDao().insertItem(cartEntity)
                    db.close()
                    return true
                }
                3 -> {
                    db.cartDao().deleteItem(cartEntity)
                    db.close()
                    return true
                }
//                4 -> {
//                    val cart: Int =db.cartDao().getRowCount()
//                    db.close()
//                    if (cart>0){
//                        return true
//                    }
//                }
            }
            return false
        }
    }

}