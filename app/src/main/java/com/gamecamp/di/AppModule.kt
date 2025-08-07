package com.gamecamp.di

import android.content.Context
import com.gamecamp.data.DriverStatusManager
import com.gamecamp.util.DriverChecker
import com.gamecamp.util.RootChecker
import com.gamecamp.util.SystemInfoManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 应用程序依赖注入模块
 * 提供全局单例依赖
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * 提供系统信息管理器
     */
    @Provides
    @Singleton
    fun provideSystemInfoManager(
        @ApplicationContext context: Context
    ): SystemInfoManager {
        return SystemInfoManager(context)
    }

    /**
     * 提供驱动检查器
     */
    @Provides
    @Singleton
    fun provideDriverChecker(): DriverChecker {
        return DriverChecker()
    }

    /**
     * 提供Root检查器
     */
    @Provides
    @Singleton
    fun provideRootChecker(): RootChecker {
        return RootChecker()
    }

    /**
     * 提供驱动状态管理器
     */
    @Provides
    @Singleton
    fun provideDriverStatusManager(@ApplicationContext context: Context): DriverStatusManager {
        return DriverStatusManager(context)
    }
}
