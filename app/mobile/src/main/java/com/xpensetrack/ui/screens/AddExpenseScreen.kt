package com.xpensetrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.xpensetrack.data.api.ApiClient
import com.xpensetrack.data.api.ExpenseApi
import com.xpensetrack.data.model.AddExpenseRequest
import com.xpensetrack.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(navController: NavController) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("FOOD") }
    val categories = listOf("🍔 Food" to "FOOD", "💡 Utilities" to "UTILITIES", "🏠 Rent" to "RENT", "🚗 Travel" to "TRAVEL", "📦 Misc" to "MISC")
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column { Text("Add Expense", fontWeight = FontWeight.Bold); Text("Money made clear", fontSize = 13.sp, color = GrayText) } },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.Close, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Purple700, titleContentColor = White, navigationIconContentColor = White)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(20.dp)) {
            // Amount
            Text("Amount", fontWeight = FontWeight.Medium, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = amount, onValueChange = { amount = it }, placeholder = { Text("₹200") },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)

            Spacer(modifier = Modifier.height(20.dp))
            Text("Description", fontWeight = FontWeight.Medium, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = description, onValueChange = { description = it },
                placeholder = { Text("e.g., Grocery Shopping") },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)

            Spacer(modifier = Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🏷️", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Category", fontWeight = FontWeight.Medium, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Category grid (3 + 2)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (row in categories.chunked(3)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { (label, value) ->
                            val selected = selectedCategory == value
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(2.dp, if (selected) Purple700 else GrayLight, RoundedCornerShape(16.dp))
                                    .background(if (selected) PurpleLight else White, RoundedCornerShape(16.dp))
                                    .clickable { selectedCategory = value }
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, fontSize = 14.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                        // Fill remaining space if row has < 3 items
                        repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            // Split with Friends
            OutlinedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("👥", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Split with Friends", fontWeight = FontWeight.Medium)
                    }
                    Icon(Icons.Default.Add, null, tint = Purple700)
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    scope.launch {
                        try {
                            ApiClient.create<ExpenseApi>().addExpense(
                                AddExpenseRequest(amount.toDoubleOrNull() ?: 0.0, description, selectedCategory))
                            // Signal dashboard to refresh
                            navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                            navController.popBackStack()
                        } catch (_: Exception) {}
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple700)
            ) { Text("Save Expense", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        }
    }
}
