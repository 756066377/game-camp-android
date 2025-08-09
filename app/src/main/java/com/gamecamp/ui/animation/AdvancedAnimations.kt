package com.gamecamp.ui.animation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.*

/**
 * 高级动效概念实现
 * 包含粒子系统、物理动画、手势跟随等
 */

/**
 * 粒子系统动画
 */
@Composable
fun ParticleSystem(
    isActive: Boolean,
    particleCount: Int = 20,
    modifier: Modifier = Modifier,
    particleColor: Color = MaterialTheme.colorScheme.primary
) {
    val particles = remember {
        List(particleCount) { Particle() }
    }
    
    LaunchedEffect(isActive) {
        if (isActive) {
            particles.forEach { it.reset() }
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "particle_system")
    
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_progress"
    )
    
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        if (isActive) {
            particles.forEach { particle ->
                particle.update(animationProgress, size)
                drawCircle(
                    color = particleColor.copy(alpha = particle.alpha),
                    radius = particle.size,
                    center = Offset(particle.x, particle.y)
                )
            }
        }
    }
}

/**
 * 粒子数据类
 */
private class Particle {
    var x: Float = 0f
    var y: Float = 0f
    var velocityX: Float = 0f
    var velocityY: Float = 0f
    var size: Float = 0f
    var alpha: Float = 1f
    var life: Float = 1f
    
    fun reset() {
        x = (0..1000).random().toFloat()
        y = (0..1000).random().toFloat()
        velocityX = (-50..50).random().toFloat()
        velocityY = (-100..-20).random().toFloat()
        size = (2..8).random().toFloat()
        alpha = 1f
        life = 1f
    }
    
    fun update(progress: Float, canvasSize: androidx.compose.ui.geometry.Size) {
        x += velocityX * 0.016f // 假设60fps
        y += velocityY * 0.016f
        
        life = 1f - progress
        alpha = life
        
        // 重力效果
        velocityY += 20f * 0.016f
        
        // 边界检查
        if (x < 0 || x > canvasSize.width || y > canvasSize.height) {
            reset()
        }
    }
}

/**
 * 波纹扩散动画
 */
@Composable
fun RippleEffect(
    isActive: Boolean,
    center: Offset = Offset.Zero,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val animatedRadius by animateFloatAsState(
        targetValue = if (isActive) 200f else 0f,
        animationSpec = tween(
            durationMillis = 600,
            easing = AnimationUtils.FastOutSlowInEasing
        ),
        label = "ripple_radius"
    )
    
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isActive) 0f else 0.3f,
        animationSpec = tween(
            durationMillis = 600,
            easing = AnimationUtils.FastOutSlowInEasing
        ),
        label = "ripple_alpha"
    )
    
    Canvas(modifier = modifier.fillMaxSize()) {
        if (isActive && animatedRadius > 0f) {
            drawCircle(
                color = color.copy(alpha = animatedAlpha),
                radius = animatedRadius,
                center = if (center == Offset.Zero) this.center else center,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

/**
 * 弹性拖拽组件
 */
@Composable
fun ElasticDraggable(
    modifier: Modifier = Modifier,
    elasticity: Float = 0.8f,
    content: @Composable () -> Unit
) {
    var offset by remember { mutableStateOf(Offset.Zero) }
    val animatedOffset by animateOffsetAsState(
        targetValue = Offset.Zero,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "elastic_offset"
    )
    
    Box(
        modifier = modifier
            .offset {
                IntOffset(
                    (offset.x + animatedOffset.x).roundToInt(),
                    (offset.y + animatedOffset.y).roundToInt()
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        offset = Offset.Zero
                    }
                ) { _, dragAmount ->
                    offset += dragAmount * elasticity
                }
            }
    ) {
        content()
    }
}

/**
 * 磁性吸附动画
 */
@Composable
fun MagneticSnap(
    targetPositions: List<Offset>,
    snapDistance: Float = 100f,
    modifier: Modifier = Modifier,
    content: @Composable (currentPosition: Offset) -> Unit
) {
    var position by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }
    
    val animatedPosition by animateOffsetAsState(
        targetValue = if (isDragging) position else findNearestSnapPoint(position, targetPositions, snapDistance),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "magnetic_position"
    )
    
    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = { isDragging = false }
                ) { _, dragAmount ->
                    position += dragAmount
                }
            }
    ) {
        content(animatedPosition)
    }
}

/**
 * 找到最近的吸附点
 */
private fun findNearestSnapPoint(
    currentPosition: Offset,
    snapPoints: List<Offset>,
    snapDistance: Float
): Offset {
    val nearestPoint = snapPoints.minByOrNull { snapPoint ->
        (currentPosition - snapPoint).getDistance()
    }
    
    return if (nearestPoint != null && (currentPosition - nearestPoint).getDistance() <= snapDistance) {
        nearestPoint
    } else {
        currentPosition
    }
}

/**
 * 路径动画
 */
@Composable
fun PathAnimation(
    path: Path,
    isAnimating: Boolean,
    duration: Int = 2000,
    modifier: Modifier = Modifier,
    content: @Composable (progress: Float, position: Offset) -> Unit
) {
    val pathMeasure = remember(path) { PathMeasure().apply { setPath(path, false) } }
    val pathLength = remember(pathMeasure) { pathMeasure.length }
    
    val animatedProgress by animateFloatAsState(
        targetValue = if (isAnimating) 1f else 0f,
        animationSpec = tween(
            durationMillis = duration,
            easing = AnimationUtils.FastOutSlowInEasing
        ),
        label = "path_progress"
    )
    
    val currentPosition = remember(animatedProgress, pathLength) {
        // 简化版路径位置计算
        val progress = animatedProgress
        Offset(progress * 100f, progress * 100f)
    }
}

/**
 * 3D翻转动画
 */
@Composable
fun Flip3D(
    isFlipped: Boolean,
    modifier: Modifier = Modifier,
    axis: FlipAxis = FlipAxis.Y,
    frontContent: @Composable () -> Unit,
    backContent: @Composable () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(
            durationMillis = 600,
            easing = AnimationUtils.FastOutSlowInEasing
        ),
        label = "flip_rotation"
    )
    
    Box(
        modifier = modifier.graphicsLayer {
            when (axis) {
                FlipAxis.X -> rotationX = rotation
                FlipAxis.Y -> rotationY = rotation
            }
            cameraDistance = 12f * density
        }
    ) {
        if (rotation <= 90f) {
            frontContent()
        } else {
            Box(
                modifier = Modifier.graphicsLayer {
                    when (axis) {
                        FlipAxis.X -> rotationX = 180f
                        FlipAxis.Y -> rotationY = 180f
                    }
                }
            ) {
                backContent()
            }
        }
    }
}

enum class FlipAxis { X, Y }

/**
 * 液体动画效果
 */
@Composable
fun LiquidAnimation(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    waveHeight: Float = 20f,
    waveFrequency: Float = 2f
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 1000,
            easing = AnimationUtils.FastOutSlowInEasing
        ),
        label = "liquid_progress"
    )
    
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_wave")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_offset"
    )
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val liquidHeight = height * animatedProgress
        
        val path = Path().apply {
            moveTo(0f, height)
            lineTo(0f, height - liquidHeight + waveHeight)
            
            // 创建波浪效果
            for (x in 0..width.toInt() step 5) {
                val y = height - liquidHeight + 
                        sin((x / width * waveFrequency * 2 * PI + waveOffset).toDouble()).toFloat() * waveHeight
                lineTo(x.toFloat(), y)
            }
            
            lineTo(width, height - liquidHeight + waveHeight)
            lineTo(width, height)
            close()
        }
        
        drawPath(
            path = path,
            color = color,
            style = Fill
        )
    }
}

/**
 * Offset动画状态
 */
@Composable
fun animateOffsetAsState(
    targetValue: Offset,
    animationSpec: AnimationSpec<Offset> = spring(),
    label: String = "OffsetAnimation",
    finishedListener: ((Offset) -> Unit)? = null
): State<Offset> {
    return animateValueAsState(
        targetValue = targetValue,
        typeConverter = Offset.VectorConverter,
        animationSpec = animationSpec,
        label = label,
        finishedListener = finishedListener
    )
}