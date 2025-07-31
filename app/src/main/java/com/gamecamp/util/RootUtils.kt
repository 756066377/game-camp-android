package com.gamecamp.util

import com.topjohnwu.superuser.Shell

/**
 * Root权限检测工具
 * 提供检测设备是否已获取Root权限的方法
 */
object RootUtils {
    /**
     * 检测设备是否具有Root权限
     * @return true 如果设备已Root
     */
    fun isRootAvailable(): Boolean {
        // libsu 推荐在工作线程中检查 root 权限
        // 这里为了简化，直接调用，但在实际应用中应考虑异步处理
        return Shell.getShell().isRoot
    }
}
