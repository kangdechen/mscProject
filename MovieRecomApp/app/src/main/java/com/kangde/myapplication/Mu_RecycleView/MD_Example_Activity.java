package com.kangde.myapplication.Mu_RecycleView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.kangde.myapplication.R;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

public class MD_Example_Activity extends AppCompatActivity {

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
  private List<ContentFragment> mFragments = new ArrayList<>();
//    private TabFragment fragment;
    private ListView mLvList;

    private String[] mMenuTitles;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_md__example_);

        mMenuTitles =new String []{"Upload","User","Log out"};
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawlayout);
        mLvList = (ListView) findViewById(R.id.lv_list);
        initToolBar();
        initMenuTitles();
        initFragments();
        initDrawerLayout();






    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_md__example, menu);
        return true;
    }

    private void initToolBar() {


        mToolbar.setNavigationIcon(R.drawable.ic_dashboard_black_24dp);//设置导航的图标
        mToolbar.setLogo(R.mipmap.ic_launcher);//设置logo

        mToolbar.setTitle("title");//设置标题
        mToolbar.setSubtitle("subtitle");//设置子标题

        mToolbar.setTitleTextColor(getResources().getColor(android.R.color.white));//设置标题的字体颜色
        mToolbar.setSubtitleTextColor(getResources().getColor(android.R.color.white));//设置子标题的字体颜色

        //设置右上角的填充菜单
        mToolbar.inflateMenu(R.menu.menu_md__example);
        //设置导航图标的点击事件
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MD_Example_Activity.this, "菜单", Toast.LENGTH_SHORT).show();
            }
        });
        //设置右侧菜单项的点击事件
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {

            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                String tip = "";
                switch (id) {
                    case R.id.action_search:
                        tip = "搜索";
                        break;
//                    case R.id.action_add:
//                        tip = "添加";
//                       // switchToAbout();
//                        break;
//                    case R.id.action_setting:
//                        tip = "设置";
//                        break;
//                    case R.id.action_help:
//                        tip = "帮助";
//                        break;
                }
                Toast.makeText(MD_Example_Activity.this, tip, Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        ;
    }

    private void initFragments() {
        ContentFragment fragment1 = new ContentFragment();
        Bundle bundle1 = new Bundle();
        bundle1.putString(ContentFragment.TEXT,"test");
        fragment1.setArguments(bundle1);


        ContentFragment fragment2 = new ContentFragment();
        Bundle bundle2 = new Bundle();
        bundle2.putString(ContentFragment.TEXT,"test2");
        fragment2.setArguments(bundle2);


        mFragments.add(fragment1);
        mFragments.add(fragment2);
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

        }
//        ContentFragment contentFragment = mFragments.get(index);
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        FragmentTransaction transaction = fragmentManager.beginTransaction();
//        transaction.replace(R.id.fl_content,contentFragment);
//        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//        transaction.commit();
    }





    private void initDrawerLayout() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,mToolbar, R.string.open, R.string.close);

        mDrawerToggle.syncState();;//同步

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        switchFragment(0);
    }
}


