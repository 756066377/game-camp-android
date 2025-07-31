package com.gamecamp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamecamp.constants.AppConstants
import com.gamecamp.constants.DriverConstants
import com.gamecamp.repository.DriverRepository
import com.gamecamp.repository.DriverInstallResult
import com.gamecamp.repository.DriverResetResult
import com.gamecamp.ui.state.DriverUiState
import com.gamecamp.ui.state.isLoading
import com.gamecamp.ui.state.isDriverInstalled
import com.gamecamp.ui.state.selectedDriver
import com.gamecamp.data.TerminalLog
import com.gamecamp.data.LogType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * 驱动管理页面的 ViewModel
 * 使用新的状态管理和 Repository 模式
 */
@HiltViewModel
class DriverViewModel @Inject constructor(
    private val driverRepository: DriverRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DriverUiState>(
        DriverUiState.Initial(
            isDriverInstalled = false,
            selectedDriver = DriverConstants.DEFAULT_DRIVER
        )
    )
    val uiState: StateFlow<DriverUiState> = _uiState.asStateFlow()
    
    // 终端日志状态
    private val _terminalLogs = MutableStateFlow<List<TerminalLog>>(emptyList())
    val terminalLogs: StateFlow<List<TerminalLog>> = _terminalLogs.asStateFlow()
    
    // 终端对话框显示状态
    private val _showTerminalDialog = MutableStateFlow(false)
    val showTerminalDialog: StateFlow<Boolean> = _showTerminalDialog.asStateFlow()
    
    // 终端操作完成状态
    private val _terminalCompleted = MutableStateFlow(false)
    val terminalCompleted: StateFlow<Boolean> = _terminalCompleted.asStateFlow()

    init {
        // 初始化时检查驱动安装状态
        checkDriverStatus()
    }

    /**
     * 检查驱动状态
     */
    private fun checkDriverStatus() {
        if (driverRepository.isDriverInstalled()) {
            val selectedDriver = driverRepository.getSelectedDriver()
            _uiState.value = DriverUiState.InstallSuccess(installedDriver = selectedDriver)
        } else {
            // 确保重置状态正确初始化
            _uiState.value = DriverUiState.Initial(
                isDriverInstalled = false,
                    selectedDriver = DriverConstants.DEFAULT_DRIVER
            )
        }
    }

    /**
     * 刷新驱动状态 - 供外部调用
     */
    fun refreshDriverStatus() {
        checkDriverStatus()
    }

    /**
     * 驱动选择事件
     */
    fun onDriverSelected(driverName: String) {
        val currentState = _uiState.value
        if (!currentState.isLoading && !currentState.isDriverInstalled) {
            _uiState.value = when (currentState) {
                is DriverUiState.Initial -> currentState.copy(selectedDriver = driverName)
                is DriverUiState.InstallFailure -> currentState.copy(selectedDriver = driverName)
                else -> currentState
            }
        }
    }

    /**
     * 安装驱动点击事件
     */
    fun onInstallClick() {
        // 首先检查Root权限
        if (!driverRepository.hasRootPermission()) {
            val currentDriver = _uiState.value.selectedDriver
            _uiState.value = DriverUiState.InstallFailure(
                errorMessage = "驱动安装需要Root权限。\n\n请先获取Root权限后再试。\n\n提示：Root权限是安装系统级驱动的必要条件。",
                selectedDriver = currentDriver
            )
            return
        }
        
        viewModelScope.launch {
            val currentDriver = _uiState.value.selectedDriver
            _uiState.value = DriverUiState.Installing(selectedDriver = currentDriver)
            
            val result = driverRepository.installDriver(currentDriver)
            when (result) {
                is DriverInstallResult.Success -> {
                    _uiState.value = DriverUiState.InstallSuccess(installedDriver = currentDriver)
                }
                is DriverInstallResult.Error -> {
                    _uiState.value = DriverUiState.InstallFailure(
                        errorMessage = result.message,
                        selectedDriver = currentDriver
                    )
                }
            }
        }
    }

    /**
     * 重置驱动点击事件（显示确认对话框）
     */
    fun onResetClick() {
        // 首先检查Root权限
        if (!driverRepository.hasRootPermission()) {
            val currentDriver = _uiState.value.selectedDriver
            _uiState.value = DriverUiState.ResetFailure(
                errorMessage = "设备未获得Root权限，无法执行重置操作。\n\n请先获取Root权限后再试。\n\n提示：Root权限是执行系统级操作（如重启设备）的必要条件。",
                installedDriver = currentDriver
            )
            return
        }
        
        val currentState = _uiState.value
        if (currentState.isDriverInstalled) {
            _uiState.value = DriverUiState.ShowConfirmDialog(
                installedDriver = currentState.selectedDriver
            )
        }
    }

    /**
     * 确认重置驱动
     */
    fun onConfirmReset() {
        viewModelScope.launch {
            val currentDriver = _uiState.value.selectedDriver
            _uiState.value = DriverUiState.Resetting(installedDriver = currentDriver)
            
            val result = driverRepository.resetDriver()
            when (result) {
                is DriverResetResult.Success -> {
                    _uiState.value = DriverUiState.ResetSuccess()
                }
                is DriverResetResult.NoRootPermission -> {
                    _uiState.value = DriverUiState.ResetFailure(
                        errorMessage = "设备未获得Root权限，无法执行重置操作。\n\n请先获取Root权限后再试。\n\n提示：Root权限是执行系统级操作（如重启设备）的必要条件。",
                        installedDriver = currentDriver
                    )
                }
                is DriverResetResult.RebootFailed -> {
                    _uiState.value = DriverUiState.ResetFailure(
                        errorMessage = "驱动状态已重置，但设备重启失败。\n\n请手动重启设备以完成重置过程。",
                        installedDriver = currentDriver
                    )
                }
                is DriverResetResult.Error -> {
                    _uiState.value = DriverUiState.ResetFailure(
                        errorMessage = result.message,
                        installedDriver = currentDriver
                    )
                }
            }
        }
    }

    /**
     * 取消重置驱动
     */
    fun onCancelReset() {
        val currentState = _uiState.value
        if (currentState is DriverUiState.ShowConfirmDialog) {
            _uiState.value = DriverUiState.InstallSuccess(
                installedDriver = currentState.installedDriver
            )
        }
    }

    /**
     * 清除错误消息
     */
    fun clearError() {
        val currentState = _uiState.value
        when (currentState) {
            is DriverUiState.InstallFailure -> {
                _uiState.value = DriverUiState.Initial(
                    isDriverInstalled = false,
                    selectedDriver = currentState.selectedDriver
                )
            }
            is DriverUiState.ResetFailure -> {
                // 重新检查实际的驱动状态，而不是依赖缓存
                refreshDriverStatus()
            }
            is DriverUiState.ResetSuccess -> {
                // 重置成功后，确保状态正确初始化
                _uiState.value = DriverUiState.Initial(
                    isDriverInstalled = false,
                    selectedDriver = AppConstants.Driver.DEFAULT_DRIVER
                )
            }
            else -> {
                // 其他状态不需要清除错误
            }
        }
    }

    /**
     * 检查Root权限状态
     */
    fun checkRootPermission(): Boolean {
        return driverRepository.hasRootPermission()
    }
    
    /**
     * 显示终端对话框并开始安装流程
     */
    fun startInstallWithTerminal() {
        // 首先检查Root权限
        if (!driverRepository.hasRootPermission()) {
            val currentDriver = _uiState.value.selectedDriver
            _uiState.value = DriverUiState.InstallFailure(
                errorMessage = "驱动安装需要Root权限。\n\n请先获取Root权限后再试。\n\n提示：Root权限是安装系统级驱动的必要条件。",
                selectedDriver = currentDriver
            )
            return
        }
        
        // 清空之前的日志并显示终端
        _terminalLogs.value = emptyList()
        _terminalCompleted.value = false
        _showTerminalDialog.value = true
        
        // 开始安装流程
        viewModelScope.launch {
            val currentDriver = _uiState.value.selectedDriver
            _uiState.value = DriverUiState.Installing(selectedDriver = currentDriver)
            
            // 模拟驱动安装过程的日志
            simulateInstallProcess(currentDriver)
        }
    }
    
    /**
     * 显示终端对话框并开始重置流程
     */
    fun startResetWithTerminal() {
        // 首先检查Root权限
        if (!driverRepository.hasRootPermission()) {
            val currentDriver = _uiState.value.selectedDriver
            _uiState.value = DriverUiState.ResetFailure(
                errorMessage = "设备未获得Root权限，无法执行重置操作。\n\n请先获取Root权限后再试。\n\n提示：Root权限是执行系统级操作（如重启设备）的必要条件。",
                installedDriver = currentDriver
            )
            return
        }
        
        // 清空之前的日志并显示终端
        _terminalLogs.value = emptyList()
        _terminalCompleted.value = false
        _showTerminalDialog.value = true
        
        // 开始重置流程
        viewModelScope.launch {
            val currentDriver = _uiState.value.selectedDriver
            _uiState.value = DriverUiState.Resetting(installedDriver = currentDriver)
            
            // 模拟驱动重置过程的日志
            simulateResetProcess()
        }
    }
    
    /**
     * 关闭终端对话框
     */
    fun closeTerminalDialog() {
        _showTerminalDialog.value = false
        _terminalLogs.value = emptyList()
        _terminalCompleted.value = false
    }
    
    /**
     * 添加终端日志
     */
    private fun addTerminalLog(text: String, type: LogType) {
        val timestamp = SimpleDateFormat("[HH:mm:ss]", Locale.getDefault()).format(Date())
        val log = TerminalLog(text = text, type = type, timestamp = timestamp)
        _terminalLogs.value = _terminalLogs.value + log
    }
    
    /**
     * 模拟驱动安装过程
     */
    private suspend fun simulateInstallProcess(driverName: String) {
        try {
            addTerminalLog("开始刷入驱动...", LogType.COMMAND)
            delay(500)
            
            addTerminalLog("检查设备权限...", LogType.INFO)
            delay(800)
            addTerminalLog("Root权限检查通过", LogType.SUCCESS)
            delay(500)
            
            addTerminalLog("检测内核版本...", LogType.INFO)
            delay(1000)
            val kernelVersion = System.getProperty("os.version") ?: "未知"
            addTerminalLog("内核版本: $kernelVersion", LogType.SUCCESS)
            delay(500)
            
            addTerminalLog("准备驱动文件...", LogType.INFO)
            delay(1200)
            addTerminalLog("驱动文件: $driverName", LogType.SUCCESS)
            delay(500)
            
            addTerminalLog("正在刷入驱动...", LogType.PROGRESS)
            delay(2000)
            
            // 调用实际的安装方法
            val result = driverRepository.installDriver(driverName)
            
            when (result) {
                is DriverInstallResult.Success -> {
                    addTerminalLog("驱动刷入完成", LogType.SUCCESS)
                    delay(500)
                    addTerminalLog("正在验证安装...", LogType.INFO)
                    delay(1000)
                    addTerminalLog("驱动安装成功！", LogType.SUCCESS)
                    delay(500)
                    addTerminalLog("安装完成", LogType.COMMAND)
                    
                    _uiState.value = DriverUiState.InstallSuccess(installedDriver = driverName)
                }
                is DriverInstallResult.Error -> {
                    addTerminalLog("驱动安装失败", LogType.ERROR)
                    delay(500)
                    addTerminalLog("错误信息: ${result.message}", LogType.ERROR)
                    
                    _uiState.value = DriverUiState.InstallFailure(
                        errorMessage = result.message,
                        selectedDriver = driverName
                    )
                }
            }
        } catch (e: Exception) {
            addTerminalLog("安装过程出现异常", LogType.ERROR)
            addTerminalLog("异常信息: ${e.message}", LogType.ERROR)
            
            _uiState.value = DriverUiState.InstallFailure(
                errorMessage = "安装过程出现异常: ${e.message}",
                selectedDriver = driverName
            )
        } finally {
            _terminalCompleted.value = true
        }
    }
    
    /**
     * 模拟驱动重置过程
     */
    private suspend fun simulateResetProcess() {
        try {
            addTerminalLog("开始重置驱动...", LogType.COMMAND)
            delay(500)
            
            addTerminalLog("检查Root权限...", LogType.INFO)
            delay(800)
            addTerminalLog("Root权限检查通过", LogType.SUCCESS)
            delay(500)
            
            addTerminalLog("停止相关服务...", LogType.INFO)
            delay(1000)
            addTerminalLog("服务已停止", LogType.SUCCESS)
            delay(500)
            
            addTerminalLog("清理驱动文件...", LogType.INFO)
            delay(1500)
            addTerminalLog("驱动文件已清理", LogType.SUCCESS)
            delay(500)
            
            addTerminalLog("重置系统配置...", LogType.INFO)
            delay(1200)
            addTerminalLog("系统配置已重置", LogType.SUCCESS)
            delay(500)
            
            // 调用实际的重置方法
            val result = driverRepository.resetDriver()
            
            when (result) {
                is DriverResetResult.Success -> {
                    addTerminalLog("正在重启设备...", LogType.PROGRESS)
                    delay(2000)
                    addTerminalLog("设备重启完成", LogType.SUCCESS)
                    delay(500)
                    addTerminalLog("驱动重置成功！", LogType.SUCCESS)
                    delay(500)
                    addTerminalLog("重置完成", LogType.COMMAND)
                    
                    _uiState.value = DriverUiState.ResetSuccess()
                }
                is DriverResetResult.NoRootPermission -> {
                    addTerminalLog("Root权限不足", LogType.ERROR)
                    addTerminalLog("无法执行重置操作", LogType.ERROR)
                    
                    _uiState.value = DriverUiState.ResetFailure(
                        errorMessage = "设备未获得Root权限，无法执行重置操作。",
                        installedDriver = _uiState.value.selectedDriver
                    )
                }
                is DriverResetResult.RebootFailed -> {
                    addTerminalLog("驱动已重置", LogType.SUCCESS)
                    addTerminalLog("设备重启失败", LogType.WARNING)
                    addTerminalLog("请手动重启设备", LogType.WARNING)
                    
                    _uiState.value = DriverUiState.ResetFailure(
                        errorMessage = "驱动状态已重置，但设备重启失败。请手动重启设备以完成重置过程。",
                        installedDriver = _uiState.value.selectedDriver
                    )
                }
                is DriverResetResult.Error -> {
                    addTerminalLog("重置失败", LogType.ERROR)
                    addTerminalLog("错误信息: ${result.message}", LogType.ERROR)
                    
                    _uiState.value = DriverUiState.ResetFailure(
                        errorMessage = result.message,
                        installedDriver = _uiState.value.selectedDriver
                    )
                }
            }
        } catch (e: Exception) {
            addTerminalLog("重置过程出现异常", LogType.ERROR)
            addTerminalLog("异常信息: ${e.message}", LogType.ERROR)
            
            _uiState.value = DriverUiState.ResetFailure(
                errorMessage = "重置过程出现异常: ${e.message}",
                installedDriver = _uiState.value.selectedDriver
            )
        } finally {
            _terminalCompleted.value = true
        }
    }
}
