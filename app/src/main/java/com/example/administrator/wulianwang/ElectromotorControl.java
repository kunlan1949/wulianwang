package com.example.administrator.wulianwang;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ElectromotorControl extends AppCompatActivity {
    private String AccessToken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_electromotor_control);
        Bundle bundle = getIntent().getExtras();                                 //得到传过来的bundle  
        assert bundle != null;
        AccessToken = bundle.getString("AccessToken");                    //读出数据
    }
    //       private void showResponse(final String response) {
//        //在子线程中更新UI
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                // 在这里进行UI操作，将结果显示到界面上
//                responseText.setText(response);
//            }
//        });
//    }
}
