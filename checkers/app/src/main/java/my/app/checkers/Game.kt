package my.app.checkers

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.system.exitProcess

class Game(context: Context): View(context) {

    //650 strings of code 10.03.2020 16:42

    private lateinit var gameBoard: Board //Board(8, listOf(listOf()))
    private var gameBoardInitialized = false

    var translatedCoordinates = 0f to 0f
    var firstInit = false
    var whoseTurn = true
    lateinit var mainCanvas: Canvas

    private fun initEnvironment(canvas: Canvas?){
        if (!firstInit){
            gameBoard.createBoard()
            gameBoard.createCheckers()
        }

        val boardFrameSize = 480f

        canvas!!.drawRect(-boardFrameSize + 440f, boardFrameSize + 440f, boardFrameSize + 440f, -boardFrameSize + 440f, brown)
        for (i in 0 until gameBoard.size) {
            for (j in 0 until gameBoard.size) {
                val boardUnitRadius = 880f / (2 * gameBoard.size)
                val boardUnitCenterX = 880f / gameBoard.size * (j + 1) - boardUnitRadius
                val boardUnitCenterY = 880f / gameBoard.size * (i + 1) - boardUnitRadius
                canvas.drawRect(
                    boardUnitCenterX - boardUnitRadius,
                    boardUnitCenterY - boardUnitRadius,
                    boardUnitCenterX + boardUnitRadius,
                    boardUnitCenterY + boardUnitRadius,
                    gameBoard.storage[i][j].paint
                )
                if (!firstInit && gameBoard.storage[i][j].checker != null) {
                    gameBoard.storage[i][j].checker!!.x = boardUnitCenterX
                    gameBoard.storage[i][j].checker!!.y = boardUnitCenterY
                    gameBoard.storage[i][j].checker!!.radius = boardUnitRadius
                    canvas.drawCircle(
                        boardUnitCenterX,
                        boardUnitCenterY,
                        boardUnitRadius - 5f * gameBoard.size / 8,
                        gameBoard.storage[i][j].checker!!.paint
                    )
                }
            }
        }
        firstInit = true
    }

    fun drawCheckers(canvas: Canvas?){
        for (i in 0 until gameBoard.size){
            for (j in 0 until gameBoard.size){
                if (gameBoard.storage[i][j].checker != null){
                    canvas!!.drawCircle(gameBoard.storage[i][j].checker!!.x!!, gameBoard.storage[i][j].checker!!.y!!, gameBoard.storage[i][j].checker!!.radius!!-5f*gameBoard.size/8, gameBoard.storage[i][j].checker!!.paint)
                    if (gameBoard.storage[i][j].checker!!.isQueen){
                        canvas.drawCircle(gameBoard.storage[i][j].checker!!.x!!, gameBoard.storage[i][j].checker!!.y!!, gameBoard.storage[i][j].checker!!.radius!!-5f*gameBoard.size/1.5f, yellow)
                    }
                }
            }
        }
    }

    var selectedChecker : Checker? = null
    var blockCycle = false
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val x = event!!.x - translatedCoordinates.first
        val y = event.y - translatedCoordinates.second

        when (event.action){
            MotionEvent.ACTION_DOWN -> {
                if (gameBoardInitialized) {
                    for (i in 0 until gameBoard.size) {
                        for (j in 0 until gameBoard.size) {
                            val tempChecker = gameBoard.storage[i][j].checker
                            if (tempChecker != null) {
                                if (x in tempChecker.x!!.minus(tempChecker.radius!!)..tempChecker.x!!.plus(tempChecker.radius!!)) {
                                    if (y in tempChecker.y!!.minus(tempChecker.radius!!)..tempChecker.y!!.plus(tempChecker.radius!!)) {
                                        if (tempChecker.paint != blue && selectedChecker == null) {
                                            gameBoard.showWhoHasToBeat()
                                            if ((gameBoard.isThereBeaters(tempChecker) && tempChecker.mustBeat) || !gameBoard.isThereBeaters(tempChecker)) {
                                                if (whoseTurn && tempChecker.corePaint == white) {
                                                    gameBoard.storage[i][j].checker!!.paint = blue
                                                    gameBoard.storage[i][j].checker!!.isQueen = gameBoard.checkIfQueen(gameBoard.storage[i][j].checker!!)
                                                    gameBoard.showTrails(gameBoard.storage[i][j].checker!!, "show")
                                                    selectedChecker = gameBoard.storage[i][j].checker
                                                } else if (!whoseTurn && tempChecker.corePaint == gray && selectedChecker == null) {
                                                    gameBoard.storage[i][j].checker!!.paint = blue
                                                    gameBoard.storage[i][j].checker!!.isQueen = gameBoard.checkIfQueen(gameBoard.storage[i][j].checker!!)
                                                    gameBoard.showTrails(gameBoard.storage[i][j].checker!!, "show")
                                                    selectedChecker = gameBoard.storage[i][j].checker
                                                }
                                            }
                                        } else if (tempChecker.paint == blue && selectedChecker == tempChecker) {
                                            gameBoard.storage[i][j].checker!!.paint = gameBoard.storage[i][j].checker!!.corePaint
                                            gameBoard.showTrails(gameBoard.storage[i][j].checker!!, "hide")
                                            selectedChecker = null
                                        }
                                        blockCycle = true
                                        invalidate()
                                        break
                                    }
                                }
                            } else if (selectedChecker != null && gameBoard.storage[i][j].paint == green) {
                                val passingRight = gameBoard.storage[i][j].isContainingClick(y to x, selectedChecker!!.radius!!, gameBoard.size)
                                if (passingRight) {
                                    selectedChecker!!.paint = selectedChecker!!.corePaint
                                    gameBoard.showTrails(selectedChecker!!, "hide")
                                    gameBoard.moveChecker(selectedChecker!!.pos, i to j)
                                    gameBoard.storage[i][j].checker!!.isQueen = gameBoard.checkIfQueen(gameBoard.storage[i][j].checker!!)
                                    if (selectedChecker!!.mustBeat) {
                                        gameBoard.clearBeaters()
                                        gameBoard.showWhoHasToBeat()
                                        if (!selectedChecker!!.mustBeat) {
                                            gameBoard.clearBeaters()
                                            selectedChecker = null
                                            whoseTurn = !whoseTurn
                                        } else {
                                            for (a in 0 until gameBoard.size) {
                                                for (b in 0 until gameBoard.size) {
                                                    if (a to b == selectedChecker!!.pos) {
                                                        gameBoard.storage[a][b].checker!!.paint = blue
                                                        gameBoard.storage[a][b].checker!!.isQueen = gameBoard.checkIfQueen(gameBoard.storage[i][j].checker!!)
                                                        gameBoard.showTrails(gameBoard.storage[a][b].checker!!, "show")
                                                    }
                                                }
                                            }

                                        }
                                    } else {
                                        gameBoard.clearBeaters()
                                        selectedChecker = null
                                        whoseTurn = !whoseTurn
                                    }
                                    blockCycle = true
                                    invalidate()
                                    break
                                }
                            }
                        }
                        if (blockCycle) {
                            blockCycle = false
                            break
                        }
                    }
                }else{
                    when (currentMenu){
                        "mainmenu" ->{
                            if (x in -310f..310f && y in -410f..-250f){     //if in start button
                                sizeMenuDrawn = false
                                invalidate()
                            }
                            if (x in -310f..310f && y in 250f..440f){     //if in quit button
                                exitProcess(1)
                            }
                        }
                        "sizemenu" ->{
                            if (x in -310f..310f && y in -410f..-250f){     //6x6
                                gameBoard = Board(6, listOf(listOf()))
                                gameBoardInitialized = true
                                invalidate()
                            }
                            if (x in -310f..310f && y in -150f..10f){       //8x8
                                gameBoard = Board(8, listOf(listOf()))
                                gameBoardInitialized = true
                                invalidate()
                            }
                            if (x in -310f..310f && y in -110f..270f){      //10x10
                                gameBoard = Board(10, listOf(listOf()))
                                gameBoardInitialized = true
                                invalidate()
                            }
                            if (x in 220f..340f && y in 500f..600f){
                                menuDrawn = false
                                invalidate()
                            }
                        }
                        "winmenu" ->{
                            if (x in -310f..310f && y in -410f..-250f){     //if in return to main menu button
                                menuDrawn = false
                                invalidate()
                            }
                        }
                    }
                }
            }
        }
        blockCycle = false
        return true
    }

    private var menuDrawn = false
    fun drawMenu(canvas: Canvas){
        canvas.translate(canvas.width/2f, canvas.height/2f)
        translatedCoordinates = canvas.width/2f to canvas.height/2f

        canvas.drawColor(black.color)
        canvas.drawRect(-420f, -620f, 420f, 620f, brown)
        canvas.drawRect(-400f, -600f, 400f, 600f, white)
        canvas.drawText("The Chess Game", 0f, -485f, black)

        canvas.drawRect(-310f, -410f, 310f, -250f, black) // start button
        canvas.drawRect(-300f, -400f, 300f, -260f, white)
        canvas.drawText("Start Game", 0f, -305f, black)

        canvas.drawRect(-310f, 250f, 310f, 410f, black)
        canvas.drawRect(-300f, 260f, 300f, 400f, white)
        canvas.drawText("Quit", 0f, 355f, black)

        canvas.drawText("v. beta 1.0", canvas.width/2f - 150f, canvas.height/2f - 40f, white)

        menuDrawn = true
        currentMenu = "mainmenu"
    }

    private var sizeMenuDrawn = true
    fun drawSizeMenu(canvas: Canvas){
        canvas.translate(canvas.width/2f, canvas.height/2f)
        translatedCoordinates = canvas.width/2f to canvas.height/2f

        canvas.drawColor(black.color)
        canvas.drawRect(-420f, -620f, 420f, 620f, brown)
        canvas.drawRect(-400f, -600f, 400f, 600f, white)
        canvas.drawText("Choose Board Size", 0f, -485f, black)

        canvas.drawRect(-310f, -410f, 310f, -250f, black)
        canvas.drawRect(-300f, -400f, 300f, -260f, white)
        canvas.drawText("6x6", 0f, -305f, black)

        canvas.drawRect(-310f, -150f, 310f, 10f, black)
        canvas.drawRect(-300f, -140f, 300f, 0f, white)
        canvas.drawText("8x8", 0f, -45f, black)

        canvas.drawRect(-310f, 110f, 310f, 270f, black)
        canvas.drawRect(-300f, 120f, 300f, 260f, white)
        canvas.drawText("10x10", 0f, 215f, black)

        canvas.drawText("Back", 280f, 550f, black)

        canvas.drawText("v. beta 1.0", canvas.width/2f - 150f, canvas.height/2f - 40f, white)

        sizeMenuDrawn = true
        currentMenu = "sizemenu"
    }

    private var winMenuDrawn = false
    fun drawWinMenu(canvas: Canvas, paint: Paint){
        canvas.translate(440f, 440f)
        translatedCoordinates = canvas.width/2f to canvas.height/2f

        canvas.drawRect(-420f, -620f, 420f, 620f, brown)
        canvas.drawRect(-400f, -600f, 400f, 600f, white)
        if (paint == white){
            canvas.drawText("White Won!", 0f, -485f, black)
        }else if (paint == gray){
            canvas.drawText("Gray Won!", 0f, -485f, black)
        }

        canvas.drawRect(-310f, -410f, 310f, -250f, black)
        canvas.drawRect(-300f, -400f, 300f, -260f, white)
        canvas.drawText("Return To Main Menu", 0f, -305f, smallTextBlack)

        winMenuDrawn = true
        currentMenu = "winmenu"
    }

    var currentMenu = "mainmenu"
    override fun onDraw(canvas: Canvas?) {
        mainCanvas = canvas!!

        if (!menuDrawn){
            drawMenu(canvas)
        }
        if (!sizeMenuDrawn){
            drawSizeMenu(canvas)
        }

        if (gameBoardInitialized){
            canvas.translate(canvas.width/2f-440f, canvas.height/2f-440f)
            translatedCoordinates = canvas.width/2f-440f to canvas.height/2f-440f

            initEnvironment(canvas)
            drawCheckers(canvas)
        }

        if (gameBoardInitialized && gameBoard.checkForWin().first){
            gameBoardInitialized = false
            drawWinMenu(canvas, white)
        }else if (gameBoardInitialized && gameBoard.checkForWin().second){
            gameBoardInitialized = false
            drawWinMenu(canvas, gray)
        }
    }
}