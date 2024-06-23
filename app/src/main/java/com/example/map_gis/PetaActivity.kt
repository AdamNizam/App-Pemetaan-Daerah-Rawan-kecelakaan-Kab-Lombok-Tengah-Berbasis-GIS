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
import android.location.Geocoder
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.example.map_gis.R.string.Map_Api_key_premium
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.maps.DirectionsApi.getDirections
import com.google.maps.android.PolyUtil
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale


class PetaActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var previousLocation: Location? = null
    private lateinit var kecamatanName: String
    private lateinit var lokasiList: ArrayList<String>
    private lateinit var jumlahLakaList: ArrayList<Int>
    private lateinit var latitudeList: DoubleArray
    private lateinit var longitudeList: DoubleArray
    private lateinit var closeCard : CardView
    private lateinit var closeIcon : ImageView
    lateinit var inputSearch : AutoCompleteTextView
    private var polyline: Polyline? = null
    private var destinationMarker: Marker? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var placesClient: PlacesClient
    private lateinit var autoCompleteAdapter: ArrayAdapter<String>
    private val autoCompleteResults = mutableListOf<String>()

    @SuppressLint("SetTextI18n", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_peta)

        closeCard = findViewById(R.id.close_card)
        closeIcon = findViewById(R.id.close_icon)
        closeIcon.setOnClickListener {
            closeCard.visibility = View.GONE
        }
        // Initialize Places API
        Places.initialize(applicationContext, getString(R.string.Map_Api_key))
        placesClient = Places.createClient(this)
        autoCompleteTextView = findViewById(R.id.search)
        autoCompleteAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, autoCompleteResults)
        autoCompleteTextView.setAdapter(autoCompleteAdapter)
        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && s.length >= 3) {
                    fetchAutoCompleteSuggestions(s.toString())
                }
            }
        })


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
        inputSearch = findViewById(R.id.search)
        inputSearch.setOnClickListener{
            searchDestination()
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

    fun fetchUserLocation() {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val userLatLng = LatLng(location.latitude, location.longitude)
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        addresses?.get(0)?.getAddressLine(0)
                        previousLocation = location
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
                                                    .title("$displayName")
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
        fetchUserLocation()

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
    @SuppressLint("SetTextI18n")
    fun searchDestination() {
        val locationName = inputSearch.text.toString()
        closeCard.visibility = View.GONE

        val geocoder = Geocoder(this)
        try {
            val addresses = geocoder.getFromLocationName(locationName, 1)
            if (!addresses.isNullOrEmpty()) {
                val destinationLatLng = addresses[0]?.let { LatLng(it.latitude, it.longitude) }
                if (destinationLatLng != null) {
                    destinationMarker?.remove()

                    destinationMarker = mMap.addMarker(
                        MarkerOptions()
                            .position(destinationLatLng).title(locationName)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                    )
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLng, 10f))

                    findViewById<TextView>(R.id.destinasi_tujuan).text = "Tempat Tujuan: $locationName"

                    inputSearch.setText("")
                    if (previousLocation != null) {
                        removePolyline()
                        getDirections(previousLocation!!.latitude, previousLocation!!.longitude, destinationLatLng.latitude, destinationLatLng.longitude)
                    }
                }
            } else {
                removePolyline()
                Log.e("MainActivity", "Location not found")
            }
        } catch (e: IOException) {
            Log.e("MainActivity", "Geocoder failed: ${e.message}")
            Toast.makeText(this, "Limit Pengunaan Request Google CLoud  Telah Habis / ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getDirections(startLat: Double, startLng: Double, endLat: Double, endLng: Double) {
        val apiKey = getString(R.string.Map_Api_key_premium)
        val origin = "$startLat,$startLng"
        val destination = "$endLat,$endLng"
        val mode = "driving"
        val url = "https://maps.googleapis.com/maps/api/directions/json?origin=$origin&destination=$destination&mode=$mode&key=$apiKey"
        Log.d("MainActivity", "URL: $url")

        Thread {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                val data = connection.inputStream.bufferedReader().use(BufferedReader::readText)
                val json = JSONObject(data)
                val routes = json.getJSONArray("routes")

                Log.d("MainActivity", "Response: $json")

                if (routes.length() > 0) {
                    val legs = routes.getJSONObject(0).getJSONArray("legs")
                    if (legs.length() > 0) {
                        val distance = legs.getJSONObject(0).getJSONObject("distance").getString("text")
                        val duration = legs.getJSONObject(0).getJSONObject("duration").getString("text")
                        runOnUiThread {
                            findViewById<TextView>(R.id.waktujarak).text = "Jarak: $distance, Waktu: $duration"
                        }
                    }

                    val points = routes.getJSONObject(0).getJSONObject("overview_polyline").getString("points")
                    Log.d("MainActivity", "Polyline Points: $points")

                    val line = PolylineOptions()
                        .addAll(PolyUtil.decode(points))
                        .width(12f)
                        .color(Color.BLUE)

                    runOnUiThread {
                        polyline?.remove()
                        polyline = mMap.addPolyline(line)
                    }
                } else {
                    Log.e("MainActivity", "No routes found")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error getting directions: ${e.message}")
            }
        }.start()
    }

    private fun fetchAutoCompleteSuggestions(query: String) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setCountries("ID")
            .build()

        placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
            autoCompleteResults.clear()
            for (prediction: AutocompletePrediction in response.autocompletePredictions) {
                autoCompleteResults.add(prediction.getFullText(null).toString())
            }
            autoCompleteAdapter.notifyDataSetChanged()
        }.addOnFailureListener { exception ->
            if (exception is ApiException) {
                Log.e("PetaActivity", "Place not found: " + exception.statusCode)
            }
        }
    }

    private fun removePolyline() {
        polyline?.remove()
        polyline = null
    }
}
