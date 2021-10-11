package my.app.xyexperimental1.shapes

import android.graphics.Canvas
import android.graphics.Paint
import my.app.xyexperimental1.PAINT

class Rectangle(var fx: Float, var fy: Float, var sx: Float, var sy: Float, var paint: Paint, var isSelected: Boolean = false) {
    var pivotPointsList = getPivotPoints()
    var drawableLinesList = getDrawableLines()

    private fun getPivotPoints(): MutableList<PivotPoint> {
        return mutableListOf(
            PivotPoint(fx, fy, PAINT.PIVOT_POINT_PAINT, this),
            PivotPoint(fx, sy, PAINT.PIVOT_POINT_PAINT, this),
            PivotPoint(sx, sy, PAINT.PIVOT_POINT_PAINT, this),
            PivotPoint(sx, fy, PAINT.PIVOT_POINT_PAINT, this)
        )
    }

    private fun getDrawableLines(): List<Line> {
        return listOf(
            Line(fx, fy, fx, sy, paint, this),
            Line(fx, sy, sx, sy, paint, this),
            Line(sx, sy, sx, fy, paint, this),
            Line(sx, fy, fx, fy, paint, this)
        )
    }

    fun checkIfOwnsPoint(point: Pair<Float, Float>): Boolean{
        getDrawableLines().forEach { if (it.checkIfOwnsPoint(point) != null) return true }
        return false
    }

    fun checkIfEndsOwnPoint(point: Pair<Float, Float>): Int? {
        val approximation = 12f

        return when {
            pivotPointsList[0].checkIfOwnsPoint(point) -> 0
            pivotPointsList[1].checkIfOwnsPoint(point) -> 1
            pivotPointsList[2].checkIfOwnsPoint(point) -> 2
            pivotPointsList[3].checkIfOwnsPoint(point) -> 3
            else -> null
        }
    }

    fun move(delta: Pair<Float, Float>) {
        fx += delta.first
        fy += delta.second
        sx += delta.first
        sy += delta.second
        pivotPointsList = getPivotPoints()
        drawableLinesList = getDrawableLines()
    }

    fun update() {
        fx = pivotPointsList[0].x
        fy = pivotPointsList[0].y
        sx = pivotPointsList[2].x
        sy = pivotPointsList[2].y
        drawableLinesList = getDrawableLines()
    }
}

fun Canvas.drawRect(rect: Rectangle) {
    val paint = if (rect.isSelected) PAINT.GREEN else rect.paint
    rect.drawableLinesList.forEach {
        this.drawLine(it, paint)
    }
}