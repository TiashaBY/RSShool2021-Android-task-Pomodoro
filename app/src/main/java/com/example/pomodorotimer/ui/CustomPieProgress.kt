package com.example.pomodorotimer.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.ProgressBar
import com.example.pomodorotimer.R

open class CustomPieProgress @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
                                                       defaultAttrs: Int = 0)
    : ProgressBar(context, attrs, defaultAttrs) {

    private var progr: Float = 0F
    private var minProgress = 0.005F
    private var painter = Paint()

    init {
        painter = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            strokeWidth = 30f
        }
        context.theme.obtainStyledAttributes(attrs, R.styleable.CustomPieProgress, 0, 0).apply {
            try {
                painter.color = getColor(R.styleable.CustomPieProgress_pieColour, Color.BLACK)
            } finally {
                //make the data associated with typed array ready for garbage collection
                recycle()
            }
        }
    }

    fun updateProgress(progress: Float) {
        //this is made to show the smallest sector of progress when timer is started instead of empty circle
        if (progress > 0) {
            this.progr = 360 * if (progress < minProgress)  minProgress else progress
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        var x = 0F
        var y = 0F
        if (height - width > 0) {
            y = ((height - width) / 2).toFloat()
        } else {
            x = ((width - height) / 2).toFloat()
        }

        val oval = RectF(x, y, width - x, height - y)
        canvas.drawArc(oval, -90F, progr, true, painter)
    }
}
