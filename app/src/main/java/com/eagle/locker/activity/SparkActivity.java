package com.eagle.locker.activity;

import android.app.Activity;
import android.os.Bundle;

import com.eagle.locker.spark.SparkView;


public class SparkActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SparkView sparkView = new SparkView(this);
        setContentView(sparkView);
    }
}
