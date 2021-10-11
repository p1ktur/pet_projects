package my.app.xyexperimental1.shapes

import android.graphics.Canvas
import android.graphics.Paint
import my.app.xyexperimental1.PAINT
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class PivotPoint(var x: Float, var y: Float, var paint: Paint, var parent: Any?, var isSelected: Boolean = false) {
    companion object {
        fun toCoordinates(list: MutableList<PivotPoint>): List<Pair<Float, Float>> {
            return list.map { it.x to it.y }
        }
    }

    override fun toString(): String {
        return "PP: ($x ; $y) $isSelected"
    }

    fun move(delta: Pair<Float, Float>) {
        x += delta.first
        y += delta.second
    }

    fun checkIfOwnsPoint(point: Pair<Float, Float>): Boolean = abs(point.first - x) <= 12f && abs(point.second - y) <= 12f
}

fun Canvas.drawPivotPoints(list: List<PivotPoint>) {
    list.forEach {
        this.drawCircle(it.x, it.y, it.paint.strokeWidth, it.paint)
    }
}

fun Canvas.drawPivotPoints(point: PivotPoint) {
    val paint = if (point.isSelected) PAINT.YELLOW else point.paint
    this.drawCircle(point.x, point.y, point.paint.strokeWidth, paint)
    this.drawCircle(point.x, point.y, point.paint.strokeWidth, PAINT.THIN_BLACK)
}