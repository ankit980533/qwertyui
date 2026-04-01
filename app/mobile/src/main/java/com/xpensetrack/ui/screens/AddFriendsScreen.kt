package com.xpensetrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.xpensetrack.data.model.FriendItem
import com.xpensetrack.data.model.FriendRequestItem
import com.xpensetrack.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFriendsScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var requests by remember { mutableStateOf<List<FriendRequestItem>>(emptyList()) }
    var friends by remember { mutableStateOf<List<FriendItem>>(emptyList()) }
    var searchResults by remember { mutableStateOf<List<FriendItem>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try { requests = ApiClient.create<FriendApi>().getPendingRequests() } catch (_: Exception) {}
            try { friends = ApiClient.create<FriendApi>().getFriends() } catch (_: Exception) {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column { Text("Add Friends", fontWeight = FontWeight.Bold); Text("Connect and share expenses", fontSize = 13.sp, color = GrayText) } },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.Close, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Purple700, titleContentColor = White, navigationIconContentColor = White)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)) {
            OutlinedTextField(value = searchQuery, onValueChange = {
                searchQuery = it
                if (it.length >= 2) scope.launch { try { searchResults = ApiClient.create<FriendApi>().search(it) } catch (_: Exception) {} }
            }, placeholder = { Text("Search by name or user ID..") }, trailingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)

            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                listOf("👤+ Add Friend" to "add", "📱 Scan QR" to "qr", "📞 Contacts" to "contacts").forEach { (label, _) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(PurpleLight), contentAlignment = Alignment.Center) {
                            Text(label.take(2), fontSize = 18.sp)
                        }
                        Text(label.drop(3), fontSize = 12.sp)
                    }
                }
            }

            // Friend Requests
            if (requests.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text("📬 Friend Request", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                requests.forEach { req ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(16.dp)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(PurpleLight), contentAlignment = Alignment.Center) {
                                Text(req.fullName.take(1), fontWeight = FontWeight.Bold, color = Purple700)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(req.fullName, fontWeight = FontWeight.Bold)
                                Text(req.displayId, fontSize = 12.sp, color = GrayText)
                                req.hostel?.let { Text(it, fontSize = 12.sp, color = GrayText) }
                                Text("${req.mutualFriends} Mutual Friends", fontSize = 12.sp, color = GrayText)
                            }
                            IconButton(onClick = { scope.launch { try { ApiClient.create<FriendApi>().respond(req.id, true) } catch (_: Exception) {} } }) {
                                Icon(Icons.Default.Check, null, tint = Green500, modifier = Modifier.size(32.dp))
                            }
                            IconButton(onClick = { scope.launch { try { ApiClient.create<FriendApi>().respond(req.id, false) } catch (_: Exception) {} } }) {
                                Icon(Icons.Default.Close, null, tint = Red500, modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }
            }

            // My Friends
            Spacer(modifier = Modifier.height(20.dp))
            Text("👥 My Friends", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            friends.forEach { friend ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(PurpleLight), contentAlignment = Alignment.Center) {
                        Text(friend.fullName.take(1), fontWeight = FontWeight.Bold, color = Purple700)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(friend.fullName, fontWeight = FontWeight.Medium)
                        Text(friend.displayId, fontSize = 12.sp, color = GrayText)
                    }
                }
            }
        }
    }
}
