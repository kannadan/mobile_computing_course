package com.example.notes.db


import androidx.room.*

@Dao
interface ReminderDao {
    @Transaction
    @Insert
    suspend fun insertReminder(reminder: Reminder): Long

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Query("DELETE FROM reminders WHERE rid = :id")
    suspend fun deleteReminder(id: Int)

    @Query("SELECT * FROM reminders")
    suspend fun getReminders(): List<Reminder>
}