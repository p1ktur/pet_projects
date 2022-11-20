package my.app.weatherapp.tablefragmentlogic

import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import my.app.weatherapp.R
import my.app.weatherapp.commonlogic.*
import java.io.*
import java.util.Calendar

//Table view fragment class
class TableViewFragment : Fragment() {
    private lateinit var tableRecyclerView: RecyclerView
    private lateinit var noDataText: TextView

    private lateinit var generateDataButton: Button
    private lateinit var fetchDataButton: Button
    private lateinit var saveDataButton: Button

    private lateinit var displayHighTempDaysButton: Button
    private lateinit var displayLowPresDaysButton: Button
    private lateinit var displayUnchangedWindsDaysButton: Button
    private lateinit var displayOriginalWeatherButton: Button

    //fragment staff
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_table_view, container, false)
    }

    //fragment staff
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val rootList = mutableListOf<WeatherData>()
        val list = mutableListOf<WeatherData>()
        retrieveFileData(rootList)
        list.addAll(rootList)

        noDataText = view.findViewById(R.id.noDataText)
        if (list.isNotEmpty()) {
            noDataText.visibility = View.GONE
        }

        tableRecyclerView = view.findViewById(R.id.tableRecyclerView)
        tableRecyclerView.adapter = TableRecyclerViewAdapter(list)
        tableRecyclerView.layoutManager = LinearLayoutManager(view.context)
        LinearSnapHelper().attachToRecyclerView(tableRecyclerView)

        generateDataButton = view.findViewById(R.id.generateDataButton)
        fetchDataButton = view.findViewById(R.id.fetchDataButton)
        saveDataButton = view.findViewById(R.id.saveDataButton)

        generateDataButton.setOnClickListener {
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

            //generates new realistic list of weather data
            dateRangePicker.addOnPositiveButtonClickListener {
                rootList.clear()

                val calendar = Calendar.getInstance()

                calendar.timeInMillis = it.first
                val from = Date(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

                calendar.timeInMillis = it.second
                val to = Date(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

                WeatherGenerator.generateListData(rootList, from, to, null)

                list.apply {
                    clear()
                    addAll(rootList)
                }

                noDataText.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE

                (tableRecyclerView.adapter as TableRecyclerViewAdapter).notifyDataSetChanged()
            }
        }

        //button to get file data
        fetchDataButton.setOnClickListener {
            retrieveFileData(rootList)
            list.apply {
                clear()
                addAll(rootList)
            }

            noDataText.visibility = if (list.isEmpty()) View.VISIBLE else {
                showToastMessage(requireContext(), "Data is retrieved.", Toast.LENGTH_SHORT)
                View.GONE
            }

            (tableRecyclerView.adapter as TableRecyclerViewAdapter).notifyDataSetChanged()
        }

        //button to save data to file
        saveDataButton.setOnClickListener {
            writeFileData(list)
        }

        //TODO does part 1 of task 1
        displayHighTempDaysButton = view.findViewById(R.id.displayHighTempDaysButton)
        displayHighTempDaysButton.setOnClickListener {
            try {
                val newList = mutableListOf<WeatherData>()
                val dataMap: HashMap<Int, MutableList<WeatherData>> = HashMap()
                val displayedYears: Set<Int> = rootList.map { it.year }.toSet()

                for (i in displayedYears.indices) {
                    dataMap[displayedYears.elementAt(i)] = rootList.filter { it.year == displayedYears.elementAt(i) }.toMutableList()
                }

                for (k in dataMap.keys) {
                    if (dataMap[k] == null) continue
                    val maxTemp = dataMap[k]!!.maxOf { it.temperature }
                    newList.addAll(dataMap[k]!!.filter { it.temperature == maxTemp })
                }

                list.apply {
                    clear()
                    addAll(newList)
                }
                (tableRecyclerView.adapter as TableRecyclerViewAdapter).notifyDataSetChanged()
            } catch (e: NoSuchElementException) {
                showToastMessage(requireContext(), "No weather data.", Toast.LENGTH_LONG)
            } catch (e: IndexOutOfBoundsException) {
                showToastMessage(requireContext(), "No weather data.", Toast.LENGTH_LONG)
            }

        }

        //TODO does part 2 of task 1
        displayLowPresDaysButton = view.findViewById(R.id.displayLowPresDaysButton)
        displayLowPresDaysButton.setOnClickListener {
            try {
                val newList = mutableListOf<WeatherData>()
                val dataMap: HashMap<Int, MutableList<WeatherData>> = HashMap()
                val displayedYears: Set<Int> = rootList.map { it.year }.toSet()

                for (i in displayedYears.indices) {
                    dataMap[displayedYears.elementAt(i)] = rootList.filter { it.year == displayedYears.elementAt(i) }.toMutableList()
                }

                for (k in dataMap.keys) {
                    if (dataMap[k] == null) continue
                    val minPres = dataMap[k]!!.minOf { it.pressure }
                    newList.addAll(dataMap[k]!!.filter { it.pressure == minPres })
                }

                list.apply {
                    clear()
                    addAll(newList)
                }
                (tableRecyclerView.adapter as TableRecyclerViewAdapter).notifyDataSetChanged()
            } catch (e: NoSuchElementException) {
                showToastMessage(requireContext(), "No weather data.", Toast.LENGTH_LONG)
            } catch (e: IndexOutOfBoundsException) {
                showToastMessage(requireContext(), "No weather data.", Toast.LENGTH_LONG)
            }
        }

        //TODO does task 2
        displayUnchangedWindsDaysButton = view.findViewById(R.id.displayUnchangedWindsDaysButton)
        displayUnchangedWindsDaysButton.setOnClickListener {
            try {
                val newList = mutableListOf<WeatherData>()

                for (i in 0 until rootList.size) {
                    if (i == 0 ) {
                        if (rootList[i].windDirection == rootList[i + 1].windDirection) newList.add(rootList[i])
                    } else if (i == rootList.size - 1){
                        if (rootList[i].windDirection == rootList[i - 1].windDirection) newList.add(rootList[i])
                    } else if (rootList[i].windDirection == rootList[i - 1].windDirection || rootList[i].windDirection == rootList[i + 1].windDirection) {
                        newList.add(rootList[i])
                    }
                }

                list.apply {
                    clear()
                    addAll(newList)
                }
                (tableRecyclerView.adapter as TableRecyclerViewAdapter).notifyDataSetChanged()
            } catch (e: NoSuchElementException) {
                showToastMessage(requireContext(), "No weather data.", Toast.LENGTH_LONG)
            } catch (e: IndexOutOfBoundsException) {
                showToastMessage(requireContext(), "No weather data.", Toast.LENGTH_LONG)
            }
        }

        //button to display original list of weather data
        displayOriginalWeatherButton = view.findViewById(R.id.displayOriginalWeatherButton)
        displayOriginalWeatherButton.setOnClickListener {
            try {
                if (list.size != rootList.size) {
                    list.apply {
                        clear()
                        addAll(rootList)
                    }
                    (tableRecyclerView.adapter as TableRecyclerViewAdapter).notifyDataSetChanged()
                    tableRecyclerView.scrollToPosition(0)
                }
            } catch (e: NoSuchElementException) {
                showToastMessage(requireContext(), "No weather data.", Toast.LENGTH_LONG)
            } catch (e: IndexOutOfBoundsException) {
                showToastMessage(requireContext(), "No weather data.", Toast.LENGTH_LONG)
            }

        }
    }

    //gets data from file
    private fun retrieveFileData(list: MutableList<WeatherData>) {
        val files = requireContext().getExternalFilesDirs(Environment.DIRECTORY_DOCUMENTS)
        val file = File(files[0].absolutePath + "/InputData.txt")

        try {
            val fis = FileInputStream(file)
            val br = BufferedReader(FileReader(file))
            var line: String?
            while (br.readLine().also {line = it} != null) {
                list.add(WeatherData.fromFileString(line!!))
            }
            br.close()
            fis.close()
        } catch (e: FileNotFoundException) {
            showToastMessage(requireContext(), "No input file is found.", Toast.LENGTH_LONG)
        } catch (e: DecipheringLogicException) {
            showToastMessage(requireContext(), "Input file data is corrupted.", Toast.LENGTH_LONG)
        } catch (e: IOException) {
            showToastMessage(requireContext(), "Cannot read data from input file.", Toast.LENGTH_LONG)
        }
    }

    //saves data to file
    private fun writeFileData(list: MutableList<WeatherData>) {
        if (list.isEmpty()) return
        val files = requireContext().getExternalFilesDirs(Environment.DIRECTORY_DOCUMENTS)
        val file = File(files[0].absolutePath + "/InputData.txt")

        try {
            val os = FileOutputStream(file, false)
            for (data in list) {
                val byteArray = "${data.toFileString()}\n".encodeToByteArray()
                os.write(byteArray)
            }
            os.close()
            showToastMessage(requireContext(), "Data is saved.", Toast.LENGTH_SHORT)
        } catch (e: FileNotFoundException) {
            showToastMessage(requireContext(), "No input file is found/created.", Toast.LENGTH_LONG)
        } catch (e: IOException) {
            showToastMessage(requireContext(), "Cannot write data into input file.", Toast.LENGTH_LONG)
        }
    }
}