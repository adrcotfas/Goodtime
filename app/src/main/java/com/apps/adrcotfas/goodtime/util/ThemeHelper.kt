/*
 * Copyright 2016-2019 Adrian Cotfas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.apps.adrcotfas.goodtime.util

import androidx.appcompat.app.AppCompatActivity
import com.apps.adrcotfas.goodtime.R
import android.util.TypedValue
import android.app.Activity
import android.content.Context
import android.widget.EditText
import androidx.annotation.AttrRes
import android.content.res.TypedArray
import android.view.View
import android.view.inputmethod.InputMethodManager
import java.lang.RuntimeException

object ThemeHelper {

    const val COLOR_INDEX_UNLABELED = 17 // index of white in the palette
    const val COLOR_INDEX_ALL_LABELS = 42
    const val COLOR_INDEX_BREAK = 43

    fun setTheme(activity: AppCompatActivity, amoled: Boolean) {
        if (amoled) {
            activity.setTheme(R.style.AppTheme)
        } else {
            activity.setTheme(R.style.AppThemeDark)
        }
    }

    fun pxToDp(context: Context, px: Int): Float {
        return px / context.resources.displayMetrics.density
    }

    fun dpToPx(context: Context, dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        ).toInt()
    }

    fun spToPx(context: Context, sp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp,
            context.resources.displayMetrics
        ).toInt()
    }

    fun dpToSp(context: Context, dp: Float): Int {
        return (dpToPx(context, dp) / context.resources.displayMetrics.scaledDensity).toInt()
    }

    fun clearFocusEditText(v: View, context: Context) {
        v.clearFocus()
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(v.windowToken, 0)
    }

    fun requestFocusEditText(v: EditText, context: Context) {
        v.setSelection(v.length())
        v.requestFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
    }

    fun getPalette(context: Context): IntArray {
        val resourceId = getResource(R.attr.palette, context)
        if (resourceId < 0) throw RuntimeException("resource not found")
        return context.resources.getIntArray(resourceId)
    }

    private fun getResource(@AttrRes attrId: Int, context: Context): Int {
        val ta = getTypedArray(attrId, context)
        val resourceId = ta.getResourceId(0, -1)
        ta.recycle()
        return resourceId
    }

    private fun getTypedArray(@AttrRes attrId: Int, context: Context): TypedArray {
        val attrs = intArrayOf(attrId)
        return context.obtainStyledAttributes(attrs)
    }

    fun getColor(context: Context, colorIndex: Int): Int {
        if (colorIndex == COLOR_INDEX_UNLABELED || colorIndex == -1) {
            return context.resources.getColor(R.color.white)
        }
        if (colorIndex == COLOR_INDEX_ALL_LABELS) {
            return context.resources.getColor(R.color.teal200)
        }
        if (colorIndex == COLOR_INDEX_BREAK) {
            return context.resources.getColor(R.color.grey500)
        }
        val colors = getPalette(context)
        return if (colorIndex < colors.size) {
            colors[colorIndex]
        } else context.resources.getColor(R.color.white)
    }

    fun getIndexOfColor(context: Context, color: Int): Int {
        if (color == context.resources.getColor(R.color.white)) {
            return COLOR_INDEX_UNLABELED
        }
        if (color == context.resources.getColor(R.color.teal200)) {
            return COLOR_INDEX_ALL_LABELS
        }
        val colors = getPalette(context)
        for (i in colors.indices) {
            if (color == colors[i]) {
                return i
            }
        }
        return COLOR_INDEX_UNLABELED
    }
}