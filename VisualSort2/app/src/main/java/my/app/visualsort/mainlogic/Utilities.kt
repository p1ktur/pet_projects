package my.app.visualsort.mainlogic

import my.app.visualsort.R
import kotlin.math.*

enum class Sorts {
    BUBBLE, SELECTION, SHELL, QUICK, MERGE, COUNTING
}

val countingSortColorsList = listOf(
    R.drawable.cs_color1_outlined_background,
    R.drawable.cs_color2_outlined_background,
    R.drawable.cs_color3_outlined_background,
    R.drawable.cs_color4_outlined_background,
    R.drawable.cs_color5_outlined_background,
    R.drawable.cs_color6_outlined_background,
    R.drawable.cs_color7_outlined_background,
    R.drawable.cs_color8_outlined_background,
    R.drawable.cs_color9_outlined_background,
    R.drawable.cs_color10_outlined_background
)

fun generateElements(size: Int, range: IntRange, divider: Double, repeatable: Boolean): MutableList<Double> {
    val elements = mutableListOf<Double>()
    var i = 0
    while (i < size) {
        val num: Double = range.shuffled().random() / divider
        if (!elements.contains(num) || repeatable) {
            elements.add(num)
            i++
        }
    }
    return elements
}

fun MutableList<Double>.swap(first: Int, second: Int) {
    if (first == second) return

    val temp = this[first]
    this[first] = this[second]
    this[second] = temp
}

enum class SortingOption {
    ASCENDING, DESCENDING
}

fun List<Double>.isSorted(option: SortingOption): Boolean {
    for (i in 1 until this.size) {
        if (option == SortingOption.ASCENDING) {
            if (this[i - 1] > this[i]) {
                return false
            }
        } else {
            if (this[i - 1] < this[i]) {
                return false
            }
        }
    }

    return true
}


fun Double.round(e: Double): Double {
    val decimals = -log10(e).toInt()
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return kotlin.math.round(this * multiplier) / multiplier
}

fun <T> List<T>.toArrayList(): ArrayList<T> {
    val newArrayList = arrayListOf<T>()
    this.forEach { newArrayList.add(it) }
    return newArrayList
}

fun MutableList<Double>.getMode(): Pair<Double, Int> {
    val map = hashMapOf<Double, Int>()

    for (i in 0 until this.size) {
        if (map[this[i]] == null) {
            map[this[i]] = 1
        } else {
            map[this[i]] = map[this[i]]!! + 1
        }
    }

    val max = map.maxOf { it.value }
    var e = 0.0

    for (i in map.keys) {
        if (map[i] == max) {
            e = i
            break
        }
    }
    return e to max
}

fun <T> MutableList<T>.copy(): MutableList<T> {
    val newList = mutableListOf<T>()
    for (element in this) { newList.add(element) }
    return newList
}

fun getEmptyIntMutableList(size: Int): MutableList<Int> {
    val list = mutableListOf<Int>()
    for (i in 0 until size) list.add(0)
    return list
}