package com.gamecamp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gamecamp.constants.DriverConstants
import com.gamecamp.ui.animation.AnimationUtils
import com.gamecamp.ui.animation.ShakeController
import com.gamecamp.ui.components.*
import com.gamecamp.ui.state.DriverUiState
import com.gamecamp.ui.state.errorMessage
import com.gamecamp.ui.state.isDriverInstalled
import com.gamecamp.ui.state.isLoading
import com.gamecamp.ui.state.selectedDriver
import com.gamecamp.ui.state.showConfirmDialog
import com.gamecamp.ui.theme.WarmOrange
import com.gamecamp.viewmodel.DriverViewModel

/**
 * 集成动效的驱动管理页面
 * 展示如何在现有页面中应用动效系统
 */
@Composable
fun AnimatedDriverScreen(
    viewModel: DriverViewModel = hiltViewModel()
) {
    // 状态收集
    val uiState by viewModel.uiState.collectAsState()
    val terminalLogs by viewModel.terminalLogs.collectAsState()
    val showTerminalDialog by viewModel.showTerminalDialog.collectAsState()
    val terminalCompleted by viewModel.terminalCompleted.collectAsState()
    val assistantSettings by viewModel.assistantSettings.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // 动画控制器
    val shakeController = AnimationUtils.rememberShakeController()
    var showAssistantDialog by remember { mutableStateOf(false) }
    
    // 错误处理 - 带抖动效果
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            shakeController.shake(iterations = 3, intensity = 8f)
            viewModel.clearError()
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // 驱动安装部分 - 带进入动画
                AnimatedListItem(
                    key = "driver_install_section",
                    enterDelay = 0
                ) {
                    SimpleDriverInstallCard(
                        uiState = uiState,
                        onInstallClick = { viewModel.startInstallWithTerminal() },
                        onDriverSelected = { driverName -> viewModel.onDriverSelected(driverName) },
                        onResetClick = { viewModel.onResetClick() },
                        shakeController = shakeController
                    )
                }
            }
            
            item {
                // 辅助功能部分 - 带延迟进入动画
                AnimatedListItem(
                    key = "assistant_section",
                    enterDelay = 150
                ) {
                    SimpleAssistantCard(
                        isDriverInstalled = uiState.isDriverInstalled,
                        onLaunchClick = { showAssistantDialog = true }
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
        
        // 使用增强版终端对话框
        EnhancedTerminalDialog(
            isVisible = showTerminalDialog,
            logs = terminalLogs,
            isCompleted = terminalCompleted,
            onDismiss = { viewModel.closeTerminalDialog() }
        )
        
        // 确认对话框
        if (uiState.showConfirmDialog) {
            DriverResetConfirmDialog(
                driverName = uiState.selectedDriver,
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
        
        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun SimpleDriverInstallCard(
    uiState: DriverUiState,
    onInstallClick: () -> Unit,
    onDriverSelected: (String) -> Unit,
    onResetClick: () -> Unit,
    shakeController: ShakeController
) {
    InfoCard(
        title = "驱动刷入",
        icon = Icons.Default.Build
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = when (uiState) {
                    is DriverUiState.InstallSuccess -> "驱动已成功刷入，无需重复操作。"
                    is DriverUiState.Installing -> "正在刷入驱动，请稍候..."
                    is DriverUiState.Resetting -> "正在重置驱动，请稍候..."
                    else -> "请选择需要刷入的驱动版本："
                },
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 驱动选择选项 - 使用交错动画
            DriverConstants.SUPPORTED_DRIVERS.forEachIndexed { index, driverName ->
                AnimatedListItem(
                    key = "driver_option_$driverName",
                    enterDelay = index * 50
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .rippleClickable(
                                onClick = { onDriverSelected(driverName) },
                                enabled = !uiState.isLoading && !uiState.isDriverInstalled
                            )
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = (uiState.selectedDriver == driverName),
                            onClick = { onDriverSelected(driverName) },
                            enabled = !uiState.isLoading && !uiState.isDriverInstalled,
                            colors = RadioButtonDefaults.colors(selectedColor = WarmOrange)
                        )
                        
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(
                                text = DriverConstants.getDriverDisplayName(driverName),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (DriverConstants.isRecommendedDriver(driverName)) {
                                    androidx.compose.ui.text.font.FontWeight.Bold
                                } else {
                                    androidx.compose.ui.text.font.FontWeight.Normal
                                }
                            )
                            
                            DriverConstants.DRIVER_DESCRIPTIONS[driverName]?.let { description ->
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 使用增强版交互按钮
            InteractiveButton(
                text = when (uiState) {
                    is DriverUiState.Installing -> "刷入中..."
                    is DriverUiState.InstallSuccess -> "驱动已刷入"
                    else -> "开始刷入 - ${uiState.selectedDriver}"
                },
                onClick = onInstallClick,
                enabled = !uiState.isLoading && !uiState.isDriverInstalled,
                loading = uiState is DriverUiState.Installing,
                icon = Icons.Default.Build,
                containerColor = WarmOrange,
                modifier = Modifier.fillMaxWidth(),
                shakeController = if (uiState.errorMessage != null) shakeController else null
            )
            
            // 重置按钮
            if (uiState.isDriverInstalled) {
                Spacer(modifier = Modifier.height(8.dp))
                
                InteractiveButton(
                    text = if (uiState is DriverUiState.Resetting) "重置中..." else "重置驱动（将重启手机）",
                    onClick = onResetClick,
                    enabled = !uiState.isLoading,
                    loading = uiState is DriverUiState.Resetting,
                    icon = Icons.Default.Refresh,
                    containerColor = Color.Gray,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SimpleAssistantCard(
    isDriverInstalled: Boolean,
    onLaunchClick: () -> Unit
) {
    InfoCard(
        title = "启动辅助功能",
        icon = Icons.Default.PlayArrow
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (isDriverInstalled) {
                    "驱动已就绪，可以启动辅助功能"
                } else {
                    "请先刷入驱动后再启动辅助功能"
                },
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            InteractiveButton(
                text = "启动辅助功能",
                onClick = onLaunchClick,
                enabled = isDriverInstalled,
                icon = Icons.Default.PlayArrow,
                containerColor = WarmOrange,
                modifier = Modifier.fillMaxWidth(),
                pulseWhenDisabled = !isDriverInstalled // 禁用时显示脉冲动画
            )
        }
    }
}