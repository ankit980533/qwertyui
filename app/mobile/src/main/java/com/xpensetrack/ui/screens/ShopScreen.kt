package com.xpensetrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.xpensetrack.data.api.ApiClient
import com.xpensetrack.data.api.DragonApi
import com.xpensetrack.data.model.ShopData
import com.xpensetrack.data.model.ShopItemData
import com.xpensetrack.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(navController: NavController) {
    var data by remember { mutableStateOf<ShopData?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("🏠 Food", "🛒 Add-ins", "👕 Skins", "🪙 Coins")
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { scope.launch { try { data = ApiClient.create<DragonApi>().getShop() } catch (_: Exception) {} } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column { Text("Shop", fontWeight = FontWeight.Bold); Text("Treat your pet", fontSize = 13.sp, color = GrayText) } },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 12.dp)
                        .background(YellowLight, RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Icon(Icons.Default.Star, null, tint = Gold, modifier = Modifier.size(18.dp))
                        Text(" ${data?.userCoins ?: 0}", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Purple700, titleContentColor = White)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Tab row
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tabs.forEachIndexed { i, tab ->
                    val selected = selectedTab == i
                    Box(modifier = Modifier.weight(1f)
                        .background(if (selected) Purple700 else GrayBg, RoundedCornerShape(12.dp))
                        .clickable { selectedTab = i }.padding(10.dp), contentAlignment = Alignment.Center) {
                        Text(tab, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (selected) White else DarkText)
                    }
                }
            }

            when (selectedTab) {
                0 -> ShopGrid(data?.food ?: emptyList(), scope, navController)
                1 -> ShopGrid(data?.addins ?: emptyList(), scope, navController)
                2 -> ShopGrid(data?.skins ?: emptyList(), scope, navController)
                3 -> CoinPacksList(data, scope)
            }
        }
    }
}

@Composable
fun ShopGrid(items: List<ShopItemData>, scope: kotlinx.coroutines.CoroutineScope, navController: NavController) {
    LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(items) { item ->
            Card(modifier = Modifier.fillMaxWidth().clickable {
                scope.launch { try { ApiClient.create<DragonApi>().buyItem(mapOf("itemId" to item.id)) } catch (_: Exception) {} }
            }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = if (item.owned) GreenLight else White),
                border = if (item.owned) androidx.compose.foundation.BorderStroke(2.dp, Green500) else null) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎁", fontSize = 40.sp)
                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    item.description?.let { Text(it, fontSize = 12.sp, color = GrayText) }
                    Spacer(modifier = Modifier.height(4.dp))
                    if (item.owned) {
                        Text("✓ Owned", color = Green500, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    } else {
                        Text("⭐ ${item.price} coins", color = Gold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CoinPacksList(data: ShopData?, scope: kotlinx.coroutines.CoroutineScope) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("🪙 Coin Packs", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Purchase coins to get exclusive items for your dragon", fontSize = 13.sp, color = GrayText)
        Spacer(modifier = Modifier.height(12.dp))
        data?.coinPacks?.forEach { pack ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), shape = RoundedCornerShape(16.dp)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🪙", fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${pack.coins} coins", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        pack.label?.let { Text(it, fontSize = 13.sp, color = GrayText) }
                        if (pack.totalCoins > pack.coins) Text("Total: ${pack.totalCoins} coins", fontSize = 12.sp, color = GrayText)
                    }
                    Button(onClick = { scope.launch { try { ApiClient.create<DragonApi>().buyCoinPack(mapOf("packId" to pack.id)) } catch (_: Exception) {} } },
                        shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Green500)) {
                        Text("₹${pack.priceInr.toInt()}", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text("✨ Secure Payment", fontWeight = FontWeight.Bold)
        Text("All transactions are secure and encrypted", fontSize = 13.sp, color = GrayText)
    }
}
