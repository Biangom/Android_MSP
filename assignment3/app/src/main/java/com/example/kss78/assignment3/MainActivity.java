package com.example.kss78.assignment3;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    //end
    TextView scanResultText;

    boolean isPermitted = false;
    final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    // 파일 매니저 관련 설정
    TextfileManager mFileMgr;


    // 날짜관련
    long mNow;
    Date mDate;

    // TimerTask 관련 설정
    Timer timer = new Timer();
    TimerTask timerTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestRuntimePermission();

        scanResultText = (TextView)findViewById(R.id.result);

        // 파일 매니저 생성
        mFileMgr = new TextfileManager();
    }

    @Override
    protected void onResume() {
        //Timer Start 설정
        super.onResume();

        try {
            String data = mFileMgr.load();
            scanResultText.setText(data);
        }catch (NullPointerException e) {
        }
        startTimerTask();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimerTask();
    }

    public void onClick(View view) {
        if(view.getId() == R.id.start) {
            //Toast.makeText(this, "WiFi scan start!!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, WifiService.class);
            startService(intent);
            //startScan(); // 내부 클래스의 startScan 함수 실행
        }
        else if(view.getId() == R.id.stop) {
            Intent intent = new Intent(this, WifiService.class);
            stopService(intent);
            //stopRecAndAm(); // 모니터링을 종료하고 브로드캐스트 리시버와 알람을 종료한다.
        }
        else if(view.getId() == R.id.reset) {
            mFileMgr.save(" ");
            //stopRecAndAm(); // 모니터링을 종료하고 브로드캐스트 리시버와 알람을 종료한다.
        }
    }

    private void requestRuntimePermission() {
        //*******************************************************************
        // Runtime permission check
        //*******************************************************************
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && // 여기서 퍼미션을 추가하면 된다.
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                ) {

            // 권한 요청을 거부했을 때.
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) ) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // 권한 요청을 받아 들였을 때

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

            }

            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) ) {

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION); // 이 인자는 key값으로 아무거나 넣어도 상관없다
                // 다만 onRequest에서 해당하는것을 실행할 것이다.

            }
        } else {
            // ACCESS_FINE_LOCATION 권한이 있는 것
            isPermitted = true;
        }
        //*********************************************************************
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // read_external_storage-related task you need to do.

                    // ACCESS_FINE_LOCATION 권한을 얻음
                    isPermitted = true;

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                    // 권한을 얻지 못 하였으므로 location 요청 작업을 수행할 수 없다
                    // 적절히 대처한다
                    isPermitted = false;

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    // 주기적으로 log데이터를 load하는 TimerTask함수 정의
    private void startTimerTask() {

        // TimerTask 생성한다
        timerTask = new TimerTask(){
            @Override
            public void run(){
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 처음 logText 설정
                        try {
                            String data = mFileMgr.load();
                            scanResultText.setText(data);
                        }catch (NullPointerException e){
                        }
                    }
                });
            }
        };


        // TimerTask를 Timer를 통해 실행시킨다
        // 4초 후에 타이머를 구동하고 5초마다 반복한다
        timer.schedule(timerTask, 4000, 5000);
        //*** Timer 클래스 메소드 이용법 참고 ***//
        // 	schedule(TimerTask task, long delay, long period)
        // http://developer.android.com/intl/ko/reference/java/util/Timer.html
        //***********************************//
    }

    private void stopTimerTask() {
        // 1. 모든 태스크를 중단한다
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

}
