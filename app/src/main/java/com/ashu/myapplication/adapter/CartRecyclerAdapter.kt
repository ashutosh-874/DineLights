package com.ashu.myapplication.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ashu.myapplication.R
import com.ashu.myapplication.database.CartEntity
import com.ashu.myapplication.model.FoodItem
import com.ashu.myapplication.model.Items
import java.util.ArrayList

class CartRecyclerAdapter(
    val context: Context, val itemList:List<CartEntity>
):RecyclerView.Adapter<CartRecyclerAdapter.CartViewHolder>() {

    class CartViewHolder(view: View): RecyclerView.ViewHolder(view){
        val txtItemName: TextView = view.findViewById(R.id.txtItemName)
        val txtItemCost: TextView = view.findViewById(R.id.txtItemCost)
        val txtItemCount: TextView = view.findViewById(R.id.txtCount)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder:CartViewHolder, position: Int) {

        val item  = itemList[position]

        holder.txtItemName.text = item.itemName
        holder.txtItemCost.text = "Rs. ${item.itemCost}"
        holder.txtItemCount.text  =(position+1).toString()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_cart_singlerow,parent,false)
        return CartViewHolder(view)
    }

    override fun getItemCount(): Int {
        return  itemList.size
    }

}