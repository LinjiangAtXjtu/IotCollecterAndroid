package com.example.a59526.iotcollecter;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText userName;
    private EditText password;
    private Context temptext;
    private Button goToLoginSucceful;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        temptext = this;

        //绑定文本框和按钮功能
        userName = (EditText) findViewById(R.id.userName);
        password = (EditText) findViewById(R.id.password);
        Button goToLoginSuccessful = (Button) findViewById(R.id.goToLoginSuccessful);
        goToLoginSuccessful.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }

    //登录到服务器
    public void login(){
        //1.创建OkHttpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();
        //2.通过new FormBody()调用build方法,创建一个RequestBody,可以用add添加键值对
        RequestBody requestBody = new FormBody.Builder().add("username", userName.getText().toString()).add("pwd",password.getText().toString()).build();
        //3.创建Request对象，设置URL地址，将RequestBody作为post方法的参数传入
        Request request = new Request.Builder().url("http://192.168.99.100:5000/login").post(requestBody).build();
        //4.创建一个call对象,参数就是Request请求对象
        Call call = okHttpClient.newCall(request);
        //5.请求加入调度,重写回调方法
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Looper.prepare();
                Toast t = Toast.makeText(temptext,"Sorry baby, please try again.", Toast.LENGTH_LONG);
                t.show();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                switch(response.body().string())
                {
                    case "1" : {
                        Intent intent = new Intent(LoginActivity.this,LoginSuccessfulActivity.class);
                        startActivity(intent);
                        break;
                    }
                    case "0" : {
                        Looper.prepare();
                        Toast t = Toast.makeText(temptext,"Sorry baby, you've entered wrong username or password.", Toast.LENGTH_LONG);
                        t.show();
                        Looper.loop();
                    }
                }
            }
        });
    }
}
