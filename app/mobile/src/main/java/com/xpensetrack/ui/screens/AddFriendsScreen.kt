package com.xpensetrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.xpensetrack.data.api.ApiClient
import com.xpensetrack.data.api.FriendApi
import com.xpensetrack.data.model.FriendItem
import com.xpensetrack.data.model.FriendRequestItem
import com.xpensetrack.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFriendsScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var requests by remember { mutableStateOf<List<FriendRequestItem>>(emptyList()) }
    var sentRequests by remember { mutableStateOf<List<FriendRequestItem>>(emptyList()) }
    var friends by remember { mutableStateOf<List<FriendItem>>(emptyList()) }
    var searchResults by remember { mutableStateOf<List<FriendItem>>(emptyList()) }
    var sentRequestIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try { requests = ApiClient.create<FriendApi>().getPendingRequests() } catch (_: Exception) {}
            try { sentRequests = ApiClient.create<FriendApi>().getSentRequests() } catch (_: Exception) {}
            try { friends = ApiClient.create<FriendApi>().getFriends() } catch (_: Exception) {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column { Text("Add Friends", fontWeight = FontWeight.Bold); Text("Connect and share expenses", fontSize = 13.sp, color = White.copy(0.8f)) } },
                actions = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.Close, null, tint = White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Purple700, titleContentColor = White)
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    if (it.length >= 2) scope.launch { try { searchResults = ApiClient.create<FriendApi>().search(it) } catch (_: Exception) {} }
                    else searchResults = emptyList()
                },
                placeholder = { Text("Search by name or user ID..") },
                trailingIcon = { Icon(Icons.Default.Search, null, tint = GrayText) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = GrayLight, focusedBorderColor = Purple700)
            )

            Spacer(Modifier.height(16.dp))

            // Quick action buttons - Add Friend, Scan QR, Contacts
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                listOf("👤+" to "Add Friend", "📱" to "Scan QR", "📞" to "Contacts").forEach { (icon, label) ->
                    Column(
                        Modifier.border(1.dp, GrayLight, RoundedCornerShape(12.dp)).padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(icon, fontSize = 24.sp)
                        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Search results
            if (searchResults.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                Card(Modifier.fillMaxWidth(), RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = White)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("People you may know", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Spacer(Modifier.height(12.dp))
                        searchResults.forEach { user ->
                            Card(
                                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5).copy(0.5f))
                            ) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFFFE0B2)), contentAlignment = Alignment.Center) {
                                        Text(user.fullName.take(1).uppercase(), fontWeight = FontWeight.Bold, color = Purple700, fontSize = 18.sp)
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(user.displayId, fontSize = 12.sp, color = GrayText)
                                        user.hostel?.let { Text(it, fontSize = 12.sp, color = GrayText) }
                                    }
                                    val isSent = user.id in sentRequestIds
                                    Button(
                                        onClick = {
                                            if (!isSent) {
                                                scope.launch {
                                                    try {
                                                        ApiClient.create<FriendApi>().sendRequest(mapOf("toUserId" to user.id))
                                                        sentRequestIds = sentRequestIds + user.id
                                                    } catch (_: Exception) {}
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSent) Green500 else Purple700
                                        ),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                                    ) { Text(if (isSent) "Sent ✓" else "Add", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                                }
                            }
                        }
                    }
                }
            }

            // Friend Requests
            if (requests.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                Card(Modifier.fillMaxWidth(), RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = White)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📬", fontSize = 18.sp); Spacer(Modifier.width(8.dp))
                            Text("Friend Request", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        }
                        Spacer(Modifier.height(12.dp))
                        requests.forEach { req ->
                            Card(
                                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5).copy(0.5f))
                            ) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFFFE0B2)), contentAlignment = Alignment.Center) {
                                        Text(req.fullName.take(1).uppercase(), fontWeight = FontWeight.Bold, color = Purple700, fontSize = 18.sp)
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(req.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(req.displayId, fontSize = 12.sp, color = GrayText)
                                        req.hostel?.let { Text(it, fontSize = 12.sp, color = GrayText) }
                                        Text("${req.mutualFriends} Mutual Friends", fontSize = 12.sp, color = GrayText)
                                    }
                                    // Accept button (green circle)
                                    Box(
                                        Modifier.size(36.dp).clip(CircleShape).background(Green500)
                                            .clickable {
                                                scope.launch {
                                                    try {
                                                        ApiClient.create<FriendApi>().respond(req.id, true)
                                                        requests = requests.filter { it.id != req.id }
                                                        try { friends = ApiClient.create<FriendApi>().getFriends() } catch (_: Exception) {}
                                                    } catch (_: Exception) {}
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) { Text("✓", color = White, fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                                    Spacer(Modifier.width(8.dp))
                                    // Reject button (red circle)
                                    Box(
                                        Modifier.size(36.dp).clip(CircleShape).background(Red500)
                                            .clickable {
                                                scope.launch {
                                                    try {
                                                        ApiClient.create<FriendApi>().respond(req.id, false)
                                                        requests = requests.filter { it.id != req.id }
                                                    } catch (_: Exception) {}
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) { Text("✕", color = White, fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                                }
                            }
                        }
                    }
                }
            }

            // Sent Requests (outgoing)
            if (sentRequests.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                Card(Modifier.fillMaxWidth(), RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = White)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📤", fontSize = 18.sp); Spacer(Modifier.width(8.dp))
                            Text("Sent Requests", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        }
                        Spacer(Modifier.height(12.dp))
                        sentRequests.forEach { req ->
                            Card(
                                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1).copy(0.5f))
                            ) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFFFE0B2)), contentAlignment = Alignment.Center) {
                                        Text(req.fullName.take(1).uppercase(), fontWeight = FontWeight.Bold, color = Purple700, fontSize = 18.sp)
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(req.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(req.displayId, fontSize = 12.sp, color = GrayText)
                                        Text("Pending", fontSize = 12.sp, color = Gold)
                                    }
                                    // Revoke button
                                    OutlinedButton(
                                        onClick = {
                                            scope.launch {
                                                try {
                                                    ApiClient.create<FriendApi>().revokeRequest(req.id)
                                                    sentRequests = sentRequests.filter { it.id != req.id }
                                                } catch (_: Exception) {}
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Red500)
                                    ) { Text("Revoke", fontSize = 12.sp, color = Red500, fontWeight = FontWeight.Bold) }
                                }
                            }
                        }
                    }
                }
            }

            // My Friends
            Spacer(Modifier.height(20.dp))
            Card(Modifier.fillMaxWidth(), RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = White)) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("👥", fontSize = 18.sp); Spacer(Modifier.width(8.dp))
                        Text("My Friends", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(12.dp))

                    if (friends.isEmpty()) {
                        Text("No friends yet. Search and add friends above!", color = GrayText, fontSize = 14.sp)
                    } else {
                        friends.forEach { friend ->
                            Card(
                                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5).copy(0.3f))
                            ) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFFFE0B2)), contentAlignment = Alignment.Center) {
                                        Text(friend.fullName.take(1).uppercase(), fontWeight = FontWeight.Bold, color = Purple700, fontSize = 18.sp)
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(friend.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(friend.displayId, fontSize = 12.sp, color = GrayText)
                                        friend.hostel?.let { Text(it, fontSize = 12.sp, color = GrayText) }
                                    }
                                    // Chat icon
                                    Text("💬", fontSize = 24.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}
