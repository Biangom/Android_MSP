package omg.techdown.a0327_basictest;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
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



    ArrayList<Data> dataList = new ArrayList<Data>();
    ArrayList<Data> subTempList; // Sub activity에서 가져올 리스트
    ArrayList<Data> addTempList; // Add activity에서 가져올 리스트

    Button btnNext;
    TextView[] idName = new TextView[3];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        idName[0] = (TextView)findViewById(R.id.IdName1);
        idName[1] = (TextView)findViewById(R.id.IdName2);
        idName[2] = (TextView)findViewById(R.id.IdName3);

        Data temp1 = new Data("gym", 1 , 1,5);
        dataList.add(temp1);
        Data temp2 = new Data("class room", 2 , 3,15);
        dataList.add(temp2);

//        for(int i = 0; i < 2; i++) {
//            // ArrayList에다가 넣을꺼 테스트
//            Data temp = new Data(Integer.toString(i), 1 , 1,5);
//            dataList.add(temp);
//        }

//        // addActivitiy에서 보내준 intent를 받는다.
//        Intent intent = getIntent(); // add인텐트에서 갖고온거
//        String str = intent.getStringExtra( "t"); //t 라는 이름의 키가 있따면 str에 담아준다.
//        idName1.setText(str);

        btnNext = (Button)findViewById(R.id.IdBtnNext);

        for(int i = 0; i< dataList.size() ; i++) {
            idName[i].setText(dataList.get(i).name);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        for(int i = 0; i < 3; i++) {
            if(i < dataList.size())
                idName[i].setText(dataList.get(i).name);
            else
                idName[i].setText("");
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
        intent.putParcelableArrayListExtra("list2", dataList);

        // 화면 전환하기!!
        // Sub는 requestCode는 1이다.
        startActivityForResult(intent, 1);
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
                addTempList = intent.getParcelableArrayListExtra("addlist");
                dataList.clear();
                for(int i = 0; i < addTempList.size(); i++ ) {
                    dataList.add(addTempList.get(i));
                }

                break;

            case 1: // SubActivity는 1로 해놓았다
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





}
