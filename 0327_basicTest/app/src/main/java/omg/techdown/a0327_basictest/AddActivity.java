package omg.techdown.a0327_basictest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AddActivity extends AppCompatActivity {
    MainActivity.Data data;
    Button btnPrev;

    EditText etName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        btnPrev = (Button)findViewById(R.id.IdBtnPrev);
        etName = (EditText)findViewById(R.id.IdName);

//        btnPrev.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                // 이전 페이지로 화면 전환
//                // 화면을 전환할때 사용하는 클래스 그것이 Intent 클래스이다.
//            }
//        });
    }

    public void homeMove(View view)
    {
        Intent intent = new Intent();

        intent.putExtra("result_msg", etName.getText().toString()); // t는 키 값임
        setResult(RESULT_OK, intent ); // 안전하게 보냈다

        finish();// 현재 액티비티 종료

    }


}
