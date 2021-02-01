package com.example.notes


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
import kotlinx.coroutines.*


class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        val username = findViewById<EditText>(R.id.regUsername)
        val pass1 = findViewById<EditText>(R.id.regPassword)
        val pass2 = findViewById<EditText>(R.id.regPassword2)
        val btnReg = findViewById<Button>(R.id.regBtn)
        btnReg.setOnClickListener {
            val un = username.text.toString()
            val pass = pass1.text.toString()
            val verify = pass2.text.toString()
            if(un.isEmpty() || pass.isEmpty() || verify.isEmpty()){
                Toast.makeText(applicationContext, R.string.emptyField, Toast.LENGTH_SHORT).show()
            } else if( pass != verify){
                Toast.makeText(applicationContext, R.string.passMiss, Toast.LENGTH_SHORT).show()
            } else {
                val hash = BCrypt.hashpw(pass.toString(), BCrypt.gensalt())
                val user = User(
                    null,
                    username = un,
                    password = hash,
                )
                Log.d("USER", user.toString())
                CoroutineScope(Dispatchers.Main).launch {
                    val dbHelper = DatabaseHelperImpl(AppDatabase.DatabaseBuilder.getInstance(applicationContext))
                    dbHelper.insert(user)
                    finish()
                }
            }


        }
    }
}