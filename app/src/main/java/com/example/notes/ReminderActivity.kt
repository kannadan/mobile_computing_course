package com.example.notes

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.notes.databinding.ActivityReminderBinding
import com.example.notes.db.AppDatabase
import com.example.notes.db.DatabaseHelperImpl
import com.example.notes.db.Reminder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random


class ReminderActivity : AppCompatActivity(), TextToSpeech.OnInitListener{
    private lateinit var binding: ActivityReminderBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var userLocation: Location
    private lateinit var customLocation: Location
    private var  textToSpeech: TextToSpeech? = null
    private  var reminder: Reminder? = null
    private var definedText = ""

    override fun onInit(status: Int) {
        // check the results in status variable.
        if (status == TextToSpeech.SUCCESS) {
            // setting the language to the default phone language.
            val ttsLang = textToSpeech!!.setLanguage(Locale.getDefault())
            // check if the language is supportable.
            if (ttsLang == TextToSpeech.LANG_MISSING_DATA || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "We can't support your language", Toast.LENGTH_LONG).show()
            } else {
                speak(definedText)
            }
        } else {
            Toast.makeText(this, "TTS Initialization failed!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun speak(text: String){
        var speechStatus = textToSpeech!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ID")
        if (speechStatus == TextToSpeech.ERROR) {
            Toast.makeText(this, "Cant use the Text to speech.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        textToSpeech = TextToSpeech(this, this)


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            Log.d("LOCATIONTEST", "no prem")
            return
        } else {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    // Got last known location. In some rare situations this can be null.
                    Log.d("LOCATIONTEST", "jello")
                    if (location != null) {
                        userLocation = location
                    }
                }
        }



        val dateView = binding.remDate
        val messageView = binding.message
        val temp = intent.getSerializableExtra("REM") as? Reminder
        if(temp != null){
            reminder = temp
        }

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
            messageView.setText(reminder!!.message)
            dateView.text = reminder!!.reminder_time
            definedText = reminder!!.message
        }


        val timeBtn = binding.timeButton
        timeBtn.setOnClickListener {
            val dpd = DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
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
                    TimePickerDialog(
                        this,
                        timeSetListener,
                        c.get(Calendar.HOUR_OF_DAY),
                        c.get(Calendar.MINUTE),
                        true
                    ).show()
                },
                year,
                month,
                day
            )
            dpd.show()
        }

        val saveBtn = binding.save
        val delBtn = binding.delete
        val checkRem = binding.checkRem
        val checkRep = binding.checkRep
        val checkLoc = binding.location
        val checkLocCur = binding.locationCurrent

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
                    applicationContext.getSharedPreferences(
                        getString(R.string.sharedPreference),
                        MODE_PRIVATE
                    ).getString("username", "").toString(),
                    "no"
                );
            } else {

                message = reminder as Reminder
                message.message = messageView.text.toString()
                message.reminder_time = dateView.text.toString()
            }

            if(checkLocCur.isChecked && userLocation != null){
                message.location_x = userLocation.latitude.toString()
                message.location_y = userLocation.longitude.toString()
                Log.d("LOCATIONTEST", "${message.location_x} ${message.location_y}")
            }
            if(checkLoc.isChecked && customLocation != null){
                message.location_x = customLocation.latitude.toString()
                message.location_y = customLocation.longitude.toString()
                Log.d("LOCATIONTEST", "${message.location_x} ${message.location_y}")
            }
            Log.d("SETREMINDER", "saving")
            CoroutineScope(Dispatchers.Main).launch {
                val dbHelper = DatabaseHelperImpl(
                    AppDatabase.DatabaseBuilder.getInstance(
                        applicationContext
                    )
                )
                if(reminder == null){
                    val uuid = dbHelper.insertReminder(message).toInt()
                    WorkManager.getInstance(applicationContext).cancelAllWorkByTag(uuid.toString());
                    if(checkRem.isChecked){
                        if(checkRep.isChecked){
                            setRepeatingReminderWithWorkManager(
                                applicationContext,
                                uuid,
                                c.timeInMillis,
                                message
                            )
                        }else {
                            setReminderWithWorkManager(
                                applicationContext,
                                uuid,
                                c.timeInMillis,
                                message
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
                                message
                            )
                        }else {
                            Log.d("SET REMINDER", "SINGLE")
                            setReminderWithWorkManager(
                                applicationContext,
                                message.rid!!,
                                c.timeInMillis,
                                message
                            )
                        }
                    }

                }
                //val intent = Intent(applicationContext, MapThatEveryAppNeeds::class.java)
                //startActivity(intent)
                finish()
            }
            //Log.d("SAVE", message.message)
        }
        delBtn.setOnClickListener {
            if(reminder != null){
                CoroutineScope(Dispatchers.Main).launch {
                    val dbHelper = DatabaseHelperImpl(
                        AppDatabase.DatabaseBuilder.getInstance(
                            applicationContext
                        )
                    )
                    if(reminder!!.rid != null){
                        val id: Int = reminder!!.rid!!
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

    fun onLocationClick(view: View){
        if(view is CheckBox){
            if(view.isChecked){
                val intent = Intent(applicationContext, MapThatEveryAppNeeds::class.java)
                startActivityForResult(intent, 1)
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("RESULTACT", resultCode.toString())
        if (resultCode == 1) {
            Log.d("RESULTACT", "okokokokok")
            if (data != null) {
                customLocation = Location("")
                data.extras?.getDouble("Lat")?.let { customLocation.latitude = it }
                data.extras?.getDouble("Lon")?.let { customLocation.longitude = it }
                Log.d("LOCATIONTEST2", customLocation.latitude.toString())
                Log.d("LOCATIONTEST2", data.extras?.getDouble("Lat").toString())
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    // Got last known location. In some rare situations this can be null.
                    Log.d("LOCATIONTEST", "jello2")
                    if (location != null) {
                        userLocation = location
                    }
                }
            }

    companion object {
        fun setReminderWithWorkManager(
            context: Context,
            uid: Int,
            timeInMillis: Long,
            message: Reminder
        ) {

            val reminderParameters = Data.Builder()
                    .putString("message", message.message)
                    .putString("Lat", message.location_x)
                    .putString("Lon", message.location_y)
                    .putString("Dist", "5")
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
            message: Reminder
        ) {

            val reminderParameters = Data.Builder()
                .putString("message", message.message)
                .putString("Lat", message.location_x)
                .putString("Lon", message.location_y)
                .putString("Dist", "5")
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

            val resultPendingIntent: PendingIntent? = PendingIntent.getActivity(
                context,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )


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