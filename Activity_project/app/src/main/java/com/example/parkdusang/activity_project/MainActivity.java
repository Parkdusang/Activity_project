package com.example.parkdusang.activity_project;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    SensorManager sm;
    Sensor oriSensor;
    Sensor accSensor;

    TextView tvX = null;
    TextView tvY = null;
    TextView tvZ = null;
    // 방향 센서값을 출력하기 위한 TextView
    TextView tvGX = null;
    TextView tvGY = null;
    TextView tvGZ = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // SensorManager 인스턴스를 가져옴
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        // 가속도 센서
        accSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // 방향 센서
        oriSensor = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);



        tvX = (TextView)findViewById(R.id.accx);
        tvY = (TextView)findViewById(R.id.accy);
        tvZ = (TextView)findViewById(R.id.accz);
        tvGX = (TextView)findViewById(R.id.gx);
        tvGY = (TextView)findViewById(R.id.gy);
        tvGZ = (TextView)findViewById(R.id.gz);


    }

    @Override
    protected void onResume(){
        super.onResume();
        sm.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_UI);
        sm.registerListener(this, oriSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause(){
        super.onPause();
        sm.unregisterListener(this);
        sm.unregisterListener(this);
    }



    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                tvX.setText(String.valueOf(sensorEvent.values[0]));
                tvY.setText(String.valueOf(sensorEvent.values[1]));
                tvZ.setText(String.valueOf(sensorEvent.values[2]));
                break;
            case Sensor.TYPE_GYROSCOPE:
                tvGX.setText(String.valueOf(sensorEvent.values[0]));
                tvGY.setText(String.valueOf(sensorEvent.values[1]));
                tvGZ.setText(String.valueOf(sensorEvent.values[2]));
                break;

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
