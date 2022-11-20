package my.app.weatherapp.commonlogic

import android.content.Context
import android.text.Html
import android.view.Gravity
import android.widget.Toast

//function that rounds float number to needed order
fun Double.round(e: Double): Double {
    val decimals = -kotlin.math.log10(e).toInt()
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return kotlin.math.round(this * multiplier) / multiplier
}

fun showToastMessage(context: Context, text: String, duration: Int) {
    val toast = Toast.makeText(context, Html.fromHtml("<font color='#000056' ><b>$text</b></font>", 0), duration)
    toast.setGravity(Gravity.CENTER, 0, 0)
    toast.show()
}