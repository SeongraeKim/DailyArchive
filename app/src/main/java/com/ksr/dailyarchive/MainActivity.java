package com.ksr.dailyarchive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.applikeysolutions.cosmocalendar.selection.OnDaySelectedListener;
import com.applikeysolutions.cosmocalendar.selection.SingleSelectionManager;
import com.applikeysolutions.cosmocalendar.settings.appearance.ConnectedDayIconPosition;
import com.applikeysolutions.cosmocalendar.settings.lists.connected_days.ConnectedDays;
import com.applikeysolutions.cosmocalendar.view.CalendarView;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private CalendarView calendarView;
    private TextView textView, dateView;

    private DBManager dbManager = new DBManager(this);
    private Set<Long> days = new TreeSet<>();

    private Date parse_date;
    private final Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    private SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy년  MM월  dd일");
    private int date = Integer.parseInt(dateFormat.format(calendar.getTime()));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        calendarView = findViewById(R.id.calendar_view);
        textView = findViewById(R.id.tv_content);
        dateView = findViewById(R.id.dateView);

        PermissionListener permissionListener = new PermissionListener() {                          //  TedPermission 설정
            @Override
            public void onPermissionGranted() { }
            @Override
            public void onPermissionDenied(List<String> deniedPermissions) { }
        };
        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setDeniedMessage("접근 거부하셨습니다T^T\n[설정] - [권한]에서 권한을 허용해주세요.")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();

        textView.setMovementMethod(new ScrollingMovementMethod());  // textView설정
        textView.scrollTo(0,0);

        setSupportActionBar(toolbar);                               // 툴바 설정

        dateView.setText(dateFormat2.format(calendar.getTime()));   // 기본날짜 설정

        calendarView.setSelectionManager(new SingleSelectionManager(new OnDaySelectedListener() {
            @Override
            public void onDaySelected() {

                List<Calendar> days = calendarView.getSelectedDates();
                Calendar calendar = days.get(0);
                dateView.setText(dateFormat2.format(calendar.getTime()));
                date = Integer.parseInt(dateFormat.format(calendar.getTime()));
                String content = dbManager.selectDB(date);

                if(content != null){
                    textView.setText(content + "");
                    textView.setGravity(Gravity.TOP);
                }else {
                    textView.setText("\" 어떤 하루였나요? \"");
                    textView.setGravity(Gravity.CENTER);
                }
            }
        }));

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
        public void onClick(View view) {                    // textView 클릭 시 date 값 가지고 이동
                Intent intent = new Intent(getBaseContext(), ShowActivity.class);
                intent.putExtra("date", date);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        ArrayList<ListData> arrayList = dbManager.selectAllDB();    // 일기 전체조회 후 캘린더에 표시

        for(int i=0; i < arrayList.size(); i++) {
            int date = arrayList.get(i).getTv_date();
            try {
                parse_date = dateFormat.parse(String.valueOf(date));
            }catch (Exception e){ e.printStackTrace(); }

            days.add(parse_date.getTime());
        }
        int textColor = Color.parseColor("#BBCEFF");
        ConnectedDays connectedDays = new ConnectedDays(days, textColor);
        calendarView.setConnectedDayIconRes(R.drawable.ic_baseline_check_12);
        calendarView.setConnectedDayIconPosition(ConnectedDayIconPosition.TOP);
        calendarView.addConnectedDays(connectedDays);

        final String content = dbManager.selectDB(date);            // 일기 조회

        if(content != null){
            textView.setText(content + "");
            textView.setGravity(Gravity.TOP);
        }else {
            textView.setText("\" 어떤 하루였나요? \"");
            textView.setGravity(Gravity.CENTER);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        calendarView.clearSelections();                             // 날짜선택 해제
        days.clear();                                               // 저장된 일기 표시 초기화
    }

    @Override
    public void onBackPressed() {                                   // 뒤로가기버튼 클릭 시 Dialog
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("앱 종료");
        dialog.setMessage("앱을 종료하시겠습니까?");
        dialog.setNegativeButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        dialog.setPositiveButton("아니오", null);
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {                                                 // 메뉴 연결
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {                                  // 메뉴별 수행작업
        switch (item.getItemId()){
            case R.id.list:
                startActivity(new Intent(this, RecyclerActivity.class));
                break;
            case R.id.action_settings:
                startActivity(new Intent(this, PreferencesActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}