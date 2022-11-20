package my.app.visualsort.sortinglogic

import androidx.lifecycle.MutableLiveData
import my.app.visualsort.mainlogic.swap

class BubbleSorting : Sorting {
    override fun sort(data: MutableLiveData<MutableList<Double>>) {
        val list = data.value!!
        val size = list.size

        for (i in 0 until size) {
            for (j in 1 until size) {
                if (list[j - 1] > list[j]) {
                    list.swap(j - 1, j)
                    data.postValue(list)
                    Thread.sleep(700)
                }
            }
        }
    }
}