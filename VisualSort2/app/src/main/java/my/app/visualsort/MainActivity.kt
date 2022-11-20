package my.app.visualsort

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import my.app.visualsort.mainlogic.*
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.sin

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var elementRecyclerView: RecyclerView
    private lateinit var sortSpinner: Spinner
    private lateinit var sortButton: Button
    private lateinit var generateButton: Button
    private lateinit var shuffleButton: Button

    private var sort: Sorts = Sorts.BUBBLE

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //var elements = generateElements(14, 1..100, 10.0, false) //normal
        var elements = generateElements(14, 1..10, 1.0, true) //repeatable ints

        elementRecyclerView = findViewById(R.id.elementRecyclerView)
        elementRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val recyclerViewAdapter = ElementRecyclerViewAdapter(elements, this)
        elementRecyclerView.adapter = recyclerViewAdapter

        val viewModel = ViewModelProvider(this)[SortingViewModel::class.java]
        viewModel.setElements(elements)
        viewModel.elements.observe(this) {
            elements = it
            (elementRecyclerView.adapter as ElementRecyclerViewAdapter).list = elements //when there are repeatable elements
            (elementRecyclerView.adapter as ElementRecyclerViewAdapter).notifyDataSetChanged()
        }
        viewModel.greenifyCounter.observe(this) {
            (elementRecyclerView.adapter as ElementRecyclerViewAdapter).notifyDataSetChanged()
        }
        viewModel.shellParameters.observe(this) {
            (elementRecyclerView.adapter as ElementRecyclerViewAdapter).shellD = it.first
            (elementRecyclerView.adapter as ElementRecyclerViewAdapter).shellP = it.second
        }
        viewModel.pivotParameter.observe(this) {
            (elementRecyclerView.adapter as ElementRecyclerViewAdapter).pivotP = it
        }
        viewModel.rangeParameters.observe(this) {
            (elementRecyclerView.adapter as ElementRecyclerViewAdapter).rangeSE = it.first to it.second
        }
        viewModel.colorParameter.observe(this) {
            (elementRecyclerView.adapter as ElementRecyclerViewAdapter).colorP = it
            (elementRecyclerView.adapter as ElementRecyclerViewAdapter).notifyDataSetChanged()
        }

        sortSpinner = findViewById(R.id.sortSpinner)
        val spinnerAdapter = ArrayAdapter(this, R.layout.spinner_element, resources.getStringArray(R.array.sorting))
        sortSpinner.adapter = spinnerAdapter
        sortSpinner.onItemSelectedListener = this

        sortButton = findViewById(R.id.sortButton)
        sortButton.setOnClickListener {
            when (sort) {
                Sorts.BUBBLE -> viewModel.startBubbleSorting(this, elementRecyclerView)
                Sorts.SELECTION -> viewModel.startSelectionSorting(this, elementRecyclerView)
                Sorts.SHELL -> viewModel.startShellSorting(this, elementRecyclerView)
                Sorts.QUICK -> viewModel.startQuickSorting(this, elementRecyclerView)
                Sorts.MERGE -> viewModel.startMergeSorting(this, elementRecyclerView)
                Sorts.COUNTING -> viewModel.startCountingSorting(this, elementRecyclerView)
            }
        }

        generateButton = findViewById(R.id.generateButton)
        generateButton.setOnClickListener {
            //val list = generateElements(14, 1..100, 10.0, false) //normal
            val list = generateElements(14, 1..10, 1.0, true) //repeatable ints
            elements = list
            viewModel.elements.value = list
            (elementRecyclerView.adapter as ElementRecyclerViewAdapter).clearSavedList()
            elementRecyclerView.adapter = ElementRecyclerViewAdapter(elements, this)
        }

        shuffleButton = findViewById(R.id.shuffleButton)
        shuffleButton.setOnClickListener {
            val list = viewModel.elements.value!!
            list.shuffle()
            (elementRecyclerView.adapter as ElementRecyclerViewAdapter).clearSavedList()
            viewModel.elements.value = list
        }

        //TODO Lab Work 2 Task
//        val matrix: Matrix

//        val columns = (4..10).shuffled().random()
//        val rows = (4..10).shuffled().random()
//
//        matrix = Matrix(columns, rows)
//
//        val coefficients = arrayListOf<Double>()
//
//        for (i in 0 until columns * rows) {
//            coefficients.add((1..100).shuffled().random() / 10.0)
//        }
//
//        matrix.fillCoefficients(coefficients)
//
//        Log.d("TAG", "Initial matrix: \n$matrix")
//
//        matrix.sort()
//
//        Log.d("TAG", "End matrix: \n$matrix")

        //TODO Lab Work 3 Task
//        val list = generateElements(20, 1..10, true)
//        Log.d("TAG", "Start array: $list")
//        val mode = list.getMode()
//        Log.d("TAG", "Mode: ${mode.first} Repeats: ${mode.second}")
//        list.removeIf { it == mode.first }
//        Log.d("TAG", "Array after removing mode: $list")
//
//        var d = list.size / 2
//        while (d >= 1) {
//            for (i in d until list.size) {
//                var j = i
//                val temp = list[i]
//                while (j > 0 && j >= d && list[j - d] > temp) {
//                    list[j] = list[j - d]
//                    j -= d
//                }
//                list[j] = temp
//            }
//            d /= 2
//        }
//
//        Log.d("TAG", "Array after sorting: $list")

        //TODO Lab Work 4 Task
//        val list = generateElements(20, -50..50, false)
//
//        Log.d("TAG", "Start array: $list")
//
//        for (i in 0 until list.size) {
//            if (list[i] < 0) list[i] = sin(list[i]).round(0.01)
//        }
//
//        Log.d("TAG", "Array after replacement: $list")
//
//        fun quickSort(list: MutableList<Double>, start: Int, end: Int) {
//            fun partition(list: MutableList<Double>, start: Int, end: Int): Int {
//                val pivot = list[end]
//                var i = start - 1
//
//                for (j in start until end) {
//                    if (list[j] <= pivot) {
//                        i++
//
//                        list.swap(i, j)
//                    }
//                }
//
//                list.swap(i + 1, end)
//
//                return i + 1
//            }
//
//            if (start < end) {
//                val index = partition(list, start, end)
//
//                quickSort(list, start, index - 1)
//                quickSort(list, index + 1, end)
//            }
//        }
//
//        quickSort(list, 0, list.size - 1)
//
//        Log.d("TAG", "Array after sorting: $list")

        //TODO Lab Work 5 Task
//        val matrix: Matrix
//
//        val columns = (4..10).shuffled().random()
//        val rows = (4..10).shuffled().random()
//
//        matrix = Matrix(columns, rows)
//
//        val coefficients = arrayListOf<Double>()
//
//        for (i in 0 until columns * rows) {
//            coefficients.add((-50..50).shuffled().random() / 10.0)
//        }
//
//        matrix.fillCoefficients(coefficients)
//
//        Log.d("TAG", "Initial matrix: \n$matrix")
//
//        for (row in matrix.getCoefficients()) {
//            val index = row.indexOf(row.min())
//            row[index] = ln(abs(row[index]))
//        }
//
//        Log.d("TAG", "Matrix after change: \n$matrix")
//
//        matrix.mergeSortColumns()
//
//        Log.d("TAG", "End matrix: \n$matrix")

        //TODO Lab Work 6 Task

        val studentNames = listOf("Renat", "Maksym", "Oleg", "Sergiy", "Dmytro", "Hrystyna", "Alina", "Ludmila", "Julija", "Borys", "Ustym", "Larysa")
        val studentMarks = listOf(1, 2, 3, 4, 5)

        data class Student(
            var name: String = "",
            var mark: Int = 1
        ) {
            fun generateList(size: Int): MutableList<Student> {
                val list = mutableListOf<Student>()
                for (i in 0 until size) list.add(Student(studentNames.shuffled().random(), studentMarks.shuffled().random()))
                return list
            }

            fun sort(list: List<Student>) {
                val size = list.size
                val elementsNumber = getEmptyIntMutableList(256)
                val outputList = mutableListOf("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "")

                for (i in 0 until size) {
                    elementsNumber[list[i].name[0].code]++
                }

                for (i in 1 until elementsNumber.size) {
                    elementsNumber[i] += elementsNumber[i - 1]
                }

                for (i in (size - 1).downTo(0)) {
                    outputList[elementsNumber[list[i].name[0].code] - 1] = list[i].name
                    elementsNumber[list[i].name[0].code]--
                }

                for (i in 0 until size) {
                    list[i].name = outputList[i]
                }
            }

            override fun toString(): String {
                return "$name: $mark"
            }
        }

        val allStudents = Student().generateList(20)

        Log.d("TAG", "Initial list: \n${allStudents.filterIndexed { index, _ -> index < 10 }}\n" +
                "${allStudents.filterIndexed { index, _ -> index >= 10 }}")

        val students = allStudents.filter { it.mark >= 4 }

        Log.d("TAG", "List of students whose mark is bigger than 3: \n$students")

        Student().sort(students)

        Log.d("TAG", "Sorted list: \n$students")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, i: Int, l: Long) {
        if (view != null && parent != null && parent.id == sortSpinner.id) {
            sort = Sorts.values()[i]
            Toast.makeText(this, "Selected $sort", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Toast.makeText(this, "Nothing is selected", Toast.LENGTH_SHORT).show()
    }
}