package com.example.kss78.assignment2;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    boolean isPermitted = false;
    final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;

    BluetoothAdapter mBTAdapter;
    BluetoothManager mBTManager;
    static final int REQUEST_ENABLE_BT = 1;
    static final int REQUEST_ENABLE_DISCOVER = 2;
    TextView logText;
    String btName;
    String userName;
    EditText bt;
    EditText user;

    // 파일 매니저 관련 설정
    TextFileManager mFileMgr;

    final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    // TimerTask 관련 설정
    Timer timer = new Timer();
    TimerTask timerTask = null;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestRuntimePermission();

        logText = (TextView)findViewById(R.id.logText);
        logText.setMovementMethod(new ScrollingMovementMethod());
        bt = (EditText)findViewById(R.id.btName);
        user = (EditText)findViewById(R.id.userName);

        // Bluetooth Adapter 얻기 ========================//
        // 1. BluetoothManager 통해서
        mBTManager = (BluetoothManager)getSystemService(BLUETOOTH_SERVICE);
        mBTAdapter = mBTManager.getAdapter(); // project 생성시 Minimum SDK 설정에서 API level 18 이상으로 선택해야
        // 2. BluetoothAdapter 클래스의 static method, getDefaultAdapter() 통해서
        //mBTAdapter = BluetoothAdapter.getDefaultAdapter();

        mFileMgr = new TextFileManager();


        // BT adapter 확인 ===============================//
        // 장치가 블루투스를 지원하지 않는 경우 null 반환
        if(mBTAdapter == null) {
            // 블루투스 지원하지 않기 때문에 블루투스를 이용할 수 없음
            // alert 메세지를 표시하고 사용자 확인 후 종료하도록 함
            // AlertDialog.Builder 이용, set method에 대한 chaining call 가능
            new AlertDialog.Builder(this)
                    .setTitle("Not compatible")
                    .setMessage("Your device does not support Bluetooth")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
        } else {
            // 블루투스 이용 가능 - 스캔하고, 연결하고 등 작업을 할 수 있음

            // 필요한 경우, 블루트스 활성화 ========================================//
            // 블루투스를 지원하지만 현재 비활성화 상태이면, 활성화 상태로 변경해야 함
            // 이는 사용자의 동의를 구하는 다이얼로그가 화면에 표시되어 사용자가 활성화 하게 됨
            if(!mBTAdapter.isEnabled()) {
                // 비활성화 상태
                Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
            } else {
                // 활성화 상태
                // 스캔을 하거나 연결을 할 수 있음
            }
        }

        // 처음 logText 설정
        try {
            String data = mFileMgr.load();
            logText.setText(data);
        }catch (NullPointerException e){
        }

        //Timer Start 설정
        startTimerTask();



    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent data) {
        // 요청 코드에 따라 처리할 루틴을 구분해줌
        switch(requestCode) {
            case REQUEST_ENABLE_BT:
                if(responseCode == RESULT_OK) {
                    // 사용자가 활성화 상태로 변경하는 것을 허용하였음
                } else if(responseCode == RESULT_CANCELED) {
                    // 사용자가 활성화 상태로 변경하는 것을 허용하지 않음
                    // 블루투스를 사용할 수 없으므로 애플리케이션 종료
                    finish();
                }
                break;
            case REQUEST_ENABLE_DISCOVER:
                if(responseCode == RESULT_CANCELED) {
                    // 사용자가 DISCOVERABLE 허용하지 않음 (다이얼로그 화면에서 거부를 선택한 경우)
                    Toast.makeText(this, "사용자가 discoverable을 허용하지 않았습니다.", Toast.LENGTH_SHORT).show();
                }
        }
    }

    // "Register BT device and user", "Start/Stop encounter monitoring" 버튼을 눌렀을 때 실행되는 메소드
    // 레이아웃 파일 activity_main.xml 파일에서 해당 버튼의 onClick 속성 값으로 지정되어 있는 상태
    public void onClick(View view) {
        if(view.getId() == R.id.regiBtn) {
            Toast.makeText(this, "BT 디바이스와 사용자 이름 등록!!", Toast.LENGTH_LONG).show();

            // EditText에 입력된 디바이스와 사용자 이름을 String 변수에 담음
            btName = bt.getText().toString();
            userName = user.getText().toString();

        } else if(view.getId() == R.id.startMonitorBtn) {
            // 등록된 BT 디바이스 이름을 주기적으로 검색하여 등록된 사용자와 encounter 모니터를 시작한다
            // 모니터를 수행하는 것은 Service로 구현
            // Service를 EncounterMonitor라는 이름의 클래스로 구현하고 startService로 이 Service를 시작
            // 위에서 모니터링 등록을 한 BT 디바이스 이름을 intent에 담아서 전달
            Intent intent = new Intent(this, EncounterMonitor.class);

            if(btName == null) {return;}

            intent.putExtra("BTName", btName);
            intent.putExtra("UserName", userName);

            startService(intent);
        } else if(view.getId() == R.id.stopMonitorBtn) {
            stopService(new Intent(this, EncounterMonitor.class));

        } else if(view.getId() == R.id.resetBtn) {
//            mFileMgr.save("r123123123");
//            mFileMgr.delete();
                show();
        }
    }

    // "Enable Bluetooth Discoverable" 버튼을 눌렀을 때 실행되는 callback 메소드
    // 레이아웃 파일 activity_main.xml 파일에서 해당 버튼의 onClick 속성 값으로 지정되어 있는 상태
    public void onClickDiscover(View view) {
        // BT discoverable을 요청하기 위한 Intent action은 정의되어 있음
        // 이를 이용하여 intent 객체를 생성
        Intent discoverIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);

        // 검색 가능 시간 설정. 기본적으로는 120초 동안 검색 가능
        // 앱이 설정할 수 있는 최대 시간 3600초, 값이 0인 경우 항상 검색 가능
        // 0 미만, 3600 초과 값은 120초로 자동 설정
        discoverIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);

        // 검색 가능하게 허용할 것인지 사용자에게 묻는 activity를 실행, 화면에 다이얼로그 같은 형태로 표시됨
        startActivityForResult(discoverIntent, REQUEST_ENABLE_DISCOVER);
    }

    private void requestRuntimePermission() {
        //*******************************************************************
        // Runtime permission check
        //*******************************************************************
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                // No explanation needed, we can request the permission.
                // 모달창 뜨는거

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            }
        } else {
            // ACCESS_COARSE_LOCATION 권한이 있는 것
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
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // read_external_storage-related task you need to do.

                    // grantResults는 모달창에서의 확인 버튼3
                    // String perimissions 배열 번지에 해당하는 Access가 int grantResults배열에 담겨져있다!

                    // ACCESS_COARSE_LOCATION 권한을 얻음
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

    public void onDestroy() {
        super.onDestroy();
        stopTimerTask();
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
                            logText = (TextView)findViewById(R.id.logText);
                            String data = mFileMgr.load();
                            logText.setText(data);
                        }catch (NullPointerException e){
                        }
                    }
                });
            }
        };


        // TimerTask를 Timer를 통해 실행시킨다
        // 1초 후에 타이머를 구동하고 5초마다 반복한다
        timer.schedule(timerTask, 1000, 5000);
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

    // 모달창 띄우기
    void show()
    {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("AlertDialog Title");
//        builder.setMessage("AlertDialog Content");
//        builder.setPositiveButton("예",
//                new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        Toast.makeText(getApplicationContext(),"예를 선택했습니다.",Toast.LENGTH_LONG).show();
//                    }
//                });
//        builder.setNegativeButton("아니오",
//                new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        Toast.makeText(getApplicationContext(),"아니오를 선택했습니다.",Toast.LENGTH_LONG).show();
//                    }
//                });
//        builder.show();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_login, null);
        builder.setView(view);
        final Button submit = (Button) view.findViewById(R.id.buttonSubmit);
        final EditText email = (EditText) view.findViewById(R.id.edittextEmailAddress);
        final EditText password = (EditText) view.findViewById(R.id.edittextPassword);

        final AlertDialog dialog = builder.create();
        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String strEmail = email.getText().toString();
                String strPassword = password.getText().toString();
                Toast.makeText(getApplicationContext(), strEmail+"/"+strPassword,Toast.LENGTH_LONG).show();

                dialog.dismiss();
            }
        });

        dialog.show();
    }




}
