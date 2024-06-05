package com.example.map_gis

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotificationActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter
    private lateinit var databaseReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = NotificationAdapter(ArrayList())
        recyclerView.adapter = adapter
        databaseReference = FirebaseDatabase.getInstance().getReference("notifications")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val notifications = mutableListOf<DataNotification>()
                for (ds in dataSnapshot.children) {
                    val title = ds.child("title").getValue(String::class.java) ?: ""
                    val message = ds.child("message").getValue(String::class.java) ?: ""
                    val timestamp = ds.child("timestamp").getValue(Long::class.java) ?: 0
                    notifications.add(DataNotification(title, message,timestamp))
                }
                adapter.setNotifications(notifications)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("NotificationActivity", "Error: ${databaseError.message}")
            }
        })
    }
}