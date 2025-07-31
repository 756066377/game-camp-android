package com.gamecamp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gamecamp.ui.state.DriverUiState
import com.gamecamp.ui.theme.WarmOrange
import com.gamecamp.viewmodel.DriverViewModel

/**
 * 游戏辅助页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameAssistantScreen(
    viewModel: DriverViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // 判断驱动是否已安装
    val isDriverInstalled = when (uiState) {
        is DriverUiState.InstallSuccess -> true
        else -> false
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // 主要功能卡片
            GameAssistantMainCard(isDriverInstalled = isDriverInstalled)
        }
        
        item {
            // 核心功能介绍卡片
            GameAssistantSettingsCard(isDriverInstalled = isDriverInstalled)
        }
        
        item {
            // 使用说明和常见问题卡片
            UsageGuideCard()
        }
        
        item {
            // 底部间距，防止被导航栏遮挡
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

/**
 * 游戏辅助主功能卡片
 */
@Composable
fun GameAssistantMainCard(isDriverInstalled: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDriverInstalled) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 标题和图标
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "游戏辅助",
                    tint = WarmOrange,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "🎮 游戏辅助系统",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // 状态指示器
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isDriverInstalled) 
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else 
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isDriverInstalled) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = "状态",
                        tint = if (isDriverInstalled) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isDriverInstalled) "✅ 系统就绪，辅助功能可用" else "⏳ 等待驱动安装完成",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (isDriverInstalled) 
                            MaterialTheme.colorScheme.onSurface 
                        else 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // 状态描述
            Text(
                text = if (isDriverInstalled) 
                    "驱动已成功安装，所有辅助功能现已可用。点击下方按钮启动游戏辅助系统。" 
                else 
                    "请先在驱动管理页面安装驱动，安装完成后即可使用游戏辅助功能。",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // 启动按钮
            Button(
                onClick = { /* TODO: 启动辅助功能 */ },
                enabled = isDriverInstalled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WarmOrange,
                    disabledContainerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "启动",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isDriverInstalled) "启动辅助功能" else "等待驱动安装",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * 辅助功能介绍卡片
 */
@Composable
fun GameAssistantSettingsCard(isDriverInstalled: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // 标题
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "功能介绍",
                    tint = WarmOrange,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "🎯 核心功能介绍",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // 核心功能列表
            val coreFeatures = listOf(
                "👁️ 丝滑流畅ESP" to "超流畅的透视功能，帧率稳定不掉帧，视觉效果清晰自然",
                "📦 齐全物资ESP" to "全面的物资透视系统，涵盖所有装备道具，精准定位不遗漏",
                "🎯 独家超准自瞄" to "采用独家算法的触摸自瞄系统，精准度极高，操作自然流畅",
                "⚖️ 稳定压枪算法" to "先进的压枪控制算法，确保功能稳定运行，长时间使用无异常",
                "🎮 外部引擎渲染" to "辅助自带外部引擎渲染，可以实现敌人漏打打哪"
            )

            coreFeatures.forEach { (title, description) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDriverInstalled) 
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        else 
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDriverInstalled) 
                                MaterialTheme.colorScheme.onSurface 
                            else 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDriverInstalled) 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            else 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2
                        )
                    }
                }
            }

            // 底部说明
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = WarmOrange.copy(alpha = 0.1f)
                )
            ) {
                Text(
                    text = "✨ 以上功能均基于先进算法开发，确保在提供强大辅助能力的同时保持系统稳定性和使用安全性",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(16.dp),
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.3
                )
            }
        }
    }
}

/**
 * 使用说明和常见问题卡片
 */
@Composable
fun UsageGuideCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // 标题
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "使用说明",
                    tint = WarmOrange,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "📖 使用说明",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // 常见问题解决方案
            Text(
                text = "常见问题解决方案",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // 问题列表
            val qaList = listOf(
                "Q：打开软件闪退" to "A：关闭VPN等软件",
                "Q：平板开启后无法触摸悬浮窗" to "A：竖屏启动，并且使用你显示设置里面的最高分辨率",
                "Q：什么是模型漏打" to "A：将整个地图静态模型加载出来，通过射线检测敌人骨骼判断模型碰撞实现漏打变色",
                "Q：手机加载模型会卡吗" to "A：PhysX引擎优化静态物体合并，减少内存占用。模型无绘制几乎不吃GPU，推荐骁龙870以上配置",
                "Q：如何区分模型漏打" to "A：模型漏打不修改内存不会闪退，无需点初始化按钮，加载需要时间效果不会立刻生效"
            )

            qaList.forEach { (question, answer) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // 问题
                        Text(
                            text = question,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        // 答案
                        Text(
                            text = answer,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2
                        )
                    }
                }
            }

            // 底部提示
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = "💡 如遇到其他问题，请确保设备已获取Root权限，并按照驱动安装步骤正确操作。如问题持续存在，请联系飞蓝技术支持。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(16.dp),
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.3
                )
            }
        }
    }
}