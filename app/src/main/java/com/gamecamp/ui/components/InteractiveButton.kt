package com.gamecamp.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamecamp.ui.animation.AnimationUtils
import com.gamecamp.ui.animation.ShakeController
import com.gamecamp.ui.animation.pulse
import com.gamecamp.ui.animation.shake

/**
 * 增强的交互按钮
 * 包含点击动画、震动反馈、状态动画等
 */
@Composable
fun InteractiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    hapticFeedback: Boolean = true,
    pulseWhenDisabled: Boolean = false,
    shakeController: ShakeController? = null
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // 按压缩放动画
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.95f else 1f,
        animationSpec = AnimationUtils.springSpec(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "button_scale"
    )
    
    // 颜色动画
    val animatedContainerColor by animateColorAsState(
        targetValue = when {
            !enabled -> containerColor.copy(alpha = 0.6f)
            loading -> containerColor.copy(alpha = 0.8f)
            else -> containerColor
        },
        animationSpec = tween(AnimationUtils.FAST_ANIMATION),
        label = "container_color"
    )
    
    Button(
        onClick = {
            if (hapticFeedback) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            onClick()
        },
        enabled = enabled && !loading,
        colors = ButtonDefaults.buttonColors(
            containerColor = animatedContainerColor,
            contentColor = contentColor
        ),
        interactionSource = interactionSource,
        modifier = modifier
            .scale(scale)
            .let { mod ->
                if (pulseWhenDisabled && !enabled) {
                    mod.pulse(enabled = true, minScale = 0.98f, maxScale = 1.02f)
                } else {
                    mod
                }
            }
            .let { mod ->
                shakeController?.let { controller ->
                    mod.shake(controller)
                } ?: mod
            }
    ) {
        AnimatedContent(
            targetState = loading,
            transitionSpec = {
                fadeIn(animationSpec = tween(AnimationUtils.FAST_ANIMATION)) togetherWith
                        fadeOut(animationSpec = tween(AnimationUtils.FAST_ANIMATION))
            },
            label = "button_content"
        ) { isLoading ->
            if (isLoading) {
                LoadingContent()
            } else {
                ButtonContent(
                    text = text,
                    icon = icon,
                    contentColor = contentColor
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = Color.White,
            strokeWidth = 2.dp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "处理中...",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ButtonContent(
    text: String,
    icon: ImageVector?,
    contentColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        icon?.let { iconVector ->
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = contentColor
        )
    }
}

/**
 * 状态指示卡片
 * 带有状态转换动画
 */
@Composable
fun AnimatedStatusCard(
    title: String,
    status: StatusCardState,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val backgroundColor by animateColorAsState(
        targetValue = when (status) {
            StatusCardState.Success -> Color(0xFF4CAF50).copy(alpha = 0.1f)
            StatusCardState.Error -> Color(0xFFF44336).copy(alpha = 0.1f)
            StatusCardState.Warning -> Color(0xFFFF9800).copy(alpha = 0.1f)
            StatusCardState.Loading -> Color(0xFF2196F3).copy(alpha = 0.1f)
            StatusCardState.Idle -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(AnimationUtils.NORMAL_ANIMATION),
        label = "background_color"
    )
    
    val borderColor by animateColorAsState(
        targetValue = when (status) {
            StatusCardState.Success -> Color(0xFF4CAF50)
            StatusCardState.Error -> Color(0xFFF44336)
            StatusCardState.Warning -> Color(0xFFFF9800)
            StatusCardState.Loading -> Color(0xFF2196F3)
            StatusCardState.Idle -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        },
        animationSpec = tween(AnimationUtils.NORMAL_ANIMATION),
        label = "border_color"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .let { mod ->
                onClick?.let { clickHandler ->
                    mod.clickable { clickHandler() }
                } ?: mod
            },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 状态指示器
            AnimatedStatusIndicator(status = status)
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 标题
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            
            // 状态文本
            AnimatedContent(
                targetState = status,
                transitionSpec = {
                    slideInVertically { it } + fadeIn() togetherWith
                            slideOutVertically { -it } + fadeOut()
                },
                label = "status_text"
            ) { currentStatus ->
                Text(
                    text = when (currentStatus) {
                        StatusCardState.Success -> "已完成"
                        StatusCardState.Error -> "失败"
                        StatusCardState.Warning -> "警告"
                        StatusCardState.Loading -> "进行中"
                        StatusCardState.Idle -> "待处理"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = when (currentStatus) {
                        StatusCardState.Success -> Color(0xFF4CAF50)
                        StatusCardState.Error -> Color(0xFFF44336)
                        StatusCardState.Warning -> Color(0xFFFF9800)
                        StatusCardState.Loading -> Color(0xFF2196F3)
                        StatusCardState.Idle -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    }
                )
            }
        }
    }
}

@Composable
private fun AnimatedStatusIndicator(status: StatusCardState) {
    when (status) {
        StatusCardState.Loading -> {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color(0xFF2196F3),
                strokeWidth = 2.dp
            )
        }
        else -> {
            val infiniteTransition = rememberInfiniteTransition(label = "status_indicator")
            
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = if (status == StatusCardState.Success) 1.2f else 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "indicator_scale"
            )
            
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .scale(scale)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        when (status) {
                            StatusCardState.Success -> Color(0xFF4CAF50)
                            StatusCardState.Error -> Color(0xFFF44336)
                            StatusCardState.Warning -> Color(0xFFFF9800)
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        }
                    )
            )
        }
    }
}

/**
 * 状态卡片状态枚举
 */
enum class StatusCardState {
    Idle,
    Loading,
    Success,
    Warning,
    Error
}

/**
 * 波纹点击效果修饰符
 */
@Composable
fun Modifier.rippleClickable(
    onClick: () -> Unit,
    enabled: Boolean = true,
    hapticFeedback: Boolean = true
): Modifier {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    
    return this.clickable(
        interactionSource = interactionSource,
        indication = androidx.compose.material.ripple.rememberRipple(
            bounded = true,
            radius = 300.dp,
            color = MaterialTheme.colorScheme.primary
        ),
        enabled = enabled,
        onClick = {
            if (hapticFeedback) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            onClick()
        }
    )
}
