package my.app.xyexperimental1.shapes

import android.graphics.Canvas
import android.graphics.Paint
import my.app.xyexperimental1.PAINT

class Polygon(var vertices: MutableList<PivotPoint>, var paint: Paint, var isSelected: Boolean = false) {
    var drawableLinesList = getDrawableLines()

    companion object {
        fun transformToPolygon(shape: Any): Polygon? {
            return when (shape) {
                is Rectangle -> {
                    val vertices = shape.pivotPointsList.toMutableList()
                    val polygon = Polygon(vertices, shape.paint, shape.isSelected)
                    vertices.forEach { it.parent = polygon }
                    polygon.also { it.vertices = vertices}
                }
                is Triangle -> {
                    val vertices = shape.pivotPointsList.toMutableList()
                    val polygon = Polygon(vertices, shape.paint, shape.isSelected)
                    vertices.forEach { it.parent = polygon }
                    polygon.also { it.vertices = vertices}
                }
                else -> null
            }
        }
    }

    private fun getDrawableLines(): List<Line> {
        //TODO make algorithm for getting lines
        val linesList = mutableListOf<Line>()
        for (i in 0..vertices.size-2) {
            linesList.add(Line(vertices[i].x, vertices[i].y, vertices[i+1].x, vertices[i+1].y, PAINT.FAT_BLACK, this))
        }
        linesList.add(Line(vertices[vertices.size-1].x, vertices[vertices.size-1].y, vertices[0].x, vertices[0].y, PAINT.FAT_BLACK, this))
        return linesList
    }

    fun checkIfOwnsPoint(point: Pair<Float, Float>): Boolean{
        getDrawableLines().forEach { if (it.checkIfOwnsPoint(point) != null) return true }
        return false
    }

    fun checkIfEndsOwnPoint(point: Pair<Float, Float>): Int? {
        val approximation = 12f

        for (i in 0 until vertices.size) {
            if (vertices[i].checkIfOwnsPoint(point)) return i
        }

        return null
    }


    fun move(delta: Pair<Float, Float>) {
        vertices.forEach {
            it.x += delta.first
            it.y += delta.second
        }
        drawableLinesList = getDrawableLines()
    }

    fun update() {
        drawableLinesList = getDrawableLines()
    }
}

fun Canvas.drawPolygon(polygon: Polygon) {
    val paint = if (polygon.isSelected) PAINT.GREEN else polygon.paint
    polygon.drawableLinesList.forEach {
        this.drawLine(it, paint)
    }
}