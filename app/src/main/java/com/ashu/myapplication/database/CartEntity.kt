package com.ashu.myapplication.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart")
data class CartEntity (
    @PrimaryKey val item_id: Int,
    @ColumnInfo(name = "item_name")val itemName: String,
    @ColumnInfo(name = "item_Cost")val itemCost: String
)