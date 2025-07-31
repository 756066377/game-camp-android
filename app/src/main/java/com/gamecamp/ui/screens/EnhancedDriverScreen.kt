package com.gamecamp.ui.screens

import com.gamecamp.constants.DriverConstants

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gamecamp.constants.AppConstants
import com.gamecamp.ui.components.InfoCard
import com.gamecamp.ui.components.TerminalDialog
import com.gamecamp.ui.state.*
import com.gamecamp.ui.theme.*
import com.gamecamp.viewmodel.DriverViewModel

/**
 * 增强版驱动管理页面
 * 使用暖质拟态风配色方案，包含视觉层次感、状态指示器、错误处理等优化
 */
@Composable
fun EnhancedDriverScreen(
    viewModel: DriverViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val terminalLogs by viewModel.terminalLogs.collectAsState()
    val showTerminalDialog by viewModel.showTerminalDialog.collectAsState()
    val terminalCompleted by viewModel.terminalCompleted.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // 页面重新进入时刷新状态
    LaunchedEffect(Unit) {
        viewModel.refreshDriverStatus()
    }

    // 处理错误消息显示
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "确定",
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmNeumorphismColors.CreamWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 驱动状态指示器
            DriverStatusCard(uiState = uiState)
            
            // 驱动安装区域
            EnhancedDriverInstallSection(
                uiState = uiState,
                onInstallClick = { viewModel.startInstallWithTerminal() },
                onDriverSelected = { driverName -> viewModel.onDriverSelected(driverName) },
                onResetClick = { viewModel.onResetClick() }  // 恢复二次确认
            )
            
        }

        // 确认对话框
        if (uiState.showConfirmDialog) {
            EnhancedConfirmDialog(
                driverName = uiState.selectedDriver,
                onConfirm = { 
                    viewModel.onCancelReset() // 先关闭对话框
                    viewModel.startResetWithTerminal() // 直接启动终端模式
                },
                onCancel = { viewModel.onCancelReset() }
            )
        }

        // 终端对话框
        if (showTerminalDialog) {
            TerminalDialog(
                isVisible = showTerminalDialog,
                logs = terminalLogs,
                isCompleted = terminalCompleted,
                onDismiss = { viewModel.closeTerminalDialog() }
            )
        }

        // Snackbar - 使用暖质拟态风配色
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) { snackbarData ->
            Snackbar(
                snackbarData = snackbarData,
                containerColor = when {
                    snackbarData.visuals.message.contains("成功") -> SuccessGreen
                    snackbarData.visuals.message.contains("失败") || snackbarData.visuals.message.contains("错误") -> ErrorRed
                    else -> MaterialTheme.colorScheme.inverseSurface
                },
                contentColor = CreamWhite
            )
        }
    }
}

/**
 * 驱动状态卡片 - 使用暖质拟态风配色
 */
@Composable
fun DriverStatusCard(
    uiState: DriverUiState,
    viewModel: DriverViewModel = hiltViewModel()
) {
    val hasRootPermission = remember { viewModel.checkRootPermission() }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                uiState.isDriverInstalled -> SuccessGreenLight
                uiState.errorMessage != null -> ErrorRedLight
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when {
                        uiState.isDriverInstalled -> Icons.Default.CheckCircle
                        uiState.errorMessage != null -> Icons.Default.Error
                        uiState.isLoading -> Icons.Default.Build
                        else -> Icons.Default.Warning
                    },
                    contentDescription = null,
                    tint = when {
                        uiState.isDriverInstalled -> SuccessGreen
                        uiState.errorMessage != null -> ErrorRed
                        uiState.isLoading -> WarmOrange
                        else -> WarningAmber
                    },
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = when {
                            uiState.isDriverInstalled -> "驱动已安装"
                            uiState.errorMessage != null -> "操作失败"
                            uiState.isLoading -> "正在处理..."
                            else -> "驱动未安装"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            uiState.isDriverInstalled -> SuccessGreen
                            uiState.errorMessage != null -> ErrorRed
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                    
                    Text(
                        text = when {
                            uiState.isDriverInstalled -> "当前驱动：${uiState.selectedDriver}"
                            uiState.isLoading -> "请稍候，正在处理您的请求..."
                            else -> "请选择并安装驱动"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Root权限状态指示 - 使用暖质拟态风配色
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (hasRootPermission) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (hasRootPermission) SuccessGreen else ErrorRed,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (hasRootPermission) "Root权限：已获取" else "Root权限：未获取",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (hasRootPermission) SuccessGreen else ErrorRed
                )
            }
        }
    }
}

/**
 * 增强版驱动安装区域 - 使用暖质拟态风配色
 */
@Composable
fun EnhancedDriverInstallSection(
    uiState: DriverUiState,
    onInstallClick: () -> Unit,
    onDriverSelected: (String) -> Unit,
    onResetClick: () -> Unit
) {
    InfoCard(
        title = "驱动管理",
        icon = Icons.Default.Build
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 驱动选择
            Text(
                text = "可用驱动版本：",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            DriverConstants.SUPPORTED_DRIVERS.forEach { driverName ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = (uiState.selectedDriver == driverName),
                        onClick = { onDriverSelected(driverName) },
                        enabled = !uiState.isLoading && !uiState.isDriverInstalled,
                        colors = RadioButtonDefaults.colors(selectedColor = WarmOrange)
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = DriverConstants.getDriverDisplayName(driverName),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (DriverConstants.isRecommendedDriver(driverName)) FontWeight.Bold else FontWeight.Normal
                            )
                            if (DriverConstants.isRecommendedDriver(driverName)) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "推荐",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
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

            // 兼容性提示
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = WarmNeumorphismColors.SurfaceSecondary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = WarmOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = DriverConstants.COMPATIBILITY_NOTICE,
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmNeumorphismColors.TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 安装按钮 - 使用暖质拟态风配色
            Button(
                onClick = onInstallClick,
                enabled = !uiState.isLoading && !uiState.isDriverInstalled,
                colors = ButtonDefaults.buttonColors(containerColor = WarmOrange),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                when (uiState) {
                    is DriverUiState.Installing -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = CreamWhite,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("正在安装...", color = CreamWhite)
                    }
                    is DriverUiState.InstallSuccess -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = CreamWhite
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("安装完成", color = CreamWhite)
                    }
                    else -> {
                        Text("安装 ${uiState.selectedDriver}", color = CreamWhite)
                    }
                }
            }

            // 重置按钮 - 使用暖质拟态风配色
            if (uiState.isDriverInstalled) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onResetClick,
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = WarningAmber
                    )
                ) {
                    when (uiState) {
                        is DriverUiState.Resetting -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = WarningAmber,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("正在重置...")
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("重置驱动")
                        }
                    }
                }
            }
        }
    }
}


/**
 * 增强版确认对话框 - 使用暖质拟态风配色，默认终端模式
 */
@Composable
fun EnhancedConfirmDialog(
    driverName: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = WarningAmber,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "确认重置驱动",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "您确定要重置 $driverName 吗？",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = WarningAmberLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "⚠️ 重要提醒：",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = WarningAmber
                        )
                        Text(
                            text = "• 重置后设备将自动重启\n• 所有未保存的数据可能会丢失\n• 请确保重要数据已备份",
                            style = MaterialTheme.typography.bodySmall,
                            color = WarningAmberDark,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 终端模式说明
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = WarmNeumorphismColors.SurfaceSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "🖥️ 终端模式重置：",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = WarmOrange
                        )
                        Text(
                            text = "将以终端模式执行重置，您可以实时查看重置过程的详细日志",
                            style = MaterialTheme.typography.bodySmall,
                            color = WarmNeumorphismColors.TextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = WarmOrange
                )
            ) {
                Text("确认重置", color = CreamWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("取消")
            }
        }
    )
}
