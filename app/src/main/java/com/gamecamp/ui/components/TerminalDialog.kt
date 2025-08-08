package com.gamecamp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gamecamp.data.LogType
import com.gamecamp.data.TerminalLog
import kotlinx.coroutines.delay

/**
 * 终端模拟器对话框组件
 * 模仿Linux终端的外观和行为，用于显示驱动安装日志
 */
@Composable
fun TerminalDialog(
    logs: List<TerminalLog>,
    isCompleted: Boolean = false,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = { if (isCompleted) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = isCompleted,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
            TerminalWindow(
                logs = logs,
                isCompleted = isCompleted,
                onClose = onDismiss
            )
        }
}

/**
 * 终端窗口主体
 */
@Composable
private fun TerminalWindow(
    logs: List<TerminalLog>,
    isCompleted: Boolean,
    onClose: () -> Unit
) {
    val listState = rememberLazyListState()
    
    // 自动滚动到底部
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .fillMaxHeight(0.8f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E) // 深灰色背景
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column {
            // 终端标题栏
            TerminalTitleBar(
                isCompleted = isCompleted,
                onClose = onClose
            )
            
            // 终端内容区域
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(16.dp)
            ) {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(logs) { log ->
                        TerminalLogItem(log = log)
                    }
                    
                    // 显示光标（如果未完成）
                    if (!isCompleted) {
                        item {
                            BlinkingCursor()
                        }
                    }
                }
            }
        }
    }
}

/**
 * 终端标题栏
 */
@Composable
private fun TerminalTitleBar(
    isCompleted: Boolean,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2D2D2D))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 模拟macOS风格的窗口控制按钮
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFFF5F57))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFFFBD2E))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF28CA42))
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = "驱动安装终端",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        if (isCompleted) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "关闭",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * 单条终端日志项
 */
@Composable
private fun TerminalLogItem(log: TerminalLog) {
    val textColor = when (log.type) {
        LogType.SUCCESS -> Color(0xFF00FF00)    // 绿色
        LogType.ERROR -> Color(0xFFFF0000)      // 红色
        LogType.WARNING -> Color(0xFFFFFF00)    // 黄色
        LogType.INFO -> Color(0xFFFFFFFF)       // 白色
        LogType.COMMAND -> Color(0xFF00FFFF)    // 青色
        LogType.PROGRESS -> Color(0xFF0080FF)   // 蓝色
    }
    
    val prefix = when (log.type) {
        LogType.COMMAND -> "$ "
        LogType.SUCCESS -> "✓ "
        LogType.ERROR -> "✗ "
        LogType.WARNING -> "⚠ "
        LogType.PROGRESS -> "⟳ "
        else -> ""
    }
    
    // 打字机效果
    var displayedText by remember { mutableStateOf("") }
    val fullText = "${log.timestamp} $prefix${log.text}"
    
    LaunchedEffect(log) {
        displayedText = ""
        fullText.forEachIndexed { index, char ->
            delay(20) // 打字机速度
            displayedText = fullText.substring(0, index + 1)
        }
    }
    
    Text(
        text = displayedText,
        color = textColor,
        fontSize = 13.sp,
        fontFamily = FontFamily.Monospace,
        lineHeight = 18.sp
    )
}

/**
 * 闪烁光标
 */
@Composable
private fun BlinkingCursor() {
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_alpha"
    )
    
    Text(
        text = "█",
        color = Color.White.copy(alpha = alpha),
        fontSize = 13.sp,
        fontFamily = FontFamily.Monospace
    )
}