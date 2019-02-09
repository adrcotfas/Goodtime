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

package com.apps.adrcotfas.goodtime.Main;

import android.os.Bundle;

import com.apps.adrcotfas.goodtime.R;
import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;

public class MainIntroActivity extends IntroActivity {
    @Override protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        addSlide(new SimpleSlide.Builder()
                .title(R.string.intro_avoid_distractions)
                .description(R.string.intro_avoid_distractions_description)
                .image(R.drawable.intro1)
                .background(R.color.gray50)
                .backgroundDark(R.color.gray50)
                .layout(R.layout.activity_main_intro)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title(R.string.intro_clear_mind)
                .description(R.string.intro_clear_mind_description)
                .image(R.drawable.intro2)
                .background(R.color.gray50)
                .backgroundDark(R.color.gray50)
                .layout(R.layout.activity_main_intro)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title(R.string.intro_get_started)
                .description(R.string.intro_get_started_description)
                .image(R.drawable.intro3)
                .background(R.color.gray50)
                .backgroundDark(R.color.gray50)
                .layout(R.layout.activity_main_intro)
                .build());
    }
}
