package com.example.locationapp

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.example.locationapp.databinding.ActivityMainBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMainBinding
    private val locationViewModel: LocationViewModel by viewModels()
    private lateinit var googleMap: GoogleMap


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {

            locationViewModel.startLocationUpdates()
            binding.locationStatusTextView.visibility = android.view.View.GONE
            binding.latitudeTextView.visibility = android.view.View.VISIBLE
            binding.longitudeTextView.visibility = android.view.View.VISIBLE
            binding.showMapButton.visibility = android.view.View.VISIBLE
        } else {

            binding.locationStatusTextView.visibility = android.view.View.VISIBLE
            binding.latitudeTextView.visibility = android.view.View.GONE
            binding.longitudeTextView.visibility = android.view.View.GONE
            binding.showMapButton.visibility = android.view.View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (allPermissionsGranted()) {

            locationViewModel.startLocationUpdates()
            binding.locationStatusTextView.visibility = android.view.View.GONE
            binding.latitudeTextView.visibility = android.view.View.VISIBLE
            binding.longitudeTextView.visibility = android.view.View.VISIBLE
            binding.showMapButton.visibility = android.view.View.VISIBLE
        } else {

            requestPermissions()
        }

        binding.showMapButton.setOnClickListener {
            if (allPermissionsGranted() && locationViewModel.locationData.value != null) {
                binding.map.visibility = android.view.View.VISIBLE
            } else {
                binding.locationStatusTextView.text = "Unable to display map. Location services or permissions are disabled."
                binding.locationStatusTextView.visibility = android.view.View.VISIBLE
            }
        }


        locationViewModel.latitude.observe(this, Observer { latitude ->
            binding.latitudeTextView.text = getString(R.string.latitude_text, latitude.toString())
        })

        locationViewModel.longitude.observe(this, Observer { longitude ->
            binding.longitudeTextView.text = getString(R.string.longitude_text, longitude.toString())
        })


        locationViewModel.locationData.observe(this, Observer { location ->
            location?.let {
                if (::googleMap.isInitialized) {
                    updateMap(it)
                }
            }
        })
    }

    private fun updateMap(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        googleMap.addMarker(MarkerOptions().position(latLng).title("Current Location"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
    }

    override fun onResume() {
        super.onResume()
        if (allPermissionsGranted()) {
            locationViewModel.startLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        locationViewModel.stopLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationViewModel.stopLocationUpdates()
    }

    private fun allPermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
}
