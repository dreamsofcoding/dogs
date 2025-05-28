package io.dreamsofcoding.dogs

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import okhttp3.Cache
import okhttp3.OkHttpClient
import timber.log.Timber
import java.io.File

@HiltAndroidApp
class DogsApp : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.Forest.plant(Timber.DebugTree())
        }
        Timber.Forest.d("Application started")

    }
}
