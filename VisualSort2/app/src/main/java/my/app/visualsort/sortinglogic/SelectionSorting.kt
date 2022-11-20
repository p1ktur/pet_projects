package my.app.visualsort.sortinglogic

import androidx.lifecycle.MutableLiveData
import my.app.visualsort.mainlogic.swap

class SelectionSorting : Sorting {
    override fun sort(data: MutableLiveData<MutableList<Double>>) {
        val list = data.value!!
        val size = list.size

        for (i in 0 until size) {
            var min = i
            for (j in i until size) {
                if (list[j] < list[min]) min = j
            }
            list.swap(i, min)
            if (i != min) {
                data.postValue(list)
                Thread.sleep(900)
            }
        }
    }
}