package com.example.sspgcek;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.sspgcek.databinding.ActivityDownloadBinding;

public class DownloadActivity extends AppCompatActivity {
    ActivityDownloadBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityDownloadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

    }
}