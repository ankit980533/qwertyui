package com.xpensetrack.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.xpensetrack.data.api.ApiClient
import com.xpensetrack.data.api.ExpenseApi
import com.xpensetrack.data.model.ReportData
import com.xpensetrack.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(navController: NavController) {
    var selectedPeriod by remember { mutableStateOf("Monthly") }
    var data by remember { mutableStateOf<ReportData?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val now = LocalDate.now()

    LaunchedEffect(selectedPeriod) {
        scope.launch {
            try {
                error = null
                data = ApiClient.create<ExpenseApi>().getReport(selectedPeriod, now.year, now.monthValue)
            } catch (e: Exception) {
                error = e.message
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Reports & Analytics", fontWeight = FontWeight.Bold)
                        Text("Track your spending patterns", fontSize = 13.sp, color = White.copy(0.8f))
                    }
                },
                actions = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, null, tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Purple700, titleContentColor = White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            // Weekly / Monthly toggle
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(GrayBg)
                    .padding(4.dp)
            ) {
                listOf("Weekly", "Monthly").forEach { period ->
                    val selected = selectedPeriod == period
                    Button(
                        onClick = { selectedPeriod = period },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected) Green500 else Color.Transparent,
                            contentColor = if (selected) White else DarkText
                        ),
                        elevation = if (selected) ButtonDefaults.buttonElevation(4.dp) else ButtonDefaults.buttonElevation(0.dp)
                    ) { Text(period, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Spent / Saved cards
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Spent card
                Card(
                    Modifier.weight(1f),
                    RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚠️", fontSize = 14.sp)
                            Spacer(Modifier.width(4.dp))
                            Text("Spent", fontSize = 14.sp, color = GrayText)
                        }
                        Text(
                            "₹${(data?.totalSpent ?: 0.0).toInt()}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (selectedPeriod == "Monthly") "This Month" else "This Week",
                            fontSize = 12.sp,
                            color = GrayText
                        )
                    }
                }
                // Saved card
                Card(
                    Modifier.weight(1f),
                    RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✅", fontSize = 14.sp)
                            Spacer(Modifier.width(4.dp))
                            Text("Saved", fontSize = 14.sp, color = Green500)
                        }
                        Text(
                            "₹${(data?.totalSaved ?: 0.0).toInt()}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Green500
                        )
                        val changeSign = if ((data?.savedChangePercent ?: 0.0) >= 0) "+" else ""
                        Text(
                            "${changeSign}${(data?.savedChangePercent ?: 0.0).toInt()}% v/s last ${if (selectedPeriod == "Monthly") "month" else "week"}",
                            fontSize = 12.sp,
                            color = Green500
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Spending Trend chart
            val trendData = if (selectedPeriod == "Monthly") data?.monthlySpendingTrend else data?.weeklySpending
            Card(
                Modifier.fillMaxWidth(),
                RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        "📊 ${if (selectedPeriod == "Monthly") "Monthly Spending Trend" else "Weekly Spending"}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Spacer(Modifier.height(16.dp))

                    if (!trendData.isNullOrEmpty()) {
                        val maxVal = trendData.maxOfOrNull {
                            maxOf(it.spent, it.budget ?: 0.0)
                        }?.coerceAtLeast(1.0) ?: 1.0

                        // Bar chart
                        Row(
                            Modifier.fillMaxWidth().height(160.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            trendData.forEach { point ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        // Budget bar (purple) - only for monthly
                                        if (point.budget != null && point.budget > 0) {
                                            Box(
                                                Modifier
                                                    .width(12.dp)
                                                    .fillMaxHeight((point.budget / maxVal).toFloat().coerceIn(0f, 1f))
                                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                    .background(Purple700)
                                            )
                                            Spacer(Modifier.width(2.dp))
                                        }
                                        // Spent bar (gold/purple)
                                        Box(
                                            Modifier
                                                .width(12.dp)
                                                .fillMaxHeight((point.spent / maxVal).toFloat().coerceIn(0f, 1f))
                                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                .background(if (point.budget != null) Gold else Purple700)
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(point.label, fontSize = 10.sp, color = GrayText)
                                }
                            }
                        }

                        // Legend
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                            if (selectedPeriod == "Monthly") {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(10.dp).background(Purple700, RoundedCornerShape(2.dp)))
                                    Text(" Budget", fontSize = 12.sp, color = GrayText)
                                    Spacer(Modifier.width(16.dp))
                                    Box(Modifier.size(10.dp).background(Gold, RoundedCornerShape(2.dp)))
                                    Text(" Spent", fontSize = 12.sp, color = GrayText)
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(10.dp).background(Purple700, RoundedCornerShape(2.dp)))
                                    Text(" Spent", fontSize = 12.sp, color = GrayText)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Savings Trend (line chart)
            val savingsData = data?.savingsTrend
            if (!savingsData.isNullOrEmpty()) {
                Card(
                    Modifier.fillMaxWidth(),
                    RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("📈 Savings Trend", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Spacer(Modifier.height(16.dp))

                        val maxSaved = savingsData.maxOfOrNull { it.saved ?: 0.0 }?.coerceAtLeast(1.0) ?: 1.0

                        // Line chart
                        Canvas(Modifier.fillMaxWidth().height(140.dp)) {
                            val points = savingsData.mapIndexed { i, point ->
                                val x = if (savingsData.size > 1) (size.width * i / (savingsData.size - 1)) else size.width / 2
                                val y = size.height - (((point.saved ?: 0.0) / maxSaved) * size.height).toFloat()
                                Offset(x, y)
                            }

                            // Fill area
                            if (points.size >= 2) {
                                val fillPath = Path().apply {
                                    moveTo(points.first().x, size.height)
                                    points.forEach { lineTo(it.x, it.y) }
                                    lineTo(points.last().x, size.height)
                                    close()
                                }
                                drawPath(fillPath, Color(0xFFF3E5F5))

                                // Line
                                val linePath = Path().apply {
                                    moveTo(points.first().x, points.first().y)
                                    points.drop(1).forEach { lineTo(it.x, it.y) }
                                }
                                drawPath(linePath, Purple700, style = Stroke(width = 3f))

                                // Dots
                                points.forEach { point ->
                                    drawCircle(Purple700, radius = 5f, center = point)
                                    drawCircle(White, radius = 3f, center = point)
                                }
                            }
                        }

                        // X-axis labels
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            savingsData.forEach { Text(it.label, fontSize = 10.sp, color = GrayText) }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), Arrangement.Center) {
                            Text("--●-- Rupees", fontSize = 12.sp, color = Purple700)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Great Job card
            val savedPct = data?.savedChangePercent ?: 0.0
            if (savedPct > 0) {
                Card(
                    Modifier.fillMaxWidth(),
                    RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Green500)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("🎉 Great Job!", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text(
                            "You saved ${savedPct.toInt()}% more this month compared to last month. Keep it up!",
                            fontSize = 14.sp, color = GrayText
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Tip card
            Card(
                Modifier.fillMaxWidth(),
                RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("💡 Tip", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text(
                        "Your food expenses are the highest category. Try cooking at home more often.",
                        fontSize = 14.sp, color = GrayText
                    )
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}
