/*
 * Copyright 2016-2021 Adrian Cotfas
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
package com.apps.adrcotfas.goodtime.about

import com.danielstone.materialaboutlibrary.MaterialAboutActivity
import com.danielstone.materialaboutlibrary.model.MaterialAboutList
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import com.apps.adrcotfas.goodtime.R
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem
import com.danielstone.materialaboutlibrary.ConvenienceBuilder
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import androidx.core.content.ContextCompat
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import android.content.Intent
import com.apps.adrcotfas.goodtime.main.MainIntroActivity
import com.apps.adrcotfas.goodtime.settings.PreferenceHelper
import com.apps.adrcotfas.goodtime.main.TimerActivity
import com.apps.adrcotfas.goodtime.util.DeviceInfo
import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.apps.adrcotfas.goodtime.BuildConfig
import com.apps.adrcotfas.goodtime.util.showOnce
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AboutActivity : MaterialAboutActivity() {

    @Inject lateinit var preferenceHelper: PreferenceHelper

    override fun getMaterialAboutList(c: Context): MaterialAboutList {
        val builder1 = MaterialAboutCard.Builder()
        val colorIcon = R.color.grey50
        builder1.addItem(
            MaterialAboutTitleItem.Builder()
                .text(getString(R.string.app_name_long))
                .icon(R.mipmap.ic_launcher)
                .build()
        )
        builder1.addItem(
            ConvenienceBuilder.createVersionActionItem(
                c,
                IconicsDrawable(c)
                    .icon(CommunityMaterial.Icon2.cmd_information_outline)
                    .color(ContextCompat.getColor(c, colorIcon))
                    .sizeDp(18),
                getString(R.string.about_version),
                false
            )
        )
        builder1.addItem(
            MaterialAboutActionItem.Builder()
                .text(getString(R.string.about_source_code))
                .icon(
                    IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_github_circle)
                        .color(ContextCompat.getColor(c, colorIcon))
                        .sizeDp(18)
                )
                .setOnClickAction(
                    ConvenienceBuilder.createWebsiteOnClickAction(
                        c, Uri.parse(getString(R.string.app_url))
                    )
                )
                .build()
        )
        builder1.addItem(MaterialAboutActionItem.Builder()
            .text(getString(R.string.about_open_source_licences))
            .icon(
                IconicsDrawable(c)
                    .icon(CommunityMaterial.Icon.cmd_book)
                    .color(ContextCompat.getColor(c, colorIcon))
                    .sizeDp(18)
            )
            .setOnClickAction {
                OpenSourceLicensesDialog().showOnce(supportFragmentManager, "licenses")
            }
            .build())
        builder1.addItem(MaterialAboutActionItem.Builder()
            .text(getString(R.string.about_app_intro))
            .icon(
                IconicsDrawable(c)
                    .icon(CommunityMaterial.Icon2.cmd_presentation)
                    .color(ContextCompat.getColor(c, colorIcon))
                    .sizeDp(18)
            )
            .setOnClickAction {
                val i = Intent(this@AboutActivity, MainIntroActivity::class.java)
                startActivity(i)
            }
            .build())
        builder1.addItem(MaterialAboutActionItem.Builder()
            .text(getString(R.string.tutorial_title))
            .icon(
                IconicsDrawable(c)
                    .icon(CommunityMaterial.Icon2.cmd_rocket)
                    .color(ContextCompat.getColor(c, colorIcon))
                    .sizeDp(18)
            )
            .setOnClickAction {
                preferenceHelper.lastIntroStep = 0
                preferenceHelper.archivedLabelHintWasShown = false
                val i = Intent(this@AboutActivity, TimerActivity::class.java)
                startActivity(i)
            }
            .build())
        val builder2 = MaterialAboutCard.Builder()
        builder2.addItem(MaterialAboutActionItem.Builder()
            .text(getString(R.string.feedback))
            .icon(
                IconicsDrawable(c)
                    .icon(CommunityMaterial.Icon.cmd_email)
                    .color(ContextCompat.getColor(c, colorIcon))
                    .sizeDp(18)
            )
            .setOnClickAction { openFeedback() }.build()
        )
        builder2.addItem(MaterialAboutActionItem.Builder()
            .text(getString(R.string.about_translate))
            .icon(
                IconicsDrawable(c)
                    .icon(CommunityMaterial.Icon2.cmd_web)
                    .color(ContextCompat.getColor(c, colorIcon))
                    .sizeDp(18)
            )
            .setOnClickAction {
                val url = getString(R.string.app_translation_url)
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
            }.build()
        )
        builder2.addItem(
            ConvenienceBuilder.createRateActionItem(
                c,
                IconicsDrawable(c)
                    .icon(CommunityMaterial.Icon2.cmd_star)
                    .color(ContextCompat.getColor(c, colorIcon))
                    .sizeDp(18),
                getString(R.string.about_rate_this_app),
                null
            )
        )
        val builder3 = MaterialAboutCard.Builder()
        builder3.addItem(MaterialAboutActionItem.Builder()
            .text(getString(R.string.other_apps))
            .icon(
                IconicsDrawable(c)
                    .icon(CommunityMaterial.Icon.cmd_application)
                    .color(ContextCompat.getColor(c, colorIcon))
                    .sizeDp(18)
            )
            .setOnClickAction {
                val url = "https://play.google.com/store/apps/developer?id=Goodtime"
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
            }.build()
        )
        return MaterialAboutList.Builder()
            .addCard(builder1.build())
            .addCard(builder2.build())
            .addCard(builder3.build())
            .build()
    }

    override fun getActivityTitle(): CharSequence? {
        return getString(R.string.mal_title_about)
    }

    private fun openFeedback() {
        val email = Intent(Intent.ACTION_SENDTO)
        email.data = Uri.Builder().scheme("mailto").build()
        email.putExtra(Intent.EXTRA_EMAIL, arrayOf("goodtime-app@googlegroups.com"))
        email.putExtra(Intent.EXTRA_SUBJECT, "[Goodtime] Feedback")
        email.putExtra(
            Intent.EXTRA_TEXT, """
     
     My device info: 
     ${DeviceInfo.deviceInfo}
     App version: ${BuildConfig.VERSION_NAME}
     """.trimIndent()
        )
        try {
            startActivity(Intent.createChooser(email, this.getString(R.string.feedback_title)))
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(this, R.string.about_no_email, Toast.LENGTH_SHORT).show()
        }
    }
}