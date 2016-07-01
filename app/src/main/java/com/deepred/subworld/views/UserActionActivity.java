package com.deepred.subworld.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.deepred.subworld.ICommon;
import com.deepred.subworld.R;

public class UserActionActivity extends AppCompatActivity {
    private static final String TAG = "InitAplication";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        if (i != null) {
            String uid = i.getStringExtra(ICommon.UID);
            if (!uid.isEmpty()) {
                setContentView(R.layout.activity_user_action);


            } else {
                Log.e(TAG, "Uid is empty");
                finish();
            }
        } else {
            Log.e(TAG, "Intent not found");
            finish();
        }
    }
}
