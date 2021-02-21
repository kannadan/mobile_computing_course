package com.example.notes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.notes.db.AppDatabase
import com.example.notes.db.DatabaseHelperImpl
import com.example.notes.db.Reminder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
            val reminders = dbHelper.getReminders()
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
            val reminders = dbHelper.getReminders()
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