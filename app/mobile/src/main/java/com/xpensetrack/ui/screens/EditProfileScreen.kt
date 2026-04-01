package com.xpensetrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.xpensetrack.data.api.ApiClient
import com.xpensetrack.data.api.ProfileApi
import com.xpensetrack.data.model.UpdateProfileRequest
import com.xpensetrack.data.model.UserProfile
import com.xpensetrack.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController) {
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var hostel by remember { mutableStateOf("") }
    var monthlyBudget by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val p = ApiClient.create<ProfileApi>().getProfile()
                profile = p
                fullName = p.fullName
                phone = p.phoneNumber
                address = p.address ?: ""
                hostel = p.hostel ?: ""
                monthlyBudget = p.monthlyBudget.toInt().toString()
            } catch (_: Exception) {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.Close, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Purple700, titleContentColor = White, navigationIconContentColor = White)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(20.dp)) {
            ProfileField("Full Name", fullName) { fullName = it }
            ProfileField("Phone Number", phone, KeyboardType.Phone) { phone = it }
            ProfileField("Address", address) { address = it }
            ProfileField("Hostel", hostel) { hostel = it }
            ProfileField("Monthly Budget (₹)", monthlyBudget, KeyboardType.Number) { monthlyBudget = it }

            message?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = if (it.contains("saved")) Green500 else Red500, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    saving = true; message = null
                    scope.launch {
                        try {
                            ApiClient.create<ProfileApi>().updateProfile(
                                UpdateProfileRequest(
                                    fullName = fullName.trim().ifEmpty { null },
                                    phoneNumber = phone.trim().ifEmpty { null },
                                    address = address.trim().ifEmpty { null },
                                    hostel = hostel.trim().ifEmpty { null },
                                    monthlyBudget = monthlyBudget.toDoubleOrNull()
                                )
                            )
                            message = "Profile saved!"
                        } catch (e: Exception) {
                            message = "Error: ${e.message}"
                        } finally { saving = false }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple700),
                enabled = !saving
            ) { Text(if (saving) "Saving..." else "Save Changes", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
fun ProfileField(label: String, value: String, keyboardType: KeyboardType = KeyboardType.Text, onValueChange: (String) -> Unit) {
    Text(label, fontWeight = FontWeight.Medium, fontSize = 16.sp)
    Spacer(modifier = Modifier.height(6.dp))
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
    Spacer(modifier = Modifier.height(14.dp))
}
