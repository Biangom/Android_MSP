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
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by kss78 on 2018-04-19.
 */

public class EncounterMonitor extends Service {
    private static final String TAG = "EncounterMonitor";

    BluetoothAdapter mBTAdapter; // 블루투스에 관한 broadcast 정보를 얻어오기위한 어답터변수 선언

    Timer timer = new Timer();
    TimerTask timerTask = null; // 주기적으로 실행할 함수를 담을 변수선언

    Vibrator vib; // 진동 설정에 관한 변수

    TextFileManager mFileMgr; // 파일 입출력을 위한 변수 선언.

    // 날짜관련
    long mNow;  // time을 저장하는 변수
    Date mDate; // time에 해당하는 날짜를 담을 객체
    // format을 지정해주는 mFormat 객체 생성
    SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    // dataList관련
    ArrayList<BtDevice> _dataList = new ArrayList<BtDevice>();


    // BT 검색과 관련한 broadcast를 받을 BroadcastReceiver 객체 정의
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // 각 브로드캐스트에 마다 달리 실행
            if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                // discovery 시작될때 Receive는 이 부분을 실행하게 된다.
                Toast.makeText(getApplicationContext(), "Bluetooth scan started..", Toast.LENGTH_SHORT).show();
            } else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                // discovery 종료할때 Receive는 이 부분을 실행하게 된다.
                Toast.makeText(getApplicationContext(), "Bluetooth scan finished..", Toast.LENGTH_LONG).show();
            } else if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                // 하나 하나씩 블루투스 기기가 검색된다.
                // 핸드폰 주변에 10대의 기기가 있으면 하나하나씩 브로드캐스트를 받는다.
                // 10대의 각 디바이스 마다 개별적으로 들어온다.

                // Bluetooth device가 검색 됨
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);

                // 현재 _dataList에 저장되있는 블루투스 디바이스들 목록만큼 반복
                for(int i = 0; i < _dataList.size(); i++ ) {
                    if (_dataList.get(i).btName.equals(device.getName())) {
                        // 검색된 디바이스 이름이 등록된 디바이스 이름과 같으면
                        // 그 디바이스의 count를 1 증가시킨다.
                        _dataList.get(i).count++;

                        if(_dataList.get(i).count == 2) {
                            _dataList.get(i).isEncountering = true; // 2번 discovery에 등장하면 Encountering됐다고 인식한다.
                        }

                        if (_dataList.get(i).isEncountering) {
                            // Encouter가 됬다면 진동을 울린다.
                            vib.vibrate(200);
                            // 이 부분에서 시간/이름/정보를 토스트메세지로 출력한 뒤
                            Toast.makeText(getApplicationContext(), "You encounter " + _dataList.get(i).userName,
                                    Toast.LENGTH_LONG).show();

                            // 파일에다가도 쓴다.
                            mFileMgr.save(getTime() + " " + _dataList.get(i).userName + "\r\n");

                            // 그다음 count를 0으로 바꾸고, Encouter도 false로 변경한다.
                            _dataList.get(i).count = 0;
                            _dataList.get(i).isEncountering = false; // 또다시 2번 만났을때 Encountering 됐다고 하기 위해서 false로 해주었다.
                        }
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

        // 화면에도 출력
        Toast.makeText(this, "EncounterMonitor 시작", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onStartCommand()");

        // MainActivity에서 Service를 시작할 때 사용한 intent에 담겨진 BT 디바이스와 사용자 이름 List얻음
        _dataList = intent.getParcelableArrayListExtra("list");


        // 주기적으로 BT discovery 수행하기 위한 timer 가동
        startTimerTask();

        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {

        // 모니터링 시간을 파일에 저장한다.
        mFileMgr.save("모니터링 종료 - " + getTime()  + "\r\n");

        // 하면에도 같이 출력해준다.
        Toast.makeText(this, "EncounterMonitor 중지", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onDestroy()");

        // TimerTask를 중지한다.
        stopTimerTask();
        unregisterReceiver(mReceiver);
    }

    private void startTimerTask() {
        // TimerTask 생성한다
        timerTask = new TimerTask() {
            @Override
            public void run() {
                // startDiscovery를 하고 10초에서 12초뒤에 알아서 브로드캐스트에게 discovery Finished 전달한다.
                mBTAdapter.startDiscovery(); // 주기적으로 discovery
                // cancelDiscovery()는 => discovery종료이다.
            }
        };

        // TimerTask를 Timer를 통해 실행시킨다
        // 1초 후에 타이머를 구동하고 15초마다 반복한다
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

    private String getTime(){
        // mNow에 시간을 생성한 뒤
        // mDate에 그 시간에 해당하는 날짜를 생성한다.
        mNow = System.currentTimeMillis();
        mDate = new Date(mNow);

        // 지정한 포맷으로 날짜데이터를 string으로 변환하여 반환
        return mFormat.format(mDate);
    }

}
