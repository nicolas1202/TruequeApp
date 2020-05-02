package com.example.truequeapp.misProductos;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ProductosViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ProductosViewModel() {
        mText = new MutableLiveData<>();
        //mText.setValue("This is mis productos fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}