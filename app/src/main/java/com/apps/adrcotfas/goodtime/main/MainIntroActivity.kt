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
package com.apps.adrcotfas.goodtime.main

import com.heinrichreimersoftware.materialintro.app.IntroActivity
import android.os.Bundle
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide
import com.apps.adrcotfas.goodtime.R

class MainIntroActivity : IntroActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addSlide(
            SimpleSlide.Builder()
                .title(R.string.intro_avoid_distractions)
                .description(R.string.intro_avoid_distractions_description)
                .image(R.drawable.intro1)
                .background(R.color.grey50)
                .backgroundDark(R.color.grey50)
                .layout(R.layout.activity_main_intro)
                .build()
        )
        addSlide(
            SimpleSlide.Builder()
                .title(R.string.intro_clear_mind)
                .description(R.string.intro_clear_mind_description)
                .image(R.drawable.intro2)
                .background(R.color.grey50)
                .backgroundDark(R.color.grey50)
                .layout(R.layout.activity_main_intro)
                .build()
        )
        addSlide(
            SimpleSlide.Builder()
                .title(R.string.intro_get_started)
                .description(R.string.intro_get_started_description)
                .image(R.drawable.intro3)
                .background(R.color.grey50)
                .backgroundDark(R.color.grey50)
                .layout(R.layout.activity_main_intro)
                .build()
        )
    }
}