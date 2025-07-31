package com.gamecamp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamecamp.util.SystemInfoManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 数据看板ViewModel
 * 管理系统信息的获取和状态
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val systemInfoManager: SystemInfoManager
) : ViewModel() {

    // 内核信息状态
    private val _kernelInfo = MutableStateFlow<Map<String, String>>(emptyMap())
    val kernelInfo: StateFlow<Map<String, String>> = _kernelInfo.asStateFlow()

    // SELinux信息状态
    private val _selinuxInfo = MutableStateFlow<Map<String, String>>(emptyMap())
    val selinuxInfo: StateFlow<Map<String, String>> = _selinuxInfo.asStateFlow()

    // 设备信息状态
    private val _deviceInfo = MutableStateFlow<Map<String, String>>(emptyMap())
    val deviceInfo: StateFlow<Map<String, String>> = _deviceInfo.asStateFlow()

    // 设备指纹信息状态
    private val _fingerprintInfo = MutableStateFlow<Map<String, String>>(emptyMap())
    val fingerprintInfo: StateFlow<Map<String, String>> = _fingerprintInfo.asStateFlow()

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 错误信息状态
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // 初始化时加载系统信息
        loadSystemInfo()
    }

    /**
     * 加载系统信息
     */
    private fun loadSystemInfo() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                // 并发获取所有系统信息
                val kernelInfoDeferred = viewModelScope.async { systemInfoManager.getKernelInfo() }
                val selinuxInfoDeferred = viewModelScope.async { systemInfoManager.getSELinuxInfo() }
                val deviceInfoDeferred = viewModelScope.async { systemInfoManager.getDeviceInfo() }
                val fingerprintInfoDeferred = viewModelScope.async { systemInfoManager.getFingerprintInfo() }

                // 等待所有信息获取完成
                _kernelInfo.value = kernelInfoDeferred.await()
                _selinuxInfo.value = selinuxInfoDeferred.await()
                _deviceInfo.value = deviceInfoDeferred.await()
                _fingerprintInfo.value = fingerprintInfoDeferred.await()

            } catch (e: Exception) {
                _errorMessage.value = "加载系统信息失败: ${e.message}"
                
                // 设置默认值
                _kernelInfo.value = mapOf("错误" to "无法获取内核信息")
                _selinuxInfo.value = mapOf("错误" to "无法获取SELinux信息")
                _deviceInfo.value = mapOf("错误" to "无法获取设备信息")
                _fingerprintInfo.value = mapOf("错误" to "无法获取指纹信息")
                
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 刷新系统信息
     */
    fun refreshSystemInfo() {
        loadSystemInfo()
    }

    /**
     * 清除错误信息
     */
    fun clearError() {
        _errorMessage.value = null
    }
}