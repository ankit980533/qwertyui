package com.xpensetrack.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.xpensetrack.data.api.ApiClient
import com.xpensetrack.data.api.ExpenseApi
import com.xpensetrack.data.model.DashboardData
import com.xpensetrack.navigation.Routes
import com.xpensetrack.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// Category colors matching Figma
val CategoryColors = mapOf(
    "FOOD" to Color(0xFF6A0DAD),
    "UTILITIES" to Color(0xFFCE93D8),
    "RENT" to Color(0xFFBDBDBD),
    "TRAVEL" to Color(0xFFFFB300),
    "MISC" to Color(0xFFFF8F00)
)

@Composable
fun DashboardTab(navController: NavController) {
    var data by remember { mutableStateOf<DashboardData?>(null) }
    val scope = rememberCoroutineScope()

    val refreshTrigger = navController.currentBackStackEntry
        ?.savedStateHandle?.getLiveData<Boolean>("refresh")?.value

    LaunchedEffect(refreshTrigger, Unit) {
        scope.launch {
            try { data = ApiClient.create<ExpenseApi>().getDashboard() } catch (_: Exception) {}
        }
    }

    LaunchedEffect(navController.currentDestination) {
        try { data = ApiClient.create<ExpenseApi>().getDashboard() } catch (_: Exception) {}
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Purple header
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(Purple700, Purple500)))
                .padding(20.dp)
        ) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column {
                    Text("Hi, ${data?.fullName ?: "User"}!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = White)
                    Text("Here's your financial overview", fontSize = 14.sp, color = White.copy(0.8f))
                }
                IconButton(onClick = { navController.navigate(Routes.NOTIFICATIONS) }) {
                    BadgedBox(badge = {
                        if ((data?.unreadNotificationCount ?: 0) > 0)
                            Badge(containerColor = Red500) { Text("${data?.unreadNotificationCount}") }
                    }) {
                        Box(Modifier.size(40.dp).background(Gold.copy(0.3f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Notifications, null, tint = Gold, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }

        Column(Modifier.padding(16.dp)) {
            // Balance card
            Card(Modifier.fillMaxWidth(), RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = White)) {
                Column(Modifier.padding(20.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(28.dp).background(Purple700, RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
                                Text("₹", color = White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text("Current Balance", fontSize = 14.sp, color = GrayText)
                        }
                        Box(Modifier.size(28.dp).background(PurpleLight, RoundedCornerShape(6.dp))
                            .clickable { navController.navigate(Routes.EDIT_PROFILE) }, contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Edit, null, tint = Purple700, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("₹${String.format("%.2f", data?.currentBalance ?: 0.0)}", fontSize = 34.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Monthly Budget", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("₹${(data?.monthlySpent ?: 0.0).toInt()}/₹${(data?.monthlyBudget ?: 0.0).toInt()}", fontSize = 14.sp, color = GrayText)
                    }
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(GrayLight)) {
                        Box(Modifier.fillMaxHeight().fillMaxWidth(((data?.budgetUsedPercent ?: 0.0) / 100).toFloat().coerceIn(0f, 1f))
                            .clip(RoundedCornerShape(5.dp)).background(Brush.horizontalGradient(listOf(Purple700, Gold))))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Remaining: ₹${(data?.remaining ?: 0.0).toInt()}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Purple700)
                        Text("${(data?.budgetLeftPercent ?: 0.0).toInt()}% left", fontSize = 13.sp, color = GrayText)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Quick actions - matching Figma exactly
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                // Add Expense card
                OutlinedCard(
                    modifier = Modifier.weight(1f).height(80.dp).clickable { navController.navigate(Routes.ADD_EXPENSE) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = White)
                ) {
                    Row(
                        Modifier.fillMaxSize().padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(36.dp).background(Color(0xFFF3E5F5), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", color = Purple700, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Add", fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 18.sp)
                            Text("Expense", fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 18.sp)
                            Text("Quick entry", fontSize = 11.sp, color = GrayText)
                        }
                    }
                }
                // Friends card
                OutlinedCard(
                    modifier = Modifier.weight(1f).height(80.dp).clickable { navController.navigate(Routes.FRIENDS) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = White)
                ) {
                    Row(
                        Modifier.fillMaxSize().padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(36.dp).background(Color(0xFFF3E5F5), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, tint = Purple700, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Friends", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Split Bills", fontSize = 11.sp, color = GrayText)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Dragon banner
            Card(Modifier.fillMaxWidth().clickable { navController.navigate(Routes.DRAGON) },
                RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🐉", fontSize = 36.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Your Dragon is Hungry!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Feed with saved coins", fontSize = 13.sp, color = GrayText)
                    }
                    Text("✨", fontSize = 24.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Spending Overview with donut chart
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Spending Overview", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("View All", color = Purple700, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { navController.navigate(Routes.REPORTS) })
            }
            Spacer(Modifier.height(12.dp))

            val breakdown = data?.monthlyBreakdown ?: emptyMap()
            val totalSpent = breakdown.values.sum()

            if (breakdown.isEmpty()) {
                Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = GrayBg)) {
                    Text("No expenses yet. Tap 'Add Expense' to start tracking!", Modifier.padding(20.dp), color = GrayText, fontSize = 14.sp)
                }
            } else {
                Card(Modifier.fillMaxWidth(), RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = White)) {
                    Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        // Large donut chart centered
                        DonutChart(breakdown, totalSpent, Modifier.size(220.dp).padding(8.dp))
                        Spacer(Modifier.height(24.dp))
                        // Legend rows
                        breakdown.forEach { (cat, amount) ->
                            val pct = if (totalSpent > 0) (amount / totalSpent * 100).toInt() else 0
                            val color = CategoryColors[cat] ?: GrayText
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Colored dot
                                Box(Modifier.size(14.dp).clip(CircleShape).background(color))
                                Spacer(Modifier.width(12.dp))
                                // Category name
                                Text(
                                    cat.lowercase().replaceFirstChar { it.uppercase() },
                                    Modifier.weight(1f),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                )
                                // Amount
                                Text(
                                    "₹${amount.toInt()}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                                Spacer(Modifier.width(8.dp))
                                // Percentage
                                Text(
                                    "$pct%",
                                    fontSize = 15.sp,
                                    color = GrayText
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Piggy Bank banner - matching Figma
            Card(
                Modifier.fillMaxWidth().clickable { navController.navigate(Routes.PIGGY_BANK) },
                RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🐷", fontSize = 36.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Your Virtual Piggy Bank", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Save for your Goal!", fontSize = 13.sp, color = GrayText)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Recent transactions - matching Figma exactly
            if (!data?.recentExpenses.isNullOrEmpty()) {
                data?.recentExpenses?.forEach { exp ->
                    Card(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(containerColor = White)
                    ) {
                        Row(
                            Modifier.padding(14.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                // Purple icon box with arrow like Figma
                                Box(
                                    Modifier.size(42.dp).background(Color(0xFFF3E5F5), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("↙", color = Purple700, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        exp.description ?: exp.category,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        "${exp.category.lowercase().replaceFirstChar { it.uppercase() }} • ${getRelativeDate(exp.date)}",
                                        fontSize = 13.sp,
                                        color = GrayText
                                    )
                                }
                            }
                            Text(
                                "-₹${exp.amount.toInt()}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = DarkText
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun DonutChart(data: Map<String, Double>, total: Double, modifier: Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 60f  // Thick donut like Figma
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)
        var startAngle = -90f

        if (total <= 0) {
            drawArc(Color.LightGray, 0f, 360f, false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(strokeWidth, cap = StrokeCap.Butt))
            return@Canvas
        }

        data.forEach { (cat, amount) ->
            val sweep = (amount / total * 360).toFloat()
            val color = when (cat) {
                "FOOD" -> Color(0xFF6A0DAD)       // Dark purple
                "UTILITIES" -> Color(0xFFCE93D8)   // Light purple
                "RENT" -> Color(0xFFBDBDBD)        // Gray
                "TRAVEL" -> Color(0xFFFFB300)      // Gold/yellow
                "MISC" -> Color(0xFFFF8F00)        // Orange
                else -> Color.Gray
            }
            drawArc(color, startAngle, sweep, false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(strokeWidth, cap = StrokeCap.Butt))
            startAngle += sweep
        }
    }
}

fun getRelativeDate(dateStr: String): String {
    return try {
        // Parse UTC instant from backend and convert to device local timezone
        val instant = java.time.Instant.parse(dateStr)
        val localDate = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
        val today = java.time.LocalDate.now()
        val days = java.time.temporal.ChronoUnit.DAYS.between(localDate, today)
        when {
            days == 0L -> "Today"
            days == 1L -> "Yesterday"
            days < 7 -> "$days days ago"
            days < 30 -> "${days / 7} weeks ago"
            else -> localDate.format(java.time.format.DateTimeFormatter.ofPattern("MMM d"))
        }
    } catch (_: Exception) {
        // Fallback: try parsing as LocalDate string
        try {
            val date = java.time.LocalDate.parse(dateStr.take(10))
            val days = java.time.temporal.ChronoUnit.DAYS.between(date, java.time.LocalDate.now())
            when {
                days == 0L -> "Today"
                days == 1L -> "Yesterday"
                days < 7 -> "$days days ago"
                else -> dateStr.take(10)
            }
        } catch (_: Exception) { dateStr }
    }
}

@Composable
fun QuickActionCard(title: String, subtitle: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    // Kept for backward compatibility but not used in dashboard anymore
}
