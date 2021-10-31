package com.apps.adrcotfas.goodtime.statistics

import android.annotation.SuppressLint
import com.github.mikephil.charting.utils.MPPointF

import android.content.Context
import android.view.View
import android.widget.TextView
import com.apps.adrcotfas.goodtime.R
import com.apps.adrcotfas.goodtime.util.StringUtils

import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight


@SuppressLint("ViewConstructor")
class ChartMarker(
    context: Context,
    layoutResource: Int,
    private val markerType : MarkerType = MarkerType.INTEGER) : MarkerView(context, layoutResource) {

    enum class MarkerType {
        INTEGER,
        PERCENTAGE,
        MINUTES
    }

    private var tvContent: TextView? = null

    init {
        tvContent = findViewById<View>(R.id.tvContent) as TextView
    }

    @SuppressLint("SetTextI18n")
    override fun refreshContent(e: Entry, highlight: Highlight?) {
        val value = when(markerType) {
            MarkerType.PERCENTAGE -> "${(e.y * 100).toInt()}%"
            MarkerType.INTEGER -> e.y.toInt().toString()
            MarkerType.MINUTES -> StringUtils.formatMinutes(e.y.toLong())
        }
        tvContent!!.text = value
        super.refreshContent(e, highlight)
    }

    private var mOffset: MPPointF? = null
    override fun getOffset(): MPPointF {
        if (mOffset == null) {
            mOffset = MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
        }
        return mOffset!!
    }
}