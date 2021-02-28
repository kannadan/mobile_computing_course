package com.example.notes

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
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
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import androidx.core.app.NotificationCompat
import androidx.work.PeriodicWorkRequestBuilder

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
            val checkRem = binding.checkRem
            val checkRep = binding.checkRep
            Log.d("SETREMINDER", "saving")
            CoroutineScope(Dispatchers.Main).launch {
                val dbHelper = DatabaseHelperImpl(AppDatabase.DatabaseBuilder.getInstance(applicationContext))
                if(reminder == null){
                    val uuid = dbHelper.insertReminder(message).toInt()
                    WorkManager.getInstance(applicationContext).cancelAllWorkByTag(uuid.toString());
                    if(checkRem.isChecked){
                        if(checkRep.isChecked){
                            setRepeatingReminderWithWorkManager(
                                applicationContext,
                                uuid,
                                c.timeInMillis,
                                message.message
                            )
                        }else {
                            setReminderWithWorkManager(
                                applicationContext,
                                uuid,
                                c.timeInMillis,
                                message.message
                            )
                        }
                    }
                } else {
                    dbHelper.updateReminder(message);
                    Log.d("SETREMINDER", "setting")
                    WorkManager.getInstance(applicationContext).cancelAllWorkByTag(message.rid!!.toString());
                    if(checkRem.isChecked){
                        Log.d("SETREMINDER", "IS CHECKED")
                        if(checkRep.isChecked){
                            Log.d("SETREMINDER", "REPEATING")
                            setRepeatingReminderWithWorkManager(
                                applicationContext,
                                message.rid!!,
                                c.timeInMillis,
                                message.message
                            )
                        }else {
                            Log.d("SET REMINDER", "SINGLE")
                            setReminderWithWorkManager(
                                applicationContext,
                                message.rid!!,
                                c.timeInMillis,
                                message.message
                            )
                        }
                    }

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
    companion object {
        fun setReminderWithWorkManager(
                context: Context,
                uid: Int,
                timeInMillis: Long,
                message: String
        ) {

            val reminderParameters = Data.Builder()
                    .putString("message", message)
                    .putInt("uid", uid)
                    .build()

            // get minutes from now until reminder
            var minutesFromNow = 0L
            if (timeInMillis > System.currentTimeMillis())
                minutesFromNow = timeInMillis - System.currentTimeMillis()
            Log.d("TIME REM", minutesFromNow.toString())
            Log.d("TIME REM", timeInMillis.toString())
            Log.d("TIME REM", System.currentTimeMillis().toString())

            val reminderRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInputData(reminderParameters)
                    .setInitialDelay(minutesFromNow, TimeUnit.MILLISECONDS)
                    .addTag(uid.toString())
                    .build()

            WorkManager.getInstance(context).enqueue(reminderRequest)
        }

        fun setRepeatingReminderWithWorkManager(
            context: Context,
            uid: Int,
            timeInMillis: Long,
            message: String
        ) {

            val reminderParameters = Data.Builder()
                .putString("message", message)
                .putInt("uid", uid)
                .build()

            // get minutes from now until reminder
            var minutesFromNow = 0L
            if (timeInMillis > System.currentTimeMillis())
                minutesFromNow = timeInMillis - System.currentTimeMillis()
            Log.d("TIME REM", minutesFromNow.toString())
            Log.d("TIME REM", timeInMillis.toString())
            Log.d("TIME REM", System.currentTimeMillis().toString())

            val reminderRequest = PeriodicWorkRequestBuilder<ReminderWorker>(15, TimeUnit.MINUTES)
                .setInputData(reminderParameters)
                .setInitialDelay(minutesFromNow, TimeUnit.MILLISECONDS)
                .addTag(uid.toString())
                .build()

            WorkManager.getInstance(context).enqueue(reminderRequest)
        }

        fun showNotification(context: Context, message: String) {

            val CHANNEL_ID = "REM_ID"
            var notificationId = Random.nextInt(10, 1000) + 5
            val resultIntent = Intent(context, MainActivity::class.java)

            val resultPendingIntent: PendingIntent? = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)


            var notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_time)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(message)
                    .setContentIntent(resultPendingIntent)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setGroup(CHANNEL_ID)

            val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Notification chancel needed since Android 8
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                        CHANNEL_ID,
                        context.getString(R.string.app_name),
                        NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = context.getString(R.string.app_name)
                }
                notificationManager.createNotificationChannel(channel)
            }

            notificationManager.notify(notificationId, notificationBuilder.build())

        }
    }

}