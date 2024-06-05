package com.example.map_gis

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class PetaActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var previousLocation: Location? = null
    private lateinit var kecamatanName: String
    private lateinit var lokasiList: ArrayList<String>
    private lateinit var jumlahLakaList: ArrayList<Int>
    private lateinit var latitudeList: DoubleArray
    private lateinit var longitudeList: DoubleArray
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_peta)

        kecamatanName = intent.getStringExtra("kecamatanName") ?: ""
        lokasiList = intent.getStringArrayListExtra("lokasiList") ?: arrayListOf()
        jumlahLakaList = intent.getIntegerArrayListExtra("jumlahLakaList") ?: arrayListOf()
        latitudeList = intent.getDoubleArrayExtra("latitudeList") ?: doubleArrayOf()
        longitudeList = intent.getDoubleArrayExtra("longitudeList") ?: doubleArrayOf()
        findViewById<TextView>(R.id.namakecamatan).text = "Nama Kecamatan : $kecamatanName"
        val layoutList = findViewById<LinearLayout>(R.id.layout_list)
        if (lokasiList.size == jumlahLakaList.size && jumlahLakaList.size == latitudeList.size && latitudeList.size == longitudeList.size) {
            for (i in lokasiList.indices) {
                val itemLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(8, 0, 0, 8)
                    }
                    setPadding(16, 16, 16, 16)
                }

                val textLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                }

                val nameTextView = TextView(this).apply {
                    text = lokasiList[i]
                    textSize = 14f
                    setTypeface(null, android.graphics.Typeface.BOLD)
                }

                val lakaTextView = TextView(this).apply {
                    text = "Jumlah Laka: ${jumlahLakaList[i]}"
                    textSize = 12f
                    setTextColor(Color.RED)
                }

                textLayout.addView(nameTextView)
                textLayout.addView(lakaTextView)
                itemLayout.addView(textLayout)
                layoutList.addView(itemLayout)
            }
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.MapPeta) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            fetchUserLocation()
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchUserLocation()
            } else {
                Toast.makeText(
                    this,
                    "Izin lokasi diperlukan untuk menampilkan lokasi pengguna",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun fetchUserLocation() {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val userLatLng = LatLng(location.latitude, location.longitude)

                        val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
                        currentUser?.let { user ->
                            val displayName: String? = user.displayName
                            val photoUrl: String? = user.photoUrl?.toString()
                            photoUrl?.let {
                                Glide.with(this)
                                    .asBitmap()
                                    .load(photoUrl)
                                    .apply(RequestOptions.circleCropTransform())
                                    .into(object : SimpleTarget<Bitmap>() {
                                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                            mMap.addMarker(
                                                MarkerOptions()
                                                    .position(userLatLng)
                                                    .title(displayName)
                                                    .icon(BitmapDescriptorFactory.fromBitmap(resource))
                                            )
                                        }
                                    })
                            }
                        }
                    }
                }
        } catch (securityException: SecurityException) {
            Log.e("PetaActivity", "SecurityException: ${securityException.message}")
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (lokasiList.size == jumlahLakaList.size && jumlahLakaList.size == latitudeList.size && latitudeList.size == longitudeList.size) {
            for (i in lokasiList.indices) {
                val latLng = LatLng(latitudeList[i], longitudeList[i])
                val markerOptions = MarkerOptions()
                    .position(latLng)
                    .title(lokasiList[i])
                    .snippet("Jumlah Laka: ${jumlahLakaList[i]}")

                mMap.addMarker(markerOptions)
                if (i == 0) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
                }
                val circleOptions = CircleOptions()
                    .center(latLng)
                    .radius(100.0)
                    .strokeWidth(5f)
                    .strokeColor(Color.RED)
                    .fillColor(Color.argb(70, 50, 50, 150))

                val circle = mMap.addCircle(circleOptions)

                val radiusAnimator = ValueAnimator.ofFloat(0f, 1f)
                radiusAnimator.repeatCount = ValueAnimator.INFINITE
                radiusAnimator.repeatMode = ValueAnimator.REVERSE
                radiusAnimator.duration = 1000

                radiusAnimator.addUpdateListener { animation ->
                    val animatedFraction = animation.animatedFraction
                    circle.radius = (100 + 50 * animatedFraction).toDouble()
                }

                val colorAnimator = ValueAnimator.ofObject(
                    ArgbEvaluator(),
                    Color.argb(70, 50, 50, 150),
                    Color.argb(70, 150, 50, 50)
                )
                colorAnimator.repeatCount = ValueAnimator.INFINITE
                colorAnimator.repeatMode = ValueAnimator.REVERSE
                colorAnimator.duration = 2000 // durasi 2 detik

                colorAnimator.addUpdateListener { animation ->
                    circle.fillColor = animation.animatedValue as Int
                }

                radiusAnimator.start()
                colorAnimator.start()
            }
        }
    }

    //end
}
