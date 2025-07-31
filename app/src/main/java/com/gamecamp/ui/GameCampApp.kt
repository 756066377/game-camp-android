package com.gamecamp.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gamecamp.ui.components.BottomNavigationBar
import com.gamecamp.ui.screens.DashboardScreen
import com.gamecamp.ui.screens.EnhancedDriverScreen
import com.gamecamp.ui.screens.GameAssistantScreen
import com.gamecamp.ui.theme.WarmNeumorphismColors

/**
 * 应用的主UI入口点。
 * 它现在不再需要传递任何权限请求函数。
 */
@Composable
fun GameCampApp() {
    val navController = rememberNavController()
    
    // 使用Surface确保背景色正确应用
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = WarmNeumorphismColors.CreamWhite
    ) {
        Scaffold(
            bottomBar = { BottomNavigationBar(navController) },
            containerColor = WarmNeumorphismColors.CreamWhite,
            modifier = Modifier.statusBarsPadding()
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = "dashboard",
                modifier = Modifier.padding(padding),
                enterTransition = { 
                    fadeIn(animationSpec = tween(300)) + 
                    slideInHorizontally(initialOffsetX = { 300 }) 
                },
                exitTransition = { 
                    fadeOut(animationSpec = tween(300)) + 
                    slideOutHorizontally(targetOffsetX = { -300 }) 
                }
            ) {
                composable("dashboard") { DashboardScreen() }
                composable("driver") { EnhancedDriverScreen() }
                composable("game_assistant") { GameAssistantScreen() }
            }
        }
    }
}
