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
import android.view.MenuItem;

import com.apps.adrcotfas.goodtime.BuildConfig;
import com.apps.adrcotfas.goodtime.R;
import com.danielstone.materialaboutlibrary.ConvenienceBuilder;
import com.danielstone.materialaboutlibrary.MaterialAboutActivity;
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard;
import com.danielstone.materialaboutlibrary.model.MaterialAboutList;
import com.danielstone.materialaboutlibrary.util.OpenSourceLicense;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class LicencesActivity extends MaterialAboutActivity {

    @NonNull
    @Override
    protected MaterialAboutList getMaterialAboutList(@NonNull final Context c) {
        int colorIcon = R.color.gray50;

        MaterialAboutCard cardMpAndroidChart = ConvenienceBuilder.createLicenseCard(c,
                new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_book)
                        .color(ContextCompat.getColor(c, colorIcon))
                        .sizeDp(18),
                "MPAndroidChart", "2018", "Philipp Jahoda",
                OpenSourceLicense.APACHE_2);

        MaterialAboutCard cardPreferenceX = ConvenienceBuilder.createLicenseCard(c,
                new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_book)
                        .color(ContextCompat.getColor(c, colorIcon))
                        .sizeDp(18),
                "AndroidX Preference eXtended", "2018", "takisoft",
                OpenSourceLicense.APACHE_2);

        MaterialAboutCard cardColorPickerX = ConvenienceBuilder.createLicenseCard(c,
                new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_book)
                        .color(ContextCompat.getColor(c, colorIcon))
                        .sizeDp(18),
                "ColorPicker with AndroidX", "2018", "takisoft",
                OpenSourceLicense.APACHE_2);

        MaterialAboutCard cardEventBus = ConvenienceBuilder.createLicenseCard(c,
                new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_book)
                        .color(ContextCompat.getColor(c, colorIcon))
                        .sizeDp(18),
                "EventBus", "2012-2017", "Markus Junginger, greenrobot",
                OpenSourceLicense.APACHE_2);

        MaterialAboutCard materialAboutLIbraryLicenseCard = ConvenienceBuilder.createLicenseCard(c,
                new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_book)
                        .color(ContextCompat.getColor(c, colorIcon))
                        .sizeDp(18),
                "material-about-library", "2016-2018", "Daniel Stone",
                OpenSourceLicense.APACHE_2);

        MaterialAboutCard androidIconicsLicenseCard = ConvenienceBuilder.createLicenseCard(c,
                new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_book)
                        .color(ContextCompat.getColor(c, colorIcon))
                        .sizeDp(18),
                "Android Iconics", "2016", "Mike Penz",
                OpenSourceLicense.APACHE_2);

        MaterialAboutCard cardJodaOrg = ConvenienceBuilder.createLicenseCard(c,
                new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_book)
                        .color(ContextCompat.getColor(c, colorIcon))
                        .sizeDp(18),
                "Joda-Time", "2019", "JodaOrg",
                OpenSourceLicense.APACHE_2);

        MaterialAboutCard cardIap = ConvenienceBuilder.createLicenseCard(c,
                new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_book)
                        .color(ContextCompat.getColor(c, colorIcon))
                        .sizeDp(18),
                "material-intro", "2017", "Jan Heinrich Reimer",
                OpenSourceLicense.MIT);

        MaterialAboutCard cardIntro = ConvenienceBuilder.createLicenseCard(c,
                new IconicsDrawable(c)
                        .icon(CommunityMaterial.Icon.cmd_book)
                        .color(ContextCompat.getColor(c, colorIcon))
                        .sizeDp(18),
                "Android In-App Billing v3", "2014", "AnjLab",
                OpenSourceLicense.APACHE_2);

        MaterialAboutList result = new MaterialAboutList()
                .addCard(cardMpAndroidChart)
                .addCard(cardPreferenceX)
                .addCard(cardColorPickerX)
                .addCard(cardEventBus)
                .addCard(materialAboutLIbraryLicenseCard)
                .addCard(androidIconicsLicenseCard)
                .addCard(cardJodaOrg)
                .addCard(cardIap)
                .addCard(cardIntro);

        if (BuildConfig.F_DROID) {
            MaterialAboutCard donationCard = ConvenienceBuilder.createLicenseCard(c,
                    new IconicsDrawable(c)
                            .icon(CommunityMaterial.Icon.cmd_book)
                            .color(ContextCompat.getColor(c, colorIcon))
                            .sizeDp(18),
                    "Android Donations Lib", "2018", "SufficientlySecure",
                    OpenSourceLicense.APACHE_2);
            result.addCard(donationCard);
        }
        return result;
    }

    @Override
    protected CharSequence getActivityTitle() {
        return getString(R.string.mal_title_licenses);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return false;
        }
    }
}
