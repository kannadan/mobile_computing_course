package com.example.notes.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "reminders")
class Reminder(
       @PrimaryKey(autoGenerate = true) var rid: Int?,
       @ColumnInfo(name = "message") var message: String,
       @ColumnInfo(name = "location_x") var location_x: String,
       @ColumnInfo(name = "location_y") var location_y: String,
       @ColumnInfo(name = "reminder_time") var reminder_time: String,
       @ColumnInfo(name = "creation_time") var creation_time: Long,
       @ColumnInfo(name = "creator_id") var creator_id: String,
       @ColumnInfo(name = "reminder_seen") var reminder_seen: String,

       ) : Serializable{



}