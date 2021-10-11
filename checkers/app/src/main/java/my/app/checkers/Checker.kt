package my.app.checkers

import android.graphics.Paint

class Checker(pos: Pair<Int, Int>, realPaint: Paint, corePaint: Paint, coordinates: Pair<Float, Float>?, radius: Float?, isQueen: Boolean, mustBeat: Boolean) {
    var corePaint = corePaint
    var paint = realPaint

    var pos = pos
    var isQueen = isQueen
    var mustBeat = mustBeat

    var x = coordinates?.first
    var y = coordinates?.second
    var radius = radius
}