package com.maturia.bookaccounting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.maturia.bookaccounting.ui.LibraryScreen
import com.maturia.bookaccounting.ui.theme.КнигоУчетTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            КнигоУчетTheme { LibraryScreen() }
        }
    }
}