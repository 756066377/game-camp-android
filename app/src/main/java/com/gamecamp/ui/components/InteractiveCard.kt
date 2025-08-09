package com.gamecamp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
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
import com.gamecamp.ui.theme.WarmNeumorphismColors
import com.gamecamp.ui.theme.WarmOrange

/**
 * 可交互的信息卡片组件
 * 包含触摸反馈、点击动效和触觉反馈
 */
@Composable
fun InteractiveInfoCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    hapticFeedback: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // 按压缩放动画
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "card_scale"
    )
    
    // 卡片颜色动画
    val cardColor by animateColorAsState(
        targetValue = if (isPressed && enabled) {
            WarmNeumorphismColors.SurfacePrimary.copy(alpha = 0.9f)
        } else {
            WarmNeumorphismColors.SurfacePrimary
        },
        animationSpec = tween(150),
        label = "card_color"
    )
    
    // 阴影动画
    val elevation by animateDpAsState(
        targetValue = if (isPressed && enabled) 4.dp else 8.dp,
        animationSpec = tween(150),
        label = "card_elevation"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .let { mod ->
                if (onClick != null && enabled) {
                    mod.clickable(
                        interactionSource = interactionSource,
                        indication = null // 使用自定义动画效果
                    ) {
                        if (hapticFeedback) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        onClick()
                    }
                } else {
                    mod
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // 卡片标题
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 图标动画
                    val iconScale by animateFloatAsState(
                        targetValue = if (isPressed && enabled) 1.1f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessHigh
                        ),
                        label = "icon_scale"
                    )
                    
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = WarmOrange,
                        modifier = Modifier
                            .size(24.dp)
                            .scale(iconScale)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = WarmNeumorphismColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // 卡片内容
            content()
        }
    }
}

/**
 * 带右侧操作按钮的可交互信息卡片
 */
@Composable
fun InteractiveInfoCardWithAction(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    actionIcon: ImageVector? = null,
    onActionClick: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    hapticFeedback: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // 按压缩放动画
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "card_scale"
    )
    
    // 卡片颜色动画
    val cardColor by animateColorAsState(
        targetValue = if (isPressed && enabled) {
            WarmNeumorphismColors.SurfacePrimary.copy(alpha = 0.9f)
        } else {
            WarmNeumorphismColors.SurfacePrimary
        },
        animationSpec = tween(150),
        label = "card_color"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .let { mod ->
                if (onClick != null && enabled) {
                    mod.clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        if (hapticFeedback) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        onClick()
                    }
                } else {
                    mod
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPressed && enabled) 4.dp else 8.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // 卡片标题
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val iconScale by animateFloatAsState(
                        targetValue = if (isPressed && enabled) 1.1f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessHigh
                        ),
                        label = "icon_scale"
                    )
                    
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = WarmOrange,
                        modifier = Modifier
                            .size(24.dp)
                            .scale(iconScale)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = WarmNeumorphismColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                // 右侧操作按钮
                if (actionIcon != null && onActionClick != null) {
                    val actionInteractionSource = remember { MutableInteractionSource() }
                    val actionPressed by actionInteractionSource.collectIsPressedAsState()
                    
                    val actionScale by animateFloatAsState(
                        targetValue = if (actionPressed) 0.9f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessHigh
                        ),
                        label = "action_scale"
                    )
                    
                    IconButton(
                        onClick = {
                            if (hapticFeedback) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            onActionClick()
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .scale(actionScale),
                        interactionSource = actionInteractionSource
                    ) {
                        Icon(
                            imageVector = actionIcon,
                            contentDescription = "操作",
                            tint = WarmNeumorphismColors.TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 卡片内容
            content()
        }
    }
}

/**
 * 简单的可点击卡片
 * 用于游戏助手等功能卡片
 */
@Composable
fun SimpleInteractiveCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    hapticFeedback: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // 按压缩放动画
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "simple_card_scale"
    )
    
    // 卡片颜色动画
    val cardColor by animateColorAsState(
        targetValue = if (isPressed && enabled) {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(150),
        label = "simple_card_color"
    )
    
    Card(
        modifier = modifier
            .scale(scale)
            .let { mod ->
                if (onClick != null && enabled) {
                    mod.clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        if (hapticFeedback) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        onClick()
                    }
                } else {
                    mod
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPressed && enabled) 2.dp else 4.dp
        )
    ) {
        Column {
            content()
        }
    }
}