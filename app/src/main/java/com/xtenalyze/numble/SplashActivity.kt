package com.xtenalyze.numble

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.xtenalyze.numble.ui.theme.SumRushTheme

class SplashActivity : ComponentActivity() {
    private val TAG = "Numble"
    private var splashFinished = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Safety timeout to avoid ANR issues
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isFinishing && !splashFinished) {
                Log.d(TAG, "Safety timeout triggered for splash screen")
                navigateToMainActivity()
            }
        }, 5000) // 5 second safety timeout

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
            Log.e(TAG, "Error setting immersive mode: ${e.message}")
        }

        setContent {
            SumRushTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SumRushSplashScreen(
                        onSplashFinished = {
                            navigateToMainActivity()
                        }
                    )
                }
            }
        }
    }

    private fun navigateToMainActivity() {
        // Avoid multiple navigation attempts
        if (splashFinished) return

        splashFinished = true
        try {
            // Navigate to MainActivity when splash screen is done
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()

            // Apply the transition effect (with safe handling)
            try {
                @Suppress("DEPRECATION")
                overridePendingTransition(
                    R.anim.slide_in_up,
                    R.anim.fade_out_with_scale
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error applying transition: ${e.message}")
            }
        } catch (e: Exception) {
            // Fallback if transition fails
            Log.e(TAG, "Error navigating to MainActivity: ${e.message}")
            try {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } catch (e: Exception) {
                Log.e(TAG, "Critical error in navigation: ${e.message}")
                finish() // At minimum, finish this activity
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // If activity is paused before splash finishes, ensure it's navigated away
        if (!splashFinished) {
            navigateToMainActivity()
        }
    }
}