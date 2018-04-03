package omg.techdown.a0327_basictest;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class AddActivity extends AppCompatActivity implements LocationListener {

    LocationManager lm;                     //locationManager로 쓰일 변수
    TextView tvLon, tvLat;                  // 위도와 경도를 표시할 TextView의 id
    // -------------------
    ArrayList<Data> _dataList;              // AddActivitiy 내에서 쓰일 dataList, 이 dataList는
                                            // MainActivitiy에서 받아 이 액티비티에서 수정 후 다시
                                            // MainActivitiy로 넘겨줄 예정
    Data tempData = new Data();             // 넣을 데이터 정보를 임시 저장\

    Button btnPrev;                         // 등록 버튼
    Button btnNowReg;                       // 현재 위치로 등록하기 위한 버튼

    EditText[] etName = new EditText[4];    // 총 입력할 데이터는 이름, 위도, 경도, 반경 4가지 데이터이므로 위와 같이 설정하였다.
                                            // 관리하기 편하게

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        // id 얻기
        etName[0] = (EditText) findViewById(R.id.IdName);
        etName[1] = (EditText) findViewById(R.id.IdLat);
        etName[2] = (EditText) findViewById(R.id.IdLon);
        etName[3] = (EditText) findViewById(R.id.IdRad);
        btnPrev = (Button) findViewById(R.id.IdBtnPrev);
        tvLon = (TextView) findViewById(R.id.IdNowLon);
        tvLat = (TextView) findViewById(R.id.IdNowLat);

        btnNowReg = (Button) findViewById(R.id.IdNowReg);

        // LocationManager 참조 객체
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Main Activity에서 데이터 가져오기
        Intent intent = getIntent();
        _dataList = intent.getParcelableArrayListExtra("list1");

    }

    @Override
    protected void onResume() {
        super.onResume();

        //  퍼미션 체크를 해야 requestLocationUpdates를 받을 수 있다.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this); // 정보 얻기
    }

    public void homeMove(View view)
    {
        // 예외처리: 이름이 중복되는지 확인한다.
        for(int i = 0; i < _dataList.size(); i++ ) {
            if( _dataList.get(i).name.equals( etName[0].getText().toString() )) {
                Toast.makeText(this, "이미 존재하는 이름입니다.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // 예외처리: 모든 입력 칸이 비어있는지 확인하다
        for(int i = 0; i < 4; i++ ) {
            if( etName[i].getText().toString().equals("")) {
                String temp;
                if(i == 0) temp = "이름";
                else if(i == 1) temp = "위도";
                else if(i == 2) temp = "경도";
                else temp = "반경";

                Toast.makeText(this, temp + "칸이 비어있습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
        }


        // 데이터를 add할 temp 데이터 값 초기화
        try {
            tempData.name = etName[0].getText().toString();
            tempData.lat = Float.parseFloat(etName[1].getText().toString());
            tempData.lon = Float.parseFloat(etName[2].getText().toString());
            tempData.rad = Integer.parseInt(etName[3].getText().toString());
        }catch (NumberFormatException e) { // 예외처리: 혹시나 toString하는 과정에서 Format이 잘못됬는지 확인한다.
            Toast.makeText(AddActivity.this, "데이터 형식이 잘못되었습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        _dataList.add(tempData); // 데이터 추가

        Intent intent = new Intent();
        intent.putParcelableArrayListExtra("addlist", _dataList); // ArrayList 보내기
        setResult(RESULT_OK, intent); // 안전하게 보냈다
        finish();// 현재 액티비티 종료
    }

    public void nowRegister(View view) {
        // 이 버튼을 클릭하면 현재의 위도, 경도로 값이 입력된다.
        etName[1].setText(tvLat.getText().toString());
        etName[2].setText(tvLon.getText().toString());
    }

    // -----------------------

    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        tvLat.setText(Double.toString(lat)); // 현재 위도 표시
        tvLon.setText(Double.toString(lon)); // 현재 경도 표시

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
