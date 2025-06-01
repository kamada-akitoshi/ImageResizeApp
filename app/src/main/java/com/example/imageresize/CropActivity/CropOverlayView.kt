package com.example.imageresize.cropactivity

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
class CropOverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val strokePaint = Paint().apply {
        color = 0x66FF0000
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }
    private val fillPaint = Paint().apply {
        color = 0x33FF0000
        style = Paint.Style.FILL
    }

    private var startX = 100f
    private var startY = 100f
    private var currentX = 400f
    private var currentY = 300f
    private var drawing = false

    // 固定比率モードフラグと比率 (幅/高さ)
    private var fixedRatioMode = false
    private var fixedRatio = 4f / 3f

    // 移動用フラグ
    private var isMoving = false
    private var lastTouchX = 0f
    private var lastTouchY = 0f

    // 外部から固定比率モードON/OFFを設定
    fun setFixedRatioMode(enabled: Boolean) {
        fixedRatioMode = enabled
        invalidate()
    }

    // 外部から比率設定（例: 4f/3f）
    fun setFixedRatio(ratio: Float) {
        fixedRatio = ratio
        if (fixedRatioMode) adjustRectToRatio()
        invalidate()
    }

    val cropRectF: RectF
        get() = RectF(
            Math.min(startX, currentX),
            Math.min(startY, currentY),
            Math.max(startX, currentX),
            Math.max(startY, currentY)
        )

    fun getCropRect(): Rect {
        val r = cropRectF
        return Rect(r.left.toInt(), r.top.toInt(), r.right.toInt(), r.bottom.toInt())
    }

    private fun adjustRectToRatio() {
        val left = Math.min(startX, currentX)
        val top = Math.min(startY, currentY)
        var width = Math.abs(currentX - startX)
        var height = Math.abs(currentY - startY)

        if (width / height > fixedRatio) {
            width = height * fixedRatio
        } else {
            height = width / fixedRatio
        }

        if (startX < currentX) {
            currentX = left + width
        } else {
            currentX = left - width
        }

        if (startY < currentY) {
            currentY = top + height
        } else {
            currentY = top - height
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(cropRectF, fillPaint)
        canvas.drawRect(cropRectF, strokePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y

                if (fixedRatioMode) {
                    // 枠内に触れたかどうかチェックして移動フラグセット
                    if (cropRectF.contains(event.x, event.y)) {
                        isMoving = true
                    } else {
                        // 新規枠作成
                        startX = event.x
                        startY = event.y
                        currentX = startX
                        currentY = startY
                        isMoving = false
                    }
                } else {
                    // 自由選択モードは新規範囲開始
                    startX = event.x
                    startY = event.y
                    currentX = startX
                    currentY = startY
                    isMoving = false
                    drawing = true
                }
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - lastTouchX
                val dy = event.y - lastTouchY
                lastTouchX = event.x
                lastTouchY = event.y

                if (fixedRatioMode && isMoving) {
                    // 移動のみ（範囲サイズは固定）
                    val newLeft = cropRectF.left + dx
                    val newTop = cropRectF.top + dy
                    val newRight = cropRectF.right + dx
                    val newBottom = cropRectF.bottom + dy

                    // ビュー内に収める制限
                    val viewWidth = width.toFloat()
                    val viewHeight = height.toFloat()

                    val offsetX = when {
                        newLeft < 0 -> -newLeft
                        newRight > viewWidth -> viewWidth - newRight
                        else -> 0f
                    }

                    val offsetY = when {
                        newTop < 0 -> -newTop
                        newBottom > viewHeight -> viewHeight - newBottom
                        else -> 0f
                    }

                    startX += dx + offsetX
                    startY += dy + offsetY
                    currentX += dx + offsetX
                    currentY += dy + offsetY
                } else if (!fixedRatioMode) {
                    currentX = event.x
                    currentY = event.y
                    drawing = true
                }
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (fixedRatioMode && !isMoving) {
                    // 新規固定比率枠作成の時は比率調整
                    currentX = event.x
                    currentY = event.y
                    adjustRectToRatio()
                }
                isMoving = false
                drawing = true
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
