package com.example.androidhive;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        super.onCreateDrawer();
        current_user_id = sharedPref.getString("user_id", "nothing returned");
    }
}
