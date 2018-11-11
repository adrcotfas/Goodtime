package com.apps.adrcotfas.goodtime.Statistics;

import com.apps.adrcotfas.goodtime.LabelAndColor;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class StatisticsViewModel extends ViewModel {
    public MutableLiveData<LabelAndColor> currentLabel = new MutableLiveData<>();
}
