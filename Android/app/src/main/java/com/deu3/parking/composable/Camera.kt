package com.deu3.parking.composable

import android.util.Log
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.deu3.parking.R
import com.deu3.parking.util.Guide
import com.google.android.gms.location.LocationServices
import java.io.File

@Composable
fun CameraScreen(
    isSecond: Boolean,
    onPictureTaken: (String, Double, Double) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val guideList = listOf(
        Guide("소화전과 차량이 함께 나오도록 촬영해 주세요", "소화전", R.drawable.hydrant_guide),
        Guide("차량과 횡단보도가 잘 나오게 촬영해 주세요", "횡단보도", R.drawable.crosswalk_guide),
        Guide("차량과 인도가 잘 나오게 촬영해 주세요", "인도(보도)", R.drawable.sidewalk_guide),
        Guide("차량과 차선이 함께 나오도록 촬영해 주세요", "어린이보호구역/교차로 모퉁이/버스정류장", R.drawable.school_cross_bus_guide)
    )

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var showGuide by remember { mutableStateOf(!isSecond) }
    var guideIndex by remember { mutableIntStateOf(0) }
    var flashEnabled by remember { mutableStateOf(false) }
    var capturing by remember { mutableStateOf(false) }
    var camera: Camera? by remember { mutableStateOf(null) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val boxWidth = maxWidth * 0.6f
        val boxHeight = maxHeight * 0.6f
        val verticalMargin = (maxHeight - boxHeight) / 2
        val horizontalMargin = (maxWidth - boxWidth) / 2

        Box(modifier = Modifier.fillMaxSize()) {

            // Camera Preview
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().apply {
                            setSurfaceProvider(previewView.surfaceProvider)
                        }
                        imageCapture = ImageCapture.Builder().build()
                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        try {
                            cameraProvider.unbindAll()
                            camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner, cameraSelector, preview, imageCapture
                            )
                            camera?.cameraControl?.enableTorch(flashEnabled)
                        } catch (e: Exception) {
                            Log.e("CameraX", "카메라 바인딩 실패", e)
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                }
            )

            if (!showGuide) {
                Box(modifier = Modifier.fillMaxWidth().height(verticalMargin).align(Alignment.TopCenter).background(Color(0x80000000)))
                Box(modifier = Modifier.fillMaxWidth().height(verticalMargin).align(Alignment.BottomCenter).background(Color(0x80000000)))
                Box(modifier = Modifier.width(horizontalMargin).height(boxHeight).align(Alignment.CenterStart).background(Color(0x80000000)))
                Box(modifier = Modifier.width(horizontalMargin).height(boxHeight).align(Alignment.CenterEnd).background(Color(0x80000000)))
                Box(modifier = Modifier.size(boxWidth, boxHeight).align(Alignment.Center).background(Color.Transparent).border(3.dp, Color.Red))
                Text(
                    text = "박스 안에 차량과 차량 번호가 식별 가능하게 촬영해주세요",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 32.dp)
                )
            }

            if (showGuide) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x80000000))
                        .padding(top = 48.dp, bottom = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = guideList[guideIndex].title,
                        color = Color.White,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    Card(
                        modifier = Modifier.size(300.dp, 200.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box {
                            Image(
                                painter = painterResource(id = guideList[guideIndex].imageRes),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { showGuide = false },
                                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(40.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "닫기", tint = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    val maxKeywordWidth = 420.dp

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (guideIndex > 0) {
                            IconButton(
                                onClick = { guideIndex-- },
                                modifier = Modifier.size(64.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "이전",
                                    tint = Color.White,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(56.dp))
                        }

                        Box(
                            modifier = Modifier
                                .width(maxKeywordWidth)
                                .height(72.dp)
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = guideList[guideIndex].keyword,
                                color = Color.White,
                                fontSize = 18.sp,
                                lineHeight = 22.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Visible,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }



                        if (guideIndex < guideList.lastIndex) {
                            IconButton(
                                onClick = { guideIndex++ },
                                modifier = Modifier.size(64.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "다음",
                                    tint = Color.White,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(56.dp))
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(16.dp)
                    .align(Alignment.CenterEnd),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(onClick = { onBack() }) {
                    Icon(Icons.Default.Close, contentDescription = "뒤로가기", tint = Color.White)
                }
                Spacer(modifier = Modifier.height(24.dp))
                IconButton(
                    onClick = {
                        if (capturing) return@IconButton
                        capturing = true
                        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            val lat = location?.latitude ?: 0.0
                            val lng = location?.longitude ?: 0.0
                            val file = File(context.externalCacheDir, "${System.currentTimeMillis()}.jpg")
                            imageCapture?.let { capture ->
                                val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
                                capture.takePicture(
                                    outputOptions,
                                    ContextCompat.getMainExecutor(context),
                                    object : ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                            capturing = false
                                            onPictureTaken(file.absolutePath, lat, lng)
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            capturing = false
                                            Toast.makeText(context, "사진 저장 실패", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                        }
                    },
                    enabled = !showGuide && !capturing,
                    modifier = Modifier.size(80.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "촬영", tint = Color.White, modifier = Modifier.size(64.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))
                IconButton(onClick = {
                    flashEnabled = !flashEnabled
                    camera?.cameraControl?.enableTorch(flashEnabled)
                }) {
                    Icon(
                        imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "플래시",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
