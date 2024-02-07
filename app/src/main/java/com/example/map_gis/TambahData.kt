package com.example.map_gis

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.FirebaseDatabase

class TambahData : AppCompatActivity() {

     lateinit var namaJalan : EditText
     lateinit var longitude : EditText
     lateinit var latitude : EditText
     lateinit var simpan : Button
     lateinit var kembali : Button
    private val database = FirebaseDatabase.getInstance("https://dbkecelakaan-default-rtdb.firebaseio.com")
    private val ref = database.getReference("data_kecelakaan")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_data)
        namaJalan = findViewById(R.id.namaJalan)
        longitude = findViewById(R.id.Longitude)
        latitude = findViewById(R.id.Latitude)
        simpan =  findViewById(R.id.simpan)
        kembali = findViewById(R.id.kembali)
        kembali.setOnClickListener{
            backToMaps()
        }
        simpan.setOnClickListener{
            val jalan = namaJalan.text.toString()
            val long = longitude.text.toString().toDouble()
            val lat = latitude.text.toString().toDouble()
            val kcl =  DataKecelakaan(jalan, long, lat)
            try{
                val newRef = ref.push()
                newRef.setValue(kcl)
                Toast.makeText(this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show()
            }catch (e: Exception) {
                Log.e("kesalahan", "Terjadi kesalahan: ${e.message}", e)
                Toast.makeText(this, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            namaJalan.setText("")
            longitude.setText("")
            latitude.setText("")
        }

    }
    private fun backToMaps (){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}