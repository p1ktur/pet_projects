package my.app.visualsort.mainlogic

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import my.app.visualsort.R

class ElementRecyclerViewAdapter(var list: List<Double>, private val context: Context) : RecyclerView.Adapter<ElementRecyclerViewAdapter.ViewHolder>() {

    private val max = list.max()
    private var initialBarHeight: Int? = null
    private val sortingOption = SortingOption.ASCENDING
    var shellD: Int? = null
    var shellP: Int? = null
    var pivotP: Int? = null
    var rangeSE: Pair<Int?, Int?> = null to null
    var colorP: Int? = null

    class ViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        val bar: ImageView = item.findViewById(R.id.colored_bar)
        val elementNumber: TextView = item.findViewById(R.id.element_number)
        val elementLayout: ConstraintLayout = item.findViewById(R.id.element_layout)

        companion object {
            var savedList: MutableList<Double>? = null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val holder = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.sorted_element, parent, false))
        if (initialBarHeight == null) initialBarHeight = holder.bar.layoutParams.height
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.elementNumber.text = list[position].toString()
        holder.bar.layoutParams.height = (initialBarHeight!! * list[position] / max).toInt()

        if (shellD == null && shellP == null && pivotP == null && colorP == null) {
            if (ViewHolder.savedList != null && ViewHolder.savedList!![position] != list[position]) {
                holder.bar.setBackgroundResource(R.drawable.red_outlined_background)

                val animation = AnimationUtils.loadAnimation(context, R.anim.element_fade_in)
                holder.elementLayout.startAnimation(animation)
            } else {
                holder.bar.setBackgroundResource(R.drawable.green_outlined_background)
            }
        } else if(shellD != null && shellP != null) {
            if (ViewHolder.savedList != null && ViewHolder.savedList!![position] != list[position]) {
                holder.bar.setBackgroundResource(R.drawable.red_outlined_background)

                val animation = AnimationUtils.loadAnimation(context, R.anim.element_fade_in)
                holder.elementLayout.startAnimation(animation)
            } else {
                var isBlued = false
                var n = shellP!!
                while (n >= 0) {
                    if (n == position) isBlued = true
                    n -= shellD!!
                }
                if (isBlued) {
                    holder.bar.setBackgroundResource(R.drawable.blue_outlined_background)

                    val animation = AnimationUtils.loadAnimation(context, R.anim.element_fade_in)
                    holder.elementLayout.startAnimation(animation)
                } else {
                    holder.bar.setBackgroundResource(R.drawable.green_outlined_background)
                }
            }
        } else if (pivotP != null) {
            if (ViewHolder.savedList != null && ViewHolder.savedList!![position] != list[position]) {
                holder.bar.setBackgroundResource(R.drawable.red_outlined_background)

                val animation = AnimationUtils.loadAnimation(context, R.anim.element_fade_in)
                holder.elementLayout.startAnimation(animation)
            } else {
                if (pivotP == position) {
                    holder.bar.setBackgroundResource(R.drawable.yellow_outlined_background)
                } else if (rangeSE.first!! <= position && position <= rangeSE.second!!) {
                    holder.bar.setBackgroundResource(R.drawable.purple_outlined_background)
                } else {
                    holder.bar.setBackgroundResource(R.drawable.green_outlined_background)
                }
            }
        } else if (colorP != null) {
            if (position <= colorP!!) {
                holder.bar.setBackgroundResource(countingSortColorsList[list[position].toInt() - 1])

                val animation = AnimationUtils.loadAnimation(context, R.anim.element_fade_in)
                holder.elementLayout.startAnimation(animation)
            } else {
                holder.bar.setBackgroundResource(R.drawable.green_outlined_background)
            }
        }

        if (ViewHolder.savedList != null) {
            ViewHolder.savedList!![position] = list[position]
        } else ViewHolder.savedList = list.toMutableList()
    }

    override fun getItemCount(): Int = list.size

    fun clearSavedList() {
        ViewHolder.savedList = null
    }
}