package com.gamecamp.data

import android.content.Context
import android.content.SharedPreferences
import com.gamecamp.constants.DriverConstants
import com.gamecamp.ui.components.AssistantSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 驱动状态管理器
 * 负责管理驱动的安装状态和相关数据
 */
@Singleton
class DriverStatusManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREF_NAME, Context.MODE_PRIVATE
    )

    companion object {
        private const val PREF_NAME = "driver_status"
        private const val PREF_DRIVER_INSTALLED = "driver_installed"
        private const val PREF_SELECTED_DRIVER = "selected_driver"
        private const val PREF_INSTALL_TIME = "install_time"
        private const val PREF_ANTI_SCREEN_RECORDING = "pref_anti_screen_recording"
        private const val PREF_NO_BACKGROUND_MODE = "pref_no_background_mode"
        private const val PREF_SINGLE_TRANSPARENT_MODE = "pref_single_transparent_mode"
    }

    /**
     * 检查驱动是否已安装
     */
    fun isDriverInstalled(): Boolean {
        return sharedPreferences.getBoolean(PREF_DRIVER_INSTALLED, false)
    }

    /**
     * 设置驱动安装状态
     */
    fun setDriverInstalled(installed: Boolean) {
        sharedPreferences.edit()
            .putBoolean(PREF_DRIVER_INSTALLED, installed)
            .apply()
        
        if (installed) {
            // 记录安装时间
            sharedPreferences.edit()
                .putLong(PREF_INSTALL_TIME, System.currentTimeMillis())
                .apply()
        }
    }

    /**
     * 获取当前选择的驱动
     */
    fun getSelectedDriver(): String {
        return sharedPreferences.getString(PREF_SELECTED_DRIVER, DriverConstants.DEFAULT_DRIVER) 
            ?: DriverConstants.DEFAULT_DRIVER
    }

    /**
     * 设置选择的驱动
     */
    fun setSelectedDriver(driverName: String) {
        sharedPreferences.edit()
            .putString(PREF_SELECTED_DRIVER, driverName)
            .apply()
    }

    /**
     * 获取驱动安装时间
     */
    fun getInstallTime(): Long {
        return sharedPreferences.getLong(PREF_INSTALL_TIME, 0L)
    }

    /**
     * 重置驱动状态
     */
    fun resetDriverStatus() {
        sharedPreferences.edit()
            .clear() // 清除所有数据
            .putBoolean(PREF_DRIVER_INSTALLED, false)
            .putString(PREF_SELECTED_DRIVER, DriverConstants.DEFAULT_DRIVER)
            .apply()
    }

    /**
     * 重启设备
     * 需要root权限
     * @return 重启命令是否执行成功
     */
    fun rebootDevice(): Boolean {
        return try {
            // 使用su命令重启设备，添加5秒延迟让用户有时间准备
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "sleep 3 && reboot"))
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            // 如果root权限不可用，尝试使用系统重启命令
            try {
                // 普通reboot命令可能需要特定权限
                val process = Runtime.getRuntime().exec("reboot")
                val exitCode = process.waitFor()
                exitCode == 0
            } catch (ex: Exception) {
                false
            }
        }
    }

    /**
     * 保存辅助功能设置
     */
    fun saveAssistantSettings(settings: AssistantSettings) {
        sharedPreferences.edit()
            .putBoolean(PREF_ANTI_SCREEN_RECORDING, settings.antiScreenRecording)
            .putBoolean(PREF_NO_BACKGROUND_MODE, settings.noBackgroundMode)
            .putBoolean(PREF_SINGLE_TRANSPARENT_MODE, settings.singleTransparentMode)
            .apply()
    }

    /**
     * 加载辅助功能设置
     */
    fun loadAssistantSettings(): AssistantSettings {
        return AssistantSettings(
            antiScreenRecording = sharedPreferences.getBoolean(PREF_ANTI_SCREEN_RECORDING, false),
            noBackgroundMode = sharedPreferences.getBoolean(PREF_NO_BACKGROUND_MODE, false),
            singleTransparentMode = sharedPreferences.getBoolean(PREF_SINGLE_TRANSPARENT_MODE, false)
        )
    }
}
