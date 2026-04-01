package com.xpensetrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import com.xpensetrack.ui.theme.*

data class BottomNavItem(val label: String, val icon: ImageVector)

@Composable
fun MainScreen(navController: NavController) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        BottomNavItem("Home", Icons.Default.Home),
        BottomNavItem("Chat", Icons.Default.Refresh),
        BottomNavItem("Calendar", Icons.Default.DateRange),
        BottomNavItem("Profile", Icons.Default.Person)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Purple700) {
                tabs.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label, fontSize = androidx.compose.ui.unit.TextUnit.Unspecified) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Gold,
                            selectedTextColor = Gold,
                            unselectedIconColor = White,
                            unselectedTextColor = White,
                            indicatorColor = Purple500
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> DashboardTab(navController)
                1 -> ChatScreen(navController)
                2 -> CalendarTab(navController)
                3 -> ProfileTab(navController)
            }
        }
    }
}
