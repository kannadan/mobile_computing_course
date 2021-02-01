package com.example.notes.db



interface DatabaseHelper {

    suspend fun insert(user: User): Long

    suspend fun delete(id: Int)

    suspend fun getUsers(): List<User>
    suspend fun getUser(username: String): List<User>
}
