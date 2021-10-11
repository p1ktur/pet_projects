package my.app.hanoitower

import android.graphics.Paint

class Disc(level: Int, rod: Int, body: Pair<Float, Float>, paint: Paint) {
    val level = level
    val height = 50f
    val width = (10-level)*30f
    val paint = paint

    var rod = rod
    var bodyLeft = body.first
    var bodyTop = body.second


}