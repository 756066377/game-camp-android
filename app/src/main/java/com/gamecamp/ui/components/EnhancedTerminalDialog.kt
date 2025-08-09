package com.gamecamp.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gamecamp.data.LogType
import com.gamecamp.data.TerminalLog
import com.gamecamp.ui.animation.AnimationUtils
import kotlinx.coroutines.delay

/**
 * 增强版终端对话框
 * 包含打字机效果、语法高亮、动画等功能
 */
@Composable
fun EnhancedTerminalDialog(
    isVisible: Boolean,
    logs: List<TerminalLog>,
    isCompleted: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = { if (isCompleted) onDismiss() },
            properties = DialogProperties(
                dismissOnBackPress = isCompleted,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = AnimationUtils.dialogEnterTransition(),
                exit = AnimationUtils.dialogExitTransition()
            ) {
                TerminalDialogContent(
                    logs = logs,
                    isCompleted = isCompleted,
                    onDismiss = onDismiss,
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
private fun TerminalDialogContent(
    logs: List<TerminalLog>,
    isCompleted: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val listState = rememberLazyListState()
    
    // 自动滚动到底部
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            delay(100) // 等待动画完成
            listState.animateScrollToItem(logs.size - 1)
        }
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth(0.95f)
            .fillMaxHeight(0.8f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E) // 深色终端背景
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 标题栏
            TerminalHeader(
                isCompleted = isCompleted,
                onDismiss = onDismiss,
                onCopyAll = {
                    val allLogs = logs.joinToString("\n") { "${it.timestamp} ${it.text}" }
                    clipboardManager.setText(AnnotatedString(allLogs))
                }
            )
            
            // 终端内容
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(
                    items = logs,
                    key = { log -> "${log.timestamp}_${log.text.hashCode()}" }
                ) { log ->
                    AnimatedTerminalLogItem(
                        log = log,
                        onCopy = { text ->
                            clipboardManager.setText(AnnotatedString(text))
                        }
                    )
                }
                
                // 底部间距
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun TerminalHeader(
    isCompleted: Boolean,
    onDismiss: () -> Unit,
    onCopyAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFF2D2D2D),
                RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 状态指示器
            StatusIndicator(isCompleted = isCompleted)
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = if (isCompleted) "操作完成" else "执行中...",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        Row {
            // 复制全部按钮
            IconButton(
                onClick = onCopyAll,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "复制全部",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
            
            // 关闭按钮
            IconButton(
                onClick = onDismiss,
                enabled = isCompleted,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "关闭",
                    tint = if (isCompleted) Color.White else Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun StatusIndicator(isCompleted: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "status")
    
    if (isCompleted) {
        // 完成状态 - 绿色圆点
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF4CAF50))
        )
    } else {
        // 执行中状态 - 脉冲动画
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_alpha"
        )
        
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFFF9800).copy(alpha = alpha))
        )
    }
}

@Composable
private fun AnimatedTerminalLogItem(
    log: TerminalLog,
    onCopy: (String) -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    
    // 进入动画
    LaunchedEffect(log) {
        delay(50) // 短暂延迟创造打字机效果
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = tween(200)
        ) + fadeIn(animationSpec = tween(200))
    ) {
        TerminalLogItem(
            log = log,
            onCopy = onCopy
        )
    }
}

@Composable
private fun TerminalLogItem(
    log: TerminalLog,
    onCopy: (String) -> Unit
) {
    val textColor = when (log.type) {
        LogType.COMMAND -> Color(0xFF81C784) // 绿色 - 命令
        LogType.SUCCESS -> Color(0xFF4CAF50) // 深绿色 - 成功
        LogType.ERROR -> Color(0xFFF44336)   // 红色 - 错误
        LogType.WARNING -> Color(0xFFFF9800) // 橙色 - 警告
        LogType.INFO -> Color(0xFF2196F3)    // 蓝色 - 信息
        else -> Color.White                   // 默认白色
    }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // 时间戳
        Text(
            text = log.timestamp,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(80.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // 日志内容
        TypewriterText(
            text = log.text,
            color = textColor,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f),
            isCommand = log.isCommand
        )
    }
}

@Composable
private fun TypewriterText(
    text: String,
    color: Color,
    fontSize: androidx.compose.ui.unit.TextUnit,
    fontFamily: FontFamily,
    modifier: Modifier = Modifier,
    isCommand: Boolean = false,
    typewriterSpeed: Long = 20L // 打字机速度（毫秒）
) {
    var displayedText by remember(text) { mutableStateOf("") }
    
    LaunchedEffect(text) {
        displayedText = ""
        text.forEachIndexed { index, _ ->
            delay(typewriterSpeed)
            displayedText = text.substring(0, index + 1)
        }
    }
    
    Row(modifier = modifier) {
        if (isCommand) {
            Text(
                text = "$ ",
                color = Color(0xFF81C784),
                fontSize = fontSize,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold
            )
        }
        
        Text(
            text = displayedText,
            color = color,
            fontSize = fontSize,
            fontFamily = fontFamily,
            fontWeight = if (isCommand) FontWeight.Medium else FontWeight.Normal
        )
        
        // 光标效果（仅在打字过程中显示）
        if (displayedText.length < text.length) {
            BlinkingCursor(color = color)
        }
    }
}

@Composable
private fun BlinkingCursor(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_alpha"
    )
    
    Text(
        text = "█",
        color = color.copy(alpha = alpha),
        fontSize = 14.sp,
        fontFamily = FontFamily.Monospace
    )
}