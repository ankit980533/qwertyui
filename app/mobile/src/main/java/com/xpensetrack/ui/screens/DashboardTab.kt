package com.xpensetrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

@Composable
fun DashboardTab(navController: NavController) {
    var data by remember { mutableStateOf<DashboardData?>(null) }
    val scope = rememberCoroutineScope()

    // Simple refresh: reload every time this composable enters composition
    // Using a savedStateHandle approach via navController
    val refreshTrigger = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<Boolean>("refresh")
        ?.let { liveData ->
            val state = liveData.value
            state
        }

    LaunchedEffect(refreshTrigger, Unit) {
        scope.launch {
            try { data = ApiClient.create<ExpenseApi>().getDashboard() } catch (_: Exception) {}
        }
    }

    // Also refresh on first load and when coming back
    LaunchedEffect(navController.currentDestination) {
        try { data = ApiClient.create<ExpenseApi>().getDashboard() } catch (_: Exception) {}
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Purple gradient header matching Figma
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(Purple700, Purple500)))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Hi, ${data?.fullName ?: "User"}!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                    Text(
                        "Here's your financial overview",
                        fontSize = 14.sp,
                        color = White.copy(alpha = 0.8f)
                    )
                }
                // Notification bell with badge
                IconButton(onClick = { navController.navigate(Routes.NOTIFICATIONS) }) {
                    BadgedBox(badge = {
                        if ((data?.unreadNotificationCount ?: 0) > 0)
                            Badge(containerColor = Red500) {
                                Text("${data?.unreadNotificationCount}", color = White, fontSize = 10.sp)
                            }
                    }) {
                        Box(
                            modifier = Modifier.size(40.dp)
                                .background(Gold.copy(alpha = 0.3f), shape = RoundedCornerShape(50)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Notifications, null, tint = Gold, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // Balance card - matching Figma exactly
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                colors = CardDefaults.cardColors(containerColor = PurpleLight.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Current Balance header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(28.dp)
                                    .background(Purple700, RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("₹", color = White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Current Balance", fontSize = 14.sp, color = GrayText)
                        }
                        Box(
                            modifier = Modifier.size(28.dp)
                                .background(PurpleLight, RoundedCornerShape(6.dp))
                                .clickable { navController.navigate(Routes.EDIT_PROFILE) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Edit, null, tint = Purple700, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "₹${String.format("%.2f", data?.currentBalance ?: 0.0)}",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Monthly Budget row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Monthly Budget", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(
                            "₹${(data?.monthlySpent ?: 0.0).toInt()}/₹${(data?.monthlyBudget ?: 0.0).toInt()}",
                            fontSize = 14.sp,
                            color = GrayText
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress bar with gradient (purple to gold like Figma)
                    Box(
                        modifier = Modifier.fillMaxWidth().height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(GrayLight)
                    ) {
                        val progress = ((data?.budgetUsedPercent ?: 0.0) / 100).toFloat().coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier.fillMaxHeight()
                                .fillMaxWidth(progress)
                                .clip(RoundedCornerShape(5.dp))
                                .background(Brush.horizontalGradient(listOf(Purple700, Gold)))
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Remaining row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Remaining: ₹${(data?.remaining ?: 0.0).toInt()}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Purple700
                        )
                        Text(
                            "${(data?.budgetLeftPercent ?: 0.0).toInt()}% left",
                            fontSize = 13.sp,
                            color = GrayText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick action buttons - matching Figma
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                QuickActionCard("Add\nExpense", "Quick entry", Icons.Default.Add, Purple700, Modifier.weight(1f)) {
                    navController.navigate(Routes.ADD_EXPENSE)
                }
                QuickActionCard("Friends", "Split Bills", Icons.Default.Person, Purple700, Modifier.weight(1f)) {
                    navController.navigate(Routes.FRIENDS)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Dragon hungry banner - yellow/gold like Figma
            if (data?.dragonHungry == true || true) { // Always show for now like Figma
                Card(
                    modifier = Modifier.fillMaxWidth()
                        .clickable { navController.navigate(Routes.DRAGON) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🐉", fontSize = 36.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Your Dragon is Hungry!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text("Feed with saved coins", fontSize = 13.sp, color = GrayText)
                        }
                        Text("✨", fontSize = 24.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Spending Overview header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Spending Overview", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(
                    "View All",
                    color = Purple700,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { navController.navigate(Routes.REPORTS) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category breakdown
            if (data?.monthlyBreakdown.isNullOrEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GrayBg)
                ) {
                    Text(
                        "No expenses yet. Tap 'Add Expense' to start tracking!",
                        modifier = Modifier.padding(20.dp),
                        color = GrayText,
                        fontSize = 14.sp
                    )
                }
            } else {
                data?.monthlyBreakdown?.forEach { (cat, amount) ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    when (cat) {
                                        "FOOD" -> "🍔"; "UTILITIES" -> "💡"; "RENT" -> "🏠"
                                        "TRAVEL" -> "🚗"; else -> "📦"
                                    },
                                    fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(cat, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            }
                            Text(
                                "₹${amount.toInt()}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Purple700
                            )
                        }
                    }
                }
            }

            // Recent expenses
            if (!data?.recentExpenses.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Recent Expenses", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                data?.recentExpenses?.forEach { exp ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(exp.description ?: exp.category, fontWeight = FontWeight.Medium)
                                Text(exp.date, fontSize = 12.sp, color = GrayText)
                            }
                            Text("₹${exp.amount.toInt()}", fontWeight = FontWeight.Bold, color = Red500)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp)) // Bottom padding for nav bar
        }
    }
}

@Composable
fun QuickActionCard(
    title: String, subtitle: String, icon: ImageVector,
    iconColor: Color, modifier: Modifier, onClick: () -> Unit
) {
    OutlinedCard(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(36.dp)
                    .background(PurpleLight, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, fontSize = 12.sp, color = GrayText)
            }
        }
    }
}
