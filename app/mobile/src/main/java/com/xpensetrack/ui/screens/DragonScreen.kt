package com.xpensetrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
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
import com.xpensetrack.data.api.DragonApi
import com.xpensetrack.data.model.DragonData
import com.xpensetrack.navigation.Routes
import com.xpensetrack.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DragonScreen(navController: NavController) {
    var data by remember { mutableStateOf<DragonData?>(null) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { scope.launch { try { data = ApiClient.create<DragonApi>().getDragon() } catch (_: Exception) {} } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column { Text("Your Pet Dragon", fontWeight = FontWeight.Bold); Text("${data?.name ?: "Baby Dragon"} • Level ${data?.level ?: 1}", fontSize = 13.sp, color = GrayText) } },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 12.dp)
                        .background(YellowLight, RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Icon(Icons.Default.Star, null, tint = Gold, modifier = Modifier.size(18.dp))
                        Text(" ${data?.userCoins ?: 0}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Purple700, titleContentColor = White)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            // Dragon display
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🐉", fontSize = 100.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(data?.name ?: "Baby Dragon", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("⭐ Level ${data?.level ?: 1}", fontSize = 14.sp, color = Gold,
                        modifier = Modifier.background(YellowLight, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 4.dp))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Happiness
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("❤️ Happiness", fontWeight = FontWeight.Medium)
                        Text("${data?.happiness ?: 50}%", fontWeight = FontWeight.Bold)
                    }
                    LinearProgressIndicator(
                        progress = { (data?.happiness ?: 50) / 100f },
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                        color = Gold, trackColor = GrayLight
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Level Up Progress
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("⭐ Level Up Progress", fontWeight = FontWeight.Medium)
                        Text("${data?.experience ?: 0}/${data?.coinsToNextLevel ?: 3000} coins", fontSize = 13.sp)
                    }
                    LinearProgressIndicator(
                        progress = { if ((data?.coinsToNextLevel ?: 1) > 0) (data?.experience ?: 0).toFloat() / (data?.coinsToNextLevel ?: 1) else 0f },
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                        color = Purple700, trackColor = PurpleLight
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(data?.levelUpMessage ?: "Unlock the level 10 and unleash new pet", fontSize = 13.sp, color = GrayText)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Feed / Shop buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedCard(modifier = Modifier.weight(1f).clickable {
                    scope.launch { try { ApiClient.create<DragonApi>().feed(mapOf("coins" to (data?.feedCost ?: 100))) } catch (_: Exception) {} }
                }, shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🍖", fontSize = 32.sp)
                        Text("Feed", fontWeight = FontWeight.Bold)
                        Text("${data?.feedCost ?: 100} coins", fontSize = 13.sp, color = GrayText)
                    }
                }
                OutlinedCard(modifier = Modifier.weight(1f).clickable { navController.navigate(Routes.SHOP) }, shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🏪", fontSize = 32.sp)
                        Text("Shop", fontWeight = FontWeight.Bold)
                        Text("${data?.shopMinCost ?: 300} coins", fontSize = 13.sp, color = GrayText)
                    }
                }
            }
        }
    }
}
