package my.app.weatherapp.main

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import my.app.weatherapp.R
import my.app.weatherapp.graphfragmentlogic.DisplayDataChangeListener
import my.app.weatherapp.graphfragmentlogic.GraphViewFragment
import my.app.weatherapp.tablefragmentlogic.TableViewFragment

//main activity class
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, DisplayDataChangeListener {
    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    lateinit var navigationDrawer: DrawerLayout
    lateinit var navigationView: NavigationView

    lateinit var fragmentLayout: FrameLayout

    lateinit var dov: DisplayOptionsVault
    private var openedFragment = FragmentOptions.TABLE
    private var displayOption = 0
    private var displayedYearIndex = 0

    enum class FragmentOptions {
        TABLE, GRAPH
    }

    //activity staff
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dov = DisplayOptionsVault(this, MODE_PRIVATE)
        retrieveData()

        navigationDrawer = findViewById(R.id.navigationDrawer)
        actionBarDrawerToggle = ActionBarDrawerToggle(this, navigationDrawer, R.string.navigation_open, R.string.navigation_close)
        navigationDrawer.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        navigationView = findViewById(R.id.navigationView)
        navigationView.setNavigationItemSelectedListener(this)

        fragmentLayout = findViewById(R.id.fragmentLayout)

        when (openedFragment) {
            FragmentOptions.TABLE -> supportFragmentManager.beginTransaction().replace(R.id.fragmentLayout, TableViewFragment()).commit()
            FragmentOptions.GRAPH -> supportFragmentManager.beginTransaction().replace(R.id.fragmentLayout, GraphViewFragment.newInstance(displayOption, displayedYearIndex)).commit()
        }
    }

    //activity staff
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }

    //activity staff
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.tableView -> {
                supportFragmentManager.beginTransaction().replace(R.id.fragmentLayout, TableViewFragment().apply { retainInstance }).commit()
                openedFragment = FragmentOptions.TABLE
                dov.saveOpenedFragment(0)
            }
            R.id.graphView -> {
                supportFragmentManager.beginTransaction().replace(R.id.fragmentLayout, GraphViewFragment()).commit()
                openedFragment = FragmentOptions.GRAPH
                dov.saveOpenedFragment(1)
            }
        }
        navigationDrawer.closeDrawer(GravityCompat.START)
        return true
    }

    //implementation of interface listener
    override fun onDataChange(option: Int, yearIndex: Int) {
        displayOption = option
        displayedYearIndex = yearIndex

        overwriteData()
    }

    //overwrites data in the file
    private fun overwriteData() {
        dov.saveDisplayOption(displayOption)
        dov.saveDisplayedYearIndex(displayedYearIndex)
    }

    //fetches data from file
    private fun retrieveData() {
        displayOption = dov.getDisplayOption()
        displayedYearIndex = dov.getDisplayedYearIndex()

        openedFragment = when (dov.getOpenedFragment()) {
            0 -> FragmentOptions.TABLE
            else -> FragmentOptions.GRAPH
        }
    }
}