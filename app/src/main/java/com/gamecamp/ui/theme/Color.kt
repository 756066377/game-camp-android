package com.gamecamp.ui.theme

import androidx.compose.ui.graphics.Color

// Material3 默认颜色
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// 暖质拟态风配色方案
object WarmNeumorphismColors {
    // 主色调 - 暖橙色系
    val WarmOrange = Color(0xFFFF6B35)
    val WarmOrangeLight = Color(0xFFFF8A65)
    val WarmOrangeDark = Color(0xFFE64A19)
    
    // 背景色 - 米白色系
    val CreamWhite = Color(0xFFFFFBF5)
    val CreamWhiteLight = Color(0xFFFFFEFC)
    val CreamWhiteDark = Color(0xFFF5F1EB)
    
    // 表面色 - 柔和色调
    val SurfacePrimary = Color(0xFFFAF7F2)
    val SurfaceSecondary = Color(0xFFF0EDE8)
    val SurfaceTertiary = Color(0xFFE8E5E0)
    
    // 文字色 - 棕色系
    val TextPrimary = Color(0xFF5D4E37)
    val TextSecondary = Color(0xFF8B7355)
    val TextTertiary = Color(0xFFA68B5B)
    
    // 边框色
    val BorderLight = Color(0xFFE0DDD8)
    val BorderMedium = Color(0xFFD0CCC7)
    val BorderDark = Color(0xFFC0BBB6)
    
    // 阴影色
    val ShadowLight = Color(0x1A000000)
    val ShadowMedium = Color(0x33000000)
    val ShadowDark = Color(0x4D000000)
}

// 状态颜色 - 暖质拟态风格
val SuccessGreen = Color(0xFF4CAF50)
val SuccessGreenLight = Color(0xFFE8F5E8)
val SuccessGreenDark = Color(0xFF2E7D32)

val ErrorRed = Color(0xFFE57373)
val ErrorRedLight = Color(0xFFFFEBEE)
val ErrorRedDark = Color(0xFFC62828)

val WarningAmber = Color(0xFFFFB74D)
val WarningAmberLight = Color(0xFFFFF3E0)
val WarningAmberDark = Color(0xFFE65100)

val InfoBlue = Color(0xFF64B5F6)
val InfoBlueLight = Color(0xFFE3F2FD)
val InfoBlueDark = Color(0xFF1976D2)

// 中性色
val SoftGray = Color(0xFF9E9E9E)
val SoftGrayLight = Color(0xFFF5F5F5)
val SoftGrayDark = Color(0xFF616161)

// 辅助色调
val SoftBrown = Color(0xFF8D6E63)
val SoftBrownLight = Color(0xFFEFEBE9)
val SoftBrownDark = Color(0xFF5D4037)

val SoftPink = Color(0xFFF48FB1)
val SoftPinkLight = Color(0xFFFCE4EC)
val SoftPinkDark = Color(0xFFAD1457)

// 兼容性别名 - 保持向后兼容
val WarmOrange = WarmNeumorphismColors.WarmOrange
val CreamWhite = WarmNeumorphismColors.CreamWhite
val TextPrimary = WarmNeumorphismColors.TextPrimary
val TextSecondary = WarmNeumorphismColors.TextSecondary
val TextTertiary = WarmNeumorphismColors.TextTertiary
