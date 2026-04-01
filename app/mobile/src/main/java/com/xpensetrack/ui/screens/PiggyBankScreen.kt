package com.xpensetrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
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
import com.xpensetrack.data.api.PiggyBankApi
import com.xpensetrack.data.model.PiggyBankOverview
import com.xpensetrack.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PiggyBankScreen(navController: NavController) {
    var data by remember { mutableStateOf<PiggyBankOverview?>(null) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { scope.launch { try { data = ApiClient.create<PiggyBankApi>().getOverview() } catch (_: Exception) {} } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column { Text("Your Virtual Piggy Bank", fontWeight = FontWeight.Bold); Text("Save Smart", fontSize = 13.sp, color = GrayText) } },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.Close, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Purple700, titleContentColor = White, navigationIconContentColor = White)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)) {
            // Piggy bank image
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("💰", fontSize = 80.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Your Savings card
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🏦", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Your Savings", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Icon(Icons.Default.Edit, null, tint = Purple700, modifier = Modifier.size(20.dp))
                    }
                    Text("This month's savings towards your goals", fontSize = 13.sp, color = GrayText)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("₹${(data?.monthlySavings ?: 0.0).toInt()}", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { ((data?.savingsProgressPercent ?: 0.0) / 100).toFloat() },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = Gold, trackColor = GrayLight
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Text("₹${(data?.savingsTarget ?: 100000.0).toInt()}", fontSize = 13.sp, color = GrayText)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Recent Goals 🔗", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                data?.recentGoals?.take(2)?.forEach { goal ->
                    Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = GreenLight)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("🎯", fontSize = 32.sp)
                            Text(goal.goalName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("₹${goal.targetAmount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Before: ${goal.deadline}", fontSize = 12.sp, color = GrayText)
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (goal.progressPercent / 100).toFloat() },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = Green500, trackColor = GrayLight
                            )
                        }
                    }
                }
                if ((data?.recentGoals?.size ?: 0) < 2) Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("ℹ️ Don't have anything in mind? Try asking our AI chatbot for the best usage of your saved money!",
                fontSize = 13.sp, color = GrayText)
        }
    }
}
