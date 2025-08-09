package com.gamecamp.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gamecamp.constants.DriverConstants
import com.gamecamp.ui.components.AssistantSettingsDialog
import com.gamecamp.ui.components.DriverResetConfirmDialog
import com.gamecamp.ui.components.InfoCard
import com.gamecamp.ui.components.SimpleInteractiveCard
import com.gamecamp.ui.components.TerminalDialog
import com.gamecamp.ui.state.DriverUiState
import com.gamecamp.ui.theme.WarmOrange
import com.gamecamp.viewmodel.DriverViewModel
import kotlinx.coroutines.delay

/**
 * 带动画效果的驱动管理页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleAnimatedDriverScreen(
    viewModel: DriverViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val terminalLogs by viewModel.terminalLogs.collectAsState()
    val showTerminalDialog by viewModel.showTerminalDialog.collectAsState()
    val terminalCompleted by viewModel.terminalCompleted.collectAsState()
    val assistantSettings by viewModel.assistantSettings.collectAsState()

    // 辅助功能设置对话框状态
    var showAssistantDialog by remember { mutableStateOf(false) }

    // 页面进入动画状态
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { it / 3 },
                animationSpec = tween(600, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(600))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(
                    items = listOf("assistant", "driver", "spacer"),
                    key = { _, item -> item }
                ) { index, item ->
                    // 交错动画效果
                    var itemVisible by remember { mutableStateOf(false) }
                    
                    LaunchedEffect(isVisible) {
                        if (isVisible) {
                            delay(index * 150L) // 每个项目延迟150ms
                            itemVisible = true
                        }
                    }
                    
                    AnimatedVisibility(
                        visible = itemVisible,
                        enter = slideInHorizontally(
                            initialOffsetX = { if (index % 2 == 0) -it else it },
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = FastOutSlowInEasing
                            )
                        ) + fadeIn(
                            animationSpec = tween(400, delayMillis = 100)
                        )
                    ) {
                        when (item) {
                            "assistant" -> {
                                AnimatedAssistantCard(
                                    isDriverInstalled = uiState is DriverUiState.InstallSuccess,
                                    onLaunchClick = { showAssistantDialog = true }
                                )
                            }
                            "driver" -> {
                            AnimatedDriverInstallCard(
                                uiState = uiState,
                                onInstallClick = { driverName ->
                                    viewModel.onDriverSelected(driverName)
                                    viewModel.onInstallClick()
                                },
                                onResetClick = { driverName ->
                                    viewModel.onResetClick()
                                }
                            )
                            }
                            "spacer" -> {
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                    }
                }
            }
        }

        // 确认对话框
        val currentState = uiState
        if (currentState is DriverUiState.ShowConfirmDialog) {
            DriverResetConfirmDialog(
                driverName = currentState.installedDriver,
                onConfirm = { viewModel.onConfirmReset() },
                onCancel = { viewModel.onCancelReset() }
            )
        }

        // 辅助功能设置对话框
        if (showAssistantDialog) {
            AssistantSettingsDialog(
                currentSettings = assistantSettings,
                onConfirm = { settings ->
                    showAssistantDialog = false
                    viewModel.launchAssistant(settings)
                },
                onDismiss = {
                    showAssistantDialog = false
                }
            )
        }

        // 终端对话框
        if (showTerminalDialog) {
            TerminalDialog(
                logs = terminalLogs,
                isCompleted = terminalCompleted,
                onDismiss = { viewModel.closeTerminalDialog() }
            )
        }
    }
}

/**
 * 带动画的辅助功能卡片
 */
@Composable
private fun AnimatedAssistantCard(
    isDriverInstalled: Boolean,
    onLaunchClick: () -> Unit
) {
    InfoCard(
        title = "启动辅助功能",
        icon = Icons.Default.PlayArrow
    ) {
        Column {
            // 状态指示动画
            val statusColor by animateColorAsState(
                targetValue = if (isDriverInstalled) Color(0xFF4CAF50) else Color(0xFFFF9800),
                animationSpec = tween(300),
                label = "status_color"
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = if (isDriverInstalled) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = "状态",
                    tint = statusColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isDriverInstalled) "驱动已安装，功能可用" else "等待驱动安装",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isDriverInstalled)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // 功能描述
            Text(
                text = if (isDriverInstalled)
                    "驱动已成功安装，现在可以启动游戏辅助功能。点击下方按钮进行详细配置。"
                else
                    "请先安装驱动程序，安装完成后即可使用游戏辅助功能。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // 启动按钮
            Button(
                onClick = onLaunchClick,
                enabled = isDriverInstalled,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WarmOrange,
                    disabledContainerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "启动",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isDriverInstalled) "启动辅助功能" else "等待驱动安装",
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * 带动画的驱动安装卡片
 */
@Composable
private fun AnimatedDriverInstallCard(
    uiState: DriverUiState,
    onInstallClick: (String) -> Unit,
    onResetClick: (String) -> Unit
) {
    InfoCard(
        title = "驱动刷入",
        icon = Icons.Default.Build
    ) {
        Column {
            // 状态显示动画
            AnimatedContent(
                targetState = uiState,
                transitionSpec = {
                    slideInVertically { it / 2 } + fadeIn() togetherWith
                    slideOutVertically { -it / 2 } + fadeOut()
                },
                label = "status_animation"
            ) { state ->
                when (state) {
                    is DriverUiState.Installing -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = WarmOrange
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "正在安装驱动...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    is DriverUiState.InstallSuccess -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "成功",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "驱动安装成功",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    is DriverUiState.InstallFailure -> {
                        Column(modifier = Modifier.padding(bottom = 16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "错误",
                                    tint = Color(0xFFF44336),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "安装失败",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFFF44336),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (state.errorMessage.isNotEmpty()) {
                                Text(
                                    text = state.errorMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                    else -> {
                        Text(
                            text = "选择要安装的驱动程序",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                }
            }

            // 驱动选项列表
            DriverConstants.SUPPORTED_DRIVERS.forEachIndexed { index, driverName ->
                val isInstalled = uiState is DriverUiState.InstallSuccess && uiState.installedDriver == driverName
                val isLoading = uiState is DriverUiState.Installing && uiState.selectedDriver == driverName

                // 选中状态动画
                val cardColor by animateColorAsState(
                    targetValue = if (isInstalled)
                        Color(0xFF4CAF50).copy(alpha = 0.1f)
                    else
                        MaterialTheme.colorScheme.surface,
                    animationSpec = tween(300),
                    label = "card_color_$index"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = cardColor
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = driverName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            // 状态文本动画
                            AnimatedContent(
                                targetState = isInstalled,
                                transitionSpec = {
                                    fadeIn() togetherWith fadeOut()
                                },
                                label = "status_text_$index"
                            ) { installed ->
                                Text(
                                    text = if (installed) "已安装" else "未安装",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (installed)
                                        Color(0xFF4CAF50)
                                    else
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Row {
                            if (isInstalled) {
                                // 重置按钮
                                OutlinedButton(
                                    onClick = { onResetClick(driverName) },
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "重置",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("重置")
                                }
                            } else {
                                // 安装按钮
                                Button(
                                    onClick = { onInstallClick(driverName) },
                                    enabled = !isLoading && uiState !is DriverUiState.Installing,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = WarmOrange
                                    )
                                ) {
                                    AnimatedContent(
                                        targetState = isLoading,
                                        transitionSpec = {
                                            fadeIn() togetherWith fadeOut()
                                        },
                                        label = "button_content_$index"
                                    ) { loading ->
                                        if (loading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                        } else {
                                            Row {
                                                Icon(
                                                    imageVector = Icons.Default.Download,
                                                    contentDescription = "安装",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("安装")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}