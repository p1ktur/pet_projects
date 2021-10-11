package my.app.xyexperimental1.shapes

import android.graphics.Canvas
import android.graphics.Paint
import my.app.xyexperimental1.GraphView
import my.app.xyexperimental1.PAINT
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class Circle(var cx: Float, var cy: Float, var radius: Float, var paint: Paint, var isSelected: Boolean = false) {
    var centerPivotPoint = getCenterPoint()

    companion object {
        fun countRadius(fx: Float, fy: Float, sx: Float, sy: Float): Float {
            return sqrt((fx - sx).pow(2) + (fy - sy).pow(2))
        }

        fun checkIfRadiusAcceptable(graphView: GraphView, inputRadius: Float, actionPoints: Pair<Float, Float>): Float {
            var radius = inputRadius
            when {
                radius > actionPoints.first -> {
                    radius = checkIfRadiusAcceptable(graphView, actionPoints.first, actionPoints)
                }
                radius > actionPoints.second -> {
                    radius = checkIfRadiusAcceptable(graphView, actionPoints.second, actionPoints)
                }
                radius > graphView.width - actionPoints.first -> {
                    radius = checkIfRadiusAcceptable(graphView, graphView.width - actionPoints.first, actionPoints)
                }
                radius > graphView.height - actionPoints.second -> {
                    radius = checkIfRadiusAcceptable(graphView, graphView.height - actionPoints.second, actionPoints)
                }
            }

            return radius
        }
    }

    private fun getCenterPoint(): PivotPoint {
        return PivotPoint(cx, cy, PAINT.PIVOT_POINT_PAINT, this)
    }

    fun checkIfOwnsPoint(point: Pair<Float, Float>): Boolean = abs(point.first - cx) <= 12f && abs(point.second - cy) <= 12f

    fun checkIfEndsOwnPoint(point: Pair<Float, Float>): Boolean = abs(point.first - cx) <= 12f && abs(point.second - cy) <= 12f

    fun move(delta: Pair<Float, Float>) {
        cx += delta.first
        cy += delta.second
        centerPivotPoint = getCenterPoint()
    }

    fun update() {
        cx = centerPivotPoint.x
        cy = centerPivotPoint.y
    }
}

fun Canvas.drawCircle(circle: Circle) {
    val paint = if (circle.isSelected) PAINT.GREEN else circle.paint
    this.drawCircle(circle.cx, circle.cy, circle.radius, paint)
}