package org.nsh07.pomodoro

import android.app.Application
import org.nsh07.pomodoro.data.AppContainer
import org.nsh07.pomodoro.data.AppDataContainer

class ZingApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}
