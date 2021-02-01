package com.example.notes

import android.content.Context
import android.content.Intent
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*

class Message : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        val listView = findViewById<ListView>(R.id.messages)
        val arrayAdapter: ArrayAdapter<String>
        var msgs: ArrayList<String> = ArrayList()
        for (i in 1..50) {
            msgs.add("Msg $i")
        }
        arrayAdapter = ArrayAdapter(this,
            android.R.layout.simple_list_item_1, msgs)
        listView.adapter = arrayAdapter

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
            Toast.makeText(applicationContext, R.string.upcoming, Toast.LENGTH_SHORT).show()
        }

        val btnProf = findViewById<ImageButton>(R.id.profileBtn)
        btnProf.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }
    }
}