package com.example.notes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnReg = findViewById<Button>(R.id.reg)
        btnReg.setOnClickListener {
            Toast.makeText(applicationContext, R.string.upcoming, Toast.LENGTH_SHORT).show()
        }

        val btnLog = findViewById<Button>(R.id.login)
        btnLog.setOnClickListener {
            Log.d("LOGIN", "Clicked login button")
            val intent = Intent(this, Message::class.java)
            startActivity(intent)

        }
    }

    override fun onResume() {
        super.onResume()
        resetText()
    }

    private fun resetText() {
        findViewById<EditText>(R.id.txtUsername).setText("");
        findViewById<EditText>(R.id.txtPassword).setText("");
    }
}