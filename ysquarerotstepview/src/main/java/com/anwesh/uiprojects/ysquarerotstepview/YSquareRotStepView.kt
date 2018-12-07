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