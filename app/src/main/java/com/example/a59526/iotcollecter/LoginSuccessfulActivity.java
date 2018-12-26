package com.example.a59526.iotcollecter;

import android.content.Context;
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

import java.util.List;

public class LoginSuccessfulActivity extends AppCompatActivity implements SensorEventListener{
    private SensorManager mSensorManager;
    private int mStepDetector = 0;
    private int mStepCounter = 0;
    private TextView stepText;
    private final LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            double lat = location.getLatitude();//获取纬度
            double lng = location.getLongitude();//获取经度
            TextView locationText = (TextView) findViewById (R.id.labText);
            locationText.setText("当前经纬度：（" + Double.toString(lng) + "，" + Double.toString(lat) + "）");
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

        Log.d("***lat***", "************************************************************************");

        //获取经纬度模块
        String serviceString = Context.LOCATION_SERVICE;// 获取的是位置服务
        LocationManager locationManager = (LocationManager) getSystemService(serviceString);// 调用getSystemService()方法来获取LocationManager对象
        String provider = judgeProvider(locationManager);
        Location location = locationManager.getLastKnownLocation(provider);// 调用getLastKnownLocation()方法获取当前的位置信息
        double lat = location.getLatitude();//获取纬度
        double lng = location.getLongitude();//获取经度
        TextView locationText = (TextView) findViewById (R.id.labText);
        locationText.setText("当前经纬度：（" + Double.toString(lng) + "，" + Double.toString(lat) + "）");
        locationManager.requestLocationUpdates(provider, 2000, 10, locationListener);// 产生位置改变事件的条件设定为距离改变10米，时间间隔为2秒，设定监听位置变化

        //获取步数模块
        stepText = (TextView) findViewById(R.id.stepText);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
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
        String desc = String.format("步数为%d", mStepCounter);
        stepText.setText(desc);
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

}
