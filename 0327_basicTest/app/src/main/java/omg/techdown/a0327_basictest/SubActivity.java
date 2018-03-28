package omg.techdown.a0327_basictest;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class SubActivity extends AppCompatActivity {

    ArrayList<Data> _dataList;
    String inputName;

    TextView[] idName = new TextView[3];
    EditText EditName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        idName[0] = (TextView) findViewById(R.id.IdTv1);
        idName[1] = (TextView) findViewById(R.id.IdTv2);
        idName[2] = (TextView) findViewById(R.id.IdTv3);
        EditName = (EditText) findViewById(R.id.IdEditText);



        // Main액티비티에서 보내준 intent 받기
        Intent intent = getIntent();

        _dataList = intent.getParcelableArrayListExtra("list2");

        for(int i = 0; i < _dataList.size() ; i++)
            idName[i].setText(_dataList.get(i).name);
    }

    public void onClickSub(View view) {
        boolean DidRemove = false; // 같은 이름이 있다면 루프를 돌때 true로 바꿔질거임
        inputName = EditName.getText().toString();

        // 예외처리 (만약 데이터가 입력되지 않았다면 빈 것이다._
        if(EditName.getText().toString().equals("")) {
            Toast.makeText(SubActivity.this, "이름을 입력해주세요", Toast.LENGTH_SHORT).show();
            return;

        }

        for(int i = 0; i < _dataList.size(); i++) {
            if(_dataList.get(i).name.equals(inputName)) {
                _dataList.remove(i);
                DidRemove = true;
            }
        }

        // 예외처리 (만약 삭제되지 않았다면 해당하는 이름의 데이터가 없는 것이다.)
        if(DidRemove == false) {
            Toast.makeText(SubActivity.this, "해당하는 이름의 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
            return;

        }
        else {
            Toast.makeText(SubActivity.this, "삭제완료", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SubActivity.this, MainActivity.class );

            intent.putParcelableArrayListExtra("sublist", _dataList);

//        intent.putExtra("result_msg", etName.getText().toString()); // t는 키 값임
            setResult(RESULT_OK, intent ); // 안전하게 보냈다

            finish();// 현재 액티비티 종료

        }


    }
}
