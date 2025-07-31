package com.gamecamp.ui.state

/**
 * 驱动页面的 UI 状态管理
 * 使用 sealed class 提供更好的状态管理
 */
sealed class DriverUiState {
    
    /**
     * 初始状态
     */
    data class Initial(
        val isDriverInstalled: Boolean = false,
        val selectedDriver: String = "RTPRO驱动"
    ) : DriverUiState()
    
    /**
     * 驱动安装中状态
     */
    data class Installing(
        val selectedDriver: String,
        val isDriverInstalled: Boolean = false
    ) : DriverUiState()
    
    /**
     * 驱动安装成功状态
     */
    data class InstallSuccess(
        val installedDriver: String,
        val isDriverInstalled: Boolean = true
    ) : DriverUiState()
    
    /**
     * 驱动安装失败状态
     */
    data class InstallFailure(
        val errorMessage: String,
        val selectedDriver: String,
        val isDriverInstalled: Boolean = false
    ) : DriverUiState()
    
    /**
     * 驱动重置中状态
     */
    data class Resetting(
        val installedDriver: String,
        val isDriverInstalled: Boolean = true
    ) : DriverUiState()
    
    /**
     * 驱动重置成功状态
     */
    data class ResetSuccess(
        val selectedDriver: String = "RTPRO驱动",
        val isDriverInstalled: Boolean = false
    ) : DriverUiState()
    
    /**
     * 驱动重置失败状态
     */
    data class ResetFailure(
        val errorMessage: String,
        val installedDriver: String,
        val isDriverInstalled: Boolean = true
    ) : DriverUiState()
    
    /**
     * 显示确认对话框状态
     */
    data class ShowConfirmDialog(
        val installedDriver: String,
        val isDriverInstalled: Boolean = true
    ) : DriverUiState()
}

/**
 * UI 状态的扩展属性，用于简化状态判断
 */
val DriverUiState.isLoading: Boolean
    get() = this is DriverUiState.Installing || this is DriverUiState.Resetting

val DriverUiState.isDriverInstalled: Boolean
    get() = when (this) {
        is DriverUiState.Initial -> isDriverInstalled
        is DriverUiState.Installing -> isDriverInstalled
        is DriverUiState.InstallSuccess -> isDriverInstalled
        is DriverUiState.InstallFailure -> isDriverInstalled
        is DriverUiState.Resetting -> isDriverInstalled
        is DriverUiState.ResetSuccess -> isDriverInstalled
        is DriverUiState.ResetFailure -> isDriverInstalled
        is DriverUiState.ShowConfirmDialog -> isDriverInstalled
    }

val DriverUiState.selectedDriver: String
    get() = when (this) {
        is DriverUiState.Initial -> selectedDriver
        is DriverUiState.Installing -> selectedDriver
        is DriverUiState.InstallSuccess -> installedDriver
        is DriverUiState.InstallFailure -> selectedDriver
        is DriverUiState.Resetting -> installedDriver
        is DriverUiState.ResetSuccess -> selectedDriver
        is DriverUiState.ResetFailure -> installedDriver
        is DriverUiState.ShowConfirmDialog -> installedDriver
    }

val DriverUiState.showConfirmDialog: Boolean
    get() = this is DriverUiState.ShowConfirmDialog

val DriverUiState.errorMessage: String?
    get() = when (this) {
        is DriverUiState.InstallFailure -> errorMessage
        is DriverUiState.ResetFailure -> errorMessage
        else -> null
    }