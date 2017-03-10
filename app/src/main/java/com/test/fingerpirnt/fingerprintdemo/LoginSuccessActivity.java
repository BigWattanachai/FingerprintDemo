package com.test.fingerpirnt.fingerprintdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class LoginSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_success);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(LoginSuccessActivity.this, MainActivity.class));
    }
}
