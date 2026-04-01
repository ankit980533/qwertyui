package com.xpensetrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.xpensetrack.data.api.ApiClient
import com.xpensetrack.data.api.ChatApi
import com.xpensetrack.data.model.ChatMessageItem
import com.xpensetrack.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController) {
    var messages by remember { mutableStateOf<List<ChatMessageItem>>(emptyList()) }
    var input by remember { mutableStateOf("") }
    val quickActions = listOf("Log Expense", "Money Tips", "Show Budget")
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try { messages = ApiClient.create<ChatApi>().getHistory() } catch (_: Exception) {}
            if (messages.isEmpty()) {
                messages = listOf(ChatMessageItem("0", "ASSISTANT",
                    "Hi! I'm your Hostel Life assistant 👋 I can help you log expenses, track savings, and give money-saving tips. What would you like to do?", ""))
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Box(modifier = Modifier.fillMaxWidth().background(Purple700).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🐉", fontSize = 32.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("AI Assistant", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = White)
                    Text("Always here to help", fontSize = 13.sp, color = White.copy(alpha = 0.8f))
                }
            }
        }

        // Messages
        LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 16.dp, vertical = 8.dp)) {
            items(messages) { msg ->
                val isUser = msg.role == "USER"
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start) {
                    Box(modifier = Modifier.widthIn(max = 280.dp)
                        .background(if (isUser) Purple700 else GrayBg, RoundedCornerShape(16.dp))
                        .padding(12.dp)) {
                        Text(msg.content, color = if (isUser) White else DarkText, fontSize = 14.sp)
                    }
                }
            }
        }

        // Quick actions
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            quickActions.forEach { action ->
                OutlinedButton(onClick = {
                    input = action
                    scope.launch {
                        try {
                            val res = ApiClient.create<ChatApi>().chat(mapOf("message" to action))
                            messages = messages + ChatMessageItem("", "USER", action, "") + ChatMessageItem("", "ASSISTANT", res.reply, "")
                        } catch (_: Exception) {}
                    }
                }, shape = RoundedCornerShape(20.dp)) { Text(action, fontSize = 13.sp) }
            }
        }

        // Input
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = input, onValueChange = { input = it },
                placeholder = { Text("Ask me anything....") },
                modifier = Modifier.weight(1f), shape = RoundedCornerShape(24.dp), singleLine = true)
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = {
                if (input.isNotBlank()) {
                    val msg = input; input = ""
                    scope.launch {
                        try {
                            val res = ApiClient.create<ChatApi>().chat(mapOf("message" to msg))
                            messages = messages + ChatMessageItem("", "USER", msg, "") + ChatMessageItem("", "ASSISTANT", res.reply, "")
                        } catch (_: Exception) {}
                    }
                }
            }, modifier = Modifier.size(48.dp).background(Purple700, CircleShape)) {
                Icon(Icons.Default.Send, null, tint = White)
            }
        }
    }
}
