package my.app.visualsort.sortinglogic

import android.util.Log
import androidx.lifecycle.MutableLiveData
import my.app.visualsort.mainlogic.SortingViewModel
import my.app.visualsort.mainlogic.swap

class ShellSorting(private val viewModel: SortingViewModel) : Sorting {
    override fun sort(data: MutableLiveData<MutableList<Double>>) {
        val list = data.value!!
        val size = list.size

        var d = size / 2
        while (d >= 1) {
            for (i in d until size) {
                var j = i
                val temp = list[i]
                while (j > 0 && j >= d && list[j - d] > temp) {
                    list[j] = list[j - d]
                    j -= d
                }
                list[j] = temp
                viewModel.notifyToBluefy(d, i)
                data.postValue(list)
                Thread.sleep(900)
            }
            d /= 2
        }
    }
}