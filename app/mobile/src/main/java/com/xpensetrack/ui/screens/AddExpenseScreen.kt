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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
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
import com.xpensetrack.data.api.ExpenseApi
import com.xpensetrack.data.api.FriendApi
import com.xpensetrack.data.model.AddExpenseRequest
import com.xpensetrack.data.model.FriendItem
import com.xpensetrack.data.model.GroupItem
import com.xpensetrack.data.model.CreateGroupRequest
import com.xpensetrack.data.model.SplitAmongEntry
import com.xpensetrack.data.model.SplitExpenseRequest
import com.xpensetrack.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(navController: NavController) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("FOOD") }
    var showSplitSection by remember { mutableStateOf(false) }
    var friends by remember { mutableStateOf<List<FriendItem>>(emptyList()) }
    var selectedFriends by remember { mutableStateOf<Set<String>>(emptySet()) }
    var splitMode by remember { mutableStateOf("auto") }
    var manualAmounts by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    // Group state
    var showGroupDialog by remember { mutableStateOf(false) }
    var groupName by remember { mutableStateOf("") }
    var groups by remember { mutableStateOf<List<GroupItem>>(emptyList()) }
    var selectedGroupId by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    // Load friends and groups
    LaunchedEffect(showSplitSection) {
        if (showSplitSection) {
            scope.launch {
                try { friends = ApiClient.create<FriendApi>().getFriends() } catch (_: Exception) {}
                try { groups = ApiClient.create<FriendApi>().getGroups() } catch (_: Exception) {}
            }
        }
    }

    val categories = listOf(
        Triple("🍔", "Food", "FOOD"),
        Triple("💡", "Utilities", "UTILITIES"),
        Triple("🏠", "Rent", "RENT"),
        Triple("🚗", "Travel", "TRAVEL"),
        Triple("📦", "Misc", "MISC")
    )

    // Create Group Dialog
    if (showGroupDialog) {
        AlertDialog(
            onDismissRequest = { showGroupDialog = false },
            title = { Text("Create Group", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        label = { Text("Group Name") },
                        placeholder = { Text("e.g., Hostel Mates") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Members:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    if (friends.isEmpty()) {
                        Text("Add friends first to create a group", color = GrayText, fontSize = 13.sp)
                    }
                    friends.forEach { friend ->
                        val selected = friend.id in selectedFriends
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedFriends = if (selected) selectedFriends - friend.id
                                    else selectedFriends + friend.id
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = selected, onCheckedChange = {
                                selectedFriends = if (selected) selectedFriends - friend.id
                                else selectedFriends + friend.id
                            }, colors = CheckboxDefaults.colors(checkedColor = Purple700))
                            Spacer(Modifier.width(8.dp))
                            Box(
                                Modifier.size(36.dp).clip(CircleShape).background(PurpleLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(friend.fullName.take(1).uppercase(), fontWeight = FontWeight.Bold, color = Purple700)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(friend.fullName, fontSize = 15.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (groupName.isNotBlank() && selectedFriends.isNotEmpty()) {
                            scope.launch {
                                try {
                                    val newGroup = ApiClient.create<FriendApi>().createGroup(
                                        CreateGroupRequest(groupName, selectedFriends.toList())
                                    )
                                    groups = groups + newGroup
                                    selectedGroupId = newGroup.id
                                    showGroupDialog = false
                                    groupName = ""
                                } catch (_: Exception) { errorMsg = "Failed to create group" }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Purple700),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showGroupDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Add Expense", fontWeight = FontWeight.Bold)
                        Text("Money made clear", fontSize = 13.sp, color = White.copy(0.8f))
                    }
                },
                actions = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, null, tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Purple700, titleContentColor = White)
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            // Amount card
            Card(
                Modifier.fillMaxWidth(),
                RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Amount", fontSize = 14.sp, color = GrayText)
                    OutlinedTextField(
                        value = if (amount.isEmpty()) "" else "₹$amount",
                        onValueChange = { v -> amount = v.replace("₹", "").filter { it.isDigit() || it == '.' } },
                        placeholder = { Text("₹200", fontSize = 24.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Description card
            Card(
                Modifier.fillMaxWidth(),
                RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Description", fontSize = 14.sp, color = GrayText)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("e.g., Grocery Shopping") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = PurpleLight,
                            focusedBorderColor = Purple700
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Category card
            Card(
                Modifier.fillMaxWidth(),
                RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🏷️", fontSize = 18.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("Category", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(16.dp))
                    for (row in categories.chunked(3)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            row.forEach { (emoji, label, value) ->
                                val selected = selectedCategory == value
                                Column(
                                    Modifier
                                        .weight(1f)
                                        .border(
                                            if (selected) 2.dp else 1.dp,
                                            if (selected) Purple700 else GrayLight,
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (selected) Color(0xFFF3E5F5) else White)
                                        .clickable { selectedCategory = value }
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(emoji, fontSize = 28.sp)
                                    Spacer(Modifier.height(4.dp))
                                    Text(label, fontSize = 13.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Split with Friends card
            Card(
                Modifier.fillMaxWidth(),
                RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        Modifier.fillMaxWidth().clickable { showSplitSection = !showSplitSection },
                        Arrangement.SpaceBetween,
                        Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("👥", fontSize = 18.sp)
                            Spacer(Modifier.width(8.dp))
                            Text("Split with Friends", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        }
                        Text(if (showSplitSection) "−" else "+", fontSize = 24.sp, color = Purple700, fontWeight = FontWeight.Bold)
                    }

                    if (showSplitSection) {
                        Spacer(Modifier.height(12.dp))

                        // Existing Groups section
                        if (groups.isNotEmpty()) {
                            Text("Select a Group", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = GrayText)
                            Spacer(Modifier.height(8.dp))
                            for (group in groups) {
                                val isSelected = selectedGroupId == group.id
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .border(
                                            if (isSelected) 2.dp else 1.dp,
                                            if (isSelected) Purple700 else GrayLight,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) Color(0xFFF3E5F5) else White)
                                        .clickable {
                                            selectedGroupId = if (isSelected) null else group.id
                                            if (!isSelected) {
                                                // Auto-select group members as friends
                                                selectedFriends = group.memberIds.toSet()
                                            }
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📁", fontSize = 20.sp)
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(group.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                        Text("${group.memberIds.size} members", fontSize = 12.sp, color = GrayText)
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, null, tint = Purple700, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                            Spacer(Modifier.height(8.dp))
                        }

                        // Create Group button
                        OutlinedButton(
                            onClick = { showGroupDialog = true },
                            Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Purple700.copy(0.5f))
                        ) {
                            Text("➕ Create New Group", color = Purple700, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(Modifier.height(16.dp))

                        // Or pick individual friends
                        Text("Or select friends", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = GrayText)
                        Spacer(Modifier.height(8.dp))

                        if (friends.isEmpty()) {
                            Text("No friends yet. Add friends first!", color = GrayText, fontSize = 13.sp)
                        } else {
                            for (row in friends.chunked(2)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    row.forEach { friend ->
                                        val selected = friend.id in selectedFriends
                                        Column(
                                            Modifier
                                                .weight(1f)
                                                .border(
                                                    if (selected) 2.dp else 1.dp,
                                                    if (selected) Purple700 else GrayLight,
                                                    RoundedCornerShape(16.dp)
                                                )
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(if (selected) Color(0xFFF3E5F5) else White)
                                                .clickable {
                                                    selectedFriends = if (selected) selectedFriends - friend.id
                                                    else selectedFriends + friend.id
                                                    selectedGroupId = null // deselect group when picking individually
                                                }
                                                .padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                Modifier.size(48.dp).clip(CircleShape).background(PurpleLight),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    friend.fullName.take(1).uppercase(),
                                                    fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Purple700
                                                )
                                            }
                                            Spacer(Modifier.height(4.dp))
                                            Text(friend.fullName.split(" ").first(), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                            if (selected) {
                                                Icon(Icons.Default.Check, null, tint = Purple700, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                    repeat(2 - row.size) { Spacer(Modifier.weight(1f)) }
                                }
                                Spacer(Modifier.height(12.dp))
                            }
                        }

                        // Split mode buttons
                        if (selectedFriends.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = { splitMode = "auto" },
                                    Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (splitMode == "auto") Purple700 else GrayBg,
                                        contentColor = if (splitMode == "auto") White else DarkText
                                    )
                                ) { Text("Auto Split", fontWeight = FontWeight.Bold) }
                                Button(
                                    onClick = { splitMode = "manual" },
                                    Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (splitMode == "manual") Purple700 else GrayBg,
                                        contentColor = if (splitMode == "manual") White else DarkText
                                    )
                                ) { Text("Manual Split", fontWeight = FontWeight.Bold) }
                            }

                            // Show split preview
                            Spacer(Modifier.height(12.dp))
                            val totalAmt = amount.toDoubleOrNull() ?: 0.0
                            val splitCount = selectedFriends.size + 1 // +1 for current user
                            val perPerson = if (splitCount > 0) totalAmt / splitCount else 0.0

                            // Calculate "You" share for manual mode
                            val friendsManualTotal = if (splitMode == "manual") {
                                selectedFriends.sumOf { (manualAmounts[it]?.toDoubleOrNull() ?: 0.0) }
                            } else 0.0
                            val yourShare = if (splitMode == "manual") totalAmt - friendsManualTotal else perPerson

                            Card(
                                Modifier.fillMaxWidth(),
                                RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(
                                        if (splitMode == "auto") "Equal Split Preview" else "Enter amounts per person",
                                        fontWeight = FontWeight.SemiBold, fontSize = 14.sp
                                    )
                                    Spacer(Modifier.height(8.dp))

                                    // Each friend's share
                                    selectedFriends.forEach { friendId ->
                                        val friendName = friends.find { it.id == friendId }?.fullName?.split(" ")?.first() ?: friendId
                                        if (splitMode == "manual") {
                                            Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                                Text(friendName, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                                Spacer(Modifier.height(4.dp))
                                                OutlinedTextField(
                                                    value = manualAmounts[friendId] ?: "",
                                                    onValueChange = { v ->
                                                        manualAmounts = manualAmounts + (friendId to v.filter { it.isDigit() || it == '.' })
                                                    },
                                                    placeholder = { Text("Enter amount") },
                                                    prefix = { Text("₹") },
                                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 15.sp),
                                                    singleLine = true,
                                                    shape = RoundedCornerShape(10.dp),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        unfocusedBorderColor = PurpleLight,
                                                        focusedBorderColor = Purple700
                                                    )
                                                )
                                            }
                                        } else {
                                            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                                Text(friendName, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                                Text("₹${perPerson.toInt()}", fontWeight = FontWeight.Bold, color = Purple700)
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(8.dp))
                                    HorizontalDivider(color = Purple700.copy(0.2f))
                                    Spacer(Modifier.height(8.dp))

                                    // Your share
                                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                        Text("Your share", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            "₹${yourShare.toInt()}",
                                            fontWeight = FontWeight.Bold, fontSize = 16.sp,
                                            color = if (splitMode == "manual" && yourShare < 0) Color.Red else Purple700
                                        )
                                    }

                                    // Warning if manual amounts exceed total
                                    if (splitMode == "manual" && yourShare < 0) {
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            "⚠️ Friends' amounts exceed total by ₹${(-yourShare).toInt()}",
                                            color = Color.Red, fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Error message
            errorMsg?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = Color.Red, fontSize = 13.sp)
            }

            Spacer(Modifier.height(24.dp))

            // Add Expense button
            Button(
                onClick = {
                    if (isSubmitting) return@Button
                    isSubmitting = true
                    errorMsg = null
                    scope.launch {
                        try {
                            val totalAmt = amount.toDoubleOrNull() ?: 0.0

                            if (selectedFriends.isNotEmpty() && totalAmt > 0) {
                                // Calculate your share
                                val splitCount = selectedFriends.size + 1
                                val equalAmt = totalAmt / splitCount

                                val myShare = if (splitMode == "manual") {
                                    val friendsTotal = selectedFriends.sumOf { fid ->
                                        manualAmounts[fid]?.toDoubleOrNull() ?: equalAmt
                                    }
                                    totalAmt - friendsTotal
                                } else equalAmt

                                // 1. Add only YOUR share as your expense
                                ApiClient.create<ExpenseApi>().addExpense(
                                    AddExpenseRequest(
                                        myShare,
                                        description,
                                        selectedCategory,
                                        date = java.time.Instant.now().toString(),
                                        splitWithFriendIds = selectedFriends.toList()
                                    )
                                )

                                // 2. Create split record so friend balances update
                                val splitAmong = selectedFriends.map { friendId ->
                                    val amt = if (splitMode == "manual") {
                                        manualAmounts[friendId]?.toDoubleOrNull() ?: equalAmt
                                    } else equalAmt
                                    SplitAmongEntry(friendId, amt)
                                }

                                ApiClient.create<FriendApi>().splitExpense(
                                    SplitExpenseRequest(
                                        description = description.ifBlank { selectedCategory },
                                        totalAmount = totalAmt,
                                        groupId = selectedGroupId,
                                        splitAmong = splitAmong
                                    )
                                )
                            } else {
                                // No split — add full amount as your expense
                                ApiClient.create<ExpenseApi>().addExpense(
                                    AddExpenseRequest(
                                        totalAmt,
                                        description,
                                        selectedCategory,
                                        date = java.time.Instant.now().toString()
                                    )
                                )
                            }

                            navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                            navController.popBackStack()
                        } catch (e: Exception) {
                            errorMsg = "Failed: ${e.localizedMessage ?: e.toString()}"
                            android.util.Log.e("AddExpense", "Error adding expense", e)
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple700),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(Modifier.size(24.dp), color = White, strokeWidth = 2.dp)
                } else {
                    Text("Add Expense", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}
