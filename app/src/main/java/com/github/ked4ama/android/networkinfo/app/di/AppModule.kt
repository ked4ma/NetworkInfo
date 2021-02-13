package com.github.ked4ama.android.networkinfo.app.di

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.telephony.TelephonyManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideWifiManager(application: Application): WifiManager {
        return application.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    @Singleton
    @Provides
    fun provideConnectivityManager(application: Application): ConnectivityManager {
        return application.applicationContext
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    @Singleton
    @Provides
    fun provideTelephonyManager(application: Application): TelephonyManager {
        return application.applicationContext
            .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }
}