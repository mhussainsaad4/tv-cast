package com.sensustech.mytvcast;

import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ironsource.mediationsdk.IronSource;


public class MirroringActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Button btn_action;
    private TextView tv_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mirroring);
        toolbar = findViewById(R.id.toolbar);
        btn_action = findViewById(R.id.btn_stop);
        tv_text = findViewById(R.id.tv_text);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle("Screen Mirroring");
        btn_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    startActivity(new Intent("android.settings.CAST_SETTINGS"));
                    return;
                } catch (Exception exception1) {
                    Toast.makeText(getApplicationContext(), "Device not supported", Toast.LENGTH_LONG).show();
                }
            }
        });
        tv_text.setText("1. Make sure that your Chromecast and TV are connected to the same WiFi network.\n\n2. Make sure that Miracast Display enabled and supported by your Chromecast model.");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        IronSource.onPause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IronSource.onResume(this);
    }

}