package com.gamecamp.constants

/**
 * 驱动常量定义
 * 红色时代支持驱动更改
 */
object DriverConstants {
    
    /**
     * 支持的驱动列表
     * 如果不是4系内核等不兼容内核，建议使用FL驱动
     */
    val SUPPORTED_DRIVERS = listOf(
        "FL驱动",
        "FT驱动", 
        "QX11.4",
        "QX10",
        "QX8"
    )
    
    /**
     * 默认选中的驱动
     * 推荐使用FL驱动（兼容性最好）
     */
    const val DEFAULT_DRIVER = "FL驱动"
    
    /**
     * 驱动描述信息
     */
    val DRIVER_DESCRIPTIONS = mapOf(
        "FL驱动" to "推荐驱动，兼容性最好，适用于大部分设备",
        "FT驱动" to "高性能驱动，适用于高端设备",
        "QX11.4" to "QX系列最新版本，功能全面",
        "QX10" to "QX系列稳定版本，性能均衡",
        "QX8" to "QX系列轻量版本，资源占用低"
    )
    
    /**
     * 驱动兼容性提示
     */
    const val COMPATIBILITY_NOTICE = "如果不是4系内核等不兼容内核，建议使用FL驱动"
    
    /**
     * 驱动安装路径
     */
    const val DRIVER_INSTALL_PATH = "/system/lib/modules/"
    
    /**
     * 驱动配置文件路径
     */
    const val DRIVER_CONFIG_PATH = "/data/local/tmp/driver_config"
    
    /**
     * 驱动状态检查间隔（毫秒）
     */
    const val DRIVER_STATUS_CHECK_INTERVAL = 1000L
    
    /**
     * 驱动安装超时时间（毫秒）
     */
    const val DRIVER_INSTALL_TIMEOUT = 30000L
    
    /**
     * 获取驱动的显示名称
     */
    fun getDriverDisplayName(driverName: String): String {
        return when (driverName) {
            "FL驱动" -> "FL驱动 (推荐)"
            "FT驱动" -> "FT驱动"
            "QX11.4" -> "QX11.4"
            "QX10" -> "QX10"
            "QX8" -> "QX8"
            else -> driverName
        }
    }
    
    /**
     * 检查驱动是否为推荐驱动
     */
    fun isRecommendedDriver(driverName: String): Boolean {
        return driverName == "FL驱动"
    }
}