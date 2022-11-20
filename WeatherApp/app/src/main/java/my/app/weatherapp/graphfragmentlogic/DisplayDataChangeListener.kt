package my.app.weatherapp.graphfragmentlogic

//interface to implement listener in order to save options/data on rotation
interface DisplayDataChangeListener {
    fun onDataChange(option: Int, yearIndex: Int)
}