package com.example.tcgtrader
import android.app.Application
import com.google.firebase.FirebaseApp

class TCGTraderApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}