package com.pmavio.cheatsheetdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.pmavio.cheatsheetdemo.utils.CheatSheet;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Created by Mavio on 2016/4/14.
 */
public class BaseActivity extends AppCompatActivity{

    protected EventBus eventBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventBus = EventBus.getDefault();
        eventBus.register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        eventBus.unregister(this);
    }

    public void startActivity(Class<? extends Activity> c) {
        super.startActivity(new Intent(this, c));
    }

    public void startActivityForResult(Class<? extends Activity> c, int requestCode) {
        super.startActivityForResult(new Intent(this, c), requestCode);
    }

    @Subscribe
    public void onGetSheet(CheatSheet sheet){
//        sheet.getWith(this); 此方法有问题，需要改进
        if(sheet.checkSender(sheet)){
            return;
        }
    }

    @Subscribe(sticky = true)
    public void onGetSheetSticky(CheatSheet sheet){
//        sheet.getWith(this); 此方法有问题，需要改进
        if(sheet.checkSender(sheet)){
            return;
        }
    }
}
