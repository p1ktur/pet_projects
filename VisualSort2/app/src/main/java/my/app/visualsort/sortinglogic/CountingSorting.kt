package my.app.visualsort.sortinglogic

import android.util.Log
import androidx.lifecycle.MutableLiveData
import my.app.visualsort.mainlogic.SortingViewModel
import my.app.visualsort.mainlogic.getEmptyIntMutableList

class CountingSorting(private val viewModel: SortingViewModel) : Sorting {
    override fun sort(data: MutableLiveData<MutableList<Double>>) {
        val list = data.value!!.map { it.toInt() }.toMutableList()
        val size = list.size

        val elementsNumber = getEmptyIntMutableList(11)
        val outputList = getEmptyIntMutableList(size)

        for (i in 0 until size) {
            elementsNumber[list[i]]++
            viewModel.notifyToColorify(i)
            Thread.sleep(1000)
        }

        for (i in 1 until elementsNumber.size) {
            elementsNumber[i] += elementsNumber[i - 1]
        }

        for (i in (size - 1).downTo(0)) {
            outputList[elementsNumber[list[i]] - 1] = list[i]
            elementsNumber[list[i]]--
        }

        data.postValue(outputList.map {it.toDouble()}.toMutableList())
    }
}