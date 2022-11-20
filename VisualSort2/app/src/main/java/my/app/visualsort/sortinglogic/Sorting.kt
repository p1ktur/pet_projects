package my.app.visualsort.sortinglogic

import androidx.lifecycle.MutableLiveData
import my.app.visualsort.mainlogic.swap

interface Sorting {
    fun sort(data: MutableLiveData<MutableList<Double>>)
}