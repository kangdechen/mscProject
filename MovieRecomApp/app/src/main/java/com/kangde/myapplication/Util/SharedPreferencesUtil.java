package com.kangde.myapplication.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

/**
 * Created by smile on 02/03/2019.
 */

public class SharedPreferencesUtil {
    private static final String TAG = "TAG";
    private static  boolean login  = false;

    private static SharedPreferences mPreferences;
    private static SharedPreferences.Editor mEditor;
    private static SharedPreferencesUtil mSharedPreferencesUtil;
    private final Context context;
    private static Bitmap b;
    private String username;

    public SharedPreferencesUtil(Context context) {
        this.context = context.getApplicationContext();
        mPreferences =   this.context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        mEditor = mPreferences.edit();
    }


    public static SharedPreferencesUtil getInstance(Context context) {
        if (mSharedPreferencesUtil ==null){
            mSharedPreferencesUtil =new SharedPreferencesUtil(context);
        }
        return  mSharedPreferencesUtil;
    }

    public void setimage (Bitmap bitmap)

    {
        b =bitmap;
    }
    public   Bitmap getImage ()
    {
        return b;
    }

    /**
     * test login
     * @return
     */
    public boolean isLogin() {
        return login;
    }
    public String getname ()
    {
        return  username;
    }
    /**
     *
     * @param value
     */
    public void setLogin(boolean value ) {
        login=value;

    }

    //--------私有方法
    public void put(String key, String value) {
        mEditor.putString(key,value);
        mEditor.commit();
    }

    public void putBoolean(String key, boolean value) {
        mEditor.putBoolean(key,value);
        mEditor.commit();
    }

    public String get(String key) {
        return mPreferences.getString(key,"");
    }

    private boolean getBoolean(String key, boolean defaultValue) {
        return mPreferences.getBoolean(key,defaultValue);
    }


    private void putInt(String key, int value) {
        mEditor.putInt(key,value);
        mEditor.apply();
    }

    private int getInt(String key, int defaultValue) {
        return mPreferences.getInt(key,defaultValue);
    }


    //--------end 私有方法
}
