// AppNavigator.kt 수정
package com.deu3.parking.util

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.*
import com.deu3.parking.CameraActivity
import com.deu3.parking.composable.MainScreen
import com.deu3.parking.composable.ReportScreen
import com.deu3.parking.composable.submitReport
import com.deu3.parking.model.Detect1Response
import com.deu3.parking.model.Detect2Response
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.activity.compose.BackHandler

@Composable
fun AppNavigator() {
    val navController = rememberNavController()
    val context = LocalContext.current

    var firstImagePath by remember { mutableStateOf<String?>(null) }
    var firstLatLng by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var detect1Result by remember { mutableStateOf<Detect1Response?>(null) }
    var secondImagePath by remember { mutableStateOf<String?>(null) }
    var secondLatLng by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var detect2Result by remember { mutableStateOf<Detect2Response?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var yoloImages by remember { mutableStateOf<Pair<String?, String?>?>(null) }
    var showAlreadyReportedDialog by remember { mutableStateOf(false) }
    var firstImageValid by rememberSaveable { mutableStateOf(false) }
    var showCarNotVisibleDialog by remember { mutableStateOf(false) }
    var showNormalDialog by remember { mutableStateOf(false) }
    var showReportDialog  by remember { mutableStateOf(false) }
    var manualSelectedType by remember { mutableStateOf<Int?>(null) } // 수동선택 상태 추가

    // 홈으로 돌아가면 기존 기록들 모두 초기화 하는 함수
    fun resetState() {
        firstImagePath = null
        firstLatLng = null
        detect1Result = null
        secondImagePath = null
        secondLatLng = null
        detect2Result = null
        errorMessage = null
        yoloImages = null
        showAlreadyReportedDialog = false
        firstImageValid = false
        manualSelectedType = null
        showCarNotVisibleDialog = false
        showNormalDialog = false
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val imagePath = data?.getStringExtra("imagePath") ?: return@rememberLauncherForActivityResult
            val lat = data.getDoubleExtra("lat", 0.0)
            val lng = data.getDoubleExtra("lng", 0.0)
            val isSecond = data.getBooleanExtra("isSecond", false)

            if (!isSecond) {
                firstImagePath = imagePath
                firstLatLng = lat to lng

                sendDetect1(imagePath, lat, lng) { result1, error ->
                    if (result1 != null) {
                        detect1Result = result1

                        if (result1.is_violation) {
                            showAlreadyReportedDialog = true
                            return@sendDetect1
                        }

                        if (result1.car_number == null) {
                            showCarNotVisibleDialog = true
                            firstImageValid = false
                        } else if (result1.violation_type.firstOrNull() == -1 || result1.violation_type.firstOrNull() == 0) {
                            showNormalDialog = true
                            firstImageValid = false
                        } else {
                            //errorMessage = null
                            firstImageValid = true
                        }
                    } else {
                        errorMessage = error ?: "서버 오류"
                        firstImageValid = false
                    }

                    if (navController.currentDestination?.route != "report") {
                        navController.navigate("report")
                    }
                }
            } else {
                secondImagePath = imagePath
                secondLatLng = lat to lng

                sendDetect2(
                    firstLatLng?.first ?: 0.0,
                    firstLatLng?.second ?: 0.0,
                    lat,
                    lng,
                    detect1Result?.car_number ?: "",
                    imagePath
                ) { result2, error ->
                    if (result2 != null) {
                        detect2Result = result2
                        yoloImages = Pair(detect1Result?.yolo_result_image, result2.yolo_result_image)
                    } else {
                        errorMessage = error ?: "서버 오류"
                        resetState()
                    }

                    if (navController.currentDestination?.route != "report") {
                        navController.navigate("report")
                    }
                }
            }
        }
    }

    val finalViolationType = detect1Result?.violation_type?.let { violationList ->
        if (manualSelectedType != null) {
            listOf(manualSelectedType!!) // 수동으로 고른 값이 있으면 그것만 넘겨줌
        } else {
            violationList // 원래 서버가 준 리스트 그대로 넘김 (ex. [5, 7])
        }
    }

    NavHost(navController, startDestination = "main") {
        composable("main") {
            MainScreen(onStartCamera = {
                navController.navigate("report")
            })
        }

        composable("report") {
            BackHandler {
                resetState()
                navController.navigate("main") {
                    popUpTo("main") { inclusive = true }
                }
            }
            ReportScreen(
                firstImagePath = firstImagePath,
                secondImagePath = secondImagePath,
                violationType = finalViolationType,
                carNumber = detect1Result?.car_number,
                yoloImages = yoloImages,
                detect2Result = detect2Result,
                errorMessage = errorMessage,
                firstImageValid = firstImageValid,
                onFirstImageClick = {
                    val intent = Intent(context, CameraActivity::class.java).apply {
                        putExtra("isSecond", false)
                    }
                    cameraLauncher.launch(intent)
                },
                onSecondImageClick = {
                    val intent = Intent(context, CameraActivity::class.java).apply {
                        putExtra("isSecond", true)
                    }
                    cameraLauncher.launch(intent)
                },
                onManualTypeToggle = { selected ->
                    manualSelectedType = selected
                },
                onSubmit = { selectedViolationType ->
                    submitReport(
                        carNumber = detect1Result?.car_number ?: return@ReportScreen,
                        latitude = firstLatLng?.first ?: return@ReportScreen,
                        longitude = firstLatLng?.second ?: return@ReportScreen,
                        violationType = selectedViolationType,
                        onSuccess = {
                            showReportDialog = true
                        },
                        onError = {
                            errorMessage = it
                            navController.navigate("report")
                        }
                    )
                },
                onHome = {
                    resetState()
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
    }

    if (showAlreadyReportedDialog) {
        AlertDialog(
            onDismissRequest = { showAlreadyReportedDialog = false },
            title = { Text("알림") },
            text = { Text("이미 신고된 민원입니다") },
            confirmButton = {
                TextButton(onClick = {
                    showAlreadyReportedDialog = false
                    resetState()
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = true }
                    }
                }) {
                    Text("홈")
                }
            }
        )
    }

    // 자동차 안 보일 때 팝업
    if (showCarNotVisibleDialog) {
        AlertDialog(
            onDismissRequest = { showCarNotVisibleDialog = false },
            title = { Text("알림") },
            text = { Text("자동차가 잘 보이게 다시 찍어주세요") },
            confirmButton = {
                TextButton(onClick = {
                    showCarNotVisibleDialog = false
                    resetState()
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = true }
                    }
                }) {
                    Text("확인")
                }
            }
        )
    }

    // 정상 차량일 때 팝업
    if (showNormalDialog) {
        AlertDialog(
            onDismissRequest = { showNormalDialog = false },
            title = { Text("알림") },
            text = { Text("정상입니다") },
            confirmButton = {
                TextButton(onClick = {
                    showNormalDialog = false
                    resetState()
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = true }
                    }
                }) {
                    Text("확인")
                }
            }
        )
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { /* 다이얼로그 밖 터치 시 닫히지 않도록 무시 */ },
            title = { Text("신고 완료") },
            text = { Text("신고가 정상적으로 접수되었습니다.\n감사합니다!") },
            confirmButton = {
                TextButton(onClick = {
                    showReportDialog = false
                    resetState()
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = true }
                    }
                }) {
                    Text("확인")
                }
            }
        )
    }
}

