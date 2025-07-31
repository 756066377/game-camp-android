package com.gamecamp.util

import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Root权限检查工具类
 */
@Singleton
class RootChecker @Inject constructor() {
    
    /**
     * 检查设备是否已获得Root权限
     * @return true表示已获得Root权限，false表示未获得
     */
    fun isRooted(): Boolean {
        return checkRootMethod1() || checkRootMethod2() || checkRootMethod3()
    }
    
    /**
     * 方法1：检查常见的Root文件是否存在
     */
    private fun checkRootMethod1(): Boolean {
        val buildTags = android.os.Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }
    
    /**
     * 方法2：检查su二进制文件是否存在
     */
    private fun checkRootMethod2(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )
        
        for (path in paths) {
            if (File(path).exists()) return true
        }
        return false
    }
    
    /**
     * 方法3：尝试执行su命令
     */
    private fun checkRootMethod3(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val `in` = java.io.BufferedReader(java.io.InputStreamReader(process.inputStream))
            `in`.readLine() != null
        } catch (t: Throwable) {
            false
        }
    }
    
    /**
     * 检查是否可以执行su命令
     */
    fun canExecuteSuCommand(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su")
            process.destroy()
            true
        } catch (e: Exception) {
            false
        }
    }
}