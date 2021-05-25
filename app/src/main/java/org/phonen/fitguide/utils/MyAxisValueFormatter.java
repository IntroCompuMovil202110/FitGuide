package org.phonen.fitguide.utils;

import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

public class MyAxisValueFormatter extends IndexAxisValueFormatter {
    @Override
    public String getFormattedValue(float value) {
        return  (int)value +" KM";
    }
}
