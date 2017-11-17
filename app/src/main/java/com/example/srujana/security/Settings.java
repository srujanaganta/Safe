package com.example.srujana.security;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Sandeep on 28-10-2017.
 */
public class Settings extends AppCompatActivity {

    private static final String EXTRA_ANSWER_IS_TRUE =
            "com.bignerdranch.android.geoquiz.answer_is_true";
    public static Intent newIntent(Context packageContext) {
        Intent i = new Intent(packageContext, Settings.class);
        i.putExtra(EXTRA_ANSWER_IS_TRUE, 10);
        i.putExtra(EXTRA_ANSWER_IS_TRUE,11);
        return i;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


    }

}
