package com.kangde.myapplication.Activitys;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jet.flowtaglayout.FlowTagLayout;
import com.kangde.myapplication.Bean.User;
import com.kangde.myapplication.Mu_RecycleView.MD_Example_Activity;
import com.kangde.myapplication.R;
import com.kangde.myapplication.Util.L;
import com.kangde.myapplication.Util.SharedPreferencesUtil;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Cookie;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private Button button_test;
    private Button button_request;
    private Button button_upload;
    private  Button button_register;
    private EditText editText_id, editText_password;

    private FlowTagLayout flowTagLayout;
    private List<String> dataList;

    private Map<Integer,Boolean> tagRegisterr = new HashMap<>();

    private MyHandler myhandler = new MyHandler(this);
    public static final String TAG="MainActivity";
    private ConcurrentHashMap<String, List<Cookie>> cookieStore = new ConcurrentHashMap<>();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private SharedPreferencesUtil sp;
    private int position;

    /**
     * the code in this activity is for test running purpose
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        position=0;
        button_request = (Button) findViewById(R.id.button_request);
        button_upload=(Button)findViewById(R.id.button_upload);
        editText_id = (EditText) findViewById(R.id.editText_id);
        editText_password = (EditText) findViewById(R.id.editText_password);
        button_register=(Button)findViewById(R.id.button_register);
        button_test=(Button)findViewById(R.id.button_test);
        Button button_md=(Button)findViewById(R.id.button_md);
        sp= SharedPreferencesUtil.getInstance(getApplicationContext());

        dataList = new ArrayList<>();

        flowTagLayout = findViewById(R.id.flowTagLayout);
        setDatas();
        flowTagLayout.addTags(dataList);
        flowTagLayout.setTagClickListener(new FlowTagLayout.OnTagClickListener() {
            @Override
            public void tagClick(int position) {
                flowTagLayout.getChildAt(position).setSelected(!flowTagLayout.getChildAt(position).isSelected());
                Toast.makeText(MainActivity.this, dataList.get(position), Toast.LENGTH_SHORT).show();

                tagRegisterr.put(position,flowTagLayout.getChildAt(position).isSelected());
            }
        });
        findViewById(R.id.button_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flowTagLayout.addTag(editText_id.getText().toString());
                dataList.add(editText_id.getText().toString());
                Toast.makeText(MainActivity.this, "添加了“" + editText_id.getText().toString() + "”", Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.button_remove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                flowTagLayout.removeTag();
                Toast.makeText(MainActivity.this, "移除了“" + dataList.get(position) + "”", Toast.LENGTH_SHORT).show();
//                dataList.remove(dataList.remove(position));
                    int counter = 0;
                    int s;
                for(Map.Entry<Integer,Boolean> entry : tagRegisterr.entrySet()){
                    if(entry.getValue() == true){
                       dataList.remove(entry.getKey() == 0 ? 0 : entry.getKey() - counter);
                        flowTagLayout.removeTagOfIndex(entry.getKey() == 0 ? 0 : entry.getKey() - counter);

                        counter++;
                        L.e("S COUNTER "+counter);
                    }
                }



            }
        });

        button_md.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MD_Example_Activity.class));
            }
        });

        InitView();
    }


    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            System.out.println(msg);
            if (mActivity.get() == null) {
                return;
            }
            mActivity.get().updateUIThread(msg);
        }
    }


    private void updateUIThread(Message msg){
        Bundle bundle = msg.getData();
        String result = bundle.getString("result");
        Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
    }

    void InitView(){
//        button_upload =(Button)findViewById(R.id.button_Upload);
        button_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConnectingToInternet()){
                    if (editText_id.toString().isEmpty()||editText_password.toString().isEmpty()) {
                        System.out.println("password cant be empty");
                    }
                    else{
                        //开启访问数据库线程
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                User user = new User();
                                user.setUsername(editText_id.getText().toString());
                                user.setPassword(editText_password.getText().toString());

                                Gson gson = new Gson();
                                String json = gson.toJson(user);
                                String address = "http://192.168.1.15:9999/MovieAppServer//LoginServlet";

                                OkHttpClient client = new OkHttpClient();


                                RequestBody body = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                                        , json);
                                Request request = new Request.Builder().url(address).post(body).build();
                                try {
                                    Response response = client.newCall(request).execute();
                                    String responseData = response.body().string();

                                    String cookie = response.headers().get("Set-Cookie");
                                    if (responseData!=null)
                                       {
                                           sp.put("userName",user.getUsername());
                                            sp.put("password",user.getPassword());

                                          // SharedPreferences.Editor editor =sp
                                           parseJSONWithGSON(responseData);
                                           Looper.prepare();
                                           Toast.makeText(MainActivity.this, "succeed", Toast.LENGTH_SHORT).show();
                                           String usertest =sp.get("userName");
                                           L.e(usertest);
                                           Looper.loop();
                                       }
                                       else
                                       {
                                           L.e(responseData);
                                           Toast.makeText(MainActivity.this, "Failure", Toast.LENGTH_SHORT).show();
                                       }

                                    Log.d(TAG, cookie);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

//                                HttpConnection.sendOkHttpRequest(address, user, new okhttp3.Callback(){
//                                    @Override
//                                    public void onFailure(Call call, IOException e) {
//                                        Log.d(MainActivity.TAG,"connectionfaill");
//                                    }
//                                    @Override
//                                    public void onResponse(Call call, Response response) throws IOException {
//                                        String responseData = response.body().string();
//                                        System.out.println("响应信息： " + responseData);
//                                      if (responseData!=null)
//                                       {
//                                           parseJSONWithGSON(responseData);
//                                           Looper.prepare();
//                                           Toast.makeText(MainActivity.this, "succeed", Toast.LENGTH_SHORT).show();
//                                           Looper.loop();
//                                       }
//                                       else
//                                       {
//                                           L.e(responseData);
//                                           Toast.makeText(MainActivity.this, "Failure", Toast.LENGTH_SHORT).show();
//                                       }
//
//
//
//                                    }
//                                });

                            }
                        }).start();
                    }
                }else{
                    System.out.println("internet not connect");
                }

            }
        });

        button_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                   startActivity(new Intent(MainActivity.this,UploadActivity.class));

            }
        });
        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,RegActivity.class));
            }
        });
        button_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,TestActivity.class));
            }
        });

    }

    public void parseJSONWithGSON(String jsonData){
        Gson gson = new Gson();
        User user = gson.fromJson(jsonData, User.class);
        Log.d(MainActivity.TAG,"no = " + user.getId());
        Log.d(MainActivity.TAG,"id = " + user.getId());
        Log.d(MainActivity.TAG,"password = " + user.getPassword());
    }


    public boolean isConnectingToInternet() {
        ConnectivityManager connectivity = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
        }
        return false;
    }

    private void setDatas() {
        dataList.add("数据结构");
        dataList.add("算法");
        dataList.add("多线程编程");
        dataList.add("JVM");
        dataList.add("自定义view");
        dataList.add("TCP/IP");
        dataList.add("gradle强化");
        dataList.add("设计模式");
        dataList.add("git");
    }

}