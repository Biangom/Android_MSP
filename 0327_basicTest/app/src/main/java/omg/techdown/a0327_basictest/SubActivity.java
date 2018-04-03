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

    ArrayList<Data> _dataList; // 임시로 사용할 dataList, 이 _dataList는
                                // MainAcitivity에서 받아서 여기서 삭제과정을 거친 후
                                // MainActivitiy로 전달할 것이다.
    String inputName;           // 삭제할 이름 저장할 string

    TextView[] idName = new TextView[3];    // 임시로 현재 저장된 데이터들의 이름들을 보여주는 TextView ID들의 모임
    EditText EditName;                      // 삭제할 이름 입력할 editText의 iD

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

        _dataList = intent.getParcelableArrayListExtra("list2"); // _dataList엔 MainActivitiy의 dataList가 저장 될 것이다.

        for(int i = 0; i < _dataList.size() ; i++)
            idName[i].setText(_dataList.get(i).name);
    }

    public void onClickSub(View view) {
        boolean DidRemove = false; // 같은 이름이 있다면 루프를 돌때 true로 바꿔질거임
        inputName = EditName.getText().toString();

        // 예외처리: 만약 데이터가 입력되지 않았다면 빈 것이다._
        if(EditName.getText().toString().equals("")) {
            Toast.makeText(SubActivity.this, "이름을 입력해주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        // loop를 돌면서 해당 이름과 같은 이름을 가진 데이터 리스트 삭제해버리기
        for(int i = 0; i < _dataList.size(); i++) {
            if(_dataList.get(i).name.equals(inputName)) {
                _dataList.remove(i);
                DidRemove = true;
            }
        }

        // 예외처리: 만약 삭제되지 않았다면 해당하는 이름의 데이터가 없는 것이다.
        if(DidRemove == false) {
            Toast.makeText(SubActivity.this, "해당하는 이름의 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            Toast.makeText(SubActivity.this, "삭제완료", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(SubActivity.this, MainActivity.class );
            intent.putParcelableArrayListExtra("sublist", _dataList); // MainActivity에게 _dataList를 전달한다.
            setResult(RESULT_OK, intent ); // 안전하게 보냈다
            finish();// 현재 액티비티 종료
        }
    }
}
