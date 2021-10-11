package my.app.xyexperimental1

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import my.app.xyexperimental1.shapes.*

class GraphView(context: Context, attrs: AttributeSet): View(context, attrs) {
    val drawing = GraphViewDrawing()
    var objectList = mutableListOf<Any?>()
    var actionPoint = 0f to 0f
    var cachePoint = 0f to 0f
    var action : Int?= MotionEvent.ACTION_DOWN

    override fun onDraw(canvas: Canvas?) {
        //objectList.removeIf { it == null }
        drawing.drawObjects(canvas, objectList)

        super.onDraw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        action = event?.action
        objectList.removeIf { it == null }

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                actionPoint = event.x to event.y

                when (drawing.mode) {
                    GraphViewDrawing.MODE.TRIANGLE_START -> {
                        objectList.add(drawing.createObject(listOf(actionPoint), this, action))
                    }
                    GraphViewDrawing.MODE.TRIANGLE -> {
                        if (objectList.isNotEmpty() && objectList.last() is PivotPoint) {
                            drawing.pivotPointsList.removeIf { it.parent == objectList.last() }
                            objectList.removeLast()
                        }
                        objectList.add(drawing.createObject(listOf(actionPoint, actionPoint, cachePoint), this, action))
                    }
                    GraphViewDrawing.MODE.SELECTION -> {
                        //drawing.selectObject(actionPoint, objectList)
                        drawing.getSelectedVertex(actionPoint, objectList)
                    }
                    GraphViewDrawing.MODE.MULTISELECTION -> {
                        drawing.selectObject(actionPoint, objectList)
                    }
                    else -> objectList.add(drawing.createObject(listOf(actionPoint), this, action))
                }

                postInvalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                actionPoint = event.x to event.y

                when (drawing.mode) {
                    GraphViewDrawing.MODE.TRIANGLE_START -> {
                        if (!drawing.firstClick) {
                            drawing.pivotPointsList.removeIf { it.parent == objectList.last() }
                            objectList.removeLast()
                        }
                        objectList.add(drawing.createObject(listOf(actionPoint), this, action))
                    }
                    GraphViewDrawing.MODE.TRIANGLE -> {
                        val fPoints = (objectList.last() as Triangle).fx to (objectList.last() as Triangle).fy
                        val sPoints = (objectList.last() as Triangle).sx to (objectList.last() as Triangle).sy
                        if (!drawing.firstClick) {
                            drawing.pivotPointsList.removeIf { it.parent == objectList.last() }
                            objectList.removeLast()
                        }
                        objectList.add(drawing.createObject(listOf(actionPoint, sPoints, fPoints), this, action))
                    }
                    GraphViewDrawing.MODE.SELECTION -> {
                        val dx = actionPoint.first - cachePoint.first
                        val dy = actionPoint.second - cachePoint.second
                        //if (cachePoint != 0f to 0f) drawing.moveSelectedObject(dx to dy, objectList)
                        if (drawing.selectedVertex != null && cachePoint != 0f to 0f) drawing.moveSelectedVertex(dx to dy, objectList)
                        cachePoint = actionPoint
                    }
                    GraphViewDrawing.MODE.MULTISELECTION -> {
                        drawing.selectObject(actionPoint, objectList)
                    }
                    else -> {
                        if (!drawing.firstClick) {
                            if (drawing.mode == GraphViewDrawing.MODE.POINT && objectList.last() is PivotPoint && (objectList.last() as PivotPoint).parent == null) {
                                drawing.pivotPointsList.removeLast()
                            } else {
                                drawing.pivotPointsList.removeIf { it.parent == objectList.last() }
                            }
                            if (objectList.isNotEmpty()) objectList.removeLast()
                        }
                        objectList.add(drawing.createObject(listOf(actionPoint), this, action))
                    }
                }

                postInvalidate()
            }
            MotionEvent.ACTION_UP -> {
                drawing.firstClick = true
                drawing.cachePoint = 0f to 0f
                cachePoint = 0f to 0f

                when (drawing.mode) {
                    GraphViewDrawing.MODE.RECTANGLE -> {
                        if (objectList.isNotEmpty()) objectList[objectList.size-1] = Polygon.transformToPolygon(objectList.last()!!)
                    }
                    GraphViewDrawing.MODE.TRIANGLE_START -> {
                        cachePoint = (objectList.last() as PivotPoint).x to (objectList.last() as PivotPoint).y
                        drawing.mode = GraphViewDrawing.MODE.TRIANGLE
                    }
                    GraphViewDrawing.MODE.TRIANGLE -> {
                        if (objectList.size > 1) objectList.removeAt(objectList.size-2)
                        if (objectList.isNotEmpty() && objectList.last() is Triangle) {
                            objectList[objectList.size-1] = Polygon.transformToPolygon(objectList.last()!!)
                            drawing.mode = GraphViewDrawing.MODE.TRIANGLE_START
                        }
                    }
                    GraphViewDrawing.MODE.SELECTION -> {
                        drawing.selectedVertex = null
                        drawing.pivotPointsList.forEach { it.isSelected = false }
                        objectList.forEach { shape ->
                            when (shape) {
                                is Line -> shape.pivotPointsList.forEach { it.isSelected = false }
                                is Rectangle -> shape.pivotPointsList.forEach { it.isSelected = false }
                                is Circle -> shape.centerPivotPoint.isSelected = false
                                is Triangle -> shape.pivotPointsList.forEach { it.isSelected = false }
                                is Polygon -> shape.vertices.forEach { it.isSelected = false }
                            }
                        }
                    }
                    GraphViewDrawing.MODE.MULTISELECTION -> {

                    }
                    GraphViewDrawing.MODE.POINT -> {
                        if (objectList.last() == null) {
                            objectList.removeLast()
                        } else if (objectList.last() is PivotPoint) {
                            val point = objectList.last() as PivotPoint
                            objectList.removeLast()
                            val parentLine = point.parent as Line

                            when (parentLine.parent) {
                                is Rectangle -> {
                                    val parent = parentLine.parent as Rectangle
                                    val polygon = Polygon.transformToPolygon(parent)!!
                                    polygon.vertices.add(point.also { it.parent = polygon })
                                    objectList.remove(parent)
                                    drawing.pivotPointsList.removeIf { it.parent == parent }
                                    objectList.add(polygon)
                                    drawing.pivotPointsList.addAll(polygon.vertices)
                                }
                                is Triangle -> {
                                    val parent = parentLine.parent as Triangle
                                    val polygon = Polygon.transformToPolygon(parent)!!
                                    polygon.vertices.add(point.also { it.parent = polygon })
                                    objectList.remove(parent)
                                    drawing.pivotPointsList.removeIf { it.parent == parent }
                                    objectList.add(polygon)
                                    drawing.pivotPointsList.addAll(polygon.vertices)
                                }
                                is Polygon -> {
                                    val polygon = parentLine.parent!! as Polygon
                                    polygon.vertices.add(point.also { it.parent = polygon })
                                    objectList.remove(parentLine.parent)
                                    drawing.pivotPointsList.removeIf { it.parent == polygon }
                                    objectList.add(polygon)
                                    drawing.pivotPointsList.addAll(polygon.vertices)
                                }
                                is Circle -> {}
                                else -> {
                                    //TODO moved lines move their pps
                                    val pointParent = point.parent as Line
                                    val newLines = (point.parent as Line).splitLine(point.x to point.y)
                                    objectList.remove(pointParent)
                                    drawing.pivotPointsList.removeIf { it.parent == pointParent }
                                    objectList.addAll(newLines)
                                    drawing.pivotPointsList.addAll(merge(newLines.map{ it.pivotPointsList }))
                                }
                            }
                        }
                    }
                    else -> {}
                }

                postInvalidate()
            }
        }

        //TODO fix triangle generating a line
        //TODO make pencil logic so that you plant points and then on drawing you connect them gradually
        //TODO make normal background
        //TODO make magnetic selectable points (first selection also magnetic)
        //TODO add action history and expand undo

        return true
    }
}