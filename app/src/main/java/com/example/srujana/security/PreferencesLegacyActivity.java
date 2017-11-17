package com.example.srujana.security;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PreferencesLegacyActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // Определение темы должно быть ДО super.onCreate и setContentView

        setTitle(R.string.menu_item_settings); // otherwise it's not changed

        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings_checkbox_or_switch);
        addPreferencesFromResource(R.xml.settings);




    }

}