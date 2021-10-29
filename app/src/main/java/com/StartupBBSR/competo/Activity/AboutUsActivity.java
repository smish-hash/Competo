package com.StartupBBSR.competo.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.StartupBBSR.competo.databinding.ActivityAboutUsBinding;

public class AboutUsActivity extends AppCompatActivity {

    private ActivityAboutUsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAboutUsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.btnCloseAboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.tvMeetTheTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri teamLink = Uri.parse("https://www.myteamos.com/aboutus");
                Intent intent = new Intent(Intent.ACTION_VIEW, teamLink);
                startActivity(intent);
            }
        });
    }
}