package my.app.visualsort.mainlogic

import android.util.Log
import androidx.lifecycle.MutableLiveData
import my.app.visualsort.sortinglogic.SelectionSorting
import kotlin.math.*

class Matrix(private val columns: Int, private val rows: Int) {
    private val epsilon = 0.001
    private var coefficients = arrayListOf<ArrayList<Double>>()
    private var values = arrayListOf<Double>()

    init {
        for (i in 0 until rows) {
            coefficients.add(arrayListOf())
            for (j in 0 until columns) {
                coefficients[i].add(0.0)
            }
        }

        for (i in 0 until rows) {
            values.add(0.0)
        }
    }

    fun getValues(): ArrayList<Double> = values

    fun getCoefficients(): ArrayList<ArrayList<Double>> = coefficients

    fun fillCoefficients(valuesList: ArrayList<Double>): Matrix {
        for (i in 0 until rows) {
            for (j in 0 until columns) {
                coefficients[i][j] = valuesList[columns*i + j]
            }
        }
        return this
    }

    fun fillOtherCoefficients(valuesList: ArrayList<ArrayList<Double>>): Matrix {
        coefficients = valuesList
        return this
    }

    fun fillValues(valuesList: ArrayList<Double>): Matrix {
        for (i in 0 until rows) {
            values[i] = valuesList[i]
        }
        return this
    }

    private fun Double.getAlgebraicComplement(a: Int, b: Int, determinant: Double): Double {
        return determinant * (-1.0).pow(a + b)
    }

    private fun getDeterminant(): Double {
        return if (columns == rows) {
            if (columns == 2) {
                coefficients[0][0] * coefficients[1][1] - coefficients[0][1] * coefficients[1][0]
            } else {
                var determinant = 0.0
                for (i in 0 until columns) {
                    val tempMatrix = Matrix(columns - 1, rows - 1)
                    val tempCoefficients = arrayListOf<Double>()
                    for (j in 1 until rows) {
                        tempCoefficients.addAll(coefficients[j].filterIndexed { index, _ -> index != i })
                    }
                    determinant += coefficients[0][i].getAlgebraicComplement(0, i, tempMatrix.fillCoefficients(tempCoefficients).getDeterminant()) * coefficients[0][i]
                }

                determinant
            }
        } else 0.0
    }

    private fun transpose(): Matrix {
        val newCoefficients = arrayListOf<Double>()
        for (j in 0 until columns) {
            for (i in 0 until rows) {
                newCoefficients.add(coefficients[i][j])
            }
        }

        return Matrix(rows, columns).fillCoefficients(newCoefficients)
    }

    fun findCramerSolution(e: Double): ArrayList<Double> {
        val mainDeterminant = getDeterminant()
        val listOfDeterminants = arrayListOf<Double>()

        for (i in 0 until rows) {
            val newCoefficients = arrayListOf<Double>()
            for (j in 0 until rows) {
                newCoefficients.addAll(coefficients[j].mapIndexed { index, d -> if (index == i) values[j] else d})
            }
            listOfDeterminants.add(Matrix(columns, rows).fillCoefficients(newCoefficients).getDeterminant())
        }
        //println("Детермінант А: $mainDeterminant, решта детермінантів $listOfDeterminants")
        return listOfDeterminants.map { (it/mainDeterminant).round(epsilon) }.toArrayList()
    }

    operator fun times(other: Matrix): Matrix? {
        return if (this.columns == other.rows) {
            val newMatrix = Matrix(other.columns, this.rows)
            val newCoefficients = arrayListOf<Double>()
            this.coefficients.forEach { row ->
                for (j in 0 until other.columns) {
                    val otherList = other.coefficients.map { values -> values[j] }.toArrayList()
                    newCoefficients.add(row.let {
                        var result = 0.0
                        for (index in 0 until row.size) {
                            result += it[index] * otherList[index]
                        }
                        result
                    })
                }
            }
            newMatrix.fillCoefficients(newCoefficients)
        } else null
    }

    operator fun plus(other: Matrix): Matrix? {
        return if (this.columns == other.columns && this.rows == other.rows) {
            val newMatrix = Matrix(this.columns, this.rows)
            val newCoefficients = arrayListOf<Double>()

            for (i in 0 until this.columns) {
                for (j in 0 until this.rows) {
                    newCoefficients.add(this.coefficients[j][i] + other.coefficients[j][i])
                }
            }
            return newMatrix.fillCoefficients(newCoefficients)
        } else null
    }

    operator fun minus(other: Matrix): Matrix? {
        return if (this.columns == other.columns && this.rows == other.rows) {
            val newMatrix = Matrix(this.columns, this.rows)
            val newCoefficients = arrayListOf<Double>()

            for (i in 0 until this.columns) {
                for (j in 0 until this.rows) {
                    newCoefficients.add(this.coefficients[j][i] - other.coefficients[j][i])
                }
            }
            return newMatrix.fillCoefficients(newCoefficients)
        } else null
    }

    override fun toString(): String {
        var displayedText = ""
        coefficients.forEach {
            it.forEach { coefficient ->
                displayedText += "${coefficient.round(epsilon)} "
            }
            displayedText += if (values.all { d -> d == 0.0 }) {
                if (columns == 1) "" else "\n"
            } else "| ${values[coefficients.indexOf(it)].round(0.001)}\n"
        }

        return displayedText.dropLast(1)
    }

    fun copy(): Matrix {
        return Matrix(columns, rows).fillOtherCoefficients(coefficients).fillValues(values)
    }

    fun swapRows(first: Int, second: Int) {
        if (first == second) return

        val temp = this.coefficients[first]
        this.coefficients[first] = this.coefficients[second]
        this.coefficients[second] = temp

        val dTemp = this.values[first]
        this.values[first] = this.values[second]
        this.values[second] = dTemp
    }

    fun swapColumns(first: Int, second: Int) {
        if (first == second) return

        val firstList = mutableListOf<Double>()
        val secondList = mutableListOf<Double>()

        for (i in 0 until rows) {
            firstList.add(coefficients[i][first])
        }

        for (i in 0 until rows) {
            secondList.add(coefficients[i][second])
        }

        for (i in 0 until rows) {
            coefficients[i][first] = secondList[i]
        }

        for (i in 0 until rows) {
            coefficients[i][second] = firstList[i]
        }
    }

    fun sortColumns() {
        for (i in 0 until coefficients[0].size) {
            var min = i
            for (j in i until coefficients[0].size) {
                if (coefficients[0][j] < coefficients[0][min]) min = j
            }
            if (i != min) {
                swapColumns(i, min)

                Log.d("TAG", "Sorting, step $i: \n$this")
            }
        }
    }

    fun mergeSortColumns() {
        var s = 1

        fun swapColumns(list: MutableList<Double>) {
            for (e in list) {
                val n = list.indexOf(e)
                val m = coefficients[0].indexOf(e)
                if (n != m) swapColumns(n, m)
            }
        }

        fun mergeSort(list: MutableList<Double>, start: Int, end: Int) {
            fun merge(list: MutableList<Double>, start: Int, middle: Int, end: Int) {
                val firstSubList = list.subList(start, middle + 1).copy()
                val secondSubList = list.subList(middle + 1, end + 1).copy()

                var k = start
                var i = 0
                var j = 0

                while (i < firstSubList.size && j < secondSubList.size) {
                    if (firstSubList[i] <= secondSubList[j]) {
                        list[k] = firstSubList[i]
                        i++
                    } else {
                        list[k] = secondSubList[j]
                        j++
                    }
                    k++
                }

                while (i < firstSubList.size) {
                    list[k] = firstSubList[i]
                    i++
                    k++
                }

                while (j < secondSubList.size) {
                    list[k] = secondSubList[j]
                    j++
                    k++
                }

                swapColumns(list)

                if (end - start > 0) {
                    Log.d("TAG", "Sorting, step $s: \n$this")
                    s++
                }
            }

            if (start >= end) return

            val middle = (start + end) / 2

            mergeSort(list, start, middle)
            mergeSort(list, middle + 1, end)

            merge(list, start, middle, end)
        }

        val firstElementsList = coefficients[0].copy()

        mergeSort(firstElementsList, 0, firstElementsList.size - 1)
    }
}