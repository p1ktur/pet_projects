package my.app.visualsort.mainlogic

import android.content.Context
import android.view.animation.AnimationUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import my.app.visualsort.R
import my.app.visualsort.sortinglogic.*

class SortingViewModel : ViewModel() {
    val elements = MutableLiveData<MutableList<Double>>()
    val greenifyCounter = MutableLiveData<Int>()
    val shellParameters = MutableLiveData<Pair<Int?, Int?>>()
    val pivotParameter = MutableLiveData<Int?>()
    val rangeParameters = MutableLiveData<Pair<Int?, Int?>>()
    val colorParameter = MutableLiveData<Int?>()

    init {
        greenifyCounter.value = 0
    }

    fun setElements(e: MutableList<Double>) {
        elements.value = e
    }

    fun startBubbleSorting(context: Context, r: RecyclerView) {
        val bs = BubbleSorting()
        GlobalScope.launch {
            bs.sort(elements)

            notifyToGreenify()
            val animation = AnimationUtils.loadAnimation(context, R.anim.element_fade_in)
            r.startAnimation(animation)
        }
    }

    fun startSelectionSorting(context: Context, r: RecyclerView) {
        val ss = SelectionSorting()
        GlobalScope.launch {
            ss.sort(elements)

            notifyToGreenify()
            val animation = AnimationUtils.loadAnimation(context, R.anim.element_fade_in)
            r.startAnimation(animation)
        }
    }

    fun startShellSorting(context: Context, r: RecyclerView) {
        val ss = ShellSorting(this)
        GlobalScope.launch {
            ss.sort(elements)

            notifyToDisableBluefy()
            notifyToGreenify()
            val animation = AnimationUtils.loadAnimation(context, R.anim.element_fade_in)
            r.startAnimation(animation)
        }
    }

    fun startQuickSorting(context: Context, r: RecyclerView) {
        val qs = QuickSorting(this)
        GlobalScope.launch {
            qs.sort(elements)

            notifyToDisableYellify()
            notifyToDisablePurplefy()
            notifyToGreenify()
            val animation = AnimationUtils.loadAnimation(context, R.anim.element_fade_in)
            r.startAnimation(animation)
        }
    }

    fun startMergeSorting(context: Context, r: RecyclerView) {
        val ms = MergeSorting(this)
        GlobalScope.launch {
            ms.sort(elements)

            notifyToDisablePurplefy()
            notifyToGreenify()
            val animation = AnimationUtils.loadAnimation(context, R.anim.element_fade_in)
            r.startAnimation(animation)
        }
    }

    fun startCountingSorting(context: Context, r: RecyclerView) {
        val cs = CountingSorting(this)
        GlobalScope.launch {
            cs.sort(elements)

            val animation = AnimationUtils.loadAnimation(context, R.anim.element_fade_in)
            r.startAnimation(animation)

            Thread.sleep(2000)

            notifyToDisableColorify()
            notifyToGreenify()
        }
    }

    private fun notifyToGreenify() {
        Thread.sleep(300)
        greenifyCounter.postValue(greenifyCounter.value!! + 1)
    }

    fun notifyToBluefy(d: Int, p: Int) {
        shellParameters.postValue(d to p)
    }

    private fun notifyToDisableBluefy() {
        shellParameters.postValue(null to null)
    }

    fun notifyToYellify(p: Int) {
        pivotParameter.postValue(p)
    }

    private fun notifyToDisableYellify() {
        pivotParameter.postValue(null)
    }

    fun notifyToPurplefy(s: Int, e: Int) {
        rangeParameters.postValue(Pair(s, e))
    }

    private fun notifyToDisablePurplefy() {
        rangeParameters.postValue(Pair(null, null))
    }

    fun notifyToColorify(l: Int) {
        colorParameter.postValue(l)
    }

    private fun notifyToDisableColorify() {
        colorParameter.postValue(null)
    }
}

