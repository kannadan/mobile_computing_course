package com.example.notes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.notes.db.AppDatabase
import com.example.notes.db.DatabaseHelperImpl
import com.example.notes.db.Reminder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class Message : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        val listView = findViewById<ListView>(R.id.messages)
        val arrayAdapter: ArrayAdapter<String>
        var remAdapter: ReminderAdapter
        var msgs: ArrayList<String> = ArrayList()
        var rems: ArrayList<Reminder> = ArrayList()
        for (i in 1..50) {
            msgs.add("Msg $i")
        }
        val dbHelper = DatabaseHelperImpl(AppDatabase.DatabaseBuilder.getInstance(applicationContext))
        CoroutineScope(Dispatchers.Main).launch {
            var reminders = dbHelper.getReminders()
            reminders = reminders.filter { it ->

                val year = it.reminder_time.toString().substring(6, 10)
                val month = it.reminder_time.toString().substring(3,5)
                val day = it.reminder_time.toString().substring(0,2)
                val hour = it.reminder_time.toString().substring(11,13)
                val minute = it.reminder_time.toString().substring(14)
                var cal = Calendar.getInstance()
                cal.set(Calendar.YEAR, year.toInt())
                cal.set(Calendar.MONTH, month.toInt() -1 )
                cal.set(Calendar.DAY_OF_MONTH, day.toInt())
                cal.set(Calendar.HOUR_OF_DAY, hour.toInt())
                cal.set(Calendar.MINUTE, minute.toInt())
                val cal2 = Calendar.getInstance()
                cal.timeInMillis < cal2.timeInMillis
            }
            rems.addAll(reminders)
            remAdapter = ReminderAdapter(applicationContext, rems)
            listView.adapter = remAdapter

            listView.setOnItemClickListener{ parent, view, position, id ->
                val intent = Intent(applicationContext, ReminderActivity::class.java)
                intent.putExtra("REM", rems[position])
                startActivity(intent)
            }
        }
        arrayAdapter = ArrayAdapter(this,
                android.R.layout.simple_list_item_1, msgs)
        //listView.adapter = re

        val btnOut = findViewById<Button>(R.id.logout)
        btnOut.setOnClickListener {
            applicationContext.getSharedPreferences(
                    getString(R.string.sharedPreference),
                    Context.MODE_PRIVATE
            ).edit().remove("username").commit();
            finish()
        }
        val btnRemind = findViewById<Button>(R.id.new_reminder)
        btnRemind.setOnClickListener {
            //Toast.makeText(applicationContext, R.string.upcoming, Toast.LENGTH_SHORT).show()
            val intent = Intent(this, ReminderActivity::class.java)
            startActivity(intent)
        }
        val btnshow = findViewById<Button>(R.id.showAll)
        btnshow.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val reminders = dbHelper.getReminders()
                rems.clear()
                rems.addAll(reminders)
                remAdapter = ReminderAdapter(applicationContext, rems)
                listView.adapter = remAdapter
                listView.setOnItemClickListener{ parent, view, position, id ->
                    val intent = Intent(applicationContext, ReminderActivity::class.java)
                    intent.putExtra("REM", rems[position])
                    startActivity(intent)
                }
            }
        }

        val btnProf = findViewById<ImageButton>(R.id.profileBtn)
        btnProf.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }
    }

    override fun onRestart() {
        super.onRestart()
        val listView = findViewById<ListView>(R.id.messages)
        var remAdapter: ReminderAdapter
        var rems: ArrayList<Reminder> = ArrayList()
        val dbHelper = DatabaseHelperImpl(AppDatabase.DatabaseBuilder.getInstance(applicationContext))
        CoroutineScope(Dispatchers.Main).launch {
            var reminders = dbHelper.getReminders()
            reminders = reminders.filter { it ->

                val year = it.reminder_time.toString().substring(6, 10)
                val month = it.reminder_time.toString().substring(3,5)
                val day = it.reminder_time.toString().substring(0,2)
                val hour = it.reminder_time.toString().substring(11,13)
                val minute = it.reminder_time.toString().substring(14)
                var cal = Calendar.getInstance()
                cal.set(Calendar.YEAR, year.toInt())
                cal.set(Calendar.MONTH, month.toInt() -1 )
                cal.set(Calendar.DAY_OF_MONTH, day.toInt())
                cal.set(Calendar.HOUR_OF_DAY, hour.toInt())
                cal.set(Calendar.MINUTE, minute.toInt())
                val cal2 = Calendar.getInstance()
                cal.timeInMillis < cal2.timeInMillis
            }
            rems.addAll(reminders)
            remAdapter = ReminderAdapter(applicationContext, rems)
            listView.adapter = remAdapter
            listView.setOnItemClickListener{ parent, view, position, id ->
                val intent = Intent(applicationContext, ReminderActivity::class.java)
                intent.putExtra("REM", rems[position])
                startActivity(intent)
            }
        }
        //Do your code here
    }
}