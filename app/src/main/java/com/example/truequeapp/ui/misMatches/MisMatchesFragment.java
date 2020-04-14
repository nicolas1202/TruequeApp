package com.example.truequeapp.ui.misMatches;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import com.example.truequeapp.R;

public class MisMatchesFragment extends Fragment {

    private MisMatchesViewModel misMatchesViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        misMatchesViewModel =
                ViewModelProviders.of(this).get(MisMatchesViewModel.class);
        View root = inflater.inflate(R.layout.mis_matches, container, false);

        misMatchesViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });
        return root;
    }
}
