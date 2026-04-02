package com.xpensetrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.xpensetrack.data.api.PiggyBankApi
import com.xpensetrack.data.model.PiggyBankOverview
import com.xpensetrack.data.model.CreatePiggyBankRequest
import com.xpensetrack.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PiggyBankScreen(navController: NavController) {
    var data by remember { mutableStateOf<PiggyBankOverview?>(null) }
    var showCreateGoal by remember { mutableStateOf(false) }
    var goalName by remember { mutableStateOf("") }
    var goalAmount by remember { mutableStateOf("") }
    var goalDeadline by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { scope.launch { try { data = ApiClient.create<PiggyBankApi>().getOverview() } catch (_: Exception) {} } }

    val savings = data?.monthlySavings ?: 0.0
    val isOverrun = savings < 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🐷", fontSize = 28.sp)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Your Virtual Piggy Bank", fontWeight = FontWeight.Bold)
                            Text("Save Smart", fontSize = 13.sp, color = White.copy(0.8f))
                        }
                    }
                },
                actions = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.Close, null, tint = White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Purple700, titleContentColor = White)
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)) {
            // Money bag image placeholder
            Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                Text("💰", fontSize = 100.sp)
            }

            Spacer(Modifier.height(16.dp))

            // Your Savings card
            var showQuickSaveDialog by remember { mutableStateOf(false) }
            var quickSaveAmount by remember { mutableStateOf("") }
            var selectedGoalForQuickSave by remember { mutableStateOf<String?>(null) }

            if (showQuickSaveDialog) {
                AlertDialog(
                    onDismissRequest = { showQuickSaveDialog = false },
                    title = { Text("Add Savings", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text("Available savings: ₹${savings.toInt()}", fontSize = 13.sp, color = if (savings > 0) Green500 else GrayText)
                            Spacer(Modifier.height(12.dp))
                            
                            // Select goal dropdown
                            val goals = data?.recentGoals ?: emptyList()
                            if (goals.isNotEmpty()) {
                                Text("Select Goal:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(8.dp))
                                goals.forEach { goal ->
                                    val isSelected = selectedGoalForQuickSave == goal.id
                                    Card(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable { selectedGoalForQuickSave = goal.id },
                                        RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) Color(0xFFF3E5F5) else GrayBg
                                        ),
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Purple700) else null
                                    ) {
                                        Row(Modifier.padding(12.dp).fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                            Column {
                                                Text(goal.goalName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                                Text("₹${goal.savedAmount.toInt()} / ₹${goal.targetAmount.toInt()}", fontSize = 12.sp, color = GrayText)
                                            }
                                            if (isSelected) {
                                                Icon(Icons.Default.Check, null, tint = Purple700, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                            } else {
                                Text("Create a goal first to add savings!", fontSize = 13.sp, color = GrayText)
                                Spacer(Modifier.height(12.dp))
                            }
                            
                            OutlinedTextField(
                                value = quickSaveAmount,
                                onValueChange = { quickSaveAmount = it.filter { c -> c.isDigit() || c == '.' } },
                                label = { Text("Amount (₹)") },
                                placeholder = { Text("e.g., 500") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                enabled = selectedGoalForQuickSave != null
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                selectedGoalForQuickSave?.let { goalId ->
                                    scope.launch {
                                        try {
                                            ApiClient.create<PiggyBankApi>().addSavings(
                                                goalId, mapOf("amount" to (quickSaveAmount.toDoubleOrNull() ?: 0.0))
                                            )
                                            showQuickSaveDialog = false
                                            quickSaveAmount = ""
                                            selectedGoalForQuickSave = null
                                            data = ApiClient.create<PiggyBankApi>().getOverview()
                                        } catch (_: Exception) {}
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Green500),
                            shape = RoundedCornerShape(12.dp),
                            enabled = selectedGoalForQuickSave != null && quickSaveAmount.isNotBlank()
                        ) { Text("Save") }
                    },
                    dismissButton = { TextButton(onClick = { showQuickSaveDialog = false; selectedGoalForQuickSave = null }) { Text("Cancel") } }
                )
            }

            Card(
                Modifier.fillMaxWidth(),
                RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🏦", fontSize = 20.sp)
                            Spacer(Modifier.width(8.dp))
                            Text("Your Savings", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                    Text("This month's savings towards your goals", fontSize = 13.sp, color = GrayText)
                    Spacer(Modifier.height(12.dp))

                    if (isOverrun) {
                        // Negative savings - RED overrun
                        Text(
                            "-₹${kotlin.math.abs(savings.toInt())}",
                            fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Red500
                        )
                        Spacer(Modifier.height(8.dp))
                        // Full red progress bar
                        Box(
                            Modifier.fillMaxWidth().height(10.dp)
                                .clip(RoundedCornerShape(5.dp)).background(Red500)
                        )
                        Spacer(Modifier.height(8.dp))
                        Card(
                            Modifier.fillMaxWidth(),
                            RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Red500)
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("⚠️", fontSize = 18.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Budget overrun by ₹${kotlin.math.abs(savings.toInt())}! You've spent more than your monthly budget.",
                                    fontSize = 13.sp, color = Red500, fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        // Positive savings - normal display
                        Text(
                            "₹${savings.toInt()}",
                            fontSize = 30.sp, fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        
                        // Show progress bar only if goals exist
                        val hasGoals = (data?.recentGoals?.size ?: 0) > 0
                        if (hasGoals) {
                            Box(
                                Modifier.fillMaxWidth().height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)).background(GrayLight)
                            ) {
                                Box(
                                    Modifier.fillMaxHeight()
                                        .fillMaxWidth(((data?.savingsProgressPercent ?: 0.0) / 100).toFloat().coerceIn(0f, 1f))
                                        .clip(RoundedCornerShape(5.dp)).background(Gold)
                                )
                            }
                            Row(Modifier.fillMaxWidth(), Arrangement.End) {
                                Text("₹${(data?.savingsTarget ?: 0.0).toInt()}", fontSize = 13.sp, color = GrayText)
                            }
                        } else {
                            // No goals set - show message
                            Card(
                                Modifier.fillMaxWidth(),
                                RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(0.5f))
                            ) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("💡", fontSize = 18.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "No savings goal set. Create a goal below to track your progress!",
                                        fontSize = 13.sp, color = DarkText, fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Error message
            errorMsg?.let {
                Card(
                    Modifier.fillMaxWidth(),
                    RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(0.5f))
                ) {
                    Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("⚠️", fontSize = 18.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(it, fontSize = 13.sp, color = Color.Red, modifier = Modifier.weight(1f))
                        IconButton(onClick = { errorMsg = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Recent Goals header
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Recent Goals", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(Modifier.width(8.dp))
                    Text("🔗", fontSize = 18.sp)
                }
                Button(
                    onClick = { showCreateGoal = !showCreateGoal },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Purple700),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) { Text(if (showCreateGoal) "Cancel" else "+ New Goal", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
            }
            Spacer(Modifier.height(12.dp))

            // Create Goal form
            if (showCreateGoal) {
                Card(
                    Modifier.fillMaxWidth(),
                    RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("🎯 Create New Goal", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = goalName, onValueChange = { goalName = it },
                            placeholder = { Text("e.g., Top for Fresher's") },
                            label = { Text("Goal Name") },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = goalAmount, onValueChange = { goalAmount = it.filter { c -> c.isDigit() || c == '.' } },
                            placeholder = { Text("e.g., 5000") },
                            label = { Text("Target Amount (₹)") },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                        )
                        Spacer(Modifier.height(10.dp))
                        
                        // Deadline with clickable date picker
                        var showDatePicker by remember { mutableStateOf(false) }
                        OutlinedTextField(
                            value = goalDeadline,
                            onValueChange = { },
                            placeholder = { Text("Select deadline") },
                            label = { Text("Deadline") },
                            modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = DarkText,
                                disabledBorderColor = GrayLight,
                                disabledLabelColor = GrayText
                            ),
                            trailingIcon = { Text("📅", fontSize = 18.sp) }
                        )
                        
                        if (showDatePicker) {
                            val datePickerState = rememberDatePickerState()
                            DatePickerDialog(
                                onDismissRequest = { showDatePicker = false },
                                confirmButton = {
                                    TextButton(onClick = {
                                        datePickerState.selectedDateMillis?.let { millis ->
                                            val date = java.time.Instant.ofEpochMilli(millis)
                                                .atZone(java.time.ZoneOffset.UTC).toLocalDate()
                                            goalDeadline = date.toString()
                                        }
                                        showDatePicker = false
                                    }) { Text("OK") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                                }
                            ) {
                                DatePicker(state = datePickerState)
                            }
                        }
                        
                        Spacer(Modifier.height(14.dp))
                        
                        var createError by remember { mutableStateOf<String?>(null) }
                        var isCreating by remember { mutableStateOf(false) }
                        
                        createError?.let { error ->
                            Text(error, color = Red500, fontSize = 12.sp)
                            Spacer(Modifier.height(8.dp))
                        }
                        
                        Button(
                            onClick = {
                                if (goalName.isNotBlank() && goalAmount.isNotBlank() && goalDeadline.isNotBlank()) {
                                    isCreating = true
                                    createError = null
                                    scope.launch {
                                        try {
                                            ApiClient.create<PiggyBankApi>().create(
                                                CreatePiggyBankRequest(
                                                    goalName = goalName,
                                                    targetAmount = goalAmount.toDoubleOrNull() ?: 0.0,
                                                    deadline = goalDeadline
                                                )
                                            )
                                            goalName = ""; goalAmount = ""; goalDeadline = ""; showCreateGoal = false
                                            data = ApiClient.create<PiggyBankApi>().getOverview()
                                        } catch (e: Exception) {
                                            createError = "Failed to create goal: ${e.message}"
                                        } finally {
                                            isCreating = false
                                        }
                                    }
                                }
                            },
                            Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Green500),
                            enabled = !isCreating && goalName.isNotBlank() && goalAmount.isNotBlank() && goalDeadline.isNotBlank()
                        ) {
                            if (isCreating) {
                                CircularProgressIndicator(Modifier.size(24.dp), color = White, strokeWidth = 2.dp)
                            } else {
                                Text("Create Goal", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Goals grid - 2 columns like Figma
            val goals = data?.recentGoals ?: emptyList()
            if (goals.isEmpty()) {
                Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = GrayBg)) {
                    Text("No goals yet. Create one to start saving!", Modifier.padding(20.dp), color = GrayText)
                }
            } else {
                for (rowGoals in goals.chunked(2)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        rowGoals.forEach { goal ->
                            val progressColor = if (goal.progressPercent > 50) Green500 else Purple700
                            val isComplete = goal.progressPercent >= 100

                            Card(
                                Modifier.weight(1f),
                                RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isComplete) Color(0xFFE8F5E9) 
                                                    else if (goal.progressPercent > 50) Color(0xFFF3E5F5) 
                                                    else Color(0xFFFFF8E1)
                                )
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(if (isComplete) "✅" else "🎯", fontSize = 32.sp)
                                    Spacer(Modifier.height(4.dp))
                                    Text(goal.goalName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("₹${goal.savedAmount.toInt()} / ₹${goal.targetAmount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Before: ${goal.deadline}", fontSize = 11.sp, color = GrayText)
                                    if (goal.dailySavingNeeded > 0 && !isComplete) {
                                        Text("Save ₹${goal.dailySavingNeeded.toInt()}/day", fontSize = 11.sp, color = Purple700)
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Box(
                                        Modifier.fillMaxWidth().height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)).background(GrayLight)
                                    ) {
                                        Box(
                                            Modifier.fillMaxHeight()
                                                .fillMaxWidth((goal.progressPercent / 100).toFloat().coerceIn(0f, 1f))
                                                .clip(RoundedCornerShape(3.dp)).background(progressColor)
                                        )
                                    }
                                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                        Text("${goal.progressPercent.toInt()}%", fontSize = 11.sp, color = GrayText)
                                        if (isComplete) {
                                            Text("Complete!", fontSize = 11.sp, color = Green500, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    
                                    // Complete button for goals that are not yet marked complete
                                    if (!isComplete && goal.progressPercent >= 90) {
                                        Spacer(Modifier.height(6.dp))
                                        val totalSavings = data?.monthlySavings ?: 0.0
                                        val needed = goal.targetAmount - goal.savedAmount
                                        val canComplete = totalSavings >= needed
                                        
                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    try {
                                                        ApiClient.create<PiggyBankApi>().markComplete(goal.id)
                                                        data = ApiClient.create<PiggyBankApi>().getOverview()
                                                    } catch (e: Exception) {
                                                        errorMsg = e.message ?: "Failed to complete goal"
                                                    }
                                                }
                                            },
                                            Modifier.fillMaxWidth().height(32.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (canComplete) Green500 else GrayLight,
                                                contentColor = if (canComplete) White else GrayText
                                            ),
                                            contentPadding = PaddingValues(0.dp),
                                            enabled = canComplete
                                        ) {
                                            Text(
                                                if (canComplete) "Mark Complete" else "Insufficient Savings",
                                                fontSize = 10.sp, fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        if (rowGoals.size < 2) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            // AI chatbot suggestion
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("ℹ️", fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Don't have anything in mind? Try asking our AI chatbot for the best usage of your saved money!",
                    fontSize = 13.sp, color = GrayText
                )
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}
