package com.kangde.myapplication;


import com.google.gson.Gson;
import com.kangde.myapplication.Bean.User;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by lenovo on 2018/3/16.
 */

public class HttpConnection {

    /*address为请求servlet的地址address="http://192.168.43.87:8080/ServerTest/LoginServlet"
　　　 192.168.43.87为测试服务器的IP
      user为请求实体类
      okhttp3.Callback callback是OkHttp库中自带的回调接口，OkHttp在enqueue()方法内部开了子线程，
      在子线程中进行Http请求，并将返回结果回调到okhttp3.Callback当中。
    * */
    public static void sendOkHttpRequest(String address, User user, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        //使用Ggon将user对象转为json
        String params = new Gson().toJson(user);
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(JSON, params);
        Request request = new Request.Builder().url(address).post(requestBody).build();
        client.newCall(request).enqueue(callback);
    }

    public static void OkHttpUploadRequest(String address, User user, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        //使用Ggon将user对象转为json
        String params = new Gson().toJson(user);
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(JSON, params);
        Request request = new Request.Builder().url(address).post(requestBody).build();
        client.newCall(request).enqueue(callback);
    }
}