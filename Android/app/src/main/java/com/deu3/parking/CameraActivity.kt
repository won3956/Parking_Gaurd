package com.deu3.parking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.deu3.parking.composable.CameraScreen
import com.deu3.parking.ui.theme.ParkingGuardTheme

class CameraActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isSecond = intent.getBooleanExtra("isSecond", false)

        setContent {
            ParkingGuardTheme {
                CameraScreen(
                    isSecond = isSecond,
                    onPictureTaken = { imagePath, lat, lng ->
                        val resultIntent = intent.apply {
                            putExtra("imagePath", imagePath)
                            putExtra("lat", lat)
                            putExtra("lng", lng)
                        }
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    },
                    onBack = { finish() }
                )
            }
        }
    }
}
