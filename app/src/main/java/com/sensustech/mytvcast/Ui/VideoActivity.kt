package com.sensustech.mytvcast.Ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sensustech.mytvcast.R
import com.sensustech.mytvcast.databinding.ActivityVideoBinding

class VideoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        var binding = ActivityVideoBinding.inflate(layoutInflater)
        val bottomSheetDialog: BottomSheetDialog by lazy {
            BottomSheetDialog()
        }
        setContentView(binding.root)
        binding.imgQueue.setOnClickListener {
            bottomSheetDialog.show(
                supportFragmentManager,
                "ModalBottomSheet")
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}