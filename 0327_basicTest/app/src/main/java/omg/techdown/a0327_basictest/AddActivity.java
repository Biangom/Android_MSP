package omg.techdown.a0327_basictest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class AddActivity extends AppCompatActivity {

    ArrayList<Data> _dataList;
    Data tempData = new Data();
    Button btnPrev;

    EditText[] etName = new EditText[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        etName[0] = (EditText) findViewById(R.id.IdName);
        etName[1] = (EditText) findViewById(R.id.IdLat);
        etName[2] = (EditText) findViewById(R.id.IdLon);
        etName[3] = (EditText) findViewById(R.id.IdRad);
        btnPrev = (Button)findViewById(R.id.IdBtnPrev);


//        btnPrev.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                // 이전 페이지로 화면 전환
//                // 화면을 전환할때 사용하는 클래스 그것이 Intent 클래스이다.
//            }
//        });
        Intent intent = getIntent();
        _dataList = intent.getParcelableArrayListExtra("list1");

    }

    public void homeMove(View view)
    {
        // 예외처리부분
        for(int i = 0; i < _dataList.size(); i++ ) {
            if( _dataList.get(i).name.equals(tempData.name)) {
                Toast.makeText(this, "이미 존재하는 이름입니다.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

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

        tempData.name = etName[0].getText().toString();
        tempData.lat = Float.parseFloat(etName[1].getText().toString());
        tempData.lon = Float.parseFloat(etName[2].getText().toString());
        tempData.rad = Integer.parseInt(etName[3].getText().toString());





        _dataList.add(tempData);


        Intent intent = new Intent();
        intent.putParcelableArrayListExtra("addlist", _dataList); // ArrayList 보내기
        setResult(RESULT_OK, intent); // 안전하게 보냈다
        finish();// 현재 액티비티 종료


    }


}
