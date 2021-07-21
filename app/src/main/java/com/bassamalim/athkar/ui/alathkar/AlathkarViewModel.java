package com.bassamalim.athkar.ui.alathkar;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AlathkarViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public AlathkarViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("هذه شاشة الأذكار");
    }

    public LiveData<String> getText() {
        return mText;
    }
}