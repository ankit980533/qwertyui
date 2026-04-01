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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.xpensetrack.data.api.ApiClient
import com.xpensetrack.data.api.FriendApi
import com.xpensetrack.data.model.FriendsOverview
import com.xpensetrack.navigation.Routes
import com.xpensetrack.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(navController: NavController) {
    var data by remember { mutableStateOf<FriendsOverview?>(null) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { scope.launch { try { data = ApiClient.create<FriendApi>().getOverview() } catch (_: Exception) {} } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column { Text("Friends", fontWeight = FontWeight.Bold); Text("Split expenses effortlessly", fontSize = 13.sp, color = GrayText) } },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.Close, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Purple700, titleContentColor = White, navigationIconContentColor = White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Routes.ADD_FRIENDS) }, containerColor = Purple700) {
                Icon(Icons.Default.Add, null, tint = White)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)) {
            // You Owe / To Receive
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = RedLight)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("⚠️ You Owe", fontSize = 13.sp, color = GrayText)
                        Text("₹${(data?.youOwe ?: 0.0).toInt()}", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = GreenLight)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("✅ To Receive", fontSize = 13.sp, color = Green500)
                        Text("₹${(data?.toReceive ?: 0.0).toInt()}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Green500)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Friend balances
            data?.friendBalances?.forEach { friend ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(PurpleLight), contentAlignment = Alignment.Center) {
                                Text(friend.fullName.take(1), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Purple700)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(friend.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(friend.label, fontSize = 14.sp,
                                    color = if (friend.amount > 0) Green500 else Red500)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        friend.transactions.forEach { tx ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("📄", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(tx.description, fontSize = 14.sp)
                                        Text(tx.status, fontSize = 12.sp,
                                            color = if (tx.status == "Settled") Green500 else Gold,
                                            modifier = Modifier.background(
                                                if (tx.status == "Settled") GreenLight else YellowLight,
                                                RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 2.dp))
                                    }
                                }
                                Text("${if (tx.amount > 0) "+" else ""}₹${tx.amount.toInt()}", fontWeight = FontWeight.Bold,
                                    color = if (tx.amount > 0) Green500 else Red500)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { scope.launch { try { ApiClient.create<FriendApi>().settle(mapOf("withUserId" to friend.userId)) } catch (_: Exception) {} } },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (friend.amount < 0) Purple700 else Gold)
                        ) { Text(if (friend.amount < 0) "Settle Up" else "Notify", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}
