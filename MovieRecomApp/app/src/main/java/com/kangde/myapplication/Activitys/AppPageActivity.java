package com.kangde.myapplication.Activitys;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;

import com.kangde.myapplication.R;
import com.kangde.myapplication.Util.SharedPreferencesUtil;

import androidx.appcompat.app.AppCompatActivity;

public class AppPageActivity extends AppCompatActivity {

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            next();
        }
    };
    private SharedPreferencesUtil sp;

    private void next() {
        Intent intent =null;
        if (sp.isLogin()) {

            intent = new Intent(AppPageActivity.this, TestActivity.class);
        } else {

            intent = new Intent(AppPageActivity.this, UserLoginActivity.class);
        }

        startActivity(intent);

        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_page);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        sp= SharedPreferencesUtil.getInstance(getApplicationContext());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                handler.sendEmptyMessage(0);
            }
        },3000);
    }
}
