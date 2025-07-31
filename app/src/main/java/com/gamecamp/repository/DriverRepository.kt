package com.gamecamp.repository

import com.gamecamp.data.DriverStatusManager
import com.gamecamp.util.RootChecker
import kotlinx.coroutines.delay
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
    private val driverStatusManager: DriverStatusManager,
    private val rootChecker: RootChecker
) {

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
     * 需要Root权限才能成功安装
     */
    suspend fun installDriver(driverName: String): DriverInstallResult {
        return try {
            // 首先检查Root权限
            if (!hasRootPermission()) {
                return DriverInstallResult.Error("驱动安装需要Root权限。\n\n请先获取Root权限后再试。\n\n提示：Root权限是安装系统级驱动的必要条件。")
            }
            
            // 模拟驱动安装过程（实际项目中这里会是真实的驱动安装逻辑）
            delay(2000)
            
            // 模拟安装可能失败的情况
            val installSuccess = simulateDriverInstallation(driverName)
            
            if (installSuccess) {
                // 设置驱动状态
                driverStatusManager.setDriverInstalled(true)
                driverStatusManager.setSelectedDriver(driverName)
                
                DriverInstallResult.Success
            } else {
                DriverInstallResult.Error("驱动安装失败，请检查设备兼容性或重试。")
            }
        } catch (e: Exception) {
            DriverInstallResult.Error("安装异常: ${e.message}")
        }
    }
    
    /**
     * 模拟驱动安装过程
     * 在实际项目中，这里会包含真实的驱动安装逻辑
     */
    private suspend fun simulateDriverInstallation(driverName: String): Boolean {
        // 模拟安装过程中的各种检查
        delay(1000)
        
        // 检查设备兼容性（这里简化为随机成功，实际项目中会有真实的检查逻辑）
        val isCompatible = checkDeviceCompatibility(driverName)
        if (!isCompatible) {
            return false
        }
        
        // 模拟驱动文件写入过程
        delay(500)
        
        // 模拟系统配置更新
        delay(500)
        
        return true
    }
    
    /**
     * 检查设备兼容性
     */
    private fun checkDeviceCompatibility(driverName: String): Boolean {
        // 在实际项目中，这里会检查设备型号、Android版本、架构等
        // 这里简化为总是兼容
        return true
    }

    /**
     * 重置驱动
     * @return 重置结果
     */
    suspend fun resetDriver(): DriverResetResult {
        return try {
            // 检查Root权限
            if (!hasRootPermission()) {
                return DriverResetResult.NoRootPermission
            }

            // 模拟重置过程
            delay(1000)
            
            // 完全重置驱动状态 - 这是关键修复
            driverStatusManager.resetDriverStatus()
            
            // 尝试重启设备
            val rebootSuccess = driverStatusManager.rebootDevice()
            if (!rebootSuccess) {
                return DriverResetResult.RebootFailed
            }
            
            DriverResetResult.Success
        } catch (e: Exception) {
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
}