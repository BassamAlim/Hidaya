package com.bassamalim.athkar.models;

import android.view.View;

public class AlathkarButton {

    private final int number;
    private final String name;
    private final View.OnClickListener listener;

    public AlathkarButton(int number, String name, View.OnClickListener listener) {
        this.number = number;
        this.name = name;
        this.listener = listener;
    }

    public int getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public View.OnClickListener getListener() {
        return listener;
    }
}
