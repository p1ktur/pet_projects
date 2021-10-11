package my.app.checkers

import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import java.lang.Math.abs

class Board(size: Int, storage: List<List<BoardUnit>>) {
    //size will be 6,8,10 | small, classic, big
    val size = size
    var storage = storage

    fun createBoard(){
        val size = this.size
        var storage = this.storage.minus(listOf(listOf()))
        var semiStorage : List<BoardUnit>

        for (i in 0 until size){
            semiStorage = listOf()
            for (j in 0 until size){
                semiStorage = semiStorage.plusElement(BoardUnit(i to j, null, determineUnitColor(j + i%2), determineUnitColor(j + i%2)))
            }
            storage = storage.plusElement(semiStorage)
        }
        this.storage = storage.toList()
    }

    fun determineUnitColor(input: Int): Paint {
        // j + i%2
        if (input%2 == 0){
            return white
        }else {
            return black
        }
    }

    fun createCheckers(){
        var storage = this.storage
        for (i in 0 until this.size){
            for (j in 0 until this.size){
                if (storage[i][j].paint == black) {
                    if (i < this.size / 2 - 1) {
                        storage[i][j].checker = Checker(i to j, gray, gray, null, null, false, false)
                    } else if (i > this.size / 2) {
                        storage[i][j].checker = Checker(i to j, white, white, null, null, false, false)
                    }
                }
            }
        }
        this.storage = storage
    }

    fun showTrails(checker: Checker, option: String){
        val size = this.size
        val storage = this.storage
        var mustBeat = false

        var optionalColor = blue
        if (option == "show"){
            optionalColor = green
        }else if (option == "hide"){
            optionalColor = black
        }
        if (!checker.isQueen){
            for (i in 0 until size){
                for (j in 0 until size){
                    if (i to j == checker.pos){
                        when (j){
                            0 -> {
                                if (i-2 in 0 until size && j+2 in 0 until size && storage[i-1][j+1].checker != null  && !checkForChecker(storage[i][j]).contains(i-2 to j+2) && storage[i-1][j+1].checker!!.corePaint != checker.corePaint){
                                    if (storage[i-2][j+2].checker == null) {
                                        storage[i-2][j+2].paint = optionalColor
                                        mustBeat = true
                                    }
                                }
                                if (i+2 in 0 until size && j+2 in 0 until size && storage[i+1][j+1].checker != null  && !checkForChecker(storage[i][j]).contains(i+2 to j+2) && storage[i+1][j+1].checker!!.corePaint != checker.corePaint){
                                    if (storage[i+2][j+2].checker == null) {
                                        storage[i+2][j+2].paint = optionalColor
                                        mustBeat = true
                                    }
                                }
                            }
                            in 1..size-2 -> {
                                if (i-2 in 0 until size && j+2 in 0 until size && storage[i-1][j+1].checker != null  && !checkForChecker(storage[i][j]).contains(i-2 to j+2) && storage[i-1][j+1].checker!!.corePaint != checker.corePaint){
                                    if (storage[i-2][j+2].checker == null) {
                                        storage[i-2][j+2].paint = optionalColor
                                        mustBeat = true
                                    }
                                }
                                if (i-2 in 0 until size && j-2 in 0 until size && storage[i-1][j-1].checker != null  && !checkForChecker(storage[i][j]).contains(i-2 to j-2) && storage[i-1][j-1].checker!!.corePaint != checker.corePaint){
                                    if (storage[i-2][j-2].checker == null) {
                                        storage[i-2][j-2].paint = optionalColor
                                        mustBeat = true
                                    }
                                }
                                if (i+2 in 0 until size && j+2 in 0 until size && storage[i+1][j+1].checker != null  && !checkForChecker(storage[i][j]).contains(i+2 to j+2) && storage[i+1][j+1].checker!!.corePaint != checker.corePaint){
                                    if (storage[i+2][j+2].checker == null) {
                                        storage[i+2][j+2].paint = optionalColor
                                        mustBeat = true
                                    }
                                }
                                if (i+2 in 0 until size && j-2 in 0 until size && storage[i+1][j-1].checker != null  && !checkForChecker(storage[i][j]).contains(i+2 to j-2) && storage[i+1][j-1].checker!!.corePaint != checker.corePaint){
                                    if (storage[i+2][j-2].checker == null) {
                                        storage[i+2][j-2].paint = optionalColor
                                        mustBeat = true
                                    }
                                }
                            }
                            size-1 -> {
                                if (i-2 in 0 until size && j-2 in 0 until size && storage[i-1][j-1].checker != null  && !checkForChecker(storage[i][j]).contains(i-2 to j-2) && storage[i-1][j-1].checker!!.corePaint != checker.corePaint){
                                    if (storage[i-2][j-2].checker == null) {
                                        storage[i-2][j-2].paint = optionalColor
                                    }
                                }
                                if (i+2 in 0 until size && j-2 in 0 until size && storage[i+1][j-1].checker != null && !checkForChecker(storage[i][j]).contains(i+2 to j-2) && storage[i+1][j-1].checker!!.corePaint != checker.corePaint) {
                                    if (storage[i+2][j-2].checker == null) {
                                        storage[i+2][j-2].paint = optionalColor
                                    }
                                }
                            }
                        }
                    }
                }
            }
            for (i in 0 until size){
                for (j in 0 until size){
                    if (i to j == checker.pos && !mustBeat){
                        when (j){
                            0 -> {
                                if (i != 0 && storage[i-1][j+1].checker == null){
                                    if(checker.corePaint == white){
                                        storage[i-1][j+1].paint = optionalColor
                                    }
                                }
                                if (i != size-1 && storage[i+1][j+1].checker == null){
                                    if(checker.corePaint == gray){
                                        storage[i+1][j+1].paint = optionalColor
                                    }
                                }
                            }
                            in 1..size-2 -> {
                                if (i != 0 && storage[i-1][j+1].checker == null){
                                    if(checker.corePaint == white){
                                        storage[i-1][j+1].paint = optionalColor
                                    }
                                }
                                if (i != 0 && storage[i-1][j-1].checker == null){
                                    if(checker.corePaint == white){
                                        storage[i-1][j-1].paint = optionalColor
                                    }
                                }
                                if (i != size-1 && storage[i+1][j+1].checker == null){
                                    if(checker.corePaint == gray){
                                        storage[i+1][j+1].paint = optionalColor
                                    }
                                }
                                if (i != size-1 && storage[i+1][j-1].checker == null){
                                    if(checker.corePaint == gray){
                                        storage[i+1][j-1].paint = optionalColor
                                    }
                                }
                            }
                            size-1 -> {
                                if (i != 0 && storage[i-1][j-1].checker == null){
                                    if(checker.corePaint == white){
                                        storage[i-1][j-1].paint = optionalColor
                                    }
                                }
                                if (i != size-1 && storage[i+1][j-1].checker == null) {
                                    if (checker.corePaint == gray) {
                                        storage[i+1][j-1].paint = optionalColor
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            checkForLineOfCheckers(storage[checker.pos.first][checker.pos.second]).forEach {
                storage[it.first][it.second].paint = optionalColor
            }
        }
        this.storage = storage
    }

    fun checkIfQueen(checker: Checker): Boolean{
        val size = this.size
        val storage = this.storage

        return if (checker.corePaint == white && checker.pos.first == 0){
            true
        }else if (checker.corePaint == gray && checker.pos.first == size-1){
            true
        }else checker.isQueen
    }

    fun checkForLineOfCheckers(unit: BoardUnit): List<Pair<Int, Int>>{
        val size = this.size
        val storage = this.storage

        var toBeReturnedList = listOf<Pair<Int, Int>>()
        var availableOptions = HashMap<String, Int>()

        if (unit.checker != null && unit.checker!!.mustBeat) {
            for (i in 0 until size) {
                for (j in 0 until size) {
                    if (i to j == unit.pos) {
                        var a = 0
                        while (a < size) {
                            if (i - a >= 0 && j - a >= 0) {
                                if (storage[i - a][j - a].checker != null) {
                                    if (i - a - 1 >= 0 && j - a - 1 >= 0) {
                                        if (storage[i - a - 1][j - a - 1].checker == null && storage[i - a][j - a].checker!!.corePaint != unit.checker!!.corePaint) {
                                            availableOptions.put("--", a)
                                        } else if ((storage[i - a - 1][j - a - 1].checker != null || storage[i - a][j - a].checker!!.corePaint == unit.checker!!.corePaint) && a != 0) {
                                            break
                                        }
                                    }
                                }
                            }
                            a++
                        }
                        a = 0
                        while (a < size) {
                            if (i + a < size && j - a >= 0) {
                                Log.d("cc", "1")
                                if (storage[i + a][j - a].checker != null) {
                                    Log.d("cc", "2")
                                    if (i + a + 1 < size && j - a - 1 >= 0) {
                                        Log.d("cc", "3")
                                        if (storage[i + a + 1][j - a - 1].checker == null && storage[i + a][j - a].checker!!.corePaint != unit.checker!!.corePaint) {
                                            Log.d("cc", "4 $availableOptions")
                                            availableOptions.put("+-", a)
                                            Log.d("cc", "5 $availableOptions")
                                        } else if ((storage[i + a + 1][j - a - 1].checker != null || storage[i + a][j - a].checker!!.corePaint == unit.checker!!.corePaint) && a != 0) {
                                            break
                                        }
                                    }
                                }
                            }
                            a++
                        }
                        a = 0
                        while (a < size) {
                            if (i + a < size && j + a < size) {
                                if (storage[i + a][j + a].checker != null) {
                                    if (i + a + 1 < size && j + a + 1 < size) {
                                        if (storage[i + a + 1][j + a + 1].checker == null && storage[i + a][j + a].checker!!.corePaint != unit.checker!!.corePaint) {
                                            availableOptions.put("++", a)
                                        } else if ((storage[i + a + 1][j + a + 1].checker != null || storage[i + a][j + a].checker!!.corePaint == unit.checker!!.corePaint) && a != 0) {
                                            break
                                        }
                                    }
                                }
                            }
                            a++
                        }
                        a = 0
                        while (a < size) {
                            if (i - a >= 0 && j + a < size) {
                                if (storage[i - a][j + a].checker != null) {
                                    if (i - a - 1 >= 0 && j + a + 1 < size) {
                                        if (storage[i - a - 1][j + a + 1].checker == null && storage[i - a][j + a].checker!!.corePaint != unit.checker!!.corePaint) {
                                            availableOptions.put("-+", a)
                                        } else if ((storage[i - a - 1][j + a + 1].checker != null || storage[i - a][j + a].checker!!.corePaint == unit.checker!!.corePaint) && a != 0) {
                                            break
                                        }
                                    }
                                }
                            }
                            a++
                        }
                    }
                }
            }
        }
        Log.d("cc", "av ----> $availableOptions")
        for (i in 0 until size){
            for (j in 0 until size){
                if (i to j == unit.pos){
                    var a = 0
                    while (a < size){
                        if (i-a >= 0 && j-a >= 0){
                            if (storage[i-a][j-a].checker == null){
                                if ((storage[i][j].checker!!.mustBeat && availableOptions["--"] != null && a >= availableOptions["--"]!!) || !storage[i][j].checker!!.mustBeat){
                                    toBeReturnedList = toBeReturnedList.plusElement(i-a to j-a)
                                }
                            }else if (storage[i-a][j-a].checker != null){
                                if (i-a-1 >= 0 && j-a-1 >= 0){
                                    if (storage[i-a-1][j-a-1].checker == null && storage[i-a][j-a].checker!!.corePaint != unit.checker!!.corePaint){
                                        toBeReturnedList = toBeReturnedList.plusElement(i-a-1 to j-a-1)
                                    }else if ((storage[i-a-1][j-a-1].checker != null|| storage[i-a][j-a].checker!!.corePaint == unit.checker!!.corePaint) && a != 0 ){
                                        break
                                    }
                                }
                            }
                        }
                        a++
                    }
                    a = 0
                    while (a < size){
                        if (i+a < size && j-a >= 0){
                            if (storage[i+a][j-a].checker == null){
                                if ((storage[i][j].checker!!.mustBeat && availableOptions["+-"] != null && a >= availableOptions["+-"]!!) || !storage[i][j].checker!!.mustBeat){
                                    toBeReturnedList = toBeReturnedList.plusElement(i+a to j-a)
                                }
                            }else if (storage[i+a][j-a].checker != null){
                                if (i+a+1 < size && j-a-1 >= 0){
                                    if (storage[i+a+1][j-a-1].checker == null && storage[i+a][j-a].checker!!.corePaint != unit.checker!!.corePaint){
                                        toBeReturnedList = toBeReturnedList.plusElement(i+a+1 to j-a-1)
                                    }else if ((storage[i+a+1][j-a-1].checker != null || storage[i+a][j-a].checker!!.corePaint == unit.checker!!.corePaint) && a != 0 ){
                                        break
                                    }
                                }
                            }
                        }
                        a++
                    }
                    a = 0
                    while (a < size){
                        if (i+a < size && j+a < size){
                            if (storage[i+a][j+a].checker == null){
                                if ((storage[i][j].checker!!.mustBeat && availableOptions["++"] != null && a >= availableOptions["++"]!!) || !storage[i][j].checker!!.mustBeat){
                                    toBeReturnedList = toBeReturnedList.plusElement(i+a to j+a)
                                }
                            }else if (storage[i+a][j+a].checker != null){
                                if (i+a+1 < size && j+a+1 < size){
                                    if (storage[i+a+1][j+a+1].checker == null && storage[i+a][j+a].checker!!.corePaint != unit.checker!!.corePaint){
                                        toBeReturnedList = toBeReturnedList.plusElement(i+a+1 to j+a+1)
                                    }else if ((storage[i+a+1][j+a+1].checker != null || storage[i+a][j+a].checker!!.corePaint == unit.checker!!.corePaint) && a != 0 ){
                                        break
                                    }
                                }
                            }
                        }
                        a++
                    }
                    a = 0
                    while (a < size){
                        if (i-a >= 0 && j+a < size){
                            if (storage[i-a][j+a].checker == null){
                                if ((storage[i][j].checker!!.mustBeat && availableOptions["-+"] != null && a >= availableOptions["-+"]!!) || !storage[i][j].checker!!.mustBeat){
                                    toBeReturnedList = toBeReturnedList.plusElement(i-a to j+a)
                                }
                            }else if (storage[i-a][j+a].checker != null){
                                if (i-a-1 >= 0 && j+a+1 < size){
                                    if (storage[i-a-1][j+a+1].checker == null && storage[i-a][j+a].checker!!.corePaint != unit.checker!!.corePaint){
                                        toBeReturnedList = toBeReturnedList.plusElement(i-a-1 to j+a+1)
                                    }else if ((storage[i-a-1][j+a+1].checker != null || storage[i-a][j+a].checker!!.corePaint == unit.checker!!.corePaint) && a != 0 ){
                                        break
                                    }
                                }
                            }
                        }
                        a++
                    }
                }
            }
        }
        return toBeReturnedList
    }

    fun checkForChecker(unit: BoardUnit): List<Pair<Int, Int>>{
        val size = this.size
        val storage = this.storage
        var toBeReturnedList = listOf<Pair<Int, Int>>()

        for (i in 0 until size){
            for (j in 0 until size){
                if (i to j == unit.pos){
                    when (j){
                        0 -> {
                            if (i != 0 && storage[i-1][j+1].checker != null){
                                toBeReturnedList = listOf(i-1 to j+1)
                            }
                            if (i != size-1 && storage[i+1][j+1].checker != null){
                                toBeReturnedList = listOf(i+1 to j+1)
                            }
                        }
                        in 1..size-2 -> {
                            if (i != 0 && storage[i-1][j+1].checker != null && storage[i-1][j-1].checker != null){
                                toBeReturnedList = listOf(i-1 to j-1, i-1 to j+1)
                            }else if (i != 0 && storage[i-1][j+1].checker != null){
                                toBeReturnedList = listOf(i-1 to j+1)
                            }else if (i != 0 && storage[i-1][j-1].checker != null){
                                toBeReturnedList = listOf(i-1 to j-1)
                            }
                            if (i != size-1 && storage[i+1][j+1].checker != null && storage[i+1][j-1].checker != null){
                                toBeReturnedList = listOf(i+1 to j-1, i+1 to j+1)
                            }else if (i != size-1 && storage[i+1][j+1].checker != null){
                                toBeReturnedList = listOf(i+1 to j+1)
                            }else if (i != size-1 && storage[i+1][j-1].checker != null){
                                toBeReturnedList = listOf(i+1 to j-1)
                            }
                        }
                        size-1 -> {
                            if (i != 0 && storage[i-1][j-1].checker != null){
                                toBeReturnedList = listOf(i-1 to j-1)
                            }
                            if (i != size-1 && storage[i+1][j-1].checker != null){
                                toBeReturnedList = listOf(i+1 to j-1)
                            }
                        }
                    }
                }
            }
        }
        return toBeReturnedList
    }

    fun moveChecker(from: Pair<Int, Int>, where: Pair<Int, Int>){
        val storage = this.storage
        val checker = storage[from.first][from.second].checker!!

        if (checker.corePaint == white){
            storage[where.first][where.second].checker = storage[from.first][from.second].checker
            storage[where.first][where.second].checker!!.pos = where
            storage[where.first][where.second].checker!!.x = storage[where.first][where.second].checker!!.x!! + 2*(where.second - from.second)*storage[where.first][where.second].checker!!.radius!!
            storage[where.first][where.second].checker!!.y = storage[where.first][where.second].checker!!.y!! + 2*(where.first - from.first)*storage[where.first][where.second].checker!!.radius!!
            storage[from.first][from.second].checker = null

            if (checker.isQueen){
                if(from.first < where.first){
                    var i = from.first
                    var j = from.second
                    while (i < where.first) {
                        if (from.second > where.second){
                            storage[i][j].checker = null
                            j--
                        }else if (from.second < where.second){
                            storage[i][j].checker = null
                            j++
                        }
                        i++
                    }
                }else if (from.first > where.first){
                    var i = from.first
                    var j = from.second
                    while (i > where.first) {
                        if (from.second > where.second){
                            storage[i][j].checker = null
                            j--
                        }else if (from.second < where.second){
                            storage[i][j].checker = null
                            j++
                        }
                        i--
                    }
                }
            }else if (abs(where.first - from.first) > 1){
                storage[(where.first + from.first)/2][(where.second + from.second)/2].checker = null
            }
        }else if (checker.corePaint == gray){
            storage[where.first][where.second].checker = storage[from.first][from.second].checker
            storage[where.first][where.second].checker!!.pos = where
            storage[where.first][where.second].checker!!.x = storage[where.first][where.second].checker!!.x!! + 2*(where.second - from.second)*storage[where.first][where.second].checker!!.radius!!
            storage[where.first][where.second].checker!!.y = storage[where.first][where.second].checker!!.y!! + 2*(where.first - from.first)*storage[where.first][where.second].checker!!.radius!!
            storage[from.first][from.second].checker = null

            if (checker.isQueen){
                if(from.first < where.first){
                    var i = from.first
                    var j = from.second
                    while (i < where.first) {
                        if (from.second > where.second){
                            storage[i][j].checker = null
                            j--
                        }else if (from.second < where.second){
                            storage[i][j].checker = null
                            j++
                        }
                        i++
                    }
                }else if (from.first > where.first){
                    var i = from.first
                    var j = from.second
                    while (i > where.first) {
                        if (from.second > where.second){
                            storage[i][j].checker = null
                            j--
                        }else if (from.second < where.second){
                            storage[i][j].checker = null
                            j++
                        }
                        i--
                    }
                }
            }else if (abs(where.first - from.first) > 1){
                storage[(where.first + from.first)/2][(where.second + from.second)/2].checker = null
            }
        }
        this.storage = storage
    }

    fun posToCoordinates(pos: Pair<Int, Int>, size: Int): Pair<Float, Pair<Float, Float>>{
        val r = 880f / (2 * size)

        return r to (880f/size*(pos.second + 1) - r to 880f/size*(pos.first + 1) - r)
    }

    fun isThereBeaters(checker: Checker): Boolean{
        val size = this.size
        val storage = this.storage

        var anyBeaters = false

        for (i in 0 until size){
            for (j in 0 until size){
                if (storage[i][j].checker != null && storage[i][j].checker!!.corePaint == checker.corePaint && storage[i][j].checker!!.mustBeat){
                    anyBeaters = true
                }
            }
        }
        return anyBeaters
    }

    fun showWhoHasToBeat(){
        val size = this.size
        val storage = this.storage

        for (i in 0 until size){
            for (j in 0 until size){
                if(storage[i][j].checker != null) {
                    var a = 0
                    while ((a < 2 && !storage[i][j].checker!!.isQueen) || (a < size && storage[i][j].checker!!.isQueen)) {
                        if (i - a >= 0 && j - a >= 0) {
                            if (storage[i - a][j - a].checker != null) {
                                if (i - a - 1 >= 0 && j - a - 1 >= 0) {
                                    if (storage[i - a - 1][j - a - 1].checker == null && storage[i - a][j - a].checker!!.corePaint != storage[i][j].checker!!.corePaint) {
                                        storage[i][j].checker!!.mustBeat = true
                                    } else if ((storage[i - a - 1][j - a - 1].checker != null || storage[i - a][j - a].checker!!.corePaint == storage[i][j].checker!!.corePaint) && a != 0) {
                                        break
                                    }
                                }
                            }
                        }
                        a++
                    }
                    a = 0
                    while ((a < 2 && !storage[i][j].checker!!.isQueen) || (a < size && storage[i][j].checker!!.isQueen)) {
                        if (i + a < size && j - a >= 0) {
                            if (storage[i + a][j - a].checker != null) {
                                if (i + a + 1 < size && j - a - 1 >= 0) {
                                    if (storage[i + a + 1][j - a - 1].checker == null && storage[i + a][j - a].checker!!.corePaint != storage[i][j].checker!!.corePaint) {
                                        storage[i][j].checker!!.mustBeat = true
                                    } else if ((storage[i + a + 1][j - a - 1].checker != null || storage[i + a][j - a].checker!!.corePaint == storage[i][j].checker!!.corePaint) && a != 0) {
                                        break
                                    }
                                }
                            }
                        }
                        a++
                    }
                    a = 0
                    while ((a < 2 && !storage[i][j].checker!!.isQueen) || (a < size && storage[i][j].checker!!.isQueen)) {
                        if (i + a < size && j + a < size) {
                            if (storage[i + a][j + a].checker != null) {
                                if (i + a + 1 < size && j + a + 1 < size) {
                                    if (storage[i + a + 1][j + a + 1].checker == null && storage[i + a][j + a].checker!!.corePaint != storage[i][j].checker!!.corePaint) {
                                        storage[i][j].checker!!.mustBeat = true
                                    } else if ((storage[i + a + 1][j + a + 1].checker != null || storage[i + a][j + a].checker!!.corePaint == storage[i][j].checker!!.corePaint) && a != 0) {
                                        break
                                    }
                                }
                            }
                        }
                        a++
                    }
                    a = 0
                    while ((a < 2 && !storage[i][j].checker!!.isQueen) || (a < size && storage[i][j].checker!!.isQueen)) {
                        if (i - a >= 0 && j + a < size) {
                            if (storage[i - a][j + a].checker != null) {
                                if (i - a - 1 >= 0 && j + a + 1 < size) {
                                    if (storage[i - a - 1][j + a + 1].checker == null && storage[i - a][j + a].checker!!.corePaint != storage[i][j].checker!!.corePaint) {
                                        storage[i][j].checker!!.mustBeat = true
                                    } else if ((storage[i - a - 1][j + a + 1].checker != null || storage[i - a][j + a].checker!!.corePaint == storage[i][j].checker!!.corePaint) && a != 0) {
                                        break
                                    }
                                }
                            }
                        }
                        a++
                    }
                }
            }
        }
        this.storage = storage
    }

    fun clearBeaters(){
        val size = this.size
        val storage = this.storage

        for (i in 0 until size){
            for (j in 0 until size){
                if (storage[i][j].checker != null){
                    storage[i][j].checker!!.mustBeat = false
                }
            }
        }
        this.storage = storage
    }

    fun checkForWin(): Pair<Boolean, Boolean>{
        val size = this.size
        val storage = this.storage

        var whiteWon = true
        var grayWon = true

        for (i in 0 until size){
            for (j in 0 until size){
                if(storage[i][j].checker != null){
                    if (storage[i][j].checker!!.corePaint == white){
                        grayWon = false
                    }else if(storage[i][j].checker!!.corePaint == gray){
                        whiteWon = false
                    }
                }
            }
        }
        return whiteWon to grayWon
    }
}