package com.example.trainsignupandlogin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.trainsignupandlogin.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Setup ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Now you can access views in activity_main.xml using binding.yourId
    }
}