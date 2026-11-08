package com.orbit.app

import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.orbit.app.core.data.repository.RoomChatRepositoryImpl
import com.orbit.app.core.designsystem.theme.OrbitTheme
import com.orbit.app.core.domain.model.ReducedMotionOverride
import com.orbit.app.core.domain.model.ThemeMode
import com.orbit.app.core.domain.repository.AppSettingsRepository
import com.orbit.app.navigation.OrbitNavHost
import com.orbit.app.splash.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val splashViewModel: SplashViewModel by viewModels()

    @Inject
    lateinit var appSettingsRepository: AppSettingsRepository

    @Inject
    lateinit var roomChatRepository: RoomChatRepositoryImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            roomChatRepository.recoverInterruptedStreams()
        }

        splashScreen.setKeepOnScreenCondition {
            !splashViewModel.isReady.value
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
                val iconView = splashScreenViewProvider.iconView
                val scaleX = ObjectAnimator.ofFloat(iconView, View.SCALE_X, 1.0f, 1.2f)
                val scaleY = ObjectAnimator.ofFloat(iconView, View.SCALE_Y, 1.0f, 1.2f)
                val alpha = ObjectAnimator.ofFloat(splashScreenViewProvider.view, View.ALPHA, 1.0f, 0.0f)

                scaleX.duration = 300L
                scaleY.duration = 300L
                alpha.duration = 300L

                alpha.interpolator = AnticipateInterpolator()
                alpha.doOnEnd {
                    splashScreenViewProvider.remove()
                }

                scaleX.start()
                scaleY.start()
                alpha.start()
            }
        }

        enableEdgeToEdge()
        setContent {
            val settings by appSettingsRepository.appSettings.collectAsStateWithLifecycle(initialValue = com.orbit.app.core.domain.model.AppSettings())
            val systemDark = isSystemInDarkTheme()

            val useDarkTheme = when (settings.themeMode) {
                ThemeMode.System -> systemDark
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
            }

            val forceReducedMotion = when (settings.reducedMotionOverride) {
                ReducedMotionOverride.System -> null
                ReducedMotionOverride.On -> true
                ReducedMotionOverride.Off -> false
            }

            OrbitTheme(
                darkTheme = useDarkTheme,
                dynamicColor = settings.dynamicColorEnabled,
                reducedMotion = forceReducedMotion ?: false
            ) {
                OrbitNavHost()
            }
        }
    }
}
