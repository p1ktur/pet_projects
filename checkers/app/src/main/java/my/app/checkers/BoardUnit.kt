package my.app.checkers

import android.graphics.Paint

class BoardUnit(pos: Pair<Int, Int>, checker: Checker?, realPaint: Paint, corePaint: Paint) {
    val corePaint = corePaint

    val pos = pos
    val x = pos.first
    val y = pos.second

    var checker = checker
    var paint = realPaint

    fun isContainingClick(yx: Pair<Float, Float>, radius: Float, size: Int): Boolean{
        val r = radius
        val x1 = yx.second
        val y1 = yx.first
        val x2 = Board(0, listOf()).posToCoordinates(this.y to this.x, size).second.second
        val y2 = Board(0, listOf()).posToCoordinates(this.y to this.x, size).second.first

        return x1 in x2-r..x2+r && y1 in y2-r..y2+r
    }
}