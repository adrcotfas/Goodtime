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

package com.apps.adrcotfas.goodtime.About;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.apps.adrcotfas.goodtime.Settings.PreferenceHelper;
import com.apps.adrcotfas.goodtime.BuildConfig;
import com.apps.adrcotfas.goodtime.Main.MainIntroActivity;
import com.apps.adrcotfas.goodtime.Main.TimerActivity;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.Util.DeviceInfo;
import com.danielstone.materialaboutlibrary.ConvenienceBuilder;
import com.danielstone.materialaboutlibrary.MaterialAboutActivity;
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem;
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem;
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard;
import com.danielstone.materialaboutlibrary.model.MaterialAboutList;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class AboutActivity extends MaterialAboutActivity {
    @Override
    @NonNull
    protected MaterialAboutList getMaterialAboutList(@NonNull Context c) {
        MaterialAboutCard.Builder builder1 = new MaterialAboutCard.Builder();
        int colorIcon = R.color.gray50;
        builder1.addItem(new MaterialAboutTitleItem.Builder()
                .text(getString(R.string.app_name))
                .icon(R.mipmap.ic_launcher)
                .build());

        builder1.addItem(ConvenienceBuilder.createVersionActionItem(c,
                new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon2.cmd_information_outline)
                        .color(ContextCompat.getColor(c, colorIcon))
                        .sizeDp(18),
                getString(R.string.about_version),
                false));

        builder1.addItem(new MaterialAboutActionItem.Builder()
                .text(getString(R.string.about_source_code))
                .icon(new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_github_circle)
                        .color(ContextCompat.getColor(c, colorIcon))
                        .sizeDp(18))
                .setOnClickAction(ConvenienceBuilder.createWebsiteOnClickAction(
                        c, Uri.parse(getString(R.string.app_url))))
                .build());

        builder1.addItem(new MaterialAboutActionItem.Builder()
                .text(getString(R.string.about_open_source_licences))
                .icon(new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_book)
                        .color(ContextCompat.getColor(c, colorIcon))
                        .sizeDp(18))
                .setOnClickAction(() -> {
                    Intent intent = new Intent(c, LicencesActivity.class);
                    c.startActivity(intent);
                })
                .build());

        builder1.addItem(new MaterialAboutActionItem.Builder()
                .text(getString(R.string.about_app_intro))
                .icon(new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon2.cmd_presentation)
                        .color(ContextCompat.getColor(c, colorIcon))
                        .sizeDp(18))
                .setOnClickAction(() -> {
                    Intent i = new Intent(AboutActivity.this, MainIntroActivity.class);
                    startActivity(i);
                })
                .build());

        builder1.addItem(new MaterialAboutActionItem.Builder()
                .text(getString(R.string.tutorial_title))
                .icon(new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon2.cmd_rocket)
                        .color(ContextCompat.getColor(c, colorIcon))
                        .sizeDp(18))
                .setOnClickAction(() -> {
                    PreferenceHelper.setLastIntroStep(0);
                    PreferenceHelper.setArchivedLabelHintWasShown(false);
                    Intent i = new Intent(AboutActivity.this, TimerActivity.class);
                    startActivity(i);
                })
                .build());

        MaterialAboutCard.Builder builder2 = new MaterialAboutCard.Builder();

        builder2.addItem(new MaterialAboutActionItem.Builder()
                .text(getString(R.string.feedback))
                .icon(new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_email)
                        .color(ContextCompat.getColor(c, colorIcon))
                        .sizeDp(18))
                .setOnClickAction(this::openFeedback).build());

        builder2.addItem(new MaterialAboutActionItem.Builder()
                .text(getString(R.string.about_translate))
                .icon(new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon2.cmd_web)
                        .color(ContextCompat.getColor(c, colorIcon))
                        .sizeDp(18))
                .setOnClickAction(() -> {
                    String url = getString(R.string.app_translation_url);
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }).build());

        builder2.addItem(ConvenienceBuilder.createRateActionItem(c,
                new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon2.cmd_star)
                        .color(ContextCompat.getColor(c, colorIcon))
                        .sizeDp(18),
                getString(R.string.about_rate_this_app),
                null
        ));

        MaterialAboutCard.Builder builder3 = new MaterialAboutCard.Builder();

        builder3.addItem(new MaterialAboutActionItem.Builder()
                .text(getString(R.string.other_apps))
                .icon(new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_application)
                        .color(ContextCompat.getColor(c, colorIcon))
                        .sizeDp(18))
                .setOnClickAction(() -> {
                    String url = "https://play.google.com/store/apps/developer?id=Adrian+Cotfas";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }).build());

        return new MaterialAboutList.Builder()
                .addCard(builder1.build())
                .addCard(builder2.build())
                .addCard(builder3.build())
                .build();
    }

    @Override
    protected CharSequence getActivityTitle() {
        return getString(R.string.mal_title_about);
    }

    private void openFeedback() {
        Intent email = new Intent(Intent.ACTION_SENDTO);
        email.setData(new Uri.Builder().scheme("mailto").build());
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{"goodtime-app@googlegroups.com"});
        email.putExtra(Intent.EXTRA_SUBJECT, "[Goodtime] Feedback");
        email.putExtra(Intent.EXTRA_TEXT, "\nMy device info: \n" + DeviceInfo.getDeviceInfo()
                + "\nApp version: " + BuildConfig.VERSION_NAME);
        try {
            startActivity(Intent.createChooser(email, this.getString(R.string.feedback_title)));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, R.string.about_no_email, Toast.LENGTH_SHORT).show();
        }
    }
}
