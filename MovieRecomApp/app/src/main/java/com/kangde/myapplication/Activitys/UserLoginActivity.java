package com.kangde.myapplication.Activitys;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kangde.myapplication.Bean.User;
import com.kangde.myapplication.HttpConnection;
import com.kangde.myapplication.R;
import com.kangde.myapplication.Util.L;
import com.kangde.myapplication.Util.SharedPreferencesUtil;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Open source code Okhttps avauable from https://github.com/square/okhttp/
 * open source Gson used to parse beteen json and Strings aviable at https://github.com/google/gson
 */
public class UserLoginActivity extends AppCompatActivity {
    private EditText editText_id, editText_password;
    private Button Login, Register;
    public static final String TAG="UserLoginActivity";

    private SharedPreferencesUtil sp;
    private Context context;
    private  User user;
    private  boolean flag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
        setContentView(R.layout.activity_login);
        flag=true;
        editText_id = (EditText) findViewById(R.id.editText_Uid);
        editText_password = (EditText) findViewById(R.id.editText_Upassword);
        Login = (Button) findViewById(R.id.button_login);
        Register = (Button) findViewById(R.id.button_registerUsers);
        sp= SharedPreferencesUtil.getInstance(getApplicationContext());
        initView();
    }

    public void initView() {
        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login();
            }
        });
        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserLoginActivity.this,RegActivity.class));
            }
        });
    }

    public void Login() {
        if (isConnectingToInternet()) {
             if (editText_id.getText().toString().isEmpty() ) {
                 Toast.makeText(UserLoginActivity.this, "Username Can't be empty", Toast.LENGTH_SHORT).show();

             }
            else if ( editText_password.getText().toString().isEmpty())
             {
                 Toast.makeText(UserLoginActivity.this, "passwprd can't be empty", Toast.LENGTH_SHORT).show();
             }
            else {

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                         user = new User();
                        user.setUsername(editText_id.getText().toString());
                        user.setPassword(editText_password.getText().toString());
                        Gson gson = new Gson();
                        String json = gson.toJson(user);
                        String address = "http://192.168.1.15:9999/MovieAppServer/LoginServlet";


                        //HttpConnection.sendOkHttpRequest(address, user, new okhttp3.Callback(){

//                        OkHttpClient client=new OkHttpClient();
////
//
//                        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
//                                , json);
//                        Request request = new Request.Builder()
//                                .url(address)
//                                .post(requestBody)
//                                .build();
//
//                        Call call = client.newCall(request);
                        Callback callback=new Callback() {
                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                L.e("connectionfaill");
                            }

                            @Override
                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                                String responseData = (response.body().string());
                                L.e("response ="+responseData);
                                String isnull= responseData.toString();
                                L.e(isnull);
                                String cookie = response.headers().get("Set-Cookie");
                                if(isnull.equals("null")) {

                                    flag=false;

                                }

                                if(flag)
                                {

                                    Gson gson = new GsonBuilder().create();
                                    user = gson.fromJson(responseData, User.class);
                                    sp.setLogin(true);
//                                    String id =user.getId();
                                    L.e(user.getId()+"dengyu");
                                    sp.put("uid",user.getId());
                                    sp.put("userName", user.getUsername());
                                    sp.put("password", user.getPassword());
                                    sp.put("pic",user.getImg());

                                    // SharedPreferences.Editor editor =sp
                                    startActivity(new Intent(UserLoginActivity.this,TestActivity.class));
                                    Looper.prepare();
                                    Toast.makeText(UserLoginActivity.this, "succeed", Toast.LENGTH_SHORT).show();
                                    String usertest = sp.get("userName");
                                    L.e(usertest);
                                    Looper.loop();
                                }
                                else
                                {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(UserLoginActivity.this, "this user is not exists", Toast.LENGTH_SHORT).show();

                                        }
                                    });

                                }
                            }
                        };
                        HttpConnection.sendOkHttpRequest(address,user,callback);

                    }
                }).start();

            }
        }
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode==KeyEvent.KEYCODE_BACK)
            return true;//不执行父类点击事件
        return super.onKeyDown(keyCode, event);//继续执行父类其他点击事件
    }


}
