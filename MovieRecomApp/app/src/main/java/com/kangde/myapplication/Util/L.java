package com.kangde.myapplication.Util;

import android.nfc.Tag;
import android.util.Log;

public class L {

    private static final  String TAG ="oKHTTP";
    private  static  boolean debug=true;
    public  static  void   e (String msg)
    {
        if(debug)
            Log.e(TAG,msg);
    }
}
