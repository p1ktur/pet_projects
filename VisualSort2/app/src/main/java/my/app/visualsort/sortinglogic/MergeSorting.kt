package my.app.visualsort.sortinglogic

import android.util.Log
import androidx.lifecycle.MutableLiveData
import my.app.visualsort.mainlogic.SortingViewModel
import my.app.visualsort.mainlogic.copy

class MergeSorting(private val viewModel: SortingViewModel) : Sorting {
    override fun sort(data: MutableLiveData<MutableList<Double>>) {
        val list = data.value!!
        val size = list.size

        mergeSort(list, 0, size - 1, data)
    }

    private fun mergeSort(list: MutableList<Double>, start: Int, end: Int, data: MutableLiveData<MutableList<Double>>) {
        if (start >= end) return

        viewModel.notifyToPurplefy(start, end)

        val middle = (start + end) / 2

        mergeSort(list, start, middle, data)
        mergeSort(list, middle + 1, end, data)

        merge(list, start, middle, end, data)
    }

    private fun merge(list: MutableList<Double>, start: Int, middle: Int, end: Int, data: MutableLiveData<MutableList<Double>>) {
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

        if (end - start > 0) {
            data.postValue(list)
            Thread.sleep(1800)
        }
    }
}