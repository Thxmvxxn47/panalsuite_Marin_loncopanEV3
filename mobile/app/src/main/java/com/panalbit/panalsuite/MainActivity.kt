package com.panalbit.panalsuite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.panalbit.panalsuite.ui.navigation.PanalSuiteRoot

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as PanalSuiteApp).container
        setContent { PanalSuiteRoot(container) }
    }
}
