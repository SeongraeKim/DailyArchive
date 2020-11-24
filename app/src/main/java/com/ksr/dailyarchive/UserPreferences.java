package com.ksr.dailyarchive;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.biometric.BiometricManager;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class UserPreferences extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SwitchPreference fingerprint, pin;
    private EditTextPreference pin_change;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.user_preferences, rootKey);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fingerprint = findPreference("fingerprint");
        pin = findPreference("pin");
        pin_change = findPreference("pin_change");

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String pass = preferences.getString("pass", "0000");
        pin_change.setSummary("현재 비밀번호: " + pass);

        // Biometric 사용가능할 수 있는지 확인
        if (BiometricManager.from(getContext()).canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS){
            fingerprint.setEnabled(true);
        }else {
            fingerprint.setEnabled(false);
            fingerprint.setSummaryOff("해당 기기에서는 지문인식을 사용하실 수 없습니다.");
        }

        if (pin.isChecked()){   // PIN ON?/OFF?
            pin_change.setVisible(true);
        } else {
            pin_change.setVisible(false);
        }

        pin_change.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {   // 비밀번호 변경 클릭 시
                pin_change.setDialogMessage("변경하실 비밀번호를 입력해주세요.(4자리수)");
                pin_change.setText("");
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        // EditTextPreference 형식지정
        pin_change = findPreference("pin_change");
        pin_change.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.selectAll();
                int maxLength = 4;
                editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {                                   // 클릭 감지
        String key = preference.getKey();
        Uri uri;

        switch (key){
            case "backup_data":
                backup_data_dialog();
                break;
            case "get_data":
                get_data_dialog();
                break;
            case "clear_data":
                clear_data_dialog();
                break;
            case "app_evaluate":
                uri = Uri.parse("market://details?id=" + getContext().getPackageName());
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
                break;
            case "feedback":
                uri = Uri.parse("mailto:tjdfo2175@naver.com");
                startActivity(new Intent(Intent.ACTION_SENDTO, uri));
                break;
        }
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {        // 값 변경 감지

        if(key.equals("fingerprint")){                                                              // 지문인식
            boolean fingerprint_check = sharedPreferences.getBoolean("fingerprint", false);
            pin = findPreference("pin");

            if (fingerprint_check && pin.isChecked()){   // 다른 잠금설정이 켜진 경우 OFF
                pin.setChecked(false);
                Snackbar.make(getView(), "보안설정은 하나만 선택 가능합니다.\nPIN 설정을 해제합니다.", Snackbar.LENGTH_SHORT).show();
            }
        }

        else if (key.equals("pin")){                                                                // PIN
            boolean pin_check = sharedPreferences.getBoolean("pin", false);
            fingerprint = findPreference("fingerprint");
            pin_change = findPreference("pin_change");

            if (pin_check && fingerprint.isChecked()){           // 다른 잠금설정이 켜진 경우 OFF
                fingerprint.setChecked(false);
                Snackbar.make(getView(), "보안설정은 하나만 선택 가능합니다.\n지문인식을 해제합니다.", Snackbar.LENGTH_SHORT).show();
            }

            if(pin_check) {
                pin_change.setVisible(true);
            } else {
                pin_change.setVisible(false);
            }
        }
        else if (key.equals("pin_change")){
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = preferences.edit();
            pin_change = findPreference("pin_change");
            String pass = pin_change.getText();

            if (pass.length() == 4){                            // 4자리 입력하도록 설정
                editor.putString("pass", pass).commit();
                pin_change.setSummary("현재 비밀번호: " + pass);
                Snackbar.make(getView(), "비밀번호 변경완료!", Snackbar.LENGTH_SHORT).show();
            } else if (0 < pass.length() && pass.length() < 4){
                Snackbar.make(getView(), "비밀번호 변경실패!\n4자리수 입력 부탁드립니다.", Snackbar.LENGTH_SHORT).show();
            }
        }

    }

    public void backup_data_dialog(){                                                               // 데이터 백업
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("데이터 백업");
        builder.setMessage("데이터를 백업 하시겠습니까?\n(* 기존 데이터는 제거됩니다.)");

        builder.setNegativeButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    File sd = Environment.getExternalStorageDirectory();
                    File data = Environment.getDataDirectory();

                    Log.d("kkk", sd + ", " + sd.canWrite());

                    if (sd.canWrite()) {
                        File currentDB = new File(data, "/data/com.ksr.dailyarchive/databases/DailyArchiveDB");
                        File backupDB = new File(sd, "/Download/DailyArchiveDB");

                        if (currentDB.length() == 0){
                            Snackbar.make(getView(), "백업할 파일이 없습니다.", Snackbar.LENGTH_SHORT).show();
                            return;
                        }

                        FileChannel src = new FileInputStream(currentDB).getChannel();
                        FileChannel dst = new FileOutputStream(backupDB).getChannel();
                        dst.transferFrom(src, 0, src.size());

                        currentDB.delete();

                        src.close();
                        dst.close();
                        Snackbar.make(getView(), "백업 완료되었습니다.", Snackbar.LENGTH_SHORT).show();

                    } else {
                        Snackbar.make(getView(), "접근 권한이 필요합니다.\n[권한]에서 저장공간 접근 허용해주세요.", Snackbar.LENGTH_SHORT)
                                .setAction("이동", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        AppInfo_permission();
                                    }
                                }).show();
                    }
                } catch (Exception e) {
                    Snackbar.make(getView(), "백업에 실패했습니다.", Snackbar.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
        builder.setPositiveButton("취소", null);
        builder.show();
    }

    private void get_data_dialog() {                                                                // 데이터 가져오기
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("데이터 가져오기");
        builder.setMessage("백업데이터를 가져오시겠습니까?\n(* 기존 데이터는 제거됩니다.)");

        builder.setNegativeButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    File sd = Environment.getExternalStorageDirectory();
                    File data = Environment.getDataDirectory();

                    if (sd.canWrite()) {
                        File currentDB = new File(sd, "/Download/DailyArchiveDB");
                        File restoreDB = new File(data, "/data/com.ksr.dailyarchive/databases/DailyArchiveDB");

                        if (!currentDB.exists()){
                            Snackbar.make(getView(), "불러올 파일이 존재하지 않습니다.", Snackbar.LENGTH_SHORT).show();
                            return;
                        }

                        FileChannel src = new FileInputStream(currentDB).getChannel();
                        FileChannel dst = new FileOutputStream(restoreDB).getChannel();
                        dst.transferFrom(src, 0, src.size());

                        currentDB.delete();

                        src.close();
                        dst.close();
                        Snackbar.make(getView(), "데이터를 불러왔습니다.", Snackbar.LENGTH_SHORT).show();

                    } else {
                        Snackbar.make(getView(), "접근 권한이 필요합니다.\n[권한]에서 저장공간 접근 허용해주세요.", Snackbar.LENGTH_SHORT)
                                .setAction("이동", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        AppInfo_permission();
                                    }
                                }).show();
                    }
                } catch (Exception e) {
                    Snackbar.make(getView(), "데이터 불러오기 실패했습니다.", Snackbar.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
        builder.setPositiveButton("취소", null);
        builder.show();
    }

    private void clear_data_dialog() {                                                              // 데이터 초기화
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("데이터 초기화");
        builder.setMessage("데이터를 초기화 하시겠습니까?\n(* 초기화 후 되돌릴 수 없습니다.)");

        builder.setNegativeButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    File sd = Environment.getExternalStorageDirectory();
                    File data = Environment.getDataDirectory();

                    if (sd.canWrite()) {
                        File currentDB = new File(data, "/data/com.ksr.dailyarchive/databases/DailyArchiveDB");

                        currentDB.delete();
                        Snackbar.make(getView(), "초기화되었습니다.", Snackbar.LENGTH_SHORT).show();

                    } else {
                        Snackbar.make(getView(), "접근 권한이 필요합니다.\n[권한]에서 저장공간 접근 허용해주세요.", Snackbar.LENGTH_SHORT)
                                .setAction("이동", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        AppInfo_permission();
                                    }
                                }).show();
                    }
                } catch (Exception e) {
                    Snackbar.make(getView(), "초기화에 실패했습니다.", Snackbar.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
        builder.setPositiveButton("취소", null);
        builder.show();
    }

    private void AppInfo_permission(){                                                              // [앱정보] 이동
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:"+getContext().getPackageName()));
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
