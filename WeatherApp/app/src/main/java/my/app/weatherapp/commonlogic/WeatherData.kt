package my.app.weatherapp.commonlogic

//weather class which stores information about weather
data class WeatherData(
    var year: Int = 2022,
    var month: Int = 8, // 0..11
    var day: Int = 17, // 1..31
    var temperature: Int = 16, // in °C
    var pressure: Int = 725, // in mm
    var humidity: Int = 70, // in %
    var windDirection: WindDirections = WindDirections.WEST // 8 directions in total
) {
    //no default constructor in data class
    //basically data class constructor must have at least one parameter by default
    //but still can be called as:
    //val data = WeatherData()

    //blank constructor
    constructor() : this(
        year = 2018,
        month = 0,
        day = 1,
        temperature = (-30..40).shuffled().random(),
        pressure = (700..740).shuffled().random(),
        humidity = (0..10).shuffled().random() * 10,
        windDirection = WindDirections.values().apply { shuffle() }.random()
    )

    //copy constructor
    constructor(data: WeatherData) : this(
        year = data.year,
        month = data.month,
        day = data.day,
        temperature = data.temperature,
        pressure = data.pressure,
        humidity = data.humidity,
        windDirection = data.windDirection
    )

    companion object {
        //returns random instance of weather data
        fun randomInstance(): WeatherData {
            val year = (2000..2023).shuffled().random()
            val month = (0..11).shuffled().random()
            val day = when (month) {
                1 -> if (year % 4 == 0 && year % 100 != 0) (1..29).shuffled().random() else (1..28).shuffled().random()
                3, 5, 8, 10 -> (1..30).shuffled().random()
                else -> (1..31).shuffled().random()
            }
            val temperature = (-30..40).shuffled().random()
            val pressure = (700..740).shuffled().random()
            val humidity = (0..10).shuffled().random() * 10
            val windDirection = WindDirections.values().apply { shuffle() }.random()

            return WeatherData(year, month, day, temperature, pressure, humidity, windDirection)
        }

        //sorts list in order by date
        fun sortByDate(list: MutableList<WeatherData>) {
            list.sortWith(compareBy { it.formatDateToComparable() })
        }

        //deciphers file data into an WeatherData object
        fun fromFileString(string: String): WeatherData {
            val dataArray = string.split("|")
            if (dataArray.size < 7) throw DecipheringLogicException("")
            return WeatherData(
                dataArray[0].toInt(),
                dataArray[1].toInt(),
                dataArray[2].toInt(),
                dataArray[3].toInt(),
                dataArray[4].toInt(),
                dataArray[5].toInt(),
                WindDirections.values()[dataArray[6].toInt()]
            )
        }
    }

    //returns date data as a Date() object
    fun getDate(): Date {
        return Date(year, month, day)
    }

    //returns date data as formatted text
    fun getDateAsString(): String {
        return Date(year, month, day).toString()
    }

    //ciphers WeatherData object data into a string to be written into a file
    fun toFileString(): String {
        return "$year|$month|$day|$temperature|$pressure|$humidity|${WindDirections.values().indexOf(windDirection)}"
    }

    //returns date as number which can be compared
    private fun formatDateToComparable(): Long {
        return if (month in 0..8) {
            if (day in 1..9) {
                "${year}0${month + 1}0$day".toLong()
            } else {
                "${year}0${month + 1}$day".toLong()
            }
        } else {
            if (day in 1..9) {
                "${year}${month + 1}0$day".toLong()
            } else {
                "${year}${month + 1}$day".toLong()
            }
        }
    }
}
