package com.pmavio.cheatsheetdemo;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.pmavio.cheatsheetdemo.globals.CheatCodes;
import com.pmavio.cheatsheetdemo.utils.CheatSheet;

public class Main2Activity extends BaseActivity implements CheatCodes, CheatSheet.AnswerSheet{

    public static final int REQUEST_UPDATE_ON_RETURN = 55555;

    //questionNo需按照传递顺序从0递增
    @CheatSheet.HardQuestion(code = CODE_POST_STICKY, questionNo = 0)
    int valueInt;
    @CheatSheet.HardQuestion(code = CODE_POST_STICKY, questionNo = 1)
    double valueDouble;
    @CheatSheet.HardQuestion(code = CODE_POST_STICKY, questionNo = 2)
    String valueStr = "";

    EditText et_int;
    EditText et_double;
    EditText et_str;
    Button bt_next;

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
        bt_next = (Button) findViewById(R.id.bt_next);

        bt_next.setText("回传参数");
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

    @Override
    public void onBackPressed() {
        updateValues();

        //发送一个新的CheatSheet对象，向构造函数依次传入code、sender，以及需要发送的对象
        eventBus.post(new CheatSheet(CODE_POST_NORMAL, this, valueInt, valueDouble, valueStr) {
        });
        super.onBackPressed();
    }

    public void next(View v){
        onBackPressed();
    }

    @Override
    public void onGetSheetSticky(CheatSheet sheet) {
        super.onGetSheetSticky(sheet);
        //在使用eventBus.postStick时，因为当前对象(Main2Activity)已经被注册至eventBus,所以会先发送至此方法，需要判断发送者不是当前对象，除非确实是发送至当前对象的
        if(sheet.checkSender(this)){
            return;
        }

        if(sheet.checkCode(CODE_POST_STICKY)){
            this.sheet = sheet;
//            valueInt = sheet.get();
//            valueDouble = sheet.get();
//            valueStr = sheet.get();
            //使用如下方法替代上面的手动获得对象
            sheet.getWith(this);

            //回收sheet，不让其继续传递
            sheet.recycle();
        }
    }
}
