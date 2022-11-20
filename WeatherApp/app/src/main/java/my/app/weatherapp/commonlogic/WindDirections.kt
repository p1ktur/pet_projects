package my.app.weatherapp.commonlogic

//wind direction class
//WEST = from west to east
enum class WindDirections {
    NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST, NORTH_WEST;

    //returns next direction clockwise
    fun getNext(): WindDirections {
        val index = values().indexOf(this)
        return if (index == 7) {
            values()[0]
        } else {
            values()[index + 1]
        }
    }

    //returns next direction anti-clockwise
    fun getPrevious(): WindDirections {
        val index = values().indexOf(this)
        return if (index == 0) {
            values()[7]
        } else {
            values()[index - 1]
        }
    }

    //returns wind direction as text
    override fun toString(): String {
        return super.toString().map { it.lowercase() }.joinToString("").replaceFirstChar { it.uppercase() }.replace("_", "")
    }
}