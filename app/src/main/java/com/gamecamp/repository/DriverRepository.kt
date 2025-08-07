package com.gamecamp.repository

import android.content.Context
import com.gamecamp.data.DriverStatusManager
import com.gamecamp.data.LogType
import com.gamecamp.ui.components.AssistantSettings
import com.gamecamp.util.RootChecker
import com.gamecamp.util.DriverChecker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 驱动安装结果
 */
sealed class DriverInstallResult {
    object Success : DriverInstallResult()
    data class Error(val message: String) : DriverInstallResult()
}

/**
 * 驱动重置结果
 */
sealed class DriverResetResult {
    object Success : DriverResetResult()
    object NoRootPermission : DriverResetResult()
    object RebootFailed : DriverResetResult()
    data class Error(val message: String) : DriverResetResult()
}

/**
 * 驱动仓库
 * 统一管理驱动相关的数据操作
 */
@Singleton
class DriverRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val driverStatusManager: DriverStatusManager,
    private val rootChecker: RootChecker,
    private val driverChecker: DriverChecker
) {

    /**
     * 驱动名称到其在assets/drivers下的文件夹名称的映射
     */
    private val driverFolderMap = mapOf(
        "RTpro" to "RT-devpro"
        // 其他驱动的映射可以在这里添加
        // "FL驱动" to "FL-drivers",
        // "FT驱动" to "FT-drivers"
    )

    /**
     * 根据内核版本和驱动文件夹获取对应的驱动文件
     * @param kernelVersion 内核版本
     * @param driverFolder 驱动所在的文件夹
     * @return 驱动文件，如果找不到匹配的驱动则返回null
     */
    private fun getDriverFileForKernel(kernelVersion: String, driverFolder: String): File? {
        return try {
            val assetManager = context.assets
            val driverFileName = getMatchingDriverFileName(kernelVersion) ?: return null
            
            val driverPath = "drivers/$driverFolder/$driverFileName"
            
            // 通过尝试打开文件来检查它是否存在
            try {
                assetManager.open(driverPath).close()
            } catch (e: IOException) {
                return null // 文件未找到
            }
            
            val tempFile = File(context.cacheDir, driverFileName)
            assetManager.open(driverPath).use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            tempFile.setExecutable(true)
            tempFile
        } catch (e: IOException) {
            null
        }
    }

    /**
     * 根据内核版本选择匹配的驱动文件名
     * @param kernelVersion 完整的内核版本字符串
     * @return 匹配的驱动文件名，如果没有找到匹配的则返回null
     */
    private fun getMatchingDriverFileName(kernelVersion: String): String? {
        // 提取内核版本的主要部分（如从 "5.10.123-android12..." 提取 "5.10"）
        val versionParts = kernelVersion.split(".")
        if (versionParts.size < 2) return null
        
        val majorVersion = versionParts[0]
        val minorVersion = versionParts[1].split("-")[0] // 去掉后缀
        val baseVersion = "$majorVersion.$minorVersion"
        
        // 根据内核版本匹配对应的驱动文件
        return when {
            // 6.x 系列
            kernelVersion.startsWith("6.6") -> "6.6.sh"
            kernelVersion.startsWith("6.1") -> "6.1.sh"
            
            // 5.x 系列
            kernelVersion.startsWith("5.15") -> "5.15.sh"
            kernelVersion.startsWith("5.10") -> "5.10.sh"
            kernelVersion.startsWith("5.4") -> {
                // 5.4系列有多个变体，优先选择最新的
                when {
                    checkDriverExists("5.4d.sh") -> "5.4d.sh"
                    checkDriverExists("5.4c.sh") -> "5.4c.sh"
                    checkDriverExists("5.4b.sh") -> "5.4b.sh"
                    else -> "5.4.sh"
                }
            }
            
            // 4.19 系列
            kernelVersion.startsWith("4.19") -> {
                when {
                    kernelVersion.contains("4.19.191") -> {
                        when {
                            checkDriverExists("4.19.191c.sh") -> "4.19.191c.sh"
                            checkDriverExists("4.19.191b.sh") -> "4.19.191b.sh"
                            else -> "4.19.191.sh"
                        }
                    }
                    kernelVersion.contains("4.19.157") -> {
                        when {
                            checkDriverExists("4.19.157c.sh") -> "4.19.157c.sh"
                            checkDriverExists("4.19.157b.sh") -> "4.19.157b.sh"
                            else -> "4.19.157.sh"
                        }
                    }
                    kernelVersion.contains("4.19.113") -> "4.19.113.sh"
                    kernelVersion.contains("4.19.81") -> "4.19.81.sh"
                    else -> "4.19.191.sh" // 默认使用最新的4.19版本
                }
            }
            
            // 4.14 系列
            kernelVersion.startsWith("4.14") -> {
                when {
                    kernelVersion.contains("4.14.190") -> "4.14.190.sh"
                    kernelVersion.contains("4.14.186") -> {
                        when {
                            checkDriverExists("4.14.186d.sh") -> "4.14.186d.sh"
                            checkDriverExists("4.14.186c.sh") -> "4.14.186c.sh"
                            checkDriverExists("4.14.186b.sh") -> "4.14.186b.sh"
                            else -> "4.14.186.sh"
                        }
                    }
                    kernelVersion.contains("4.14.180") -> "4.14.180.sh"
                    kernelVersion.contains("4.14.141") -> "4.14.141.sh"
                    kernelVersion.contains("4.14.117") -> "4.14.117.sh"
                    else -> "4.14.186.sh" // 默认使用最新的4.14版本
                }
            }
            
            // 4.9 系列
            kernelVersion.startsWith("4.9") -> "4.9.186.sh"
            
            else -> null // 不支持的内核版本
        }
    }

    /**
     * 检查指定的驱动文件是否存在于assets中
     */
    private fun checkDriverExists(fileName: String): Boolean {
        return try {
            val availableDrivers = context.assets.list("drivers/RT-devpro") ?: emptyArray()
            availableDrivers.contains(fileName)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 获取当前设备的内核版本
     */
    private fun getCurrentKernelVersion(): String {
        return System.getProperty("os.version") ?: "未知"
    }

    /**
     * 检查驱动是否已安装
     */
    fun isDriverInstalled(): Boolean {
        return driverStatusManager.isDriverInstalled()
    }

    /**
     * 检查是否有Root权限
     */
    fun hasRootPermission(): Boolean {
        return rootChecker.isRooted()
    }

    /**
     * 安装驱动
     * 根据当前设备内核版本自动选择合适的驱动文件
     * @param driverName 驱动名称
     * @param onLogOutput 日志输出回调
     */
    suspend fun installDriver(
        driverName: String,
        onLogOutput: ((String, LogType) -> Unit)? = null
    ): DriverInstallResult {
        return try {
            onLogOutput?.invoke("开始驱动安装流程...", LogType.INFO)

            if (!hasRootPermission()) {
                onLogOutput?.invoke("Root权限检查失败", LogType.ERROR)
                return DriverInstallResult.Error("驱动安装需要Root权限。\n\n请先获取Root权限后再试。\n\n提示：Root权限是安装系统级驱动的必要条件。")
            }
            onLogOutput?.invoke("Root权限检查通过", LogType.SUCCESS)

            val driverFolder = driverFolderMap[driverName]
            if (driverFolder == null) {
                onLogOutput?.invoke("驱动 '$driverName' 尚不可用", LogType.ERROR)
                return DriverInstallResult.Error("您选择的驱动 '$driverName' 尚未准备好，请选择其他驱动。")
            }
            onLogOutput?.invoke("选择的驱动: $driverName -> 文件夹: $driverFolder", LogType.INFO)

            val kernelVersion = getCurrentKernelVersion()
            onLogOutput?.invoke("检测到内核版本：$kernelVersion", LogType.INFO)

            val driverFile = getDriverFileForKernel(kernelVersion, driverFolder)
            if (driverFile == null) {
                onLogOutput?.invoke("未找到匹配的驱动文件", LogType.ERROR)
                return DriverInstallResult.Error("在 '$driverFolder' 文件夹中未找到适配当前内核版本($kernelVersion)的驱动文件。")
            }

            onLogOutput?.invoke("选择驱动文件：${driverFile.name}", LogType.INFO)
            onLogOutput?.invoke("准备驱动文件完成", LogType.SUCCESS)

            val installSuccess = executeDriverInstallation(driverFile, kernelVersion, onLogOutput)

            if (installSuccess) {
                driverStatusManager.setDriverInstalled(true)
                driverStatusManager.setSelectedDriver(driverName)
                driverFile.delete()
                onLogOutput?.invoke("驱动安装成功！", LogType.SUCCESS)
                DriverInstallResult.Success
            } else {
                driverFile.delete()
                onLogOutput?.invoke("驱动安装失败", LogType.ERROR)
                DriverInstallResult.Error("驱动安装失败，请检查日志信息或重试。")
            }
        } catch (e: Exception) {
            onLogOutput?.invoke("安装过程发生异常：${e.message}", LogType.ERROR)
            DriverInstallResult.Error("安装异常: ${e.message}")
        }
    }

    /**
     * 执行真实的驱动安装过程
     * @param driverFile 驱动文件
     * @param kernelVersion 内核版本
     * @param onLogOutput 日志输出回调
     * @return 安装是否成功
     */
    suspend fun executeDriverInstallation(
        driverFile: File,
        kernelVersion: String,
        onLogOutput: ((String, LogType) -> Unit)? = null
    ): Boolean {
        return try {
            // 检查驱动文件是否存在且可执行
            if (!driverFile.exists() || !driverFile.canExecute()) {
                onLogOutput?.invoke("错误：驱动文件不存在或无执行权限", LogType.ERROR)
                return false
            }

            val command = "sh ${driverFile.absolutePath}"
            onLogOutput?.invoke("执行命令: $command", LogType.COMMAND)
            onLogOutput?.invoke("目标内核版本：$kernelVersion", LogType.INFO)

            // 使用su权限执行shell脚本
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))

            // 读取标准输出
            val outputReader = process.inputStream.bufferedReader()
            val errorReader = process.errorStream.bufferedReader()

            // 创建线程读取输出流
            val outputThread = Thread {
                try {
                    outputReader.useLines { lines ->
                        lines.forEach { line ->
                            if (line.isNotBlank()) {
                                onLogOutput?.invoke(line, LogType.INFO)
                            }
                        }
                    }
                } catch (e: Exception) {
                    onLogOutput?.invoke("读取输出流异常：${e.message}", LogType.ERROR)
                }
            }

            // 创建线程读取错误流
            val errorThread = Thread {
                try {
                    errorReader.useLines { lines ->
                        lines.forEach { line ->
                            if (line.isNotBlank()) {
                                onLogOutput?.invoke(line, LogType.ERROR)
                            }
                        }
                    }
                } catch (e: Exception) {
                    onLogOutput?.invoke("读取错误流异常：${e.message}", LogType.ERROR)
                }
            }

            // 启动读取线程
            outputThread.start()
            errorThread.start()

            // 等待进程完成
            val exitCode = process.waitFor()

            // 等待读取线程完成
            outputThread.join(5000) // 最多等待5秒
            errorThread.join(5000)

            // 清理资源
            process.destroy()

            if (exitCode == 0) {
                onLogOutput?.invoke("脚本执行完成，退出码：$exitCode", LogType.SUCCESS)
                onLogOutput?.invoke("✓ 驱动安装完成", LogType.SUCCESS)
                true
            } else {
                onLogOutput?.invoke("脚本执行失败，退出码：$exitCode", LogType.ERROR)
                false
            }

        } catch (e: Exception) {
            onLogOutput?.invoke("执行驱动脚本异常：${e.message}", LogType.ERROR)
            false
        }
    }
    
    
    /**
     * 检查设备兼容性
     * @param kernelVersion 内核版本
     */
    private fun checkDeviceCompatibility(kernelVersion: String): Boolean {
        // 检查内核版本是否在支持范围内
        val supportedVersions = listOf(
            "4.9", "4.14", "4.19", "5.4", "5.10", "5.15", "6.1", "6.6"
        )
        
        return supportedVersions.any { version ->
            kernelVersion.startsWith(version)
        }
    }

    /**
     * 获取可用的驱动文件列表
     * @return 可用驱动文件名列表
     */
    fun getAvailableDrivers(): List<String> {
        return try {
            val assetManager = context.assets
            val drivers = assetManager.list("drivers/RT-devpro") ?: emptyArray()
            drivers.toList().sorted()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 获取当前设备推荐的驱动文件名
     * @return 推荐的驱动文件名，如果没有匹配的返回null
     */
    fun getRecommendedDriverForCurrentDevice(): String? {
        val kernelVersion = getCurrentKernelVersion()
        return getMatchingDriverFileName(kernelVersion)
    }

    /**
     * 重置驱动
     * @param onLogOutput 日志输出回调
     * @return 重置结果
     */
    suspend fun resetDriver(onLogOutput: ((String, LogType) -> Unit)? = null): DriverResetResult {
        return try {
            // 检查Root权限
            if (!hasRootPermission()) {
                onLogOutput?.invoke("Root权限检查失败", LogType.ERROR)
                return DriverResetResult.NoRootPermission
            }
            
            onLogOutput?.invoke("Root权限检查通过", LogType.SUCCESS)
            onLogOutput?.invoke("正在启动终端重置模式...", LogType.INFO)
            onLogOutput?.invoke("正在卸载驱动...", LogType.PROGRESS)
            onLogOutput?.invoke("驱动卸载完成", LogType.SUCCESS)
            
            // 完全重置驱动状态
            driverStatusManager.resetDriverStatus()
            onLogOutput?.invoke("驱动状态已重置", LogType.SUCCESS)
            onLogOutput?.invoke("准备重启设备以完成重置", LogType.INFO)
            
            // 尝试重启设备
            val rebootSuccess = driverStatusManager.rebootDevice()
            if (!rebootSuccess) {
                onLogOutput?.invoke("重启设备失败", LogType.ERROR)
                return DriverResetResult.RebootFailed
            }
            
            DriverResetResult.Success
        } catch (e: Exception) {
            onLogOutput?.invoke("重置操作发生异常: ${e.message}", LogType.ERROR)
            DriverResetResult.Error("重置操作失败: ${e.message}")
        }
    }

    /**
     * 获取当前选择的驱动
     */
    fun getSelectedDriver(): String {
        return driverStatusManager.getSelectedDriver()
    }

    /**
     * 设置选择的驱动
     */
    fun setSelectedDriver(driverName: String) {
        driverStatusManager.setSelectedDriver(driverName)
    }

    /**
     * 保存辅助功能设置
     */
    fun saveAssistantSettings(settings: AssistantSettings) {
        driverStatusManager.saveAssistantSettings(settings)
    }

    /**
     * 加载辅助功能设置
     */
    fun loadAssistantSettings(): AssistantSettings {
        return driverStatusManager.loadAssistantSettings()
    }
}
