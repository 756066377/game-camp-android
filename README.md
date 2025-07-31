# GameCamp Android 应用

## 一、项目简介
GameCamp 是一款基于 Jetpack Compose 与 MVVM 架构的 Android 辅助工具应用，提供驱动管理、系统监控与游戏辅助功能。通过灵活的模块化设计和易扩展的架构，旨在帮助用户对设备驱动和游戏性能进行智能化管理。

## 二、主要功能
- **数据看板**（Dashboard）：展示设备信息、CPU/内存/GPU 监控、网络状态等。  
- **驱动管理**（Driver Management）：检测、安装、重置驱动，确保系统兼容性。  
- **游戏辅助**（Game Assistant）：丝滑 ESP、物资 ESP、独家触摸自瞄、稳定压强算法，并提供使用说明与常见问题解答。

## 三、技术栈
- 语言：Kotlin  
- UI：Jetpack Compose (Material3)  
- 架构：MVVM + Hilt 依赖注入  
- 网络/存储：协程 + Flow + Room（如需扩展）  
- 构建：Gradle (Kotlin DSL)  
- 依赖管理：Hilt、Compose Navigation、Accompanist 等

## 四、环境准备
1. 安装 [Android Studio Arctic Fox](https://developer.android.com/studio) 及以上版本  
2. 安装 JDK 11 或更高版本  
3. 设置环境变量 `JAVA_HOME` 指向 JDK 安装目录  
4. Clone 本仓库并打开 Android Studio 导入项目  
5. Gradle 会自动下载依赖，无需手动干预

## 五、编译与运行
```bash
# 进入项目根目录
cd game-camp-android

# 使用 Gradle Wrapper 运行（首次会自动下载 Gradle）
./gradlew clean assembleDebug

# 在设备或模拟器上运行
./gradlew installDebug
```
> 或直接在 Android Studio 中点击 Run 按钮。

## 六、项目结构
```
game-camp-android/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/gamecamp/
│   │   │   │   ├── ui/              # UI 层：Compose 页面、组件
│   │   │   │   ├── viewmodel/       # ViewModel
│   │   │   │   ├── repository/      # 数据仓库
│   │   │   ├── res/                 # 资源文件（布局、主题、图标等）
│   ├── build.gradle                 # 模块配置
├── build.gradle                     # 根项目配置
├── settings.gradle
└── gradle/
```

## 七、核心模块说明
- **ui/screens**：Compose 页面，包含 `DashboardScreen`, `DriverScreen`, `GameAssistantScreen`。  
- **ui/components**：可复用 UI 组件，如导航栏、卡片、对话框等。  
- **viewmodel**：MVVM 中的业务逻辑和状态管理，通过 `DriverViewModel`、`DashboardViewModel` 等协调 UI 与数据。  
- **repository**：封装数据来源与驱动安装逻辑，统一接口，便于单元测试。  
- **util**：工具类集合，如 Root 授权、权限管理、系统信息获取等。

## 八、开发规范
- **函数级注释**：每个函数必须使用 Kotlin 文档注释，说明参数与返回值含义。  
- **命名规范**：变量与函数采用有意义名称，遵循驼峰命名法，避免单字母或模糊命名。  
- **错误处理**：外部 API 或系统调用必须捕获异常，并在 UI 层给出友好提示。  
- **类型提示**：函数参数与返回值均需显式类型声明。  
- **架构指南**：遵循《Kotlin 项目代码规范与架构指南》，保持清晰的层次分离与高内聚低耦合。

## 九、贡献指南
1. Fork 本仓库并新建分支：`feature/xxx` 或 `fix/xxx`  
2. 提交代码前请先运行 `./gradlew check` 通过所有检测  
3. 发起 Pull Request，描述本次变更内容与测试结果  

## 十、常见问题
- 无法编译或依赖下载慢：请检查网络代理或更换 Gradle 镜像。  
- Hilt 注入失败：确认在 `Application` 类中已正确使用 `@HiltAndroidApp` 并配置 `AppModule`。  
- Compose 布局异常：重启 Android Studio 并清理缓存(`File -> Invalidate Caches / Restart`)。
