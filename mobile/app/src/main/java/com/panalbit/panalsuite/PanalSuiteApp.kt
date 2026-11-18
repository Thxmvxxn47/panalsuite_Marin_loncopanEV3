package com.panalbit.panalsuite

import android.app.Application
import com.panalbit.panalsuite.di.AppContainer

class PanalSuiteApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
