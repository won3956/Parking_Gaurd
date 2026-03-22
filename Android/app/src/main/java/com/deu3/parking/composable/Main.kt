package com.deu3.parking.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deu3.parking.R
import com.deu3.parking.ui.theme.ParkingGuardTheme

@Composable
fun MainScreen(onStartCamera: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFCF5E8))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 경고 아이콘
        Icon(
            painter = painterResource(id = R.drawable.warning_icon),
            contentDescription = "Warning Icon",
            modifier = Modifier
                .size(64.dp)
                .padding(bottom = 8.dp),
            tint = Color.Unspecified
        )

        // 안내 제목
        Text(
            text = "6대 불법 주정차 금지구역",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 26.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.ExtraBold
            ),
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 아이콘 배치
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconWithLabel("소화전", R.drawable.fire_hydrant, highlight = true)
                IconWithLabel("횡단보도", R.drawable.crosswalk, highlight = true)
                IconWithLabel("인도(보도)", R.drawable.sidewalk, highlight = true)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconWithLabel("어린이보호구역", R.drawable.school_zone, highlight = true)
                IconWithLabel("교차로 모퉁이", R.drawable.crossroads, highlight = true)
                IconWithLabel("버스정류장", R.drawable.bus_stop, highlight = true)
            }

            // 밑줄과 도로주정차선 텍스트 추가
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                color = Color.Gray,
                thickness = 2.dp
            )

            Text(
                text = "도로주정차선",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 26.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.ExtraBold
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 1.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconWithLabel("황색 실선", R.drawable.solid_yellow)
                IconWithLabel("황색 이중선", R.drawable.yellow_double_line)
                IconWithLabel("황색 점선", R.drawable.dotted_yellow_line)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 촬영 시작 버튼
        Button(
            onClick = onStartCamera,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2C3434),
                contentColor = Color.White
            )
        ) {
            Text(
                text = "신고하기",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun IconWithLabel(label: String, iconRes: Int, highlight: Boolean = false) {
    Column(
        modifier = Modifier
            .width(110.dp)
            .height(140.dp)
            .then(
                if (highlight) Modifier.background(Color.White, RoundedCornerShape(12.dp)) else Modifier
            )
            .padding(top = 4.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            modifier = Modifier.size(100.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    ParkingGuardTheme {
        MainScreen(onStartCamera = {})
    }
}
