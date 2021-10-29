package com.StartupBBSR.competo.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import com.StartupBBSR.competo.databinding.ActivityFaqBinding;

public class FAQActivity extends AppCompatActivity {

    private ActivityFaqBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFaqBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}