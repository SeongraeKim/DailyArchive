package com.ksr.dailyarchive;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import java.util.concurrent.Executor;

public class LoadingActivity extends AppCompatActivity {

    private TextView loadingText;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        loadingText = findViewById(R.id.loadingText);
        loadingText.setTextSize(18 * getBaseContext().getResources().getDisplayMetrics().density);

        lock_bio();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);// 지문인식 ON/OFF
        if (preferences.getBoolean("fingerprint", false)){
            biometricPrompt.authenticate(promptInfo);
        }
        else if (preferences.getBoolean("pin", false)){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getBaseContext(), PinActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 2000);
        }
        else {
            biometricPrompt.cancelAuthentication();
            startLoading();
        }
    }

    private void startLoading() {                                                                   // 화면이동
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 2000);
    }

    private void lock_bio() {                                                                       // 잠금(지문인식) 메서드
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getBaseContext(), "인증없이는 접근하실 수 없습니다 :(\n빠잉~", Toast.LENGTH_SHORT).show();
                finish();
            }
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(getBaseContext(), "인증성공했습니다!\n반갑습니다 :D", Toast.LENGTH_SHORT).show();
                startLoading();
            }
            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getBaseContext(), "인증실패했습니다!\n누구냐? 정체를 밝혀랏!", Toast.LENGTH_SHORT).show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("지문 인증")
                .setNegativeButtonText("취소")
                .setDeviceCredentialAllowed(false)
                .build();
    }
}
