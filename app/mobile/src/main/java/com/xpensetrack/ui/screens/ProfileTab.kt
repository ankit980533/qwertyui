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
import com.xpensetrack.data.api.ProfileApi
import com.xpensetrack.data.model.UserProfile
import com.xpensetrack.navigation.Routes
import com.xpensetrack.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ProfileTab(navController: NavController) {
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun loadData() {
        scope.launch {
            isRefreshing = true
            try { profile = ApiClient.create<ProfileApi>().getProfile() } catch (_: Exception) {}
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) { loadData() }

    val pullRefreshState = rememberPullRefreshState(isRefreshing, ::loadData)

    Box(modifier = Modifier.fillMaxSize().pullRefresh(pullRefreshState)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            // Purple header
            Box(Modifier.fillMaxWidth().background(Purple700).padding(20.dp)) {
                Column {
                    Text("Profile", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = White)
                    Text("Manage your Account", fontSize = 14.sp, color = White.copy(0.8f))
                }
            }

            Column(Modifier.padding(16.dp)) {
            // Profile info card
            Card(
                Modifier.fillMaxWidth(),
                RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(Modifier.padding(20.dp)) {
                    // Avatar + Name + ID
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(72.dp).clip(CircleShape).background(Color(0xFFFFE0B2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                profile?.fullName?.take(1)?.uppercase() ?: "U",
                                fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Purple700
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                profile?.fullName ?: "Username",
                                fontWeight = FontWeight.Bold, fontSize = 22.sp
                            )
                            Text(
                                "ID: ${profile?.displayId ?: "HL000000"}",
                                fontSize = 14.sp, color = GrayText
                            )
                            Text(
                                "Joined ${profile?.joinedMonth ?: ""}",
                                fontSize = 14.sp, color = GrayText
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Contact info rows with icons
                    ProfileInfoRow("✉️", profile?.email ?: "User Mail ID")
                    ProfileInfoRow("📞", profile?.phoneNumber ?: "+91 00000 00000")
                    ProfileInfoRow("📍", profile?.address ?: profile?.hostel ?: "Address")

                    Spacer(Modifier.height(20.dp))

                    // Stats row - 3 colored boxes
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                        // Total Saved - green border
                        StatBox(
                            value = "₹${(profile?.totalSaved ?: 0.0).toInt()}",
                            label = "Total\nSaved",
                            borderColor = Green500,
                            valueColor = Green500,
                            modifier = Modifier.weight(1f)
                        )
                        // Total Spent - purple border
                        StatBox(
                            value = "₹${(profile?.totalSpent ?: 0.0).toInt()}",
                            label = "Total\nSpent",
                            borderColor = Purple700,
                            valueColor = Purple700,
                            modifier = Modifier.weight(1f)
                        )
                        // Active time - gold border (shows days or months)
                        StatBox(
                            value = profile?.activeLabel ?: "0 days",
                            label = "Active",
                            borderColor = Gold,
                            valueColor = Gold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Menu items card
            Card(
                Modifier.fillMaxWidth(),
                RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column {
                    ProfileMenuItem("👥", "Friends & Roommates", Purple700) {
                        navController.navigate(Routes.FRIENDS)
                    }
                    HorizontalDivider(color = GrayLight, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileMenuItem("⚙️", "Account Settings", Purple700) {
                        navController.navigate(Routes.EDIT_PROFILE)
                    }
                    HorizontalDivider(color = GrayLight, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileMenuItem("🔔", "Notifications", Purple700) {
                        navController.navigate(Routes.NOTIFICATIONS)
                    }
                    HorizontalDivider(color = GrayLight, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileMenuItem("🔒", "Privacy & Security", Purple700) { }
                    // Payment Methods - Commented out as requested
                    // HorizontalDivider(color = GrayLight, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    // ProfileMenuItem("💳", "Payment Methods", Purple700) {
                    //     navController.navigate(Routes.PAYMENT)
                    // }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Logout button
            OutlinedCard(
                Modifier.fillMaxWidth().clickable {
                    // Clear token and navigate to login
                    ApiClient.token = null
                    scope.launch {
                        try {
                            val context = navController.context
                            com.xpensetrack.data.TokenStore.clearToken(context)
                        } catch (_: Exception) {}
                    }
                    navController.navigate(Routes.LOGIN) { popUpTo(0) }
                },
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("↪", fontSize = 18.sp, color = Green500)
                    Spacer(Modifier.width(8.dp))
                    Text("Logout", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Green500)
                }
            }

            Spacer(Modifier.height(80.dp))
            }
        }
        PullRefreshIndicator(isRefreshing, pullRefreshState, Modifier.align(Alignment.TopCenter))
    }
}

@Composable
fun ProfileInfoRow(icon: String, text: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 18.sp)
        Spacer(Modifier.width(12.dp))
        Text(text, fontSize = 15.sp, color = GrayText)
    }
}

@Composable
fun StatBox(value: String, label: String, borderColor: Color, valueColor: Color, modifier: Modifier) {
    Box(
        modifier.border(1.5.dp, borderColor, RoundedCornerShape(12.dp)).padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = valueColor)
            Text(label, fontSize = 11.sp, color = GrayText, textAlign = TextAlign.Center, lineHeight = 14.sp)
        }
    }
}

@Composable
fun ProfileMenuItem(icon: String, title: String, iconBgColor: Color, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(40.dp).background(PurpleLight, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 18.sp)
        }
        Spacer(Modifier.width(14.dp))
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Text(">", fontSize = 18.sp, color = GrayText)
    }
}
