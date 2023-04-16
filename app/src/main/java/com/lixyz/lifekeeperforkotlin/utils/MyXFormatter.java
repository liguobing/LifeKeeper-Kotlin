package com.lixyz.lifekeeperforkotlin.utils;

import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;

public class MyXFormatter extends ValueFormatter {

    private ArrayList<String> labelList;


    public MyXFormatter() {
        labelList = new ArrayList<>();
        for (int i = 0; i < 31; i++) {
            labelList.add((i + 1) + "日");
        }
    }

    @Override
    public String getFormattedValue(float value) {
        if (value < 0) {
            return "";
        } else {
            return labelList.get((int) value);
        }
    }
}
