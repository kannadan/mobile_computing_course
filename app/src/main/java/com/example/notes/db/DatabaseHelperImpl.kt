package com.example.notes.db

class DatabaseHelperImpl(private val appDatabase: AppDatabase) : DatabaseHelper {

    override suspend fun insert(user: User): Long = appDatabase.userDao().insert(user)

    override suspend fun delete(id: Int) = appDatabase.userDao().delete(id)
    override suspend fun getUsers(): List<User> = appDatabase.userDao().getUsers()
    override suspend fun getUser(username: String): List<User> = appDatabase.userDao().getUser(username)

}
