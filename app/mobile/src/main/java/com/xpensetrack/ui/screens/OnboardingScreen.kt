package com.xpensetrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.xpensetrack.navigation.Routes
import com.xpensetrack.ui.theme.*

@Composable
fun OnboardingScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Purple200, White)))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Piggy bank icon placeholder
            Text("🐷", fontSize = 100.sp)
            Spacer(modifier = Modifier.height(32.dp))
            Text("Save Smarter with Piggy", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DarkText)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Set your goal, stay consistent and achieve it", fontSize = 16.sp, color = GrayText)
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = { navController.navigate(Routes.LOGIN) { popUpTo(Routes.ONBOARDING) { inclusive = true } } },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple700)
            ) {
                Text("Get Started", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
