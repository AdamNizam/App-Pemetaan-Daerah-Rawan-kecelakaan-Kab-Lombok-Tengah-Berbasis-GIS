package com.example.map_gis

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

class PetaActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_peta)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.MapPeta) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    val kecamatanName = intent.getStringExtra("kecamatanName")
    val lokasiList = intent.getStringArrayListExtra("lokasiList")
    val jumlahLakaList = intent.getIntegerArrayListExtra("jumlahLakaList")
    val latitudeList = intent.getDoubleArrayExtra("latitudeList")
    val longitudeList = intent.getDoubleArrayExtra("longitudeList")

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (lokasiList != null && jumlahLakaList != null && latitudeList != null && longitudeList != null) {
            if (lokasiList.size == jumlahLakaList.size && jumlahLakaList.size == latitudeList.size && latitudeList.size == longitudeList.size) {
                for (i in lokasiList.indices) {
                    val latLng = LatLng(latitudeList[i], longitudeList[i])
                    val markerOptions = MarkerOptions()
                        .position(latLng)
                        .title(lokasiList[i])
                        .snippet("Jumlah Laka: ${jumlahLakaList[i]}")

                    mMap.addMarker(markerOptions)
                    if (i == 0) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
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
                    radiusAnimator.duration = 1000 // durasi 1 detik

                    radiusAnimator.addUpdateListener { animation ->
                        val animatedFraction = animation.animatedFraction
                        circle.radius =
                            (100 + 50 * animatedFraction).toDouble()
                    }

                    val colorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), Color.argb(70, 50, 50, 150), Color.argb(70, 150, 50, 50))
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
    }
}