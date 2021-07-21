package com.bassamalim.athkar.ui.prayers;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PrayersViewModel extends ViewModel {
    private MutableLiveData<String> mText;

    public PrayersViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("هذه شاشة أوقات الصلاة");
    }

    public LiveData<String> getText() {
        return mText;
    }
}