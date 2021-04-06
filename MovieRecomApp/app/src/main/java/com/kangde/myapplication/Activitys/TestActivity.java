package com.kangde.myapplication.Activitys;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kangde.myapplication.Bean.Recommendation;
import com.kangde.myapplication.Mu_RecycleView.ContentFragment;
import com.kangde.myapplication.R;
import com.kangde.myapplication.Util.L;
import com.kangde.myapplication.Util.RecArrayAdapter;
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
import java.util.List;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;

/**
 * this activity used the listview for the recommendations that works with recArrayAdapter in package Util
 * The drawer layout and toolbar  implement the Material Design
 * the example code is adapted and changed base on the blog from github
 */
public class TestActivity extends AppCompatActivity {
    private WebSocket webSocket;
    private Button button_get;
    private TextView textView1;
    private ImageView img;
    private Button button_img;

    Cookie ckUsername, ckSessionid;
    private String imageName;
    private Context context;
    private String filter;
    private String rec;

    private ListView listView;
    private List<Recommendation> recList;

    private RecArrayAdapter mAdapte;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private List<ContentFragment> mFragments = new ArrayList<>();
    //    private TabFragment fragment;
    private ListView mLvList;

    private String[] mMenuTitles;

    private SharedPreferencesUtil sp;

    private EditText search;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        sp= SharedPreferencesUtil.getInstance(getApplicationContext());

        setContentView(R.layout.activity_test);

        filter="";
        mMenuTitles =new String []{"Hello","Upload","User","Logout","Main"};
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawlayout);
        mLvList = (ListView) findViewById(R.id.lv_list);

        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor( R.color.google_red));
        search=(EditText) findViewById(R.id.editText_SearchBar);
        img=(ImageView) findViewById(R.id.image_profilePic);
        rec="";
        initToolBar();
        initMenuTitles();
        initFragments();
        initDrawerLayout();
        context = this;
        recList = new ArrayList<>();
        // button_get=(Button)findViewById(R.id.button_Get);

      //  img = (ImageView) findViewById(R.id.Image_Download);

        listView = (ListView) findViewById(R.id.list_view);
//        doGet();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshRecommendation();
            }
        });

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                handler.sendEmptyMessage(0);
            }
        },3000);


    }

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            next();
        }
    };
    private void next() {
        Intent intent =null;
        if (!sp.isLogin()) {

            intent = new Intent(TestActivity.this, UserLoginActivity.class);
            startActivity(intent);
            finish();
        } else {


            Toast.makeText(TestActivity.this, "you are log in", Toast.LENGTH_SHORT).show();

        }

    }

//

    public  void search()
    {
        recList.clear();
        OkHttpClient okHttpClient = new OkHttpClient();

        Request.Builder builder = new Request.Builder();

        Request request = builder.url("ws://192.168.1.15:9999/MovieAppServer/SearchRecServlet")
                .addHeader("Filter",filter)
                .addHeader("Search",search.getText().toString())
                .addHeader("uname",sp.get("userName"))
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
                final String res = response.body().string();



                try {
                    JSONArray array = new JSONArray(res);

                    for(int i = 0; i< array.length();i++){
                        JSONObject obj = array.getJSONObject(i);

                        int id = obj.getInt("Id");
                        String username = obj.getString("username");
                        String movieName= obj.getString("movieName");
                        String comment= obj.getString("comment");
                        double rating= obj.getDouble("rating");
                        String pic= obj.getString("pic");

                        recList.add(new Recommendation(id,username,movieName,comment,rating,pic));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                new Thread (new Runnable(){
                    public  void run ()
                    {
                        try
                        {
                            Thread.sleep(2000);
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                mAdapte = new RecArrayAdapter(context, recList);

                                listView.setAdapter(mAdapte);
                                swipeRefreshLayout.setRefreshing(false);

                                mAdapte.notifyDataSetChanged();

                            }
                        });
                    }
                }).start();


                L.e(res);

            }
        });



    }

    /**
     * for pullrefresh to get new recommendation
     */
    private  void refreshRecommendation ()
    {
        new Thread (new Runnable(){
            public  void run ()
            {
                try
                {
                    Thread.sleep(2000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recList.clear();
                        mAdapte = new RecArrayAdapter(context, recList);
                        doGet();

                        swipeRefreshLayout.setRefreshing(false);

                        mAdapte.notifyDataSetChanged();

                    }
                });
            }
    }).start();

    }
    public void doGet() {

        recList.clear();
        OkHttpClient okHttpClient = new OkHttpClient();

        Request.Builder builder = new Request.Builder();

        Request request = builder.url("ws://192.168.1.15:9999/MovieAppServer/ListReviewServlet")
                .addHeader("Search",search.getText().toString())
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
                final String res = response.body().string();



                try {
                    JSONArray array = new JSONArray(res);

                    for(int i = 0; i< array.length();i++){
                        JSONObject obj = array.getJSONObject(i);

                        int id = obj.getInt("Id");
                        String username = obj.getString("username");
                        String movieName= obj.getString("movieName");
                        String comment= obj.getString("comment");
                        double rating= obj.getDouble("rating");
                        String pic= obj.getString("pic");

                        recList.add(new Recommendation(id,username,movieName,comment,rating,pic));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

//                Gson gson = new Gson();
//
//                Type type = new TypeToken<List<Recommendation>>() {
//                }.getType();
//                recList = gson.fromJson(res, type);
////                for (Recommendation rec : recList){
////                    recList.add(rec);
////                }

               runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       mAdapte = new RecArrayAdapter(context, recList);
                       listView.setAdapter(mAdapte);
//                       mAdapte.notifyDataSetChanged();

                   }
               });

                L.e(res);

            }
        });


    }
    /*
        the matereil design example i learned to set the tool bar and drawer layout
        its the second blog from Chaychan
     Title: MaterialDesignExercise
*    Author: chaychan
*    Date: 2017
*    Availability: https://github.com/chaychan/MaterialDesignExercise
     */

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_md__example, menu);
        return true;
    }


    private void initToolBar() {


        getProfile();
        mToolbar.setNavigationIcon(R.drawable.ic_dashboard_black_24dp);//设置导航的图标
//        mToolbar.setLogo(img);//设置logo

        mToolbar.setTitle("Welcome");//设置标题
        mToolbar.setSubtitle(sp.get("userName"));//设置子标题

        mToolbar.setTitleTextColor(getResources().getColor(android.R.color.background_dark));//设置标题的字体颜色
        mToolbar.setSubtitleTextColor(getResources().getColor(android.R.color.white));//设置子标题的字体颜色

        //设置右上角的填充菜单
        mToolbar.inflateMenu(R.menu.menu_md__example);
        //设置导航图标的点击事件
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TestActivity.this, "菜单", Toast.LENGTH_SHORT).show();
            }
        });
        //设置右侧菜单项的点击事件
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {

            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                String tip = "";
                switch (id) {
                    case R.id.action_search:
                        tip = "search";
                        search();
                        break;
                    case R.id.action_recommendation:
                        tip = "recommend Reviews";
                        filter="rec";
                        search();
                        break;
                    case R.id.search_Uploader:
                        tip = "Search by uploader";
                        filter="userName";
                        break;
                    case R.id.search_movieName:
                        tip = "Search by MovieName";
                        filter="movieName";

                        break;
                    case R.id.search_tag:
                        tip = "search by Tag";
                        filter="tag";
                        break;
                }
                Toast.makeText(TestActivity.this, tip, Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        ;
    }

    private void initFragments() {
        ContentFragment fragment1 = new ContentFragment();
        Bundle bundle1 = new Bundle();
        bundle1.putString(ContentFragment.TEXT,"Welcome User"+sp.get("userName"));
        fragment1.setArguments(bundle1);






        mFragments.add(fragment1);

    }


    private void initMenuTitles() {
//        mMenuTitles = getResources().getStringArray(R.array.menuTitles);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mMenuTitles);
        mLvList.setAdapter(arrayAdapter);
        mLvList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switchFragment(position);//切换fragment
                mDrawerLayout.closeDrawers();//收起DrawerLayout
            }
        });
    }

//    private void switchToAbout() {
//        getSupportFragmentManager().beginTransaction().replace(R.id.fl_content, new TabFragment()).commit();
//        mToolbar.setTitle("about fragment");
//    }

    private void switchFragment(int index) {

        if(index==1)
        {

            startActivity(new Intent(TestActivity.this, UploadActivity.class));

        }
        if(index==2)
        {
            startActivity(new Intent(TestActivity.this, UserInfoActivity.class));
        }
        if(index==3)
        {
            sp.setLogin(false);
            sp.put("userName","");

            startActivity(new Intent(getApplicationContext(),AppPageActivity.class));
        }
        if (index==0)
        {
            ContentFragment contentFragment = mFragments.get(index);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fl_content,contentFragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.commit();
        }
//
    }





    private void initDrawerLayout() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,mToolbar, R.string.open, R.string.close);

        mDrawerToggle.syncState();;//同步

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        switchFragment(0);

    }

    private void getProfile() {


        OkHttpClient okHttpClient = new OkHttpClient();


        Request.Builder builder = new Request.Builder();
//        String pic = picUrl;
        Request request = builder.url("http://192.168.1.15:9999/MovieAppServer/DownLoadServlet")
                .addHeader("profile","True")
                .addHeader("filename",sp.get("userName"))
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
                        sp.setimage(bitmap);
                        img.setImageBitmap(bitmap);


                    }
                }));


            }
        });

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            home.addCategory(Intent.CATEGORY_HOME);
            startActivity(home);
            return true;
        }
        return super.onKeyDown(keyCode, event);

    }

    @Override
    public void onResume()
    {  // After a pause OR at startup
        super.onResume();
        doGet();
    }
}
