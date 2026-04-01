package com.xpensetrack.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.xpensetrack.data.api.ApiClient
import com.xpensetrack.data.api.ProfileApi
import com.xpensetrack.data.model.UserProfile
import com.xpensetrack.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ProfileTab(navController: NavController) {
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { scope.launch { try { profile = ApiClient.create<ProfileApi>().getProfile() } catch (_: Exception) {} } }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(modifier = Modifier.fillMaxWidth().background(Purple700).padding(20.dp)) {
            Column {
                Text("Profile", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = White)
                Text("Manage your Account", fontSize = 14.sp, color = White.copy(alpha = 0.8f))
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // Profile card
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(72.dp).clip(CircleShape).background(PurpleLight), contentAlignment = Alignment.Center) {
                            Text(profile?.fullName?.take(1) ?: "U", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Purple700)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(profile?.fullName ?: "Username", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text("ID: ${profile?.displayId ?: "HL000000"}", fontSize = 13.sp, color = GrayText)
                            Text("Joined ${profile?.joinedMonth ?: ""}", fontSize = 13.sp, color = GrayText)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Contact info
                    listOf(
                        "✉️" to (profile?.email ?: "User Mail ID"),
                        "📞" to (profile?.phoneNumber ?: "+91 00000 00000"),
                        "📍" to (profile?.address ?: profile?.hostel ?: "Address")
                    ).forEach { (icon, text) ->
                        Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(icon, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text, fontSize = 14.sp, color = GrayText)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats row
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatCard("₹${(profile?.totalSaved ?: 0.0).toInt()}", "Total\nSaved", GreenLight, Green500, Modifier.weight(1f))
                        StatCard("₹${(profile?.totalSpent ?: 0.0).toInt()}", "Total\nSpent", PurpleLight, Purple700, Modifier.weight(1f))
                        StatCard("${profile?.monthsActive ?: 0}", "Months\nActive", YellowLight, Gold, Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Menu items
            listOf("👥 Friends & Roommates" to "friends", "⚙️ Account Settings" to "edit_profile").forEach { (label, route) ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    .clickable { navController.navigate(route) },
                    shape = RoundedCornerShape(16.dp)) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(label, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                        Icon(Icons.Default.ArrowForward, null, tint = GrayText)
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(value: String, label: String, bgColor: androidx.compose.ui.graphics.Color, textColor: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = bgColor)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor)
            Text(label, fontSize = 11.sp, color = GrayText, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
