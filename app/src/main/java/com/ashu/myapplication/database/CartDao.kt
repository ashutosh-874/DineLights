package com.ashu.myapplication.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query


@Dao
interface CartDao {

    @Insert
    fun insertItem(cartEntity: CartEntity)

    @Delete
    fun deleteItem(cartEntity: CartEntity)

    @Query("DELETE FROM cart")
    fun deleteAllItems()

    @Query("SELECT * FROM cart")
    fun getAllItems(): List<CartEntity>

    @Query("SELECT COUNT(item_name) FROM CART")
    fun getRowCount():Int

    @Query("SELECT * FROM cart WHERE  item_id = :itemId")
    fun getItemById(itemId: String): CartEntity

}
