package com.bassamalim.athkar;

import android.location.Location;

import java.util.ArrayList;
import java.util.Calendar;

public class DataSaver {

    public Location location;
    public Calendar[] times;


    private Calendar[] formatTimes(ArrayList<String> givenTimes) {
        Calendar[] formattedTimes = new Calendar[givenTimes.size()];

        for (int i = 0; i < givenTimes.size(); i++) {
            formattedTimes[i] = Calendar.getInstance();

            formattedTimes[i].setTimeInMillis(System.currentTimeMillis());
            formattedTimes[i].set(Calendar.HOUR_OF_DAY, Integer.parseInt(givenTimes.get(i).substring(0, 2)));
            formattedTimes[i].set(Calendar.MINUTE, Integer.parseInt(givenTimes.get(i).substring(3, 5)));
        }
        return formattedTimes;
    }

}
