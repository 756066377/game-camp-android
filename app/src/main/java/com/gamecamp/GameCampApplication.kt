package com.gamecamp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * 游戏营地应用程序类
 * 使用Hilt进行依赖注入管理
 */
@HiltAndroidApp
class GameCampApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // 应用程序初始化逻辑
    }
}