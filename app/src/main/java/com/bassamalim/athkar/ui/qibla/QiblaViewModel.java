package com.bassamalim.athkar.ui.qibla;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class QiblaViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public QiblaViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("هذه شاشة القبلة");
    }

    public LiveData<String> getText() {
        return mText;
    }
}