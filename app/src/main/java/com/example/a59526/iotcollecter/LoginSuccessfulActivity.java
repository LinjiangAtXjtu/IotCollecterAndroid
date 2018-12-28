package com.example.a59526.iotcollecter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginSuccessfulActivity extends AppCompatActivity implements SensorEventListener{
    private SensorManager mSensorManager;
    private int mStepDetector = 0;
    private int mStepCounter = 0;
    private int mStepTemp = -101;
    private int stepSumOfYesterdayInt = 9999;
    private TextView stepText;
    private Context temptext;
    private TextView stepSumOfYesterday;
    private double lng;
    private double lat;
    private String username;
    private final LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            lat = location.getLatitude();//获取纬度
            lng = location.getLongitude();//获取经度
            TextView locationText = (TextView) findViewById (R.id.labText);
            locationText.setText("当前经纬度：（" + Double.toString(lng) + "，" + Double.toString(lat) + "）");
            uploadLocation();
        }

        @Override
        public void onProviderDisabled(String arg0) {
            Log.d("***lat***onPD", "hhh");
        }

        @Override
        public void onProviderEnabled(String arg0) {
            Log.d("***lat***onPE", "hhh");
        }

        @Override
        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
            Log.d("***lat***onstatus", "hhh");
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_successful);

        //显示用户名
        SharedPreferences preferences = getSharedPreferences("data",MODE_PRIVATE);
        TextView welcomeText = (TextView) findViewById (R.id.welcome);
        username = preferences.getString("username", "");
        welcomeText.setText("Welcome " + username);

        //显示获取到的昨日总步数
        stepSumOfYesterday = (TextView) findViewById(R.id.stepSumOfYesterday);
        getStepOfYeserday();
        stepSumOfYesterdayInt = preferences.getInt("stepSumOfYesterday", 0);
        stepSumOfYesterday.setText(Integer.toString(stepSumOfYesterdayInt));

        //获取经纬度模块
        String serviceString = Context.LOCATION_SERVICE;// 获取的是位置服务
        LocationManager locationManager = (LocationManager) getSystemService(serviceString);// 调用getSystemService()方法来获取LocationManager对象
        String provider = judgeProvider(locationManager);
        Location location = locationManager.getLastKnownLocation(provider);// 调用getLastKnownLocation()方法获取当前的位置信息
        lat = location.getLatitude();//获取纬度
        lng = location.getLongitude();//获取经度
        TextView locationText = (TextView) findViewById (R.id.labText);
        locationText.setText("当前经纬度：（" + Double.toString(lng) + "，" + Double.toString(lat) + "）");
        locationManager.requestLocationUpdates(provider, 2000, 10, locationListener);// 产生位置改变事件的条件设定为距离改变10米，时间间隔为2秒，设定监听位置变化

        //获取步数模块
        stepText = (TextView) findViewById(R.id.stepText);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    public void uploadLocation () {
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder().add("user_name", username).add("lng", Double.toString(lng)).add("lat",Double.toString(lat)).build();
        Request request = new Request.Builder().url("http://192.168.99.100:5000/uploadLocation").post(requestBody).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }

    public void uploadStep (int stepsum, int steptoday) {
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder().add("user_name", username).add("step_sum", Integer.toString(stepsum)).add("step_today",Integer.toString(steptoday)).build();
        Request request = new Request.Builder().url("http://192.168.99.100:5000/uploadStep").post(requestBody).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        int suitable = 0;
        List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : sensorList) {
            if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
                suitable += 1;
            } else if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                suitable += 10;
            }
        }
        if (suitable/10>0 && suitable%10>0) {
            mSensorManager.registerListener(this,
                    mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR),
                    SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(this,
                    mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
                    SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            stepText.setText("当前设备不支持计步器，请检查是否存在步行检测传感器和计步器传感器");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            if (event.values[0] == 1.0f) {
                mStepDetector++;
            }
        } else if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            mStepCounter = (int) event.values[0];
        }
        if(mStepCounter > stepSumOfYesterdayInt){
            String desc = String.format("步数为%d", mStepCounter - stepSumOfYesterdayInt);
            stepText.setText(desc);
            if(mStepCounter - mStepTemp > 99){
                uploadStep(mStepCounter, mStepCounter - stepSumOfYesterdayInt);
                mStepTemp = mStepCounter;
            }
        }
        else{
            String desc = String.format("步数为%d", mStepCounter);
            stepText.setText(desc);
            if(mStepCounter - mStepTemp > 99){
                uploadStep(mStepCounter, mStepCounter);
                mStepTemp = mStepCounter;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private String judgeProvider(LocationManager locationManager) {
        List<String> prodiverlist = locationManager.getProviders(true);
        if(prodiverlist.contains(LocationManager.NETWORK_PROVIDER)){
            return LocationManager.NETWORK_PROVIDER;//网络定位
        }else if(prodiverlist.contains(LocationManager.GPS_PROVIDER)) {
            return LocationManager.GPS_PROVIDER;//GPS定位
        }else{
        }
        return null;
    }

    public void getStepOfYeserday() {
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder().add("username", username).build();
        Request request = new Request.Builder().url("http://192.168.99.100:5000/getStepOfYesterday").post(requestBody).build();
        Call call = okHttpClient.newCall(request);
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
                String res = response.body().string();
                if(res.equals("0")){
//                    Looper.prepare();
//                    Toast t = Toast.makeText(temptext,"Sorry baby, you've entered wrong username or password.", Toast.LENGTH_LONG);
//                    t.show();
//                    Looper.loop();
                }
                else{
                    Log.d("****************", res);
                    SharedPreferences.Editor editor = getSharedPreferences("data",MODE_PRIVATE).edit();
                    editor.putInt("stepSumOfYesterday", Integer.parseInt(res));
                    editor.commit();
                }
            }
        });
    }

}
