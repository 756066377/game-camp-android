package com.gamecamp.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.gamecamp.ui.theme.WarmOrange

/**
 * 确认对话框组件
 * 用于重要操作的二次确认
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "确认",
    cancelText: String = "取消",
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(text = title)
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = WarmOrange)
            ) {
                Text(confirmText, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(cancelText)
            }
        }
    )
}

/**
 * 驱动重置确认对话框
 */
@Composable
fun DriverResetConfirmDialog(
    driverName: String,
    installTime: String? = null,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val message = buildString {
        append("您确定要重置 $driverName 吗？\n\n")
        if (installTime != null) {
            append("当前驱动安装时间：$installTime\n\n")
        }
        append("⚠️ 重要提醒：\n")
        append("• 重置后设备将自动重启\n")
        append("• 所有未保存的数据可能会丢失\n")
        append("• 请确保重要数据已备份\n")
        append("• 重启过程中请勿断电")
    }
    
    ConfirmDialog(
        title = "确认重置驱动",
        message = message,
        confirmText = "确认重置",
        cancelText = "取消",
        onConfirm = onConfirm,
        onCancel = onCancel
    )
}
