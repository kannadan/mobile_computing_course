package com.example.notes.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) var uid: Int?,
    @ColumnInfo(name = "username") var username: String,
    @ColumnInfo(name = "password") var password: String,
)