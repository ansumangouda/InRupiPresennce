package com.inrupipresennce.uiScreen.Screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inrupipresennce.R

data class Teammate(val name: String, val role: String, val imageRes: Int)

@Composable
fun HomePage() {
    Scaffold(
        bottomBar = { HomeBottomNavBar() }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF6F6F6))
        ) {
           // item { HomePageHeader() }
            item {Spacer(modifier = Modifier.height(80.dp)) }
            item { PunchActions() }
            item { ActionButtons() }
          /*  item { TeammateSection(title = "Wish Them", teammates = getBirthdayMates()) }
            item { TeammateSection(title = "Off Today", teammates = getOffTodayMates()) }
            item { TeammateSection(title = "My Teammates", teammates = getMyTeammates(), showSeeMoreCount = "+16") }
            item { TeammateSection(title = "Early Bird for the day", teammates = getEarlyBirds()) }
            item { TeammateSection(title = "Early Bird Ranking", teammates = getEarlyBirdRanking()) }*/
        }
    }
}







@Composable
fun PunchActions() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        PunchInfo("10:00 AM", "Punch In")
        PunchInfo("1:00 PM", "Lunch Start")
        PunchInfo("1:45 PM", "Lunch End")
        PunchInfo("6:00 PM", "Punch Out")
    }
}

@Composable
fun PunchInfo(time: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // VVV FIX THE ICON HERE VVV
        Icon(
            imageVector = Icons.Default.Home, // Use a guaranteed existing icon
            contentDescription = label,
            tint = Color.Gray
        )
        // ^^^ FIX THE ICON HERE ^^^
        Spacer(modifier = Modifier.height(4.dp))
        Text(time, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(label, fontSize = 11.sp, color = Color.Gray)
    }
}


@Composable
fun ActionButtons() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = { /*TODO*/ },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB7DB4F)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Punch In", color = Color.Black)
        }
        Button(
            onClick = { /*TODO*/ },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9C80E)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Lunch Break", color = Color.Black)
        }
    }
}

/*@Composable
fun TeammateSectio(title: String, teammates: List<Teammate>, showSeeMoreCount: String? = null) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(teammates) { teammate ->
                TeammateItem(teammate)
            }
            if (showSeeMoreCount != null) {
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(64.dp)) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0xFFB7DB4F).copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(showSeeMoreCount, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("See More", fontSize = 12.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}*/

@Composable
fun TeammateIte(teammate: Teammate) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(64.dp)) {
        Image(
            painter = painterResource(id = teammate.imageRes),
            contentDescription = teammate.name,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(teammate.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        Text(teammate.role, fontSize = 11.sp, color = Color.Gray, maxLines = 1)
    }
}

// Dummy data functions
fun getBirthdayMate(): List<Teammate> = listOf(
    Teammate("Ipsitha De..", "Today", R.drawable.face_scan),
    Teammate("Komal Ma..", "12 Dec", R.drawable.face_scan),
    Teammate("Keerthan..", "13 Dec", R.drawable.face_scan),
    Teammate("Sharath R E", "15 Dec", R.drawable.face_scan)
)
fun getOffTodayMates(): List<Teammate> = listOf(
    Teammate("Anshuma..", "Today", R.drawable.face_scan),
    Teammate("Asma", "Today", R.drawable.face_scan),
    Teammate("Abina Gur..", "Today", R.drawable.face_scan)
)
fun getMyTeammates(): List<Teammate> = listOf(
    Teammate("Ipsitha De..", "Content C..", R.drawable.face_scan),
    Teammate("Komal Ma..", "CTO", R.drawable.face_scan),
    Teammate("Keerthan..", "Digital Ma..", R.drawable.face_scan),
    Teammate("Sharath R E", "Full Stack..", R.drawable.face_scan)
)
fun getEarlyBirds(): List<Teammate> = listOf(
    Teammate("Anshuma..", "First", R.drawable.face_scan),
    Teammate("Asma Banu", "Second", R.drawable.face_scan),
    Teammate("Abina Gur..", "Third", R.drawable.face_scan)
)
fun getEarlyBirdRanking(): List<Teammate> = listOf(
    Teammate("Anshuma..", "November", R.drawable.face_scan),
    Teammate("Asma Banu", "October", R.drawable.face_scan),
    Teammate("Abina Gur..", "September", R.drawable.face_scan)
)


@Composable
fun HomeBottomNavBar() {
    NavigationBar(
        containerColor = Color(0xFFB7DB4F),
        modifier = Modifier
            .padding(12.dp)
            .clip(RoundedCornerShape(20.dp))
    ) {
        val items = listOf("Home", "Attendance", "Calendar", "My Team")
        val icons = listOf(Icons.Default.Home, Icons.Default.Home, Icons.Default.Home, Icons.Default.Person)

        items.forEachIndexed { index, item ->
            val isSelected = item == "Home"
            NavigationBarItem(
                selected = isSelected,
                onClick = { /* Handle navigation */ },
                icon = { Icon(icons[index], contentDescription = item, tint = if (isSelected) Color.White else Color.Black.copy(0.6f)) },
                label = { Text(item, color = if (isSelected) Color.White else Color.Black.copy(0.6f)) },
                colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
            )
        }
    }
}


@Preview(showBackground = true, device = "id:pixel_4")
@Composable
fun HomePagePreview() {
    MaterialTheme {
        HomePage()
    }
}

/*

package com.inrupipresennce.uiScreen.Screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inrupipresennce.R

*/
/* =========================
   MAIN SCREEN
   ========================= *//*


@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun AttendanceHomeScreen() {

    val listState = rememberLazyListState()

    Box(modifier = Modifier.fillMaxSize()) {

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {

            // space for header + curve
            item { Spacer(Modifier.height(260.dp)) }

            item { CameraCircleSection() }
            item { TimeLineSection() }
            item { ActionButtonsSection() }
            item { WishThemSection() }
            item { OffTodaySection() }
            item { MyTeamSection() }
            item { Spacer(Modifier.height(100.dp)) }
        }



        */
/* GlassHeaderCard(
            modifier = Modifier.align(Alignment.TopCenter)
        )

        CurvedHeaderBackdropBlur(
            listState = listState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        HeaderContentOverlay(
            modifier = Modifier.align(Alignment.TopCenter)
        )
        AttendanceBottomBar(
            modifier = Modifier.align(Alignment.BottomCenter)
        )*//*

        HeaderBackdropBlur(
            listState = listState,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // 🔹 3. HEADER CONTENT (NO BLUR)
        HeaderContentOverlay(
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

*/
/* =========================
   SCROLL → BLUR
   ========================= *//*


@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun HeaderBackdropBlur(
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val blur = rememberScrollBlur(listState)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .graphicsLayer {
                if (blur > 0f) {
                    renderEffect =
                        android.graphics.RenderEffect
                            .createBlurEffect(
                                blur,
                                blur,
                                android.graphics.Shader.TileMode.CLAMP
                            )
                            .asComposeRenderEffect()
                } else {
                    renderEffect = null
                }
            }
            // ✅ REAL GLASS LOOK
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.18f),
                        Color.White.copy(alpha = 0.05f)
                    )
                )
            )
    )
}


@Composable
fun GlassHeaderCard(modifier: Modifier = Modifier) {

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(120.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Color(0xFFF5FAE8) // soft glass green
            )
    ) {

        // subtle inner gradient (glass feel)
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.6f),
                            Color.White.copy(alpha = 0.25f)
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            // profile
            Image(
                painter = painterResource(R.drawable.face_scan),
                contentDescription = null,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "12:00 pm",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    "Wednesday | Dec 10 2025",
                    fontSize = 15.sp,
                    color = Color.Black.copy(alpha = 0.7f)
                )
            }

            // bell
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        Color(0xFFE8F3C9),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Notifications, null)
            }
        }
    }
}

@Composable
fun rememberScrollBlur(listState: LazyListState): Float {
    val offset = listState.firstVisibleItemScrollOffset
    return (offset / 10f).coerceIn(0f, 35f)
}


*/
/* =========================
   CURVED SHAPE
   ========================= *//*


class BottomArcShape : Shape {


    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height - 90f)
            quadraticBezierTo(
                size.width / 2,
                size.height + 90f,
                0f,
                size.height - 90f
            )
            close()
        }
        return Outline.Generic(path)




    }
    }

*/
/* =========================
   BACKDROP BLUR
   ========================= *//*


@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun CurvedHeaderBackdropBlur(
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val blur = rememberScrollBlur(listState)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .graphicsLayer {
                clip = true
                shape = BottomArcShape()

                if (blur > 0f) {
                    renderEffect =
                        android.graphics.RenderEffect
                            .createBlurEffect(
                                blur,
                                blur,
                                android.graphics.Shader.TileMode.CLAMP
                            )
                            .asComposeRenderEffect()
                }
            }
            .background(Color.White.copy(alpha = blur / 120f))
    )
}

*/
/* =========================
   HEADER CONTENT (NO BLUR)
   ========================= *//*


@Composable
fun HeaderContentOverlay(modifier: Modifier = Modifier) {

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Image(
            painter = painterResource(R.drawable.face_scan),
            contentDescription = null,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "12:00 pm",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Wednesday | Dec 10 2025",
                fontSize = 13.sp,
                color = Color.Black.copy(alpha = 0.7f)
            )
        }

        Icon(
            Icons.Default.Notifications,
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )
    }
}


*/
/* =========================
   CAMERA CIRCLE
   ========================= *//*


@Composable
fun CameraCircleSection() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .border(4.dp, Color(0xFFB7DB4F), CircleShape)
                .clip(CircleShape)
                .background(Color.LightGray)
        )
    }
}

*/
/* =========================
   SECTIONS
   ========================= *//*


@Composable
fun TimeLineSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TimeItem("10:00 AM", "Punch In")
        TimeItem("1:00 PM", "Lunch Start")
        TimeItem("1:45 PM", "Lunch End")
        TimeItem("6:00 PM", "Punch Out")
    }
}

@Composable
fun TimeItem(time: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Search, null)
        Text(time, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 11.sp, color = Color.Gray)
    }
}

@Composable
fun ActionButtonsSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = {},
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7BA93B))
        ) {
            Text("Punch In")
        }

        Button(
            onClick = {},
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF4B400))
        ) {
            Text("Lunch Break")
        }
    }
}

@Composable
fun WishThemSection() {
    SectionTitle("Wish Them")
    PeopleRow()
}

@Composable
fun OffTodaySection() {
    SectionTitle("Off Today")
    PeopleRow()
}

@Composable
fun MyTeamSection() {
    SectionTitle("My Teammates")
    PeopleRow()
}

@Composable
fun PeopleRow() {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(5) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                )
                Text("Name", fontSize = 12.sp)
                Text("Role", fontSize = 11.sp, color = Color.Gray)
            }
            Spacer(Modifier.width(12.dp))
        }
    }
}

@Composable
fun AttendanceBottomBar(modifier: Modifier = Modifier) {
    NavigationBar(
        modifier = modifier,
        containerColor = Color(0xFFB7DB4F)
    ) {

        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = {
                Icon(Icons.Default.Home, contentDescription = "Home")
            },
            label = {
                Text("Home")
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = {
                Icon(Icons.Default.DateRange, contentDescription = "Attendance")
            },
            label = {
                Text("Attendance")
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = {
                Icon(Icons.Default.Face, contentDescription = "My Team")
            },
            label = {
                Text("My Team")
            }
        )
    }
}


@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(16.dp)
    )
}

*/
/* =========================
   PREVIEW
   ========================= *//*


@RequiresApi(Build.VERSION_CODES.S)
@Preview(showBackground = true, showSystemUi = true, device = Devices.PIXEL_7)
@Composable
fun AttendanceHomeScreenPreview() {
    MaterialTheme {
        AttendanceHomeScreen()
    }
}






*/
/*
package com.inrupipresennce.uiScreen.Screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inrupipresennce.R

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun AttendanceHomeScreen() {

    val listState = rememberLazyListState()

    Box(modifier = Modifier.fillMaxSize()) {



        // 🔹 MAIN CONTENT
        Scaffold(
            bottomBar = { AttendanceBottomBar() }
        ) { padding ->


            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color.White)
            ) {

                // Space for header
             //   item { Spacer(Modifier.height(0.dp)) }
                item { AttendanceHeader() }

                item { CameraCircleSection() }
                item { TimeLineSection() }
                item { ActionButtonsSection() }
                item { WishThemSection() }
                item { OffTodaySection() }
                item { MyTeamSection() }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
        // 🔹 BLUR BACKDROP (ONLY THIS BLURS SCROLL CONTENT)
        HeaderBackdropBlur(
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // 🔹 HEADER CONTENT (NEVER BLURRED)
        HeaderContentOverlay(
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // 🔹 GLASS HEADER OVERLAY

    }
}
@Composable
fun AttendanceHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .background(
                color = Color(0xFFB7DB4F),
                shape = RoundedCornerShape(bottomStart = 120.dp, bottomEnd = 120.dp)
            )
    ) {
       *//*

*/
/* Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        )
        {

            Image(
                painter = painterResource(id = R.drawable.face_scan
                ),
                contentDescription = null,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "12:00 pm",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Wednesday | Dec 10 2025",
                    fontSize = 14.sp
                )
            }

            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, CircleShape)
                    .padding(8.dp)
            )
        }*//*
*/
/*

    }
}
@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun HeaderBackdropBlur(modifier: Modifier = Modifier) {

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .graphicsLayer {
                clip = true
                renderEffect =
                    android.graphics.RenderEffect
                        .createBlurEffect(
                            90f,
                            90f,
                            android.graphics.Shader.TileMode.DECAL
                        )
                        .asComposeRenderEffect()
            }
            .background(Color.White.copy(alpha = 0.2f))
    )
}
@Composable
fun HeaderContentOverlay(modifier: Modifier = Modifier) {

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Image(
                painter = painterResource(id = R.drawable.face_scan),
                contentDescription = null,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "12:00 pm",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Wednesday | Dec 10 2025",
                    fontSize = 13.sp,
                    color = Color.Black.copy(alpha = 0.7f)
                )
            }

            Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}


@Composable
private fun HeaderContent() {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Image(
            painter = painterResource(id = R.drawable.face_scan),
            contentDescription = null,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "12:00 pm",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Wednesday | Dec 10 2025",
                fontSize = 13.sp,
                color = Color.Black.copy(alpha = 0.7f)
            )
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    Color.White.copy(alpha = 0.6f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Notifications, null)
        }
    }
}






@Composable
fun CameraCircleSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-90).dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .border(4.dp, Color(0xFFB7DB4F), CircleShape)
                .clip(CircleShape)
                .background(Color.LightGray)
        )
    }
}

@Composable
fun TimeLineSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TimeItem("10:00 AM", "Punch In")
        TimeItem("1:00 PM", "Lunch Start")
        TimeItem("1:45 PM", "Lunch End")
        TimeItem("6:00 PM", "Punch Out")
    }
}

@Composable
fun TimeItem(time: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Search, contentDescription = null)
        Text(time, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text(label, fontSize = 11.sp, color = Color.Gray)
    }
}
@Composable
fun ActionButtonsSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7BA93B)),
            modifier = Modifier.weight(1f)
        ) {
            Text("Punch In")
        }

        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF4B400)),
            modifier = Modifier.weight(1f)
        ) {
            Text("Lunch Break")
        }
    }
}

@Composable
fun WishThemSection() {
    SectionTitle("Wish Them")

    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp)) {
        items(5) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )
                Text("B’DAY", color = Color(0xFF7BA93B), fontSize = 12.sp)
                Text("Today", fontSize = 11.sp)
            }
            Spacer(Modifier.width(12.dp))
        }
    }
}
@Composable
fun OffTodaySection() {
    SectionTitle("Off Today")
    PeopleRow()
}

@Composable
fun MyTeamSection() {
    SectionTitle("My Teammates")
    PeopleRow()
}

@Composable
fun PeopleRow() {
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp)) {
        items(5) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                )
                Text("Name", fontSize = 12.sp)
                Text("Role", fontSize = 11.sp, color = Color.Gray)
            }
            Spacer(Modifier.width(12.dp))
        }
    }
}
@Composable
fun AttendanceBottomBar() {
    NavigationBar(containerColor = Color(0xFFB7DB4F)) {
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Default.DateRange, null) },
            label = { Text("Attendance") }
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Default.Face, null) },
            label = { Text("My Team") }
        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        color = Color.Black
    )
}

@RequiresApi(Build.VERSION_CODES.S)
@Preview(
    showBackground = true,
    showSystemUi = true,
    device = Devices.PIXEL_7
)
@Composable
fun AttendanceHomeScreenPreview() {
    MaterialTheme {
        AttendanceHomeScreen()
    }
}






*/

