package com.kangde.myapplication.Activitys;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jet.flowtaglayout.FlowTagLayout;
import com.kangde.myapplication.Bean.Recommendation;
import com.kangde.myapplication.Bean.Tags;
import com.kangde.myapplication.Bean.UserPreference;
import com.kangde.myapplication.R;
import com.kangde.myapplication.Util.L;
import com.kangde.myapplication.Util.SharedPreferencesUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
/**
 * AT this activity the reference code include the tag flowlayout  for tag functions
 * Also include the Materialdesign Rating Bar example by zhanghai
 * The Okhttp3 is also used to request framework

 */
public class RecDetailActivity extends AppCompatActivity {

    private List<String> dataList;
    private List<String> tagsList;
    private  List<Tags> tagList;
    private Map<Integer,Boolean> tagRegisterr = new HashMap<>();
    private int position;
    private String tag;
    private boolean likeclicked;
    private boolean Unlikeclicked;
    private EditText editText_id;
    private TextView uploader;
    private TextView comment;
    private TextView movieName;

    private Recommendation rec;
    private ImageView img;
    private Context context;
    private Button like;
    private Button dislike;
    private Button submit;
    private SharedPreferencesUtil sp;
    private MaterialRatingBar rb;
    private FlowTagLayout flowTagLayout;
    private boolean flag;
    private UserPreference UP;

    /*
*    Title: MaterialRatingBar
*    Author: zhanghai
*    Date: 2016
*    Code version: 1.4
*    Availability: https://github.com/zhanghai/MaterialRatingBar
*        Copyright 2016 Zhang Hai
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rec_detail);

        this.context = context;
        sp= SharedPreferencesUtil.getInstance(getApplicationContext());
        flag=true;

        Unlikeclicked=false;
        likeclicked=false;
        editText_id = (EditText) findViewById(R.id.editText_tags);
        uploader = (TextView) findViewById(R.id.textView_Uploader);
        comment = (TextView) findViewById(R.id.TextView_comment);
        movieName = (TextView) findViewById(R.id.TextView_movie);
        rb = (MaterialRatingBar) findViewById(R.id.SRatingBar);
        like = (Button) findViewById(R.id.button_like);
        dislike = (Button) findViewById(R.id.button_dislike);
        submit =(Button)findViewById(R.id.button_submit);
        rec = (Recommendation) getIntent().getSerializableExtra("rec");
        img = (ImageView) findViewById(R.id.image_view);

        dataList = new ArrayList<>();
        tagList=new ArrayList<>();
        tagsList=new ArrayList<>();

        /**
         * FlowTag layout adapted from github use to save and get the recommedantion tags
         */
        flowTagLayout = findViewById(R.id.TagLayout);
        setDatas();
        flowTagLayout.addTags(dataList);
        flowTagLayout.setTagClickListener(new FlowTagLayout.OnTagClickListener() {
            @Override
            public void tagClick(int position) {
                flowTagLayout.getChildAt(position).setSelected(!flowTagLayout.getChildAt(position).isSelected());
                Toast.makeText(RecDetailActivity.this, dataList.get(position), Toast.LENGTH_SHORT).show();

                tagRegisterr.put(position,flowTagLayout.getChildAt(position).isSelected());
            }
        });
        //add the tag in to thhe layout
        findViewById(R.id.button_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flowTagLayout.addTag(editText_id.getText().toString());
                dataList.add(editText_id.getText().toString());
                Toast.makeText(RecDetailActivity.this, "添加了“" + editText_id.getText().toString() + "”", Toast.LENGTH_SHORT).show();
            }
        });
        //remove the tags from layout
        findViewById(R.id.button_remove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                flowTagLayout.removeTag();
                Toast.makeText(RecDetailActivity.this, "移除了“" + dataList.get(position) + "”", Toast.LENGTH_SHORT).show();
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
        //call the function to save the user select tag unto server
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveTags();
                tagsList.clear();
            }
        });
        /**
         * create a favour user prefernce on Recoomendation
         * if user already like the post ,clicked like agian will delete the like
         */
        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(likeclicked)
                {

                    deleteUp(1);
                    likeclicked=false;
                    like.setBackgroundResource(android.R.color.background_light);

                }
                else
                {

                    likeOrDislike(1);
                    if(Unlikeclicked)
                    {
                        deleteUp(-1);
                        Unlikeclicked=false;
                        dislike.setBackgroundResource(android.R.color.background_light);
                    }
                    likeclicked=true;

                    like.setBackgroundResource(android.R.color.holo_red_dark);
                }


            }
        });
        //save  or delete  user dislike
        dislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Unlikeclicked)
                {
                    deleteUp(-1);
                    Unlikeclicked=false;
                    dislike.setBackgroundResource(android.R.color.background_light);
                }
                else
                {
                    likeOrDislike(-1);
                    if(likeclicked)
                    {
                        deleteUp(1);
                        likeclicked=false;
                        like.setBackgroundResource(android.R.color.background_light);
                    }
                    likeclicked=false;
                    Unlikeclicked=true;

                    dislike.setBackgroundResource(android.R.color.holo_red_light);
                }

            }
        });
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarHeader);
        toolbar.setTitle("Review Details");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        viewDetails();

    }

    public void viewDetails() {
        uploader.setText(rec.getUsername());
        comment.setText(rec.getComment());
        movieName.setText(rec.getMovieName());
        rb.setProgress((int) rec.getRating());

        String pic = rec.getPic();
        //.with(context).load(url).into(img);
        //LoadImage.Download();
        like.setBackgroundResource(android.R.color.background_light);
        dislike.setBackgroundResource(android.R.color.background_light);
        Download(pic);
        ButtonDetails();
    }

    private  void deleteUp(int rating)
    {
        OkHttpClient okHttpClient = new OkHttpClient();

        int r= rating;
        Request.Builder builder = new Request.Builder();
        Request request = builder.url("http://192.168.1.15:9999/MovieAppServer/deleteUpServlet")
                .addHeader("uname",sp.get("userName"))
                .addHeader("recId",String.valueOf(rec.getId()))
                .addHeader("rating",String.valueOf(r))
                .build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                L.e("onFailure:" + e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String rs = (response.body().string());
                boolean responseData = Boolean.valueOf(rs);
                if (responseData)
                {

                    Looper.prepare();
                    Toast.makeText(RecDetailActivity.this, "success delete", Toast.LENGTH_SHORT).show();
                    Looper.loop();

                }
                else
                {
                    Looper.prepare();
                    Toast.makeText(RecDetailActivity.this, "Fail to delete", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }

            }
        });
    }
    private void ButtonDetails()
    {


        OkHttpClient okHttpClient = new OkHttpClient();


        Request.Builder builder = new Request.Builder();
        Request request = builder.url("http://192.168.1.11:9999/MovieAppServer/upDetailsServlet")
                .addHeader("uname",sp.get("userName"))
                .addHeader("recId",String.valueOf(rec.getId()))
                .build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                L.e("onFailure:" + e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseData = (response.body().string());
                String isnull= responseData.toString();
                L.e(isnull);

                if(isnull.equals("null")) {

                    flag=false;

                }
                if(flag)
                {
                     Gson gson = new GsonBuilder().create();
                    UP = gson.fromJson(responseData, UserPreference.class);
                    int rating =UP.getRate();
                    if(rating==1)
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                like.setBackgroundResource(android.R.color.holo_red_light);
                                dislike.setBackgroundResource(android.R.color.background_light);
                                likeclicked=true;
                            }
                        });

                    }
                    else if(rating==-1) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dislike.setBackgroundResource(android.R.color.holo_red_light);
                                like.setBackgroundResource(android.R.color.background_light);
                                Unlikeclicked =true;

                            }
                        });
                    }

                }

                if(!flag)
                {
                    like.setBackgroundResource(android.R.color.background_light);
                    dislike.setBackgroundResource(android.R.color.background_light);
                   L.e("this rec have no up yet");
                }

            }
        });
    }

    private void Download(String picUrl) {


        OkHttpClient okHttpClient = new OkHttpClient();


        Request.Builder builder = new Request.Builder();
        String pic = picUrl;
        Request request = builder.url("http://192.168.1.11:9999/MovieAppServer/DownLoadServlet")
                .addHeader("profile","False")
                .addHeader("filename",pic)
                .build();

        Call call = okHttpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                L.e("onFailure:" + e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                InputStream inputStream = response.body().byteStream();
                //将输入流数据转化为Bitmap位图数据
                final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                final File file = new File("/mnt/sdcard/picture.jpg");
                file.createNewFile();
                //创建文件输出流对象用来向文件中写入数据
                FileOutputStream out = new FileOutputStream(file);
                //将bitmap存储为jpg格式的图片
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                //刷新文件流
                out.flush();
                out.close();
                final Message msg = Message.obtain();
                msg.obj = bitmap;

                // Glide.with(this).load(url).into(mImageView);
                // img.setImageBitmap(file.);
                runOnUiThread(new Thread(new Runnable() {
                    @Override
                    public void run() {
                        img.setImageBitmap(bitmap);


                    }
                }));


            }
        });

    }
    // check the user like or unlike of the current post
    private void likeOrDislike(int rate) {

        final UserPreference up = new UserPreference();
        up.setRate(rate);
        up.setMovieName(rec.getMovieName());
        up.setUploader(rec.getUsername());
        up.setUserName(sp.get("userName"));
        up.setRecId(rec.getId());
        if (isConnectingToInternet()) {
             {
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Gson gson = new Gson();
                        String json = gson.toJson(up);
                        String address = "http://192.168.1.11:9999/MovieAppServer/UserPreferenceServlet";


                        //HttpConnection.sendOkHttpRequest(address, user, new okhttp3.Callback(){

                        OkHttpClient client = new OkHttpClient();
//

                        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                                , json);
                        Request request = new Request.Builder()
                                .url(address)
                                .post(requestBody)
                                .build();

                        Call call = client.newCall(request);
                        call.enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.d(MainActivity.TAG, "connectionfaill");
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                boolean responseData = Boolean.valueOf(response.body().string());
                                L.e("response data： " + responseData);
                                L.e("res code" + response.code());
                                //  String cookie = response.headers().get("Set-Cookie");

                                // Log.d(TAG, cookie);
                                L.e("res data" + responseData);
                                if (responseData) {

                                    Looper.prepare();
                                    Toast.makeText(RecDetailActivity.this, "succeed", Toast.LENGTH_SHORT).show();
                                    Looper.loop();
                                }
//                                 else
//                                 {
//                                     startActivity(new Intent(RegActivity.this,MainActivity.class));
//                                 }


                            }
                        });

                    }
                }).start();
            }
        } else {
            System.out.println("internet not connect");
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

    /**
     * I adapte this example code and use it to set the tags retrive from databse to the layout
     * Title: FlowTagLayoutDemo
     *    Author: jetLee92
     *    Date:2018
     *    Code version:1.0.1
     *    Availability: https://github.com/jetLee92/FlowTagLayoutDemo
     */
    public void setDatas() {

        new Thread(new Runnable() {
            @Override
            public void run() {




                int recID =rec.getId();



                String address= "ws://192.168.1.11:9999/MovieAppServer/listTagServlet";

                OkHttpClient client = new OkHttpClient();
                Request request = new Request
                        .Builder()
                        .url(address)
                        .addHeader("recID",String.valueOf(recID))
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();


                    JSONArray array = null;
                    try {
                        array = new JSONArray(responseData);

                        for(int i = 0; i< array.length();i++) {
                            JSONObject obj = array.getJSONObject(i);


                           tag = obj.getString("context");
                            L.e(tag);

                            dataList.add(tag);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    flowTagLayout.addTags(dataList);
                                }
                            });

                        }




                    } catch (JSONException e) {
                        e.printStackTrace();
                    }




                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();






    }

   public void showTag(List <Tags>tagList)
    {
        L.e("is tag empty"+tagList.isEmpty());

        for(int i = 0; i< tagList.size();i++){

            Tags t = tagList.get(i);
            dataList.add(t.getContext());
        }
        ;
    }

    public void SaveTags ()
    {
        int counter=0;
        for(Map.Entry<Integer,Boolean> entry : tagRegisterr.entrySet()){
            if(entry.getValue() == true){
               tagsList.add(dataList.get(entry.getKey()));


                L.e("S COUNTER "+entry.getValue());
            }
        }

        if(tagsList.isEmpty())
        {
            Toast.makeText(RecDetailActivity.this, "please select tags", Toast.LENGTH_SHORT).show();
        }
        int recID =rec.getId();
        Gson gson = new Gson();
        String json = gson.toJson(tagsList);
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                , json);
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();

        Request request = builder.url("http://192.168.1.11:9999/MovieAppServer/addTagServlet")
                .addHeader("userId",sp.get("uid"))
                .addHeader("recID",String.valueOf(recID))
                .post(requestBody)
                .build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                L.e("onFailure:" + e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                L.e("onResponse:");

                boolean responseData = Boolean.valueOf(response.body().string());
                L.e("response ="+responseData);
                String cookie = response.headers().get("Set-Cookie");
                if (responseData) {

                    Looper.prepare();
                    Toast.makeText(RecDetailActivity.this, "succeed", Toast.LENGTH_SHORT).show();

                    Looper.loop();


                } else {
                    L.e("status="+responseData);
                    Looper.prepare();
                    Toast.makeText(RecDetailActivity.this, "Failure", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }

        });
    }
}