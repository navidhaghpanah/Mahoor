package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MahoorViewModel
import com.example.ui.screens.MahoorMainScreen
import com.example.ui.screens.IntroVideoPlayer
import com.example.ui.screens.SplashLogoScreen

enum class StartupState {
    VIDEO,
    LOGO_SCREEN,
    MAIN_APP
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    val viewModel: MahoorViewModel by viewModels {
      MahoorViewModel.Factory(application)
    }

    setContent {
      MyApplicationTheme {
        var startupState by remember { mutableStateOf(StartupState.VIDEO) }

        when (startupState) {
            StartupState.VIDEO -> {
                IntroVideoPlayer(
                    onVideoFinished = { startupState = StartupState.LOGO_SCREEN }
                )
            }
            StartupState.LOGO_SCREEN -> {
                SplashLogoScreen(
                    onTimeout = { startupState = StartupState.MAIN_APP }
                )
            }
            StartupState.MAIN_APP -> {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Insets are fully managed inside MahoorMainScreen Scaffold
                    MahoorMainScreen(viewModel)
                }
            }
        }
      }
    }
  }
}
