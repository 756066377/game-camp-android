package com.gamecamp.constants

/**
 * 应用程序常量定义
 * 注意：驱动相关常量已移至 DriverConstants.kt 文件中统一管理
 */
object AppConstants {
    
    /**
     * UI 相关常量
     */
    object UI {
        const val DRIVER_INSTALL_DELAY = 2000L
        const val DRIVER_RESET_DELAY = 1000L
    }
    
    /**
     * 错误消息常量
     */
    object ErrorMessages {
        const val NO_ROOT_PERMISSION = "设备未获得Root权限，无法执行重启操作"
        const val REBOOT_FAILED = "重启命令执行失败，请手动重启设备"
        const val DRIVER_INSTALL_FAILED = "驱动安装失败，请重试"
        const val DRIVER_RESET_FAILED = "驱动重置失败，请重试"
    }
    
    /**
     * 成功消息常量
     */
    object SuccessMessages {
        const val DRIVER_INSTALLED = "驱动安装成功"
        const val DRIVER_RESET = "驱动重置成功，设备即将重启"
    }
}
