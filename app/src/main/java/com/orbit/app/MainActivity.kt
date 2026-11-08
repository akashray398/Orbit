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
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.orbit.app.core.designsystem.theme.OrbitTheme
import com.orbit.app.navigation.OrbitNavHost
import com.orbit.app.splash.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val splashViewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

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
            OrbitTheme {
                OrbitNavHost()
            }
        }
    }
}
