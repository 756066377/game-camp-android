package com.gamecamp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 暖质拟态风深色主题配色方案
private val WarmNeumorphismDarkColorScheme = darkColorScheme(
    primary = WarmNeumorphismColors.WarmOrange,
    onPrimary = WarmNeumorphismColors.CreamWhite,
    primaryContainer = WarmNeumorphismColors.WarmOrangeDark,
    onPrimaryContainer = WarmNeumorphismColors.CreamWhiteLight,
    
    secondary = SoftBrown,
    onSecondary = WarmNeumorphismColors.TextPrimary,
    secondaryContainer = SoftBrownDark,
    onSecondaryContainer = WarmNeumorphismColors.CreamWhite,
    
    tertiary = SoftPink,
    onTertiary = WarmNeumorphismColors.TextPrimary,
    tertiaryContainer = SoftPinkDark,
    onTertiaryContainer = WarmNeumorphismColors.CreamWhite,
    
    background = WarmNeumorphismColors.TextPrimary,
    onBackground = WarmNeumorphismColors.CreamWhite,
    
    surface = WarmNeumorphismColors.SurfaceSecondary,
    onSurface = WarmNeumorphismColors.CreamWhite,
    surfaceVariant = WarmNeumorphismColors.SurfaceTertiary,
    onSurfaceVariant = WarmNeumorphismColors.TextSecondary,
    
    outline = WarmNeumorphismColors.BorderMedium,
    outlineVariant = WarmNeumorphismColors.BorderLight,
    
    error = ErrorRed,
    onError = WarmNeumorphismColors.CreamWhite,
    errorContainer = ErrorRedDark,
    onErrorContainer = WarmNeumorphismColors.CreamWhiteLight,
    
    inverseSurface = WarmNeumorphismColors.CreamWhite,
    inverseOnSurface = WarmNeumorphismColors.TextPrimary,
    inversePrimary = WarmNeumorphismColors.WarmOrangeDark,
)

// 暖质拟态风浅色主题配色方案
private val WarmNeumorphismLightColorScheme = lightColorScheme(
    primary = WarmNeumorphismColors.WarmOrange,
    onPrimary = WarmNeumorphismColors.CreamWhite,
    primaryContainer = WarmNeumorphismColors.WarmOrangeLight,
    onPrimaryContainer = WarmNeumorphismColors.TextPrimary,
    
    secondary = SoftBrown,
    onSecondary = WarmNeumorphismColors.CreamWhite,
    secondaryContainer = SoftBrownLight,
    onSecondaryContainer = WarmNeumorphismColors.TextPrimary,
    
    tertiary = SoftPink,
    onTertiary = WarmNeumorphismColors.CreamWhite,
    tertiaryContainer = SoftPinkLight,
    onTertiaryContainer = WarmNeumorphismColors.TextPrimary,
    
    background = WarmNeumorphismColors.CreamWhiteLight,
    onBackground = WarmNeumorphismColors.TextPrimary,
    
    surface = WarmNeumorphismColors.SurfacePrimary,
    onSurface = WarmNeumorphismColors.TextPrimary,
    surfaceVariant = WarmNeumorphismColors.SurfaceSecondary,
    onSurfaceVariant = WarmNeumorphismColors.SurfaceTertiary,
    
    outline = WarmNeumorphismColors.BorderMedium,
    outlineVariant = WarmNeumorphismColors.BorderLight,
    
    error = ErrorRed,
    onError = WarmNeumorphismColors.CreamWhite,
    errorContainer = ErrorRedLight,
    onErrorContainer = WarmNeumorphismColors.TextPrimary,
    
    inverseSurface = WarmNeumorphismColors.TextPrimary,
    inverseOnSurface = WarmNeumorphismColors.CreamWhite,
    inversePrimary = WarmNeumorphismColors.WarmOrangeLight,
)

// 兼容性配色方案（保持向后兼容）
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

/**
 * 游戏营地主题
 * 使用暖质拟态风配色方案，提供温暖舒适的用户体验
 */
@Composable
fun GameCampTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    // 使用暖质拟态风配色
    useWarmNeumorphism: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        
        useWarmNeumorphism -> {
            if (darkTheme) WarmNeumorphismDarkColorScheme else WarmNeumorphismLightColorScheme
        }
        
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // 设置沉浸式状态栏
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            
            // 启用沉浸式模式
            WindowCompat.setDecorFitsSystemWindows(window, false)
            
            // 设置状态栏图标颜色（浅色主题用深色图标，深色主题用浅色图标）
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

/**
 * 扩展属性 - 提供额外的主题颜色
 */
val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    get() = ExtendedColors(
        success = SuccessGreen,
        onSuccess = SoftGray,
        warning = WarningAmber,
        onWarning = ErrorRed,
        
        // 状态颜色
        successContainer = SuccessGreen,
        warningContainer = ErrorRed,
        errorContainer = WarningAmber,
        
        // 表面颜色变体
        surfaceVariant2 = ErrorRed,
        surfaceVariant3 = SuccessGreen,
        surfaceVariant4 = WarningAmber,
        
        // 阴影颜色
        shadow = ErrorRed,
        shadowVariant = SuccessGreenLight,
        shadowVariant2 = WarningAmberLight,
        shadowVariant3 = ErrorRedLight,
        shadowVariant4 = InfoBlueLight,
        
        // 高亮颜色
        highlight = WarmNeumorphismColors.ShadowMedium,
        highlightVariant = SuccessGreen,
        highlightVariant2 = WarningAmber,
        highlightVariant3 = ErrorRed,
    )

/**
 * 扩展颜色数据类
 */
data class ExtendedColors(
    val success: androidx.compose.ui.graphics.Color,
    val onSuccess: androidx.compose.ui.graphics.Color,
    val warning: androidx.compose.ui.graphics.Color,
    val onWarning: androidx.compose.ui.graphics.Color,
    val successContainer: androidx.compose.ui.graphics.Color,
    val warningContainer: androidx.compose.ui.graphics.Color,
    val errorContainer: androidx.compose.ui.graphics.Color,
    val surfaceVariant2: androidx.compose.ui.graphics.Color,
    val surfaceVariant3: androidx.compose.ui.graphics.Color,
    val surfaceVariant4: androidx.compose.ui.graphics.Color,
    val shadow: androidx.compose.ui.graphics.Color,
    val shadowVariant: androidx.compose.ui.graphics.Color,
    val shadowVariant2: androidx.compose.ui.graphics.Color,
    val shadowVariant3: androidx.compose.ui.graphics.Color,
    val shadowVariant4: androidx.compose.ui.graphics.Color,
    val highlight: androidx.compose.ui.graphics.Color,
    val highlightVariant: androidx.compose.ui.graphics.Color,
    val highlightVariant2: androidx.compose.ui.graphics.Color,
    val highlightVariant3: androidx.compose.ui.graphics.Color,
)