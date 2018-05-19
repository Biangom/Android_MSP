package com.example.kss78.assignment3;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by kss78 on 2018-05-19.
 */



public class WifiService extends Service {
    ArrayList<locationData> loc_elevator;
    ArrayList<locationData> loc_401;
    ArrayList<locationData> loc_408;

    //end
    TextView scanResultText;

    // wifiManger 설정
    WifiManager wifiManager;
    List<ScanResult> scanResultList; // 스캔 결과를 담을 리스트

    // 알람매니저와 알람을 설정할 pending Intent 설정
    AlarmManager am;
    PendingIntent pendingIntent;

    // 파일 매니저 관련 설정
    TextfileManager mFileMgr;


    // 날짜관련
    long mNow;
    Date mDate;
    SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    @Override
    public void onCreate() {

        wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        if(wifiManager.isWifiEnabled() == false)
            wifiManager.setWifiEnabled(true);

        // 알람 매너지 생성
        am = (AlarmManager)getSystemService(ALARM_SERVICE);

        // first setting
        loc_elevator = new ArrayList<locationData>();
        loc_401 = new ArrayList<locationData>();
        loc_408 = new ArrayList<locationData>();

        loc_elevator.add(new locationData("KUTAP","50:0f:80:b2:51:60",-54, false));
        loc_elevator.add(new locationData("KUTAP_N","50:0f:80:b2:51:61",-54, false));
        //loc_el

        loc_401.add(new locationData("cse2.4G","64:e5:99:db:05:c8", -66, false));
        loc_401.add(new locationData("406","00:08:9f:52:b0:e4", -73, false));

        // 55* 6 / 6
        loc_408.add(new locationData("unknown","40:01:7a:de:11:62", -55, false));
        loc_408.add(new locationData("KUTAP_N","40:01:7a:de:11:60", -55, false));
        // 332 / 6


        // ----------FileManager
        mFileMgr = new TextfileManager();
        Log.w("123123","qweqwe");

    }


    // onCreate() -> onStartCommand() 순으로 호출
    // startService() 호출시 onStartCommand 호출이 됨

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // intent: startService() 호출 시 넘기는 intent 객체
        // flags: service start 요청에 대한 부가 정보. 0, START_FLAG_REDELIVERY, START_FLAG_RETRY
        // startId: start 요청을 나타내는 unique integer id

        mFileMgr.save("모니터링 시작 - " + getTime() + "\r\n");

        // wifi scan 결과 발생 시 전송되는 broadcast를 수신할 receiver 등록
        IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mReceiver, filter);

        // Alarm 발생 시 전송되는 broadcast를 수신할 receiver 등록
        IntentFilter intentFilter = new IntentFilter("kr.ac.koreatech.msp.alarm");
        registerReceiver(AlarmReceiver, intentFilter);

        // Alarm이 발생할 시간이 되었을 때, 안드로이드 시스템에 전송을 요청할 broadcast를 지정
        Intent intent_am = new Intent("kr.ac.koreatech.msp.alarm");
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent_am, 0);

        // Alarm이 발생할 시간 및 alarm 발생시 이용할 pending intent 설정
        // 10초 후 alarm 발생
        am.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 10000, pendingIntent);


        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        try {
            unregisterReceiver(mReceiver);
            // Alarm 발생 시 전송되는 broadcast 수신 receiver를 해제
            unregisterReceiver(AlarmReceiver);
        } catch(IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        // AlarmManager에 등록한 alarm 취소
        // ss: cancel 꼭 해줘야됌
        try {
            am.cancel(pendingIntent);
        } catch(NullPointerException e) {
            e.printStackTrace(); // 예외처리 해줘야된다.
        }
        mFileMgr.save("모니터링 종료 - " + getTime() + "\r\n");

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private BroadcastReceiver AlarmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("kr.ac.koreatech.msp.alarm")) {

                //*****************
                // Alarm이 발생하였을 때 wifi scan을 수행한다
                wifiManager.startScan();
                Toast.makeText(getApplicationContext(), "Wifi Scan start",Toast.LENGTH_LONG).show();
                //-----------------
                // Alarm receiver에서는 장시간에 걸친 연산을 수행하지 않도록 한다
                // Alarm을 발생할 때 안드로이드 시스템에서 wakelock을 잡기 때문에 CPU를 사용할 수 있지만
                // 그 시간은 제한적이기 때문에 애플리케이션에서 필요하면 wakelock을 잡아서 연산을 수행해야 함
                //*****************

                Intent in = new Intent("kr.ac.koreatech.msp.alarm");
                pendingIntent = PendingIntent.getBroadcast(WifiService.this, 0, in, 0);
                am.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + 10000, pendingIntent);
            }
        }
    };

    // Wifi scan 결과를 받는 용도로 사용하는 Broadcast Recevier
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                getWifiInfo();
                isproxim();
            }
        }
    };


    private void isproxim() {
        int SENSE_ELE = 7;
        int SENSE_401 = 8;
        int SENSE_408 = 7;

        int MAX_ELE = 7; // 엘리베이터에서 상위 8개만 검사한다.
        int MAX_408 = 6; // 408호 앞에서  상위 8개만 검사한다.
        int MAX_401 = 16; // 401호 앞에서  상위 8개만 검사한다.

        for(int i = 0; i < MAX_401; i++ ) {
//            Log.w("level difference : ", Integer.toString(Math.abs(scanResultList.get(i).level - loc_elevator.get(0).level)));

            if(i < MAX_ELE) {
                if (scanResultList.get(i).BSSID.equals(loc_elevator.get(0).bssid)) {
                    if (Math.abs(scanResultList.get(i).level - loc_elevator.get(0).level) < SENSE_ELE) {
                        loc_elevator.get(0).isLocated = true;
                    }
                }

                if (scanResultList.get(i).BSSID.equals(loc_elevator.get(1).bssid)) {
                    if (Math.abs(scanResultList.get(i).level - loc_elevator.get(1).level) < SENSE_ELE) {
                        loc_elevator.get(1).isLocated = true;
                    }
                }
            }

            if( i < MAX_408) {
                if (scanResultList.get(i).BSSID.equals(loc_408.get(0).bssid)) {
                    if (Math.abs(scanResultList.get(i).level - loc_408.get(0).level) < SENSE_408) {
                        loc_408.get(0).isLocated = true;
                    }
                }

                if (scanResultList.get(i).BSSID.equals(loc_408.get(1).bssid)) {
                    if (Math.abs(scanResultList.get(i).level - loc_408.get(1).level) < SENSE_408) {
                        loc_408.get(1).isLocated = true;
                    }
                }
            }

            if (scanResultList.get(i).BSSID.equals(loc_401.get(0).bssid)) {
                if (Math.abs(scanResultList.get(i).level - loc_401.get(0).level) < SENSE_401) {
                    loc_401.get(0).isLocated = true;
                }
            }

            if (scanResultList.get(i).BSSID.equals(loc_401.get(1).bssid)) {
                if (Math.abs(scanResultList.get(i).level - loc_401.get(1).level) < SENSE_401) {
                    loc_401.get(1).isLocated = true;
                }
            }
        }


        // 엘리베이터 앞에 위치해 있다면
        if( loc_elevator.get(0).isLocated  && loc_elevator.get(1).isLocated ) {
            mFileMgr.save(getTime() + " 4층 엘리베이터 앞 \r\n");
            Toast.makeText(getApplicationContext(), "4층 엘리베이터 앞",Toast.LENGTH_LONG).show();
        } else if ( loc_401.get(0).isLocated  && loc_401.get(1).isLocated ) {
            mFileMgr.save(getTime() + " 401호 계단 앞 \r\n");
            Toast.makeText(getApplicationContext(), "401호 계단 앞",Toast.LENGTH_LONG).show();
        } else if( loc_408.get(0).isLocated  && loc_408.get(1).isLocated ) {
            mFileMgr.save(getTime() + " 408호 계단 앞 \r\n");
            Toast.makeText(getApplicationContext(), "408호 계단 앞",Toast.LENGTH_LONG).show();
        } else {
            mFileMgr.save(getTime() + " unknown \r\n");
            Toast.makeText(getApplicationContext(), "unknown",Toast.LENGTH_LONG).show();
        }

        for(int i = 0; i < 2; i++) {
            loc_elevator.get(i).isLocated = false;
            loc_401.get(i).isLocated = false;
            loc_408.get(i).isLocated = false;
        }
        //scanResultText.setText(mFileMgr.load());
    }

    private void getWifiInfo() {
        scanResultList = wifiManager.getScanResults();

        // 정렬하기
        for(int i = 0; i < scanResultList.size() - 1; i++) {
            for(int j = i; j < scanResultList.size(); j++) {
                if(scanResultList.get(i).level < scanResultList.get(j).level) {
                    ScanResult temp = scanResultList.get(i);
                    scanResultList.set(i, scanResultList.get(j));
                    scanResultList.set(j, temp);
                }
            }
        }
    }

    private String getTime(){
        mNow = System.currentTimeMillis();
        mDate = new Date(mNow);
        return mFormat.format(mDate);
    }
}
