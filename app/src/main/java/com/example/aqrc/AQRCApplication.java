package com.example.aqrc;

import android.content.Context;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.example.aqrc.helpers.util.SharedPrefUtil;
import com.example.aqrc.helpers.util.database.DatabaseUtil;

//import com.google.android.gms.ads.MobileAds;
//
//import qrcoba.w3engineers.com.qrcoba.helpers.util.SharedPrefUtil;
//import qrcoba.w3engineers.com.qrcoba.helpers.util.database.DatabaseUtil;

public class AQRCApplication extends MultiDexApplication {

    private static AQRCApplication sInstance;

    public static Context getContext() {
        return sInstance.getApplicationContext();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        SharedPrefUtil.init(getApplicationContext());
        DatabaseUtil.init(getApplicationContext());
       // MobileAds.initialize(this, getString(R.string.admob_app_id));
    }
}
