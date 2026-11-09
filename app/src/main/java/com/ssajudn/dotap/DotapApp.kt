package com.ssajudn.dotap

import android.app.Application
import com.ssajudn.dotap.di.AppContainer

class DotapApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
