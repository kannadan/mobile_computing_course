package com.example.notes.db


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface UserDao {
    @Transaction
    @Insert
    suspend fun insert(user: User): Long

    @Query("DELETE FROM users WHERE uid = :id")
    suspend fun delete(id: Int)

    @Query("SELECT * FROM users")
    suspend fun getUsers(): List<User>

    @Query("SELECT * FROM users WHERE username=:username ")
    suspend fun getUser(username: String): List<User>
}