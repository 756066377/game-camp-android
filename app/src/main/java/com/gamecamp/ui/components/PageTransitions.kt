package com.gamecamp.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.dp
import com.gamecamp.ui.animation.AnimationUtils

/**
 * 页面转场动画容器
 * 提供各种页面切换动画效果
 */
@Composable
fun PageTransition(
    targetState: Any,
    transitionType: PageTransitionType = PageTransitionType.SlideHorizontal,
    modifier: Modifier = Modifier,
    content: @Composable (targetState: Any) -> Unit
) {
    val transitionSpec = when (transitionType) {
        PageTransitionType.SlideHorizontal -> {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(AnimationUtils.NORMAL_ANIMATION)
            ) + fadeIn(animationSpec = tween(AnimationUtils.NORMAL_ANIMATION)) togetherWith
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -fullWidth },
                        animationSpec = tween(AnimationUtils.NORMAL_ANIMATION)
                    ) + fadeOut(animationSpec = tween(AnimationUtils.NORMAL_ANIMATION))
        }
        
        PageTransitionType.SlideVertical -> {
            slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(AnimationUtils.NORMAL_ANIMATION)
            ) + fadeIn(animationSpec = tween(AnimationUtils.NORMAL_ANIMATION)) togetherWith
                    slideOutVertically(
                        targetOffsetY = { fullHeight -> -fullHeight },
                        animationSpec = tween(AnimationUtils.NORMAL_ANIMATION)
                    ) + fadeOut(animationSpec = tween(AnimationUtils.NORMAL_ANIMATION))
        }
        
        PageTransitionType.Scale -> {
            scaleIn(
                initialScale = 0.8f,
                transformOrigin = TransformOrigin.Center,
                animationSpec = AnimationUtils.tweenSpec()
            ) + fadeIn(animationSpec = AnimationUtils.tweenSpec()) togetherWith
                    scaleOut(
                        targetScale = 1.2f,
                        transformOrigin = TransformOrigin.Center,
                        animationSpec = AnimationUtils.tweenSpec()
                    ) + fadeOut(animationSpec = AnimationUtils.tweenSpec())
        }
        
        PageTransitionType.Fade -> {
            fadeIn(animationSpec = AnimationUtils.tweenSpec()) togetherWith
                    fadeOut(animationSpec = AnimationUtils.tweenSpec())
        }
        
        PageTransitionType.SharedAxis -> {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth / 3 },
                animationSpec = tween(AnimationUtils.NORMAL_ANIMATION)
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = AnimationUtils.NORMAL_ANIMATION,
                    delayMillis = 90
                )
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth / 3 },
                animationSpec = tween(AnimationUtils.NORMAL_ANIMATION)
            ) + fadeOut(
                animationSpec = tween(
                    durationMillis = 90
                )
            )
        }
    }
    
    AnimatedContent(
        targetState = targetState,
        transitionSpec = { transitionSpec },
        modifier = modifier,
        label = "page_transition"
    ) { state ->
        content(state)
    }
}

/**
 * 页面转场类型
 */
enum class PageTransitionType {
    SlideHorizontal,
    SlideVertical,
    Scale,
    Fade,
    SharedAxis
}

/**
 * 共享元素转场动画
 */
@Composable
fun SharedElementTransition(
    key: Any,
    screenKey: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // 简化版共享元素转场实现
    Box(modifier = modifier) {
        content()
    }
}

/**
 * 容器转换动画
 */
@Composable
fun ContainerTransform(
    targetState: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable (expanded: Boolean) -> Unit
) {
    val transition = updateTransition(
        targetState = targetState,
        label = "container_transform"
    )
    
    val cornerRadius by transition.animateDp(
        transitionSpec = {
            tween(
                durationMillis = AnimationUtils.NORMAL_ANIMATION,
                easing = AnimationUtils.FastOutSlowInEasing
            )
        },
        label = "corner_radius"
    ) { expanded ->
        if (expanded) 0.dp else 16.dp
    }
    
    val elevation by transition.animateDp(
        transitionSpec = {
            tween(
                durationMillis = AnimationUtils.NORMAL_ANIMATION,
                easing = AnimationUtils.FastOutSlowInEasing
            )
        },
        label = "elevation"
    ) { expanded ->
        if (expanded) 0.dp else 8.dp
    }
    
    Box(modifier = modifier) {
        content(targetState)
    }
}

/**
 * 列表项动画
 */
@Composable
fun AnimatedListItem(
    key: Any,
    modifier: Modifier = Modifier,
    enterDelay: Int = 0,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(key) {
        kotlinx.coroutines.delay(enterDelay.toLong())
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = tween(
                durationMillis = AnimationUtils.NORMAL_ANIMATION,
                easing = AnimationUtils.FastOutSlowInEasing
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = AnimationUtils.NORMAL_ANIMATION,
                easing = AnimationUtils.FastOutSlowInEasing
            )
        ),
        modifier = modifier
    ) {
        content()
    }
}

/**
 * 交错动画列表
 */
@Composable
fun StaggeredAnimationList(
    items: List<Any>,
    staggerDelay: Int = 50,
    modifier: Modifier = Modifier,
    itemContent: @Composable (item: Any, index: Int) -> Unit
) {
    items.forEachIndexed { index, item ->
        AnimatedListItem(
            key = item,
            enterDelay = index * staggerDelay,
            modifier = modifier
        ) {
            itemContent(item, index)
        }
    }
}