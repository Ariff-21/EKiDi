package com.example.ekidi.ui.game

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class GameCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    // ─── State Game ──────────────────────────────────────────────
    var karakter = KarakterData()
    var rintanganList = mutableListOf<RintanganData>()
    var isRunning = false
    var isJumping = false
    var isStopped = false
    var gameOver = false

    // Background scroll
    private var bgOffset = 0f
    private val bgSpeed = 3f

    // Ground
    private var groundY = 0f
    private val groundHeight = 60f

    // Paint objects
    private val paintGround = Paint().apply {
        color = Color.parseColor("#0f3460")
        style = Paint.Style.FILL
    }

    private val paintGroundLine = Paint().apply {
        color = Color.parseColor("#e94560")
        style = Paint.Style.FILL
        strokeWidth = 3f
    }

    private val paintBg = Paint().apply {
        color = Color.parseColor("#1a1a2e")
        style = Paint.Style.FILL
    }

    private val paintBgStripe = Paint().apply {
        color = Color.parseColor("#16213e")
        style = Paint.Style.FILL
        alpha = 100
    }

    private val paintText = Paint().apply {
        color = Color.WHITE
        textSize = 60f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val paintRintangan = Paint().apply {
        color = Color.parseColor("#e94560")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val paintRintanganBorder = Paint().apply {
        color = Color.parseColor("#FF6B6B")
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    private val paintParticle = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // Particles untuk efek
    private val particles = mutableListOf<ParticleData>()

    // Star background
    private val stars = mutableListOf<StarData>()
    private var starsInitialized = false

    data class KarakterData(
        var x: Float = 150f,
        var y: Float = 0f,
        var size: Float = 70f,
        var jumpVelocity: Float = 0f,
        var isOnGround: Boolean = true,
        var animFrame: Int = 0,
        var animTimer: Int = 0,
        var isHurt: Boolean = false,
        var hurtTimer: Int = 0
    )

    data class RintanganData(
        var x: Float,
        var y: Float,
        var size: Float = 65f,
        var emoji: String,
        var speed: Float = 5f,
        var passed: Boolean = false
    )

    data class ParticleData(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var life: Int,
        var maxLife: Int,
        var color: Int,
        var size: Float
    )

    data class StarData(
        val x: Float,
        val y: Float,
        val size: Float,
        val alpha: Int
    )

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        groundY = h - groundHeight - 20f
        karakter.y = groundY - karakter.size
        karakter.x = w * 0.2f

        // Init stars
        if (!starsInitialized) {
            repeat(30) {
                stars.add(StarData(
                    x = (Math.random() * w).toFloat(),
                    y = (Math.random() * h * 0.7f).toFloat(),
                    size = (Math.random() * 3 + 1).toFloat(),
                    alpha = (Math.random() * 200 + 55).toInt()
                ))
            }
            starsInitialized = true
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        // Background
        canvas.drawRect(0f, 0f, w, h, paintBg)

        // Stars
        val starPaint = Paint().apply { isAntiAlias = true }
        stars.forEach { star ->
            starPaint.color = Color.WHITE
            starPaint.alpha = star.alpha
            canvas.drawCircle(star.x, star.y, star.size, starPaint)
        }

        // Garis dekorasi background
        for (i in 0..5) {
            val stripeX = ((bgOffset + i * w / 3) % (w + 100)) - 50
            canvas.drawRect(stripeX, 0f, stripeX + 2f, groundY, paintBgStripe)
        }

        // Ground
        canvas.drawRect(0f, groundY, w, h, paintGround)
        canvas.drawLine(0f, groundY, w, groundY, paintGroundLine)

        // Garis dekorasi ground
        val dashPaint = Paint().apply {
            color = Color.parseColor("#e94560")
            alpha = 80
            strokeWidth = 2f
        }
        for (i in 0..20) {
            val dashX = ((bgOffset * 2 + i * 60) % (w + 60)) - 30
            canvas.drawLine(dashX, groundY + 15, dashX + 30, groundY + 15, dashPaint)
        }

        // Rintangan
        rintanganList.forEach { rintangan ->
            if (!rintangan.passed) {
                // Kotak rintangan
                val rect = RectF(
                    rintangan.x - rintangan.size / 2,
                    rintangan.y - rintangan.size / 2,
                    rintangan.x + rintangan.size / 2,
                    rintangan.y + rintangan.size / 2
                )
                canvas.drawRoundRect(rect, 16f, 16f, paintRintangan)
                canvas.drawRoundRect(rect, 16f, 16f, paintRintanganBorder)

                // Emoji rintangan
                paintText.textSize = rintangan.size * 0.6f
                canvas.drawText(rintangan.emoji, rintangan.x, rintangan.y + rintangan.size * 0.2f, paintText)
            }
        }

        // Karakter
        val karakterEmoji = when {
            karakter.isHurt -> "😵"
            isJumping -> "😄"
            !isRunning -> "😐"
            karakter.animFrame == 0 -> "🏃"
            else -> "🏃"
        }

        // Flash efek saat kena
        if (!karakter.isHurt || karakter.hurtTimer % 4 < 2) {
            paintText.textSize = karakter.size * 0.85f
            canvas.drawText(
                karakterEmoji,
                karakter.x,
                karakter.y + karakter.size * 0.8f,
                paintText
            )
        }

        // Particles
        val toRemove = mutableListOf<ParticleData>()
        particles.forEach { p ->
            val alpha = ((p.life.toFloat() / p.maxLife) * 255).toInt()
            paintParticle.color = p.color
            paintParticle.alpha = alpha
            canvas.drawCircle(p.x, p.y, p.size * (p.life.toFloat() / p.maxLife), paintParticle)
            p.x += p.vx
            p.y += p.vy
            p.vy += 0.2f
            p.life--
            if (p.life <= 0) toRemove.add(p)
        }
        particles.removeAll(toRemove)

        // Update animasi
        if (isRunning && !isStopped) {
            bgOffset = (bgOffset + bgSpeed) % (width / 3f)
            karakter.animTimer++
            if (karakter.animTimer > 8) {
                karakter.animFrame = (karakter.animFrame + 1) % 2
                karakter.animTimer = 0
            }
        }

        // Update jump
        if (isJumping) {
            karakter.y += karakter.jumpVelocity
            karakter.jumpVelocity += 1.5f
            if (karakter.y >= groundY - karakter.size) {
                karakter.y = groundY - karakter.size
                karakter.jumpVelocity = 0f
                isJumping = false
                karakter.isOnGround = true
            }
        }

        // Update hurt timer
        if (karakter.isHurt) {
            karakter.hurtTimer++
            if (karakter.hurtTimer > 30) {
                karakter.isHurt = false
                karakter.hurtTimer = 0
            }
        }

        if (isRunning) invalidate()
    }

    fun doJump() {
        if (karakter.isOnGround && !isJumping) {
            isJumping = true
            karakter.isOnGround = false
            karakter.jumpVelocity = -22f
            addJumpParticles()
        }
    }

    fun doHurt() {
        karakter.isHurt = true
        karakter.hurtTimer = 0
        addHurtParticles()
    }

    private fun addJumpParticles() {
        repeat(8) {
            particles.add(ParticleData(
                x = karakter.x,
                y = karakter.y + karakter.size,
                vx = (Math.random() * 6 - 3).toFloat(),
                vy = (Math.random() * -4).toFloat(),
                life = 20,
                maxLife = 20,
                color = Color.parseColor("#FFD700"),
                size = (Math.random() * 8 + 4).toFloat()
            ))
        }
    }

    private fun addHurtParticles() {
        repeat(12) {
            particles.add(ParticleData(
                x = karakter.x,
                y = karakter.y + karakter.size / 2,
                vx = (Math.random() * 10 - 5).toFloat(),
                vy = (Math.random() * -8).toFloat(),
                life = 30,
                maxLife = 30,
                color = Color.parseColor("#EF4444"),
                size = (Math.random() * 10 + 5).toFloat()
            ))
        }
    }

    fun addSuccessParticles() {
        repeat(15) {
            particles.add(ParticleData(
                x = karakter.x + (Math.random() * 100 - 50).toFloat(),
                y = karakter.y,
                vx = (Math.random() * 8 - 4).toFloat(),
                vy = (Math.random() * -10 - 2).toFloat(),
                life = 40,
                maxLife = 40,
                color = listOf(
                    Color.parseColor("#FFD700"),
                    Color.parseColor("#10B981"),
                    Color.parseColor("#7C3AED")
                ).random(),
                size = (Math.random() * 12 + 6).toFloat()
            ))
        }
    }

    fun startGame() {
        isRunning = true
        isStopped = false
        invalidate()
    }

    fun stopGame() {
        isStopped = true
    }

    fun resumeGame() {
        isStopped = false
        invalidate()
    }
}