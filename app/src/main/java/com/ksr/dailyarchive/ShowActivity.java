package com.ksr.dailyarchive;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ShowActivity extends AppCompatActivity {

    private TextView show_date, show_content;
    private Button btn_save, btn_modify, btn_delete, btn_cancel;
    private AdView adView;

    private DBManager dbManager = new DBManager(this);

    private Date parse_date  = null;
    SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy  /  MM  /  dd  E요일");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show);

        show_date       = findViewById(R.id.show_date);
        show_content    = findViewById(R.id.show_content);
        btn_save        = findViewById(R.id.btn_save);
        btn_modify      = findViewById(R.id.btn_modify);
        btn_delete      = findViewById(R.id.btn_delete);
        btn_cancel      = findViewById(R.id.btn_cancel);
        adView          = findViewById(R.id.adView);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        Intent intent = getIntent();
        final int get_date = intent.getIntExtra("date", 1);                     // ex) 20200118
        try {
            parse_date = dateFormat1.parse(String.valueOf(get_date));
        }catch (Exception e){ e.printStackTrace(); }

        final String format_date = dateFormat2.format(parse_date);                                  // ex) 2020  /  01  /  18  토요일

        String content = dbManager.selectDB(get_date);

        show_date.setText(format_date + "");    // show_date, show_content 설정
        show_content.setText(content);

        // 내용 유무에 따른 버튼교체
        if(!show_content.getText().toString().equals("")) {
            btn_save.setVisibility(View.GONE);
            btn_modify.setVisibility(View.VISIBLE);
            btn_delete.setVisibility(View.VISIBLE);
        }

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {                                                      // 저장버튼

                int _date = get_date;
                String _content = show_content.getText().toString();

                // 내용이 없을 경우 return
                if(_content.equals("")){
                    Snackbar.make(view, "내용을 입력해주세요.", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                dbManager.insertDB(_date, _content);    // DB 데이터 추가
                Snackbar.make(view, "일기가 저장되었습니다.", Snackbar.LENGTH_SHORT).show();
                delay500();

            }
        });

        btn_modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {                                                    // 수정버튼

                int _date = get_date;
                String _content = show_content.getText().toString();

                dbManager.updateDB(_date, _content);    // DB 데이터 수정
                Snackbar.make(view, "일기가 수정되었습니다.", Snackbar.LENGTH_SHORT).show();
                delay500();
            }
        });

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {                                                    // 삭제버튼

                int _date = get_date;

                dbManager.deleteDB(_date);  // Db 데이터 삭제
                Snackbar.make(view, "일기가 삭제되었습니다.", Snackbar.LENGTH_SHORT).show();
                delay500();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {                                                    // 취소버튼
                finish();
            }
        });
    }

    public void delay500(){                                                                         // 딜레이 후 Activity 이동
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 500);

    }
}
