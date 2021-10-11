package my.app.xyexperimental1.shapes

import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import my.app.xyexperimental1.PAINT
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class Line(var startX: Float, var startY: Float, var stopX: Float, var stopY: Float, var paint: Paint, var parent: Any?, var isSelected: Boolean = false) {
    var pivotPointsList = getPivotPoints()

    override fun toString(): String {
        return "Line: start: ($startX ; $startY) stop: ($stopX ; $stopY) $isSelected"
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is Line -> {
                startX == other.startX && startY == other.startY && stopX == other.stopX && stopY == other.stopY && paint == other.paint && isSelected == other.isSelected
            }
            else -> super.equals(other)
        }
    }

    private fun getPivotPoints(): MutableList<PivotPoint> {
        return mutableListOf(PivotPoint(startX, startY, PAINT.PIVOT_POINT_PAINT, this), PivotPoint(stopX, stopY, PAINT.PIVOT_POINT_PAINT, this))
    }

    fun checkIfOwnsPoint(point: Pair<Float, Float>): Line? {
        val tan = (stopY - startY)/(stopX - startX)
        val possibleY = tan*(point.first - startX) + startY
        val approximation = if (!tan.equals(Float.POSITIVE_INFINITY) && !tan.equals(Float.NEGATIVE_INFINITY)) 12f*2f.pow(abs(tan)/12) else 12f

        return when {
            stopX > startX && point.first in startX-approximation..stopX+approximation -> {
                if (point.second in possibleY-approximation..possibleY+approximation) this else null
            }
            startX > stopX && point.first in stopX-approximation..startX+approximation -> {
                if (point.second in possibleY-approximation..possibleY+approximation) this else null
            }
            else -> if (point.first in stopX-approximation..startX+approximation) {
                if (stopY >= startY) {
                    if (point.second in startY-approximation..stopY+approximation) this else null
                } else {
                    if (point.second in stopY-approximation..startY+approximation) this else null
                }
            } else null
        }
    }

    fun checkIfEndsOwnPoint(point: Pair<Float, Float>): Int? {
        val approximation = 12f

        return when {
            pivotPointsList[0].checkIfOwnsPoint(point) -> 0
            pivotPointsList[1].checkIfOwnsPoint(point) -> 1
            else -> null
        }
    }

    fun checkIfPointIsInRange(point: Pair<Float, Float>): Boolean {
        val approximation = 12f
        return if (stopX > startX) {
            if (stopY > startY) {
                point.first in startX-approximation..stopX+approximation && point.second in startY-approximation..stopY+approximation
            } else {
                point.first in startX-approximation..stopX+approximation && point.second in stopY-approximation..startY+approximation
            }
        } else {
            if (stopY > startY) {
                point.first in stopX-approximation..startX+approximation && point.second in startY-approximation..stopY+approximation
            } else {
                point.first in stopX-approximation..startX+approximation && point.second in stopY-approximation..startY+approximation
            }
        }
    }

    fun checkWhereOwnsPoint(point: Pair<Float, Float>): Pair<Float, Float> {
        val tan = (stopY - startY)/(stopX - startX)
        val possibleY = tan*(point.first - startX) + startY

        return point.first to possibleY
    }

    fun getDistanceToPoint(point: Pair<Float, Float>): Float {
        //Ax + By + C = 0 | M(Mx;My)
        val tan = (stopY - startY)/(stopX - startX)
        val distance = abs(-tan*point.first + point.second + tan*startX - startY)/ sqrt(tan.pow(2) + 1)

        return distance
    }

    fun getEdgePoint(point: Pair<Float, Float>): Pair<Float, Float> {
        return if (stopX > startX) {
            if (point.first > stopX) stopX to stopY else startX to startY
        } else {
            if (point.first > startX) startX to startY else stopX to stopY
        }
    }

    fun move(delta: Pair<Float, Float>) {
        startX += delta.first
        startY += delta.second
        stopX += delta.first
        stopY += delta.second
        pivotPointsList = getPivotPoints()
    }

    fun update() {
        startX = pivotPointsList[0].x
        startY = pivotPointsList[0].y
        stopX = pivotPointsList[1].x
        stopY = pivotPointsList[1].y
    }

    fun splitLine(point: Pair<Float, Float>): List<Line> {
        return listOf(Line(startX, startY, point.first, point.second, paint, parent), Line(point.first, point.second, stopX, stopY, paint, parent))
    }
}

fun Canvas.drawLine(line: Line, paint: Paint) {
    this.drawLine(line.startX, line.startY, line.stopX, line.stopY, paint)
}

fun Canvas.drawLine(line: Line) {
    val paint = if (line.isSelected) PAINT.GREEN else line.paint
    this.drawLine(line.startX, line.startY, line.stopX, line.stopY, paint)
}