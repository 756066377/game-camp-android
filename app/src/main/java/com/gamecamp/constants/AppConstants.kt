package com.gamecamp.constants

/**
 * 应用程序常量定义
 */
object AppConstants {
    
    /**
     * 驱动相关常量
     */
    object Driver {
        const val RTPRO_DRIVER = "RTPRO驱动"
        const val RTHOOK_DRIVER = "RThook驱动"
        const val GT_DRIVER = "GT驱动"
        
        val AVAILABLE_DRIVERS = listOf(
            RTPRO_DRIVER,
            RTHOOK_DRIVER,
            GT_DRIVER
        )
        
        const val DEFAULT_DRIVER = RTPRO_DRIVER
    }
    
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