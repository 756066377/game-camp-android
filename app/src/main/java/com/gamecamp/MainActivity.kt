package com.gamecamp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.gamecamp.ui.GameCampApp
import com.gamecamp.ui.theme.GameCampTheme
import com.gamecamp.ui.theme.WarmNeumorphismColors
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val _permissionsGranted = MutableStateFlow(false)
    val permissionsGranted = _permissionsGranted.asStateFlow()

    // 权限请求发射器
    private val requestPermissionsLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            // 更新整体权限状态
            _permissionsGranted.value = permissions.values.all { it }

            if (!_permissionsGranted.value) {
                Toast.makeText(this, "存储权限被拒绝，驱动管理功能可能受限", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "权限已授予", Toast.LENGTH_SHORT).show()
            }
        }

    // 驱动管理应用必要权限列表
    private val requiredPermissions: Array<String> by lazy {
        val list = mutableListOf<String>()
        
        // 存储权限（仅在 Android 10 及以下需要）
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            list.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        list.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        
        list.toTypedArray()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 启用边到边显示（沉浸式状态栏的前提）
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            GameCampTheme {
                // 不使用statusBarsPadding，让内容延伸到状态栏
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = WarmNeumorphismColors.CreamWhite
                ) {
                    GameCampApp()
                }
            }
        }

        // 在UI加载后，检查并请求权限
        if (!checkAndSetPermissions()) {
            requestPermissionsLauncher.launch(requiredPermissions)
        }
    }

    private fun checkAndSetPermissions(): Boolean {
        val allGranted = requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        _permissionsGranted.value = allGranted
        return allGranted
    }
}