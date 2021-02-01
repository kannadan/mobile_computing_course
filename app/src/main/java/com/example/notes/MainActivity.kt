package com.example.notes

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.room.Room
import com.example.notes.db.AppDatabase
import com.example.notes.db.DatabaseHelperImpl
import com.example.notes.db.User
import com.example.notes.db.UserDao
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        applicationContext.getSharedPreferences(
            getString(R.string.sharedPreference),
            Context.MODE_PRIVATE
        ).edit().remove("username").commit();
        val username = findViewById<EditText>(R.id.txtUsername)
        val password = findViewById<EditText>(R.id.txtPassword)
        val btnReg = findViewById<Button>(R.id.reg)
        btnReg.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        val btnLog = findViewById<Button>(R.id.login)
        btnLog.setOnClickListener {
            val un = username.text.toString()
            val pass = password.text.toString()
            if(un.isEmpty() || pass.isEmpty()){
                Toast.makeText(applicationContext, R.string.emptyField, Toast.LENGTH_SHORT).show()
            } else {
                val dbHelper = DatabaseHelperImpl(AppDatabase.DatabaseBuilder.getInstance(applicationContext))
                CoroutineScope(Dispatchers.Main).launch {
                    val user = dbHelper.getUser(un)
                    when {
                        user.isEmpty() -> {
                            Toast.makeText(applicationContext, R.string.badUser, Toast.LENGTH_SHORT).show()
                        }
                        BCrypt.checkpw(pass, user[0].password) -> {
                            applicationContext.getSharedPreferences(
                                getString(R.string.sharedPreference),
                                Context.MODE_PRIVATE
                            ).edit().putString("username", un).apply()
                            val intent = Intent(applicationContext, Message::class.java)
                            startActivity(intent)
                        }
                        else -> {
                            Toast.makeText(applicationContext, R.string.badPass, Toast.LENGTH_SHORT).show()
                        }
                    }

                }

            }

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