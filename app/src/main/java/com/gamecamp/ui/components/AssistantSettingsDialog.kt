package com.gamecamp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gamecamp.ui.theme.WarmOrange

/**
 * 辅助功能设置数据类
 */
data class AssistantSettings(
    val antiScreenRecording: Boolean = false,
    val noBackgroundMode: Boolean = false,
    val singleTransparentMode: Boolean = false
)

/**
 * 辅助功能设置对话框
 * 用于配置防录屏、无后台模式、单透模式等功能
 */
@Composable
fun AssistantSettingsDialog(
    isVisible: Boolean,
    currentSettings: AssistantSettings,
    onConfirm: (AssistantSettings) -> Unit,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        // 当对话框可见时，创建一个内部状态来管理设置的变更
        // 这个状态会用外部传入的 currentSettings 初始化
        // 使用 remember(currentSettings) 确保当外部设置变化时，内部状态能同步更新
        var settings by remember(currentSettings) { mutableStateOf(currentSettings) }

        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // 标题栏
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "辅助功能设置",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        IconButton(
                            onClick = onDismiss
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "关闭",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 设置选项列表
                    SettingItem(
                        title = "防录屏模式",
                        description = "开启后将阻止应用被录屏或截图",
                        checked = settings.antiScreenRecording,
                        onCheckedChange = { checked ->
                            settings = settings.copy(antiScreenRecording = checked)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingItem(
                        title = "无后台模式",
                        description = "开启后应用将在后台保持活跃状态",
                        checked = settings.noBackgroundMode,
                        onCheckedChange = { checked ->
                            settings = settings.copy(noBackgroundMode = checked)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingItem(
                        title = "单透模式",
                        description = "开启后应用界面将变为半透明状态",
                        checked = settings.singleTransparentMode,
                        onCheckedChange = { checked ->
                            settings = settings.copy(singleTransparentMode = checked)
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 按钮区域
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 取消按钮
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text("取消")
                        }

                        // 确认按钮
                        Button(
                            onClick = { onConfirm(settings) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WarmOrange
                            )
                        ) {
                            Text(
                                text = "启动",
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 单个设置项组件
 */
@Composable
private fun SettingItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = WarmOrange,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}