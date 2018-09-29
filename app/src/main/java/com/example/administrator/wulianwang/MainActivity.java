package com.example.administrator.wulianwang;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity {
    private GifImageView mGifImageView;
    private TextView responseText;
    private TextView Text;
    private float value;
    private float Va;
    private Switch aSwitch;
    private int status_fan;
    private String AccessToken;
    private String Data_fan;

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private String url_fan = "http://api.nlecloud.com/Cmds?deviceId=13067&apiTag=one_fan";
    private String url = "http://api.nlecloud.com/devices/13067/Sensors/panao_temperature";
    private String url_onefan="http://api.nlecloud.com/devices/13067/Sensors/one_fan";
    // private String url_data="http://api.nlecloud.com/Devices/Datas?devIds=13067";


    GifImageView giv1;//由按键控制播放的gif
    GifDrawable gifDrawable;//资源对象
    private Handler handler ;
    private Handler handler2;
    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        aSwitch=findViewById(R.id.switch1);
        Bundle bundle = getIntent().getExtras();                                 //得到传过来的bundle  
        assert bundle != null;
        AccessToken = bundle.getString("AccessToken");                    //读出数据
        sendRequestWithOkHttp();
   //     responseText = findViewById(R.id.data_temp);
        //Text=findViewById(R.id.data_fan);
        try {
            giv1 = (GifImageView) findViewById(R.id.giv1);
            //这里控制播放的对象实际是gifDrawable
                gifDrawable = new GifDrawable(getResources(), R.drawable.fan);
                giv1.setImageDrawable(gifDrawable);//这里是实际决定资源的地方，优先级高于xml文件的资源定义
            } catch (IOException e) {
                e.printStackTrace();
            }


            Button to_electromotor=findViewById(R.id.electromotor);
            to_electromotor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(MainActivity.this,ElectromotorControl.class);
                    Bundle bundle=new Bundle();
                    bundle.putString("AccessToken",AccessToken);//压入数据    
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
            final Button on=findViewById(R.id.open_fan);
            final Button off=findViewById(R.id.close_fan);
            on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendOkHttp("1");
                if(status_fan==0) {
                    gifDrawable.start();
                    Toast.makeText(MainActivity.this, "开启成功", Toast.LENGTH_SHORT).show();
                    sendDevice();
                    show();
                }else {
                    Toast.makeText(MainActivity.this, "开启失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
        off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendOkHttp("0");
                if(status_fan==0) {
                    gifDrawable.stop();
                    Toast.makeText(MainActivity.this, "关闭成功", Toast.LENGTH_SHORT).show();
                    sendDevice();
                    show();
                }else {
                    Toast.makeText(MainActivity.this, "关闭失败", Toast.LENGTH_SHORT).show();
                }
            }
        });

        final Button set_temp=findViewById(R.id.set_temp);
        set_temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDevice();
                showkeep();
            }
        });

        on.setEnabled(true);
        off.setEnabled(true);
        set_temp.setEnabled(false);


        handler=new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 0) {
                    /*** 写执行的代码*/
                    sendRequestWithOkHttp();
                    show();
                }
            }
        };

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // (1) 使用handler发送消息
                Message message=new Message();
                message.what=0;
                handler.sendMessage(message);
            }
        },0,2000);


        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    on.setEnabled(false);
                    off.setEnabled(false);
                    set_temp.setEnabled(true);
                    Toast.makeText(MainActivity.this,"当前模式:自动",Toast.LENGTH_SHORT).show();
                }else {
                    on.setEnabled(true);
                    off.setEnabled(true);
                    set_temp.setEnabled(false);
                    Toast.makeText(MainActivity.this,"当前模式:手动",Toast.LENGTH_SHORT).show();

                }
            }
        });

   }




    public void sendOkHttp(final String integer) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient okHttpClient = new OkHttpClient();   //定义一个OKHttpClient实例
                    RequestBody requestBody =RequestBody.create(JSON,integer);
                    //实例化一个Respon对象，用于发送HTTP请求
                    Request request = new Request.Builder()
                            .url(url_fan)                                         //设置目标网址
                            .addHeader("AccessToken",AccessToken)
                            .post(requestBody)
                            .build();
                    Response response = okHttpClient.newCall(request).execute();  //获取服务器返回的数据
                    if (response.body() != null) {
                        Data_fan = response.body().string();//存储服务器返回的数据
                        Log.d("data_fan", Data_fan);
                        JWG(Data_fan);
                      //  show(Data_fan);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
//    private void sendfan() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                //在子线程中执行Http请求，并将最终的请求结果回调到okhttp3.Callback中
//                HttpUtil.sendOkHttpRequest(url_fan, new okhttp3.Callback() {
//                    @Override
//                    public void onFailure(Call call, IOException e) {
//                        //在这里进行异常情况处理
//                    }
//                    @Override
//                    public void onResponse(Call call, @NonNull Response response) throws IOException {
//                        //得到服务器返回的具体内容
//                        String Data = response.body().string();
//                        JWG(Data);
//                        //显示UI界面，调用的showResponse方法
//                        show(Data);
//                    }
//                }, AccessToken);
//            }
//        }).start();
//    }

    private void showkeep() {
        EditText low=findViewById(R.id.low_temp);
        EditText high=findViewById(R.id.high_temp);
        String h = high.getText().toString();
        String l= low.getText().toString();
        float m;
        float n;
        m=Float.parseFloat(h);
        n=Float.parseFloat(l);
        Toast.makeText(MainActivity.this,"设置成功",Toast.LENGTH_SHORT).show();
        if(value<=n){
            gifDrawable.stop();
        }
        if (value>=m){
            gifDrawable.start();
        }
    }
    private void show() {
        //在子线程中更新UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 在这里进行UI操作，将结果显示到界面上
             //   String re=String.valueOf(value);
                sendDevice();
                if(Va==1)
                {
                    gifDrawable.start();
                }
                if(Va==0)
                {
                    gifDrawable.stop();
                }
                //Text.setText(re);
            }
        });
    }

    private void JWG(String json) {
        Gson gson = new Gson();
        java.lang.reflect.Type type = new TypeToken<Da>() {}.getType();
        Da da= gson.fromJson(json, type);
        status_fan = da.getStatus();
        Log.d("status_fan", String.valueOf(status_fan));
    }
    public static class Da{
        private int Status;
        private int StatusCode;
        private String Msg;
        private Object ErrorObj;

        public int getStatus() {
            return Status;
        }

        public void setStatus(int status) {
            Status = status;
        }

        public int getStatusCode() {
            return StatusCode;
        }

        public String getMsg() {
            return Msg;
        }

        public Object getErrorObj() {
            return ErrorObj;
        }

    }

        private void sendDevice() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //在子线程中执行Http请求，并将最终的请求结果回调到okhttp3.Callback中
                HttpUtil.sendOkHttpRequest(url_onefan, new okhttp3.Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        //在这里进行异常情况处理
                    }
                    @Override
                    public void onResponse(Call call, @NonNull Response response) throws IOException {
                        //得到服务器返回的具体内容
                        String ponse= response.body().string();
                        JSONWithGSON(ponse);
                        //显示UI界面，调用的showResponse方法
                       // showResponse(responseData);
                    }
                }, AccessToken);
            }
        }).start();
    }
    private void JSONWithGSON(String json) {
        Gson gson = new Gson();
        java.lang.reflect.Type type = new TypeToken<MM>() {}.getType();
        MM mm = gson.fromJson(json, type);
        Va=mm.getResultObj().getValue();
        Log.d("Va",String.valueOf(Va));
    }
    public static class MM{
        private ResultObj ResultObj;
        private int Status;
        private int StatusCode;
        private String Msg;
        private Object ErrorObj;
        public static class ResultObj{
            private String ApiTag;
            private byte Groups;
            private byte Protocol;
            private String Name;
            private String createData;
            private byte TransType;
            private byte DataType;
            private Object TypeAttrs;
            private int DevicesID;
            private String SensorType;
            private float Value;
            private String RecordTime;
            //传感器
//            private String Unit;
            //执行器
            private byte OperType;
            private String OperTypeAttrs;

            public float getValue() {
                return Value;
            }

            public void setValue(float value) {
                Value = value;
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

        public Object getErrorObj() {
            return ErrorObj;
        }

        public MM.ResultObj getResultObj() {
            return ResultObj;
        }
    }


    private void sendRequestWithOkHttp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //在子线程中执行Http请求，并将最终的请求结果回调到okhttp3.Callback中
                HttpUtil.sendOkHttpRequest(url, new okhttp3.Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        //在这里进行异常情况处理
                    }
                    @Override
                    public void onResponse(Call call, @NonNull Response response) throws IOException {
                        //得到服务器返回的具体内容
                        String responseData = response.body().string();
                        parseJSONWithGSON(responseData);
                        //显示UI界面，调用的showResponse方法
                       // showResponse(responseData);
                    }
                }, AccessToken);
            }
        }).start();
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
    private void parseJSONWithGSON(String json) {
        Gson gson = new Gson();
        java.lang.reflect.Type type = new TypeToken<Data>() {}.getType();
        Data data = gson.fromJson(json, type);
        value = (float) data.getResultObj().getValue();
        String v=String.valueOf(value);
        TextView temp=findViewById(R.id.temp);
        temp.setText(v);
        Log.d("Value",v);
    }
    public static class Data {
        private ResultObj ResultObj;
        private int Status;
        private int StatusCode;
        private String Msg;
        private Object ErrorObj;
        public static class ResultObj{
            private String ApiTag;
            private byte Groups;
            private byte Protocol;
            private String Name;
            private String createData;
            private byte TransType;
            private byte DataType;
            private Object TypeAttrs;
            private int DevicesID;
            private String SensorType;
            private float Value;
            private String RecordTime;
            //传感器
            private String Unit;
            //执行器
//            private byte OperType;
//            private String OperTypeAttrs;

            public Object getValue() {
                return Value;
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

        public Object getErrorObj() {
            return ErrorObj;
        }

        public Data.ResultObj getResultObj() {
            return ResultObj;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
