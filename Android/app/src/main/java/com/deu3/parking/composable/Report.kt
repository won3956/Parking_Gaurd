package com.deu3.parking.composable

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deu3.parking.R
import com.deu3.parking.model.Detect2Response
import com.deu3.parking.ui.theme.ParkingGuardTheme
import com.deu3.parking.ui.theme.*
import com.deu3.parking.util.ApiClient
import com.deu3.parking.util.ApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun ReportScreen(
    firstImagePath: String?,
    secondImagePath: String?,
    violationType: List<Int>?,
    carNumber: String?,
    yoloImages: Pair<String?, String?>?,
    detect2Result: Detect2Response?,
    errorMessage: String?,
    firstImageValid: Boolean,
    onFirstImageClick: () -> Unit,
    onSecondImageClick: () -> Unit,
    onSubmit: (Int) -> Unit,
    onHome: () -> Unit,
    onManualTypeToggle: (Int) -> Unit // 추가됨
) {
    var countdown by rememberSaveable { mutableStateOf(10) }
    val shouldStartCountdown = firstImagePath != null && errorMessage == null && secondImagePath == null
    val secondCardEnabled = countdown == 0 && shouldStartCountdown
    var selectedManualType by remember { mutableStateOf<Int?>(null) }
    var firstViolationType by rememberSaveable { mutableStateOf<List<Int>?>(null) }

    LaunchedEffect(violationType, firstImagePath) {
        if (firstViolationType == null && violationType != null && firstImagePath != null) {
            firstViolationType = violationType
        }
    }

    LaunchedEffect(shouldStartCountdown, countdown) {
        if (shouldStartCountdown && countdown > 0) {
            kotlinx.coroutines.delay(1000)
            countdown--
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White_Color)
            .padding(16.dp)
            .statusBarsPadding(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PhotoSlot(
                imageBase64 = yoloImages?.first ?: firstImagePath,
                label = "1차 사진을 촬영해주세요",
                onClick = onFirstImageClick,
                errorMessage = if (errorMessage != null && secondImagePath == null) errorMessage else null,
                enabled = secondImagePath == null && firstImagePath == null,
                checked = firstImageValid && secondImagePath == null,

            )

            if (!secondCardEnabled && shouldStartCountdown) {
                CountdownBox(countdown)
            } else {
                PhotoSlot(
                    imageBase64 = yoloImages?.second ?: secondImagePath,
                    label = "2차 사진을 촬영해주세요",
                    onClick = onSecondImageClick,
                    errorMessage = if (errorMessage != null && secondImagePath != null) errorMessage else null,
                    enabled = secondImagePath == null && countdown == 0
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            if (detect2Result?.car_number_match == false) {
                Text("차량 번호 미일치", color = MaterialTheme.colorScheme.error)
            }
            if (detect2Result?.within_5m == false) {
                Text("첫 번째 사진과 5미터 이상 차이남", color = MaterialTheme.colorScheme.error)
            }

            RowItem(label = "촬영날짜:", value = if (!firstImagePath.isNullOrEmpty()) getTodayDate() else "")

            CarNumberRow(carNumber)
            if (firstViolationType?.containsAll(listOf(5, 7)) == true) {
                Text("신고유형 선택:", fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    RadioButtonWithLabel(
                        selected = selectedManualType == 5,
                        onClick = {
                            selectedManualType = 5
                            onManualTypeToggle(5)
                        },
                        label = "교차로 모퉁이"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    RadioButtonWithLabel(
                        selected = selectedManualType == 7,
                        onClick = {
                            selectedManualType = 7
                            onManualTypeToggle(7)
                        },
                        label = "일반 불법주정차"
                    )
                }
            } else if (firstViolationType?.isNotEmpty() == true) {
                val typeLabel = when (firstViolationType!!.first()) {
                    1 -> "어린이 보호구역"
                    2 -> "소화전"
                    3 -> "횡단보도"
                    4 -> "인도"
                    6 -> "버스 정류장"
                    else -> "차량 탐지를 못했어요"
                }
                Text("신고유형: $typeLabel", fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val selectedType = if (firstViolationType?.containsAll(listOf(5, 7)) == true) {
            selectedManualType
        } else {
            firstViolationType?.firstOrNull()
        }

        Button(
            onClick = {
                if (selectedType != null) {
                    onSubmit(selectedType)
                }
            },
            enabled = (detect2Result?.car_number_match == true && detect2Result.within_5m == true && selectedType != null),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("신고 접수", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun RadioButtonWithLabel(selected: Boolean, onClick: () -> Unit, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onClick() }
    ) {
        RadioButton(
            selected = selected,
            onClick = null, // null로 설정하여 라벨 클릭 시에도 반응하도록
            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
        )
        Text(text = label, fontSize = 14.sp)
    }
}

@Composable
private fun PhotoSlot(
    imageBase64: String?,
    label: String,
    onClick: () -> Unit,
    errorMessage: String? = null,
    enabled: Boolean = true,
    checked: Boolean = false
) {
    val backgroundColor = if (enabled) Term_Blue_Color else Gray.copy(alpha = 0.2f)
    val borderColor = if (enabled) Blue_Color else Gray

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable(enabled = enabled) { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val bitmap = remember(imageBase64) {
                try {
                    val pureBase64 = imageBase64?.substringAfter("base64,", imageBase64)
                    val imageBytes = Base64.decode(pureBase64, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)?.asImageBitmap()
                } catch (e: Exception) {
                    null
                }
            }

            if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                ) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

            when {
                bitmap != null -> {
                    Image(
                        bitmap = bitmap,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                enabled -> {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "추가",
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.Center),
                        tint = Black_Color
                    )
                }
            }

            if (checked) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_check_circle), // 커스텀 아이콘 리소스
                    contentDescription = "완료됨",
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                )
            }

            if (enabled && (imageBase64 == null || errorMessage != null)) {
                Text(
                    text = label,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun CountdownBox(countdown: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Light_Blue_Color)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("2차 촬영까지 대기 중",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "${countdown / 60}분 ${countdown % 60}초",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun CarNumberRow(carNumber: String?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Text("차량번호:", fontSize = 14.sp, modifier = Modifier.width(80.dp))
        Text(carNumber ?: "", fontSize = 14.sp)
    }
}

@Composable
private fun RowItem(label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Text(label, fontSize = 14.sp, modifier = Modifier.width(80.dp))
        Text(value, fontSize = 14.sp)
    }
}

private fun getTodayDate(): String {
    val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
    return dateFormat.format(java.util.Date())
}


fun submitReport(
    carNumber: String,
    latitude: Double,
    longitude: Double,
    violationType: Int,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val api = ApiClient.retrofit.create(ApiService::class.java)

    val capturedAtBody = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .toString()
        .toRequestBody("text/plain".toMediaTypeOrNull())

    val carNumberBody = carNumber.toRequestBody("text/plain".toMediaTypeOrNull())
    val latitudeBody = latitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
    val longitudeBody = longitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
    val violationTypeBody = violationType.toString().toRequestBody("text/plain".toMediaTypeOrNull())

    api.report(
        carNumber = carNumberBody,
        capturedAt = capturedAtBody,
        latitude = latitudeBody,
        longitude = longitudeBody,
        violationType = violationTypeBody
    ).enqueue(object : Callback<Void> {
        override fun onResponse(call: Call<Void>, response: Response<Void>) {
            if (response.isSuccessful) {
                onSuccess()
            } else {
                onError("서버 오류: ${response.code()}")
            }
        }

        override fun onFailure(call: Call<Void>, t: Throwable) {
            onError("네트워크 오류: ${t.message}")
        }
    })
}

// 미리보기
@Preview(showBackground = true)
@Composable
fun ReportScreenPreview() {
    ParkingGuardTheme {
        ReportScreen(
            firstImagePath = null,
            secondImagePath = null,
            violationType = listOf(1, 2),
            carNumber = "12가3456",
            yoloImages = null,
            detect2Result = null,
            errorMessage = null,
            firstImageValid = false,
            onFirstImageClick = {},
            onSecondImageClick = {},
            onSubmit = {},
            onHome = {},
            onManualTypeToggle = {} // Preview용 빈 콜백
        )
    }
}
