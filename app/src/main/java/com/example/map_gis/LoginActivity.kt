package com.example.map_gis

import android.app.Activity
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log.d
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth
    private lateinit var googleSignInClient : GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        findViewById<MaterialButton>(R.id.btnLoginWithGoogle).setOnClickListener{
            googleSignIn()
        }
        val popupDialog = Dialog(this)
        popupDialog.setContentView(R.layout.activity_main)

    }

    private fun googleSignIn() {
        val signInClient = googleSignInClient.signInIntent
        launcher.launch(signInClient)
    }
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        if(result.resultCode == Activity.RESULT_OK){
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            d("RESULT", task.result.toString())
            manageResults(task)
        }else{
            d("RESULT", result.resultCode.toString())
        }
    }

    private fun manageResults(task: Task<com.google.android.gms.auth.api.signin.GoogleSignInAccount>) {
        val account : com.google.android.gms.auth.api.signin.GoogleSignInAccount? = task.result
        d("ACCOUNT", account?.displayName.toString())
        if(account != null){
            val credential = GoogleAuthProvider.getCredential(account.idToken,null)
            auth.signInWithCredential(credential).addOnCompleteListener{
                if(task.isSuccessful){
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    createNotification(this, "Authentification", "Anda Melakukan Aktivitas Login ")
                    Toast.makeText(this,"Login With Google Succesfully", Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(this,task.exception.toString(),Toast.LENGTH_SHORT).show()
                }
            }
            d("CREDENTIAL", credential.toString())
        }
        else{
            Toast.makeText(this,task.exception.toString(),Toast.LENGTH_SHORT).show()
        }

    }
    fun createNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("channel_id", "Channel Name", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, "channel_id")
            .setSmallIcon(R.drawable.notification1)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSound(soundUri)
        notificationManager.notify(1, builder.build())
    }


}