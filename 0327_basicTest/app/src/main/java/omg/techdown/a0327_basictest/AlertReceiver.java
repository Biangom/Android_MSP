package omg.techdown.a0327_basictest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.widget.Toast;

/**
 * Created by kss78 on 2018-04-02.
 */

// broadCast하는 recevier 설정 부분
public class AlertReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent proxintent) {
        boolean isEntering = proxintent.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, false);
        // boolean getBooleanExtra(String name, boolean defaultValue)
        String name = proxintent.getExtras().getString("name"); // name을 인텐트로 받아와서 해당하는 지역 이름을 출력


        if(isEntering)
            Toast.makeText(context, name + "목표 지점에 접근중입니다..", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(context, name + "목표 지점에서 벗어납니다..", Toast.LENGTH_LONG).show();
    }
}
