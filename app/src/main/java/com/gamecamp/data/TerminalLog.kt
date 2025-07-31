package com.gamecamp.data

/**
 * 终端日志数据类
 * 用于表示终端模拟器中的单条日志记录
 */
data class TerminalLog(
    val text: String,           // 日志文本内容
    val type: LogType,          // 日志类型
    val timestamp: String,      // 时间戳
    val isCommand: Boolean = false  // 是否为命令行
)

/**
 * 日志类型枚举
 * 用于区分不同类型的日志消息
 */
enum class LogType {
    SUCCESS,    // 成功 - 绿色
    ERROR,      // 错误 - 红色  
    WARNING,    // 警告 - 黄色
    INFO,       // 信息 - 白色
    COMMAND,    // 命令 - 青色
    PROGRESS    // 进度 - 蓝色
}