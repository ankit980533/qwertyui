package com.xpensetrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.xpensetrack.data.api.ApiClient
import com.xpensetrack.data.api.FriendApi
import com.xpensetrack.data.model.FriendBalanceItem
import com.xpensetrack.data.model.FriendsOverview
import com.xpensetrack.navigation.Routes
import com.xpensetrack.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun FriendsScreen(navController: NavController) {
    var data by remember { mutableStateOf<FriendsOverview?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    var showSettleDialog by remember { mutableStateOf(false) }
    var selectedFriend = remember { mutableStateOf<FriendBalanceItem?>(null) }
    val scope = rememberCoroutineScope()

    fun loadData() {
        scope.launch {
            isRefreshing = true
            try { data = ApiClient.create<FriendApi>().getOverview() } catch (_: Exception) {}
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) { loadData() }

    val pullRefreshState = rememberPullRefreshState(isRefreshing, ::loadData)

    // Settle Up Dialog
    if (showSettleDialog && selectedFriend.value != null) {
        AlertDialog(
            onDismissRequest = { showSettleDialog = false },
            title = { Text("Settle Up", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Pay ${selectedFriend.value?.fullName}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Text("Amount: ₹${kotlin.math.abs(selectedFriend.value?.amount?.toInt() ?: 0)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Purple700)
                    Spacer(Modifier.height(16.dp))
                    Text("Payment Method:", fontSize = 14.sp, color = GrayText)
                    Spacer(Modifier.height(8.dp))
                    Card(
                        Modifier.fillMaxWidth(),
                        RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("💵", fontSize = 24.sp)
                            Spacer(Modifier.width(12.dp))
                            Text("Cash Payment", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Confirm that you have paid in cash", fontSize = 13.sp, color = GrayText)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                selectedFriend.value?.let {
                                    ApiClient.create<FriendApi>().settle(mapOf("withUserId" to it.userId))
                                    data = ApiClient.create<FriendApi>().getOverview()
                                }
                                showSettleDialog = false
                                selectedFriend.value = null
                            } catch (_: Exception) {}
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Purple700),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Confirm Payment") }
            },
            dismissButton = {
                TextButton(onClick = { showSettleDialog = false; selectedFriend.value = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column { Text("Friends", fontWeight = FontWeight.Bold); Text("Split expenses effortlessly", fontSize = 13.sp, color = White.copy(0.8f)) } },
                actions = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.Close, null, tint = White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Purple700, titleContentColor = White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Routes.ADD_FRIENDS) }, containerColor = Purple700) {
                Icon(Icons.Default.Add, null, tint = White)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).pullRefresh(pullRefreshState)) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            // You Owe / To Receive cards
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(Modifier.weight(1f), RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚠️", fontSize = 14.sp); Spacer(Modifier.width(4.dp))
                            Text("You Owe", fontSize = 13.sp, color = GrayText)
                        }
                        Text("₹${(data?.youOwe ?: 0.0).toInt()}", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Card(Modifier.weight(1f), RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✅", fontSize = 14.sp); Spacer(Modifier.width(4.dp))
                            Text("To Receive", fontSize = 13.sp, color = Green500)
                        }
                        Text("₹${(data?.toReceive ?: 0.0).toInt()}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Green500)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Friend balance cards - Splitwise style
            data?.friendBalances?.forEach { friend ->
                Card(
                    Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        // Friend header with avatar
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(52.dp).clip(CircleShape).background(Color(0xFFFFE0B2)), contentAlignment = Alignment.Center) {
                                Text(friend.fullName.take(1).uppercase(), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Purple700)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(friend.fullName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(
                                    friend.label,
                                    fontSize = 15.sp,
                                    color = if (friend.amount > 0) Green500 else Purple700,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text("ⓘ", fontSize = 20.sp, color = GrayText)
                        }

                        Spacer(Modifier.height(12.dp))

                        // Transaction items
                        friend.transactions.forEach { tx ->
                            Card(
                                Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = if (tx.status == "Settled") Color(0xFFF1F8E9) else Color(0xFFFFF8E1)),
                                border = androidx.compose.foundation.BorderStroke(
                                    0.5.dp,
                                    if (tx.status == "Settled") Green500.copy(0.3f) else Gold.copy(0.3f)
                                )
                            ) {
                                Row(Modifier.padding(12.dp).fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Status icon
                                        Box(
                                            Modifier.size(32.dp).clip(CircleShape)
                                                .background(if (tx.status == "Settled") Color(0xFFE8F5E9) else Color(0xFFFFF3E0)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(if (tx.status == "Settled") "✅" else "🕐", fontSize = 14.sp)
                                        }
                                        Spacer(Modifier.width(10.dp))
                                        Column {
                                            Text(tx.description, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                            Text(
                                                tx.status,
                                                fontSize = 12.sp,
                                                color = if (tx.status == "Settled") Green500 else Gold,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier
                                                    .background(
                                                        if (tx.status == "Settled") Color(0xFFC8E6C9) else Color(0xFFFFE0B2),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        "${if (tx.amount > 0) "+" else ""}₹${kotlin.math.abs(tx.amount.toInt())}",
                                        fontWeight = FontWeight.Bold, fontSize = 16.sp,
                                        color = if (tx.amount > 0) Green500 else Purple700
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Show button based on balance
                        if (friend.amount > 0) {
                            // Friend owes you - show Notify button
                            Button(
                                onClick = {
                                    scope.launch {
                                        try {
                                            // TODO: Implement proper notification API
                                            // For now, just show a toast/snackbar
                                            // ApiClient.create<NotificationApi>().sendReminder(friend.userId)
                                        } catch (_: Exception) {}
                                    }
                                },
                                Modifier.fillMaxWidth().height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Green500)
                            ) {
                                Text("Notify", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        } else {
                            // You owe friend - show Settle Up button (navigate to payment screen)
                            Button(
                                onClick = {
                                    selectedFriend.value = friend
                                    showSettleDialog = true
                                },
                                Modifier.fillMaxWidth().height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Purple700)
                            ) {
                                Text("Settle Up", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }

            // Settled friends
            if (data?.friendBalances?.isEmpty() == true) {
                Spacer(Modifier.height(16.dp))
                Text("No pending balances", color = GrayText, fontSize = 14.sp)
            }

            Spacer(Modifier.height(16.dp))

            // Add Friends button at bottom
            OutlinedCard(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Purple700.copy(0.3f))
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("👥", fontSize = 18.sp)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Friends", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Green500)
                }
            }

                Spacer(Modifier.height(80.dp))
            }
            PullRefreshIndicator(isRefreshing, pullRefreshState, Modifier.align(Alignment.TopCenter))
        }
    }
}
