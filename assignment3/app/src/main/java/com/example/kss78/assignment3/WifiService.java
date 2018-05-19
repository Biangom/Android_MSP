package com.example.kss78.assignment3;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by kss78 on 2018-05-19.
 */

public class WifiService extends Service {
    ArrayList<locationData> loc_elevator; // 4층 엘리베이터앞 정보를 저장하는 데이터 리스트
    ArrayList<locationData> loc_401; // 401층 앞 정보를 저장하는 데이터 리스트
    ArrayList<locationData> loc_408; // 408층 앞 정보를 저장하는 데이터 리스트

    WifiManager wifiManager; // wifi 메소드를 쓰기위해 wifiManger 설정
    List<ScanResult> scanResultList; // 스캔 결과를 담을 리스트

    AlarmManager am; // alarm 메소드를 쓰기위해 알람매니저
    PendingIntent pendingIntent; // 알람 브로드캐스트를 설정할 pending Intent 설정

    TextfileManager mFileMgr; // 파일 매니저 관련 설정

    // 날짜 관련 변수들 설정
    long mNow; // long형의 날짜 데이터값
    Date mDate;  // date형의 날짜 데이터값
    SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    // -> 포맷형식을 년도-월-일 시-분-초 로한다.

    @Override
    public void onCreate() {
        // wifiManager매니저에 서비스를 받아들여온다.
        wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        if(wifiManager.isWifiEnabled() == false) // wifi서비스가 활성화 되자 않았다면
            wifiManager.setWifiEnabled(true); // 활성화한다.

        // 알람 매니저 생성
        am = (AlarmManager)getSystemService(ALARM_SERVICE);

        // 각 위치의 ArrayList를 생성한다.
        loc_elevator = new ArrayList<locationData>();
        loc_401 = new ArrayList<locationData>();
        loc_408 = new ArrayList<locationData>();

        // 각 위치 정보를 생성한다.
        // 각 위치를 구별할 수 있는 AP 2개를 등록한다. (2개의 후보를 만든다)
        // 등록된 2개의 AP의 세기가 해당 MAX_XXX 등 수 안에 들어 가고
        // 그 2개의 AP 세기가 SENSE_??? 차이 이하면
        // 그 장소에 있다고 판단할 예정이다.

        // 즉 해당 위치의 등록된 AP 2개 후보가
        // 검색 리스트안에 해당 등수와 해당 dBm에 들어가면
        // 그 장소에 있다고 판단하는 것이다.

        // 엘리베이터 정보 add
        loc_elevator.add(new locationData("KUTAP","50:0f:80:b2:51:60",-54, false));
        loc_elevator.add(new locationData("KUTAP_N","50:0f:80:b2:51:61",-54, false));

        // 401 정보 add
        loc_401.add(new locationData("cse2.4G","64:e5:99:db:05:c8", -66, false));
        loc_401.add(new locationData("406","00:08:9f:52:b0:e4", -73, false));

        // 408 정보 add
        loc_408.add(new locationData("unknown","40:01:7a:de:11:62", -55, false));
        loc_408.add(new locationData("KUTAP_N","40:01:7a:de:11:60", -55, false));

        // ----------FileManager 생성
        mFileMgr = new TextfileManager();
    }

    // onCreate() -> onStartCommand() 순으로 호출
    // startService() 호출시 onStartCommand 호출이 됨
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // intent: startService() 호출 시 넘기는 intent 객체
        // flags: service start 요청에 대한 부가 정보. 0, START_FLAG_REDELIVERY, START_FLAG_RETRY
        // startId: start 요청을 나타내는 unique integer id

        // service가 시작하면 파일에 모니터링이 시작됐다고 정보를 쓴다.
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
        // 60초 후 alarm 발생하도록 설정
        am.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 60000, pendingIntent);

        return super.onStartCommand(intent, flags, startId);
    }

    // MainActivity stopService를 호출했을 때 실행된다.
    public void onDestroy() {
        // Receiver를 해제한다.
        try {
            // scan 결과를 받았을 때 실행했던 리시버 해제
            unregisterReceiver(mReceiver);
            // Alarm 발생 시 전송되는 broadcast 수신 receiver를 해제
            unregisterReceiver(AlarmReceiver);
        } catch(IllegalArgumentException ex) {
            ex.printStackTrace();
        }

        // AlarmManager에 등록한 alarm 취소
        // 반드시 해야한다.
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
            // 현재 온 브로드캐스트가 알람키값이라면 아래 블록 실행
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

                // 인텐트에 알람키값을 설정한다.
                // 알람은 60초마다 한번씩 실행하도록 한다.
                Intent in = new Intent("kr.ac.koreatech.msp.alarm");
                pendingIntent = PendingIntent.getBroadcast(WifiService.this, 0, in, 0);
                am.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + 60000, pendingIntent);
            }
        }
    };

    // Wifi scan 결과를 받는 용도로 사용하는 Broadcast Recevier
    // Wifi scan 결과르 받았는 브로드캐스트를 받았을 때 실행한다.
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                getWifiInfo(); // 스캔 결과를 받아온다.
                isproxim(); // 해당 스캔 결과를 바탕으로 어디에 위치해있는지 판단한다.
            }
        }
    };

    private void isproxim() {
        // 가장 세기가 큰 순서대로 정렬 한뒤
        // 등록된 2개의 AP의 세기(2개의 후보)가 해당 MAX_XXX 등 수 안에 들어 가고
        // 그 2개의 AP 세기가 SENSE_??? 차이 이하면
        // 그 장소에 있다고 판단한다

        int SENSE_ELE = 7; // dBm차이가 7이하면
        int SENSE_408 = 7; // dBm차이가 7이하면
        int SENSE_401 = 9; // dBm차이가 8이하면

        int MAX_ELE = 7; // 엘리베이터에서 상위 7등안에 들어가는지 검사하기위해 설정된 값
        int MAX_408 = 6; // 408호 앞에서  상위 6등안에 들어가는지 검사하기위해 설정된 값
        int MAX_401 = 26; // 401호 앞에서  상위 26등안에 들어가는지 검사하기위해 설정된 값

        // 우선 401호가 3장소 중 상위 갯수를 검사하는게 가장 크기 때문에
        // 그 만큼 반복을 한다.
        for(int i = 0; i < MAX_401; i++ ) {

            // 엘리베이터에 있을 때 상위 MAX_ELE개만 검사한다,
            if(i < MAX_ELE) {
                if (scanResultList.get(i).BSSID.equals(loc_elevator.get(0).bssid)) {
                    // 그 dBm차이가 SENSE_ELE라면 ELE의 후보 1를 true로 한다,
                    if (Math.abs(scanResultList.get(i).level - loc_elevator.get(0).level) < SENSE_ELE) {
                        loc_elevator.get(0).isLocated = true;
                    }
                }

                if (scanResultList.get(i).BSSID.equals(loc_elevator.get(1).bssid)) {
                    // 그 dBm차이가 SENSE_ELE라면 ELE의 후보 2를 true로 한다,
                    if (Math.abs(scanResultList.get(i).level - loc_elevator.get(1).level) < SENSE_ELE) {
                        loc_elevator.get(1).isLocated = true;
                    }
                }
            }


            // 408호에 있을 때 상위 MAX_408개만 검사한다,
            if( i < MAX_408) {
                if (scanResultList.get(i).BSSID.equals(loc_408.get(0).bssid)) {
                    // 그 dBm차이가 SENSE_408라면 408의 후보 1를 true로 한다,
                    if (Math.abs(scanResultList.get(i).level - loc_408.get(0).level) < SENSE_408) {
                        loc_408.get(0).isLocated = true;
                    }
                }

                if (scanResultList.get(i).BSSID.equals(loc_408.get(1).bssid)) {
                    // 그 dBm차이가 SENSE_408라면 408의 후보 2를 true로 한다,
                    if (Math.abs(scanResultList.get(i).level - loc_408.get(1).level) < SENSE_408) {
                        loc_408.get(1).isLocated = true;
                    }
                }
            }

            if (scanResultList.get(i).BSSID.equals(loc_401.get(0).bssid)) {
                // 그 dBm차이가 SENSE_401라면 401의 후보 1를 true로 한다,
                if (Math.abs(scanResultList.get(i).level - loc_401.get(0).level) < SENSE_401) {
                    loc_401.get(0).isLocated = true;
                }
            }

            if (scanResultList.get(i).BSSID.equals(loc_401.get(1).bssid)) {
                // 그 dBm차이가 SENSE_401라면 401의 후보 2를 true로 한다,
                if (Math.abs(scanResultList.get(i).level - loc_401.get(1).level) < SENSE_401) {
                    loc_401.get(1).isLocated = true;
                }
            }
        }

        // 만약 2개의 후보가 모두 TRUE 일 경우에 그 위치에 있다고 판단한다.
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

        // 판단을 한뒤
        // 모든 후보의 위치의 값을 false로 초기화한다.
        for(int i = 0; i < 2; i++) {
            loc_elevator.get(i).isLocated = false;
            loc_401.get(i).isLocated = false;
            loc_408.get(i).isLocated = false;
        }
    }

    // 스캔의 결과값을 받아오는 함수이다/
    private void getWifiInfo() {
        // 스캔 결과값을 scanResultList의 배열에 담는다.
        scanResultList = wifiManager.getScanResults();

        // 내림차순 정렬
        Descending descending = new Descending();
        Collections.sort(scanResultList, descending);
    }

    // sort를 쓰기위한 comparator 정의
    // 내림차순으로 하기 위함
    class Descending implements Comparator<ScanResult> {

        @Override
        public int compare(ScanResult scanResult, ScanResult t1) {
            return t1.level - scanResult.level;
        }
    }


    // 현재 시간을 지정한 format에 맞게
    // string 값으로 반환하는 함수이다.
    private String getTime(){
        mNow = System.currentTimeMillis(); // 현재 시간을 millis단위로 얻어온다.
        mDate = new Date(mNow); // 그 시간에 해당하는 값을 date자료형으로 넣는다.
        return mFormat.format(mDate); // 그 date자료형에 해당하는 값을 string으로 바꾼다.
    }
}
