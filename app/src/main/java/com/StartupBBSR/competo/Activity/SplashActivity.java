package com.StartupBBSR.competo.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.StartupBBSR.competo.BuildConfig;
import com.StartupBBSR.competo.R;
import com.StartupBBSR.competo.databinding.ActivitySplashBinding;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //        Disable night mode
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.splash_animation);
        Animation textAnimation = AnimationUtils.loadAnimation(this, R.anim.project_fab_open);
        binding.image.startAnimation(animation);
        binding.textView.startAnimation(textAnimation);
        binding.textView.startAnimation(textAnimation);

        new Handler(Looper.myLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, OnboardingActivity.class);
                startActivity(intent);
                finish();
            }

        }, 1000);

        TextView versionTextView = findViewById(R.id.tvVersion);
        versionTextView.setText("v" + BuildConfig.VERSION_NAME);

    }
}