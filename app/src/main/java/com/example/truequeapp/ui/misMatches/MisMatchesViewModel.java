package com.example.truequeapp.ui.misMatches;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MisMatchesViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public MisMatchesViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Fragment Matches");
    }

    public LiveData<String> getText() {
        return mText;
    }
}