package com.gamecamp.ui.animation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

/**
 * 动画工具类
 * 提供各种常用的动画效果和转场动画
 */
object AnimationUtils {
    
    // 动画时长常量
    const val FAST_ANIMATION = 200
    const val NORMAL_ANIMATION = 300
    const val SLOW_ANIMATION = 500
    
    // 缓动函数
    val FastOutSlowInEasing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
    val FastOutLinearInEasing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
    val LinearOutSlowInEasing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
    
    /**
     * 弹性动画规格
     */
    fun springSpec(
        dampingRatio: Float = Spring.DampingRatioMediumBouncy,
        stiffness: Float = Spring.StiffnessMedium
    ) = spring<Float>(
        dampingRatio = dampingRatio,
        stiffness = stiffness
    )
    
    /**
     * 补间动画规格
     */
    fun tweenSpec(
        durationMillis: Int = NORMAL_ANIMATION,
        easing: Easing = FastOutSlowInEasing
    ) = tween<Float>(
        durationMillis = durationMillis,
        easing = easing
    )
    
    /**
     * 页面滑入转场动画
     */
    fun slideInFromRight(): EnterTransition {
        return slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(NORMAL_ANIMATION)
        ) + fadeIn(animationSpec = tween(NORMAL_ANIMATION))
    }
    
    fun slideInFromLeft(): EnterTransition {
        return slideInHorizontally(
            initialOffsetX = { fullWidth -> -fullWidth },
            animationSpec = tween(NORMAL_ANIMATION)
        ) + fadeIn(animationSpec = tween(NORMAL_ANIMATION))
    }
    
    fun slideInFromBottom(): EnterTransition {
        return slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(NORMAL_ANIMATION)
        ) + fadeIn(animationSpec = tween(NORMAL_ANIMATION))
    }
    
    /**
     * 页面滑出转场动画
     */
    fun slideOutToLeft(): ExitTransition {
        return slideOutHorizontally(
            targetOffsetX = { fullWidth -> -fullWidth },
            animationSpec = tween(NORMAL_ANIMATION)
        ) + fadeOut(animationSpec = tween(NORMAL_ANIMATION))
    }
    
    fun slideOutToRight(): ExitTransition {
        return slideOutHorizontally(
            targetOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(NORMAL_ANIMATION)
        ) + fadeOut(animationSpec = tween(NORMAL_ANIMATION))
    }
    
    fun slideOutToBottom(): ExitTransition {
        return slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(NORMAL_ANIMATION)
        ) + fadeOut(animationSpec = tween(NORMAL_ANIMATION))
    }

    /**
     * 现代化平滑页面进入动效
     * - 轻微水平位移（约屏幕宽度的1/6）
     * - 透明度渐入
     * - 细微缩放（避免弹跳感）
     */
    fun smoothPageEnter(
        duration: Int = NORMAL_ANIMATION
    ): EnterTransition {
        return slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth / 6 },
            animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing)) + androidx.compose.animation.scaleIn(
            initialScale = 0.995f,
            transformOrigin = TransformOrigin.Center,
            animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing)
        )
    }

    /**
     * 现代化平滑页面退出动效（与进入动效对称）
     * - 轻微向左位移（约屏幕宽度的1/6）
     * - 透明度渐出
     * - 细微缩放
     */
    fun smoothPageExit(
        duration: Int = NORMAL_ANIMATION
    ): ExitTransition {
        return slideOutHorizontally(
            targetOffsetX = { fullWidth -> -fullWidth / 6 },
            animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing)) + androidx.compose.animation.scaleOut(
            targetScale = 0.995f,
            transformOrigin = TransformOrigin.Center,
            animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing)
        )
    }
    
    /**
     * 缩放动画
     */
    fun scaleIn(
        initialScale: Float = 0.8f,
        transformOrigin: TransformOrigin = TransformOrigin.Center
    ): EnterTransition {
        return scaleIn(
            initialScale = initialScale,
            transformOrigin = transformOrigin,
            animationSpec = tweenSpec()
        ) + fadeIn(animationSpec = tweenSpec())
    }
    
    fun scaleOut(
        targetScale: Float = 0.8f,
        transformOrigin: TransformOrigin = TransformOrigin.Center
    ): ExitTransition {
        return scaleOut(
            targetScale = targetScale,
            transformOrigin = transformOrigin,
            animationSpec = tweenSpec()
        ) + fadeOut(animationSpec = tweenSpec())
    }
    
    /**
     * 对话框动画
     */
    fun dialogEnterTransition(): EnterTransition {
        return scaleIn(
            initialScale = 0.8f,
            transformOrigin = TransformOrigin.Center
        ) + fadeIn(animationSpec = tweenSpec(FAST_ANIMATION))
    }
    
    fun dialogExitTransition(): ExitTransition {
        return scaleOut(
            targetScale = 0.8f,
            transformOrigin = TransformOrigin.Center
        ) + fadeOut(animationSpec = tweenSpec(FAST_ANIMATION))
    }
    
    /**
     * 抖动动画
     */
    @Composable
    fun rememberShakeController(): ShakeController {
        return remember { ShakeController() }
    }
}

/**
 * 抖动控制器
 */
class ShakeController {
    private var _shakeConfig by mutableStateOf<ShakeConfig?>(null)
    val shakeConfig: ShakeConfig? get() = _shakeConfig
    
    /**
     * 触发抖动动画
     */
    fun shake(
        iterations: Int = 4,
        intensity: Float = 10f,
        duration: Int = AnimationUtils.FAST_ANIMATION
    ) {
        _shakeConfig = ShakeConfig(
            iterations = iterations,
            intensity = intensity,
            duration = duration
        )
    }
    
    /**
     * 重置抖动状态
     */
    fun reset() {
        _shakeConfig = null
    }
}

/**
 * 抖动配置
 */
data class ShakeConfig(
    val iterations: Int,
    val intensity: Float,
    val duration: Int
)

/**
 * 抖动修饰符
 */
@Composable
fun Modifier.shake(controller: ShakeController): Modifier {
    val shakeConfig = controller.shakeConfig
    
    val offsetX by animateFloatAsState(
        targetValue = if (shakeConfig != null) shakeConfig.intensity else 0f,
        animationSpec = if (shakeConfig != null) {
            repeatable(
                iterations = shakeConfig.iterations,
                animation = tween(shakeConfig.duration / shakeConfig.iterations),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            tween(0)
        },
        finishedListener = {
            controller.reset()
        },
        label = "shake"
    )
    
    return this.then(
        Modifier.offset(x = offsetX.dp)
    )
}

/**
 * 脉冲动画修饰符
 */
@Composable
fun Modifier.pulse(
    enabled: Boolean = true,
    minScale: Float = 0.95f,
    maxScale: Float = 1.05f,
    duration: Int = 1000
): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(duration),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    return if (enabled) {
        this.then(Modifier.graphicsLayer(scaleX = scale, scaleY = scale))
    } else {
        this
    }
}

/**
 * 呼吸动画修饰符
 */
@Composable
fun Modifier.breathe(
    enabled: Boolean = true,
    minAlpha: Float = 0.6f,
    maxAlpha: Float = 1.0f,
    duration: Int = 2000
): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "breathe")
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = minAlpha,
        targetValue = maxAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe_alpha"
    )
    
    return if (enabled) {
        this.then(Modifier.alpha(alpha))
    } else {
        this
    }
}

/**
 * 共享元素转场动画容器
 */
@Composable
fun SharedElementTransition(
    key: Any,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // 这里可以实现共享元素转场的逻辑
    // 目前先用简单的Box包装
    Box(modifier = modifier) {
        content()
    }
}