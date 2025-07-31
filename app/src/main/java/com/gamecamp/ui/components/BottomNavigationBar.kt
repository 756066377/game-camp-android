package com.gamecamp.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * 底部导航栏组件
 * 提供主页、驱动管理和游戏辅助页面之间的导航功能
 */
@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("dashboard", "数据看板", Icons.Default.Home),
        BottomNavItem("driver", "驱动管理", Icons.Default.Build),
        BottomNavItem("game_assistant", "游戏辅助", Icons.Default.PlayArrow)
    )
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { 
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        // 避免重复导航到同一页面
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

/**
 * 底部导航项数据类
 */
data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

/**
 * 页面路由枚举
 */
enum class Screen(val route: String, val title: String, val icon: ImageVector) {
    Dashboard("dashboard", "数据看板", Icons.Default.Home),
    Driver("driver", "驱动管理", Icons.Default.Build),
    GameAssistant("game_assistant", "游戏辅助", Icons.Default.PlayArrow)
}