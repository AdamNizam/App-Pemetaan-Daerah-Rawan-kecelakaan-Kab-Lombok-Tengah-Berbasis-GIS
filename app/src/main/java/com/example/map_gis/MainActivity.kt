package com.example.map_gis

import android.content.Context
import android.content.Intent
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.Polyline
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity(), OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener{

    private lateinit var googleMap: GoogleMap
    private lateinit var auth: FirebaseAuth
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var currentUserLocation: LatLng? = null
    private lateinit var googleSignInClient: GoogleSignInClient
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val menuImageView = findViewById<ImageView>(R.id.menu)
        menuImageView.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        val navView = findViewById<NavigationView>(R.id.nav_view)
        navView.setNavigationItemSelectedListener { menuItem ->
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        auth = FirebaseAuth.getInstance()
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val currentUser = auth.currentUser
        val header = navView.getHeaderView(0)
        val username =  header.findViewById<TextView>(R.id.username)
        val email = header.findViewById<TextView>(R.id.email)
        val profilImage =  header.findViewById<ImageView>(R.id.profileImage)
        val photoUrl = currentUser?.photoUrl
        if (currentUser != null) {
            username.text = currentUser.displayName
        }
        if (currentUser != null) {
            email.text = currentUser.email
        }
        if (photoUrl != null) {
            Glide.with(this)
                .load(photoUrl)
                .circleCrop()
                .into(profilImage)
        }
        navView.setNavigationItemSelectedListener(this)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    currentUserLocation = currentLatLng
                    showCurrentLocationOnMap(currentLatLng, auth.currentUser?.displayName ?: "", auth.currentUser?.photoUrl)
                }
            }
        }

    }
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        getCurrentLocation()
        requestLocationUpdates()
        val LombokTengah = LatLng(-8.7131, 116.2716)
        val defaultZoomLevel = 10f
        val radius = 900.0
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("data_kecelakaan")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (locationSnapshot in dataSnapshot.children) {
                    val latitude = locationSnapshot.child("latitude").getValue(Double::class.java)
                    val longitude = locationSnapshot.child("longitude").getValue(Double::class.java)
                    val nama = locationSnapshot.child("namaJalan").getValue(String::class.java)
                    val location = LatLng(latitude!!, longitude!!)
                    googleMap.addMarker(MarkerOptions().position(location).title(nama))
                    val circleOptions = CircleOptions()
                        .center(location)
                        .radius(radius)
                        .strokeColor(Color.RED)
                        .fillColor(Color.argb(70, 0, 0, 255))
                    googleMap.addCircle(circleOptions)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LombokTengah, defaultZoomLevel))
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation()
                } else {
                    Toast.makeText(this,"Izin Ditolak", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 10f))
                    val user = auth.currentUser
                    val displayName = user?.displayName
                    val photoUrl = user?.photoUrl
                    if (displayName != null) {
                        showCurrentLocationOnMap(currentLatLng, displayName, photoUrl)
                    } else {
                        Log.w("Location", "Display name is null.")
                        showLocationUnavailableDialog(this)
                    }
                } ?: run {
                    Log.e("Location", "Last known location is null.")
                    showLocationUnavailableDialog(this)
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            Log.e("Location", "Location permission not granted.")
        }
    }
    private fun requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            Log.e("Location", "Location permission not granted.")
        }
    }
    private fun showLocationUnavailableDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Location Tidak Ditemukan")
            .setMessage("Pastikan Anda Mengaktipkan Layanan GPS Pada Smartphone Anda!")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = builder.create()
        dialog.show()
    }
    private fun showCurrentLocationOnMap(location: LatLng, username: String, photoUrl: Uri?) {
        val markerTitle = "Hello $username"
        val markerOptions = MarkerOptions()
            .position(location)
            .title(markerTitle)
            .icon(getMarkerIconFromURL(photoUrl))
        googleMap.addMarker(markerOptions)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10f))
        currentUserLocation = location
    }
    private fun getMarkerIconFromURL(photoUrl: Uri?): BitmapDescriptor {
        return runBlocking(Dispatchers.IO) {
            try {
                val markerIcon = Glide.with(applicationContext)
                    .asBitmap()
                    .load(photoUrl)
                    .transform(CircleCrop())
                    .submit()
                    .get()

                return@runBlocking BitmapDescriptorFactory.fromBitmap(markerIcon)
            } catch (e: Exception) {
                Log.e("Location", "Error loading user's photo: ${e.message}")
                return@runBlocking BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            }
        }
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.peta -> {
                Toast.makeText(this, "Successfully", Toast.LENGTH_LONG).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            R.id.tambahDataKecelakaan ->{
                val intent = Intent(this, TambahData::class.java)
                startActivity(intent)
            }
            R.id.notifikasi -> {
                Toast.makeText(this, "Successfully", Toast.LENGTH_LONG).show()
            }
            R.id.logout -> {
                signOut()
            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
    private fun signOut() {
        googleSignInClient.signOut()
            .addOnCompleteListener(this) {
                Toast.makeText(applicationContext,"Logout succesfully", Toast.LENGTH_SHORT).show()
               val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
    }

}

