package my.app.xyexperimental1

import android.graphics.Canvas
import android.util.Log
import android.view.MotionEvent
import my.app.xyexperimental1.shapes.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class GraphViewDrawing {
    var mode = MODE.PENCIL
    private val paint = PAINT.BLACK
    var firstClick = true
    var cachePoint = 0f to 0f
    var pivotPointsList = mutableListOf<PivotPoint>()
    var selectedVertex: PivotPoint? = null

    enum class MODE {
        LINE,
        RECTANGLE,
        CIRCLE,
        TRIANGLE_START,
        TRIANGLE,
        POINT,
        PENCIL,
        SELECTION,
        MULTISELECTION
    }

    fun createObject(iPoints: List<Pair<Float, Float>>, graphView: GraphView, action: Int?, iMode: MODE = mode): Any? {
        val points = (when (mode){
            MODE.TRIANGLE -> listOf(pullPoint(iPoints[0], iPoints.drop(1)), iPoints[1], iPoints[2])
            //MODE.TRIANGLE_START -> listOf(pullPoint(iPoints[0], iPoints))
            MODE.POINT -> listOf(pullPoint(iPoints[0], listOf(cachePoint), graphView.objectList))
            else -> listOf(pullPoint(iPoints[0], listOf(cachePoint)))
        }).toMutableList()

        when (iMode) {
            MODE.LINE -> {
                val newLine: Line

                if (firstClick) {
                    cachePoint = points[0]
                    firstClick = false
                    newLine = Line(points[0].first, points[0].second, points[0].first, points[0].second, paint, null)
                } else {
                    newLine = Line(cachePoint.first, cachePoint.second, points[0].first, points[0].second, paint, null)
                    if (action != MotionEvent.ACTION_MOVE) firstClick = true
                }

                pivotPointsList.addAll(newLine.pivotPointsList)
                return newLine
            }
            MODE.RECTANGLE -> {
                val newRect: Rectangle

                if (firstClick) {
                    cachePoint = points[0]
                    firstClick = false
                    newRect = Rectangle(points[0].first, points[0].second, points[0].first, points[0].second, paint)
                } else {
                    newRect = Rectangle(cachePoint.first, cachePoint.second, points[0].first, points[0].second, paint)
                    if (action != MotionEvent.ACTION_MOVE) firstClick = true
                }

                pivotPointsList.addAll(newRect.pivotPointsList)
                return newRect
            }
            MODE.CIRCLE -> {
                val radius: Float
                val newCircle: Circle

                if (firstClick) {
                    cachePoint = points[0]
                    firstClick = false
                    newCircle = Circle(points[0].first, points[0].second, 0f, paint)
                } else {
                    radius = Circle.countRadius(cachePoint.first, cachePoint.second, points[0].first, points[0].second)
                    //radius = Circle.checkIfRadiusAcceptable(graphView, radius, cachePoints)
                    newCircle = Circle(cachePoint.first, cachePoint.second, radius, paint)

                    if (action != MotionEvent.ACTION_MOVE) firstClick = true
                }

                pivotPointsList.add(newCircle.centerPivotPoint)
                return newCircle
            }
            MODE.TRIANGLE_START -> {
                if (firstClick) {
                    cachePoint = points[0]
                    firstClick = false
                }
                return PivotPoint(points[0].first, points[0].second, PAINT.TRIANGLE_BUILD_PAINT, null)
            }
            MODE.TRIANGLE -> {
                if (firstClick) {
                    //points[1] = pullPoint(points[1], listOf(points[0], points[2]))
                    firstClick = false
                }
                val newTriangle = Triangle(points[2].first, points[2].second, points[1].first, points[1].second, points[0].first, points[0].second, paint)
                pivotPointsList.addAll(newTriangle.pivotPointsList)
                if (action != MotionEvent.ACTION_MOVE) firstClick = true
                return newTriangle
            }
            MODE.POINT -> {
                if (firstClick) {
                    cachePoint = points[0]
                    firstClick = false
                }
                //expanding logic
                val parentLine = getParentLine(points[0], graphView.objectList)
                if (parentLine != null) {
                    val newPoint = PivotPoint(points[0].first, points[0].second, PAINT.PIVOT_POINT_PAINT, parentLine)
                    //pivotPointsList.add(newPoint)
                    return newPoint
                }
            }
            MODE.PENCIL -> {
                if (firstClick) {
                    cachePoint = iPoints[0]
                    firstClick = false
                } else {
                    val newLine = Line(cachePoint.first, cachePoint.second, points[0].first, points[0].second, paint, null)
                    firstClick = true
                    return newLine
                }
            }
            else -> {

            }
        }
        return null
    }

    fun drawObjects(canvas: Canvas?, objectList: MutableList<Any?>) {
        objectList.forEach {
            when (it) {
                is Line -> {
                    canvas?.drawLine(it)
                    val points = it.pivotPointsList
                    canvas?.drawPivotPoints(points)
                }
                is Rectangle -> {
                    canvas?.drawRect(it)
                    val points = it.pivotPointsList
                    canvas?.drawPivotPoints(points)
                }
                is Circle -> {
                    canvas?.drawCircle(it)
                    val point = it.centerPivotPoint
                    canvas?.drawPivotPoints(point)
                }
                is Triangle -> {
                    canvas?.drawTriangle(it)
                    val points = it.pivotPointsList
                    canvas?.drawPivotPoints(points)
                }
                is Polygon -> {
                    canvas?.drawPolygon(it)
                    val points = it.vertices
                    canvas?.drawPivotPoints(points)
                }
                is PivotPoint -> {
                    canvas?.drawPivotPoints(it)
                }
                //MODE.PENCIL -> {
                //
                //}
            }
        }
    }

    private fun pullPoint(point: Pair<Float, Float>, selfPoints: List<Pair<Float, Float>>, objectList: MutableList<Any?>? = null): Pair<Float, Float> {
        val pullDistance = 30f

        Log.d("TAG", "${pivotPointsList.size} $pivotPointsList")

        if (mode == MODE.POINT) {
            val shapeList = objectList!!.filterNot { it is PivotPoint }
            val linesList = mutableListOf<Line>()
            shapeList.forEach {
                when (it) {
                    is Line -> linesList.add(it)
                    is Rectangle -> linesList.addAll(it.drawableLinesList)
                    is Triangle -> linesList.addAll(it.drawableLinesList)
                    is Polygon -> linesList.addAll(it.drawableLinesList)
                    is Circle -> {}
                }
            }

            linesList.forEach {
                if (it.checkIfOwnsPoint(point) != null) return it.checkWhereOwnsPoint(point)
            }

            val distanceList = linesList.map { it.getDistanceToPoint(point) }
            val closestLine = linesList[distanceList.indexOf(minOf(distanceList))]
            return if (closestLine.checkIfPointIsInRange(point)) closestLine.checkWhereOwnsPoint(point) else closestLine.getEdgePoint(point)

        } else {
            pivotPointsList.filterNot { selfPoints.contains(it.x to it.y) }.forEach {
                val xd = abs(it.x - point.first)
                val yd = abs(it.y - point.second)
                if (xd <= pullDistance && yd <= pullDistance) {
                    val distance = sqrt(xd.pow(2) + yd.pow(2))
                    if (distance <= pullDistance && distance != 0f) return it.x to it.y
                }
            }
        }

        return point
    }

    private fun getParentLine(point: Pair<Float, Float>, objectList: MutableList<Any?>): Line? {
        val shapeList = objectList.filterNot { it is PivotPoint }
        val linesList = mutableListOf<Line>()
        shapeList.forEach {
            when (it) {
                is Line -> linesList.add(it)
                is Rectangle -> linesList.addAll(it.drawableLinesList)
                is Triangle -> linesList.addAll(it.drawableLinesList)
                is Polygon -> {linesList.addAll(it.drawableLinesList); Log.d("TAG", "${it.drawableLinesList}")}
                is Circle -> {}
            }
        }

        linesList.forEach {
            if (it.checkIfOwnsPoint(point) != null) return it
        }

        val distanceList = linesList.map { it.getDistanceToPoint(point) }
        val closestLine = linesList[distanceList.indexOf(minOf(distanceList))]
        return if (closestLine.checkIfPointIsInRange(point)) closestLine else null
    }

    fun clearAllSelection(objectList: MutableList<Any?>) {
        objectList.forEach {
            when (it) {
                is Line -> it.isSelected = false
                is Rectangle -> it.isSelected = false
                is Circle -> it.isSelected = false
                is Triangle -> it.isSelected = false
                is Polygon -> it.isSelected = false
                is PivotPoint -> it.isSelected = false
            }
        }
    }

    fun selectObject(point: Pair<Float, Float>, objectList: MutableList<Any?>) {
        if (mode == MODE.SELECTION) clearAllSelection(objectList)
        val selectedObject = detectObject(point, objectList) ?: return
    }

    //if i want to move pivotpoints
    fun getSelectedVertex(point: Pair<Float, Float>, objectList: MutableList<Any?>) {
        objectList.forEach {
            when (it) {
                is Line -> {
                    val endPoint = it.checkIfEndsOwnPoint(point)
                    if (endPoint != null) {
                        it.pivotPointsList[endPoint].isSelected = true
                        selectedVertex = it.pivotPointsList[endPoint]
                    }
                }
                is Rectangle -> {
                    val endPoint = it.checkIfEndsOwnPoint(point)
                    if (endPoint != null) {
                        it.pivotPointsList[endPoint].isSelected = true
                        selectedVertex = it.pivotPointsList[endPoint]
                    }
                }
                is Circle -> {
                    if (it.checkIfEndsOwnPoint(point)) {
                        it.centerPivotPoint.isSelected = true
                        selectedVertex = it.centerPivotPoint
                    }
                }
                is Triangle -> {
                    val endPoint = it.checkIfEndsOwnPoint(point)
                    if (endPoint != null) {
                        it.pivotPointsList[endPoint].isSelected = true
                        selectedVertex = it.pivotPointsList[endPoint]
                    }
                }
                is Polygon -> {
                    val endPoint = it.checkIfEndsOwnPoint(point)
                    if (endPoint != null) {
                        it.vertices[endPoint].isSelected = true
                        selectedVertex = it.vertices[endPoint]
                    }
                }
                else -> {}
            }
        }
    }

    //if i want to move by lines
    private fun detectObject(point: Pair<Float, Float>, objectList: MutableList<Any?>): Int? {
        objectList.forEach {
            when (it) {
                is Line -> if (it.checkIfOwnsPoint(point) != null) {
                    it.isSelected = true
                    return objectList.indexOf(it)
                }
                is Rectangle -> if (it.checkIfOwnsPoint(point)) {
                    it.isSelected = true
                    return objectList.indexOf(it)
                }
                is Circle -> if (it.checkIfOwnsPoint(point)) {
                    it.isSelected = true
                    return objectList.indexOf(it)
                }
                is Triangle -> if (it.checkIfOwnsPoint(point)) {
                    it.isSelected = true
                    return objectList.indexOf(it)
                }
                is Polygon -> if (it.checkIfOwnsPoint(point)) {
                    it.isSelected = true
                    return objectList.indexOf(it)
                }
                is PivotPoint -> if (it.checkIfOwnsPoint(point)) {
                    it.isSelected = true
                    return objectList.indexOf(it)
                }
                else -> {}
            }
        }
        return null
    }

    private fun detectObjectToExpand(point: Pair<Float, Float>, objectList: MutableList<Any?>): Line? {
        objectList.forEach { shape ->
            when (shape) {
                is Line -> {
                    val ownership = shape.checkIfOwnsPoint(point)
                    if (ownership != null) return ownership
                }
                is Rectangle -> shape.drawableLinesList.forEach {
                    val ownership = it.checkIfOwnsPoint(point)
                    if (ownership != null) return ownership
                }
                is Circle -> {
                    //val ownership = shape.centerPivotPoint.checkIfOwnsPoint(point)
                    //if (ownership != null) return ownership
                }
                is Triangle -> shape.drawableLinesList.forEach {
                    val ownership = it.checkIfOwnsPoint(point)
                    if (ownership != null) return ownership
                }
            }
        }
        return null
    }

    fun moveSelectedVertex(delta: Pair<Float, Float>, objectList: MutableList<Any?>) {
        val parent = selectedVertex!!.parent
        val parentIndex = objectList.indexOf(parent)
        val parentPointsList = mutableListOf<PivotPoint>()
        val pointIndex: Int = when (parent) {
            is Line -> {
                parentPointsList.addAll(parent.pivotPointsList)
                parent.pivotPointsList.indexOf(selectedVertex!!)
            }
            is Rectangle -> {
                parentPointsList.addAll(parent.pivotPointsList)
                parent.pivotPointsList.indexOf(selectedVertex!!)
            }
            is Triangle -> {
                parentPointsList.addAll(parent.pivotPointsList)
                parent.pivotPointsList.indexOf(selectedVertex!!)
            }
            is Polygon -> {
                parentPointsList.addAll(parent.vertices)
                parent.vertices.indexOf(selectedVertex!!)
            }
            else -> 0
        }

        val newPoint = pullPoint(selectedVertex!!.x + delta.first to selectedVertex!!.y + delta.second, mutableListOf(selectedVertex!!.x to selectedVertex!!.y) + parentPointsList.map { it.x to it.y })
        if (newPoint != selectedVertex!!.x to selectedVertex!!.y) {
            val newDelta = newPoint.first - selectedVertex!!.x to newPoint.second - selectedVertex!!.y
            selectedVertex!!.move(newDelta)
        } else selectedVertex!!.move(delta)

        when (val newParent = objectList[parentIndex]) {
            is Line -> {
                newParent.pivotPointsList[pointIndex] = selectedVertex!!
                newParent.update()
            }
            is Rectangle -> {
                newParent.pivotPointsList[pointIndex] = selectedVertex!!
                newParent.update()
            }
            is Circle -> {
                newParent.centerPivotPoint = selectedVertex!!
                newParent.update()
            }
            is Triangle -> {
                newParent.pivotPointsList[pointIndex] = selectedVertex!!
                newParent.update()
            }
            is Polygon -> {
                newParent.vertices[pointIndex] = selectedVertex!!
                newParent.update()
            }
        }
    }

    //perhaps can be optimized and remade
    fun moveSelectedObject(delta: Pair<Float, Float>, objectList: MutableList<Any?>) {
        objectList.filter { when (it) {
                is Line -> it.isSelected
                is Rectangle -> it.isSelected
                is Circle -> it.isSelected
                is Triangle -> it.isSelected
                is Polygon -> it.isSelected
                is PivotPoint -> it.isSelected
                else -> false
            }
        }.forEach {
            when (it) {
                is Line -> {
                    var pullDetected = false
                    it.pivotPointsList.forEach { point ->
                        if (!pullDetected) {
                            val newPoint = pullPoint(point.x + delta.first to point.y + delta.second, PivotPoint.toCoordinates(it.pivotPointsList.filter{ e -> e != point }.toMutableList()))
                            if (newPoint != point.x to point.y) {
                                val newDelta = newPoint.first - point.x to newPoint.second - point.y
                                it.move(newDelta)
                                pullDetected = true
                            }
                        }
                    }
                    if (!pullDetected) {
                        it.move(delta)
                        Log.d("TAG", "${pivotPointsList.map { e -> e.parent }} $it")
                        pivotPointsList.removeIf { e -> e.parent == it }
                        pivotPointsList.addAll(it.pivotPointsList)
                    }
                }
                is Rectangle -> {
                    var pullDetected = false
                    it.pivotPointsList.forEach { point ->
                        if (!pullDetected) {
                            val newPoint = pullPoint(point.x + delta.first to point.y + delta.second, PivotPoint.toCoordinates(it.pivotPointsList.filter{ e -> e != point }.toMutableList()))
                            if (newPoint != point.x to point.y) {
                                val newDelta = newPoint.first - point.x to newPoint.second - point.y
                                it.move(newDelta)
                                pullDetected = true
                            }
                        }
                    }
                    if (!pullDetected) it.move(delta)
                }
                is Polygon -> {
                    var pullDetected = false
                    it.vertices.forEach { point ->
                        if (!pullDetected) {
                            val newPoint = pullPoint(point.x + delta.first to point.y + delta.second, PivotPoint.toCoordinates(it.vertices.filter{ e -> e != point }.toMutableList()))
                            if (newPoint != point.x to point.y) {
                                val newDelta = newPoint.first - point.x to newPoint.second - point.y
                                it.move(newDelta)
                                pullDetected = true
                            }
                        }
                    }
                    if (!pullDetected) it.move(delta)
                }
                is Circle -> {
                    val newPoint = pullPoint(it.cx + delta.first to it.cy + delta.second, listOf(it.cx to it.cy))
                    if (newPoint != it.cx to it.cy) {
                        val newDelta = newPoint.first - it.cx to newPoint.second - it.cy
                        it.move(newDelta)
                    } else it.move(delta)
                }
                is Triangle -> {
                    var pullDetected = false
                    it.pivotPointsList.forEach { point ->
                        if (!pullDetected) {
                            val newPoint = pullPoint(point.x + delta.first to point.y + delta.second, PivotPoint.toCoordinates(it.pivotPointsList.filter{ e -> e != point }.toMutableList()))
                            if (newPoint != point.x to point.y) {
                                val newDelta = newPoint.first - point.x to newPoint.second - point.y
                                it.move(newDelta)
                                pullDetected = true
                            }
                        }
                    }
                    if (!pullDetected) it.move(delta)
                }
                is PivotPoint -> {
                    val newPoint = pullPoint(it.x + delta.first to it.y + delta.second, listOf(it.x to it.y))
                    if (newPoint != it.x to it.y) {
                        val newDelta = newPoint.first - it.x to newPoint.second - it.y
                        it.move(newDelta)
                    } else it.move(delta)
                }
            }
        }
    }
}