package com.gamecamp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import android.content.Intent
import android.net.Uri
import com.gamecamp.ui.components.InfoCard
import com.gamecamp.ui.theme.WarmNeumorphismColors
import com.gamecamp.ui.theme.WarmOrange
import com.gamecamp.viewmodel.DashboardViewModel

/**
 * 带操作按钮的信息卡片组件
 * 用于显示各种系统信息，支持右上角操作按钮
 */
@Composable
fun InfoCardWithAction(
    title: String,
    icon: ImageVector,
    actionIcon: ImageVector,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = WarmNeumorphismColors.CreamWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        )
    ) {
        Column {
            // 卡片标题和操作按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = WarmNeumorphismColors.WarmOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "红色时代",
            style = MaterialTheme.typography.headlineMedium,
            color = WarmOrange,
            modifier = Modifier.padding(bottom = 16.dp)
        )
                }
                
                IconButton(
                    onClick = onActionClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = "操作",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // 分割线
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 1.dp
            )
            
            // 卡片内容
            Box(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

/**
 * 数据看板页面
 * 显示真实的设备系统信息
 */
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val kernelInfo by viewModel.kernelInfo.collectAsState()
    val selinuxInfo by viewModel.selinuxInfo.collectAsState()
    val deviceInfo by viewModel.deviceInfo.collectAsState()
    val fingerprintInfo by viewModel.fingerprintInfo.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmNeumorphismColors.CreamWhite)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 页面标题和刷新按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "红色时代",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            IconButton(
                onClick = { viewModel.refreshSystemInfo() },
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "刷新",
                    tint = if (isLoading) MaterialTheme.colorScheme.onSurfaceVariant 
                          else MaterialTheme.colorScheme.primary
                )
            }
        }

        // 错误提示
        errorMessage?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // 内核信息卡片
        InfoCard(
            title = "内核信息",
            icon = Icons.Default.Memory
        ) {
            if (isLoading) {
                repeat(3) {
                    LoadingInfoRow()
                    if (it < 2) Spacer(modifier = Modifier.height(12.dp))
                }
            } else {
                kernelInfo.entries.forEachIndexed { index, (key, value) ->
                    SystemInfoRow(
                        label = key,
                        value = value,
                        isMonospace = key == "编译信息"
                    )
                    if (index < kernelInfo.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
        
        // SELinux状态卡片
        InfoCard(
            title = "SELinux状态",
            icon = Icons.Default.Security
        ) {
            if (isLoading) {
                repeat(3) {
                    LoadingInfoRow()
                    if (it < 2) Spacer(modifier = Modifier.height(12.dp))
                }
            } else {
                selinuxInfo.entries.forEachIndexed { index, (key, value) ->
                    val valueColor = when {
                        key == "SELinux状态" && value.contains("Enforcing") -> MaterialTheme.colorScheme.primary
                        key == "SELinux状态" && value.contains("Permissive") -> MaterialTheme.colorScheme.tertiary
                        key == "SELinux状态" && value.contains("Disabled") -> MaterialTheme.colorScheme.error
                        key == "SELinux状态" && value.contains("需要Root权限") -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                    
                    SystemInfoRow(
                        label = key,
                        value = value,
                        valueColor = valueColor
                    )
                    if (index < selinuxInfo.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                // 如果需要Root权限，显示获取按钮
                if (selinuxInfo["SELinux状态"]?.contains("需要Root权限") == true) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { /* Root权限获取指南 */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WarmNeumorphismColors.WarmOrange.copy(alpha = 0.8f),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "了解Root权限获取",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
        
        // 设备信息卡片
        InfoCard(
            title = "设备信息",
            icon = Icons.Default.PhoneAndroid
        ) {
            if (isLoading) {
                repeat(4) {
                    LoadingInfoRow()
                    if (it < 3) Spacer(modifier = Modifier.height(12.dp))
                }
            } else {
                deviceInfo.entries.forEachIndexed { index, (key, value) ->
                    SystemInfoRow(
                        label = key,
                        value = value
                    )
                    if (index < deviceInfo.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
        
        // 设备指纹卡片
        InfoCard(
            title = "设备指纹",
            icon = Icons.Default.Fingerprint
        ) {
            if (isLoading) {
                repeat(4) {
                    LoadingInfoRow()
                    if (it < 3) Spacer(modifier = Modifier.height(12.dp))
                }
            } else {
                fingerprintInfo.entries.forEachIndexed { index, (key, value) ->
                    SystemInfoRow(
                        label = key,
                        value = value,
                        isMonospace = key == "设备指纹"
                    )
                    if (index < fingerprintInfo.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
        
        // TG频道引流按钮
        TelegramChannelButton()
    }
}

/**
 * 系统信息行组件
 */
@Composable
fun SystemInfoRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    isMonospace: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), // 增强清晰度
            modifier = Modifier.weight(1f)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = value,
            style = if (isMonospace) {
                MaterialTheme.typography.bodySmall.copy(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            } else {
                MaterialTheme.typography.bodyMedium
            },
            fontWeight = FontWeight.Medium,
            color = valueColor,
            modifier = Modifier.weight(2f)
        )
    }
}

/**
 * 加载中的信息行
 */
@Composable
fun LoadingInfoRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 标签占位符
        Box(
            modifier = Modifier
                .height(16.dp)
                .width(80.dp)
                .background(
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(4.dp)
                )
        )
        
        // 值占位符
        Box(
            modifier = Modifier
                .height(16.dp)
                .width(120.dp)
                .background(
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                )
        )
    }
}

/**
 * TG频道引流按钮 - 转化终点设计
 * 一眼能认、一秒能点、一键能跳
 */
@Composable
fun TelegramChannelButton() {
    val context = LocalContext.current
    
    Button(
        onClick = {
            try {
                // 直接使用浏览器打开，确保能正确跳转到频道
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/guiprofl"))
                // 强制使用浏览器而不是Telegram应用
                webIntent.addCategory(Intent.CATEGORY_BROWSABLE)
                context.startActivity(webIntent)
            } catch (e: Exception) {
                // 如果失败，记录错误
                android.util.Log.e("TelegramButton", "无法打开Telegram频道: ${e.message}")
            }
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF0088CC), // Telegram蓝色
            contentColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp), // 更高的按钮，更容易点击
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Telegram图标
            Text(
                text = "✈️",
                style = MaterialTheme.typography.headlineSmall
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 按钮文字
            Text(
                text = "加入TG频道",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // 跳转图标
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
