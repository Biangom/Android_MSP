package com.example.kss78.assignment2;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by kss78 on 2018-04-19.
 */

public class EncounterMonitor extends Service {
    private static final String TAG = "EncounterMonitor";

    BluetoothAdapter mBTAdapter;
    String btName;
    String userName;
    boolean isEncountering = false;

    Timer timer = new Timer();
    TimerTask timerTask = null;

    Vibrator vib;

    int count = 0; // discovery를 실행할때 count회만큼 블루투스 기기의 목록이 떠야 encount이다.

    TextFileManager mFileMgr; // 파일 입출력을 위한 변수 선언.

    // 날짜관련
    long mNow;
    Date mDate;
    SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");


    // BT 검색과 관련한 broadcast를 받을 BroadcastReceiver 객체 정의
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // 각 브로드캐스트에 마다 달리 실행
            if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                // discovery 시작됨
                // 아래는 toast 메시지 표시하는 코드
                Toast.makeText(getApplicationContext(), "Bluetooth scan started..", Toast.LENGTH_SHORT).show();
            } else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                // discovery 종료됨
                // 아래는 toast 메시지 표시하는 코드
                Toast.makeText(getApplicationContext(), "Bluetooth scan finished..", Toast.LENGTH_LONG).show();
            } else if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                // 하나 하나씩 블루투스 기기가 검색된다.
                // 핸드폰 주변에 10대의 기기가 있으면 하나하나씩 브로드캐스트를 받는다.
                // 10대의 각 디바이스 마다 개별적으로 들어온다.

                // Bluetooth device가 검색 됨
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);

                if (btName.equals(device.getName())) {
                    // 검색된 디바이스 이름이 등록된 디바이스 이름과 같으면
                    // 진동과 Toast 메시지 표시
                    count++;
                    if(count == 2) {
                        vib.vibrate(200);
                        // 이 부분에서 시간/이름/정보를 토스트메세지로 출력한 뒤
                        // 파일 입력
                        Toast.makeText(getApplicationContext(), "You encounter " + userName,
                                Toast.LENGTH_LONG).show();

                        mFileMgr.save(getTime() + " "  + userName + "\r\n");
                        count = 0;
                    }
                }
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");

        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // BT 디바이스 검색 관련하여 어떤 종류의 broadcast를 받을 것인지 IntentFilter로 설정
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);


        // BroadcastReceiver 등록
        registerReceiver(mReceiver, filter);

        mFileMgr = new TextFileManager(); // 파일매니저 생성
    }


    // onCreate() -> onStartCommand() 순으로 호출
    // startService() 호출시 onStartCommand 호출이 됨

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // intent: startService() 호출 시 넘기는 intent 객체
        // flags: service start 요청에 대한 부가 정보. 0, START_FLAG_REDELIVERY, START_FLAG_RETRY
        // startId: start 요청을 나타내는 unique integer id

        // 모니터링 시간을 파일 txt에 저장
        mFileMgr.save("모니터링 시작 - " + getTime() + "\r\n");

        Toast.makeText(this, "EncounterMonitor 시작", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onStartCommand()");

        // MainActivity에서 Service를 시작할 때 사용한 intent에 담겨진 BT 디바이스와 사용자 이름 얻음
        btName = intent.getStringExtra("BTName");
        userName = intent.getStringExtra("UserName");

        // 주기적으로 BT discovery 수행하기 위한 timer 가동
        startTimerTask();

        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {

        // 모니터링 식나을 string에 저장하고
        mFileMgr.save("모니터링 종료 - " + getTime()  + "\r\n");

        // 그것을 파일에 저장
        Toast.makeText(this, "EncounterMonitor 중지", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onDestroy()");

        stopTimerTask();
        unregisterReceiver(mReceiver);
    }

    private void startTimerTask() {
        // TimerTask 생성한다
        timerTask = new TimerTask() {
            @Override
            public void run() {
                mBTAdapter.startDiscovery(); // 주기적으로 discovery // cancelDiscovery()
            }
        };

        // TimerTask를 Timer를 통해 실행시킨다
        // 1초 후에 타이머를 구동하고 30초마다 반복한다
        timer.schedule(timerTask, 1000, 15000);
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

    // startDiscovery를 하고 10초에서 12초뒤에 알아서 브로드캐스트에게 discovery Finished 전달한다.

    private String getTime(){
        mNow = System.currentTimeMillis();
        mDate = new Date(mNow);
        return mFormat.format(mDate);
    }

}
