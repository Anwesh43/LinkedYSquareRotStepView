package com.anwesh.uiprojects.ysquarerotstepview

/**
 * Created by anweshmishra on 07/12/18.
 */

import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.app.Activity
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color

val nodes : Int = 5
val lines : Int = 3
val color : Int = Color.parseColor("#0D47A1")
val scGap : Float = 0.05f
val scDiv : Double = 0.51
val strokeFactor : Int = 90
val sizeFactor : Float = 2.4f

fun Int.getInverse() : Float = 1f / this

fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.getInverse(), Math.max(0f, this - i * n.getInverse())) * n

fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()

fun Float.mirrorValue(a : Int, b : Int) : Float = (1 - scaleFactor()) * a.getInverse() + scaleFactor() * b.getInverse()

fun Float.updateScale(dir : Float, a : Int, b : Int) : Float = dir * scGap * mirrorValue(a, b)

fun Canvas.drawYSRSNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = Math.min(w, h) / nodes
    val size : Float = gap / sizeFactor
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    paint.strokeCap = Paint.Cap.ROUND
    paint.color = color
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    val xGap : Float = (w - paint.strokeWidth) / (lines - 1)
    save()
    translate(gap * (i + 1), h/2)
    rotate(90f * sc2)
    for (j in 0..lines) {
        val sc : Float = sc1.divideScale(j, lines)
        val sf : Float = 1f - 2 * (j % 2)
        save()
        translate(xGap * j + paint.strokeWidth/2, 0f)
        drawLine(0f, 0f, 0f, size * sf * sc, paint)
        restore()
    }
    restore()
}

class YSquareRotStepView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scale.updateScale(dir, lines, 1)
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, private var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class YSRSNode(var i : Int, val state : State = State()) {

        private var next : YSRSNode? = null
        private var prev : YSRSNode? = null

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = YSRSNode(i + 1)
                next?.prev = this
            }
        }

        init {
            addNeighbor()
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawYSRSNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : YSRSNode {
            var curr : YSRSNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class YSquareRotStep(var i : Int) {
        private val root : YSRSNode = YSRSNode(0)
        private var curr : YSRSNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : YSquareRotStepView) {

        private val animator : Animator = Animator(view)
        private var ysrs : YSquareRotStep = YSquareRotStep(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            ysrs.draw(canvas, paint)
            animator.animate {
                ysrs.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            ysrs.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity: Activity) : YSquareRotStepView {
            val view : YSquareRotStepView = YSquareRotStepView(activity)
            activity.setContentView(view)
            return view 
        }
    }
}