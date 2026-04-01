package com.xpensetrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.xpensetrack.data.api.ApiClient
import com.xpensetrack.data.api.AuthApi
import com.xpensetrack.data.model.LoginRequest
import com.xpensetrack.navigation.Routes
import com.xpensetrack.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Purple200, White)))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            // Logo
            Box(
                modifier = Modifier.size(80.dp)
                    .background(Purple700, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) { Text("₹", fontSize = 36.sp, color = Gold, fontWeight = FontWeight.Bold) }
            Spacer(modifier = Modifier.height(12.dp))
            Row {
                Text("Xpense", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Purple700)
                Text("Track", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Gold)
            }
            Text("Master Your Money", fontSize = 14.sp, color = GrayText)
            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Email", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email, onValueChange = { email = it },
                        placeholder = { Text("Enter your email") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Password", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password, onValueChange = { password = it },
                        placeholder = { Text("*******") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    error?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, color = Red500, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            loading = true; error = null
                            scope.launch {
                                try {
                                    val res = ApiClient.create<AuthApi>().login(LoginRequest(email.trim(), password))
                                    ApiClient.token = res.token
                                    com.xpensetrack.data.TokenStore.saveToken(context, res.token)
                                    navController.navigate(Routes.MAIN) { popUpTo(0) }
                                } catch (e: retrofit2.HttpException) {
                                    error = e.response()?.errorBody()?.string() ?: "Login failed (${e.code()})"
                                } catch (e: Exception) {
                                    error = e.message ?: "Connection failed"
                                } finally { loading = false }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Purple700),
                        enabled = !loading
                    ) { Text(if (loading) "Loading..." else "Login", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Text("Don't have an account? ", color = GrayText)
                        Text("Sign up", color = Purple700, fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { navController.navigate(Routes.SIGNUP) })
                    }
                }
            }
        }
    }
}
