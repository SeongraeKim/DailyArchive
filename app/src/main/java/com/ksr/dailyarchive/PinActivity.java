package com.ksr.dailyarchive;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.hanks.passcodeview.PasscodeView;

public class PinActivity extends AppCompatActivity {

    private PasscodeView passcodeView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String pass = preferences.getString("pass", "0000");

        Log.d("kkk", "현재암호 : " + pass);

        passcodeView = findViewById(R.id.passcode_view);
        passcodeView.setPasscodeLength(4).setLocalPasscode(pass).setListener(new PasscodeView.PasscodeViewListener() {
            @Override
            public void onFail() { /* Not Working :( */ }
            @Override
            public void onSuccess(String number) {
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
