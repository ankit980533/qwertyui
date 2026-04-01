package com.xpensetrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.xpensetrack.data.TokenStore
import com.xpensetrack.data.api.ApiClient
import com.xpensetrack.navigation.Routes
import com.xpensetrack.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        delay(1500)
        // Check for saved token
        val savedToken = TokenStore.getToken(context)
        if (savedToken != null) {
            ApiClient.token = savedToken
            navController.navigate(Routes.MAIN) { popUpTo(Routes.SPLASH) { inclusive = true } }
        } else {
            navController.navigate(Routes.ONBOARDING) { popUpTo(Routes.SPLASH) { inclusive = true } }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Purple200, White))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(Purple700, shape = RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("₹", fontSize = 60.sp, color = Gold, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(32.dp))
            Row {
                Text("Xpense", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Purple700)
                Text("Track", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Gold)
            }
            Text("Master Your Money", fontSize = 18.sp, color = GrayText, textAlign = TextAlign.Center)
        }
    }
}
