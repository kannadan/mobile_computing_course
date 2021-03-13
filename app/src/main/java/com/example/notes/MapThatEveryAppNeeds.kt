package com.example.notes

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

class MapThatEveryAppNeeds : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private val TAG = MapThatEveryAppNeeds::class.java.simpleName
    private val REQUEST_LOCATION_PERMISSION = 1



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_that_every_app_needs)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        val oulu = LatLng(65.08238, 25.44262)
        val zoomLevel = 15f
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(oulu, zoomLevel))
        map.addMarker(MarkerOptions().position(oulu))

        setMapLongClick(map)
        setPoiClick(map)
        setMapStyle(map)
        enableMyLocation()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.map_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    // Allow users to add markers on map with long click
    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener {
            val snippet = String.format(
                    Locale.getDefault(),
                    "Lat: %1$.5f, Lng: %2$.5f",
                    it.latitude,
                    it.longitude
            )
            map.clear()
            map.addMarker(
                    MarkerOptions()
                            .position(it)
                            .title(getString(R.string.dropped_pin))
                            .snippet(snippet)
            )
            var resultCode = 1;
            val resultIntent = Intent()
            resultIntent.putExtra("Lat", it.latitude);
            resultIntent.putExtra("Lon", it.longitude);
            setResult(resultCode, resultIntent);
            finish();

            /*val database = Firebase.database("https://lateral-vision-303710-default-rtdb.europe-west1.firebasedatabase.app/")
            val myRef = database.getReference("locations")
            database.getReference("test_url").setValue("https://oulu.fi")
            println("Reference: $myRef")
            val key = myRef.push().key
//            val data = key?.let { it1 -> myRef.push().child("locations/").setValue("Hello world") }
            val data = myRef.push().child("data").setValue("Hello world")
//            myRef.setValue("Davido")

            println("Key: $key")
            println("Data: $data")*/
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener {
            val poiMarker = map.addMarker(MarkerOptions()
                    .position(it.latLng)
                    .title(it.name)
            )
            poiMarker.showInfoWindow()
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style)
            )
            if (!success) {
                Log.e(TAG, "Style parsing failed")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        if(requestCode == REQUEST_LOCATION_PERMISSION) {
            if(grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }
}