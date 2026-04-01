package com.xpensetrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.xpensetrack.data.api.ApiClient
import com.xpensetrack.data.api.ExpenseApi
import com.xpensetrack.data.model.CalendarData
import com.xpensetrack.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarTab(navController: NavController) {
    var yearMonth by remember { mutableStateOf(YearMonth.now()) }
    var data by remember { mutableStateOf<CalendarData?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(yearMonth) {
        scope.launch { try { data = ApiClient.create<ExpenseApi>().getCalendar(yearMonth.year, yearMonth.monthValue) } catch (_: Exception) {} }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(modifier = Modifier.fillMaxWidth().background(Purple700).padding(16.dp)) {
            Column {
                Text("Calendar", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = White)
                Text("Track bills and expenses", fontSize = 13.sp, color = White.copy(alpha = 0.8f))
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // Month navigation
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { yearMonth = yearMonth.minusMonths(1) }) { Icon(Icons.Default.ArrowBack, null) }
                Text("${yearMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${yearMonth.year}",
                    fontWeight = FontWeight.Bold, fontSize = 20.sp)
                IconButton(onClick = { yearMonth = yearMonth.plusMonths(1) }) { Icon(Icons.Default.ArrowForward, null) }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Day headers
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa").forEach {
                    Text(it, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 13.sp, color = GrayText, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))

            // Calendar grid
            val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value % 7
            val daysInMonth = yearMonth.lengthOfMonth()
            val dayStatuses = data?.days?.associate { it.date to it.status } ?: emptyMap()
            val today = LocalDate.now()

            var dayCounter = 1
            for (week in 0..5) {
                if (dayCounter > daysInMonth) break
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (dow in 0..6) {
                        if (week == 0 && dow < firstDayOfWeek || dayCounter > daysInMonth) {
                            Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                        } else {
                            val day = dayCounter
                            val dateStr = "${yearMonth.year}-${yearMonth.monthValue.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
                            val status = dayStatuses[dateStr]
                            val isToday = yearMonth.atDay(day) == today
                            val bgColor = when (status) {
                                "SPENT_MOST" -> RedLight
                                "SPENT_LEAST" -> GreenLight
                                else -> if (isToday) PurpleLight else White
                            }
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f).padding(2.dp)
                                .clip(CircleShape).background(bgColor), contentAlignment = Alignment.Center) {
                                Text("$day", fontSize = 14.sp, fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal)
                            }
                            dayCounter++
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(RedLight))
                    Text(" Spent the most", fontSize = 12.sp, color = Red500)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(GreenLight))
                    Text(" Spent the least", fontSize = 12.sp, color = Green500)
                }
            }

            // Upcoming Events
            Spacer(modifier = Modifier.height(20.dp))
            Text("🕐 Upcoming Events", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            data?.upcomingEvents?.forEach { event ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (event.amount > 1000) YellowLight else White)) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("⚠️ ${event.title}", fontWeight = FontWeight.Bold)
                            Text(event.dueDate, fontSize = 13.sp, color = Red500)
                        }
                        Text("₹${event.amount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
            }

            // Today's expenses
            if (!data?.todayExpenses.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text("🐉 Your expense of the day", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                data?.todayExpenses?.forEach { exp ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(16.dp)) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("⚠️ ${exp.description ?: exp.category}", fontWeight = FontWeight.Bold)
                                Text(exp.category, fontSize = 13.sp, color = GrayText)
                            }
                            Text("₹${exp.amount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}
