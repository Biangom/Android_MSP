package omg.techdown.a0327_basictest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Intent intent;

    public class Data {
        String name;
        float lat;
        float lon;
        int rad;

        public Data(String name, float lat, float lon, int rad) {
            this.name = name;
            this.lat = lat;
            this.lon = lon;
            this.rad = rad;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public float getLat() {
            return lat;
        }

        public void setLat(float lat) {
            this.lat = lat;
        }

        public float getLon() {
            return lon;
        }

        public void setLon(float lon) {
            this.lon = lon;
        }

        public int getRad() {
            return rad;
        }

        public void setRad(int rad) {
            this.rad = rad;
        }
    }

    ArrayList<Data> dataList = new ArrayList<Data>();

    Button btnNext;
    TextView[] idName = new TextView[3];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        idName[0] = (TextView)findViewById(R.id.IdName1);
        idName[1] = (TextView)findViewById(R.id.IdName2);
        idName[2] = (TextView)findViewById(R.id.IdName3);

        for(int i = 0; i < 2; i++) {
            // ArrayList에다가 넣을꺼 테스트
            Data temp = new Data(Integer.toString(i), 1 , 1,5);
            dataList.add(temp);
        }

//        // addActivitiy에서 보내준 intent를 받는다.
//        Intent intent = getIntent(); // add인텐트에서 갖고온거
//        String str = intent.getStringExtra( "t"); //t 라는 이름의 키가 있따면 str에 담아준다.
//        idName1.setText(str);

        btnNext = (Button)findViewById(R.id.IdBtnNext);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //만약 3개가 꽉찼다면 입력을 받으면 안됀다.
                if(dataList.size() == 3) {
                    Toast.makeText(MainActivity.this, "요소가 꽉찼습니다!", Toast.LENGTH_SHORT).show();
                    return;
                }
                //다음 패이지로 화면을 전환
                // 화면을 전환할때 사용하는 클래스 그것이 Intent 클래스이다.
                // 첫번쨰 파라미터는 이동 전 액티비티, 두번쨰 파라미터는 이동할 액티비티
                // 이 인텐트는 a에서 b로 이동한다는 화면 전환 정보를 가지고 있다./
                Intent intent = new Intent(MainActivity.this, AddActivity.class );
                // 화면 전환하기!!
                startActivityForResult(intent, 0);
            }


        });

        for(int i = 0; i< dataList.size() ; i++) {
            idName[i].setText(dataList.get(i).name);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // 아 이코드가 반드시 들어가야 돼구나!
        if (resultCode != RESULT_OK) {
            Toast.makeText(MainActivity.this, "결과가 성공이 아님.", Toast.LENGTH_SHORT).show();
            return;
        }


        //requestCode에 맞는 분기실행
        switch (requestCode) {
            case 0: // AddActivity에서 0으로 해놓았다.
                String str = intent.getStringExtra("result_msg"); // msg키값에 해당하는 거 받기
                Data temp = new Data(str, 1, 1, 1);
                dataList.add(temp);
                idName[dataList.size()-1].setText(temp.name);
                // 이미 이전에 3개가 꽉차면 실행하지 않기로해서 따로 예외처리는 안했다.
                break;


            default:

                break;

        }

    }




}
