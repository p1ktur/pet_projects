package com.renatsolocorp.dairy

import android.app.*
import android.content.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.forEachIndexed
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import kotlinx.android.synthetic.main.activity_day.*
import kotlinx.android.synthetic.main.activity_week.*
import kotlinx.android.synthetic.main.lesson_layout.view.lesson_checkbox
import kotlinx.android.synthetic.main.lesson_layout.view.lesson_homework
import kotlinx.android.synthetic.main.lesson_layout.view.lesson_name
import kotlinx.android.synthetic.main.lesson_layout.view.lesson_number
import kotlinx.android.synthetic.main.lesson_layout_additional.view.*
import com.renatsolocorp.dairy.lessons_logic.*
import com.renatsolocorp.dairy.lessons_logic.day_activity_logic.*
import com.renatsolocorp.dairy.notifications_logic.*
import java.util.*

var nightMode = false
var currentAppTheme = 0
var dialogAppTheme = 0

lateinit var lessonsList: MutableList<MutableList<String>>

const val selectedWeekWeek = "SELECTED_WEEK_WEEK"
const val selectedWeekYear = "SELECTED_WEEK_YEAR"

var intentOpenDayNumber = 0
const val intentOpenDayId = "INTENT_OPEN_DAY_ID"
const val numberAnimation = "NUMBER_ANIMATION"
var selectedLesson: Int? = null
var contentText = ""
var notificationId = 0
lateinit var notificationManager: NotificationManager
lateinit var notificationChannel: NotificationChannel
lateinit var builder: Notification.Builder
const val channelId = "my.app.dairyv11"
private const val description = "Homework"

lateinit var notificationToast: Toast

lateinit var context: Context
lateinit var alarmManager: AlarmManager
lateinit var notificationServiceIntent: Intent

var dayOfWeekNames = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
var selectedDay: LinearLayout? = null
var daysList = mutableListOf<LinearLayout>()
var daysLayouts = mutableListOf<LinearLayout>()
var daysNamesLayouts = mutableListOf<LinearLayout>()
var daysNamesViewsList = mutableListOf<TextView>()
var scrollsList = mutableListOf<ScrollView>()
var lessonsViewsList = mutableListOf<LinearLayout>()

lateinit var currentWeek: Week
lateinit var selectedWeek: Week

var scheduleOption = false

var todayYear = Calendar.getInstance().get(Calendar.YEAR)
var todayMonth = Calendar.getInstance().get(Calendar.MONTH)
var todayDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

lateinit var urgentHomework: RecyclerView
var urgentHomeworkList = mutableListOf<String>()

lateinit var weekReceiver: Receiver

lateinit var globalPreference: Preferences

lateinit var identifierRed: Drawable
lateinit var identifierGreen: Drawable
lateinit var noHomeworkYet: TextView
lateinit var lessonNumberAnimation: AlphaAnimation

lateinit var drawerToggle: ActionBarDrawerToggle

lateinit var weekText: TextView
lateinit var weekParams: TextView
lateinit var todayParams: TextView
lateinit var currentWeekText: TextView

lateinit var dayLayout: RelativeLayout
lateinit var editButton: ImageButton
lateinit var notificationButton: ImageButton
lateinit var pickerLayout: LinearLayout
lateinit var dayBackButton: ImageButton
lateinit var dayName: TextView
lateinit var clearDayButton: ImageButton
lateinit var returnButton: ImageButton

lateinit var lessonContainter: LinearLayout

class WeekActivity : AppCompatActivity(), UrgentHomeworkAdapter.OnUrgentClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("sd", "onCreate")

        globalPreference = Preferences(this)

        nightMode = globalPreference.getNightmode()
        updateTheme(this)
        setContentView(R.layout.activity_week)

        initWeek()

        urgentHomework = urgent_homework_list
        urgentHomework.layoutManager = LinearLayoutManager(this)
        urgentHomework.adapter = UrgentHomeworkAdapter(urgentHomeworkList, this, this)
        val itemDivider = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        itemDivider.setDrawable(getDrawable(R.drawable.urgent_divider_horizontal)!!)
        urgentHomework.addItemDecoration(itemDivider)

        val serviceIntent = Intent(context, NotificationIntentService::class.java)
        notificationServiceIntent = serviceIntent
        startService(serviceIntent)

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT){
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                (urgentHomework.adapter as UrgentHomeworkAdapter).removeItem(viewHolder.adapterPosition)
                stopService(serviceIntent)
                startService(serviceIntent)
            }
        }
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(urgentHomework)

        weekReceiver = Receiver()
        registerReceiver(weekReceiver, IntentFilter("UpdateUrgents"))

        val notToast = Toast.makeText(this, "Click on the Number to select a lesson", Toast.LENGTH_LONG)
        notToast.setGravity(Gravity.BOTTOM, 0, 220)
        notificationToast = notToast

        drawerToggle = ActionBarDrawerToggle(this, main_drawer, R.string.drawerOpen, R.string.drawerClose)
        main_drawer.setDrawerListener(drawerToggle)
        drawerToggle.syncState()

        drawer_nav_view.menu.findItem(R.id.schedule_option).title = if (scheduleOption) getString(R.string.schedule_option_on) else getString(R.string.schedule_option_off)
        drawer_nav_view.menu.findItem(R.id.nightmode_option).title = if (nightMode) getString(R.string.nightmode_on) else getString(R.string.nightmode_off)
        drawer_nav_view.menu.findItem(R.id.nightmode_option).icon = if (nightMode) getDrawable(R.drawable.ic_baseline_brightness_3_24) else getDrawable(R.drawable.ic_baseline_brightness_5_24)
        drawer_nav_view.menu.forEachIndexed { index, item ->
            item.title
        }

        drawer_nav_view.setNavigationItemSelectedListener {
            when (it.itemId){
                R.id.nightmode_option -> {
                    nightMode = !nightMode
                    if (nightMode){
                        drawer_nav_view.menu.findItem(R.id.nightmode_option).title = getString(R.string.nightmode_on)
                        drawer_nav_view.menu.findItem(R.id.nightmode_option).icon = getDrawable(R.drawable.ic_baseline_brightness_3_24)
                    }else{
                        drawer_nav_view.menu.findItem(R.id.nightmode_option).title = getString(R.string.nightmode_off)
                        drawer_nav_view.menu.findItem(R.id.nightmode_option).icon = getDrawable(R.drawable.ic_baseline_brightness_5_24)
                    }
                    globalPreference.saveNightmodeOption(nightMode)

                    restart()
                }
                R.id.schedule_option -> {
                    scheduleOption = !scheduleOption
                    if (scheduleOption){
                        drawer_nav_view.menu.findItem(R.id.schedule_option).title = getString(R.string.schedule_option_on)
                    }else{
                        drawer_nav_view.menu.findItem(R.id.schedule_option).title = getString(R.string.schedule_option_off)
                    }
                    globalPreference.saveScheduleOption(scheduleOption)
                }
                R.id.clear_current_week -> {
                    val builder = AlertDialog.Builder(this, dialogAppTheme)
                    builder.setTitle("Warning!")
                    builder.setMessage("It will clear all your current week schedules and homework. Are you sure you want to proceed?")
                    builder.setPositiveButton(Html.fromHtml("Yes")) { dialogInterface: DialogInterface, i: Int ->
                        lessonsList = globalPreference.getDefault()
                        for (i in daysList.indices){
                            setLessons(daysList[i])
                        }
                        (urgentHomework.adapter as UrgentHomeworkAdapter).clearSelectedWeekUrgents()
                        globalPreference.saveLessons(lessonsList, selectedWeek)
                        globalPreference.saveNotifications(urgentsToString(urgentHomeworkList))
                        stopService(serviceIntent)
                        startService(serviceIntent)
                    }
                    builder.setNegativeButton(Html.fromHtml("No")) { dialogInterface: DialogInterface, i: Int -> }
                    builder.setCancelable(true)
                    builder.show()
                }
                R.id.clear_cache -> {
                    val builder = AlertDialog.Builder(this, dialogAppTheme)
                    builder.setTitle("Warning!")
                    builder.setMessage("It will clear all your data: homework, schedules and etc. Are you sure you want to proceed?")
                    builder.setPositiveButton(Html.fromHtml("Yes")) { dialogInterface: DialogInterface, i: Int ->
                        globalPreference.clearPreferences()

                        scheduleOption = false
                        globalPreference.saveNightmodeOption(nightMode)

                        lessonsList = globalPreference.getDefault()
                        for (i in daysList.indices){
                            setLessons(daysList[i])
                        }
                        (urgentHomework.adapter as UrgentHomeworkAdapter).clearAllUrgents()
                        globalPreference.saveNotifications(urgentsToString(urgentHomeworkList))
                        stopService(serviceIntent)
                        startService(serviceIntent)
                    }
                    builder.setNegativeButton(Html.fromHtml("No")) { dialogInterface: DialogInterface, i: Int -> }
                    builder.setCancelable(true)
                    builder.show()
                }
                R.id.clear_all_urgents -> {
                    val builder = AlertDialog.Builder(this, dialogAppTheme)
                    builder.setTitle("Warning!")
                    builder.setMessage("Are you sure?")
                    builder.setPositiveButton(Html.fromHtml("Yes")) { dialogInterface: DialogInterface, i: Int ->
                        (urgentHomework.adapter as UrgentHomeworkAdapter).clearAllUrgents()
                        stopService(serviceIntent)
                        startService(serviceIntent)
                    }
                    builder.setNegativeButton(Html.fromHtml("No")) { dialogInterface: DialogInterface, i: Int -> }
                    builder.setCancelable(true)
                    builder.show()
                }
            }

            true
        }

        drawer_button.setOnClickListener {
            main_drawer.openDrawer(GravityCompat.START)
            drawerToggle.syncState()
        }

        week_next.setOnClickListener {
            nextWeek()
        }

        week_back.setOnClickListener {
            prevWeek()
        }

        day_back_button.setOnClickListener {
            dayBackOnClick(it)
        }

        return_button.setOnClickListener {
            checkForCurrentWeek()
            changeWeek(compareWeeks(week_text.text.split(" ")[1].toInt(), getWeekTextYear(week_params.text.toString()).toInt()),
                week_text.text.split(" ")[1].toInt() to getWeekTextYear(week_params.text.toString()).toInt())
            hideButton(this, returnButton)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        Log.d("sd", "onPostCreate")
        
        for (i in daysList.indices){
            createDaysList(daysList, i)
            daysList[i].setOnClickListener {
                openDay(i, "open")
            }
        }
    }

    private fun restart() {
        unregisterReceiver(weekReceiver)
        finish()
        clearAllGlobalVars()
        Log.d("sd", "RESTART PUTTING ${selectedWeek.week} ${selectedWeek.year}")
        intent.putExtra(selectedWeekWeek, week_text.text.split(" ")[1].toInt())
        intent.putExtra(selectedWeekYear, getWeekTextYear(week_params.text.toString()).toInt())
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        Log.d("sd", "onStart")
        
        registerReceiver(weekReceiver, IntentFilter("UpdateUrgents"))
    }

    override fun onRestart() {
        super.onRestart()
        Log.d("sd", "onRestart")
    }

    override fun onPause() {
        super.onPause()
        Log.d("sd", "onPause")
        
        (urgentHomework.adapter as UrgentHomeworkAdapter).checkLessonsForUrgentChanges()

        if (selectedDay != null){
            recordLessons(lessonsViewsList, globalPreference)

            globalPreference.saveLessons(lessonsList, selectedWeek)
            globalPreference.saveNotifications(urgentsToString(urgentHomeworkList))
        }
        Log.d("sd", "week onPaused")
    }

    override fun onStop() {
        super.onStop()
        Log.d("sd", "onStop")

        if (bigLayoutsId.size < 42) unregisterReceiver(weekReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("sd", "onDestroy")
    }

    //TODO normal opening when pressing on notification

    override fun onNewIntent(intent: Intent?) {
        var position = 69
        if (intent!!.getIntExtra(unSortedUrgentPosition, 69) != 69){
            //position = intent.getIntExtra(unSortedUrgentPosition, 69)
        }
        Log.d("sd", "newIntent recieved position = $position")
        //if (position != 69) changeWeek(compareWeeks(urgentHomeworkList[position].split(" ")[1].toInt(), urgentHomeworkList[position].split(separator)[0].split(smallSeparator)[2].toInt()),
        //    urgentHomeworkList[position].split(" ")[1].toInt() to urgentHomeworkList[position].split(separator)[0].split(smallSeparator)[2].toInt())
        
        if (position != 69){
            if (selectedDay != null) closeDay()
            //openDay(dayToIterator(dayOfWeekNames[intent.getIntExtra(intentOpenDayId, 69)]), intent.getStringExtra(numberAnimation)!!)
        }

        super.onNewIntent(intent)
    }

    fun drawerBackButtonOnClick(view: View){
        main_drawer.closeDrawer(GravityCompat.START)
        drawerToggle.syncState()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item)){
            true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemClick(position: Int) {
        changeWeek(compareWeeks(urgentHomeworkList[position].split(" ")[1].toInt(), urgentHomeworkList[position].split(separator)[0].split(smallSeparator)[2].toInt()),
            urgentHomeworkList[position].split(" ")[1].toInt() to urgentHomeworkList[position].split(separator)[0].split(smallSeparator)[2].toInt())

        for (i in dayOfWeekNames.indices){
            if (urgentHomeworkList[position].contains(dayOfWeekNames[i])){
                openDay(dayToIterator(dayOfWeekNames[i]), position.toString())
            }
        }
    }

    fun clearDayOnClick(view: View){
        val builder = AlertDialog.Builder(this, dialogAppTheme)
        builder.setTitle("Warning!")
        builder.setMessage("It will clear current day schedule and homework. Are you sure you want to proceed?")
        builder.setPositiveButton(Html.fromHtml("Yes")) { dialogInterface: DialogInterface, i: Int ->
            lessonNumberAnimation.cancel()
            notificationButtonToDefault(this)
            editButtonToDefault(this)
            lessonsViewsList.forEach {
                it.lesson_name.setText("")
                it.lesson_homework.setText("")
                it.lesson_checkbox.isChecked = false
            }
            lessonsList[getSelectedDayId()] = globalPreference.getDefault()[getSelectedDayId()]
            (urgentHomework.adapter as UrgentHomeworkAdapter).clearSelectedDayUrgents(dayOfWeekNames[getSelectedDayId()])
            (urgentHomework.adapter as UrgentHomeworkAdapter).checkLessonsForUrgentChanges()
            globalPreference.saveLessons(lessonsList, selectedWeek)
            globalPreference.saveNotifications(urgentsToString(urgentHomeworkList))
        }
        builder.setNegativeButton(Html.fromHtml("No")) { dialogInterface: DialogInterface, i: Int -> }
        builder.setCancelable(true)
        builder.show()
    }

    fun dayBackOnClick(view: View){
        if (editMode){
            editButtonToDefault(this)
        }else if (notificationMode){
            notificationButtonToDefault(this)
        }else if(day_layout.visibility == View.VISIBLE){
            globalPreference.saveLessons(lessonsList, selectedWeek)
            globalPreference.saveNotifications(urgentsToString(urgentHomeworkList))
            closeDay()
        }else{
            globalPreference.saveLessons(lessonsList, selectedWeek)
            globalPreference.saveNotifications(urgentsToString(urgentHomeworkList))
            moveTaskToBack(true)
        }
    }

    fun editButtonOnClick(view: View){
        if(notificationMode) notificationButtonToDefault(this)
        editMode = !editMode

        if (!editMode){
            editButtonToDefault(this)

            recordLessons(lessonsViewsList, globalPreference)

            (urgentHomework.adapter as UrgentHomeworkAdapter).checkLessonsForUrgentChanges()
            stopService(notificationServiceIntent)
            startService(notificationServiceIntent)
        }else{
            day_back_button.setImageDrawable(getDrawable(R.drawable.ic_clear_black_36dp))
            hideButton(this, editButton)
            hideButton(this, notificationButton)
            hideButton(this, clearDayButton)
            dayName.text = "Editing  "
        }

        lessonsViewsList.forEach {
            it.lesson_name.isFocusable = editMode
            it.lesson_name.isFocusableInTouchMode = editMode
            it.lesson_name.isEnabled = editMode

            it.lesson_homework.isFocusable = editMode
            it.lesson_homework.isFocusableInTouchMode = editMode
            it.lesson_homework.isEnabled = editMode
        }
    }

    fun notificationButtonOnClick(view: View){
        if (editMode) editButtonToDefault(this)
        notificationMode = !notificationMode

        if (notificationMode){
            notificationToast.show()

            dayBackButton.setImageDrawable(getDrawable(R.drawable.ic_clear_black_36dp))
            //lessonContainter.setBackgroundColor(colorSelector)

            hideButton(this, editButton)
            hideButton(this, notificationButton)
            hideButton(this, clearDayButton)

            dayName.text = "Create notification  "

            lessonsViewsList.forEach {
                it.setBackgroundColor(colorSelector)
                it.lesson_number.minWidth = 160
                it.lesson_number.textSize = 26f
                it.lesson_number.setHintTextColor(getColor(R.color.colorBlack))
                it.lesson_homework.setText(it.lesson_homework.text.toString().shorten())


                if(it == lessonsViewsList[lessonsViewsList.size-1]){
                    it.lesson_name_static.setOnClickListener {
                        notificationToast.cancel()
                        initDateTimePickers(date_picker, hour_picker, minute_picker)
                        if (pickerLayout.visibility == View.GONE && notificationMode){
                            pickerLayout.visibility = View.VISIBLE
                            val animation = AnimationUtils.loadAnimation(this, R.anim.time_picker_appear)
                            pickerLayout.startAnimation(animation)
                        } else if (pickerLayout.visibility == View.VISIBLE && notificationMode){
                            val animation = AnimationUtils.loadAnimation(this, R.anim.time_picker_disappear)
                            pickerLayout.startAnimation(animation)
                            pickerLayout.postOnAnimation {
                                pickerLayout.visibility = View.GONE
                            }
                        }
                        selectedLesson = 8
                    }
                }else{
                    it.lesson_number.setOnClickListener {
                        notificationToast.cancel()
                        initDateTimePickers(date_picker, hour_picker, minute_picker)
                        if (pickerLayout.visibility == View.GONE && notificationMode){
                            pickerLayout.visibility = View.VISIBLE
                            val animation = AnimationUtils.loadAnimation(this, R.anim.time_picker_appear)
                            pickerLayout.startAnimation(animation)
                        } else if (pickerLayout.visibility == View.VISIBLE && notificationMode){
                            val animation = AnimationUtils.loadAnimation(this, R.anim.time_picker_disappear)
                            pickerLayout.startAnimation(animation)
                            pickerLayout.postOnAnimation {
                                pickerLayout.visibility = View.GONE
                            }
                        }
                        selectedLesson = (it as TextView).text.toString().dropLast(1).toInt()
                    }
                }
            }
        } else {
            notificationButtonToDefault(this)
        }
    }

    fun pickerOkButtonOnClick(view: View){
        if (notificationMode){
            notificationToast.cancel()

            val animation = AnimationUtils.loadAnimation(this, R.anim.time_picker_disappear)
            pickerLayout.startAnimation(animation)
            pickerLayout.postOnAnimation {
                pickerLayout.visibility = View.GONE
            }

            notificationButtonToDefault(this)
            showButton(this, editButton)

            notificationMode = false

            lessonsViewsList.forEach {
                if (it.lesson_number.text.toString().dropLast(1).toInt() == selectedLesson){
                    contentText = "Week ${selectedWeek.getWeek()} ${dayOfWeekNames[getSelectedDayId()]}${setDate(selectedWeek.weekRange.first+ getSelectedDayId())} $smallSeparator$selectedLesson.${it.lesson_name.text}: ${it.lesson_homework.text}"
                    intentOpenDayNumber = getSelectedDayId()
                }
            }

            val calendarTime = Calendar.getInstance()
            calendarTime.set(Calendar.MONTH,  date_picker.displayedValues[date_picker.value].split(" ")[2].monthIntoNumber())
            calendarTime.set(Calendar.DAY_OF_MONTH,  date_picker.displayedValues[date_picker.value].split(" ")[1].toInt())
            calendarTime.set(Calendar.YEAR,  emptyCalendar.get(Calendar.YEAR))
            calendarTime.set(Calendar.HOUR_OF_DAY, hour_picker.value-1)
            calendarTime.set(Calendar.MINUTE, (minute_picker.value-1)*5)
            calendarTime.set(Calendar.SECOND, 0)
            val textToAdd = contentText + smallSeparator + selectedWeek.year + separator + calendarTime.timeInMillis
            Log.d("sd", "texttoadd $textToAdd ${emptyCalendar.get(Calendar.YEAR)}")
            (urgentHomework.adapter as UrgentHomeworkAdapter).addItem(textToAdd)
            globalPreference.addNotification(textToAdd)
            (urgentHomework.adapter as UrgentHomeworkAdapter).checkLessonsForUrgentChanges()

            stopService(notificationServiceIntent)
            startService(notificationServiceIntent)

            selectedLesson = null
        }
    }

    fun pickerCancelButtonOnClick(view: View){
        notificationToast.cancel()
        val animation = AnimationUtils.loadAnimation(this, R.anim.time_picker_disappear)
        pickerLayout.startAnimation(animation)
        pickerLayout.postOnAnimation {
            pickerLayout.visibility = View.GONE
        }
        selectedLesson = null
    }

    override fun onBackPressed() {
        if (editMode){
            editButtonToDefault(this)
        }else if (notificationMode){
            notificationButtonToDefault(this)
        }else if(day_layout.visibility == View.VISIBLE){
            globalPreference.saveLessons(lessonsList, selectedWeek)
            globalPreference.saveNotifications(urgentsToString(urgentHomeworkList))
            closeDay()
        }else{
            globalPreference.saveLessons(lessonsList, selectedWeek)
            globalPreference.saveNotifications(urgentsToString(urgentHomeworkList))
            moveTaskToBack(true)
        }
    }

    fun openDay(iterator: Int, option: String){
        if (selectedDay == null){
            Log.d("sd", "day opening starts day")
            selectedDay = daysList[iterator]

            //val newIntent = Intent(this, DayActivity::class.java)
            //newIntent.putExtra(intentOpenDayId, iterator)
            //if (option != "open") newIntent.putExtra(numberAnimation, option)
            //startActivity(newIntent)

            day_name.text = daysNamesViewsList[iterator].text

            setViewsLessons(lessonsViewsList)

            animateNumber(lessonsViewsList, option)

            lockDayButtons("unlock")
            day_layout.visibility = View.VISIBLE
            val animation = AnimationUtils.loadAnimation(this, R.anim.day_appear)
            day_layout.startAnimation(animation)
            hideButton(this, drawer_button)
            hideButton(this, returnButton)
            hideButton(this, currentWeekText)
            showButton(this, dayBackButton)
            showButton(this, clearDayButton)
            showButton(this, day_name)
            main_drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }
    }

    fun closeDay(){
        dayBackButton.isClickable = false
        lessonNumberAnimation.cancel()
        recordLessons(lessonsViewsList, globalPreference)
        (urgentHomework.adapter as UrgentHomeworkAdapter).checkLessonsForUrgentChanges()

        setLessons(selectedDay!!)
        selectedDay = null

        lockDayButtons("lock")
        val animation = AnimationUtils.loadAnimation(this, R.anim.day_disappear)
        day_layout.startAnimation(animation)
        day_layout.postOnAnimation {
            day_layout.visibility = View.GONE
        }
        showButton(this, drawer_button)
        if (selectedWeek.week != currentWeek.week) showButton(this, returnButton)
        if (selectedWeek.week == currentWeek.week) showButton(this, currentWeekText)
        hideButton(this, dayBackButton)
        hideButton(this, clearDayButton)
        hideButton(this, day_name)
        main_drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }

    fun initWeek(){

        lessonNumberAnimation = AlphaAnimation(0f, 1f).also{
            it.duration = 400
            it.repeatCount = 10
            it.repeatMode = Animation.REVERSE
        }

        val tempCalendar = Calendar.getInstance()
        if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == 1 && Calendar.getInstance().firstDayOfWeek == Calendar.SUNDAY){
            tempCalendar.setWeekDate(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.WEEK_OF_YEAR) - 1, Calendar.MONDAY)
            currentWeek = Week(Calendar.getInstance().get(Calendar.WEEK_OF_YEAR) - 1, tempCalendar.get(Calendar.MONTH), Calendar.getInstance().get(Calendar.YEAR), getCurrentWeekRange())
        } else {
            tempCalendar.setWeekDate(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.WEEK_OF_YEAR), Calendar.MONDAY)
            currentWeek = Week(Calendar.getInstance().get(Calendar.WEEK_OF_YEAR), tempCalendar.get(Calendar.MONTH), Calendar.getInstance().get(Calendar.YEAR), getCurrentWeekRange())
        }
        currentWeek.weekRange = getWeekRange(currentWeek)
        lessonsList = globalPreference.getLessons(currentWeek)
        currentWeek.setText(week_text, week_params, today_params)
        Log.d("sd", "WEEKCODE ${getWeekCode(currentWeek)}")
        Log.d("sd", "${globalPreference.getLessons(currentWeek)}")

        //clearing sharedpreferences
        //globalPreference.clearPreferences()

        selectedWeek = currentWeek
        val intentsWeek = intent.getIntExtra(selectedWeekWeek, selectedWeek.week)
        val intentsYear = intent.getIntExtra(selectedWeekYear, selectedWeek.year)
        Log.d("sd", "$intentsWeek $intentsYear ${selectedWeek.week} ${selectedWeek.year}")
        if (intentsWeek != currentWeek.week || intentsYear != currentWeek.year){
            changeWeek(compareWeeks(intentsWeek, intentsYear),intentsWeek to intentsYear)
        }
        selectedWeek.setText(week_text, week_params, today_params)

        if (checkForListErrors(globalPreference.getLessons(selectedWeek))){
            lessonsList = globalPreference.getLessons(selectedWeek)
            //lessonsList = globalPreference.getDefault()
        } else {
            lessonsList = globalPreference.getDefault()
        }
        Log.d("sd", "$lessonsList")

        noHomeworkYet = no_homework_yet
        identifierRed = getDrawable(R.drawable.notified_identifier_red)!!
        identifierGreen = getDrawable(R.drawable.notified_identifier_green)!!

        context = this
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.enableVibration(true)
            notificationChannel.vibrationPattern = longArrayOf(800L, 800L)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        scheduleOption = globalPreference.getScheduleOption()

        if (globalPreference.getNotifications() != "") {
            urgentHomeworkList = globalPreference.getNotifications().split(megaSeparator).toMutableList()
            noHomeworkYet.visibility = View.GONE
        }else noHomeworkYet.visibility = View.VISIBLE

        daysLayouts.add(mtw_layout)
        daysLayouts.add(tfs_layout)

        daysNamesLayouts.add(mtw_names)
        daysNamesLayouts.add(tfs_names)

        daysList.add(monday_layout)
        daysList.add(tuesday_layout)
        daysList.add(wednesday_layout)
        daysList.add(thursday_layout)
        daysList.add(friday_layout)
        daysList.add(saturday_layout)

        Log.d("sd", "DAYSLIST SIZE ${daysList.size}")

        daysNamesViewsList.add(monday_name)
        daysNamesViewsList.add(tuesday_name)
        daysNamesViewsList.add(wednesday_name)
        daysNamesViewsList.add(thursday_name)
        daysNamesViewsList.add(friday_name)
        daysNamesViewsList.add(saturday_name)

        scrollsList.add(monday_scroll)
        scrollsList.add(tuesday_scroll)
        scrollsList.add(wednesday_scroll)
        scrollsList.add(thursday_scroll)
        scrollsList.add(friday_scroll)
        scrollsList.add(saturday_scroll)

        lessonsViewsList.add(lesson1)
        lessonsViewsList.add(lesson2)
        lessonsViewsList.add(lesson3)
        lessonsViewsList.add(lesson4)
        lessonsViewsList.add(lesson5)
        lessonsViewsList.add(lesson6)
        lessonsViewsList.add(lesson7)
        lessonsViewsList.add(additional_lesson)

//        for (i in daysList.indices){
//            createDaysList(daysList, i)
//            daysList[i].setOnClickListener {
//                openDay(i, "open")
//            }
//        }

        for (i in daysNamesViewsList.indices){
            daysNamesViewsList[i].text = if (i == 2 && setDate(selectedWeek.weekRange.first+i).length > 5) "Wed." + setDate(selectedWeek.weekRange.first+i) else dayOfWeekNames[i] + setDate(selectedWeek.weekRange.first+i)
        }

        arrowBack = getDrawable(R.drawable.ic_baseline_arrow_back_36)!!
        timePickerDisappear = AnimationUtils.loadAnimation(this, R.anim.time_picker_disappear)
        colorSelector = getAttributeColor(this, R.attr.lessonSelectorColor)
        colorPrimary = getAttributeColor(this, R.attr.colorPrimary)
        colorPrimaryDark = getAttributeColor(this, R.attr.colorPrimaryDark)

        day_name.text = ""

        weekText = week_text
        weekParams = week_params
        todayParams = today_params
        currentWeekText = current_week_text

        lessonContainter = lesson_list

        dayLayout = day_layout
        editButton = edit_button
        notificationButton = notification_setting_button
        pickerLayout = picker_layout
        dayBackButton = week_back_button
        dayName = day_name
        clearDayButton = clear_day_button
        returnButton = return_button

        if (selectedWeek.week != currentWeek.week) showButton(this, returnButton)
        if (selectedWeek.week == currentWeek.week) showButton(this, currentWeekText)

        checkGooglePlayServices()
    }

    private fun checkGooglePlayServices(): Boolean {
        // 1
        val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        // 2
        return if (status != ConnectionResult.SUCCESS) {
            Log.d("sd", "GOOGLE ERROR")
            // ask user to update google play services and manage the error.
            false
        } else {
            // 3
            Log.d("sd", "GOOGLE SERVICES UPDATED")
            true
        }
    }
}
