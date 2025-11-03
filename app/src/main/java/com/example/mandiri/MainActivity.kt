package com.example.mandiri

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.example.mandiri.data.NewsRepository
import com.example.mandiri.data.remote.NewsApiService
import com.example.mandiri.ui.NewsViewModel
import com.example.mandiri.ui.NewsViewModelFactory
import com.example.mandiri.ui.navigation.MandiriNavHost
import com.example.mandiri.ui.theme.MandiriTheme

class MainActivity : ComponentActivity() {

    private val viewModel: NewsViewModel by viewModels {
        NewsViewModelFactory(NewsRepository(NewsApiService.create()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MandiriTheme {
                val navController = rememberNavController()
                MandiriNavHost(
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }
    }
}