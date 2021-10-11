package my.app.xyexperimental1

fun minOf(list: List<Float>): Float {
    var min = list[0]
    for (i in 1 until list.size) {
        if (list[i] < min) min = list[i]
    }
    return min
}

fun <T> merge(list: List<List<T>>): List<T> {
    val newList = mutableListOf<T>()
    list.forEach { newList.addAll(it) }
    return newList.toList()
}