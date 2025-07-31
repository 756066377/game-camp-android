package com.gamecamp.util

import android.content.Context
import android.os.Build
import android.os.StatFs
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 系统信息管理器
 * 负责获取真实的设备系统信息
 */
@Singleton
class SystemInfoManager @Inject constructor(
    private val context: Context
) {

    /**
     * 获取内核信息
     */
    suspend fun getKernelInfo(): Map<String, String> = withContext(Dispatchers.IO) {
        val kernelInfo = mutableMapOf<String, String>()
        
        try {
            // 内核版本
            val kernelVersion = System.getProperty("os.version") ?: "未知"
            kernelInfo["内核版本"] = kernelVersion
            
            // CPU架构
            val architecture = Build.SUPPORTED_ABIS.firstOrNull() ?: "未知"
            kernelInfo["CPU架构"] = architecture
            
            // 内核编译时间
            try {
                val process = Runtime.getRuntime().exec("uname -v")
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val buildInfo = reader.readLine() ?: "未知"
                reader.close()
                kernelInfo["编译信息"] = buildInfo.take(50) + if (buildInfo.length > 50) "..." else ""
            } catch (e: Exception) {
                kernelInfo["编译信息"] = "无法获取"
            }
            
        } catch (e: Exception) {
            kernelInfo["错误"] = "获取内核信息失败: ${e.message}"
        }
        
        kernelInfo
    }


    /**
     * 获取SELinux状态信息
     */
    suspend fun getSELinuxInfo(): Map<String, String> = withContext(Dispatchers.IO) {
        val selinuxInfo = mutableMapOf<String, String>()
        
        // 首先检查Root权限
        val hasRootPermission = checkRootPermission()
        
        if (!hasRootPermission) {
            // 无Root权限时的友好提示
            selinuxInfo["SELinux状态"] = "需要Root权限获取 🔒"
            selinuxInfo["权限提示"] = "获取Root权限可查看完整状态"
            selinuxInfo["安全说明"] = "SELinux信息需要系统级权限访问"
            return@withContext selinuxInfo
        }
        
        try {
            // 有Root权限时获取完整SELinux信息
            val enforceStatus = try {
                // 优先尝试使用su命令读取
                val suProcess = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat /sys/fs/selinux/enforce"))
                val suResult = suProcess.inputStream.bufferedReader().readText().trim()
                suProcess.waitFor()
                
                if (suResult.isNotEmpty()) {
                    when (suResult) {
                        "1" -> "Enforcing ✅"
                        "0" -> "Permissive ⚠️"
                        else -> "未知状态"
                    }
                } else {
                    // 备用方案：尝试getenforce命令
                    val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "getenforce"))
                    val result = process.inputStream.bufferedReader().readText().trim()
                    process.waitFor()
                    when (result.lowercase()) {
                        "enforcing" -> "Enforcing ✅"
                        "permissive" -> "Permissive ⚠️"
                        "disabled" -> "Disabled ❌"
                        else -> result.ifEmpty { "未知状态" }
                    }
                }
            } catch (e: Exception) {
                "获取失败"
            }
            
            selinuxInfo["SELinux状态"] = enforceStatus
            
            // 获取SELinux策略版本
            val policyVersion = try {
                val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat /sys/fs/selinux/policyvers"))
                val result = process.inputStream.bufferedReader().readText().trim()
                process.waitFor()
                result.ifEmpty { "未知" }
            } catch (e: Exception) {
                "未知"
            }
            
            selinuxInfo["策略版本"] = policyVersion
            
            // 根据状态设置安全级别描述
            val securityLevel = when {
                enforceStatus.contains("Enforcing") -> "高安全级别 🛡️"
                enforceStatus.contains("Permissive") -> "宽松模式 ⚠️"
                enforceStatus.contains("Disabled") -> "已禁用 ❌"
                else -> "未知状态"
            }
            
            selinuxInfo["安全级别"] = securityLevel
            
        } catch (e: Exception) {
            selinuxInfo["SELinux状态"] = "获取失败 ❌"
            selinuxInfo["错误信息"] = "Root权限可能不稳定"
            selinuxInfo["建议"] = "请确保设备已正确获取Root权限"
        }
        
        selinuxInfo
    }
    
    /**
     * 检查Root权限
     */
    private fun checkRootPermission(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val outputStream = process.outputStream
            outputStream.write("exit\n".toByteArray())
            outputStream.flush()
            outputStream.close()
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 获取SELinux状态
     */
    private fun getSELinuxStatus(): String {
        return try {
            val process = Runtime.getRuntime().exec("getenforce")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val status = reader.readLine()?.trim() ?: "未知"
            reader.close()
            status
        } catch (e: Exception) {
            try {
                // 备用方法：读取SELinux状态文件
                File("/sys/fs/selinux/enforce").readText().trim().let { enforce ->
                    when (enforce) {
                        "1" -> "Enforcing"
                        "0" -> "Permissive"
                        else -> "未知"
                    }
                }
            } catch (e2: Exception) {
                "无法获取"
            }
        }
    }

    /**
     * 获取内存信息
     */
    private fun getMemoryInfo(): String {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            val memInfo = android.app.ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memInfo)
            
            val totalMemory = memInfo.totalMem / (1024 * 1024 * 1024) // GB
            val availableMemory = memInfo.availMem / (1024 * 1024 * 1024) // GB
            val usedMemory = totalMemory - availableMemory
            
            "${usedMemory}GB / ${totalMemory}GB"
        } catch (e: Exception) {
            "无法获取"
        }
    }

    /**
     * 获取存储信息
     */
    private fun getStorageInfo(): String {
        return try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val totalBytes = stat.totalBytes
            val availableBytes = stat.availableBytes
            val usedBytes = totalBytes - availableBytes
            
            val totalGB = totalBytes / (1024 * 1024 * 1024)
            val usedGB = usedBytes / (1024 * 1024 * 1024)
            
            "${usedGB}GB / ${totalGB}GB"
        } catch (e: Exception) {
            "无法获取"
        }
    }

    /**
     * 获取设备信息
     */
    suspend fun getDeviceInfo(): Map<String, String> = withContext(Dispatchers.IO) {
        val deviceInfo = mutableMapOf<String, String>()
        
        try {
            // 设备型号
            deviceInfo["设备型号"] = "${Build.MANUFACTURER} ${Build.MODEL}"
            
            // 系统版本
            deviceInfo["系统版本"] = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
            
            // 内存信息
            deviceInfo["内存信息"] = getMemoryInfo()
            
            // 存储空间
            deviceInfo["存储空间"] = getStorageInfo()
            
            // 处理器信息
            deviceInfo["处理器"] = Build.HARDWARE
            
            // 安全补丁级别
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                deviceInfo["安全补丁"] = Build.VERSION.SECURITY_PATCH
            }
            
        } catch (e: Exception) {
            deviceInfo["错误"] = "获取设备信息失败: ${e.message}"
        }
        
        deviceInfo
    }

    /**
     * 获取设备指纹信息
     */
    suspend fun getFingerprintInfo(): Map<String, String> = withContext(Dispatchers.IO) {
        val fingerprintInfo = mutableMapOf<String, String>()
        
        try {
            // 设备指纹
            fingerprintInfo["设备指纹"] = Build.FINGERPRINT
            
            // 构建ID
            fingerprintInfo["构建ID"] = Build.ID
            
            // 构建时间
            val buildTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Date(Build.TIME))
            fingerprintInfo["构建时间"] = buildTime
            
            // 安全补丁级别
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                fingerprintInfo["安全补丁"] = Build.VERSION.SECURITY_PATCH
            }
            
        } catch (e: Exception) {
            fingerprintInfo["错误"] = "获取指纹信息失败: ${e.message}"
        }
        
        fingerprintInfo
    }
}
