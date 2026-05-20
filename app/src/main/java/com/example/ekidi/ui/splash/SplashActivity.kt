package com.example.ekidi.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.ekidi.R
import com.example.ekidi.databinding.ActivitySplashBinding
import com.example.ekidi.ui.auth.LoginActivity
import com.example.ekidi.ui.home.HomeActivity
import com.example.ekidi.utils.SessionManager

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var sessionManager: SessionManager

    companion object {
        private const val SPLASH_DURATION = 2800L
        private const val ANIM_DELAY_LOGO = 200L
        private const val ANIM_DELAY_DOTS = 600L
        private const val ANIM_DELAY_VERSION = 800L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        sessionManager = SessionManager(this)
        startAnimations()
        scheduleNavigation()
    }

    private fun startAnimations() {
        val fadeScaleIn = AnimationUtils.loadAnimation(this, R.anim.fade_scale_in)
        Handler(Looper.getMainLooper()).postDelayed({
            binding.containerMain.alpha = 1f
            binding.containerMain.startAnimation(fadeScaleIn)
        }, ANIM_DELAY_LOGO)

        val bounceAnim = AnimationUtils.loadAnimation(this, R.anim.bounce)
        Handler(Looper.getMainLooper()).postDelayed({
            binding.iconLogo.startAnimation(bounceAnim)
        }, ANIM_DELAY_LOGO + 300L)

        Handler(Looper.getMainLooper()).postDelayed({
            binding.loadingDots.animate().alpha(1f).setDuration(400).start()
            animateDots()
        }, ANIM_DELAY_DOTS)

        Handler(Looper.getMainLooper()).postDelayed({
            binding.tvVersion.animate().alpha(1f).setDuration(300).start()
        }, ANIM_DELAY_VERSION)

        animateDecorations()
    }

    private fun animateDots() {
        val handler = Handler(Looper.getMainLooper())
        var step = 0
        val runnable = object : Runnable {
            override fun run() {
                when (step % 3) {
                    0 -> { setDotActive(0); setDotInactive(1); setDotInactive(2) }
                    1 -> { setDotInactive(0); setDotActive(1); setDotInactive(2) }
                    2 -> { setDotInactive(0); setDotInactive(1); setDotActive(2) }
                }
                step++
                handler.postDelayed(this, 400)
            }
        }
        handler.post(runnable)
    }

    private fun setDotActive(index: Int) {
        val dot = when (index) { 0 -> binding.dot1; 1 -> binding.dot2; else -> binding.dot3 }
        dot.setBackgroundResource(R.drawable.dot_active)
        dot.animate().scaleX(1.3f).scaleY(1.3f).setDuration(200).start()
    }

    private fun setDotInactive(index: Int) {
        val dot = when (index) { 0 -> binding.dot1; 1 -> binding.dot2; else -> binding.dot3 }
        dot.setBackgroundResource(R.drawable.dot_inactive)
        dot.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
    }

    private fun animateDecorations() {
        val floatAnim = { view: android.view.View, delay: Long ->
            view.animate()
                .translationY(-12f)
                .setDuration(1000)
                .setStartDelay(delay)
                .withEndAction {
                    view.animate()
                        .translationY(0f)
                        .setDuration(1000)
                        .withEndAction { animateDecorations() }
                        .start()
                }
                .start()
        }
        floatAnim(binding.starTopLeft, 0)
        floatAnim(binding.starTopRight, 300)
        floatAnim(binding.heartLeft, 600)
    }

    private fun scheduleNavigation() {
        Handler(Looper.getMainLooper()).postDelayed({
            navigateNext()
        }, SPLASH_DURATION)
    }

    private fun navigateNext() {
        binding.root.animate()
            .alpha(0f)
            .setDuration(400)
            .withEndAction {
                val intent = if (sessionManager.isLoggedIn()) {
                    Intent(this, HomeActivity::class.java)
                } else {
                    Intent(this, LoginActivity::class.java)
                }
                startActivity(intent)
                finish()
                overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out)
            }
            .start()
    }
}