package com.gamecamp.util

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 驱动检查器 - 简化版
 * 移除了JNI调用，不再检查驱动安装状态
 */
@Singleton
class DriverChecker @Inject constructor() {

    companion object {
        private const val TAG = "DriverChecker"
    }

    /**
     * 简化的驱动状态检查方法
     * 始终返回true，不再进行实际检查
     */
    fun checkDriverStatus(): Boolean {
        Log.i(TAG, "驱动状态检查已禁用，默认返回true")
        return true
    }

    /**
     * 安全地检查驱动状态
     * 返回默认状态，不进行实际检查
     */
    fun checkDriverStatusSafely(): DriverStatus {
        Log.i(TAG, "驱动状态检查已禁用，返回默认状态")
        return DriverStatus(
            isInstalled = true,
            driverPath = "",
            kernelVersion = 0.0f,
            errorMessage = null
        )
    }

    /**
     * 获取详细的驱动信息
     * 返回默认信息，不进行实际检查
     */
    fun getDriverInfo(): DriverInfo {
        Log.i(TAG, "驱动信息获取已禁用，返回默认信息")
        return DriverInfo(
            isInstalled = true,
            driverPath = "",
            driverType = DriverType.UNKNOWN,
            kernelVersion = 0.0f
        )
    }

    /**
     * 驱动状态数据类
     */
    data class DriverStatus(
        val isInstalled: Boolean,
        val driverPath: String,
        val kernelVersion: Float,
        val errorMessage: String?
    )

    /**
     * 驱动详细信息数据类
     */
    data class DriverInfo(
        val isInstalled: Boolean,
        val driverPath: String,
        val driverType: DriverType,
        val kernelVersion: Float
    )

    /**
     * 驱动类型枚举
     */
    enum class DriverType {
        DEV_DRIVER,    // /dev目录下的驱动
        PROC_DRIVER,   // /proc目录下的驱动
        UNKNOWN        // 未知类型
    }
}
