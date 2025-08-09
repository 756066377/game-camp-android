# GameCamp Android 客户端

项目简介
- 名称：GameCamp（Android 客户端）
- 包名：com.gamecamp
- 技术栈：Kotlin + Jetpack Compose + Hilt(Dagger) + MVVM
- 主要功能：驱动安装/管理、设备信息看板、游戏辅助功能、终端样式日志显示与丰富动画组件。

关键模块
- app/src/main/java/com/gamecamp/
  - GameCampApplication.kt：Hilt 注解的 Application，应用初始化入口。
  - MainActivity.kt：Compose 主 Activity，处理运行时权限并启动 UI。
  - di/AppModule.kt：Hilt DI 提供全局单例（SystemInfoManager、DriverChecker、RootChecker、DriverStatusManager）。
  - repository/DriverRepository.kt：驱动业务逻辑（选择 assets/drivers 中脚本、复制到 cache、以 su 执行脚本、读取输出流并通过回调返回日志）。
  - data/DriverStatusManager.kt：使用 SharedPreferences 保存驱动安装状态与设置，提供重置/重启逻辑。
  - viewmodel/：DashboardViewModel、DriverViewModel — 暴露 StateFlow，管理 UI 状态与安装流程。
  - ui/：Compose 层（screens、components、animation、theme），包含终端对话框、底部导航与动画工具。
  - util/：RootChecker、DriverChecker、SystemInfoManager 等工具类。

资源位置
- 驱动脚本：app/src/main/assets/drivers/RT-devpro/*.sh（按内核版本组织）
- 图标与主题资源：app/src/main/res/

运行与构建（本地）
1. 使用 Android Studio 打开项目（或在项目根目录使用 Gradle wrapper）。
2. 构建 debug 包：
   - Windows: `.\gradlew.bat assembleDebug`
3. 在设备/模拟器上运行，注意授予存储权限（Android Q 及以下需 WRITE_EXTERNAL_STORAGE）。

重要注意事项
- 驱动安装依赖设备已获得 root 权限（Repository 通过 su 调用脚本）。在非 root 设备上安装会失败。
- Repository 使用 Runtime.exec 调用 su 和脚本，需注意权限、I/O 及并发读取 stdout/stderr 的异常处理。
- 项目内有 TODO（辅助功能实现留空），请参考 DriverViewModel 中相关注释继续实现。
- 操作驱动脚本具有潜在风险，请在理解脚本作用并备份数据后操作。

贡献指南
- 提交前确保代码风格与现有架构一致：MVVM + Hilt + Compose 组件化。
- 更新驱动脚本请放到 assets/drivers/{folder} 并同步修改 DriverRepository 的映射（如有需要）。
- 提交示例：
  - git add <files>
  - git commit -m "feat: 描述性信息"
  - git push origin <branch>

常见命令（本仓库）
- 查看当前分支：`git rev-parse --abbrev-ref HEAD`
- 推送当前分支：`git push origin $(git rev-parse --abbrev-ref HEAD)`

许可证
- 未指定，请在需要时添加 LICENSE 文件。

如需我将 README 内容调整为英文、添加更详细文件清单或包含 mermaid 架构图，请说明。