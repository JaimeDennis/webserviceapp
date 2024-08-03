package com.example.webserviceapp.Objetos

import android.content.Context
import android.provider.Settings

class Device {
    companion object {
        fun getSecureId(context: Context): String? {
            return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        }
    }
}