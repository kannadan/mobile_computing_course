package com.example.notes

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*
import com.example.notes.databinding.ActivityReminderBinding
import com.example.notes.db.AppDatabase
import com.example.notes.db.DatabaseHelperImpl
import com.example.notes.db.Reminder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ReminderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReminderBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dateView = binding.remDate
        val messageView = binding.message
        val reminder = intent.getSerializableExtra("REM") as? Reminder

        //val dateF = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val timeF = SimpleDateFormat("dd.MM.YYYY HH:mm", Locale.getDefault())
        //val date: String = dateF.format(Calendar.getInstance().time)
        val time: String = timeF.format(c.time)

        dateView.text = time
        if(reminder != null){
            Log.d("REMACT", reminder!!.message)
            messageView.setText(reminder.message)
            dateView.text = reminder.reminder_time
        }

        val timeBtn = binding.timeButton
        timeBtn.setOnClickListener {
            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                // Display Selected date in TextView
                c.set(Calendar.YEAR, year)
                c.set(Calendar.MONTH, monthOfYear)
                c.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                    c.set(Calendar.HOUR_OF_DAY, hour)
                    c.set(Calendar.MINUTE, minute)
                    val time: String = timeF.format(c.time)
                    dateView.text = time
                }
                TimePickerDialog(this, timeSetListener, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
            }, year, month, day)
            dpd.show()
        }

        val saveBtn = binding.save
        val delBtn = binding.delete
        saveBtn.setOnClickListener {
            var message: Reminder
            if(reminder == null){
                message = Reminder(
                        null,
                        messageView.text.toString(),
                        "",
                        "",
                        dateView.text.toString(),
                        Calendar.getInstance().timeInMillis,
                        applicationContext.getSharedPreferences(getString(R.string.sharedPreference), MODE_PRIVATE).getString("username", "").toString(),
                        "no");
            } else {
                message = reminder
                message.message = messageView.text.toString()
                message.reminder_time = dateView.text.toString()
            }

            CoroutineScope(Dispatchers.Main).launch {
                val dbHelper = DatabaseHelperImpl(AppDatabase.DatabaseBuilder.getInstance(applicationContext))
                if(reminder == null){
                    dbHelper.insertReminder(message)
                } else {
                    dbHelper.updateReminder(message)
                }

                finish()
            }
            //Log.d("SAVE", message.message)
        }
        delBtn.setOnClickListener {
            if(reminder != null){
                CoroutineScope(Dispatchers.Main).launch {
                    val dbHelper = DatabaseHelperImpl(AppDatabase.DatabaseBuilder.getInstance(applicationContext))
                    if(reminder.rid != null){
                        val id: Int = reminder.rid!!
                        dbHelper.deleteReminder(id)
                    }


                    finish()
                }
            } else {
                finish()
            }
        }

        //autoTime.setText(time)
    }
}