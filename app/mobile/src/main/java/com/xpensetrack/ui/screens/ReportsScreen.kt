package com.xpensetrack.ui.screens

import androidx.compose.foundation.background
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
    val scope = rememberCoroutineScope()
    val now = LocalDate.now()

    LaunchedEffect(selectedPeriod) {
        scope.launch {
            try { data = ApiClient.create<ExpenseApi>().getReport(selectedPeriod, now.year, now.monthValue) } catch (_: Exception) {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column { Text("Reports & Analytics", fontWeight = FontWeight.Bold); Text("Track your spending patterns", fontSize = 13.sp, color = GrayText) } },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.Close, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Purple700, titleContentColor = White, navigationIconContentColor = White)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)) {
            // Weekly / Monthly toggle
            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(GrayBg).padding(4.dp)) {
                listOf("Weekly", "Monthly").forEach { period ->
                    val selected = selectedPeriod == period
                    Button(
                        onClick = { selectedPeriod = period },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected) Green500 else GrayBg,
                            contentColor = if (selected) White else DarkText
                        )
                    ) { Text(period, fontWeight = FontWeight.Bold) }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Spent / Saved cards
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = White)) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚠️ Spent", fontSize = 14.sp, color = GrayText)
                        Text("₹${(data?.totalSpent ?: 0.0).toInt()}", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text(if (selectedPeriod == "Monthly") "This Month" else "This Week", fontSize = 12.sp, color = GrayText)
                    }
                }
                Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = White)) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("✅ Saved", fontSize = 14.sp, color = Green500)
                        Text("₹${(data?.totalSaved ?: 0.0).toInt()}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Green500)
                        Text("+${(data?.savedChangePercent ?: 0.0).toInt()}% v/s last ${if (selectedPeriod == "Monthly") "month" else "week"}",
                            fontSize = 12.sp, color = Green500)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Monthly Spending Trend
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📊 ${if (selectedPeriod == "Monthly") "Monthly" else "Weekly"} Spending Trend", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    val trend = if (selectedPeriod == "Monthly") data?.monthlySpendingTrend else data?.weeklySpending
                    val maxVal = trend?.maxOfOrNull { it.spent } ?: 1.0
                    trend?.forEach { point ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(point.label, fontSize = 12.sp, modifier = Modifier.width(50.dp))
                            Box(modifier = Modifier.weight(1f).height(16.dp).clip(RoundedCornerShape(4.dp)).background(GrayBg)) {
                                Box(modifier = Modifier.fillMaxHeight().fillMaxWidth((point.spent / maxVal).toFloat()).background(Purple700, RoundedCornerShape(4.dp)))
                            }
                            Text("₹${point.spent.toInt()}", fontSize = 11.sp, modifier = Modifier.width(50.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Savings Trend
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📈 Savings Trend", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    val savings = data?.savingsTrend
                    val maxSaved = savings?.maxOfOrNull { it.saved ?: 0.0 } ?: 1.0
                    savings?.forEach { point ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(point.label, fontSize = 12.sp, modifier = Modifier.width(50.dp))
                            Box(modifier = Modifier.weight(1f).height(16.dp).clip(RoundedCornerShape(4.dp)).background(GrayBg)) {
                                Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(((point.saved ?: 0.0) / maxSaved).toFloat()).background(Green500, RoundedCornerShape(4.dp)))
                            }
                            Text("₹${(point.saved ?: 0.0).toInt()}", fontSize = 11.sp, modifier = Modifier.width(50.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                        }
                    }
                }
            }
        }
    }
}
