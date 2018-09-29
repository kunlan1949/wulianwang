package com.example.administrator.wulianwang;

import android.content.Intent;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class login extends AppCompatActivity {

    private String N;
    private String P;

    private int status;
    private String AccessToken;
    private String msg;
    private TextView responseText;
    private String responseData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final Button login=findViewById(R.id.login);
        responseText=findViewById(R.id.responseText);
        CheckBox rp=null;
        EditText name=findViewById(R.id.name);
        EditText password=findViewById(R.id.password);
         N=name.getText().toString();
         P=password.getText().toString();
         rp=findViewById(R.id.checkbox);
         rp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
             @Override
             public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                 // TODO Auto-generated method stub
                      if(isChecked){
                          showResponse(responseData);
                          Toast.makeText(login.this,"记住密码",Toast.LENGTH_SHORT).show();
                      }
             }
         });





        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequestWithOkHttp(N,P);
                if(status==0) {
                    Intent intent= new Intent();
                    intent.setClass(login.this,MainActivity.class);
                    Bundle bundle=new Bundle();
                    bundle.putString("AccessToken","96EE2F9B2B31BC0BB8660978E73701EA42BA8324FED748A5D60114F1CFC039BF5DD725258E21323D1E419AAAEBC385F462421A63CF945B490F9E6BA3A90C5C5658C3A4C8CC2F7315A427CDBB3E43DBBDE96EA3ADAE1A4FDEA58ECDF76EEA62BA38B7643CCEE08461745562B032AE931E2E3F0ECE40BCD902D3C7C3378DB3D76ABFC4071D5A44A261832B9886A4F370B67FB765B5B81BDC32FE6C191F6CF11DC0C2C7FF1EC48D3E273371340B2827E538155025F5DDCDF41A7056F295FD349D769AF4E730CDFFF1A6E16EED8AC5526095AB17B4BCCE66F2EFA41745B4BE595C3DF4E9F286D6F7D8C5BC6FEF2EEE5396F9");//压入数据    
                    intent.putExtras(bundle);
                    startActivity(intent);
                    Toast.makeText(login.this, "登陆成功!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(login.this, "登陆失败,请检查用户名和密码!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }



    /************************************************向服务器发送登录请求**************************************************************/
    public void sendRequestWithOkHttp(final String Account, final String password) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient okHttpClient = new OkHttpClient();   //定义一个OKHttpClient实例
                    RequestBody requestBody = new FormBody.Builder()
                            .add("Account", Account)
                            .add("Password", password)
                            .add("IsRememberMe", "true")
                            .build();
                    Log.d("Account", Account);
                    Log.d("Password", password);
                    //实例化一个Respon对象，用于发送HTTP请求
                    Request request = new Request.Builder()
                            .url("http://api.nlecloud.com/Users/Login")             //设置目标网址
                            .post(requestBody)
                            .build();
                    Response response = okHttpClient.newCall(request).execute();  //获取服务器返回的数据
                    if (response.body() != null) {
                         responseData = response.body().string();//存储服务器返回的数据
                        Log.d("data", responseData);
                        parseJSONWithGSON(responseData);


                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

           private void showResponse(final String response) {
        //在子线程中更新UI
            runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 在这里进行UI操作，将结果显示到界面上
                responseText.setText(response);
            }
        });
    }
    private void parseJSONWithGSON(String json) {
        Gson gson = new Gson();
        java.lang.reflect.Type type = new TypeToken<login.App>() {}.getType();
        App app = gson.fromJson(json, type);
        status = app.getStatus(); // 获取登录状态
        AccessToken = app.getResultObj().getAccessToken(); //获取返回的确定设备标识的字符串

        msg = app.getMsg();
        Log.d("AccessToken_data",AccessToken);
        Log.d("status", String.valueOf(status));
    }




    public static class App {
        private login.App.ResultObj ResultObj;
        private int Status;
        private int StatusCode;
        private String Msg;
        private Object ErrorObj;
        public static class ResultObj{
            private  int UserID;
            private String UserName;
            private String Email;
            private String Telphone;
            private Boolean Gender;
            private int CollegeID;
            private String CollegeName;
            private String RoleName;
            private int RoleID;
            private String AccessToken;
            private String ReturnUrl;
            private String DataToken;

            public String getAccessToken() {
                return AccessToken;
            }
        }

        public int getStatus() {
            return Status;
        }

        public int getStatusCode() {
            return StatusCode;
        }

        public String getMsg() {
            return Msg;
        }

        public void setStatus(int status) {
            Status = status;
        }

        public void setMsg(String msg) {
            Msg = msg;
        }

        public Object getErrorObj() {
            return ErrorObj;
        }

        public login.App.ResultObj getResultObj() {
            return ResultObj;
        }
    }




}
