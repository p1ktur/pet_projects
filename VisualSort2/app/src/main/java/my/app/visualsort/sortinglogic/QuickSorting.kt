package my.app.visualsort.sortinglogic

import androidx.lifecycle.MutableLiveData
import my.app.visualsort.mainlogic.SortingViewModel
import my.app.visualsort.mainlogic.swap

class QuickSorting(private val viewModel: SortingViewModel) : Sorting {
    override fun sort(data: MutableLiveData<MutableList<Double>>) {
        val list = data.value!!
        val size = list.size

        quickSort(list, 0, size - 1, data, size - 1)
    }

    private fun quickSort(list: MutableList<Double>, start: Int, end: Int, data: MutableLiveData<MutableList<Double>>, oldPivot: Int) {
        if (start < end) {
            viewModel.notifyToYellify(oldPivot)
            viewModel.notifyToPurplefy(start, end)

            val index = partition(list, start, end, data)

            viewModel.notifyToYellify(index)

            quickSort(list, start, index - 1, data, index)
            quickSort(list, index + 1, end, data, index)
        }
    }

    private fun partition(list: MutableList<Double>, start: Int, end: Int, data: MutableLiveData<MutableList<Double>>): Int {
        val pivot = list[end]
        var i = start - 1

        for (j in start until end) {
            if (list[j] <= pivot) {
                i++

                list.swap(i, j)

                if (i != j) {
                    data.postValue(list)
                    Thread.sleep(1500)
                }
            }
        }

        list.swap(i + 1, end)

        data.postValue(list)
        Thread.sleep(1500)

        return i + 1
    }
}