package com.renatsolocorp.dutyapp.profile

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.renatsolocorp.dutyapp.R
import kotlinx.android.synthetic.main.spinner_elem_layout.view.*

class SettingsSpinnerAdapter(val context: Context, val list: MutableList<String>): BaseAdapter() {
    override fun getCount(): Int = list.size

    override fun getItem(position: Int): Any = list[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(context).inflate(R.layout.spinner_elem_layout, null)
        val image = view.flag_view
        val text = view.language_view

        text.text = list[position]
        when (list[position]){
            "Eng" -> {
                image.text = context.getString(R.string.engflag)
            }
            "Укр" -> {
                image.text = context.getString(R.string.ukrflag)
            }
            "Рус" -> {
                image.text = context.getString(R.string.rusflag)
            }
            context.getString(R.string.night) -> {
                image.text = context.getString(R.string.moon)
            }
            context.getString(R.string.day) -> {
                image.text = context.getString(R.string.sun)
            }
        }

        return view
    }
}