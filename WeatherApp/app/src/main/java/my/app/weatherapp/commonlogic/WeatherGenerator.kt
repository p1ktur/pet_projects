package my.app.weatherapp.commonlogic

//TODO does task 6
//class which generates realistic weather data
class WeatherGenerator {
    companion object {
        //fills list with generated data
        fun generateListData(list: MutableList<WeatherData>, from: Date, to: Date, previousInstance: WeatherData?) {
            if (from > to) return

            val data = getWeatherByDate(from, previousInstance)

            list.add(data)

            generateListData(list, from.getNextDay(), to, data)
        }

        //generates data depending on current date and previous weather information
        private fun getWeatherByDate(date: Date, previousInstance: WeatherData?): WeatherData {
            val humidity = getHumidityByDate(previousInstance?.humidity)
            return WeatherData(
                date.year,
                date.month,
                date.day,
                getTemperatureByDate(date, previousInstance?.temperature),
                getPressureByDate(date, humidity),
                humidity,
                getWindDirection(previousInstance?.windDirection)
            )
        }

        //generates temperature depending on current date and previous weather information
        private fun getTemperatureByDate(date: Date, previousInstance: Int?): Int {
            val averageTemperature = 15
            var temperatureBoost = 0

            when (date.month) {
                0 -> temperatureBoost -= 20
                1 -> temperatureBoost -= 15
                2 -> temperatureBoost -= 5
                3 -> temperatureBoost += 5
                4 -> temperatureBoost += 10
                5 -> temperatureBoost += 10
                6 -> temperatureBoost += 15
                7 -> temperatureBoost += 10
                8 -> temperatureBoost += 5
                9 -> temperatureBoost += 0
                10 -> temperatureBoost -= 10
                11 -> temperatureBoost -= 15
            }

            return if ((1..100).shuffled().random() < 31 || previousInstance == null) {
                averageTemperature + temperatureBoost + (-3..3).shuffled().random()
            } else{
                previousInstance + (-1..1).shuffled().random()
            }

        }

        //generates pressure depending on current date and current humidity
        private fun getPressureByDate(date: Date, humidity: Int): Int {
            val averagePressure = 750

            var pressureBoost: Int = humidity / 4

//            when (date.month) {
//                0 -> pressureBoost -= 20
//                1 -> pressureBoost -= 10
//                2 -> pressureBoost += 0
//                3 -> pressureBoost += 5
//                4 -> pressureBoost += 10
//                5 -> pressureBoost += 15
//                6 -> pressureBoost += 15
//                7 -> pressureBoost += 10
//                8 -> pressureBoost += 5
//                9 -> pressureBoost += 0
//                10 -> pressureBoost -= 10
//                11 -> pressureBoost -= 15
//            }

            val final = averagePressure + pressureBoost + (-3..3).shuffled().random()
            return if (final < 720) return 720 else final
        }

        //generates humidity depending on previous humidity
        private fun getHumidityByDate(previousInstance: Int?): Int {
            val averageHumidity = 50

            return if ((1..100).shuffled().random() < 31 || previousInstance == null) {
                val final = averageHumidity + (-40..35).shuffled().random()
                if (final < 10) 10 else final
            } else {
                val final = previousInstance + (-5..5).shuffled().random()
                if (final < 10) 10 else final
            }
        }

        //generates wind direction depending on previous wind direction
        private fun getWindDirection(previousInstance: WindDirections?): WindDirections {
            return if ((0..1).shuffled().random() == 1 && previousInstance != null) {
                if ((0..1).shuffled().random() == 1) {
                    previousInstance.getNext()
                } else {
                    previousInstance.getPrevious()
                }
            } else {
                val values = WindDirections.values().toMutableList()
                values.shuffled().random()
            }
        }
    }
}