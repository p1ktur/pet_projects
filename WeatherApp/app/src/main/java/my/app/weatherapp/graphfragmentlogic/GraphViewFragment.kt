package my.app.weatherapp.graphfragmentlogic

import android.content.res.Configuration
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.viewpager2.widget.ViewPager2.Orientation
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.datepicker.MaterialDatePicker
import my.app.weatherapp.R
import my.app.weatherapp.commonlogic.*
import my.app.weatherapp.commonlogic.Date
import my.app.weatherapp.main.MainActivity
import java.io.*
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.math.abs

//TODO does task 5
//Graph (Chart) view fragment class
class GraphViewFragment : Fragment() {
    private val DISPLAY_OPTION_PARAM = "param1"
    private val YEAR_INDEX_PARAM = "param2"

    lateinit var weatherChart: LineChart
    private var dataMap: HashMap<Int, MutableList<WeatherData>> = HashMap()
    private val rootList = mutableListOf<WeatherData>()

    lateinit var displayOptionsRadioGroup: RadioGroup
    private var displayOption = DisplayOptions.TEMPERATURE
    private var displayedYearIndex = 0
    private var displayedYears: Set<Int> = HashSet()

    lateinit var previousYearButton: Button
    lateinit var nextYearButton: Button

    lateinit var displayAverTempAndHighHumButton: Button
    lateinit var averageTemperatureText: TextView
    lateinit var highestHumText: TextView
    lateinit var selectedPeriodText: TextView

    private var hardPeriodList = mutableListOf<Pair<WeatherData, WeatherData>>()
    private var displayedPeriod = 0
    private var hardPeriodInitialized = false
    lateinit var periodTextDecription: TextView
    lateinit var periodText: TextView
    lateinit var prevPeriodButton: ImageButton
    lateinit var nextPeriodButton: ImageButton

    enum class DisplayOptions {
        TEMPERATURE, PRESSURE, HUMIDITY
    }

    //Fragment staff
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            displayOption = when (it.getInt(DISPLAY_OPTION_PARAM)) {
                0 -> DisplayOptions.TEMPERATURE
                1 -> DisplayOptions.PRESSURE
                else -> DisplayOptions.HUMIDITY
            }
            displayedYearIndex = it.getInt(YEAR_INDEX_PARAM)
        }
    }

    //Fragment staff
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_graph_view, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance(option: Int, yearIndex: Int) =
            GraphViewFragment().apply {
                arguments = Bundle().apply {
                    putInt(DISPLAY_OPTION_PARAM, option)
                    putInt(YEAR_INDEX_PARAM, yearIndex)
                }
            }
    }

    //Fragment staff
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val files = view.context.getExternalFilesDirs(Environment.DIRECTORY_DOCUMENTS)
        val file = File(files[0].absolutePath + "/InputData.txt")

        retrieveFileData(file)

        weatherChart = view.findViewById(R.id.weatherChart)

        initGraph()

        //buttons to selected chart displaying options
        displayOptionsRadioGroup = view.findViewById(R.id.displayRadioGroup)
        displayOptionsRadioGroup.setOnCheckedChangeListener { _, viewId ->
            when (viewId) {
                R.id.tempRadioButton -> {
                    displayOption = DisplayOptions.TEMPERATURE
                    notifyListener(activity as MainActivity)
                    displayData()
                }
                R.id.presRadioButton -> {
                    displayOption = DisplayOptions.PRESSURE
                    notifyListener(activity as MainActivity)
                    displayData()
                }
                R.id.humRadioButton -> {
                    displayOption = DisplayOptions.HUMIDITY
                    notifyListener(activity as MainActivity)
                    displayData()
                }
            }
        }

        //button to switch displayed year backwards
        previousYearButton = view.findViewById(R.id.previousYearButton)
        previousYearButton.setOnClickListener {
            if (displayedYearIndex == 0) displayedYearIndex = displayedYears.size - 1 else displayedYearIndex--
            notifyListener(activity as MainActivity)
            displayData()
        }

        //button to switch displayed year forwards
        nextYearButton = view.findViewById(R.id.nextYearButton)
        nextYearButton.setOnClickListener {
            if (displayedYearIndex == displayedYears.size - 1) displayedYearIndex = 0 else displayedYearIndex++
            notifyListener(activity as MainActivity)
            displayData()
        }

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            averageTemperatureText = view.findViewById(R.id.averageTemperatureText)
            highestHumText = view.findViewById(R.id.highestHumText)
            selectedPeriodText = view.findViewById(R.id.selectedPeriodText)

            //TODO does task 3
            displayAverTempAndHighHumButton = view.findViewById(R.id.displayAverTempAndHighHumButton)
            displayAverTempAndHighHumButton.setOnClickListener {
                val dateRangePicker =
                    MaterialDatePicker.Builder.dateRangePicker()
                        .setTitleText("Select dates to generate data")
                        .setSelection(
                            androidx.core.util.Pair(
                                MaterialDatePicker.thisMonthInUtcMilliseconds(),
                                MaterialDatePicker.todayInUtcMilliseconds()
                            )
                        )
                        .build()

                dateRangePicker.show(parentFragmentManager, "")

                dateRangePicker.addOnPositiveButtonClickListener {
                    try {
                        val calendar = Calendar.getInstance()

                        calendar.timeInMillis = it.first
                        var tempF = Date(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                        var tempS = rootList.first().getDate()
                        val first = if (tempF < tempS) tempS else tempF

                        calendar.timeInMillis = it.second
                        tempF = Date(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                        tempS = rootList.last().getDate()
                        val second = if (tempF > tempS) tempS else tempF

                        val tempList = rootList.filter { data ->
                            data.getDate() > first && data.getDate() < second
                        }

                        var sum = 0.0
                        var q = 0

                        for (d in tempList) {
                            sum += d.temperature
                            q++
                        }

                        val average: Double = (sum / q).round(0.01)

                        val maxHumidityList = mutableListOf<WeatherData>()
                        for (k in displayedYears) {
                            if (tempList.none { data -> data.year == k }) continue
                            val maxHumidity = tempList.filter { data -> data.year == k }.maxOf { data -> data.humidity }
                            maxHumidityList.addAll(tempList.filter { data -> data.humidity == maxHumidity && data.year == k })
                        }

                        var humidityText = "Highest humidity days: "
                        for (i in 0 until maxHumidityList.size) {
                            humidityText += "${maxHumidityList[i].humidity}% | ${maxHumidityList[i].getDate()} (${maxHumidityList[i].getDate().getDayOfYear()})"
                            if (i != maxHumidityList.size - 1) humidityText += "; "
                        }

                        selectedPeriodText.text = "$first - $second"
                        averageTemperatureText.text = "$average °C"
                        highestHumText.text = humidityText
                    } catch (e: NoSuchElementException) {
                        showToastMessage(requireContext(), "No weather data.", Toast.LENGTH_LONG)
                    } catch (e: IndexOutOfBoundsException) {
                        showToastMessage(requireContext(), "No weather data.", Toast.LENGTH_LONG)
                    }
                }
            }

            periodTextDecription = view.findViewById(R.id.periodTextDecription)
            periodText = view.findViewById(R.id.periodText)

            //TODO does task 4
            //button to switch displayed periods backwards
            prevPeriodButton = view.findViewById(R.id.prevPeriodButton)
            prevPeriodButton.setOnClickListener {
                try {
                    if (!hardPeriodInitialized) {
                        initHardPercentagePeriods()
                        hardPeriodInitialized = true
                    } else {
                        if (displayedPeriod == 0) displayedPeriod = hardPeriodList.size - 1 else displayedPeriod--
                        val first = "${hardPeriodList[displayedPeriod].first.temperature} °C | ${hardPeriodList[displayedPeriod].first.pressure} mm | ${hardPeriodList[displayedPeriod].first.getDate()} (${hardPeriodList[displayedPeriod].first.getDate().getDayOfYear()})"
                        val second = "${hardPeriodList[displayedPeriod].second.temperature} °C | ${hardPeriodList[displayedPeriod].second.pressure} mm | ${hardPeriodList[displayedPeriod].second.getDate()} (${hardPeriodList[displayedPeriod].second.getDate().getDayOfYear()})"
                        periodText.text = "$first\n—\n$second"
                    }
                } catch (e: NoSuchElementException) {
                    showToastMessage(requireContext(), "No weather data.", Toast.LENGTH_LONG)
                } catch (e: IndexOutOfBoundsException) {
                    showToastMessage(requireContext(), "No weather data.", Toast.LENGTH_LONG)
                }
            }

            //TODO does task 4
            //button to switch displayed periods forwards
            nextPeriodButton = view.findViewById(R.id.nextPeriodButton)
            nextPeriodButton.setOnClickListener {
                try {
                    if (!hardPeriodInitialized) {
                        initHardPercentagePeriods()
                        hardPeriodInitialized = true
                    } else {
                        if (displayedPeriod == hardPeriodList.size - 1) displayedPeriod = 0 else displayedPeriod++
                        val first = "${hardPeriodList[displayedPeriod].first.temperature} °C | ${hardPeriodList[displayedPeriod].first.pressure} mm | ${hardPeriodList[displayedPeriod].first.getDate()} (${hardPeriodList[displayedPeriod].first.getDate().getDayOfYear()})"
                        val second = "${hardPeriodList[displayedPeriod].second.temperature} °C | ${hardPeriodList[displayedPeriod].second.pressure} mm | ${hardPeriodList[displayedPeriod].second.getDate()} (${hardPeriodList[displayedPeriod].second.getDate().getDayOfYear()})"
                        periodText.text = "$first\n—\n$second"
                    }
                } catch (e: NoSuchElementException) {
                    showToastMessage(requireContext(), "No weather data.", Toast.LENGTH_LONG)
                } catch (e: IndexOutOfBoundsException) {
                    showToastMessage(requireContext(), "No weather data.", Toast.LENGTH_LONG)
                }
            }
        }


        when (displayOption) {
            DisplayOptions.TEMPERATURE -> displayOptionsRadioGroup.check(R.id.tempRadioButton)
            DisplayOptions.PRESSURE -> displayOptionsRadioGroup.check(R.id.presRadioButton)
            DisplayOptions.HUMIDITY -> displayOptionsRadioGroup.check(R.id.humRadioButton)
        }

        displayData()
    }

    //TODO play with permissions
    //gets data from file
    private fun retrieveFileData(file: File) {
        val list = mutableListOf<WeatherData>()

        try {
            val fis = FileInputStream(file)
            val br = BufferedReader(FileReader(file))
            var line: String?
            while (br.readLine().also {line = it} != null) {
                list.add(WeatherData.fromFileString(line!!))
            }
            br.close()
            fis.close()

            rootList.addAll(list)
            displayedYears = list.map { it.year }.toSet()

            for (i in displayedYears.indices) {
                dataMap[displayedYears.elementAt(i)] = list.filter { it.year == displayedYears.elementAt(i) }.toMutableList()
            }
        } catch (e: FileNotFoundException) {
            showToastMessage(requireContext(), "No input file is found.", Toast.LENGTH_LONG)
        } catch (e: DecipheringLogicException) {
            showToastMessage(requireContext(), "Input file data is corrupted.", Toast.LENGTH_LONG)
        } catch (e: IOException) {
            showToastMessage(requireContext(), "Cannot read data from input file.", Toast.LENGTH_LONG)
        }
    }

    //configures chart
    private fun initGraph() {
        weatherChart.setNoDataText("No weather data found")
        weatherChart.setNoDataTextColor(ColorTemplate.rgb("#000056"))
        weatherChart.description.isEnabled = false
        weatherChart.setDrawGridBackground(false)
        weatherChart.setTouchEnabled(true)
        weatherChart.maxHighlightDistance = 50f
        weatherChart.isDragEnabled = true
        weatherChart.setScaleEnabled(true)
        weatherChart.setMaxVisibleValueCount(200)
        weatherChart.setPinchZoom(true)
    }

    //sends data to be saved
    private fun notifyListener(listener: DisplayDataChangeListener) {
        listener.onDataChange(DisplayOptions.values().indexOf(displayOption), displayedYearIndex)
    }

    //sets weather data to be displayed on a chart
    private fun displayData() {
        if (dataMap.isEmpty()) {
            weatherChart.setNoDataText("No weather data found")
            return
        }

        val list = dataMap[displayedYears.elementAt(displayedYearIndex)] ?: return

        val pointsList = mutableListOf<Entry>()

        var d = 1f
        for (data in list) {
            when (displayOption) {
                DisplayOptions.TEMPERATURE -> pointsList.add(Entry(d, data.temperature.toFloat()))
                DisplayOptions.PRESSURE -> pointsList.add(Entry(d, data.pressure.toFloat()))
                DisplayOptions.HUMIDITY -> pointsList.add(Entry(d, data.humidity.toFloat()))
            }
            d++
        }

        val label = when (displayOption) {
            DisplayOptions.TEMPERATURE -> "Temperature from ${list.first().getDateAsString()} to ${list.last().getDateAsString()}"
            DisplayOptions.PRESSURE -> "Pressure from ${list.first().getDateAsString()} to ${list.last().getDateAsString()}"
            DisplayOptions.HUMIDITY -> "Humidity from ${list.first().getDateAsString()} to ${list.last().getDateAsString()}"
        }

        val dataSet = LineDataSet(pointsList, label)
        dataSet.highLightColor = ColorTemplate.rgb("#000056")
        dataSet.color = ColorTemplate.rgb("#000056")
        dataSet.setDrawValues(false)
        dataSet.setDrawCircles(false)
        weatherChart.legend.textColor = ColorTemplate.rgb("#000056")
        weatherChart.legend.textSize = 16f

        weatherChart.data = LineData(arrayListOf(dataSet) as List<ILineDataSet>?)

        weatherChart.invalidate()
    }

    //does task 4
    private fun initHardPercentagePeriods() {
        //mid = (high + low) / 2
        //perc = |high - mid| / mid
        //perc <= 1.43% (0.0143) for pressure and 5.673% (0.05673) for temperature

        val t0 = 0.05673
        val p0 = 0.0143

        var start = -1
        var end = -1
        var low: WeatherData? = null
        var high: WeatherData? = null
        var detected = false

        for (i in 0 until rootList.size) {
            if (start == -1) {
                start = i
                low = rootList[i]
            } else if (end == -1){
                end = i
                if (rootList[i].temperature < low!!.temperature) {
                    high = low
                    low = rootList[i]
                } else high = rootList[i]
                continue
            } else {
                end = i
            }

            if (low != null && high != null && end != -1) {
                if (rootList[i].temperature < low.temperature) {
                    low = rootList[i]

                    val mt = (high.temperature + low.temperature) / 2.0
                    val mp = (high.pressure + low.pressure) / 2.0

                    val pt = abs((high.temperature - mt) / mt)
                    val pp = abs((high.pressure - mp) / mp)

                    //Log.d("TAG", "$i | $start - $end | ${low.temperature} ${high.temperature} | $pt $pp")

                    if (pt <= t0 && pp <= p0) {
                        detected = true
                    } else {
                        if (detected) {
                            //Log.d("TAG", "OUT $start: ${rootList[start].temperature} | ${end - 1}: ${rootList[end - 1].temperature}")
                            //Log.d("TAG", "OUT ${rootList.filter { it.getDate().getDayOfYear() - 1 in start until end }.map { it.temperature }}")
                            hardPeriodList.add(rootList[start] to rootList[end - 1])
                        }
                        low = null
                        high = null
                        start = -1
                        end = -1
                        detected = false
                    }
                } else if (rootList[i].temperature > high.temperature) {
                    high = rootList[i]

                    val mt = (high.temperature + low.temperature) / 2.0
                    val mp = (high.pressure + low.pressure) / 2.0

                    val pt = abs((high.temperature - mt) / mt)
                    val pp = abs((high.pressure - mp) / mp)

                    //Log.d("TAG", "$i | $start - $end | ${low.temperature} ${high.temperature} | $pt $pp")

                    if (pt <= t0 && pp <= p0) {
                        detected = true
                    } else {
                        if (detected) {
                            //Log.d("TAG", "OUT $start: ${rootList[start].temperature} | ${end - 1}: ${rootList[end - 1].temperature}")
                            //Log.d("TAG", "OUT ${rootList.filter { it.getDate().getDayOfYear() - 1 in start until end }.map { it.temperature }}")
                            hardPeriodList.add(rootList[start] to rootList[end - 1])
                        }
                        low = null
                        high = null
                        start = -1
                        end = -1
                        detected = false
                    }
                }
            }
        }

        if (hardPeriodList.size != 0) {
            //hardPeriodList = hardPeriodList.filter { it.first.year == 2018 && it.second.year == 2018 }.toMutableList()
            periodTextDecription.text = periodTextDecription.text.toString().dropLast(1) + " (Size: ${hardPeriodList.size}):"

            val first = "${hardPeriodList[0].first.temperature} °C | ${hardPeriodList[0].first.pressure} mm | ${hardPeriodList[0].first.getDate()} (${hardPeriodList[0].first.getDate().getDayOfYear()})"
            val second = "${hardPeriodList[0].second.temperature} °C | ${hardPeriodList[0].second.pressure} mm | ${hardPeriodList[0].second.getDate()} (${hardPeriodList[0].second.getDate().getDayOfYear()})"
            periodText.text = "$first\n—\n$second"
        }
    }
}