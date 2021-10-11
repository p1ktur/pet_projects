package my.app.xyexperimental1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import my.app.xyexperimental1.shapes.PivotPoint


lateinit var graphView: GraphView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        graphView = findViewById(R.id.graph_view)

        val addLineBut = findViewById<ImageButton>(R.id.add_line_but).apply {
            setOnClickListener {
                graphView.drawing.run {
                    clearAllSelection(graphView.objectList)
                    mode = GraphViewDrawing.MODE.LINE
                    firstClick = true
                    cachePoint = 0f to 0f
                }
            }
        }

        val addRectangleBut = findViewById<ImageButton>(R.id.add_rectangle_but).apply {
            setOnClickListener {
                graphView.drawing.run {
                    clearAllSelection(graphView.objectList)
                    mode = GraphViewDrawing.MODE.RECTANGLE
                    firstClick = true
                    cachePoint = 0f to 0f
                }
            }
        }

        val addCircleBut = findViewById<ImageButton>(R.id.add_circle_but).apply {
            setOnClickListener {
                graphView.drawing.run {
                    clearAllSelection(graphView.objectList)
                    mode = GraphViewDrawing.MODE.CIRCLE
                    firstClick = true
                    cachePoint = 0f to 0f
                }
            }
        }
        val addTriangleBut = findViewById<ImageButton>(R.id.add_triangle_but).apply {
            setOnClickListener {
                graphView.drawing.run {
                    clearAllSelection(graphView.objectList)
                    mode = GraphViewDrawing.MODE.TRIANGLE_START
                    firstClick = true
                    cachePoint = 0f to 0f
                }
            }
        }

        val addPointBut = findViewById<ImageButton>(R.id.add_point_but).apply {
            setOnClickListener {
                graphView.drawing.run {
                    clearAllSelection(graphView.objectList)
                    mode = GraphViewDrawing.MODE.POINT
                    firstClick = true
                    cachePoint = 0f to 0f
                }
            }
        }

        val pencilBut = findViewById<ImageButton>(R.id.pencil_but).apply {
            setOnClickListener {
                graphView.drawing.run {
                    clearAllSelection(graphView.objectList)
                    mode = GraphViewDrawing.MODE.PENCIL
                    firstClick = true
                    cachePoint = 0f to 0f
                }
            }
        }

        val clearButton = findViewById<Button>(R.id.clear_but).apply {
            setOnClickListener {
                graphView.objectList.clear()
                graphView.drawing.pivotPointsList.clear()
                graphView.postInvalidate()
            }
        }

        val undoButton = findViewById<Button>(R.id.undo_but).apply {
            setOnClickListener {
                if (graphView.objectList.isNotEmpty()) {
                    if (graphView.objectList.last() !is PivotPoint) graphView.drawing.pivotPointsList.removeIf { parent == graphView.objectList.last() }
                    graphView.objectList.removeLast()
                    graphView.postInvalidate()
                }
            }
        }

        val selectButton = findViewById<Button>(R.id.select_but).apply {
            setOnClickListener {
                graphView.drawing.run {
                    mode = GraphViewDrawing.MODE.SELECTION
                    firstClick = true
                    cachePoint = 0f to 0f
                }
            }
        }

        val multiSelectButton = findViewById<Button>(R.id.multi_select_but).apply {
            setOnClickListener {
                graphView.drawing.run {
                    mode = GraphViewDrawing.MODE.MULTISELECTION
                    firstClick = true
                    cachePoint = 0f to 0f
                }
            }
        }

        val clearSelectionButton = findViewById<Button>(R.id.clear_selection_but).apply {
            setOnClickListener {
                graphView.drawing.clearAllSelection(graphView.objectList)
                graphView.postInvalidate()
            }
        }
    }
}
