package com.svantheemsche.appetijt

import android.app.Application
import com.svantheemsche.appetijt.BuildConfig
import timber.log.Timber

class AppetijtApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
