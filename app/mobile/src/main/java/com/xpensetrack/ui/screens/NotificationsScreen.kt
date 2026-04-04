package com.xpensetrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.xpensetrack.data.api.NotificationApi
import com.xpensetrack.data.model.NotificationItem
import com.xpensetrack.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavController) {
    var notifications by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) { 
        scope.launch { 
            try { 
                notifications = ApiClient.create<NotificationApi>().getAll()
                // Mark all unread notifications as read
                notifications.filter { !it.read }.forEach { notif ->
                    try {
                        ApiClient.create<NotificationApi>().markRead(notif.id)
                    } catch (_: Exception) {}
                }
            } catch (_: Exception) {} 
        } 
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column { Text("Notifications", fontWeight = FontWeight.Bold); Text("Catch up on updates", fontSize = 13.sp, color = GrayText) } },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.Close, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Purple700, titleContentColor = White, navigationIconContentColor = White)
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
            items(notifications) { notif ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (notif.read) White else PurpleLight)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                        Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(PurpleLight), contentAlignment = Alignment.Center) {
                            Text(when (notif.type) { "DRAGON_UPDATE" -> "🐉"; "PAYMENT_SUCCESS" -> "💳"; else -> "🔔" }, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkText)
                            Text(notif.message, fontSize = 13.sp, color = GrayText)
                            Text(notif.createdAt.take(16), fontSize = 11.sp, color = GrayText)
                        }
                    }
                }
            }
        }
    }
}
