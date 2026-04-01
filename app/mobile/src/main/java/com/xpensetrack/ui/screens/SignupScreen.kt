package com.xpensetrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.xpensetrack.data.api.ApiClient
import com.xpensetrack.data.api.AuthApi
import com.xpensetrack.data.model.SignupRequest
import com.xpensetrack.navigation.Routes
import com.xpensetrack.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SignupScreen(navController: NavController) {
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Purple200, White)))) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    listOf(
                        "Full Name" to fullName, "Phone number" to phone, "Email" to email
                    ).forEach { (label, value) ->
                        Text(label, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = value,
                            onValueChange = { v ->
                                when (label) { "Full Name" -> fullName = v; "Phone number" -> phone = v; "Email" -> email = v }
                            },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Text("Password", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = password, onValueChange = { password = it },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Confirm Password", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = termsAccepted, onCheckedChange = { termsAccepted = it },
                            colors = CheckboxDefaults.colors(checkedColor = Purple700))
                        Text("I agree to the ", fontSize = 13.sp)
                        Text("Terms and Conditions", color = Purple700, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    error?.let { Text(it, color = Red500, fontSize = 13.sp); Spacer(modifier = Modifier.height(4.dp)) }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            loading = true; error = null
                            scope.launch {
                                try {
                                    val res = ApiClient.create<AuthApi>().signup(
                                        SignupRequest(fullName.trim(), phone.trim(), email.trim(), password, confirmPassword, termsAccepted))
                                    ApiClient.token = res.token
                                    com.xpensetrack.data.TokenStore.saveToken(context, res.token)
                                    navController.navigate(Routes.MAIN) { popUpTo(0) }
                                } catch (e: retrofit2.HttpException) {
                                    val body = e.response()?.errorBody()?.string()
                                    error = body ?: "Signup failed (${e.code()})"
                                } catch (e: Exception) { error = e.message ?: "Signup failed" }
                                finally { loading = false }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Purple700),
                        enabled = !loading
                    ) { Text("Create Account", fontSize = 18.sp, fontWeight = FontWeight.Bold) }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Text("----------or----------", color = GrayText, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { /* Google OAuth */ },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) { Text("Continue with Google", fontSize = 16.sp, color = DarkText) }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Text("Already a member? ", color = GrayText)
                        Text("Login", color = Purple700, fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { navController.popBackStack() })
                    }
                }
            }
        }
    }
}
