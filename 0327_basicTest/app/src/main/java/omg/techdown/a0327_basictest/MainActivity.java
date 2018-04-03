package omg.techdown.a0327_basictest;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private static final String FILENAME = "listData.txt";      // 파일 입출력을 위한 파일명 담는 변수
    LocationManager locManager;                                 // location Manager로 쓰일 변수
    AlertReceiver receiver;                                     // 경보받을 리시버
    PendingIntent[] proximityIntentList;                        // 3개를 선언할 것이므로
    boolean isPermitted = false;                                // permit이 되어있는지 check
    boolean isLocRequested = false;                             // locationRequest 받았는지 check
    boolean isAlertRegistered = false;                          // Alert이 등록되었는지 check
    final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;  // FINE 옵션으로 설정
//    ------------------

    private Intent intent;

    ArrayList<Data> dataList = new ArrayList<Data>();   // Data 자료형 리스트, Data 자료형은 이름, 위도, 경도, 반경 정보가 들어있다.
    ArrayList<Data> subTempList; // Sub activity에서 가져올 데이터 리스트
    ArrayList<Data> addTempList; // Add activity에서 가져올 데이터 리스트

    TextView[] idName = new TextView[3];    // 3가지 데이터들의 이름만 표현하는 TextView들이 담긴 배열

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        idName[0] = (TextView) findViewById(R.id.IdName1);
        idName[1] = (TextView) findViewById(R.id.IdName2);
        idName[2] = (TextView) findViewById(R.id.IdName3);

        // 처음에 파일이 있다면 load(input)한다.
        try {
            inputFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 초기에 data들의 이름으로 TextView들을 초기화
        for (int i = 0; i < dataList.size(); i++) {
            idName[i].setText(dataList.get(i).name);
        }

        // ---proximityAlert 셋팅----
        proximityIntentList = new PendingIntent[3]; // 3개의 데이터가 있으므로 길이는 3으로 한다.

        // location Manager 얻기
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        requestRuntimePermission(); // 퍼미션 체크
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, this);

    }

    private void requestRuntimePermission() {
        //*******************************************************************
        // Runtime permission check
        //*******************************************************************
        // 즉 PERMISSION_GRANTED(승인) 된 상태가 아니면 if문 진행
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                // 내가 요청할 퍼미션의 종류가 정의 되고 있고, 퍼미션을 요청한다.
                // 이게 호출이 되면 안드로이드 시스템이 받아서 모달창하나(위치 허락하시겠습니까?)가 뜨게 된다.

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                //MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION 는 request하는데 아이디라고 생각하면 된다.
                // 이제  onRequestPermissionsResult가 실행된다.
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
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //  grantResults[0] == PackageManager.PERMISSION_GRANTED 는 allow 버튼을 눌렀니? 의 의미
                    // allow버튼 눌렸으면 값에 하나라도 있을 것이다.(즉 0이 아닐 것이다)

                    // permission was granted, yay! Do the
                    // read_external_storage-related task you need to do.

                    // ACCESS_FINE_LOCATION 권한을 얻었으므로
                    // 관련 작업을 수행할 수 있다

                    // permission was granted, yay! Do the
                    // read_external_storage-related task you need to do.

                    // ACCESS_FINE_LOCATION 권한을 얻음
                    isPermitted = true;
                } else {
                    // 권한을 얻지 못 하였으므로 location 요청 작업을 수행할 수 없다
                    // 적절히 대처한다
                    isPermitted = false;
                }
                return;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 다시 재개할 때, 모든 textView 들을 초기화 해야한다.
        for(int i = 0; i < 3; i++) {
            if(i < dataList.size())
                idName[i].setText(dataList.get(i).name);
            else
                idName[i].setText("");
        }

        // 모든 데이터 간에 Alert을 진행한다.
        for(int i = 0; i < dataList.size(); i++)
            startAlert(i);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 앱이 종료될 시점에 파일 저장
        try {
            outputFile();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();


        //-------------------------alert
        // 앱이 다른 액티비티로 가거나 종료되는 등 화면 벗어날때 Alert들 을 종료해준다.
        for(int i = 0; i < dataList.size(); i++)
            exitAlert(i);

        // 등록되었을 떄만 해제하기 위함
        try {
            unregisterReceiver(receiver);
        }
        catch (IllegalArgumentException e)
        {
            return;
        }
    }


    public void startAlert(int index) {
        // 근접 경보를 받을 브로드캐스트 리시버 객체 생성 및 등록
        // 액션이 kr.ac.koreatech.msp.locationAlert인 브로드캐스트 메시지를 받도록 설정
        receiver = new AlertReceiver();
        IntentFilter filter = new IntentFilter("dis"+index); // action을 filter마다 달리 지정해주어야지 여러 가지 근접경보를 받을 수 있다.
        registerReceiver(receiver, filter);

        // ProximityAlert 등록을 위한 PendingIntent 객체 얻기
        Intent intent = new Intent("dis"+index);
        intent.putExtra("name",dataList.get(index).name);
        proximityIntentList[index] = PendingIntent.getBroadcast(this, 0, intent, 0);
        try {
            // 근접 경보 등록 메소드
            // void addProximityAlert(double latitude, double longitude, float radius, long expiration, PendingIntent intent)
            // expiration 만료 매개 변수가 지정한 밀리 초 후에 위치 관리자는이 근접 경고를 삭제하고 더 이상 모니터링하지 않습니다. 값 -1은 만료 시간이 없어야 함을 나타냅니다.
            // 아래 위도, 경도 값의 위치는 2공학관 420호 창가 부근
            locManager.addProximityAlert(dataList.get(index).lat, dataList.get(index).lon, dataList.get(index).rad, 20000, proximityIntentList[index]);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        isAlertRegistered = true;
    }

    // 해당하는 proximityIntentList의 근접경보를 해제하는 코드
    public void exitAlert(int index) {
        // 자원 사용 해제
        try {
            if(isLocRequested) {
                locManager.removeUpdates(this); // Updates를 더이상 안한다.
                isLocRequested = false;
            }
            if(isAlertRegistered) {
                locManager.removeProximityAlert(proximityIntentList[index]);  // 해당 proximityIntent를 해제한다.
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }

    }

    // 파일 가져오는 함수
    public void inputFile() throws IOException {

        File file = new File(getFilesDir(), FILENAME); // 파일 객체를 생성한다
        FileReader fr = null; // 파일를 읽는 객체를 생성한다.
        BufferedReader bufread = null; // 파일 읽기용 버퍼 만든다

        if (file.exists()) // 만약에 파일이 존재한다면?
        {
            try
            {
                // 파일 읽는다.
                fr = new FileReader(file);
                bufread = new BufferedReader(fr);

                String str;
                while ((str = bufread.readLine()) != null)
                {
                    String[] data = str.split("/"); // 슬래시로 구분해준다.
                    Data temp = new Data();
                    temp.name = data[0];
                    temp.lat = Double.parseDouble(data[1]);
                    temp.lon = Double.parseDouble(data[2]);
                    temp.rad = Float.parseFloat(data[3]);

                    dataList.add(temp);
                }
                bufread.close();
                fr.close();
                Toast.makeText(this, "파일 읽기 완료~!", Toast.LENGTH_LONG).show();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    // 파일 저장하는 함수
    public void outputFile() throws IOException {
        File file = new File(getFilesDir(), FILENAME); // 데이터를 저장할 객체 생성
        FileWriter fw = null;        // 파일쓰기 객체
        BufferedWriter bufwrit = null; // 파일 쓰기용 버퍼
        try
        {
            // open file.
            fw = new FileWriter(file);
            bufwrit = new BufferedWriter(fw);

            for (int i = 0; i < dataList.size(); i++)
            {
                bufwrit.write(dataList.get(i).name + "/" + dataList.get(i).lat + "/" +
                        dataList.get(i).lon + "/" + dataList.get(i).rad);
                bufwrit.newLine();
            }
            // 버퍼를 비운다.
            bufwrit.flush();
            Toast.makeText(this, "파일 저장 완료~!", Toast.LENGTH_LONG).show();
        }
        catch (Exception e) {
            e.printStackTrace() ;
        }

        try
        {
            // 파일을 닫는다.
            if (bufwrit != null) bufwrit.close();
            if (fw != null) fw.close();
        }
        catch (Exception e) {
            e.printStackTrace() ;
        }
    }

    public void onClickAdd(View view) {
        if(dataList.size() == 3) {
            Toast.makeText(MainActivity.this, "요소가 꽉찼습니다!", Toast.LENGTH_SHORT).show();
            return;
        }
        //다음 패이지로 화면을 전환
        // 화면을 전환할때 사용하는 클래스 그것이 Intent 클래스이다.
        // 첫번쨰 파라미터는 이동 전 액티비티, 두번쨰 파라미터는 이동할 액티비티
        // 이 인텐트는 a에서 b로 이동한다는 화면 전환 정보를 가지고 있다.
        Intent intent = new Intent(MainActivity.this, AddActivity.class );

        intent.putParcelableArrayListExtra("list1", dataList);
        // 화면 전환하기!!
        startActivityForResult(intent, 0);
    }

    public void onClickSub(View view){
        if(dataList.size() == 0) {
            Toast.makeText(MainActivity.this, "요소가 하나도 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        //다음 패이지로 화면을 전환
        // 화면을 전환할때 사용하는 클래스 그것이 Intent 클래스이다.
        // 첫번쨰 파라미터는 이동 전 액티비티, 두번쨰 파라미터는 이동할 액티비티
        // 이 인텐트는 a에서 b로 이동한다는 화면 전환 정보를 가지고 있다./
        Intent intent = new Intent(MainActivity.this, SubActivity.class );
        intent.putParcelableArrayListExtra("list2", dataList); // ArrayList를 넘겨주기 위해선 Parcelable으로 넘겨주어야함

        // 화면 전환하기!!
        // Sub는 requestCode는 1이다.
        startActivityForResult(intent, 1);
    }

    // 현재 해당하는 번호의 Data에 대한 정보를 보기위한 이벤트 함수
    public void ClcikInfo(View view) {
        int i = -1;
        switch(view.getId()) {
            case R.id.IdInfo1 : // 첫 번째 번호의 정보보기, 그 원소의 갯수가 충분하다면 그 번호 반환
                if(dataList.size() >= 1) i = 0;
                break;
            case R.id.IdInfo2 : // 두 번째 번호의 정보보기
                if(dataList.size() >= 2) i = 1;
                break;
            case R.id.IdInfo3 :// 세 번째 번호의 정보보기
                if(dataList.size() >= 3) i = 2;
                break;
        }
        if( i == -1 )
            Toast.makeText(getApplicationContext(),"정보가 없습니다.", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getApplicationContext(),
                "이름: " + dataList.get(i).name + "\n위도: " + dataList.get(i).lat + "\n경도: " + dataList.get(i).lon + "\n반경: " + dataList.get(i).rad, Toast.LENGTH_LONG).show();
    }


    // 액티비티 간 이동하고 다시 이 액티비티로 돌아올 때 하는 행동 정의
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // 아 이 코드가 반드시 들어가야 됀다
        // 뒤로가기 또는 비정상적인 종료라면 실행되야 되는 부분
        if (resultCode != RESULT_OK) {
            Toast.makeText(MainActivity.this, "데이터가 입력이 안된 채로 종료되었습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        //건너온 액티비티의 requestCode에 맞는 분기실행
        switch (requestCode) {
            case 0: // AddActivity에서 0으로 해놓았다.
                // AddActivitiy에서 실행했던 addList를 통째로 받아와 초기화한다.
                addTempList = intent.getParcelableArrayListExtra("addlist");
                dataList.clear();
                for(int i = 0; i < addTempList.size(); i++ ) {
                    dataList.add(addTempList.get(i));
                }
                break;

            case 1: // SubActivity는 1로 해놓았다
                // SubActivitiy에서 실행했던 addList를 통째로 받아와 초기화한다.
                subTempList = intent.getParcelableArrayListExtra("sublist");
                dataList.clear();
                for(int i = 0; i < subTempList.size(); i++ ) {
                    dataList.add(subTempList.get(i));
                }
                break;
            default:
                break;
        }
    }


    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
