package com.example.notes

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class ReminderWorker(appContext:Context, workerParameters: WorkerParameters) :
    Worker(appContext,workerParameters) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var userLocation: Location
    override fun doWork(): Result {

        val text = inputData.getString("message") // this comes from the reminder parameters
        var latDoub: Double = 0.0
        var lonDoub: Double = 0.0
        var distInt = 0
        val latitude = inputData.getString("Lat")
        val longitude = inputData.getString("Lon")
        val maxDist = inputData.getString("Dist")
        if(latitude != null && latitude != ""){
            latDoub = latitude.toDouble()
        }
        if(longitude != null && longitude != ""){
            lonDoub = longitude.toDouble()
        }
        if(maxDist != null && maxDist != ""){
            distInt = maxDist.toInt()
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("LOCATIONTESTREM", "no prem")
        } else {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    // Got last known location. In some rare situations this can be null.
                    Log.d("LOCATIONTEST", "jello")
                    if (location != null) {
                        userLocation = location
                        if(latDoub != 0.0 && lonDoub != 0.0 && distInt != 0){
                            Log.d("REMOWORK", "LOCATION $latDoub $lonDoub")
                            Log.d("REMOWORK", "LOCATION user ${userLocation.latitude} ${userLocation.longitude}")
                            val distance = calcDistance(latDoub, lonDoub)
                            Log.d("REMOWORK", "DISTANCE $distance")
                            if(distance != null && distance/1000 < distInt){
                                ReminderActivity.showNotification(applicationContext,text!!)
                            }
                        }
                    }
                }
        }
        if(latDoub == 0.0 && lonDoub == 0.0 && distInt == 0) {
            Log.d("REMOWORK", "NO LOCATIOn")
            ReminderActivity.showNotification(applicationContext,text!!)
        }

        return   Result.success()
    }

    private fun calcDistance(lat: Double, lon: Double): Float {
        var tempLoc = Location("")
        tempLoc.latitude = lat
        tempLoc.longitude = lon
        return tempLoc.distanceTo(userLocation)

    }
}