package com.gamecamp.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.PlayArrow
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
import com.gamecamp.ui.components.TerminalDialog
import com.gamecamp.ui.state.DriverUiState
import com.gamecamp.ui.state.errorMessage
import com.gamecamp.ui.state.isDriverInstalled
import com.gamecamp.ui.state.isLoading
import com.gamecamp.ui.state.selectedDriver
import com.gamecamp.ui.state.showConfirmDialog
import com.gamecamp.ui.theme.WarmOrange
import com.gamecamp.viewmodel.DriverViewModel

/**
 * 驱动管理页面
 * 使用新的状态管理和确认对话框
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DriverScreen(
    viewModel: DriverViewModel = hiltViewModel()
) {
    // 从 ViewModel 中收集 UI 状态
    val uiState by viewModel.uiState.collectAsState()
    val terminalLogs by viewModel.terminalLogs.collectAsState()
    val showTerminalDialog by viewModel.showTerminalDialog.collectAsState()
    val terminalCompleted by viewModel.terminalCompleted.collectAsState()
    val assistantSettings by viewModel.assistantSettings.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // 辅助功能设置对话框状态
    var showAssistantDialog by remember { mutableStateOf(false) }

    // 处理错误消息显示
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
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
            item(key = "DriverInstallSection") {
                DriverInstallSection(
                    modifier = Modifier.animateItemPlacement(),
                    uiState = uiState,
                    onInstallClick = { viewModel.startInstallWithTerminal() },
                    onDriverSelected = { driverName -> viewModel.onDriverSelected(driverName) },
                    onResetClick = { viewModel.onResetClick() }
                )
            }
            
            item(key = "AssistantLaunchSection") {
                AssistantLaunchSection(
                    modifier = Modifier.animateItemPlacement(),
                    isDriverInstalled = uiState.isDriverInstalled,
                    onLaunchClick = { showAssistantDialog = true }
                )
            }
            
            item(key = "BottomSpacer") {
                // 添加底部间距，确保内容不会被 Snackbar 遮挡
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // 确认对话框
        AnimatedVisibility(
            visible = uiState.showConfirmDialog,
            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), initialScale = 0.8f),
            exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + scaleOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow), targetScale = 0.8f)
        ) {
            DriverResetConfirmDialog(
                driverName = uiState.selectedDriver,
                onConfirm = { viewModel.onConfirmReset() },
                onCancel = { viewModel.onCancelReset() }
            )
        }

        // 辅助功能设置对话框
        AnimatedVisibility(
            visible = showAssistantDialog,
            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), initialScale = 0.8f),
            exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + scaleOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow), targetScale = 0.8f)
        ) {
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
        AnimatedVisibility(
            visible = showTerminalDialog,
            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), initialScale = 0.8f),
            exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + scaleOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow), targetScale = 0.8f)
        ) {
            TerminalDialog(
                logs = terminalLogs,
                isCompleted = terminalCompleted,
                onDismiss = { viewModel.closeTerminalDialog() }
            )
        }

        // Snackbar 用于显示错误消息
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * 辅助功能启动部分
 */
@Composable
fun AssistantLaunchSection(
    isDriverInstalled: Boolean,
    onLaunchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    InfoCard(
        title = "启动辅助功能",
        icon = Icons.Default.PlayArrow,
        modifier = modifier
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
            
            Button(
                onClick = onLaunchClick,
                enabled = isDriverInstalled,
                colors = ButtonDefaults.buttonColors(containerColor = WarmOrange),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("启动辅助功能")
            }
        }
    }
}

@Composable
fun DriverInstallSection(
    uiState: DriverUiState,
    onInstallClick: () -> Unit,
    onDriverSelected: (String) -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    InfoCard(
        title = "驱动刷入",
        icon = Icons.Default.Build,
        modifier = modifier
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
            Spacer(modifier = Modifier.height(8.dp))

            // 驱动选择选项
            DriverConstants.SUPPORTED_DRIVERS.forEach { driverName ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
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
                            fontWeight = if (DriverConstants.isRecommendedDriver(driverName)) FontWeight.Bold else FontWeight.Normal
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

            Spacer(modifier = Modifier.height(16.dp))

            // 安装按钮
            Button(
                onClick = onInstallClick,
                enabled = !uiState.isLoading && !uiState.isDriverInstalled,
                colors = ButtonDefaults.buttonColors(containerColor = WarmOrange),
                modifier = Modifier.fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = uiState,
                    label = "InstallButtonAnimation"
                ) { state ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        when (state) {
                            is DriverUiState.Installing -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("刷入中...")
                            }
                            is DriverUiState.InstallSuccess -> {
                                Text("驱动已刷入")
                            }
                            else -> {
                                Text("开始刷入 - ${state.selectedDriver}")
                            }
                        }
                    }
                }
            }

            // 重置按钮（驱动已安装时显示）
            if (uiState.isDriverInstalled) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onResetClick,
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AnimatedContent(
                        targetState = uiState,
                        label = "ResetButtonAnimation"
                    ) { state ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            when (state) {
                                is DriverUiState.Resetting -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("重置中...")
                                }
                                else -> {
                                    Text("重置驱动（将重启手机）")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}