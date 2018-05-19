package com.example.kss78.assignment3;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    // 모니터링에 대한 정보를 출력할
    // textview ID를 갖고있는 변수
    TextView scanResultText;

    // 승인이 됐는지 판단하는 함수.
    boolean isPermitted = false;
    final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    // 파일 매니저 관련 설정
    TextfileManager mFileMgr;

    // 주기적으로 함수를 실행하기 위한
    // TimerTask 관련 설정
    Timer timer = new Timer();
    TimerTask timerTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // permission을 요청한다.
        requestRuntimePermission();

        // 모니터링의 정보를 출력할 Text의 id를 얻어와서
        // 저장한다.
        scanResultText = (TextView)findViewById(R.id.result);

        // 파일 매니저 생성
        mFileMgr = new TextfileManager();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 처음에 저장된 log파일을 로드한다.
        // 그런 뒤 textview에 해당 내용을 출력한다.
        try {
            String data = mFileMgr.load();
            scanResultText.setText(data);
        }catch (NullPointerException e) {
        }
        // timertask를 시작한다.
        startTimerTask();

    }

    @Override
    protected void onStop() {
        super.onStop();
        // timertask를 종료한다.
        stopTimerTask();
    }

    public void onClick(View view) {
        // start 버튼을 클릭했을 때
        // 모니터링을 시작한다.
        if(view.getId() == R.id.start) {
            // intent를 wifiservice로 설정한다.
            Intent intent = new Intent(this, WifiService.class);
            startService(intent); // service를 시작한다.
        }
        // stop 버튼을 클릭했을 때
        // 모니터링을 종료한다.
        else if(view.getId() == R.id.stop) {
            Intent intent = new Intent(this, WifiService.class);
            stopService(intent); // service를 종료한다.
        }
        // reset 버튼을 클릭했을 때
        // 해당 log파일을 모두 삭제한다. 즉 초기화한다.
        else if(view.getId() == R.id.reset) {
            mFileMgr.delete(); // 파일 모두 삭제
        }
    }

    private void requestRuntimePermission() {
        //*******************************************************************
        // Runtime permission check
        //*******************************************************************
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && // 여기서 퍼미션을 추가하면 된다. 여기선 쓰기 퍼미션을 추기했다.
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                ) {

            // 권한 요청을 거부했거나
            // wifi 승인요청이 있는지 물어본다.
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) ) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // 그게 아니라면 우리는 퍼미션을 요청한다.
                // 해당 퍼미션은 2개(wifi와 file write퍼미션)있으므로 2개를 요청한다.
                // 모달창을 뜨게한다.
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            }

            // write 승인 요청이 있는지 물어본다.
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) ) {

            } else {
                // 없으면 퍼미션 요청한다.
                // 모달창을 뜨게한다.
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

    // 퍼미션을 요청했을 때 뜨게하는 모달창이다.
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
                        // 파일에서 읽어들여와
                        // logText를 초기화한다.
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
        // 4초 후에 타이머를 구동하고 10초마다 반복한다
        timer.schedule(timerTask, 4000, 10000);
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
