package com.example.ekidi.ui.game

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class GameCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    // ─── State ───────────────────────────────────────────────────
    var isRunning = false
    var isStopped = false
    var gameOver = false

    // ─── Karakter ────────────────────────────────────────────────
    var karakterX = 0f
    var karakterY = 0f
    private val karakterSize = 80f
    private var jumpVelocity = 0f
    private var isJumping = false
    private var isOnGround = true
    private var isHurt = false
    private var hurtTimer = 0
    private var runFrame = 0
    private var runTimer = 0
    private var legAngle = 0f
    private var legDir = 1f

    // ─── Batu ────────────────────────────────────────────────────
    var batuX = 0f
    var batuY = 0f
    var batuVisible = false
    private val batuWidth = 110f
    private val batuHeight = 90f

    // ─── Ground & Background ─────────────────────────────────────
    private var groundY = 0f
    private val groundHeight = 50f
    private var bgScrollX = 0f
    private val bgSpeed = 4f
    private val clouds = mutableListOf<CloudData>()
    private val bgTrees = mutableListOf<TreeData>()
    private val groundTiles = mutableListOf<Float>()
    private var initialized = false

    // ─── Callbacks ───────────────────────────────────────────────
    var onObstacleReached: (() -> Unit)? = null

    // ─── Partikel ────────────────────────────────────────────────
    private val particles = mutableListOf<ParticleData>()

    // ─── Data Classes ────────────────────────────────────────────
    data class CloudData(var x: Float, var y: Float, val size: Float, val speed: Float)
    data class TreeData(var x: Float, val h: Float, val w: Float, val speed: Float)
    data class ParticleData(
        var x: Float, var y: Float,
        var vx: Float, var vy: Float,
        var life: Int, val maxLife: Int,
        val color: Int, var size: Float
    )

    // ─── Paint Objects ───────────────────────────────────────────

    // Sky gradient
    private val skyPaint = Paint()
    private lateinit var skyShader: LinearGradient

    // Ground
    private val groundPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val groundTopPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
        color = Color.parseColor("#4CAF50")
    }
    private val groundShadowPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#2E7D32")
    }

    // Ground texture
    private val grassPaint = Paint().apply {
        color = Color.parseColor("#66BB6A")
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }

    // Cloud
    private val cloudPaint = Paint().apply {
        color = Color.WHITE
        alpha = 200
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // Tree
    private val treeTrunkPaint = Paint().apply {
        color = Color.parseColor("#795548")
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val treeLeavePaint = Paint().apply {
        color = Color.parseColor("#2E7D32")
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val treeLeaveHighlightPaint = Paint().apply {
        color = Color.parseColor("#4CAF50")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // Batu 3D paints
    private val batuBasePaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val batuTopPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val batuSidePaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val batuShadowPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
        color = Color.parseColor("#33000000")
    }
    private val batuCrackPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f
        color = Color.parseColor("#607D8B")
        isAntiAlias = true
        alpha = 150
    }
    private val batuHighlightPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
        color = Color.parseColor("#ECEFF1")
        alpha = 100
    }

    // Karakter paints
    private val bodyPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val headPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
        color = Color.parseColor("#FFCC80")
    }
    private val headBorderPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.parseColor("#FF8A65")
        isAntiAlias = true
    }
    private val eyePaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
        color = Color.parseColor("#212121")
    }
    private val mouthPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }
    private val shirtPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
        color = Color.parseColor("#1565C0")
    }
    private val pantsPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
        color = Color.parseColor("#4E342E")
    }
    private val shoesPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
        color = Color.parseColor("#212121")
    }
    private val limbPaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 10f
        color = Color.parseColor("#FFCC80")
    }
    private val armPaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 9f
        color = Color.parseColor("#1565C0")
    }

    // Particle
    private val particlePaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // Shadow karakter
    private val charShadowPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        groundY = h * 0.78f
        karakterX = w * 0.18f
        karakterY = groundY - karakterSize

        // Sky gradient
        skyShader = LinearGradient(
            0f, 0f, 0f, groundY,
            intArrayOf(
                Color.parseColor("#87CEEB"),
                Color.parseColor("#B0E0E6"),
                Color.parseColor("#E0F7FA")
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        skyPaint.shader = skyShader

        // Ground gradient
        groundPaint.shader = LinearGradient(
            0f, groundY, 0f, h.toFloat(),
            intArrayOf(
                Color.parseColor("#5D4037"),
                Color.parseColor("#4E342E"),
                Color.parseColor("#3E2723")
            ),
            floatArrayOf(0f, 0.4f, 1f),
            Shader.TileMode.CLAMP
        )

        if (!initialized) {
            // Init clouds
            repeat(4) {
                clouds.add(CloudData(
                    x = (Math.random() * w).toFloat(),
                    y = (Math.random() * groundY * 0.5f + 20).toFloat(),
                    size = (Math.random() * 40 + 25).toFloat(),
                    speed = (Math.random() * 1.5 + 0.5).toFloat()
                ))
            }

            // Init trees
            repeat(5) { i ->
                bgTrees.add(TreeData(
                    x = (w * 0.15f + i * w * 0.18f),
                    h = (Math.random() * 50 + 60).toFloat(),
                    w = (Math.random() * 20 + 20).toFloat(),
                    speed = (Math.random() * 1.5 + 1).toFloat()
                ))
            }

            // Init ground tiles
            var tx = 0f
            while (tx < w + 60) {
                groundTiles.add(tx)
                tx += 60f
            }

            // Init batu position
            batuX = w * 0.85f
            batuY = groundY - batuHeight + 15f

            initialized = true
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()

        // 1. Sky
        canvas.drawRect(0f, 0f, w, groundY, skyPaint)

        // 2. Clouds
        drawClouds(canvas)

        // 3. Background trees
        drawBgTrees(canvas)

        // 4. Ground
        drawGround(canvas, w, h)

        // 5. Batu (di depan ground, di belakang karakter)
        if (batuVisible) drawBatu3D(canvas)

        // 6. Shadow karakter
        val shadowAlpha = if (isJumping) {
            val distFromGround = (groundY - karakterSize) - karakterY
            (180 - (distFromGround * 1.5f).toInt()).coerceIn(40, 180)
        } else 150
        val shadowWidth = if (isJumping) {
            val distFromGround = (groundY - karakterSize) - karakterY
            (karakterSize * 0.8f - distFromGround * 0.3f).coerceIn(20f, karakterSize * 0.8f)
        } else karakterSize * 0.8f

        charShadowPaint.shader = RadialGradient(
            karakterX, groundY + 5f,
            shadowWidth / 2,
            intArrayOf(Color.parseColor("#66000000"), Color.TRANSPARENT),
            null, Shader.TileMode.CLAMP
        )
        canvas.drawOval(
            karakterX - shadowWidth / 2, groundY,
            karakterX + shadowWidth / 2, groundY + 12f,
            charShadowPaint
        )

        // 7. Karakter
        drawKarakter(canvas)

        // 8. Partikel
        drawParticles(canvas)

        // Update state
        if (isRunning) {
            updateGame(w)
        }

        if (isRunning) invalidate()
    }

    // ─── Draw Cloud ───────────────────────────────────────────────
    private fun drawClouds(canvas: Canvas) {
        clouds.forEach { cloud ->
            // Awan terdiri dari beberapa lingkaran
            canvas.drawCircle(cloud.x, cloud.y, cloud.size * 0.6f, cloudPaint)
            canvas.drawCircle(cloud.x + cloud.size * 0.5f, cloud.y + cloud.size * 0.1f, cloud.size * 0.45f, cloudPaint)
            canvas.drawCircle(cloud.x - cloud.size * 0.4f, cloud.y + cloud.size * 0.15f, cloud.size * 0.4f, cloudPaint)
            canvas.drawCircle(cloud.x + cloud.size * 0.1f, cloud.y - cloud.size * 0.2f, cloud.size * 0.35f, cloudPaint)
        }
    }

    // ─── Draw Background Trees ────────────────────────────────────
    private fun drawBgTrees(canvas: Canvas) {
        bgTrees.forEach { tree ->
            val tx = tree.x
            val ty = groundY

            // Trunk
            canvas.drawRect(
                tx - tree.w * 0.15f, ty - tree.h,
                tx + tree.w * 0.15f, ty,
                treeTrunkPaint
            )

            // Leaves (3 layers untuk efek 3D)
            treeLeavePaint.alpha = 180
            canvas.drawCircle(tx, ty - tree.h - tree.w * 0.3f, tree.w * 0.6f, treeLeavePaint)

            treeLeaveHighlightPaint.alpha = 150
            canvas.drawCircle(tx - tree.w * 0.1f, ty - tree.h - tree.w * 0.5f, tree.w * 0.4f, treeLeaveHighlightPaint)

            treeLeavePaint.alpha = 120
            canvas.drawCircle(tx + tree.w * 0.2f, ty - tree.h - tree.w * 0.2f, tree.w * 0.35f, treeLeavePaint)
        }
    }

    // ─── Draw Ground ─────────────────────────────────────────────
    private fun drawGround(canvas: Canvas, w: Float, h: Float) {
        // Top grass strip
        canvas.drawRect(0f, groundY - 8f, w, groundY, groundTopPaint)

        // Ground body
        canvas.drawRect(0f, groundY, w, h, groundPaint)

        // Grass tufts
        groundTiles.forEach { tx ->
            val rx = ((tx - bgScrollX * 0.5f) % (w + 60) + w + 60) % (w + 60) - 30
            // Rumput kecil
            canvas.drawLine(rx, groundY - 8f, rx - 4f, groundY - 18f, grassPaint)
            canvas.drawLine(rx + 8f, groundY - 8f, rx + 8f, groundY - 15f, grassPaint)
            canvas.drawLine(rx + 16f, groundY - 8f, rx + 20f, groundY - 16f, grassPaint)
        }

        // Shadow bawah ground top
        canvas.drawRect(0f, groundY, w, groundY + 6f, groundShadowPaint)
    }

    // ─── Draw Batu 3D ─────────────────────────────────────────────
    private fun drawBatu3D(canvas: Canvas) {
        val bx = batuX
        val by = batuY
        val bw = batuWidth
        val bh = batuHeight
        val depth = 18f // kedalaman 3D

        // Shadow batu di tanah
        val shadowPaint2 = Paint().apply {
            shader = RadialGradient(
                bx, groundY + 5f, bw * 0.7f,
                intArrayOf(Color.parseColor("#55000000"), Color.TRANSPARENT),
                null, Shader.TileMode.CLAMP
            )
            style = Paint.Style.FILL
        }
        canvas.drawOval(bx - bw * 0.6f, groundY - 4f, bx + bw * 0.6f, groundY + 14f, shadowPaint2)

        // ── Sisi kanan batu (efek 3D depth) ──
        val rightPath = Path().apply {
            moveTo(bx + bw * 0.45f, by + bh * 0.15f)           // titik kanan atas muka
            lineTo(bx + bw * 0.45f + depth, by + bh * 0.15f - depth * 0.6f)  // titik kanan atas belakang
            lineTo(bx + bw * 0.45f + depth, by + bh + depth * 0.1f)           // titik kanan bawah belakang
            lineTo(bx + bw * 0.45f, by + bh)                    // titik kanan bawah muka
            close()
        }
        batuSidePaint.shader = LinearGradient(
            bx + bw * 0.45f, by,
            bx + bw * 0.45f + depth, by,
            Color.parseColor("#546E7A"),
            Color.parseColor("#263238"),
            Shader.TileMode.CLAMP
        )
        canvas.drawPath(rightPath, batuSidePaint)

        // ── Sisi atas batu (efek 3D top) ──
        val topPath = Path().apply {
            moveTo(bx - bw * 0.45f, by + bh * 0.05f)             // kiri atas muka
            lineTo(bx - bw * 0.45f + depth * 0.7f, by + bh * 0.05f - depth * 0.6f) // kiri atas belakang
            lineTo(bx + bw * 0.45f + depth, by + bh * 0.15f - depth * 0.6f)         // kanan atas belakang
            lineTo(bx + bw * 0.45f, by + bh * 0.15f)             // kanan atas muka
            close()
        }
        batuTopPaint.shader = LinearGradient(
            bx, by,
            bx, by - depth,
            Color.parseColor("#B0BEC5"),
            Color.parseColor("#90A4AE"),
            Shader.TileMode.CLAMP
        )
        canvas.drawPath(topPath, batuTopPaint)

        // ── Muka batu utama ──
        val mainPath = Path().apply {
            // Bentuk batu tidak simetris untuk kesan natural
            moveTo(bx - bw * 0.45f, by + bh)           // bawah kiri
            lineTo(bx + bw * 0.45f, by + bh)           // bawah kanan
            lineTo(bx + bw * 0.5f, by + bh * 0.5f)    // kanan tengah
            lineTo(bx + bw * 0.45f, by + bh * 0.15f)  // kanan atas
            lineTo(bx + bw * 0.1f, by)                  // atas kanan tengah
            lineTo(bx - bw * 0.15f, by + bh * 0.05f)  // atas kiri tengah
            lineTo(bx - bw * 0.45f, by + bh * 0.2f)   // kiri atas
            lineTo(bx - bw * 0.5f, by + bh * 0.55f)   // kiri tengah
            close()
        }
        batuBasePaint.shader = LinearGradient(
            bx - bw * 0.5f, by,
            bx + bw * 0.5f, by + bh,
            intArrayOf(
                Color.parseColor("#90A4AE"),
                Color.parseColor("#78909C"),
                Color.parseColor("#546E7A"),
                Color.parseColor("#455A64")
            ),
            floatArrayOf(0f, 0.3f, 0.7f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawPath(mainPath, batuBasePaint)

        // ── Highlight di kiri atas (cahaya) ──
        val highlightPath = Path().apply {
            moveTo(bx - bw * 0.35f, by + bh * 0.25f)
            lineTo(bx - bw * 0.05f, by + bh * 0.08f)
            lineTo(bx + bw * 0.1f, by + bh * 0.15f)
            lineTo(bx - bw * 0.1f, by + bh * 0.35f)
            close()
        }
        canvas.drawPath(highlightPath, batuHighlightPaint)

        // ── Retakan batu ──
        // Retakan 1
        val crack1 = Path().apply {
            moveTo(bx - bw * 0.1f, by + bh * 0.3f)
            lineTo(bx + bw * 0.05f, by + bh * 0.45f)
            lineTo(bx + bw * 0.15f, by + bh * 0.65f)
        }
        canvas.drawPath(crack1, batuCrackPaint)

        // Retakan 2
        val crack2 = Path().apply {
            moveTo(bx + bw * 0.05f, by + bh * 0.45f)
            lineTo(bx + bw * 0.2f, by + bh * 0.4f)
        }
        canvas.drawPath(crack2, batuCrackPaint)

        // Retakan 3
        val crack3 = Path().apply {
            moveTo(bx - bw * 0.25f, by + bh * 0.55f)
            lineTo(bx - bw * 0.05f, by + bh * 0.7f)
            lineTo(bx + bw * 0.1f, by + bh * 0.68f)
        }
        canvas.drawPath(crack3, batuCrackPaint)

        // ── Bintik-bintik tekstur ──
        val specklePaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            color = Color.parseColor("#607D8B")
            alpha = 80
        }
        listOf(
            Pair(bx - bw * 0.2f, by + bh * 0.4f),
            Pair(bx + bw * 0.2f, by + bh * 0.5f),
            Pair(bx - bw * 0.05f, by + bh * 0.7f),
            Pair(bx + bw * 0.3f, by + bh * 0.3f),
            Pair(bx - bw * 0.3f, by + bh * 0.7f)
        ).forEach { (sx, sy) ->
            canvas.drawCircle(sx, sy, 4f, specklePaint)
        }

        // ── Outline batu ──
        val outlinePaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = Color.parseColor("#37474F")
            isAntiAlias = true
        }
        canvas.drawPath(mainPath, outlinePaint)
    }

    // ─── Draw Karakter ────────────────────────────────────────────
    private fun drawKarakter(canvas: Canvas) {
        val cx = karakterX
        val cy = if (isHurt) karakterY + 15f else karakterY // Stumble lower
        val s = karakterSize

        canvas.save()

        // Efek hurt: getar + rotasi sedikit jatuh ke depan
        if (isHurt) {
            canvas.rotate(15f, cx, cy + s)
            if (hurtTimer % 4 < 2) {
                canvas.translate((Math.random() * 8 - 4).toFloat(), 0f)
            }
        }

        val bodyY = cy + s * 0.42f
        val bodyH = s * 0.32f
        val bodyW = s * 0.28f

        // ── Kaki (bergerak saat lari) ──
        val leftLegAngle = if (isJumping) -30f else legAngle
        val rightLegAngle = if (isJumping) -30f else -legAngle

        // Kaki kiri
        canvas.save()
        canvas.rotate(leftLegAngle, cx, bodyY + bodyH)
        // Paha
        limbPaint.strokeWidth = 11f
        limbPaint.color = Color.parseColor("#4E342E") // celana
        canvas.drawLine(cx - bodyW * 0.3f, bodyY + bodyH * 0.7f,
            cx - bodyW * 0.3f, bodyY + bodyH * 0.7f + s * 0.22f, limbPaint)
        // Sepatu kiri
        canvas.drawOval(
            cx - bodyW * 0.3f - 12f, bodyY + bodyH * 0.7f + s * 0.22f - 6f,
            cx - bodyW * 0.3f + 14f, bodyY + bodyH * 0.7f + s * 0.22f + 6f,
            shoesPaint
        )
        canvas.restore()

        // Kaki kanan
        canvas.save()
        canvas.rotate(rightLegAngle, cx, bodyY + bodyH)
        limbPaint.color = Color.parseColor("#4E342E")
        canvas.drawLine(cx + bodyW * 0.3f, bodyY + bodyH * 0.7f,
            cx + bodyW * 0.3f, bodyY + bodyH * 0.7f + s * 0.22f, limbPaint)
        canvas.drawOval(
            cx + bodyW * 0.3f - 14f, bodyY + bodyH * 0.7f + s * 0.22f - 6f,
            cx + bodyW * 0.3f + 12f, bodyY + bodyH * 0.7f + s * 0.22f + 6f,
            shoesPaint
        )
        canvas.restore()

        // ── Tubuh (kaos) ──
        val bodyRect = RectF(cx - bodyW, bodyY, cx + bodyW, bodyY + bodyH)
        canvas.drawRoundRect(bodyRect, 10f, 10f, shirtPaint)

        // Highlight kaos
        val shirtHighlight = Paint().apply {
            color = Color.parseColor("#1976D2")
            style = Paint.Style.FILL
            isAntiAlias = true
            alpha = 120
        }
        canvas.drawRoundRect(
            RectF(cx - bodyW * 0.5f, bodyY + 4f, cx + bodyW * 0.3f, bodyY + bodyH * 0.5f),
            6f, 6f, shirtHighlight
        )

        // ── Tangan (bergerak saat lari) ──
        val leftArmAngle = if (isJumping) -60f else -legAngle * 0.7f
        val rightArmAngle = if (isJumping) 60f else legAngle * 0.7f

        // Tangan kiri
        canvas.save()
        canvas.rotate(leftArmAngle, cx - bodyW, bodyY + bodyH * 0.2f)
        armPaint.strokeWidth = 9f
        canvas.drawLine(
            cx - bodyW, bodyY + bodyH * 0.2f,
            cx - bodyW - s * 0.15f, bodyY + bodyH * 0.55f, armPaint
        )
        // Tangan (warna kulit)
        limbPaint.strokeWidth = 7f
        limbPaint.color = Color.parseColor("#FFCC80")
        canvas.drawCircle(cx - bodyW - s * 0.15f, bodyY + bodyH * 0.55f, 6f, headPaint)
        canvas.restore()

        // Tangan kanan
        canvas.save()
        canvas.rotate(rightArmAngle, cx + bodyW, bodyY + bodyH * 0.2f)
        canvas.drawLine(
            cx + bodyW, bodyY + bodyH * 0.2f,
            cx + bodyW + s * 0.15f, bodyY + bodyH * 0.55f, armPaint
        )
        canvas.drawCircle(cx + bodyW + s * 0.15f, bodyY + bodyH * 0.55f, 6f, headPaint)
        canvas.restore()

        // ── Kepala ──
        val headR = s * 0.22f
        val headCY = cy + headR + 2f
        canvas.drawCircle(cx, headCY, headR, headPaint)
        canvas.drawCircle(cx, headCY, headR, headBorderPaint)

        // Rambut
        val hairPaint = Paint().apply {
            color = Color.parseColor("#4E342E")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val hairPath = Path().apply {
            moveTo(cx - headR, headCY - headR * 0.3f)
            quadTo(cx - headR * 0.5f, headCY - headR * 1.3f, cx, headCY - headR * 1.1f)
            quadTo(cx + headR * 0.5f, headCY - headR * 1.3f, cx + headR, headCY - headR * 0.3f)
            quadTo(cx + headR * 0.8f, headCY - headR * 0.8f, cx, headCY - headR)
            quadTo(cx - headR * 0.8f, headCY - headR * 0.8f, cx - headR, headCY - headR * 0.3f)
            close()
        }
        canvas.drawPath(hairPath, hairPaint)

        // Mata
        if (isHurt) {
            // Mata X saat kena
            val xPaint = Paint().apply {
                style = Paint.Style.STROKE; strokeWidth = 2.5f
                color = Color.parseColor("#D32F2F"); isAntiAlias = true
            }
            canvas.drawLine(cx - headR * 0.35f, headCY - headR * 0.2f,
                cx - headR * 0.15f, headCY, xPaint)
            canvas.drawLine(cx - headR * 0.15f, headCY - headR * 0.2f,
                cx - headR * 0.35f, headCY, xPaint)
            canvas.drawLine(cx + headR * 0.15f, headCY - headR * 0.2f,
                cx + headR * 0.35f, headCY, xPaint)
            canvas.drawLine(cx + headR * 0.35f, headCY - headR * 0.2f,
                cx + headR * 0.15f, headCY, xPaint)
        } else if (isJumping) {
            // Mata bintang saat lompat
            eyePaint.color = Color.parseColor("#212121")
            canvas.drawCircle(cx - headR * 0.28f, headCY - headR * 0.1f, 4f, eyePaint)
            canvas.drawCircle(cx + headR * 0.28f, headCY - headR * 0.1f, 4f, eyePaint)
            // Highlight mata
            val eyeHL = Paint().apply {
                color = Color.WHITE; style = Paint.Style.FILL; isAntiAlias = true
            }
            canvas.drawCircle(cx - headR * 0.23f, headCY - headR * 0.15f, 1.5f, eyeHL)
            canvas.drawCircle(cx + headR * 0.33f, headCY - headR * 0.15f, 1.5f, eyeHL)
        } else {
            // Mata normal
            eyePaint.color = Color.parseColor("#212121")
            canvas.drawCircle(cx - headR * 0.28f, headCY - headR * 0.1f, 3.5f, eyePaint)
            canvas.drawCircle(cx + headR * 0.28f, headCY - headR * 0.1f, 3.5f, eyePaint)
            val eyeHL = Paint().apply {
                color = Color.WHITE; style = Paint.Style.FILL; isAntiAlias = true
            }
            canvas.drawCircle(cx - headR * 0.23f, headCY - headR * 0.15f, 1.5f, eyeHL)
            canvas.drawCircle(cx + headR * 0.33f, headCY - headR * 0.15f, 1.5f, eyeHL)
        }

        // Mulut
        mouthPaint.color = if (isHurt) Color.parseColor("#D32F2F")
        else if (isJumping) Color.parseColor("#1B5E20")
        else Color.parseColor("#BF360C")
        mouthPaint.strokeWidth = 2.5f

        if (isJumping) {
            // Senyum lebar saat lompat
            val mouthPath = Path().apply {
                moveTo(cx - headR * 0.3f, headCY + headR * 0.2f)
                quadTo(cx, headCY + headR * 0.5f, cx + headR * 0.3f, headCY + headR * 0.2f)
            }
            canvas.drawPath(mouthPath, mouthPaint)
        } else if (isHurt) {
            // Mulut cemberut saat kena
            val mouthPath = Path().apply {
                moveTo(cx - headR * 0.3f, headCY + headR * 0.35f)
                quadTo(cx, headCY + headR * 0.1f, cx + headR * 0.3f, headCY + headR * 0.35f)
            }
            canvas.drawPath(mouthPath, mouthPaint)
        } else {
            // Mulut senyum biasa
            val mouthPath = Path().apply {
                moveTo(cx - headR * 0.25f, headCY + headR * 0.2f)
                quadTo(cx, headCY + headR * 0.42f, cx + headR * 0.25f, headCY + headR * 0.2f)
            }
            canvas.drawPath(mouthPath, mouthPaint)
        }

        canvas.restore()
    }

    // ─── Draw Particles ───────────────────────────────────────────
    private fun drawParticles(canvas: Canvas) {
        val toRemove = mutableListOf<ParticleData>()
        particles.forEach { p ->
            val alpha = ((p.life.toFloat() / p.maxLife) * 255).toInt()
            particlePaint.color = p.color
            particlePaint.alpha = alpha
            canvas.drawCircle(p.x, p.y, p.size * (p.life.toFloat() / p.maxLife), particlePaint)
            p.x += p.vx
            p.y += p.vy
            p.vy += 0.3f
            p.life--
            if (p.life <= 0) toRemove.add(p)
        }
        particles.removeAll(toRemove)
    }

    // ─── Update Game Logic ────────────────────────────────────────
    private fun updateGame(w: Float) {
        if (!isStopped) {
            // Scroll background
            bgScrollX += bgSpeed

            // Update clouds
            clouds.forEach { cloud ->
                cloud.x -= cloud.speed
                if (cloud.x < -cloud.size * 2) cloud.x = w + cloud.size
            }

            // Update trees
            bgTrees.forEach { tree ->
                tree.x -= tree.speed
                if (tree.x < -tree.w * 2) tree.x = w + tree.w
            }

            // Update batu (moving towards character)
            if (batuVisible) {
                batuX -= bgSpeed
                val stopDistance = 150f
                if (batuX <= karakterX + stopDistance) {
                    batuX = karakterX + stopDistance
                    isStopped = true
                    onObstacleReached?.invoke()
                }
            }
        }

        // Update jump (always update even if stopped for anim)
        if (isJumping) {
            karakterY += jumpVelocity
            jumpVelocity += 1.4f
            
            // Move batu past character during jump for realism
            if (karakterY < groundY - karakterSize - 20) {
                batuX -= 8f 
            }

            if (karakterY >= groundY - karakterSize) {
                karakterY = groundY - karakterSize
                jumpVelocity = 0f
                isJumping = false
                isOnGround = true
                addLandParticles()
            }
        }

        // Update leg animation (only if running)
        if (!isStopped && !isJumping) {
            legAngle += legDir * 5f
            if (legAngle > 35f || legAngle < -35f) legDir *= -1
        } else if (isStopped && !isJumping) {
            // Idle position
            legAngle = if (legAngle > 0) (legAngle - 2f).coerceAtLeast(0f) 
                       else (legAngle + 2f).coerceAtLeast(0f)
        }

        // Update hurt timer
        if (isHurt) {
            hurtTimer++
            if (hurtTimer > 35) {
                isHurt = false
                hurtTimer = 0
                // After hurt, move batu past to continue
                if (batuVisible) {
                    batuX -= 120f
                    isStopped = false
                }
            }
        }
    }

    // ─── Public Functions ─────────────────────────────────────────
    fun spawnBatu() {
        val w = width.toFloat()
        batuX = w + 100f
        batuVisible = true
        isStopped = false
    }
    fun doJump() {
        if (isOnGround && !isJumping) {
            isJumping = true
            isOnGround = false
            jumpVelocity = -28f // Increased for better clearance
            addJumpParticles()
            isStopped = false // Resume background movement
        }
    }

    fun doHurt() {
        isHurt = true
        hurtTimer = 0
        addHurtParticles()
    }

    fun addSuccessParticles() {
        val colors = listOf(
            Color.parseColor("#FFD700"),
            Color.parseColor("#10B981"),
            Color.parseColor("#7C3AED"),
            Color.parseColor("#F59E0B"),
            Color.parseColor("#EF4444")
        )
        repeat(20) {
            particles.add(ParticleData(
                x = karakterX + (Math.random() * 60 - 30).toFloat(),
                y = karakterY + karakterSize * 0.3f,
                vx = (Math.random() * 10 - 5).toFloat(),
                vy = (Math.random() * -12 - 2).toFloat(),
                life = 45, maxLife = 45,
                color = colors.random(),
                size = (Math.random() * 12 + 5).toFloat()
            ))
        }
    }

    private fun addJumpParticles() {
        repeat(10) {
            particles.add(ParticleData(
                x = karakterX + (Math.random() * 30 - 15).toFloat(),
                y = groundY,
                vx = (Math.random() * 6 - 3).toFloat(),
                vy = (Math.random() * -5 - 1).toFloat(),
                life = 25, maxLife = 25,
                color = Color.parseColor("#A5D6A7"),
                size = (Math.random() * 7 + 3).toFloat()
            ))
        }
    }

    private fun addHurtParticles() {
        repeat(15) {
            particles.add(ParticleData(
                x = karakterX,
                y = karakterY + karakterSize * 0.5f,
                vx = (Math.random() * 12 - 6).toFloat(),
                vy = (Math.random() * -9 - 1).toFloat(),
                life = 35, maxLife = 35,
                color = Color.parseColor("#EF4444"),
                size = (Math.random() * 10 + 4).toFloat()
            ))
        }
    }

    private fun addLandParticles() {
        repeat(8) {
            particles.add(ParticleData(
                x = karakterX + (Math.random() * 40 - 20).toFloat(),
                y = groundY,
                vx = (Math.random() * 8 - 4).toFloat(),
                vy = (Math.random() * -4).toFloat(),
                life = 20, maxLife = 20,
                color = Color.parseColor("#8D6E63"),
                size = (Math.random() * 5 + 2).toFloat()
            ))
        }
    }

    fun startGame() {
        isRunning = true
        isStopped = false
        invalidate()
    }

    fun stopGame() {
        isRunning = false
    }

    fun resumeGame() {
        isRunning = true
        isStopped = false
        invalidate()
    }
}