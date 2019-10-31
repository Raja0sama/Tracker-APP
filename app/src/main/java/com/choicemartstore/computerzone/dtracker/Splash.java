package com.choicemartstore.computerzone.dtracker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Timer;
import java.util.TimerTask;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    timer();

    }
    public void timer(){
                startActivity(new Intent(getApplicationContext(), signin.class));
                finish();

    }
}
