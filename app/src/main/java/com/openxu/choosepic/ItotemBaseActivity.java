package com.openxu.choosepic;

import android.app.Activity;
import android.os.Bundle;

import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * User: lizheng<br>
 * Date: 13-7-1<br>
 * Time: 上午10:38<br>
 * Email: kenny.li@itotemdeveloper.com<br>
 * ItotemBaseActivity
 */
public abstract class ItotemBaseActivity extends Activity {

    protected String TAG;
    protected Activity mContext;
    protected ImageLoader imageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        TAG = getClass().getSimpleName();
        imageLoader = ImageLoader.getInstance();
        initView();
        initData();
        setListener();
    }


    protected abstract void initView();

    protected abstract void initData();

    protected void setListener() {
    }

}