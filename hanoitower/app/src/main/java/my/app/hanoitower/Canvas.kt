package my.app.hanoitower

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View

class CasualCanvas(context: Context): View(context)  {

    private var mainCanvas = Canvas()
    private var rods = initRods()
    private var transCoordinates = 0f to 0f
    private var circlesColors = mutableListOf(initPaints()[5], initPaints()[5], initPaints()[5])

    private fun initPaints(): List<Paint>{
        val black = Paint()
        black.color = Color.BLACK
        black.strokeWidth = 15f
        black.textSize = 168f
        black.textAlign = Paint.Align.CENTER

        val red = Paint()
        red.color = Color.RED
        red.strokeWidth = 15f

        val green = Paint()
        green.color = Color.GREEN
        green.strokeWidth = 15f

        val yellow = Paint()
        yellow.color = Color.YELLOW
        yellow.strokeWidth = 15f

        val gray = Paint()
        gray.color = Color.GRAY
        gray.strokeWidth = 50f

        val blue = Paint()
        blue.color = Color.BLUE
        blue.strokeWidth = 50f

        val white = Paint()
        white.color = Color.WHITE
        white.strokeWidth = 50f

        val pink = Paint()
        pink.color = Color.argb(255, 150, 0, 50)
        white.strokeWidth = 50f

        val cyan = Paint()
        cyan.color = Color.CYAN
        white.strokeWidth = 50f

        return listOf(black, gray, red, green, yellow, blue, white, pink, cyan)
        //              0     1     2     3       4      5     6      7     8
    }

    private fun initEnvironment(canvas: Canvas?){
        if(transCoordinates == 0f to 0f){
            transCoordinates = canvas!!.width/2f to canvas.height/2f
        }
        canvas!!.translate(canvas.width/2f, canvas.height/2f)

        canvas.drawLine(-300f,400f,-300f,-200f, initPaints()[1])
        canvas.drawLine(0f,400f,0f,-200f, initPaints()[1])
        canvas.drawLine(300f,400f,300f,-200f, initPaints()[1])

        canvas.drawCircle(-300f, -300f, 50f, circlesColors[0])
        canvas.drawCircle(0f, -300f, 50f, circlesColors[1])
        canvas.drawCircle(300f, -300f, 50f, circlesColors[2])

        canvas.drawLine(-canvas.width/2f,405f,canvas.height/2f,405f, initPaints()[0])
    }

    private fun initDiscs(): List<Disc>{
        var discs = listOf<Disc>()
        discs = discs.plus(Disc(1,0, 0f to 0f, initPaints()[2]))
        discs = discs.plus(Disc(2,0, 0f to 0f, initPaints()[3]))
        discs = discs.plus(Disc(3,0, 0f to 0f, initPaints()[4]))
        discs = discs.plus(Disc(4,0, 0f to 0f, initPaints()[5]))
        discs = discs.plus(Disc(5,0, 0f to 0f, initPaints()[7]))
        discs = discs.plus(Disc(6,0, 0f to 0f, initPaints()[8]))
        return discs
    }

    private fun initRods(): List<Rod>{
        val rod1 = Rod(-300f, 0, initDiscs())
        val rod2 = Rod(0f, 1, listOf())
        val rod3 = Rod(300f, 2, listOf())
        return listOf(rod1, rod2, rod3)
    }

    private fun updateCircles(pos: Float, rods: List<Rod>){
        if (isDiscChosen){
            for (i in 0..rods.size-1){
                circlesColors[i] = initPaints()[3]
                if (rods[i].position == pos){
                    circlesColors[i] = initPaints()[2]
                }
            }
        }else{
            for (i in 0..rods.size-1){
                circlesColors[i] = initPaints()[5]
            }
        }
        invalidate()
    }

    private var isDiscChosen = false
    private var discChosen : Rod? = null
    override fun onTouchEvent(event: MotionEvent?): Boolean{
        val x = event!!.x - transCoordinates.first
        val y = event.y - transCoordinates.second

        when(event.action){
            MotionEvent.ACTION_DOWN -> {
                if (!isDiscChosen){
                    for (i in 0..2){
                        if(x in rods[i].position-60..rods[i].position+60 && rods[i].discs.isNotEmpty()){
                            isDiscChosen = true
                            discChosen = rods[i]
                            updateCircles(rods[i].position, rods)
                        }
                    }
                }else{
                    for (i in 0..2){
                        if(x in rods[i].position-60..rods[i].position+60){
                            if(rods[i].discs.isEmpty()){
                                if(discChosen != rods[i]){
                                    rods[i].replaceDiscs(discChosen!!)
                                }
                                isDiscChosen = false
                                discChosen = null
                                updateCircles(rods[i].position, rods)
                            }else if(discChosen!!.discs[discChosen!!.discs.size-1].level > rods[i].discs[rods[i].discs.size-1].level){
                                if(discChosen != rods[i]){
                                    rods[i].replaceDiscs(discChosen!!)
                                }
                                isDiscChosen = false
                                discChosen = null
                                updateCircles(rods[i].position, rods)
                            }
                            if(discChosen == rods[i]){
                                isDiscChosen = false
                                discChosen = null
                                updateCircles(rods[i].position, rods)
                            }
                        }
                    }
                    checkForWin(rods, mainCanvas)
                }
                if(isOkAllowed){
                    if(x in okButton.cx - okButton.width..okButton.cx + okButton.width && y in okButton.cy - okButton.height..okButton.cy + okButton.height){
                        rods = initRods()
                        updateCanvas(mainCanvas, "update")
                        isOkAllowed = false
                    }
                }
            }
        }
        return true
    }
    private fun updateCanvas(canvas: Canvas?, param: String){
        canvas!!.drawRect(-canvas.width.toFloat(), -canvas.height.toFloat(), canvas.width.toFloat(), canvas.height.toFloat(), initPaints()[6])
        initEnvironment(canvas)
        for (j in 0..2){
            for (i in 0..rods[j].discs.size-1){
                val x1 = rods[j].position - rods[j].discs[i].width/2f
                val x2 = rods[j].position + rods[j].discs[i].width/2f
                val y1 = 400f - (i)*rods[j].discs[i].height
                val y2 = 400f - (i + 1)*rods[j].discs[i].height

                rods[j].discs[i].bodyLeft = x1
                rods[j].discs[i].bodyTop = y2
                canvas.drawRect(x1,y2,x2,y1,rods[j].discs[i].paint)
            }
        }
        if (isOkAllowed){
            canvas.drawRect(-420f, 220f, 420f, -220f, initPaints()[0])
            canvas.drawRect(-400f, 200f, 400f, -200f, initPaints()[6])
            canvas.drawText("You Win!", 0f, 60f, initPaints()[0])

            canvas.drawRect(-220f, 640f, 220f, 880f, initPaints()[0])
            canvas.drawRect(-200f, 660f, 200f, 860f, initPaints()[6])
            canvas.drawText("OK", 0f, 820f, initPaints()[0])
        }
        if (param == "update"){
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        mainCanvas = canvas!!
        updateCanvas(canvas, "first")
    }

    var okButton = Button(0f to 760f)
    var isOkAllowed = false
    fun checkForWin(rods: List<Rod>, canvas: Canvas?){
        for (i in 1..rods.size-1){
            if (rods[i].discs.size == 6){
                isOkAllowed = true
            }
        }
        invalidate()
    }
}
