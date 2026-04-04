package com.xpensetrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CalendarTab(navController: NavController) {
    var yearMonth by remember { mutableStateOf(YearMonth.now()) }
    var data by remember { mutableStateOf<CalendarData?>(null) }
    var selectedDay by remember { mutableStateOf<Int?>(null) }
    var selectedDayExpenses by remember { mutableStateOf<List<com.xpensetrack.data.model.ExpenseItem>>(emptyList()) }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun loadData() {
        scope.launch {
            isRefreshing = true
            try { data = ApiClient.create<ExpenseApi>().getCalendar(yearMonth.year, yearMonth.monthValue) }
            catch (_: Exception) {}
            isRefreshing = false
        }
    }

    LaunchedEffect(yearMonth) {
        loadData()
        selectedDay = null
        selectedDayExpenses = emptyList()
    }

    // When a day is selected, fetch all expenses and filter by that date
    LaunchedEffect(selectedDay) {
        if (selectedDay != null) {
            scope.launch {
                try {
                    val allExpenses = ApiClient.create<ExpenseApi>().getExpenses()
                    val selectedDate = "${yearMonth.year}-${yearMonth.monthValue.toString().padStart(2, '0')}-${selectedDay.toString().padStart(2, '0')}"
                    selectedDayExpenses = allExpenses.filter { exp ->
                        // Parse UTC date and check if it matches selected date
                        try {
                            val expDate = java.time.Instant.parse(exp.date)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate().toString()
                            expDate == selectedDate
                        } catch (_: Exception) {
                            exp.date.startsWith(selectedDate)
                        }
                    }
                } catch (_: Exception) { selectedDayExpenses = emptyList() }
            }
        } else {
            selectedDayExpenses = emptyList()
        }
    }

    val pullRefreshState = rememberPullRefreshState(isRefreshing, ::loadData)

    Box(modifier = Modifier.fillMaxSize().pullRefresh(pullRefreshState)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            // Purple header
            Box(Modifier.fillMaxWidth().background(Purple700).padding(16.dp)) {
                Column {
                    Text("Calendar", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = White)
                    Text("Track bills and expenses", fontSize = 13.sp, color = White.copy(0.8f))
                }
            }

            Column(Modifier.padding(16.dp)) {
            // Month navigation with arrows in purple boxes
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Box(
                    Modifier.size(36.dp).border(1.dp, PurpleLight, RoundedCornerShape(8.dp))
                        .clickable { yearMonth = yearMonth.minusMonths(1) },
                    contentAlignment = Alignment.Center
                ) { Text("<", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Purple700) }

                Text(
                    "${yearMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${yearMonth.year}",
                    fontWeight = FontWeight.Bold, fontSize = 22.sp
                )

                Box(
                    Modifier.size(36.dp).border(1.dp, PurpleLight, RoundedCornerShape(8.dp))
                        .clickable { yearMonth = yearMonth.plusMonths(1) },
                    contentAlignment = Alignment.Center
                ) { Text(">", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Purple700) }
            }

            Spacer(Modifier.height(16.dp))

            // Calendar card
            Card(
                Modifier.fillMaxWidth(),
                RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(Modifier.padding(12.dp)) {
                    // Day headers
                    Row(Modifier.fillMaxWidth()) {
                        listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa").forEach {
                            Text(it, Modifier.weight(1f), textAlign = TextAlign.Center,
                                fontSize = 13.sp, color = GrayText, fontWeight = FontWeight.Medium)
                        }
                    }
                    Spacer(Modifier.height(8.dp))

                    // Calendar grid
                    val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value % 7
                    val daysInMonth = yearMonth.lengthOfMonth()
                    val dayStatuses = data?.days?.associate {
                        it.date.takeLast(2).toIntOrNull() to it.status
                    } ?: emptyMap()
                    val today = LocalDate.now()

                    var dayCounter = 1
                    for (week in 0..5) {
                        if (dayCounter > daysInMonth) break
                        Row(Modifier.fillMaxWidth()) {
                            for (dow in 0..6) {
                                if ((week == 0 && dow < firstDayOfWeek) || dayCounter > daysInMonth) {
                                    // Empty cell or next month days
                                    Box(Modifier.weight(1f).aspectRatio(1f).padding(2.dp)) {
                                        if (week > 0 && dayCounter > daysInMonth) {
                                            // Show next month days in gray
                                            val nextDay = dow - (daysInMonth % 7) + 1
                                            if (nextDay > 0 && nextDay < 5) {
                                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                    Text("$nextDay", fontSize = 14.sp, color = GrayLight)
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    val day = dayCounter
                                    val status = dayStatuses[day]
                                    val isToday = yearMonth.atDay(day) == today
                                    val isSelected = selectedDay == day

                                    // Colors based on Figma:
                                    // Red circle = SPENT_MOST (overspent)
                                    // Green circle = SPENT_LEAST (under budget)
                                    // Black/dark circle = today
                                    // Purple circle = selected date
                                    // No color = on budget or no expense
                                    val bgColor = when {
                                        isSelected -> Purple700
                                        isToday -> DarkText
                                        status == "SPENT_MOST" -> Color(0xFFFFCDD2) // Light red
                                        status == "SPENT_LEAST" -> Color(0xFFC8E6C9) // Light green
                                        else -> Color.Transparent
                                    }
                                    val textColor = when {
                                        isSelected || isToday -> White
                                        status == "SPENT_MOST" -> Red500
                                        status == "SPENT_LEAST" -> Green500
                                        else -> DarkText
                                    }

                                    Box(
                                        Modifier.weight(1f).aspectRatio(1f).padding(2.dp)
                                            .clip(CircleShape)
                                            .background(bgColor)
                                            .clickable { selectedDay = if (selectedDay == day) null else day },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("$day", fontSize = 14.sp, fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal, color = textColor)
                                    }
                                    dayCounter++
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Legend
            Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterVertically) {
                Box(Modifier.size(12.dp).clip(CircleShape).background(Color(0xFFFFCDD2)))
                Text(" Spent the most", fontSize = 13.sp, color = Red500)
                Spacer(Modifier.width(20.dp))
                Box(Modifier.size(12.dp).clip(CircleShape).background(Color(0xFFC8E6C9)))
                Text(" Spent the least", fontSize = 13.sp, color = Green500)
            }

            Spacer(Modifier.height(20.dp))

            // Upcoming Events
            if (!data?.upcomingEvents.isNullOrEmpty()) {
                Text("🕐 Upcoming Events", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                data?.upcomingEvents?.forEach { event ->
                    Card(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (event.amount > 1000) Color(0xFFFFF8E1) else White
                        ),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(Modifier.padding(16.dp).fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Column {
                                Text("⚠️ ${event.title}", fontWeight = FontWeight.Bold)
                                Text(event.dueDate.toString(), fontSize = 13.sp, color = Red500)
                            }
                            Text("₹${event.amount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // Your expense of the day (or selected date)
            val displayDate = if (selectedDay != null) {
                "${selectedDay} ${yearMonth.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }}"
            } else "the day"
            Text("🐉 Your expense of $displayDate", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(8.dp))

            val expensesToShow = if (selectedDay != null) selectedDayExpenses else data?.todayExpenses ?: emptyList()

            if (expensesToShow.isEmpty()) {
                Text(
                    if (selectedDay != null) "No expenses on this date" else "No expenses today",
                    color = GrayText, fontSize = 14.sp
                )
            } else {
                expensesToShow.forEachIndexed { index, exp ->
                    // First expense gets yellow border (like Figma), rest get purple light
                    val borderColor = if (index == 0) Color(0xFFFFB300) else PurpleLight
                    val bgColor = if (index == 0) Color(0xFFFFF8E1) else White
                    Card(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            .border(1.dp, borderColor, RoundedCornerShape(16.dp)),
                        RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = bgColor),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(Modifier.padding(16.dp).fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    when (exp.category) { "FOOD" -> "🍔"; "UTILITIES" -> "💡"; "RENT" -> "🏠"; "TRAVEL" -> "🚗"; else -> "⚠️" },
                                    fontSize = 20.sp
                                )
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(exp.description ?: exp.category, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text(exp.category.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 13.sp, color = GrayText)
                                }
                            }
                            Text("₹${exp.amount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            }

            // Budget overrun warning
            if (data?.budgetOverrun == true) {
                Spacer(Modifier.height(12.dp))
                Text("⚠️ There was a budget overrun.", fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                    color = DarkText, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }

            Spacer(Modifier.height(16.dp))

            // Weekly Spend Limit card
            Card(
                Modifier.fillMaxWidth(),
                RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(Modifier.padding(16.dp).fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💳", fontSize = 20.sp)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Weekly Spend Limit", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Current weekly spend limit", fontSize = 12.sp, color = GrayText)
                        }
                    }
                    Text("₹${(data?.weeklySpendLimit ?: 0.0).toInt()}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Attention warning
            Text(
                "Attention User:\nWarning is generated if day spending is higher than limit set by app, according to your set weekly spent limit",
                fontSize = 12.sp, color = GrayText, textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(80.dp))
            }
        }
        PullRefreshIndicator(isRefreshing, pullRefreshState, Modifier.align(Alignment.TopCenter))
    }
}
