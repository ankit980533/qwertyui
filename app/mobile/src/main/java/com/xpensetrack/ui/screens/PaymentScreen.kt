package com.xpensetrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.xpensetrack.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(navController: NavController) {
    var selectedMethod by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column { Text("Payment", fontWeight = FontWeight.Bold); Text("Settle transactions instantly", fontSize = 13.sp, color = GrayText) } },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.Close, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Purple700, titleContentColor = White, navigationIconContentColor = White)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            // Pay to header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Pay Riya Thakur ✅", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("🔒 100% Secure", fontSize = 13.sp, color = Green500,
                    modifier = Modifier.background(GreenLight, RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 4.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Total Amount
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Amount", fontWeight = FontWeight.Medium)
                    Text("₹400", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Cashback banner
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = GreenLight)) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("5% Cashback", fontWeight = FontWeight.Bold, color = Green500)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Claim now with payment offers", fontSize = 13.sp, color = GrayText)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Payment methods
            listOf(
                Triple("UPI", "Pay by any UPI app", "Get upto 30% cashback • 3 offers available"),
                Triple("Credit/ Debit/ ATM Card", "Add and secure cards as per RBI guidelines", "Get upto 5% cashback • 2 offers available"),
                Triple("Settled", "Settled outside with cash or online payment options.", null)
            ).forEach { (title, desc, offer) ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { selectedMethod = title },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (selectedMethod == title) PurpleLight else White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(when (title) { "UPI" -> "📱"; "Settled" -> "✨"; else -> "💳" }, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Icon(Icons.Default.KeyboardArrowDown, null)
                        }
                        Text(desc, fontSize = 13.sp, color = GrayText)
                        offer?.let { Text(it, fontSize = 13.sp, color = Green500, fontWeight = FontWeight.Medium) }
                    }
                }
            }
        }
    }
}
