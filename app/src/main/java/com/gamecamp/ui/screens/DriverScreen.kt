package com.gamecamp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gamecamp.constants.DriverConstants
import com.gamecamp.ui.components.ConfirmDialog
import com.gamecamp.ui.components.DriverResetConfirmDialog
import com.gamecamp.ui.components.InfoCard
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
@Composable
fun DriverScreen(
    viewModel: DriverViewModel = hiltViewModel()
) {
    // 从 ViewModel 中收集 UI 状态
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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
            item {
                DriverInstallSection(
                    uiState = uiState,
                    onInstallClick = { viewModel.onInstallClick() },
                    onDriverSelected = { driverName -> viewModel.onDriverSelected(driverName) },
                    onResetClick = { viewModel.onResetClick() }
                )
            }
            
            
            item {
                // 添加底部间距，确保内容不会被 Snackbar 遮挡
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // 确认对话框
        if (uiState.showConfirmDialog) {
            DriverResetConfirmDialog(
                driverName = uiState.selectedDriver,
                onConfirm = { viewModel.onConfirmReset() },
                onCancel = { viewModel.onCancelReset() }
            )
        }

        // Snackbar 用于显示错误消息
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun DriverInstallSection(
    uiState: DriverUiState,
    onInstallClick: () -> Unit,
    onDriverSelected: (String) -> Unit,
    onResetClick: () -> Unit
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
                when (uiState) {
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
                        Text("开始刷入 - ${uiState.selectedDriver}")
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
                    when (uiState) {
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

