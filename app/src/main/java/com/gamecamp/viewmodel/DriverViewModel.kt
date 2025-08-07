package com.gamecamp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamecamp.data.LogType
import com.gamecamp.data.TerminalLog
import com.gamecamp.repository.DriverRepository
import com.gamecamp.ui.components.AssistantSettings
import com.gamecamp.ui.state.DriverUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DriverViewModel @Inject constructor(
    private val driverRepository: DriverRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DriverUiState>(DriverUiState.Initial())
    val uiState: StateFlow<DriverUiState> = _uiState

    private val _terminalLogs = MutableStateFlow<List<TerminalLog>>(emptyList())
    val terminalLogs: StateFlow<List<TerminalLog>> = _terminalLogs

    private val _showTerminalDialog = MutableStateFlow(false)
    val showTerminalDialog: StateFlow<Boolean> = _showTerminalDialog

    private val _terminalCompleted = MutableStateFlow(false)
    val terminalCompleted: StateFlow<Boolean> = _terminalCompleted

    private val _assistantSettings = MutableStateFlow(AssistantSettings())
    val assistantSettings: StateFlow<AssistantSettings> = _assistantSettings

    init {
        refreshDriverStatus()
        loadAssistantSettings()
    }

    fun refreshDriverStatus() {
        viewModelScope.launch {
            val isInstalled = driverRepository.isDriverInstalled()
            val selectedDriver = driverRepository.getSelectedDriver()
            _uiState.value = if (isInstalled) {
                DriverUiState.InstallSuccess(selectedDriver)
            } else {
                DriverUiState.Initial(selectedDriver = selectedDriver)
            }
        }
    }

    fun onDriverSelected(driverName: String) {
        driverRepository.setSelectedDriver(driverName)
        val currentState = _uiState.value
        if (currentState is DriverUiState.Initial) {
            _uiState.value = currentState.copy(selectedDriver = driverName)
        }
    }

    fun onInstallClick() {
        viewModelScope.launch {
            val selectedDriver = driverRepository.getSelectedDriver()
            _uiState.value = DriverUiState.Installing(selectedDriver)
            val result = driverRepository.installDriver(selectedDriver) { log, type ->
                addLog(log, type)
            }
            _uiState.value = when (result) {
                is com.gamecamp.repository.DriverInstallResult.Success -> DriverUiState.InstallSuccess(selectedDriver)
                is com.gamecamp.repository.DriverInstallResult.Error -> DriverUiState.InstallFailure(result.message, selectedDriver)
            }
            _terminalCompleted.value = true
        }
    }

    fun onResetClick() {
        val currentState = _uiState.value
        if (currentState is DriverUiState.InstallSuccess) {
            _uiState.value = DriverUiState.ShowConfirmDialog(currentState.installedDriver)
        }
    }

    fun onConfirmReset() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is DriverUiState.ShowConfirmDialog) {
                _uiState.value = DriverUiState.Resetting(currentState.installedDriver)
                val result = driverRepository.resetDriver { log, type ->
                    addLog(log, type)
                }
                _uiState.value = when (result) {
                    is com.gamecamp.repository.DriverResetResult.Success -> DriverUiState.ResetSuccess()
                    is com.gamecamp.repository.DriverResetResult.Error -> DriverUiState.ResetFailure(result.message, currentState.installedDriver)
                    is com.gamecamp.repository.DriverResetResult.NoRootPermission -> DriverUiState.ResetFailure("无Root权限", currentState.installedDriver)
                    is com.gamecamp.repository.DriverResetResult.RebootFailed -> DriverUiState.ResetFailure("重启设备失败", currentState.installedDriver)
                }
                _terminalCompleted.value = true
            }
        }
    }

    fun onCancelReset() {
        refreshDriverStatus()
    }

    fun clearError() {
        val currentState = _uiState.value
        if (currentState is DriverUiState.InstallFailure) {
            _uiState.value = DriverUiState.Initial(selectedDriver = currentState.selectedDriver)
        } else if (currentState is DriverUiState.ResetFailure) {
            _uiState.value = DriverUiState.InstallSuccess(currentState.installedDriver)
        }
    }

    fun startInstallWithTerminal() {
        _terminalLogs.value = emptyList()
        _terminalCompleted.value = false
        _showTerminalDialog.value = true
        addLog("正在启动终端安装模式...", LogType.INFO)
        onInstallClick()
    }

    fun startResetWithTerminal() {
        viewModelScope.launch {
            _terminalLogs.value = emptyList()
            _terminalCompleted.value = false
            _showTerminalDialog.value = true
            
            val currentState = _uiState.value
            if (currentState is DriverUiState.InstallSuccess || currentState is DriverUiState.ShowConfirmDialog) {
                val driverName = if (currentState is DriverUiState.InstallSuccess) {
                    currentState.installedDriver
                } else if (currentState is DriverUiState.ShowConfirmDialog) {
                    currentState.installedDriver
                } else {
                    driverRepository.getSelectedDriver()
                }
                
                _uiState.value = DriverUiState.Resetting(driverName)
                
                val result = driverRepository.resetDriver { log, type ->
                    addLog(log, type)
                }
                
                _uiState.value = when (result) {
                    is com.gamecamp.repository.DriverResetResult.Success -> DriverUiState.ResetSuccess()
                    is com.gamecamp.repository.DriverResetResult.Error -> DriverUiState.ResetFailure(result.message, driverName)
                    is com.gamecamp.repository.DriverResetResult.NoRootPermission -> DriverUiState.ResetFailure("无Root权限", driverName)
                    is com.gamecamp.repository.DriverResetResult.RebootFailed -> DriverUiState.ResetFailure("重启设备失败", driverName)
                }
                
                _terminalCompleted.value = true
            }
        }
    }

    private fun addLog(text: String, type: LogType) {
        val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val newLog = TerminalLog(text, type, timestamp, type == LogType.COMMAND)
        _terminalLogs.value = _terminalLogs.value + newLog
    }

    fun closeTerminalDialog() {
        _showTerminalDialog.value = false
        refreshDriverStatus()
    }

    fun checkRootPermission(): Boolean {
        return driverRepository.hasRootPermission()
    }

    /**
     * 加载已保存的辅助功能设置
     */
    private fun loadAssistantSettings() {
        viewModelScope.launch {
            _assistantSettings.value = driverRepository.loadAssistantSettings()
        }
    }

    /**
     * 启动辅助功能
     * @param settings 辅助功能设置
     */
    fun launchAssistant(settings: AssistantSettings) {
        viewModelScope.launch {
            // 保存设置以供下次使用
            driverRepository.saveAssistantSettings(settings)
            _assistantSettings.value = settings

            // 在终端对话框中显示日志
            _terminalLogs.value = emptyList()
            _terminalCompleted.value = false
            _showTerminalDialog.value = true
            
            addLog("正在启动红色时代.", LogType.INFO)
            addLog("  - 防录屏: ${if (settings.antiScreenRecording) "开启" else "关闭"}", LogType.INFO)
            addLog("  - 无后台模式: ${if (settings.noBackgroundMode) "开启" else "关闭"}", LogType.INFO)
            addLog("  - 单透模式: ${if (settings.singleTransparentMode) "开启" else "关闭"}", LogType.INFO)

            // TODO: 在 Repository 中实现具体的辅助功能启动逻辑
            // val result = driverRepository.applyAssistantSettings(settings)
            // when(result) { ... }

            addLog("辅助功能已成功启动", LogType.SUCCESS)
            _terminalCompleted.value = true
        }
    }
}