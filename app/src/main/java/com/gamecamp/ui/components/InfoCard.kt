package com.gamecamp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gamecamp.ui.theme.WarmNeumorphismColors
import com.gamecamp.ui.theme.WarmOrange

/**
 * 可复用的信息卡片组件
 * 采用暖质拟态风格设计，遵循"上重下轻、左重右轻、先横后纵"的设计原则
 * @param title 卡片标题
 * @param icon 卡片图标
 * @param content 卡片的具体内容
 */
@Composable
fun InfoCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = WarmNeumorphismColors.SurfacePrimary
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // 卡片标题 - 左重右轻布局
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                // 左侧：主要标题信息（重）
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = WarmOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = WarmNeumorphismColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                // 右侧：辅助信息区域（轻）- 预留给刷新按钮等
                // 这里可以通过参数传入右侧内容
            }

            // 卡片内容 - 遵循上重下轻原则
            content()
        }
    }
}

/**
 * 带右侧操作按钮的信息卡片
 */
@Composable
fun InfoCardWithAction(
    title: String,
    icon: ImageVector,
    actionIcon: ImageVector? = null,
    onActionClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = WarmNeumorphismColors.SurfacePrimary
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // 卡片标题 - 左重右轻布局
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                // 左侧：主要标题信息（重）
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = WarmOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = WarmNeumorphismColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                // 右侧：操作按钮（轻）
                if (actionIcon != null && onActionClick != null) {
                    IconButton(
                        onClick = onActionClick,
                        modifier = Modifier.size(32.dp)
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

            // 卡片内容 - 遵循上重下轻原则
            content()
        }
    }
}
