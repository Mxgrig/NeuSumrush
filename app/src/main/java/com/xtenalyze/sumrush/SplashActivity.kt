package com.xtenalyze.sumrush

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.xtenalyze.sumrush.ui.theme.SumRushTheme

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            // Safely handle immersive mode with null checks and try-catch
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                // For API 30+ (Android 11+)
                val controller = window.insetsController
                if (controller != null) {
                    controller.hide(android.view.WindowInsets.Type.statusBars())
                    controller.systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                // For older versions
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or
                        android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            }
        } catch (e: Exception) {
            // Catch any exceptions that might occur when setting UI flags
            // Just continue without immersive mode if there's an issue
        }

        setContent {
            SumRushTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SumRushSplashScreen (
                        onSplashFinished = {
                            try {
                                // Navigate to MainActivity when splash screen is done
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()

                                // Apply the transition effect (with safe handling)
                                @Suppress("DEPRECATION")
                                overridePendingTransition(
                                    R.anim.slide_in_up,
                                    R.anim.fade_out_with_scale
                                )
                            } catch (e: Exception) {
                                // Fallback if transition fails
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                        }
                    )
                }
            }
        }
    }
}