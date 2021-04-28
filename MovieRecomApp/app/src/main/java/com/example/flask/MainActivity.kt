package com.example.flask

import android.R
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    fun onClick(v: View) {
        val userName: String = userNameEdit.getText().toString()
        val passWord: String = passWordEdit.getText().toString()
        if (userName == "" || passWord == "") {
            showWarnSweetDialog("账号密码不能为空")
            return
        }
        when (v.getId()) {
            R.id.log_Button -> {
                val url = "http://192.168.253.1:5000/user" /*在此处改变你的服务器地址*/
                getCheckFromServer(url, userName, passWord)
            }
            R.id.Sign_Button -> {
                val url2 = "http://192.168.253.1:5000/register" /*在此处改变你的服务器地址*/
                registeNameWordToServer(url2, userName, passWord)
            }
        }
    }


}