package com.ashu.myapplication.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ResDao {
        @Insert
        fun insertRestaurant(resEntity: ResEntity)

        @Delete
        fun deleteRestaurant(resEntity: ResEntity)

        @Query("DELETE FROM restaurants")
        fun deleteAllRestaurants()

        @Query("SELECT * FROM restaurants" )
        fun getAllRestaurants(): List<ResEntity>

        @Query("SELECT * From restaurants WHERE  res_id = :resId")
        fun getRestaurantsById(resId: String): ResEntity
    }
