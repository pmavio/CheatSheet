package com.pmavio.cheatsheetdemo;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.pmavio.cheatsheetdemo.globals.CheatCodes;
import com.pmavio.cheatsheetdemo.utils.CheatSheet;

public class MainActivity extends BaseActivity implements CheatCodes{

    public static final int REQUEST_UPDATE_ON_RETURN = 55555;

    int valueInt;
    double valueDouble;
    String valueStr = "";

    EditText et_int;
    EditText et_double;
    EditText et_str;

    CheatSheet sheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initWidgets();


        //更新控件内容
        if(sheet != null)
            updateWidgets();
    }

    protected void initWidgets(){
        et_int = (EditText) findViewById(R.id.et_int);
        et_double = (EditText) findViewById(R.id.et_double);
        et_str = (EditText) findViewById(R.id.et_str);
    }

    protected void updateWidgets(){
        et_int.setText(valueInt + "");
        et_double.setText(valueDouble + "");
        et_str.setText(valueStr);
    }

    protected void updateValues(){
        String s = et_int.getText().toString();
        if(!TextUtils.isEmpty(s)){
            try {
                valueInt = Integer.parseInt(s);
            }catch (NumberFormatException e){
                valueInt = 0;
            }
        }

        s = et_double.getText().toString();
        if(!TextUtils.isEmpty(s)){
            try{
                valueDouble = Double.parseDouble(s);
            }catch(NumberFormatException e){
                valueDouble = 0;
            }
        }

        valueStr = et_str.getText().toString();
    }

    public void next(View v){
        updateValues();

        //发送一个新的CheatSheet对象，向构造函数依次传入code、sender，以及需要发送的对象
        eventBus.postSticky(new CheatSheet(CODE_POST_STICKY, this, valueInt, valueDouble, valueStr) {
        });

        startActivityForResult(Main2Activity.class, REQUEST_UPDATE_ON_RETURN);
    }

    @Override
    public void onGetSheet(CheatSheet sheet) {
        super.onGetSheet(sheet);
        //在使用eventBus.postStick时，因为当前对象(MainActivity)已经被注册至eventBus,所以会先发送至此方法，需要判断发送者不是当前对象，除非确实是发送至当前对象的
        if(sheet.checkSender(this)){
            return;
        }

        if(sheet.checkCode(CODE_POST_NORMAL)) {
            //逐个取出对象
            valueInt = sheet.get();
            valueDouble = sheet.get();
            valueStr = sheet.get();

            updateWidgets();
        }
    }

    //使用onGetSheet代替
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(requestCode == REQUEST_UPDATE_ON_RETURN && resultCode == RESULT_OK){
//            updateWidgets();
//        }
//    }
}
