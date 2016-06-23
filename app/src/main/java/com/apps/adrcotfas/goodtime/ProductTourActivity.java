package com.apps.adrcotfas.goodtime;

import android.os.Bundle;

import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;

public class ProductTourActivity extends IntroActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setFullscreen(true);

        /**
         * Standard fade (like Google's intros)
         */
        addSlide(new SimpleSlide.Builder()
                .title("Avoid distractions")
                .description("Keep your work on track using timed sessions handled by the app.")
                .image(R.drawable.intro01)
                .background(R.color.intro01)
                .backgroundDark(R.color.intro01)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title("Clear your mind")
                .description("Reward yourself with short breaks and get refreshed.")
                .image(R.drawable.intro02)
                .background(R.color.intro02)
                .backgroundDark(R.color.intro02)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title("Get started")
                .description("Stay focused, get rid of procrastination and improve your productivity.")
                .image(R.drawable.intro03)
                .background(R.color.intro03)
                .backgroundDark(R.color.intro03)
                .build());
    }
}
