package my.app.checkers

import android.graphics.Color
import android.graphics.Paint

fun initColors(){
    white.color = Color.WHITE
    white.textSize = 40f
    white.strokeWidth = 6f
    white.textAlign = Paint.Align.CENTER

    black.color = Color.BLACK
    black.textSize = 72f
    black.textAlign = Paint.Align.CENTER

    smallTextBlack.color = Color.BLACK
    smallTextBlack.textSize = 48f
    smallTextBlack.textAlign = Paint.Align.CENTER

    gray.color = Color.GRAY

    brown.color = Color.rgb(139, 69, 19)

    blue.color = Color.BLUE

    red.color = Color.RED

    green.color = Color.GREEN

    pinkyGray.color = Color.rgb(180, 100, 128)

    pink.color = Color.rgb(255, 105, 180)

    yellow.color = Color.YELLOW
}

val white = Paint()
val black = Paint()
val smallTextBlack = Paint()
val gray = Paint()
val brown = Paint()
val blue = Paint()
val red = Paint()
val green = Paint()
val pinkyGray = Paint()
val pink = Paint()
val yellow = Paint()