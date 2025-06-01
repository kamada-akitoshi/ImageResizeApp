package com.example.imageresize

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
        color = 0x66FF0000 // 半透明赤の枠線
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    private val fillPaint = Paint().apply {
        color = 0x33FF0000 // 半透明赤の塗りつぶし
        style = Paint.Style.FILL
    }

    private var startX = 0f
    private var startY = 0f
    private var currentX = 0f
    private var currentY = 0f
    private var drawing = false

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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (drawing) {
            canvas.drawRect(cropRectF, fillPaint)
            canvas.drawRect(cropRectF, strokePaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                currentX = startX
                currentY = startY
                drawing = true
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                currentX = event.x
                currentY = event.y
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                currentX = event.x
                currentY = event.y
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
