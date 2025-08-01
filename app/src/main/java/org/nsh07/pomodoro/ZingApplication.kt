package org.nsh07.pomodoro

import android.app.Application
import org.nsh07.pomodoro.data.AppContainer
import org.nsh07.pomodoro.data.DefaultAppContainer

class ZingApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}