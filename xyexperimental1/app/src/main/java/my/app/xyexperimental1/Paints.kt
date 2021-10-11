package my.app.xyexperimental1

import android.graphics.Color
import android.graphics.Paint

object PAINT {
    var RED = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }

    val BLUE = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }

    val GREEN = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }

    val YELLOW = Paint().apply {
        color = Color.YELLOW
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 6f
    }

    val THIN_BLACK = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    val BLACK = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    val FAT_BLACK = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }

    val PIVOT_POINT_PAINT = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 6f
    }

    val TRIANGLE_BUILD_PAINT = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 6f
    }
}
