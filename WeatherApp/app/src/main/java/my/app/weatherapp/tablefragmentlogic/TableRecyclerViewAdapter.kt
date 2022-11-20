package my.app.weatherapp.tablefragmentlogic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import my.app.weatherapp.R
import my.app.weatherapp.commonlogic.WeatherData

//adapter class to display data as a table
class TableRecyclerViewAdapter(private val list: MutableList<WeatherData>) : RecyclerView.Adapter<TableRecyclerViewAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateText: TextView = view.findViewById(R.id.dateText)
        val temperatureText: TextView = view.findViewById(R.id.temperatureText)
        val pressureText: TextView = view.findViewById(R.id.pressureText)
        val humidityText: TextView = view.findViewById(R.id.humidityText)
        val windDirectionText: TextView = view.findViewById(R.id.windDirectionText)
    }

    //adapter staff
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.table_view_element, parent, false))
    }

    //adapter staff
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.dateText.text = list[position].getDateAsString()
        val temperature = list[position].temperature
        holder.temperatureText.text = if (temperature > 0) "+$temperature" else temperature.toString()
        holder.pressureText.text = list[position].pressure.toString()
        holder.humidityText.text = list[position].humidity.toString()
        holder.windDirectionText.text = list[position].windDirection.toString()
    }

    //adapter staff
    override fun getItemCount(): Int = list.size
}