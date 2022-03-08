/*
 * Copyright (C) 2020 The exTHmUI Open Source Project
 * Copyright (C) 2021 AOSP-Krypton Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.exthmui.game.qs

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TextView

import androidx.appcompat.content.res.AppCompatResources

import org.exthmui.game.R
import org.exthmui.game.qs.tiles.QSTile

@SuppressLint("AppCompatCustomView")
open class TileView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : TextView(context, attrs, defStyleAttr, defStyleRes), View.OnClickListener {

    private val background = LayerDrawable(
        arrayOf(AppCompatResources.getDrawable(context, R.drawable.qs_background))
    )
    private var icon: Drawable? = null

    private var selectedIconTint = Color.WHITE
    private var deselectedIconTint = Color.BLACK

    private var tile: QSTile? = null

    init {
        gravity = Gravity.CENTER
        setEms(5)
        maxLines = 2
        with(resources) {
            setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                getDimensionPixelSize(R.dimen.gaming_qs_text_size).toFloat()
            )
            compoundDrawablePadding = getDimensionPixelSize(R.dimen.qs_compound_icon_padding)
            val isNightMode =
                (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            if (isNightMode) {
                selectedIconTint = Color.BLACK
                deselectedIconTint = Color.WHITE
            } else {
                selectedIconTint = Color.WHITE
                deselectedIconTint = Color.BLACK
            }
        }
    }

    final override fun setTextSize(value: Int, float: Float) {
        super.setTextSize(value, float)
    }

    final override fun setEms(ems: Int) {
        super.setEms(ems)
    }

    fun setTile(tile: QSTile?) {
        this.tile = tile
        tile?.let {
            if (it.getTitleRes() != 0) {
                setText(it.getTitleRes())
            }
            setIcon(it.getIconRes())
            isSelected = it.isSelected
            it.setCallback(object : QSTile.Callback {
                override fun onStateChanged(selected: Boolean) {
                    isSelected = selected
                }
            })
        }
    }

    private fun setIcon(resId: Int) {
        if (resId != 0) {
            icon = AppCompatResources.getDrawable(context, resId)
            updateIconTint()
            if (background.numberOfLayers >= 2 && background.getDrawable(1) != null) {
                // Replace existing drawable at index instead of adding a layer on top it
                background.setDrawable(1, icon)
            } else {
                background.addLayer(icon)
            }
            val inset = context.resources.getDimensionPixelSize(R.dimen.gaming_qs_icon_padding)
            background.setLayerInset(1, inset, inset, inset, inset)
            val size = context.resources.getDimensionPixelSize(R.dimen.gaming_qs_icon_size)
            background.setBounds(0, 0, size, size)
            setCompoundDrawables(null, background, null, null)
        }
    }

    private fun updateIconTint() {
        icon?.setTint(if (isSelected) selectedIconTint else deselectedIconTint)
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        updateIconTint()
    }

    override fun onClick(v: View) {
        tile?.handleClick(v)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setOnClickListener(this)
    }

    override fun onDetachedFromWindow() {
        setOnClickListener(null)
        super.onDetachedFromWindow()
    }
}