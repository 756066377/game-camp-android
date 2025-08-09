# 动效系统使用指南

本项目实现了完整的动效系统，包含页面转场、终端对话框动效、交互反馈动效和高级动效概念。

## 🎬 已实现的动效功能

### 1. 页面转场动效 (PageTransitions.kt)
- **SlideHorizontal**: 水平滑动转场
- **SlideVertical**: 垂直滑动转场  
- **Scale**: 缩放转场
- **Fade**: 淡入淡出转场
- **SharedAxis**: 共享轴转场

```kotlin
PageTransition(
    targetState = currentPage,
    transitionType = PageTransitionType.SlideHorizontal
) { page ->
    // 页面内容
}
```

### 2. 终端对话框动效 (EnhancedTerminalDialog.kt)
- **打字机效果**: 逐字显示文本
- **语法高亮**: 不同日志类型的颜色区分
- **光标闪烁**: 模拟真实终端
- **自动滚动**: 新日志自动滚动到底部
- **状态指示器**: 执行中/完成状态动画

```kotlin
EnhancedTerminalDialog(
    isVisible = showDialog,
    logs = terminalLogs,
    isCompleted = isCompleted,
    onDismiss = { /* 关闭对话框 */ }
)
```

### 3. 交互反馈动效 (InteractiveButton.kt)
- **点击缩放**: 按钮按压时的缩放效果
- **加载动画**: 处理中的旋转指示器
- **颜色过渡**: 状态变化的颜色动画
- **震动反馈**: 触觉反馈
- **脉冲动画**: 禁用状态的提示动画
- **抖动效果**: 错误时的抖动动画

```kotlin
InteractiveButton(
    text = "开始操作",
    onClick = { /* 点击处理 */ },
    loading = isLoading,
    enabled = isEnabled,
    shakeController = shakeController
)
```

### 4. 高级动效概念 (AdvancedAnimations.kt)
- **粒子系统**: 动态粒子效果
- **波纹扩散**: 点击波纹动画
- **弹性拖拽**: 带回弹的拖拽效果
- **磁性吸附**: 自动吸附到目标位置
- **路径动画**: 沿路径移动的动画
- **3D翻转**: 立体翻转效果
- **液体动画**: 流体填充效果

```kotlin
// 粒子系统
ParticleSystem(
    isActive = showParticles,
    particleCount = 20
)

// 3D翻转
Flip3D(
    isFlipped = isFlipped,
    frontContent = { /* 正面内容 */ },
    backContent = { /* 背面内容 */ }
)
```

## 🛠️ 动画工具类 (AnimationUtils.kt)

### 基础动画规格
```kotlin
// 弹性动画
AnimationUtils.springSpec(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMedium
)

// 补间动画
AnimationUtils.tweenSpec(
    durationMillis = 300,
    easing = FastOutSlowInEasing
)
```

### 转场动画
```kotlin
// 滑入动画
AnimationUtils.slideInFromRight()
AnimationUtils.slideInFromBottom()

// 缩放动画
AnimationUtils.scaleIn(initialScale = 0.8f)
AnimationUtils.scaleOut(targetScale = 0.8f)
```

### 特殊效果
```kotlin
// 抖动控制器
val shakeController = AnimationUtils.rememberShakeController()
shakeController.shake(iterations = 4, intensity = 10f)

// 修饰符扩展
Modifier
    .shake(shakeController)
    .pulse(enabled = true)
    .breathe(enabled = true)
```

## 📱 实际应用示例

### 在DriverScreen中的应用
```kotlin
@Composable
fun AnimatedDriverScreen() {
    val shakeController = AnimationUtils.rememberShakeController()
    
    // 错误时触发抖动
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            shakeController.shake()
        }
    }
    
    LazyColumn {
        item {
            // 带进入动画的列表项
            AnimatedListItem(
                key = "driver_section",
                enterDelay = 0
            ) {
                // 使用增强版按钮
                InteractiveButton(
                    text = "开始刷入",
                    onClick = onInstall,
                    shakeController = shakeController
                )
            }
        }
    }
    
    // 使用增强版终端对话框
    EnhancedTerminalDialog(
        isVisible = showTerminal,
        logs = logs,
        isCompleted = completed,
        onDismiss = onDismiss
    )
}
```

## 🎨 动效设计原则

### 1. 性能优化
- 使用`remember`缓存动画状态
- 避免不必要的重组
- 合理控制动画时长(200-500ms)

### 2. 用户体验
- 提供触觉反馈
- 保持动画一致性
- 支持动画开关(无障碍)

### 3. 视觉层次
- 重要操作使用明显动效
- 次要操作使用微妙动效
- 错误状态使用警示动效

## 🔧 自定义动效

### 创建自定义动画
```kotlin
@Composable
fun CustomAnimation() {
    val animatedValue by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy
        )
    )
    
    Box(
        modifier = Modifier
            .scale(animatedValue)
            .alpha(animatedValue)
    ) {
        // 内容
    }
}
```

### 组合多个动效
```kotlin
@Composable
fun CombinedAnimation() {
    var isVisible by remember { mutableStateOf(false) }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        InteractiveButton(
            text = "组合动效",
            onClick = { },
            modifier = Modifier.pulse(enabled = true)
        )
    }
}
```

## 📋 最佳实践

1. **渐进增强**: 先实现基础功能，再添加动效
2. **性能监控**: 使用Compose工具监控动画性能
3. **用户测试**: 收集用户对动效的反馈
4. **平台适配**: 考虑不同设备的性能差异
5. **无障碍支持**: 提供动画开关选项

通过这套完整的动效系统，可以大大提升应用的用户体验和视觉吸引力。