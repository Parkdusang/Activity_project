package com.example.parkdusang.activity_project;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener{
    SensorManager sm;
    Sensor oriSensor;
    Sensor accSensor;
    Sensor pressSensor;
    TextView tvX = null;
    TextView tvY = null;
    TextView tvZ = null;
    // 방향 센서값을 출력하기 위한 TextView
    TextView tvGX = null;
    TextView tvGY = null;
    TextView tvGZ = null;
    TextView state = null;
    TextView air_pressure = null;
    TextView real_STATE = null;

    Button btn_run, btn_sitdown,btn_walk,btn_lie,btn_upstair,btn_downstair ,start_Btn ,real_test;

    int set_mode =0 ,i=0;

    Thread getActivityState,pustData;

    Boolean break_another_mode=false, realTimeTest = false;

    InputStream is = null;

    float save_pressure[] = new float[16];
    float pre_avgPressure=0; // 이전의
    float vari_Pressure = 0; //변화량
    float total_sum =0;

    MultiLayer ML;

    String RX,RY,RZ,RGX,RGY,RGZ;
    String line = null;
    String url ="http://pesang72.cafe24.com/handisoft/handisoft_get_sensorData.php";
    String url2 = "http://pesang72.cafe24.com/handisoft/real_time_data.php";
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
        // 기압 센서
        pressSensor = sm.getDefaultSensor(Sensor.TYPE_PRESSURE);

        ML = new MultiLayer();
        tvX = (TextView)findViewById(R.id.accx);
        tvY = (TextView)findViewById(R.id.accy);
        tvZ = (TextView)findViewById(R.id.accz);
        tvGX = (TextView)findViewById(R.id.gx);
        tvGY = (TextView)findViewById(R.id.gy);
        tvGZ = (TextView)findViewById(R.id.gz);
        state = (TextView)findViewById(R.id.now_state);
        air_pressure = (TextView)findViewById(R.id.air_press);
        real_STATE = (TextView)findViewById(R.id.realstate);

        real_STATE.setVisibility(View.INVISIBLE);

        btn_run = (Button)findViewById(R.id.activity_run); // 달리기
        btn_sitdown = (Button)findViewById(R.id.activity_sitdown); // 앉기
        btn_walk = (Button)findViewById(R.id.activity_Towalk);
        btn_lie = (Button)findViewById(R.id.activity_lie);
        btn_upstair = (Button)findViewById(R.id.activity_Upstairs);
        btn_downstair = (Button)findViewById(R.id.activity_Downstairs);

        start_Btn = (Button)findViewById(R.id.start_activity);

        real_test = (Button)findViewById(R.id.realTimeTest);

        btn_run.setOnClickListener(this);
        btn_sitdown.setOnClickListener(this);
        btn_walk.setOnClickListener(this);
        btn_lie.setOnClickListener(this);
        btn_upstair.setOnClickListener(this);
        btn_downstair.setOnClickListener(this);

        start_Btn.setOnClickListener(this);
        real_test.setOnClickListener(this);


    }

    @Override
    protected void onResume(){
        super.onResume();
        sm.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_UI);
        sm.registerListener(this, oriSensor, SensorManager.SENSOR_DELAY_UI);
        sm.registerListener(this, pressSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause(){
        super.onPause();
        break_another_mode = false;
        set_mode = 0;
        start_Btn.setText("데이터 수집 시작!");
        state.setText("수집모드 : 대기중");
        vari_Pressure = 0;
        pre_avgPressure = 0;
        sm.unregisterListener(this);
        sm.unregisterListener(this);
        sm.unregisterListener(this);

        if(realTimeTest){
            pustData = new Thread(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    //pushRealData(0);

                }
            });
            pustData.start();


            realTimeTest = false;
            real_test.setText("실시간 데이터 테스트!");
        }
        real_STATE.setVisibility(View.INVISIBLE);
    }




    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                tvX.setText("X : "+String.valueOf(sensorEvent.values[0]));
                tvY.setText("Y : "+String.valueOf(sensorEvent.values[1]));
                tvZ.setText("Z : "+String.valueOf(sensorEvent.values[2]));
                RX =String.valueOf(sensorEvent.values[0]);
                RY =String.valueOf(sensorEvent.values[1]);
                RZ =String.valueOf(sensorEvent.values[2]);
                if(RX.length() >7 && !RX.contains("E")){
                    RX = RX.substring(0,6);
                }
                if(RY.length() >7 && !RY.contains("E")){
                    RY = RY.substring(0,6);
                }
                if(RZ.length() >7 && !RZ.contains("E")){
                    RZ = RZ.substring(0,6);
                }
                break;
            case Sensor.TYPE_GYROSCOPE:
                tvGX.setText("GX : "+String.valueOf(sensorEvent.values[0]));
                tvGY.setText("GY : "+String.valueOf(sensorEvent.values[1]));
                tvGZ.setText("GZ : "+String.valueOf(sensorEvent.values[2]));

                RGX = String.valueOf(sensorEvent.values[0]);
                if(RGX.length() >7 && !RGX.contains("E")){
                    RGX = RGX.substring(0,6);
                }
                RGY = String.valueOf(sensorEvent.values[1]);
                if(RGY.length() >7 && !RGY.contains("E")){
                    RGY = RGY.substring(0,6);
                }
                RGZ = String.valueOf(sensorEvent.values[2]);
                if(RGZ.length() >7 && !RGZ.contains("E")){
                    RGZ = RGZ.substring(0,6);
                }
                break;
            case Sensor.TYPE_PRESSURE:
                save_pressure[i] = sensorEvent.values[0];
                i++;

                if(i == 16){
                    i = 0;
                    for(int j = 0 ; j<16 ; j++)
                        total_sum += save_pressure[j];
                    if(pre_avgPressure == 0){
                        air_pressure.setText("기압 : " + total_sum/16);
                        pre_avgPressure = total_sum/16;
                        total_sum = 0;
                    }
                    else{
                        air_pressure.setText("기압 : " + total_sum/16);
                        vari_Pressure = pre_avgPressure - total_sum/16;
                        pre_avgPressure = total_sum/16;
                        total_sum = 0;
                    }

                }
                break;


        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.activity_Towalk:
                if(!break_another_mode) {
                    if(!realTimeTest) {
                        set_mode = 1;
                        state.setText("수집모드 : 걷기");
                    }
                }
                break;
            case R.id.activity_run:
                if(!break_another_mode) {
                    if(!realTimeTest) {
                        set_mode = 2;
                        state.setText("수집모드 : 달리기");
                    }

                }
                break;
            case R.id.activity_sitdown:
                if(!break_another_mode) {
                    if(!realTimeTest) {
                        set_mode = 3;
                        state.setText("수집모드 : 앉기");
                    }
                }
                break;
            case R.id.activity_lie:
                if(!break_another_mode) {
                    if(!realTimeTest) {
                        set_mode = 4;
                        state.setText("수집모드 : 눕기");
                    }
                }
                break;
            case R.id.activity_Upstairs:
                if(!break_another_mode){
                    if(!realTimeTest) {
                        state.setText("수집모드 : 계단오르기");
                        set_mode = 5;
                    }
                }
                break;
            case R.id.activity_Downstairs:
                if(!break_another_mode) {
                    if(!realTimeTest) {
                        state.setText("수집모드 : 계단 내려가기");
                        set_mode = 6;
                    }
                }
                break;
            case R.id.realTimeTest:
                if(!realTimeTest) {
                    if(!break_another_mode){
                        pustData = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                //pushRealData(1);
                                pushRealData_inSmartPhone();
                            }
                        });
                        pustData.start();
                        real_STATE.setVisibility(View.VISIBLE);
                        realTimeTest= true;
                        real_test.setText("실시간 테스트 종료");
                    }
                }
                else{
                    if(!break_another_mode){
                        realTimeTest = false;
                        real_test.setText("실시간 데이터 테스트!");
                        pustData = new Thread(new Runnable() {
                            @Override
                            public void run() {
                               // pushRealData(0);
                            }
                        });
                        pustData.start();
                        real_STATE.setVisibility(View.INVISIBLE);
                        realTimeTest = false;
                        real_test.setText("실시간 데이터 테스트!");
                    }
                }
                break;
            case R.id.start_activity:
                if(!break_another_mode){
                    if(!realTimeTest) {
                        if (set_mode != 0) {
                            getActivityState = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    // TODO Auto-generated method stub
                                    checkState();

                                }
                            });
                            getActivityState.start();
                            break_another_mode = true;
                            start_Btn.setText("데이터 수집 중지 (클릭)");
                        } else {
                            Toast.makeText(getApplicationContext(), "모드설정이 되어있지 않습니다. ", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                else{
                    if(!realTimeTest) {
                        break_another_mode = false;
                        set_mode = 0;
                        start_Btn.setText("데이터 수집 시작!");
                        state.setText("수집모드 : 대기중");
                        vari_Pressure = 0;
                        pre_avgPressure = 0;
                        Toast.makeText(getApplicationContext(), "데이터 수집을 종료합니다. ", Toast.LENGTH_SHORT).show();
                    }
                }



                break;
            default:
                Toast.makeText(getApplicationContext(),"잘못된 버튼인데요? ",Toast.LENGTH_SHORT).show();
                break;


        }
    }
    public void pushRealData_inSmartPhone(){
        int ST;
        while (true) {
            if (realTimeTest) {
                if(vari_Pressure > 0)
                    ST = ML.checkState(RX,RY,RZ,RGX,RGY,RGZ, (double)((vari_Pressure * 100) * (vari_Pressure * 100)) );
                else
                    ST = ML.checkState(RX,RY,RZ,RGX,RGY,RGZ, (double)( (-1)*(vari_Pressure * 100) * (vari_Pressure * 100)) );
                if(ST == 0){
                    runOnUiThread(new Runnable() {
                        public void run() {
                            // 메시지 큐에 저장될 메시지의 내용
                            real_STATE.setText("실시간 상태 : 걷기");
                        }
                    });
                }
                else if(ST == 1){
                    runOnUiThread(new Runnable() {
                        public void run() {
                            // 메시지 큐에 저장될 메시지의 내용
                            real_STATE.setText("실시간 상태 : 달리기");
                        }
                    });
                }
                else if(ST == 2){
                    runOnUiThread(new Runnable() {
                        public void run() {
                            // 메시지 큐에 저장될 메시지의 내용
                            real_STATE.setText("실시간 상태 : 앉기");
                        }
                    });
                }
                else if(ST == 3){
                    runOnUiThread(new Runnable() {
                        public void run() {
                            // 메시지 큐에 저장될 메시지의 내용
                            real_STATE.setText("실시간 상태 : 눕기");
                        }
                    });

                }

                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void pushRealData(int state){
        Log.e("pass 1", "connection pushdata "+ state);
        if(state == 0){
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("txX", RX));
            nameValuePairs.add(new BasicNameValuePair("txY", RY));
            nameValuePairs.add(new BasicNameValuePair("txZ", RZ));
            nameValuePairs.add(new BasicNameValuePair("txGX", RGX));
            nameValuePairs.add(new BasicNameValuePair("txGY", RGY));
            nameValuePairs.add(new BasicNameValuePair("txGZ", RGZ));
            nameValuePairs.add(new BasicNameValuePair("state", state + ""));
            nameValuePairs.add(new BasicNameValuePair("press", "" + (vari_Pressure * 100) * (vari_Pressure * 100)));
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(url2);
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
                HttpResponse response = httpclient.execute(httppost);
                Log.e("pass 1", "connection success ");
            } catch (Exception e) {
                Log.e("Fail 1", e.toString());

            }
        }
        else {

            Log.e("pass 1", "connection else ");
            while (true) {
                if (realTimeTest) {
                    ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("txX", RX));
                    nameValuePairs.add(new BasicNameValuePair("txY", RY));
                    nameValuePairs.add(new BasicNameValuePair("txZ", RZ));
                    nameValuePairs.add(new BasicNameValuePair("txGX", RGX));
                    nameValuePairs.add(new BasicNameValuePair("txGY", RGY));
                    nameValuePairs.add(new BasicNameValuePair("txGZ", RGZ));
                    nameValuePairs.add(new BasicNameValuePair("state", state + ""));
                    nameValuePairs.add(new BasicNameValuePair("press", "" + (vari_Pressure * 100) * (vari_Pressure * 100)));

                    Log.i("TAG", "2: ");
                    try {
                        HttpClient httpclient = new DefaultHttpClient();
                        HttpPost httppost = new HttpPost(url2);
                        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
                        HttpResponse response = httpclient.execute(httppost);
                        Log.e("pass 1", "connection success ");
                    } catch (Exception e) {
                        Log.e("Fail 1", e.toString());

                    }
                }

                try {
                    Thread.sleep(1200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void checkState(){
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while(true){
            if(break_another_mode){

                if(vari_Pressure > 0)
                    PushData(url,RX,RY,RZ,RGX,RGY,RGZ,set_mode ,(vari_Pressure*100)*(vari_Pressure*100));
                else
                    PushData(url,RX,RY,RZ,RGX,RGY,RGZ,set_mode , (-1)*(vari_Pressure*100)*(vari_Pressure*100));

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void PushData(String url, String x,String y,String z,String gx,String gy,String gz,int state,float pressu) {


        class GetDataJSON extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {
                String uri = params[0];
                String tvX = params[1];
                String tvY = params[2];
                String tvZ = params[3];
                String tvGX = params[4];
                String tvGY = params[5];
                String tvGZ = params[6];
                String state = params[7];
                String press = params[8];
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("txX", tvX));
                nameValuePairs.add(new BasicNameValuePair("txY", tvY));
                nameValuePairs.add(new BasicNameValuePair("txZ", tvZ));
                nameValuePairs.add(new BasicNameValuePair("txGX", tvGX));
                nameValuePairs.add(new BasicNameValuePair("txGY", tvGY));
                nameValuePairs.add(new BasicNameValuePair("txGZ", tvGZ));
                nameValuePairs.add(new BasicNameValuePair("state", state));
                nameValuePairs.add(new BasicNameValuePair("press", press));

                //Log.i("TAG", "2: ");
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(uri);
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();

                    Log.e("pass 1", "connection success ");
                } catch (Exception e) {
                    Log.e("Fail 1", e.toString());

                }


                try {
                    String result;
                    BufferedReader reader = new BufferedReader
                            (new InputStreamReader(is, "UTF-8"));
                    StringBuilder sb = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();

                    Log.e("1223", state + " "+ result);
                    return state+" "+sb.toString().trim();
                } catch (Exception e) {
                    Log.e("Fail 2", e.toString());
                    return null;
                }

            }

            @Override
            protected void onPostExecute(String result) {
                //Log.e("total", result);


            }
        }


        GetDataJSON g = new GetDataJSON();
        g.execute(url, x, y ,z , gx , gy, gz,state+"",pressu+""); // == join

    }

}
